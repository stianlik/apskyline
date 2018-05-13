package ifis.skysim2.junk;

import ifis.skysim2.algorithms.pQueueSync.NonBlockingBlockQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueuePerformanceCheck {

	private static class QueueReader implements Runnable {

		private BlockingQueue queue;
		private final int n;

		public QueueReader(BlockingQueue queue, int n) {
			this.queue = queue;
			this.n = n;
		}

		@Override
		public void run() {
			for (int i = 0; i < n; i++) {
				try {
					queue.take();
				} catch (InterruptedException ex) {
					Logger.getLogger(QueuePerformanceCheck.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	private static class QueueWriter implements Runnable {

		private BlockingQueue queue;
		private final int n;

		public QueueWriter(BlockingQueue queue, int n) {
			this.queue = queue;
			this.n  = n;
		}

		@Override
		public void run() {
			Object object = new Object();
			for (int i = 0; i < n; i++) {
				try {
					queue.put(object);
				} catch (InterruptedException ex) {
					Logger.getLogger(QueuePerformanceCheck.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private static int nCapacity = 1000;
	private static double nInit = 0.1;
	private static int nWrite = 10000000;
	private static int pRead = 1;
	private static int pWrite = pRead;

	public static void main(String[] args) {
//		BlockingQueue queue = new ArrayBlockingQueue(nCapacity);
		BlockingQueue queue = new NonBlockingBlockQueue();

		Object object = new Object();
		for (int i = 0; i < nInit * nCapacity; i++) {
			try {
				queue.put(object);
			} catch (InterruptedException ex) {
				Logger.getLogger(QueuePerformanceCheck.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		long tic;
		long toc;

		Thread[] threadsR = new Thread[pRead];
		for (int i = 0; i < pRead; i++) {
			QueueReader queueReader = new QueueReader(queue, nWrite);
			Thread thread = new Thread(queueReader);
			threadsR[i] = thread;
		}
		
		Thread[] threadsW = new Thread[pWrite];
		for (int i = 0; i < pWrite; i++) {
			QueueWriter queueWriter = new QueueWriter(queue, nWrite);
			Thread thread = new Thread(queueWriter);
			threadsW[i] = thread;
		}
		
		tic = System.nanoTime();
		
		for (int i = 0; i < pWrite; i++) {
			threadsW[i].start();
		}
		
		for (int i = 0; i < pRead; i++) {
			threadsR[i].start();
		}
		
		for (int i = 0; i < pWrite; i++) {
			try {
				threadsW[i].join();
			} catch (InterruptedException ex) {
				Logger.getLogger(QueuePerformanceCheck.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		for (int i = 0; i < pRead; i++) {
			try {
				threadsR[i].join();
			} catch (InterruptedException ex) {
				Logger.getLogger(QueuePerformanceCheck.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		toc = System.nanoTime();
		System.out.format("%.1f ms%n", (toc - tic) / 1000000.0);
	}
}

