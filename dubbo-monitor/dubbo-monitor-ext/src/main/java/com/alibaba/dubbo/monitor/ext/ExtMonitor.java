package com.alibaba.dubbo.monitor.ext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.rpc.Invoker;
import com.codahale.metrics.Snapshot;

/**
 * 
 * @author minjun@youku.com
 *
 */
public class ExtMonitor implements Monitor {

	private static final Logger logger = LoggerFactory.getLogger(ExtMonitor.class);

	// 定时任务执行器
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3,
			new NamedThreadFactory("DubboMonitorSendTimer", true));

	// 统计信息收集定时器
	private final ScheduledFuture<?> sendFuture;

	private final Invoker<MonitorService> monitorInvoker;

	private final MonitorService monitorService;

	private final long monitorInterval;

	private final ConcurrentMap<Statistics, AtomicReference<StatisticsData>> statisticsMap = new ConcurrentHashMap<Statistics, AtomicReference<StatisticsData>>();

	public ExtMonitor(Invoker<MonitorService> monitorInvoker, MonitorService monitorService) {
		this.monitorInvoker = monitorInvoker;
		this.monitorService = monitorService;
		this.monitorInterval = monitorInvoker.getUrl().getPositiveParameter("interval", 60000);
		// 启动统计信息收集定时器
		sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				// 收集统计信息
				try {
					send();
				} catch (Throwable t) { // 防御性容错
					logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
				}
			}
		}, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
	}

	public void send() {
		if (logger.isInfoEnabled()) {
			logger.info("Send statistics to monitor " + getUrl());
		}
		String timestamp = String.valueOf(System.currentTimeMillis());
		for (Map.Entry<Statistics, AtomicReference<StatisticsData>> entry : statisticsMap.entrySet()) {
			// 获取已统计数据
			Statistics statistics = entry.getKey();
			AtomicReference<StatisticsData> reference = entry.getValue();
			StatisticsData data = reference.get();
			long success = data.getSuccess();
			long failure = data.getFailure();
			long input = data.getInput();
			long output = data.getOutput();
			long elapsed = data.getElapsed();
			long concurrent = data.getConcurrent();
			long maxInput = data.getMaxInput();
			long maxOutput = data.getMaxOutput();
			long maxElapsed = data.getMaxElapsed();
			long maxConcurrent = data.getMaxConcurrent();
			// 获取直方分布的快照
			Snapshot snapshot = data.getHistogram().getSnapshot();

			// 发送汇总信息
			URL url = statistics.getUrl().addParameters(//
					MonitorService.TIMESTAMP, timestamp, //
					MonitorService.SUCCESS, String.valueOf(success), //
					MonitorService.FAILURE, String.valueOf(failure), //
					MonitorService.INPUT, String.valueOf(input), //
					MonitorService.OUTPUT, String.valueOf(output), //
					MonitorService.ELAPSED, String.valueOf(elapsed), //
					MonitorService.CONCURRENT, String.valueOf(concurrent), //
					MonitorService.MAX_INPUT, String.valueOf(maxInput), //
					MonitorService.MAX_OUTPUT, String.valueOf(maxOutput), //
					MonitorService.MAX_ELAPSED, String.valueOf(maxElapsed), //
					MonitorService.MAX_CONCURRENT, String.valueOf(maxConcurrent), //
					MonitorService.PERCENT75_KEY, String.valueOf(snapshot.getValue(0.75)), //
					MonitorService.PERCENT90_KEY, String.valueOf(snapshot.getValue(0.90)), //
					MonitorService.PERCENT95_KEY, String.valueOf(snapshot.getValue(0.95)), //
					MonitorService.PERCENT98_KEY, String.valueOf(snapshot.getValue(0.98)), //
					MonitorService.PERCENT99_KEY, String.valueOf(snapshot.getValue(0.99)), //
					MonitorService.PERCENT999_KEY, String.valueOf(snapshot.getValue(0.999))//
			);
			// monitorService.collect(url);
			System.out.println(Arrays.toString(snapshot.getValues()));
			System.out.println(url);

			// 减去上次统计的信息
			// 由于cas操作会导致丢失部分histogram中的样本数据（collect之后compareAndSet成功之前收集的数据），但是histogram直方分布本来就是抽样统计的，所以丢失这部分数据也没有太大影响
			StatisticsData current;
			StatisticsData update = new StatisticsData();
			do {
				current = reference.get();
				if (current == null) {
					update.setSuccess(0);
					update.setFailure(0);
					update.setInput(0);
					update.setOutput(0);
					update.setElapsed(0);
					update.setConcurrent(0);
				} else {
					update.setSuccess(current.getSuccess() - success);
					update.setFailure(current.getFailure() - failure);
					update.setInput(current.getInput() - input);
					update.setOutput(current.getOutput() - output);
					update.setElapsed(current.getElapsed() - elapsed);
					update.setConcurrent(current.getConcurrent() - concurrent);
				}
				update.initHistogram();
			} while (!reference.compareAndSet(current, update));
		}
	}

	public void collect(URL url) {
		// 读写统计变量
		int success = url.getParameter(MonitorService.SUCCESS, 0);
		int failure = url.getParameter(MonitorService.FAILURE, 0);
		int input = url.getParameter(MonitorService.INPUT, 0);
		int output = url.getParameter(MonitorService.OUTPUT, 0);
		int elapsed = url.getParameter(MonitorService.ELAPSED, 0);
		int concurrent = url.getParameter(MonitorService.CONCURRENT, 0);
		// 初始化原子引用
		Statistics statistics = new Statistics(url);
		AtomicReference<StatisticsData> reference = statisticsMap.get(statistics);
		if (reference == null) {
			statisticsMap.putIfAbsent(statistics, new AtomicReference<StatisticsData>());
			reference = statisticsMap.get(statistics);
		}

		// CompareAndSet并发加入统计数据
		// 当前数据，cas返回false时，current不会被修改
		StatisticsData current;
		// 新数据
		StatisticsData update = new StatisticsData();
		do {
			current = reference.get();
			if (current == null) {
				update.setSuccess(success);
				update.setFailure(failure);
				update.setInput(input);
				update.setOutput(output);
				update.setElapsed(elapsed);
				update.setConcurrent(concurrent);
				update.setMaxInput(input);
				update.setMaxOutput(output);
				update.setMaxElapsed(elapsed);
				update.setMaxConcurrent(concurrent);
				update.initHistogram();
			} else {
				update.setSuccess(current.getSuccess() + success);
				update.setFailure(current.getFailure() + failure);
				update.setInput(current.getInput() + input);
				update.setOutput(current.getOutput() + output);
				update.setElapsed(current.getElapsed() + elapsed);
				update.setConcurrent((current.getConcurrent() + concurrent) / 2);
				update.setMaxInput(Math.max(current.getMaxInput(), input));
				update.setMaxOutput(Math.max(current.getMaxOutput(), output));
				update.setMaxElapsed(Math.max(current.getMaxElapsed(), elapsed));
				update.setMaxConcurrent(Math.max(current.getMaxConcurrent(), concurrent));
				// 统计百分比
				update.setHistogram(current.getHistogram());
				update.updateHistogram(elapsed);
			}
		} while (!reference.compareAndSet(current, update));
	}

	public List<URL> lookup(URL query) {
		return monitorService.lookup(query);
	}

	public URL getUrl() {
		return monitorInvoker.getUrl();
	}

	public boolean isAvailable() {
		return monitorInvoker.isAvailable();
	}

	public void destroy() {
		try {
			sendFuture.cancel(true);
		} catch (Throwable t) {
			logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
		}
		monitorInvoker.destroy();
	}

}
