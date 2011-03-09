/**
 * 
 */
package org.wltea.luci.client;

import java.net.URL;

/**
 * 索引服务工厂类
 * @author linliangyi
 *
 */
public class IndexServiceFactory {
	
	/**
	 * 获取索引服务本地实现
	 * @return
	 */
	public static IndexService getLocalIndexService(String indexName){
		return new LocalIndexService(indexName);
	}
	
	/**
	 * 获取索引服务远程实现
	 * @param remoteHttpURL
	 * @return
	 */
	public static IndexService getRemoteIndexService(String indexName , URL remoteHttpURL){
		return new RemoteIndexService(indexName , remoteHttpURL);
	}
}
