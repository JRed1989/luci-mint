/**
 * 
 */
package org.wltea.luci.client;

import java.util.List;

/**
 * Luci 索引服务接口
 * @author linliangyi
 *
 */
public interface IndexService {
	
	/**
	 * 构建索引（与ADD不同，用于索引第一次的初始化，数据批量导入）
	 * @param dataBean 带有luci annotation的Bean
	 */
	public void build(Object dataBean);
	
	/**
	 * 批量构建索引
	 * @param dataBeanList 带有luci annotation的Bean List
	 */
	public void build(List<?> dataBeanList);
	

	/**
	 * 备份索引（向备份库构建索引）
	 * @param dataBean 带有luci annotation的Bean
	 */
	public void backup(Object dataBean);
	
	/**
	 * 批量备份索引（向备份库构建索引）
	 * @param dataBeanList 带有luci annotation的Bean List
	 */
	public void backup(List<?> dataBeanList);
	

	/**
	 * 新增索引
	 * @param dataBean 带有luci annotation的Bean
	 */
	public void add(Object dataBean);
	
	/**
	 * 批量新增索引
	 * @param dataBeanList 带有luci annotation的Bean List
	 */
	public void add(List<?> dataBeanList);	
	

	/**
	 * 修改索引
	 * @param dataBean 带有luci annotation的Bean
	 */
	public void update(Object dataBean);
	
	/**
	 * 批量修改索引
	 * @param dataBeanList 带有luci annotation的Bean List
	 */
	public void update(List<?> dataBeanList);
	
	/**
	 * 删除索引
	 * @param dataBean 带有luci annotation的Bean
	 */
	public void delete(Object dataBean);
	
	/**
	 * 批量删除索引
	 * @param dataBeanList 带有luci annotation的Bean List
	 */
	public void delete(List<?> dataBeanList);			
	

	/**
	 * 优化索引
	 * @param immediately 是否立刻执行优化
	 * 
	 */
	public void optimize(boolean immediately);
	
	/**
	 * 优化备份索引
	 * @param immediately 是否立刻执行优化
	 * 
	 */
	public void optimizeBackup(boolean immediately);	
	
		
	/**
	 * 查询主索引
	 * @param queryString 使用IKExp
	 * @param pageNo
	 * @param pageSize
	 * @param reverse 默认使用DOC ID排序
	 * @return
	 */
	public QueryResults query(String queryString , int pageNo , int pageSize , boolean reverse);
	
	/**
	 * 查询主索引
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @param sort
	 * @param inBackupIndex
	 * @return
	 */
	public QueryResults query(String queryString , int pageNo , int pageSize , boolean reverse , String sortFieldName , String sortFieldType);
	
	
	/**
	 * 查询备份索引
	 * 
	 * @param queryString 使用IKExp
	 * @param pageNo
	 * @param pageSize
	 * @param reverse 默认使用DOC ID排序
	 * @return
	 */
	public QueryResults queryBackup(String queryString , int pageNo , int pageSize , boolean reverse);
	
	/**
	 * 查询备份索引
	 * @param queryString
	 * @param pageNo
	 * @param pageSize
	 * @param reverse
	 * @param sortFieldName
	 * @param sortFieldType
	 * @return
	 */
	public QueryResults queryBackup(String queryString , int pageNo , int pageSize , boolean reverse , String sortFieldName , String sortFieldType);
}
