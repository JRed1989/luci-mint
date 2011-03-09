/**
 * 
 */
package org.wltea.spring.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 作为监听器配置在web.xml，如：
 * <listener>
        <listener-class>com.sohu.fz173.util.spring.SpringContextLoader</listener-class>
    </listener>
 * @author 卓诗垚 ， 林良益（linliangyi2005@gmail.com)
 * Jul 26, 2010
 */
public class SpringContextLoader extends ContextLoaderListener {
	
	
	//配置文件路径
	private static final String CONFIG_FILE_LOCATION = "spring.xml";
	//Spring 上下文对象
	private static ApplicationContext context;
	
	/**
	 * 通过web context 初始化ApplicationContext
	 */
	@Override
	public void contextInitialized(ServletContextEvent event){
		if (context == null) {
			synchronized(SpringContextLoader.class){
				if(context == null){
					super.contextInitialized(event);
					ServletContext servletCtx = event.getServletContext();
				   	//获取web环境下的ApplicationContext
					context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletCtx);
				}
			}
		}
	} 

	/**
	 * 取得spring 的ApplicationContext
	 * @return
	 */
	public static ApplicationContext getSpringContext(){
		if (context == null) {
			synchronized(SpringContextLoader.class){
				//直接初始化
				if(context == null){
					context = new ClassPathXmlApplicationContext(CONFIG_FILE_LOCATION);
					((AbstractApplicationContext )context).registerShutdownHook();
				}
			}
		}
 		return context;
	}
	
	/**
	 * 取得spring环境的Bean
	 * @param beanName
	 * @return
	 */
	public static Object getBean(String beanName) {
		return getSpringContext().getBean(beanName);
	}
}
