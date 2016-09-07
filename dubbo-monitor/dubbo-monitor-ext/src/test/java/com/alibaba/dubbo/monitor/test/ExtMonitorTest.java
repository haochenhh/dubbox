package com.alibaba.dubbo.monitor.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.monitor.ext.cas.CasExtMonitor;
import com.alibaba.dubbo.monitor.ext.lock.SyncExtMonitor;
import com.alibaba.dubbo.monitor.ext.Printable;
import com.alibaba.dubbo.monitor.support.AbstractMonitorFactory;
import com.alibaba.dubbo.monitor.support.MonitorFilter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;

public class ExtMonitorTest {

	// private volatile URL lastStatistics;

	// private volatile Invocation lastInvocation;

	class CountInvoker implements Invoker<MonitorService> {

		private AtomicInteger num = new AtomicInteger(0);

		public int count() {
			return num.get();
		}

		public Class<MonitorService> getInterface() {
			return MonitorService.class;
		}

		public URL getUrl() {
			try {
				return URL.valueOf("dubbo://" + NetUtils.getLocalHost() + ":20880?" + "interval=10000&"
						+ Constants.APPLICATION_KEY + "=abc&" + Constants.SIDE_KEY + "=" + Constants.CONSUMER_SIDE + "&"
						+ Constants.MONITOR_KEY + "="
						+ URLEncoder.encode("dubbo://" + NetUtils.getLocalHost() + ":7070", "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

		public boolean isAvailable() {
			return false;
		}

		public Result invoke(Invocation invocation) throws RpcException {
			// lastInvocation = invocation;
			try {
				Thread.sleep(num.getAndIncrement());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		public void destroy() {
		}
	}

	private Random random = new Random();

	private final CountInvoker serviceInvoker = new CountInvoker();

	private MonitorFactory monitorFactory = new AbstractMonitorFactory() {

		@Override
		protected Monitor createMonitor(URL url) {
			return new CasExtMonitor(serviceInvoker, null);
		}
	};

	@Test
	public void testMonitor() {
		MonitorFilter filter = new MonitorFilter();
		filter.setMonitorFactory(monitorFactory);
		Invocation invocation = new RpcInvocation("aaa", new Class<?>[0], new Object[0]);
		RpcContext.getContext().setRemoteAddress(NetUtils.getLocalHost(), 20880)
				.setLocalAddress(NetUtils.getLocalHost(), 2345);

		// multiThreadOp(invocation, filter);
		op(invocation, filter);
	}

	private void op(final Invocation invocation, final MonitorFilter filter) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 500; i++) {
			filter.invoke(serviceInvoker, invocation);
		}
		long cost = System.currentTimeMillis() - start;
		System.out.println(serviceInvoker.count());
		System.out.println(cost + "ms");
	}

	private void multiThreadOp(final Invocation invocation, final MonitorFilter filter) {
		int threadNum = 5;
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(threadNum);
		for (int i = 0; i < threadNum; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						startGate.await();
						try {
							for (int i = 0; i < 100; i++) {
								filter.invoke(serviceInvoker, invocation);
							}
						} finally {
							endGate.countDown();
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}).start();
		}
		long start = System.currentTimeMillis();
		startGate.countDown();
		try {
			endGate.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long cost = System.currentTimeMillis() - start;
		System.out.println(serviceInvoker.count());
		System.out.println(cost + "ms");
	}

	// 49132ms
	// 45097ms
	@Test
	public void testExtMonitor() {
		MonitorService monitorService = new MockMonitorService();
//		final SyncExtMonitor monitor = new SyncExtMonitor(serviceInvoker, monitorService);
		 final CasExtMonitor monitor = new CasExtMonitor(serviceInvoker, monitorService);

		multiOp(monitor);
//		 singleOp(monitor);
	}

	private void singleOp(final Monitor monitor) {
		long start = System.currentTimeMillis();
		for (int i = 1; i <= 10000; i++) {
			URL url = new URL("", NetUtils.getLocalHost(), 8080, "/", MonitorService.SUCCESS, "1",
					MonitorService.ELAPSED, String.valueOf(i), MonitorService.CONCURRENT, String.valueOf(1),
					Constants.INPUT_KEY, "1", Constants.OUTPUT_KEY, "1");
			monitor.collect(url);
		}
		long cost = System.currentTimeMillis() - start;
		System.out.println(cost + "ms");
		if (monitor instanceof Printable) {
			((Printable) monitor).print();
		}

	}

	private void multiOp(final Monitor monitor) {
		final int threadNum = 3;
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(threadNum);
		final AtomicInteger num = new AtomicInteger(0);
		for (int i = 0; i < threadNum; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						startGate.await();
						try {
							for (int i = 0; i < 10000; i++) {
								int elapsed = getElapsed();
								URL url = new URL("", NetUtils.getLocalHost(), 8080, "/", MonitorService.SUCCESS, "1",
										MonitorService.ELAPSED, String.valueOf(elapsed), MonitorService.CONCURRENT,
										String.valueOf(1), Constants.INPUT_KEY, "1", Constants.OUTPUT_KEY, "1");
								monitor.collect(url);
							}
						} finally {
							endGate.countDown();
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}).start();
		}
		long start = System.currentTimeMillis();
		startGate.countDown();
		try {
			endGate.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long cost = System.currentTimeMillis() - start;
		System.out.println(cost + "ms");
		if (monitor instanceof Printable) {
			((Printable) monitor).print();
		}

	}

	protected int getElapsed() {
		int r = random.nextInt(10);
		try {
			Thread.sleep(r);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return r;
	}
}
