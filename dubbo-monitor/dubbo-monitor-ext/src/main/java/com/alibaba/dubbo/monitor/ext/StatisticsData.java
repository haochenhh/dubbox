package com.alibaba.dubbo.monitor.ext;

import com.codahale.metrics.Histogram;

/**
 * 由于dubbo的monitor中将所有统计数据都存入原子数组变量中AtomicReference<Long[]>中，
 * 当需要扩展统计数据的时候，必须手动改变Long[]的长度，而且只能存储long类型数据，非常不灵活，
 * 设计也非常不OOP。所以我使用AtomicReference<StatisticsData>对象来存储所有统计数据
 * 
 * @author loda
 *
 */
public class StatisticsData {

	private final long success;

	private final long failure;

	private final long input;

	private final long output;

	private final long elapsed;

	private final long concurrent;

	private final long maxInput;

	private final long maxOutput;

	private final long maxElapsed;

	private final long maxConcurrent;

	private final Histogram histogram;

	public StatisticsData(long success, long failure, long input, long output, long elapsed, long concurrent,
			long maxInput, long maxOutput, long maxElapsed, long maxConcurrent, Histogram histogram) {
		super();
		this.success = success;
		this.failure = failure;
		this.input = input;
		this.output = output;
		this.elapsed = elapsed;
		this.concurrent = concurrent;
		this.maxInput = maxInput;
		this.maxOutput = maxOutput;
		this.maxElapsed = maxElapsed;
		this.maxConcurrent = maxConcurrent;
		this.histogram = histogram;
	}

	public long getSuccess() {
		return success;
	}

	public long getFailure() {
		return failure;
	}

	public long getInput() {
		return input;
	}

	public long getOutput() {
		return output;
	}

	public long getElapsed() {
		return elapsed;
	}

	public long getConcurrent() {
		return concurrent;
	}

	public long getMaxInput() {
		return maxInput;
	}

	public long getMaxOutput() {
		return maxOutput;
	}

	public long getMaxElapsed() {
		return maxElapsed;
	}

	public long getMaxConcurrent() {
		return maxConcurrent;
	}

	public Histogram getHistogram() {
		return histogram;
	}

}
