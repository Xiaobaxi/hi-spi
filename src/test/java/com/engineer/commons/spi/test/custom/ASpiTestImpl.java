package com.engineer.commons.spi.test.custom;

import com.engineer.commons.spi.test.ISpiCustomTest;

/**
 * @author fangzhibin 2015年4月11日 下午3:42:25
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月11日 下午3:42:25
 */
public class ASpiTestImpl implements ISpiCustomTest {
	
	@Override
	public String helloworld(String name) {
		return name + " custom a";
	}
}
