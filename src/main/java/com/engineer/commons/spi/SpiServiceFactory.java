package com.engineer.commons.spi;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spi的服务获取工厂，兼容标准java的spi和自定义的spi
 * @author fangzhibin 2015年4月10日 下午4:39:33
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月10日 下午4:39:33
 */
public class SpiServiceFactory {
	
	// spi的缓存map
	private static ConcurrentHashMap<Class<?>, SpiServiceLoader<?>> spis = new ConcurrentHashMap<Class<?>, SpiServiceLoader<?>>();
	
	/**
	 * 根据接口或类的class获取spi的服务,如果有多个返回顺序第一个
	 * @author fangzhibin 2015年4月11日 下午1:55:38
	 * @param service 接口或类的class
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午1:55:38
	 */
	@SuppressWarnings("unchecked")
	public static <S> S getService(Class<S> service) {
		SpiServiceLoader<S> serviceLoader = (SpiServiceLoader<S>)spis.get(service);
		if (null == serviceLoader) {
			spis.putIfAbsent(service, SpiServiceLoader.load(service));
			serviceLoader = (SpiServiceLoader<S>)spis.get(service);
		}
		if (null != serviceLoader) {
			Iterator<S> iterator = serviceLoader.iterator();
			if (null != iterator && iterator.hasNext()) {
				return iterator.next();
			}
		}
		return null;
	}
	
	/**
	 * 根据接口或类的class和服务类型获取spi的服务
	 * @author fangzhibin 2015年4月11日 下午1:55:38
	 * @param service 接口或类的class
	 * @param type 服务类型，如果使用标准java的spi配置，该值是spi内具体配置值
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午1:55:38
	 */
	@SuppressWarnings("unchecked")
	public static <S> S getService(Class<S> service, String type) {
		return getService(service, type, false);
	}
	
	/**
	 * 根据接口或类的class和服务类型获取spi的服务
	 * @author fangzhibin 2015年4月11日 下午1:55:38
	 * @param service 接口或类的class
	 * @param type 服务类型，如果使用标准java的spi配置，该值是spi内具体配置值
	 * @param reload 缓存不存在实例，是否重载
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午1:55:38
	 */
	@SuppressWarnings("unchecked")
	public static <S> S getService(Class<S> service, String type, boolean reload) {
		SpiServiceLoader<S> serviceLoader = (SpiServiceLoader<S>)spis.get(service);
		if (null == serviceLoader) {
			spis.putIfAbsent(service, SpiServiceLoader.load(service));
			serviceLoader = (SpiServiceLoader<S>)spis.get(service);
		}
		if (null != serviceLoader) {
			return serviceLoader.getService(type, reload);
		}
		return null;
	}
	
	/**
	 * 设置spi的服务加载器
	 * @author fangzhibin 2015年4月11日 下午4:04:37
	 * @param serviceLoader
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午4:04:37
	 */
	public static void setServiceLoader(SpiServiceLoader<?> serviceLoader) {
		spis.put(serviceLoader.getService(), serviceLoader);
	}
}
