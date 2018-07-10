package com.engineer.commons.spi;

import org.junit.Test;
import com.engineer.commons.spi.test.ISpiCustomTest;

/**
 * @author fangzhibin 2015年4月11日 下午3:39:16
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月11日 下午3:39:16
 */
public class CustomSpiTest {
	
	@Test
	public void test1() {
		ISpiCustomTest test = SpiServiceFactory.getService(ISpiCustomTest.class);
		test.helloworld("test1");
	}
	
	@Test
	public void testa() {
		ISpiCustomTest test = SpiServiceFactory.getService(ISpiCustomTest.class, "a");
		test.helloworld("testa");
	}
	
	@Test
	public void testb() {
		ISpiCustomTest test = SpiServiceFactory.getService(ISpiCustomTest.class, "b");
		test.helloworld("testb");
	}
	
	@Test
	public void testc() {
		ISpiCustomTest test = SpiServiceFactory.getService(ISpiCustomTest.class, "c");
		if (null != test) {
			throw new RuntimeException(" is not null");
		}
	}
}
