package com.engineer.commons.spi;

import org.junit.BeforeClass;
import org.junit.Test;
import com.engineer.commons.spi.test.ISpiJavaTest;
import com.engineer.commons.spi.test.java.ASpiTestImpl;
import com.engineer.commons.spi.test.java.BSpiTestImpl;

/**
 * @author fangzhibin 2015年4月11日 下午3:39:27
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月11日 下午3:39:27
 */
public class JavaSpiTest {
	
	@BeforeClass
	public void init() {
		SpiServiceLoader<ISpiJavaTest> serviceLoader = SpiServiceLoader.load(ISpiJavaTest.class);
		serviceLoader.setPrefix("META-INF/services/");
		SpiServiceFactory.setServiceLoader(serviceLoader);
	}
	
	@Test
	public void test1() {
		ISpiJavaTest test = SpiServiceFactory.getService(ISpiJavaTest.class);
		test.helloworld("test1");
	}
	
	@Test
	public void testa() {
		ISpiJavaTest test = SpiServiceFactory.getService(ISpiJavaTest.class, ASpiTestImpl.class.getName());
		test.helloworld("testa");
	}
	
	@Test
	public void testb() {
		ISpiJavaTest test = SpiServiceFactory.getService(ISpiJavaTest.class, BSpiTestImpl.class.getName());
		test.helloworld("testb");
	}
	
	@Test
	public void testc() {
		ISpiJavaTest test = SpiServiceFactory.getService(ISpiJavaTest.class, "c");
		if (null != test) {
			throw new RuntimeException(" is not null");
		}
	}
}
