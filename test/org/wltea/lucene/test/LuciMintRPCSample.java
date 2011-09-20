/**
 * 
 */
package org.wltea.lucene.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.wltea.luci.client.IndexService;
import org.wltea.luci.client.IndexServiceFactory;
import org.wltea.luci.client.QueryResults;
import org.wltea.luci.sample.SampleJavaBean;

/**
 * 远程调用例子
 * @author linliangyi
 *
 */
public class LuciMintRPCSample {
	
	/**
	 * 远程索引HelloWorld例子
	 * @param args
	 */
	public static void main(String[] args){		
	
		URL remoteHttpURL = null;
		try {			
			remoteHttpURL = new URL("http://10.5.21.86/sc/web/indexservice");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		//根据命名类获取Luci索引器实例
		IndexService indexService = IndexServiceFactory.getRemoteIndexService("SAMPLE", remoteHttpURL);
//		List<SampleJavaBean> beanList1 = new ArrayList<SampleJavaBean>();
//		for(int i = 0 ; i <  20 ; i++){
//			//一个需要建索引的JavaBean
//			SampleJavaBean bean = new SampleJavaBean();
//			bean.setCheckFlag(true);
//			bean.setRegistTime(new Date());
//			bean.setUrl("http://sample.lucimint.org");
//			bean.setUserName("LuciMint" + i);
//			bean.setUuid(20000 + i);
//			beanList1.add(bean);
//			indexService.add(bean);
//		}
//		indexService.add(beanList1);
//**********************************		
		
		
		SampleJavaBean bean = new SampleJavaBean();
		bean.setCheckFlag(true);
		bean.setRegistTime(new Date());
		bean.setUrl("http://sample.lucimint.org");
		bean.setUserName("LuciMint Test");
		bean.setUuid(30008);
//		indexService.build(bean);
//		indexService.add(bean);
//
//		try {
//			Thread.sleep(3000);
//			System.out.println("*****************************");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}	
		
		//**********************************查询部分
		//编写查询逻辑
		String queryString = "uuid='30008'";
		QueryResults queryResults;
		try {
			queryResults = indexService.query(queryString, 1, 20, true);
			System.out.println("PageNo :" + queryResults.getPageNo());
			System.out.println("PageSize :" + queryResults.getPageSize());
			System.out.println("TotalHit :" + queryResults.getTotalHit());
			System.out.println("TotalPage :" + queryResults.getTotalPage());
			
			List<SampleJavaBean> beanList = queryResults.getResultBeans(SampleJavaBean.class); //读取具体的数据列表
			
			for(SampleJavaBean b : beanList){
				System.out.println(b.getUuid() + " | " +b.getUserName());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
