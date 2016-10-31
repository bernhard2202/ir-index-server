package ch.eth.ir.indexserver.server.threadpool;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Monitors the thread pool and constantly outputs its status
 */
public class ThreadPoolMonitor implements Runnable {
	private ThreadPoolExecutor executor;
	private int seconds;
	private boolean run = true;

	public ThreadPoolMonitor(ThreadPoolExecutor executor, int delay) {
		this.executor = executor;
		this.seconds = delay;
	}

	public void shutdown() {
		this.run = false;
	}

	@Override
	public void run() {
		while (run) {
			System.out.println(String.format(
					"[monitor] [%d/%d] Queue-Size: %d Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
					this.executor.getPoolSize(), this.executor.getCorePoolSize(), executor.getQueue().size(),
					this.executor.getActiveCount(),
					this.executor.getCompletedTaskCount(), this.executor.getTaskCount(), this.executor.isShutdown(),
					this.executor.isTerminated()));
			executor.getQueue().size();
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
