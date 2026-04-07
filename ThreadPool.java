import java.util.*;
import java.util.concurrent.locks.*;

/**
 * A simple thread pool API.
 * 
 * Tasks that wish to get run by the thread pool must implement the
 * java.lang.Runnable interface.
 */

public class ThreadPool
{
	private final List<Runnable> taskQueue;
    private final List<WorkerThread> workerThreads;
    private boolean isShutdown = false;
    private final ReentrantLock poolLock = new ReentrantLock();
	/**
	 * Create a default size thread pool.
 	 */
	public ThreadPool() {
		this(5);
    }
	
	
	/**
	 * Create a thread pool with a specified size.
	 * 
	 * @param int size The number of threads in the pool.
	 */
	public ThreadPool(int size) {
		taskQueue = new LinkedList<>();
        workerThreads = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            WorkerThread worker = new WorkerThread();
            workerThreads.add(worker);
            worker.start();
        }
    }
	
	 
	/**
	 * shut down the pool.
	 */
	public void shutdown() {
		synchronized (taskQueue) {
            isShutdown = true;
            for (WorkerThread worker : workerThreads) {
                worker.interrupt(); // Interrupt based on API requirement [cite: 24]
            }
        }
	}
	
	/**
	 * Add work to the queue.
	 */
	public void add(Runnable task) {
		synchronized (taskQueue) {
            if (isShutdown) return;
            taskQueue.add(task);
            taskQueue.notify(); 
        }
	}
	// extra credit: Dynamically adjust the size of the thread pool
	public void setSize(int newSize) {
		poolLock.lock();
		try {
			int currentSize = workerThreads.size();
			if (newSize > currentSize) {
				for (int i = currentSize; i < newSize; i++) {
					WorkerThread worker = new WorkerThread();
					workerThreads.add(worker);
					worker.start();
				}
			} else if (newSize < currentSize) {
				for (int i = currentSize - 1; i >= newSize; i--) {
					WorkerThread worker = workerThreads.get(i);
					worker.interrupt();
					workerThreads.remove(i);
				}
			}
		} finally {
			poolLock.unlock();
		}
	}

	private class WorkerThread extends Thread {
		public void run() {
			while (!isShutdown || !taskQueue.isEmpty()) {
				Runnable task;
				synchronized (taskQueue) {
					while (taskQueue.isEmpty() && !isShutdown) {
						try {
							taskQueue.wait();
						} catch (InterruptedException e) {
							if (isShutdown) return;
						}
					}
					if (!taskQueue.isEmpty()) {
						task = taskQueue.remove(0);
					} else {
						continue;
					}
				}
				try {
					task.run();
				} catch (RuntimeException e) {
					// Handle exceptions from tasks
					System.err.println("Task execution error: " + e.getMessage());
				}
			}
		}
	}
}
