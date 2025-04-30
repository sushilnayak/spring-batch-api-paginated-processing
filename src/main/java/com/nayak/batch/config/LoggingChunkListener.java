package com.nayak.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("[LoggingChunkListener] Starting a new chunk...");
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("[LoggingChunkListener] Successfully completed a chunk.");
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        log.info("[LoggingChunkListener] Error occurred in chunk.");
    }
}
