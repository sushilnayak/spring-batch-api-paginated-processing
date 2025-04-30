package com.nayak.batch.batch;

import com.nayak.batch.api.ApiClient;
import com.nayak.batch.config.LoggingChunkListener;
import com.nayak.batch.model.Comments;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class NonPaginatedBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApiClient apiClient;
    private final DataSource dataSource;

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
    public Job nonPaginatedJob() {
        return new JobBuilder("chunkedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(nonPaginatedApiStep())
                .build();
    }
}
