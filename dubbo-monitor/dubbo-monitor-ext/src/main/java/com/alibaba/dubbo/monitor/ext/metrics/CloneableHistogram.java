package com.alibaba.dubbo.monitor.ext.metrics;

import com.codahale.metrics.Histogram;

public class CloneableHistogram extends Histogram implements Cloneable<Histogram> {

	public CloneableHistogram(CloneableReservior reservoir) {
		super(reservoir);
	}

	@Override
	public Histogram doClone() {
		CloneableReservior op = (CloneableReservior) reservoir;
		return new CloneableHistogram(new CloneableReservior(op));

	}
}
