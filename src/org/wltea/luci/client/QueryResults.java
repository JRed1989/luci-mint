/**
 * 
 */
package org.wltea.luci.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询结果对象
 * @author linliangyi
 *
 */
public class QueryResults{

	/*
	 * 总的查询命中数量
	 */
	private int totalHit;
	
	/*
	 * 当前页码
	 */
	private int pageNo;
	
	/*
	 * 页面大小 
	 */
	private int pageSize = 1;

	/*
	 * 结果集
	 */
	private List<Map<String, String>> results;
	
	/**
	 * 构造结果对象
	 */
	public QueryResults() {
	}

	public int getTotalHit() {
		return totalHit;
	}

	public void setTotalHit(int totalHit) {
		this.totalHit = totalHit;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<Map<String, String>> getResults() {
		return results;
	}

	public void setResults(List<Map<String, String>> results) {
		this.results = results;
	}

	public <T> List<T> getResultBeans(Class<T> resultType){
		List<T> resultBeans = new ArrayList<T>();	
		if(this.results != null){		
			for(Map<String ,String> rowData : this.results){
				T bean = BasicDataFormatter.createBeanFromMap(rowData, resultType);
				resultBeans.add(bean);
			}
		}
		return resultBeans;
	}
	
	/**
	 * 根据pageSize和totalHit计算总页数
	 * @return
	 */
	public int getTotalPage() {
		int totalPage = this.totalHit / this.pageSize; 
		if(this.totalHit % this.pageSize != 0){
			totalPage = totalPage + 1;
		}
		return totalPage;
	}	
	
}
