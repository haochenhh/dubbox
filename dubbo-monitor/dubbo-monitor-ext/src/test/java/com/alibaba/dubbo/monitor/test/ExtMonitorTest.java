package com.alibaba.dubbo.monitor.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.monitor.ext.ExtMonitor;
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

	private Random random = new Random();

	private final Invoker<MonitorService> serviceInvoker = new Invoker<MonitorService>() {
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
				Thread.sleep(random.nextInt(1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		public void destroy() {
		}
	};

	private MonitorFactory monitorFactory = new AbstractMonitorFactory() {

		@Override
		protected Monitor createMonitor(URL url) {
			return new ExtMonitor(serviceInvoker, null);
		}
	};

	@Test
	public void testMonitor() throws InterruptedException {
		final MonitorFilter filter = new MonitorFilter();
		filter.setMonitorFactory(monitorFactory);
		final Invocation invocation = new RpcInvocation("aaa", new Class<?>[0], new Object[0]);
		RpcContext.getContext().setRemoteAddress(NetUtils.getLocalHost(), 20880)
				.setLocalAddress(NetUtils.getLocalHost(), 2345);

		for (int i = 0; i < 5; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						filter.invoke(serviceInvoker, invocation);
					}
				}
			}).start();
		}

		
		new CountDownLatch(1).await();
	}
}
