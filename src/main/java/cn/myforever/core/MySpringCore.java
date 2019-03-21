package cn.myforever.core;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.myforever.annotation.Bean;
import cn.myforever.annotation.Source;
import cn.myforever.utils.ClassUtil;

/**
 * 自定义简单的spring IOC和DI框架，支持xml和注解配置方式
 * 在容易启动的时候就会实例化bean,而不是加载的时候实例化,默认属性都是首字母小写
 * @author forever
 */
public class MySpringCore {
	private ConcurrentHashMap<String, Object> map =new ConcurrentHashMap<String, Object>();
	//配置文件信息，启动应该是单线程的，不会有并发问题
	private List<ConfigInfo> configInfos = new ArrayList<ConfigInfo>(); 
	/**
	 * 初始化spring容器，若是path以.xml结尾，则表明是采用配置文件方式，否则，就表明是采用注解方式
	 * 传进来的就是一个包路径，配置文件方式默认读取classpath:spring.xml
	 * @param path
	 * @throws Exception 
	 */
	public MySpringCore(String path) throws Exception {
		//1、判断是配置文件方式还是注解的方式
		if(path.contains(".xml")) {
			System.out.println("配置文件方式启动");
			initBeanByXml(path);
		}else {
			System.out.println("非配置文件方式启动，那么应该是注解的方式启动");
			//这里就要进行扫包，然后获取该包下的所有class
			initBeanByPacakage(path);
		}
	}
	/**
	 * 获取bean
	 * @param beanName
	 * @return
	 */
	public Object getBean(String beanName) {
		return map.get(beanName);
	}
	/**
	 * 初始化bean
	 * @param path xml文件路径
	 * @throws Exception 
	 */
	private void initBeanByXml(String path) throws Exception {
		//1、去获取配置文件的所有bean节点信息,这里只获取bean
		List<Element> elements = readXml(path);
		if(elements==null) {
			return;
		}
		//这里就表明，配置文件中配置有bean，初始化bean
		initConfigByElements(elements);
		System.out.println("配置文件加载成功："+configInfos.toString());
		//这一步是提前实例化bean，防止循环依赖
		initBeanByConfigInfos();
		System.out.println("容器初始化bean成功："+map.toString());
		//处理依赖
		dealDependency();
		System.out.println("容器初始化bean依赖成功："+map.toString());
		
	}
	/**
	 * 处理依赖
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void dealDependency() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		//遍历配置文件，看看哪些是有属性的
		for (ConfigInfo configInfo : configInfos) {
			if("bean".equals(configInfo.getTagName())) {
				List<String> propertys = configInfo.getPropertys();
				Object obj = map.get(configInfo.getId());
				if(propertys!=null&&propertys.size()>0) {
					for (String string : propertys) {
						//获取属性对应的Object
						String[] strs = string.split("#");
						String id = strs[0];
						String ref = strs[1];
						Object object = map.get(ref);
						//设置属性，不一定需要有get，set方法
						Class<?> clazz  =obj.getClass();
						//getDeclaredField是可以获取一个类的所有字段. 
						//getField只能获取类的public 字段. 
						//Field field = clazz.getField(id);
						Field[] fields = clazz.getDeclaredFields();
						for (Field field2 : fields) {
							if(id.equals(field2.getName())) {
								//这样就可以改动私有方法
								field2.setAccessible(true);
								field2.set(obj, object);
							}
						}
					}
				}
			}
		}
	}
	/**
	 * 更具配置文件信息初始化
	 * @throws Exception 
	 */
	private void initBeanByConfigInfos() throws Exception {
		for (ConfigInfo configInfo : configInfos) {
			//只处理bean标签
			if("bean".equals(configInfo.getTagName())){
				Class<?> clazz= Class.forName(configInfo.getClazz());
				if(clazz==null) {
					throw new Exception(configInfo.getClazz()+"反射生成class失败");
				}
				Object object = clazz.newInstance();
				if(object==null) {
					throw new Exception(configInfo.getClazz()+"实例化失败");
				}
				//将生产的Object放入map
				map.put(configInfo.getId(), object);
			}
		}
	}
	/**
	 * 初始化配置文件
	 * @param elements
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private void initConfigByElements(List<Element> elements) throws Exception {
		for (Element element : elements) {
			//这里只会初始化bean标签的元素
			if("bean".equals(element.getName())) {
				String id = element.attributeValue("id");
				String clazz = element.attributeValue("class");
				if(StringUtils.isBlank(id)||StringUtils.isBlank(clazz)) {
					throw new Exception("bean的属性定义不规范");
				}
				ConfigInfo configInfo = new ConfigInfo();
				configInfo.setId(id);
				configInfo.setClazz(clazz);
				configInfo.setTagName(element.getName());
				//获取property属性文件，这里只会处理依赖
				List<Element> eles = element.elements();
				List<String> propertys= new ArrayList<String>();
				if(eles!=null&&eles.size()>0) {
					for (Element ele : eles) {
						if("property".equals((ele.getName()))) {
							String ref = ele.attributeValue("ref");
							String id2 = ele.attributeValue("id");
							if(id2==null) {
								id2=ref;
							}
							if(ref==null) {
								throw new Exception("property标签必须有ref属性");
							}
							//这里就将属性值加入
							propertys.add(id2+"#"+ref);
						}
					}
				}
				configInfo.setPropertys(propertys);
				configInfos.add(configInfo);
			}
		}
	}
	/**
	 * 用dom4j解析配置文件
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	private List<Element> readXml(String path) throws Exception {
		SAXReader saxReader  = new SAXReader();
		if(StringUtils.isBlank(path)) {
			throw new Exception("配置文件路径不能为空");
		}
		//构造Document对象
		Document doc = saxReader.read(getInputStreamFromPath(path));
		//获取根节点信息
		Element element=doc.getRootElement();
		//判断是否是beans
		String rootName = element.getName();
		if(!"beans".equals(rootName)) {
			throw new Exception("xml文件格式不对，根节点必须是beans");
		}
		//获取所有的子节点，子节点必须是bean,如果是用来实例化的话
		@SuppressWarnings("unchecked")
		List<Element> elements = element.elements();
		if(elements==null||elements.isEmpty()) {
			return null;
		}
		return elements;
	}
	/**
	 * 默认去classpath下面找寻配置文件
	 * @param path
	 * @return
	 */
	private InputStream getInputStreamFromPath(String path) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		return is;
	}
	public MySpringCore() throws Exception {
		this("spring.xml");
	}
	//-------------------------------------------------------//
	//下面是以注解的方式启动
	private void initBeanByPacakage(String path) throws InstantiationException, IllegalAccessException {
		//用工具类扫包获取包及子包下面的所有类
		List<Class<?>> list= ClassUtil.getClasses(path);
		//若是没有类，则不处理
		if(list==null||list.size()<1) {
			return;
		}
		//获取所有加了@Bean的类
		List<Class<?>> haveBeanClass = findHaveBeanAnnotationClass(list);
		//初始化bean对象
		initBeanByClasses(haveBeanClass);
		//初始化依赖问题
		dealDependencyBySource();
		//初始化成功
		System.out.println("容器初始化bean依赖成功："+map.toString());
		
	}
	/**
	 * 处理依赖问题
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void dealDependencyBySource() throws IllegalArgumentException, IllegalAccessException {
		for(Entry<String,Object> entry :map.entrySet()) {
			dealDependencyBySource(entry.getValue());
		}
		
	}
	/**
	 * 处理属性依赖
	 * @param value
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void dealDependencyBySource(Object value) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = value.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			//判断属性上是否有@Source注解
			Source source = field.getAnnotation(Source.class);
			if(source!=null) {
				String name = field.getName();
				//去map获取需要注入的bean
				Object obj = map.get(name);
				//设置属性
				field.setAccessible(true);
				field.set(value, obj);
			}
		}
		
	}
	/**
	 * 这些都是有@Bean注解的类，所以要初始化，如果用户要把@Bean加载接口上，那么初始化会报错，用户自己处理
	 * @param haveBeanClass
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void initBeanByClasses(List<Class<?>> haveBeanClass) throws InstantiationException, IllegalAccessException {
		//循环实例化
		for (Class<?> class1 : haveBeanClass) {
			//获取类的名称，然后首字母小写变成key
			String name  =class1.getSimpleName();
			name = toLowerCaseFirstOne(name);
			//实例化
			Object object = class1.newInstance();
			map.put(name, object);
		}
		
	}
	// 首字母转小写
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}
	/**
	 * 获取所有类上有@Bean注解的类
	 * @param lists
	 * @return
	 */
	private List<Class<?>> findHaveBeanAnnotationClass(List<Class<?>> lists) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (Class<?> class1 : lists) {
			if(class1.isAnnotationPresent(Bean.class)) {
				list.add(class1);
			}
		}
		return list;
	}
	
	
	
	
	
	
}
