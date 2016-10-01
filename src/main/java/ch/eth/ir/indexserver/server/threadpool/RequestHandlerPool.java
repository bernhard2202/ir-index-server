package ch.eth.ir.indexserver.server.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.eth.ir.indexserver.server.request.AbstractRequest;
import ch.eth.ir.indexserver.server.response.AbstractResponse;

public class RequestHandlerPool extends ThreadPoolExecutor{
	
	private RequestHandlerPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}
	
	private static RequestHandlerPool INSTANCE = null;
	
	public static RequestHandlerPool getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RequestHandlerPool(5, 10, 60, TimeUnit.SECONDS,
					new PriorityBlockingQueue<Runnable>(20));
		}
		return INSTANCE;
	}
	
	public<T extends AbstractResponse> Future<T> submit(AbstractRequest<T> task, int priority) {
		return (Future<T>) super.submit(new ComparableFutureTask<T>(task, priority));
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return (RunnableFuture<T>) callable;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return (RunnableFuture<T>) runnable;
    }

	static class ComparableFutureTask<T> extends FutureTask<T> implements Comparable<ComparableFutureTask<T>> {
	
	    volatile int priority = 0;
	
	    public ComparableFutureTask(Runnable runnable, T result, int priority) {
	        super(runnable, result);
	        this.priority = priority;
	    }
	
	    public ComparableFutureTask(Callable<T> callable, int priority) {
	        super(callable);
	        this.priority = priority;
	    }
	
	    @Override
	    public int compareTo(ComparableFutureTask<T> o) {
	        return Integer.valueOf(priority).compareTo(o.priority);
	    }
	    
	    @Override
	    public boolean cancel(boolean mayInterruptIfRunning) {
	    	return super.cancel(mayInterruptIfRunning);
	    }
	}
}
