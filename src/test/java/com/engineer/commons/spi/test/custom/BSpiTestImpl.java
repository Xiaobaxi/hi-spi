package com.engineer.commons.spi.test.custom;

import com.engineer.commons.spi.test.ISpiCustomTest;

/**
 * @author fangzhibin 2015年4月11日 下午3:42:46
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月11日 下午3:42:46
 */
public class BSpiTestImpl implements ISpiCustomTest {
	
	@Override
	public String helloworld(String name) {
		return name + " custom b";
	}
}
