package com.example.demo.batch.scheduler;

import com.example.demo.settlementhistory.service.SettlementHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    private final SettlementHistoryService settlementHistoryService;
    
    /**
     * 매일 새벽 4시에 정산 배치 작업을 실행
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void runSettlementJob() {
        log.info("Starting scheduled settlement job at 4:00 AM");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("trigger", "scheduled")
                    .toJobParameters();
            
            jobLauncher.run(settlementJob, jobParameters);
            log.info("Scheduled settlement job completed successfully");
            
        } catch (Exception e) {
            log.error("Error occurred during scheduled settlement job", e);
        }
    }
    
    /**
     * 매일 새벽 4시 5분에 Stored Procedure를 직접 호출하는 정산 실행
     */
    @Scheduled(cron = "0 5 4 * * *")
    public void runSettlementProcedure() {
        log.info("Starting scheduled settlement procedure at 4:05 AM");
        
        try {
            var result = settlementHistoryService.executeSettlement();
            log.info("Scheduled settlement procedure completed - Processed: {}, Errors: {}", 
                    result.getProcessedCount(), result.getErrorCount());
            
        } catch (Exception e) {
            log.error("Error occurred during scheduled settlement procedure", e);
        }
    }
}
