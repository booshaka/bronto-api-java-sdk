package com.bronto.api;

import com.bronto.api.model.ObjectBuilder;
import com.bronto.api.model.WriteResult;
import com.bronto.api.request.BrontoReadRequest;
import com.bronto.api.operation.BrontoWriteBatch;

import java.util.Iterator;

public interface CommonOperations<O> {
    public ObjectBuilder<O> newObject();
    public Iterable<O> readAll(BrontoReadRequest<O> request);
    public Iterable<WriteResult> writeAll(BrontoWriteBatch<O> batch);
}
