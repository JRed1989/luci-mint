/**
 * 
 */
package org.wltea.luci.client;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.luci.index.IndexContext;
import org.wltea.luci.index.IndexContextContainer;
import org.wltea.luci.search.PagedResultSet;

/**
 * 本地化（相对远程RPC）索引服务实现
 * @author linliangyi
 *
 */
public class LocalIndexService implements IndexService {
	
	/*
	 * 本地索引上下文对象
	 */
	private IndexContext indexContext;
	
	public LocalIndexService(String indexName){
		indexContext = IndexContextContainer.loadIndexContext(indexName);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#build(java.lang.Object)
	 */
	public void build(Object dataBean) {
		Document doc = DocumentDataFormatter.createDocumentfromBean(dataBean , indexContext.getIndexConfig());
		indexContext.build(doc);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#build(java.util.List)
	 */
	public void build(List<?> dataBeanList) {
		List<Document> docs = DocumentDataFormatter.createDocumentfromBeans(dataBeanList , indexContext.getIndexConfig());
		for(Document doc : docs){
			indexContext.build(doc);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#backup(java.lang.Object)
	 */
	public void backup(Object dataBean) {
		Document doc = DocumentDataFormatter.createDocumentfromBean(dataBean , indexContext.getIndexConfig());
		indexContext.backup(doc);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#backup(java.util.List)
	 */
	public void backup(List<?> dataBeanList) {
		List<Document> docs = DocumentDataFormatter.createDocumentfromBeans(dataBeanList , indexContext.getIndexConfig());
		for(Document doc : docs){
			indexContext.backup(doc);
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#add(java.lang.Object)
	 */
	public void add(Object dataBean) {
		Document doc = DocumentDataFormatter.createDocumentfromBean(dataBean , indexContext.getIndexConfig());
		indexContext.add(doc);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#add(java.util.List)
	 */
	public void add(List<?> dataBeanList) {
		List<Document> docs = DocumentDataFormatter.createDocumentfromBeans(dataBeanList , indexContext.getIndexConfig());
		for(Document doc : docs){
			indexContext.add(doc);
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#update(java.lang.Object)
	 */
	public void update(Object dataBean) {
		Document doc = DocumentDataFormatter.createDocumentfromBean(dataBean , indexContext.getIndexConfig());
		indexContext.update(doc);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#update(java.util.List)
	 */
	public void update(List<?> dataBeanList) {
		List<Document> docs = DocumentDataFormatter.createDocumentfromBeans(dataBeanList , indexContext.getIndexConfig());
		for(Document doc : docs){
			indexContext.update(doc);
		}
	}	

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#delete(java.lang.Object)
	 */
	public void delete(Object dataBean) {
		Document doc = DocumentDataFormatter.createDocumentfromBean(dataBean , indexContext.getIndexConfig());
		indexContext.delete(doc);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#delete(java.util.List)
	 */
	public void delete(List<?> dataBeanList) {
		List<Document> docs = DocumentDataFormatter.createDocumentfromBeans(dataBeanList , indexContext.getIndexConfig());
		for(Document doc : docs){
			indexContext.delete(doc);
		}
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#optimize(java.lang.Object, boolean)
	 */
	public void optimize(boolean immediately) {
		indexContext.optimize(immediately);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#optimizeBackup(java.lang.Object, boolean)
	 */
	public void optimizeBackup(boolean immediately) {
		indexContext.optimizeBackupIndex(immediately);
	}

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#query(java.lang.String, int, int, boolean, java.lang.Class)
	 */
	public QueryResults query(String queryString, int pageNo,
			int pageSize, boolean reverse) {
		if(queryString == null){
			throw new IllegalArgumentException("Parameter 'queryString' is undefined.");
		}
		//Query String转成Lucene Query对象
		Query query = IKQueryParser.parse(queryString);			
		//构造 Sort 对象
		//初始化默认排序方式
		Sort querySort = new Sort(new SortField(null , SortField.DOC ,reverse));
		return this.query(query, pageNo, pageSize, querySort, false);
	}
	
	public QueryResults query(String queryString, int pageNo,
			int pageSize, boolean reverse, String sortFieldName,
			String sortFieldType) {

		if(queryString == null){
			throw new IllegalArgumentException("Parameter 'queryString' is undefined.");
		}

		Sort querySort = null;
		if(sortFieldName == null){
			querySort = new Sort(new SortField(null , SortField.DOC ,reverse));
		}else if(sortFieldType == null){
			//sortFieldType = "STRING";
			querySort = new Sort(new SortField(sortFieldName , SortField.STRING ,reverse));
		}else{
			if("BYTE".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.BYTE ,reverse));
			}else if("SHORT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.SHORT ,reverse));
			}else if("INT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.INT ,reverse));
			}else if("LONG".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.LONG ,reverse));
			}else if("FLOAT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.FLOAT ,reverse));
			}else if("DOUBLE".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.DOUBLE ,reverse));
			}else if("STRING".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.STRING ,reverse));
			}else {
				throw new IllegalArgumentException( "Unkown query mode. 'sortType' is Unkown.");
			}
		}		
		//Query String转成Lucene Query对象
		Query query = IKQueryParser.parse(queryString);			
		return this.query(query, pageNo, pageSize, querySort, false);
	}	

	/* (non-Javadoc)
	 * @see org.wltea.luci.client.IndexService#queryBackup(java.lang.String, int, int, boolean, java.lang.Class)
	 */
	public QueryResults queryBackup(String queryString, int pageNo,
			int pageSize, boolean reverse) {
		
		if(queryString == null){
			throw new IllegalArgumentException("Parameter 'queryString' is undefined.");
		}
		//Query String转成Lucene Query对象
		Query query = IKQueryParser.parse(queryString);			
		//构造 Sort 对象
		//初始化默认排序方式
		Sort querySort = new Sort(new SortField(null , SortField.DOC ,reverse));		
		return this.query(query, pageNo, pageSize, querySort, true);
	}

	public QueryResults queryBackup(String queryString, int pageNo,
			int pageSize, boolean reverse, String sortFieldName,
			String sortFieldType) {

		if(queryString == null){
			throw new IllegalArgumentException("Parameter 'queryString' is undefined.");
		}

		Sort querySort = null;
		if(sortFieldName == null){
			querySort = new Sort(new SortField(null , SortField.DOC ,reverse));
		}else if(sortFieldType == null){
			//sortFieldType = "STRING";
			querySort = new Sort(new SortField(sortFieldName , SortField.STRING ,reverse));
		}else{
			if("BYTE".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.BYTE ,reverse));
			}else if("SHORT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.SHORT ,reverse));
			}else if("INT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.INT ,reverse));
			}else if("LONG".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.LONG ,reverse));
			}else if("FLOAT".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.FLOAT ,reverse));
			}else if("DOUBLE".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.DOUBLE ,reverse));
			}else if("STRING".equals(sortFieldType)){
				querySort = new Sort(new SortField(sortFieldName , SortField.STRING ,reverse));
			}else {
				throw new IllegalArgumentException( "Unkown query mode. 'sortType' is Unkown.");
			}
		}		
		//Query String转成Lucene Query对象
		Query query = IKQueryParser.parse(queryString);			
		return this.query(query, pageNo, pageSize, querySort, true);
	}
	
	/**
	 * 查询索引
	 * 本地接口底层API方法
	 * 用户可以自己构造复杂的Query和Sort对象
	 * @param <T>
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @param sort
	 * @param resultType
	 * @return
	 */
	public QueryResults query(Query query, int pageNo,
			int pageSize, Sort sort, boolean isBackup) {
		PagedResultSet resultSet = this.indexContext.search(query, pageNo, pageSize, sort, isBackup);
		return pack(resultSet);
	}
	
	/**
	 * 将PagedResultSet 包装成 QueryResults
	 * @param resultSet
	 * @return
	 */
	private QueryResults pack(PagedResultSet resultSet){
		QueryResults queryResults = new QueryResults();
		if(resultSet != null){
			queryResults.setPageNo(resultSet.getPageNo());
			queryResults.setPageSize(resultSet.getPageSize());
			queryResults.setResults(resultSet.getResults());
			queryResults.setTotalHit(resultSet.getTotalHit());
		}
		return queryResults;
	}
}
