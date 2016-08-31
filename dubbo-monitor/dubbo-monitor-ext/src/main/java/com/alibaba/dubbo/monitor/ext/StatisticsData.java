package com.alibaba.dubbo.monitor.ext;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
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

	private long success;

	private long failure;

	private long input;

	private long output;

	private long elapsed;

	private long concurrent;

	private long maxInput;

	private long maxOutput;

	private long maxElapsed;

	private long maxConcurrent;

	private Histogram histogram;

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

	public void setSuccess(long success) {
		this.success = success;
	}

	public void setFailure(long failure) {
		this.failure = failure;
	}

	public void setInput(long input) {
		this.input = input;
	}

	public void setOutput(long output) {
		this.output = output;
	}

	public void setElapsed(long elapsed) {
		this.elapsed = elapsed;
	}

	public void setConcurrent(long concurrent) {
		this.concurrent = concurrent;
	}

	public void setMaxInput(long maxInput) {
		this.maxInput = maxInput;
	}

	public void setMaxOutput(long maxOutput) {
		this.maxOutput = maxOutput;
	}

	public void setMaxElapsed(long maxElapsed) {
		this.maxElapsed = maxElapsed;
	}

	public void setMaxConcurrent(long maxConcurrent) {
		this.maxConcurrent = maxConcurrent;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public void setHistogram(Histogram histogram) {
		this.histogram = histogram;
	}

	public void updateHistogram(long elapsed) {
		System.out.println(elapsed);
		histogram.update(elapsed);
	}

	public void initHistogram() {
		histogram = newHistogram();
	}

	private Histogram newHistogram() {
		return new Histogram(new ExponentiallyDecayingReservoir());
	}

}
