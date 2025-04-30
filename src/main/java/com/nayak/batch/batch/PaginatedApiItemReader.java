package com.nayak.batch.batch;

import com.nayak.batch.api.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.*;

import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class PaginatedApiItemReader<T> implements ItemReader<T>, ItemStream {
    private final ApiClient apiClient;
    private final int pageSize;
    private final Class<T> clazz;
    private int currentPage = 0;
    private Iterator<T> currentBatchIterator;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentBatchIterator == null || !currentBatchIterator.hasNext()) {

            List<T> nextBatch = apiClient.fetchPage(currentPage, pageSize, clazz);
            if (nextBatch == null || nextBatch.isEmpty()) {
                return null;
            }
            currentBatchIterator = nextBatch.iterator();

            if (currentPage == 0) currentPage = currentPage + pageSize + 1;
            else currentPage = currentPage + pageSize;
        }
        return currentBatchIterator.hasNext() ? currentBatchIterator.next() : null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        currentPage = executionContext.get("currentPage", Integer.class, 0);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("currentPage", currentPage);
    }

    @Override
    public void close() throws ItemStreamException {
    }
}
