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
public class PaginatedBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApiClient apiClient;
    private final DataSource dataSource;

    @Bean
    public ItemReader<Comments> paginatedItemReader() {
        return new PaginatedApiItemReader<Comments>(apiClient, 50, Comments.class);
    }

    @Bean
    public ItemProcessor<Comments, Comments> paginatedItemProcessor() {
        return item -> {
            Comments newItem = new Comments();
            newItem.setId(item.getId());
            newItem.setPostId(item.getPostId());
            newItem.setBody(item.getBody());
            newItem.setName("TESTX-" + item.getName());
            newItem.setEmail(item.getEmail());
            return newItem;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Comments> paginatedJdbcBatchItemWriter(DataSource dataSource) {
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
    public Step paginatedStep() {
        return new StepBuilder("paginatedStep", jobRepository)
                .<Comments, Comments>chunk(50, transactionManager)
                .reader(paginatedItemReader())
                .processor(paginatedItemProcessor())
                .writer(paginatedJdbcBatchItemWriter(dataSource))
                .listener(new LoggingChunkListener())
                .build();
    }

    @Bean
    public Job paginatedJob() {
        return new JobBuilder("paginatedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paginatedStep())
                .build();
    }

}
