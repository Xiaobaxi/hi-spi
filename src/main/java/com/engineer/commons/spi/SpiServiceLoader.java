package com.engineer.commons.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;

/**
 * spi服务加载
 * @author fangzhibin 2015年4月10日 下午4:30:11
 * @version V1.0
 * @modify: {原因} by fangzhibin 2015年4月10日 下午4:30:11
 */
public class SpiServiceLoader<S> implements Iterable<S> {
	
	// spi的服务配置目录
	private String prefix = "META-INF/services/spi/";
	// 类或接口的class
	private Class<S> service;
	// 类加载器
	private ClassLoader loader;
	// 缓存类或接口的实例
	private LinkedHashMap<String, S> providers = new LinkedHashMap<String, S>();
	// 延迟加载当前spi服务
	private LazyIterator lookupIterator;
	
	/**
	 * 重新加载spi服务
	 * @author fangzhibin 2015年4月11日 上午10:23:17
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:23:17
	 */
	public void reload() {
		providers.clear();
		lookupIterator = new LazyIterator(service, loader);
	}
	
	// 内部构造函数
	private SpiServiceLoader(Class<S> svc, ClassLoader cl) {
		service = svc;
		loader = cl;
		reload();
	}
	
	// spi服务配置异常
	private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
	}
	
	// spi服务配置异常
	private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg);
	}
	
	// spi服务配置异常
	private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
		fail(service, u + ":" + line + ": " + msg);
	}
	
	/**
	 * 按行解析配置文件
	 * @author fangzhibin 2015年4月11日 上午10:28:26
	 * @param service 类或接口的class
	 * @param u 配置文件的url
	 * @param r 读buffer
	 * @param lc 行数
	 * @param names 解析结果集合
	 * @return
	 * @throws IOException
	 * @throws ServiceConfigurationError
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:28:26
	 */
	private int parseLine(Class<S> service, URL u, BufferedReader r, int lc, List<String> names) throws IOException, ServiceConfigurationError {
		String ln = r.readLine();
		if (null == ln) {
			return -1;
		}
		// #前面的作为有效配置，#号后面的作为注释
		int ci = ln.indexOf('#');
		if (ci >= 0) {
			ln = ln.substring(0, ci);
		}
		ln = ln.trim();
		int n = ln.length();
		if (n != 0) {
			String[] spi = ln.split("=");
			String classType = null;
			String className = null;
			// length==1表示java默认的spi配置方式，自动解析为key=value
			if (spi.length == 1) {
				classType = spi[0].trim();
				className = spi[0].trim();
				// length==2表示自定义的 spi配置方式，key表示查询关键字
			} else if (spi.length == 2) {
				classType = spi[0].trim();
				className = spi[1].trim();
			}
			// 解析key和value的字符串
			if (null != classType && null != className) {
				n = className.length();
				if ((className.indexOf(' ') >= 0) || (className.indexOf('\t') >= 0)) {
					fail(service, u, lc, "Illegal configuration-file syntax");
				}
				int cp = className.codePointAt(0);
				if (!Character.isJavaIdentifierStart(cp)) {
					fail(service, u, lc, "Illegal provider-class name: " + className);
				}
				for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
					cp = className.codePointAt(i);
					if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
						fail(service, u, lc, "Illegal provider-class name: " + className);
					}
				}
				// 排除重复添加
				if (!providers.containsKey(classType) && !names.contains(ln)) {
					names.add(ln);
				}
			}
		}
		return lc + 1;
	}
	
	/**
	 * 读取spi的服务配置信息
	 * @author fangzhibin 2015年4月11日 上午10:29:55
	 * @param service 类或接口的class
	 * @param u 配置文件的url
	 * @return
	 * @throws ServiceConfigurationError
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:29:55
	 */
	private Iterator<String> parse(Class<S> service, URL u) throws ServiceConfigurationError {
		InputStream in = null;
		BufferedReader r = null;
		ArrayList<String> names = new ArrayList<String>();
		try {
			in = u.openStream();
			r = new BufferedReader(new InputStreamReader(in, "utf-8"));
			int lc = 1;
			// 按行解析spi配置
			while ((lc = parseLine(service, u, r, lc, names)) >= 0);
		} catch (IOException x) {
			fail(service, "Error reading configuration file", x);
		} finally {
			try {
				if (null != r) {
					r.close();
				}
				if (null != in) {
					in.close();
				}
			} catch (IOException y) {
				fail(service, "Error closing configuration file", y);
			}
		}
		return names.iterator();
	}
	
	/**
	 * 懒加载迭代器
	 * @author fangzhibin 2015年4月11日 上午10:31:37
	 * @version V1.0
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:31:37
	 */
	private class LazyIterator implements Iterator<S> {
		
		// 类或接口的class
		Class<S> service;
		// 类加载器
		ClassLoader loader;
		// 配置文件集合
		Enumeration<URL> configs = null;
		Iterator<String> pending = null;
		// 下一个
		String nextName = null;
		
		private LazyIterator(Class<S> service, ClassLoader loader) {
			this.service = service;
			this.loader = loader;
		}
		
		public boolean hasNext() {
			// 如果nextName不为空，则说明有下一个，不需要再次解析
			if (null != nextName && nextName.trim().length() > 0) {
				return true;
			}
			if (null == configs) {
				try {
					// spi配置文件相对路径
					String fullName = prefix + service.getName();
					// 加载配置
					if (loader == null) {
						configs = ClassLoader.getSystemResources(fullName);
					} else {
						configs = loader.getResources(fullName);
					}
				} catch (IOException x) {
					fail(service, "Error locating configuration files", x);
				}
			}
			while ((null == pending) || !pending.hasNext()) {
				if (null == configs || !configs.hasMoreElements()) {
					return false;
				}
				pending = parse(service, configs.nextElement());
			}
			nextName = pending.next();
			if (null != nextName && nextName.trim().length() > 0) {
				return true;
			}
			return false;
		}
		
		public S next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			String cn = nextName;
			String[] spi = cn.split("=");
			nextName = null;
			Class<?> c = null;
			if (null != spi && spi.length > 0) {
				String classType = null;
				String className = null;
				if (spi.length == 1) {
					classType = spi[0].trim();
					className = spi[0].trim();
				} else if (spi.length == 2) {
					classType = spi[0].trim();
					className = spi[1].trim();
				}
				if (null != classType && null != className) {
					// 如果服务已经存在，则无需再实例
					S p = providers.get(classType);
					if (null == p) {
						try {
							c = Class.forName(className, false, loader);
						} catch (ClassNotFoundException x) {
							fail(service, "Provider " + cn + " not found");
						}
						if (!service.isAssignableFrom(c)) {
							fail(service, "Provider " + cn + " not a subtype");
						}
						try {
							p = service.cast(c.newInstance());
							providers.put(classType, p);
							return p;
						} catch (Throwable x) {
							fail(service, "Provider " + cn + " could not be instantiated", x);
						}
					} else {
						return p;
					}
				} else {
					fail(service, "Provider " + cn + " spi config error");
				}
			} else {
				fail(service, "Provider " + cn + " spi config error");
			}
			throw new Error();
		}
		
		/**
		 * spi不支持删除服务
		 * @author fangzhibin 2015年4月11日 上午10:59:51
		 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:59:51
		 * @deprecated
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * 根据spi中配置的类型查询服务
	 * @author fangzhibin 2015年4月11日 上午11:15:03
	 * @param type spi服务类型
	 * @param reload 找不到时是否重载
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午11:15:03
	 */
	public S getService(String type, boolean reload) {
		S service = providers.get(type);
		if (null == service) {
			if (reload) {
				reload();
			}
			load();
			service = providers.get(type);
		}
		return service;
	}
	
	/**
	 * 加载spi到缓存中
	 * @author fangzhibin 2015年5月5日 下午4:38:55
	 * @modify: {原因} by fangzhibin 2015年5月5日 下午4:38:55
	 */
	private synchronized void load() {
		if (providers.isEmpty()) {
			while (lookupIterator.hasNext()) {
				lookupIterator.next();
			}
		}
	}
	
	/**
	 * 获取spi服务迭代器
	 * @author fangzhibin 2015年4月11日 上午11:00:49
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午11:00:49
	 */
	public Iterator<S> iterator() {
		load();
		final Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();
		return new Iterator<S>() {
			
			public boolean hasNext() {
				return knownProviders.hasNext();
			}
			
			public S next() {
				if (!knownProviders.hasNext()) {
					throw new NoSuchElementException();
				}
				return knownProviders.next().getValue();
			}
			
			/**
			 * spi不支持删除服务
			 * @author fangzhibin 2015年4月11日 上午10:59:51
			 * @modify: {原因} by fangzhibin 2015年4月11日 上午10:59:51
			 * @deprecated
			 */
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/**
	 * 加载spi的服务加载对象
	 * @author fangzhibin 2015年4月11日 上午11:04:05
	 * @param service 类或接口的class
	 * @param loader 类加载器
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午11:04:05
	 */
	public static <S> SpiServiceLoader<S> load(Class<S> service, ClassLoader loader) {
		return new SpiServiceLoader<S>(service, loader);
	}
	
	/**
	 * 加载spi的服务加载对象
	 * @author fangzhibin 2015年4月11日 上午11:04:05
	 * @param service 类或接口的class
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午11:04:05
	 */
	public static <S> SpiServiceLoader<S> load(Class<S> service) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return SpiServiceLoader.load(service, cl);
	}
	
	/**
	 * 加载spi的服务加载对象
	 * @author fangzhibin 2015年4月11日 上午11:04:05
	 * @param service 类或接口的class
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 上午11:04:05
	 */
	public static <S> SpiServiceLoader<S> loadInstalled(Class<S> service) {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		ClassLoader prev = null;
		while (cl != null) {
			prev = cl;
			cl = cl.getParent();
		}
		return SpiServiceLoader.load(service, prev);
	}
	
	/**
	 * 对象的字符串描述
	 * @return spi服务类名
	 */
	public String toString() {
		return "SpiServiceLoader[" + service.getName() + "]";
	}
	
	/**
	 * 设置spi配置文件目录前缀
	 * @author fangzhibin 2015年4月11日 下午1:52:56
	 * @param prefix 前缀目录，默认值：META-INF/services/spi/
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午1:52:56
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * 获取spi类或者接口的class
	 * @author fangzhibin 2015年4月11日 下午4:04:07
	 * @return
	 * @modify: {原因} by fangzhibin 2015年4月11日 下午4:04:07
	 */
	public Class<S> getService() {
		return service;
	}
}
