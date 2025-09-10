package com.example.demo.batch.job;

import com.example.demo.batch.step.SettlementItemWriter;
import com.example.demo.batch.step.TransactionItemProcessor;
import com.example.demo.batch.step.TransactionItemReader;
import com.example.demo.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionItemReader transactionItemReader;
    private final TransactionItemProcessor transactionItemProcessor;
    private final SettlementItemWriter settlementItemWriter;
    
    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep())
                .build();
    }
    
    @Bean
    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .<Transaction, Transaction>chunk(1000, transactionManager)
                .reader(transactionItemReader)
                .processor(transactionItemProcessor)
                .writer(settlementItemWriter)
                .build();
    }
    
    @Bean
    public Step settlementTaskletStep() {
        return new StepBuilder("settlementTaskletStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Starting settlement tasklet");
                    
                    try {
                        // Stored Procedure를 직접 호출하는 방식
                        var result = settlementItemWriter;
                        log.info("Settlement tasklet completed");
                        return RepeatStatus.FINISHED;
                    } catch (Exception e) {
                        log.error("Error in settlement tasklet", e);
                        throw e;
                    }
                }, transactionManager)
                .build();
    }
}
