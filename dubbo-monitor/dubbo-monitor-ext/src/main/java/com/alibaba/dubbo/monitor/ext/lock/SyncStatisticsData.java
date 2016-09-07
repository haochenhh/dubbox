package com.alibaba.dubbo.monitor.ext.lock;

import java.util.Arrays;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;

public class SyncStatisticsData {

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

	public SyncStatisticsData(long success, long failure, long input, long output, long elapsed, long concurrent,
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

	public SyncStatisticsData() {
		super();
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

	public void setHistogram(Histogram histogram) {
		this.histogram = histogram;
	}

	@Override
	public String toString() {
		Snapshot s = histogram.getSnapshot();
		System.out.println(Arrays.toString(s.getValues()));
		System.out.println("size is " + s.getValues().length);
		return "StatisticsData [success=" + success + ", failure=" + failure + ", input=" + input + ", output=" + output
				+ ", elapsed=" + elapsed + ", concurrent=" + concurrent + ", maxInput=" + maxInput + ", maxOutput="
				+ maxOutput + ", maxElapsed=" + maxElapsed + ", maxConcurrent=" + maxConcurrent + ", histogram="
				+ "\n75%=" + s.get75thPercentile() + "\n95%=" + s.get95thPercentile() + "\n98%=" + s.get98thPercentile()
				+ "\n99%=" + s.get99thPercentile() + "\n999%=" + s.get999thPercentile() + "]";
	}

}
