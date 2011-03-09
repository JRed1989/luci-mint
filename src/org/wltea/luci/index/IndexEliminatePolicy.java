/**
 * 
 */
package org.wltea.luci.index;

import org.apache.lucene.search.Query;

/**
 * 索引淘汰策略接口
 * @author linliangyi
 *
 */
public interface IndexEliminatePolicy {

	/**
	 * 获取索引淘汰条件
	 * @return
	 */
	public Query getEliminateCondition(IndexConfig indexConfig);
}
