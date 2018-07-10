package com.engineer.commons.spi.test.java;

import com.engineer.commons.spi.test.ISpiJavaTest;

/**
 * @author fangzhibin 2015年4月11日 下午3:42:46
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月11日 下午3:42:46
 */
public class BSpiTestImpl implements ISpiJavaTest {
	
	@Override
	public String helloworld(String name) {
		return name + " java a";
	}
}
