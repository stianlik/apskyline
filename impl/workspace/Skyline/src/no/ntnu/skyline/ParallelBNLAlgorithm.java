package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelBNLAlgorithm extends AbstractSkylineAlgorithm {
	
	Node head, tail;
	
	@Override
	public List<float[]> compute(PointSource data) {
		
		long startTime = System.nanoTime();
		
		head = new Node(null);
		tail = new Node(null);
		head.next = tail;
		int threadcount = config.getNumberOfCPUs();
		
		// Calculate in parallel
		ExecutorService executor = Executors.newFixedThreadPool(config.getNumberOfCPUs());
		for (int t = 0; t < threadcount; ++t) {
			executor.execute(new BNLRunner(data, head, tail, t, threadcount));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Unable to terminate threads");
		}
		
		totalTimeNS = System.nanoTime() - startTime;
		
		// Write into linked list for post-processing 
		List<float[]> window = new LinkedList<float[]>();
		Node cur = head;
		while ( (cur = cur.next) != tail ) {
			window.add(cur.data);
		}
		
		return window;
	}
	
	@Override
	public String getShortName() {
		return "Basic BNL Algorithm";
	}
	
	/**
	 * Add node after cur. If cur is not the tail, this method
	 * will fail and return false.
	 * @param prev
	 * @param cur Tail of list
	 * @param node
	 * @return true on success, false on failure
	 */
	public boolean addToWindow(Node prev, Node cur, Node node) {
		prev.lock();
		cur.lock();
		//if (prev.deleted || prev.next != cur || cur.next != tail) {
		if (prev.deleted || prev.next != cur) {
			prev.unlock();
			cur.unlock();
			return false;
		}
		node.next = cur;
		prev.next = node;
		prev.unlock();
		cur.unlock();
		return true;
	}

	public static boolean removeFromWindow(Node prev, Node cur) {
		prev.lock();
		cur.lock();
		if (prev.deleted || prev.next != cur) {
			prev.unlock();
			cur.unlock();
			return false;
		}
		cur.deleted = true;
		prev.next = cur.next;
		prev.unlock();
		cur.unlock();
		return false;
	}
	
	public class Node {
		
		public Node next = null;
		public volatile boolean deleted = false;
		public final float data[];
		private final Lock lock;
		
		public Node(float data[]) {
			//this.data = Arrays.copyOf(data, data.length); // Copy for memory locality?
			this.data = data;
			this.lock = new ReentrantLock();
		}
		
		public void lock() {
			lock.lock();
		}
		public void unlock() {
			lock.unlock();
		}
		
	}
	
	public class BNLRunner implements Runnable {
		
		final PointSource data;
		final Node head, tail;
		int curIndex = -1;
		final int threadid, threadcount;
		
		public BNLRunner(PointSource data, Node head, Node tail, int threadid, int threadcount) {
			this.data = data;
			this.head = head;
			this.tail = tail;
			this.curIndex = threadid - threadcount;
			this.threadid = threadid;
			this.threadcount = threadcount;
		}
		
		@Override
		public void run() {
			Node prev, cur;
			float[] p;
			PointRelationship d;
			nextrecord: while ( (p = next()) != null) {
				restart: while (true) {
					prev = head;
					cur = prev.next;
					while (cur != tail) {
						d = PointComparator.compare(cur.data, p);
						if (d == PointRelationship.DOMINATES) {
							continue nextrecord;
						}
						else if (d == PointRelationship.IS_DOMINATED_BY) {
							if (removeFromWindow(prev, cur)) {
								cur = cur.next;
							}
							else {
								continue restart;
							}
						}
						else {
							prev = cur;
							cur = cur.next;
						}
					}
					if (!addToWindow(prev, cur, new Node(p))) {
						continue restart;
					}
					else {
						break restart;
					}
				}
			}
		}
		
		private float[] next() {
			curIndex += threadcount;
			return (curIndex < data.size()) ? data.get(curIndex) : null;
		}
	}
	
	class LazyIterator {
		final Node head, tail;
		Node prev, cur;
		
		public LazyIterator(Node head, Node tail) {
			this.head = head;
			this.tail = tail;
			this.prev = head;
			this.cur = head.next;
		}
		
		public Node next() {
			prev = cur;
			cur = cur.next;
			return cur;
		}
		
	}
	
}