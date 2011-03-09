/**
 * 
 */
package org.wltea.luci.sample;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.wltea.luci.index.IndexConfig;
import org.wltea.luci.index.IndexEliminatePolicy;

/**
 * BBS索引淘汰策略
 * 
 * @author linliangyi
 *
 */
public class BBSIndexEliminatePolicy implements IndexEliminatePolicy {

	/* (non-Javadoc)
	 * @see org.wltea.luci.index.IndexEliminatePolicy#getEliminateCondition(IndexConfig indexConfig)
	 */
	public Query getEliminateCondition(IndexConfig indexConfig) {
		//构造时间查询条件（距离当前时间365天以前的记录）
		long timeMillis = System.currentTimeMillis() - indexConfig.getMigrateCritical();
		String timeQueryStr = new SimpleDateFormat("yyyyMMdd000000").format(new Date(timeMillis));
		Query query = new TermRangeQuery("POSTTIME", null , timeQueryStr , false , true);
		return query;
	}

}
