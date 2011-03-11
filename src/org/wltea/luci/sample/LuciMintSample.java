/**
 * 
 */
package org.wltea.luci.sample;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.wltea.luci.client.IndexService;
import org.wltea.luci.client.IndexServiceFactory;
import org.wltea.luci.client.QueryResults;

/**
 * 
 * 简易索引例子
 * @author linliangyi
 *
 */
public class LuciMintSample {

	
	/**
	 * 本地索引HelloWorld例子
	 * @param args
	 */
	public static void main(String[] args){
		
		
		
		
		//根据命名类获取Luci索引器实例
		IndexService indexService = IndexServiceFactory.getLocalIndexService("BBS");
		
		for(int i = 0 ; i <  20 ; i++){
			//一个需要建索引的JavaBean
			SampleJavaBean bean = new SampleJavaBean();
			bean.setCheckFlag(true);
			bean.setRegistTime(new Date());
			bean.setUrl("http://sample.lucimint.org");
			bean.setUserName("LuciMint" + i);
			bean.setUuid(20000 + i);
			try {
				indexService.add(bean);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Thread.sleep(1000);
			System.out.println("*****************************");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//**********************************查询部分
		//编写查询逻辑
		String queryString = "url='http://sample.lucimint.org'";
		QueryResults queryResults;
		try {
			queryResults = indexService.query(queryString, 1, 20, true);
			System.out.println("PageNo :" + queryResults.getPageNo());
			System.out.println("PageSize :" + queryResults.getPageSize());
			System.out.println("TotalHit :" + queryResults.getTotalHit());
			System.out.println("TotalPage :" + queryResults.getTotalPage());
			
			List<SampleJavaBean> beanList = queryResults.getResultBeans(SampleJavaBean.class); //读取具体的数据列表
			
			for(SampleJavaBean bean : beanList){
				System.out.println(bean.getUuid() + " | " +bean.getUserName());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
