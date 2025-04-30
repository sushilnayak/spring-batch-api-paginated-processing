package com.nayak.batch.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.*;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class NonPaginatedApiItemReader<T> implements ItemReader<T>, ItemStream {
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
