package org.apache.http.client.fluent;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.apache.http.client.ResponseHandler;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class TracedAsync {

    private Executor executor;
    private java.util.concurrent.Executor concurrentExec;
    private Tracer tracer;

    public static TracedAsync newInstance(Tracer tracer) {
        return new TracedAsync(tracer);
    }

    TracedAsync() {
        super();
    }

    TracedAsync(Tracer tracer) {
        super();
        this.tracer = tracer;
    }


    public TracedAsync use(final Executor executor) {
        this.executor = executor;
        return this;
    }

    public TracedAsync use(final java.util.concurrent.Executor concurrentExec) {
        this.concurrentExec = concurrentExec;
        return this;
    }

    static class TracedExecRunnable<T> extends Async.ExecRunnable {

        private final Span span;
        private final Tracer tracer;

        TracedExecRunnable(
                final Tracer tracer,
                final BasicFuture<T> future,
                final Request request,
                final Executor executor,
                final ResponseHandler<T> handler) {
            super(future, request, executor, handler);
            this.tracer = tracer;
            this.span = tracer.buildSpan(request.toString()).startActive(false).span();
        }

        @Override
        public void run() {
            try (Scope scope = tracer.scopeManager().activate(span, false)) {
                super.run();
            }
        }
    }

    public <T> Future<T> execute(
            final Request request, final ResponseHandler<T> handler, final FutureCallback<T> callback) {

        final BasicFuture<T> future = new BasicFuture<>(callback);
        final TracedExecRunnable<T> runnable = new TracedExecRunnable<>(
                tracer,
                future,
                request,
                this.executor != null ? this.executor : Executor.newInstance(),
                handler);
        if (this.concurrentExec != null) {
            this.concurrentExec.execute(runnable);
        } else {
            final Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();
        }
        return future;
    }

    public <T> Future<T> execute(final Request request, final ResponseHandler<T> handler) {
        return execute(request, handler, null);
    }

    public Future<Content> execute(final Request request, final FutureCallback<Content> callback) {
        return execute(request, new ContentResponseHandler(), callback);
    }

    public Future<Content> execute(final Request request) {
        return execute(request, new ContentResponseHandler(), null);
    }

}
