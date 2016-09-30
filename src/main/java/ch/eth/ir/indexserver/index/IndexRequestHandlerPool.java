package ch.eth.ir.indexserver.index;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class IndexRequestHandlerPool extends ThreadPoolExecutor{
	
	private IndexRequestHandlerPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	//TODO IndexRequestHandlerPool
	
	private static Logger logger = Logger.getLogger(IndexRequestHandlerPool.class);

	private static IndexRequestHandlerPool INSTANCE = null;
	
	public static IndexRequestHandlerPool getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IndexRequestHandlerPool(5, 10, 60, TimeUnit.SECONDS,
					new PriorityBlockingQueue<Runnable>(20));
		}
		return INSTANCE;
	}
	
	
}
