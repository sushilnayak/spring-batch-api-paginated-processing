package com.nayak.ecommerce.springbatchapipaginatedprocessing.config;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class LoggingChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        System.out.println("[ChunkListener] Starting a new chunk...");
    }

    @Override
    public void afterChunk(ChunkContext context) {
        System.out.println("[ChunkListener] Successfully completed a chunk.");
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        System.out.println("[ChunkListener] Error occurred in chunk.");
    }
}
