package com.nayak.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/batch")
public class BatchController {
    private final JobLauncher jobLauncher;
    private final Job nonPaginatedJob;
    private final Job paginatedJob;

    @GetMapping("/non-paginated")
    public ResponseEntity<String> triggerJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(nonPaginatedJob, jobParameters);
            return ResponseEntity.ok("Non Paginated Job started with ID: " + execution.getId());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting job: " + e.getMessage());
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<String> paginatedJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(paginatedJob, jobParameters);
            return ResponseEntity.ok("Paginated Job started with ID: " + execution.getId());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting job: " + e.getMessage());
        }
    }
}
