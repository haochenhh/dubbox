package com.alibaba.dubbo.monitor.test;

import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.MonitorService;

public class MockMonitorService implements MonitorService {

	@Override
	public void collect(URL statistics) {
		try {
			System.out.println("收集数据开始");
			Thread.sleep(100);
			System.out.println(statistics);
			System.out.println("收集数据结束");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<URL> lookup(URL query) {
		return null;
	}

}
