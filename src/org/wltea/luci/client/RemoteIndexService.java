/**
 * 
 */
package org.wltea.luci.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.wltea.web.util.SimpleHttpClient;

/**
 * 远程RPC索引服务客户端实现
 * @author linliangyi
 *
 */
public class RemoteIndexService implements IndexService {
	
	/*
	 * 运程服务地址
	 */
	private URL remoteAddress;
	/*
	 * 索引名称
	 */
	private String indexName;

	public RemoteIndexService(String indexName , URL remoteAddress){
		if(indexName == null){
			throw new IllegalArgumentException("Parameter 'indexName' is null.");
		}
		if(remoteAddress == null){
			throw new IllegalArgumentException("Parameter 'remoteAddress' is null.");
		}
		this.remoteAddress = remoteAddress;
		this.indexName = indexName;
	}
	
	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#build(java.lang.Object)
	 */
	public void build(Object dataBean) {
		if(dataBean == null){
			return ;
		}
		List<Object> dataBeanList = new ArrayList<Object>();
		dataBeanList.add(dataBean);
		this.build(dataBeanList);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#build(java.util.List)
	 */
	public void build(List<?> dataBeanList) {
		if(dataBeanList == null || dataBeanList.isEmpty()){
			return;
		}
		String xmlData = XMLDataFormatter.createXMLfromBeans(dataBeanList);
		try {
			this.postIndexCommand("build", xmlData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#backup(java.lang.Object)
	 */
	public void backup(Object dataBean) {
		if(dataBean == null){
			return ;
		}
		List<Object> dataBeanList = new ArrayList<Object>();
		dataBeanList.add(dataBean);
		this.backup(dataBeanList);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#backup(java.util.List)
	 */
	public void backup(List<?> dataBeanList) {
		if(dataBeanList == null || dataBeanList.isEmpty()){
			return;
		}
		String xmlData = XMLDataFormatter.createXMLfromBeans(dataBeanList);
		try {
			this.postIndexCommand("backup", xmlData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#add(java.lang.Object)
	 */
	public void add(Object dataBean) {
		if(dataBean == null){
			return ;
		}
		List<Object> dataBeanList = new ArrayList<Object>();
		dataBeanList.add(dataBean);
		this.add(dataBeanList);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#add(java.util.List)
	 */
	public void add(List<?> dataBeanList) {
		if(dataBeanList == null || dataBeanList.isEmpty()){
			return;
		}
		String xmlData = XMLDataFormatter.createXMLfromBeans(dataBeanList);
		try {
			this.postIndexCommand("add", xmlData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#update(java.lang.Object)
	 */
	public void update(Object dataBean) {
		if(dataBean == null){
			return ;
		}
		List<Object> dataBeanList = new ArrayList<Object>();
		dataBeanList.add(dataBean);
		this.update(dataBeanList);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#update(java.util.List)
	 */
	public void update(List<?> dataBeanList) {
		if(dataBeanList == null || dataBeanList.isEmpty()){
			return;
		}
		String xmlData = XMLDataFormatter.createXMLfromBeans(dataBeanList);
		try {
			this.postIndexCommand("update", xmlData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#delete(java.lang.Object)
	 */
	public void delete(Object dataBean) {
		if(dataBean == null){
			return ;
		}
		List<Object> dataBeanList = new ArrayList<Object>();
		dataBeanList.add(dataBean);
		this.delete(dataBeanList);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#delete(java.util.List)
	 */
	public void delete(List<?> dataBeanList) {
		if(dataBeanList == null || dataBeanList.isEmpty()){
			return;
		}
		String xmlData = XMLDataFormatter.createXMLfromBeans(dataBeanList);
		try {
			this.postIndexCommand("delete", xmlData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#optimize(boolean)
	 */
	public void optimize(boolean immediately) {
		try {
			this.postOptimizeCommand("optimize", immediately);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#optimizeBackup(boolean)
	 */
	public void optimizeBackup(boolean immediately) {
		try {
			this.postOptimizeCommand("optimizeBackup", immediately);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#query(java.lang.String, int, int, boolean, java.lang.Class)
	 */
	public QueryResults query(String queryString, int pageNo,
			int pageSize, boolean reverse) {
		try {
			String jsonResponse = this.postQueryCommand("query", queryString, pageNo, pageSize, reverse, null, null);
			//JSON输出反序列化
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
		    QueryResults queryResult = mapper.readValue(jsonResponse, QueryResults.class);
			return queryResult;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public QueryResults query(String queryString, int pageNo,
			int pageSize, boolean reverse, String sortFieldName,
			String sortFieldType) {
		try {
			String jsonResponse = this.postQueryCommand("query", queryString, pageNo, pageSize, reverse, sortFieldName, sortFieldType);
			//JSON输出反序列化
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
		    QueryResults queryResult = mapper.readValue(jsonResponse, QueryResults.class);
			return queryResult;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#queryBackup(java.lang.String, int, int, boolean, java.lang.Class)
	 */
	public QueryResults queryBackup(String queryString, int pageNo,
			int pageSize, boolean reverse) {
		try {
			String jsonResponse = this.postQueryCommand("queryBackup", queryString, pageNo, pageSize, reverse, null, null);
			//JSON输出反序列化
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
		    QueryResults queryResult = mapper.readValue(jsonResponse, QueryResults.class);
			return queryResult;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public QueryResults queryBackup(String queryString, int pageNo,
			int pageSize, boolean reverse, String sortFieldName,
			String sortFieldType) {
		try {
			String jsonResponse = this.postQueryCommand("queryBackup", queryString, pageNo, pageSize, reverse, sortFieldName, sortFieldType);
			//JSON输出反序列化
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
		    QueryResults queryResult = mapper.readValue(jsonResponse, QueryResults.class);
			return queryResult;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	

	/**
	 * 提交索引类指令
	 * @param operate
	 * @param xmlData
	 * @return
	 * @throws IOException
	 */
	private String postIndexCommand(String operate , String xmlData) throws IOException{
		
		Map<String , String> httpParams = new HashMap<String , String>();
		httpParams.put("index-name", this.indexName);
		httpParams.put("operate", operate);
		httpParams.put("xml-data", xmlData);
		
		byte[] responseData = SimpleHttpClient.post(this.remoteAddress , httpParams , "UTF-8");
		if(responseData != null && responseData.length > 0){
			String responseString = new String(responseData , "UTF-8");
			return responseString;
		}
		return "";		
	}
	
	/**
	 * 提交索引类指令
	 * @param operate
	 * @param immediately
	 * @return
	 * @throws IOException
	 */
	private String postOptimizeCommand(String operate , boolean immediately) throws IOException{
		
		Map<String , String> httpParams = new HashMap<String , String>();
		httpParams.put("index-name", this.indexName);
		httpParams.put("operate", operate);
		httpParams.put("right-now", String.valueOf(immediately));
		
		byte[] responseData = SimpleHttpClient.post(this.remoteAddress , httpParams , "UTF-8");
		if(responseData != null && responseData.length > 0){
			String responseString = new String(responseData , "UTF-8");
			return responseString;
		}
		return "";		
	}

	/**
	 * 提交搜索类指令
	 * @param operate
	 * @param xmlData
	 * @return
	 * @throws IOException
	 */
	private String postQueryCommand(String operate , String query , int pageNo , int pageSize , boolean reverse , String sortFieldName,
			String sortFieldType) throws IOException{
		
		Map<String , String> httpParams = new HashMap<String , String>();
		httpParams.put("index-name", this.indexName);
		httpParams.put("operate", operate);
		httpParams.put("query", query);
		httpParams.put("page-no", String.valueOf(pageNo));
		httpParams.put("page-size", String.valueOf(pageSize));
		httpParams.put("sort-reverse", String.valueOf(reverse));
		httpParams.put("sort-by", sortFieldName);
		httpParams.put("sort-type", sortFieldType);		
		
		byte[] responseData = SimpleHttpClient.post(this.remoteAddress , httpParams , "UTF-8");
		if(responseData != null && responseData.length > 0){
			String responseString = new String(responseData , "UTF-8");
			return responseString;
		}
		return "";		
	}

}
