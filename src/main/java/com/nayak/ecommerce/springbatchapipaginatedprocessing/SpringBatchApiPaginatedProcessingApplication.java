package com.nayak.ecommerce.springbatchapipaginatedprocessing;

import com.nayak.ecommerce.springbatchapipaginatedprocessing.api.ApiClient;
import com.nayak.ecommerce.springbatchapipaginatedprocessing.config.LoggingChunkListener;
import com.nayak.ecommerce.springbatchapipaginatedprocessing.model.Comments;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@SpringBootApplication
public class SpringBatchApiPaginatedProcessingApplication {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApiClient apiClient;
    private final DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApiPaginatedProcessingApplication.class, args);
    }

    @Bean
    public ItemReader<Comments> nonPaginatedApiItemReader(ApiClient apiClient) {
        return new NonPaginatedApiItemReader<>(apiClient::fetchAllRecords);
    }

    @Bean
    public ItemProcessor<Comments, Comments> processor() {
        return dto -> {
            Comments entity = new Comments();
            entity.setId(dto.getId());
            entity.setPostId(dto.getPostId());
            entity.setEmail(dto.getEmail());
            entity.setBody(dto.getBody());
            entity.setName("TEST-" + dto.getName());
            return entity;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Comments> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Comments>()
                .dataSource(dataSource)
                .sql("""
                            INSERT INTO comments (id, post_id, email, body, name)
                            VALUES (:id, :postId, :email, :body, :name)
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public Step nonPaginatedApiStep() {
        return new StepBuilder("nonPaginatedApiStep", jobRepository)
                .<Comments, Comments>chunk(5, transactionManager)
                .reader(nonPaginatedApiItemReader(apiClient))
                .processor(processor())
                .writer(jdbcBatchItemWriter(dataSource))
                .listener(new LoggingChunkListener())
                .build();
    }

    @Bean
    public Job chunkedJob() {
        return new JobBuilder("chunkedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(nonPaginatedApiStep())
                .build();
    }

}


@RequiredArgsConstructor
class NonPaginatedApiItemReader<T> implements ItemReader<T>, ItemStream {
    private final Supplier<List<T>> apiFetcher;
    private Iterator<T> iterator;
    private boolean alreadyFetched = false;


    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!alreadyFetched) {
            List<T> data = apiFetcher.get();
            iterator = data.iterator();
            alreadyFetched = true;
        }
        return iterator != null && iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        alreadyFetched = executionContext.get("alreadyFetched", Boolean.class, Boolean.FALSE);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("alreadyFetched", alreadyFetched);
    }

    @Override
    public void close() throws ItemStreamException {
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/batch")
class BatchController {
    private final JobLauncher jobLauncher;
    private final Job job;

    @GetMapping
    public ResponseEntity<String> triggerJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(job, jobParameters);
            return ResponseEntity.ok("Job started with ID: " + execution.getId());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting job: " + e.getMessage());
        }
    }
}

