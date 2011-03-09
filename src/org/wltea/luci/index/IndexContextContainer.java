/**
 * 
 */
package org.wltea.luci.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.wltea.spring.util.SpringContextLoader;

/**
 * 多索引上下文容器
 * @author linliangyi
 *
 */
public class IndexContextContainer{
	
	private final static String SPRING_ID = "LuciMint.IndexContextContainer";

	private static class SingletonHolder {   
		static IndexContextContainer instance = new IndexContextContainer();   
	}   

	/**
	 * 多个索引配置
	 * 
	 */
	private List<IndexConfig> indexConfigs;
	
	/**
	 * 索引上下文映射
	 * IndexName --> IndexContext
	 */
	private HashMap<String , IndexContext> indexContexts;
	
	private IndexContextContainer(){
		this.indexConfigs = new ArrayList<IndexConfig>(4);
		this.indexContexts =  new HashMap<String , IndexContext>(4);
	}


	/**
	 * 单例构造器
	 * @return
	 */
	public static IndexContextContainer getInstance(){
		return SingletonHolder.instance;
	}
	
	/**
	 * 静态方法，加载scheme对应的IndexControllerContext
	 * @param indexName
	 * @return
	 */
	public static IndexContext loadIndexContext(String indexName){
		IndexContextContainer singleton = (IndexContextContainer)SpringContextLoader.getBean(SPRING_ID);
		return singleton.getIndexControllerContext(indexName);		
	}

	public List<IndexConfig> getIndexConfigs() {
		return indexConfigs;
	}


	public void setIndexConfigs(List<IndexConfig> indexConfigs) {
		this.indexConfigs = indexConfigs;
	}

	/**
	 * 构造scheme对应的IndexControllerContext
	 * @param schemeName
	 * @return
	 */
	private IndexContext getIndexControllerContext(String indexName){
		
		if(indexName == null){
			throw new IllegalArgumentException("非法参数异常：indexName为null");
		}
		
		IndexContext context = this.indexContexts.get(indexName);
		if(context == null){
			synchronized(this.indexContexts){
				context = this.indexContexts.get(indexName);
				if(context == null){
					//取indexName对应的IndexConfig
					for(IndexConfig config : this.indexConfigs){
						if(indexName.equals(config.getIndexName())){
							//构造IndexControllerContext
							context = new IndexContext(config);
							//缓存到map
							this.indexContexts.put(indexName, context);
							return context;							
						}
					}
				}
			}
		}
		return context;
	}
}
