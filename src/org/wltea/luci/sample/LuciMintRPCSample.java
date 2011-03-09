/**
 * 
 */
package org.wltea.luci.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.wltea.luci.client.IndexService;
import org.wltea.luci.client.IndexServiceFactory;
import org.wltea.luci.client.QueryResults;

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
		
		//唯一不一样的就是这里，根据索引命名，获取Luci远程索引器实例   
        IndexService indexService = IndexServiceFactory.getRemoteIndexService("SAMPLE", remoteHttpURL);   
        //使用带注释的Bean，创建索引   
        for(int i = 0 ; i <  20 ; i++){   
            //一个需要建索引的JavaBean   
            SampleJavaBean bean = new SampleJavaBean();   
            bean.setCheckFlag(true);   
            bean.setRegistTime(new Date());   
            bean.setUrl("http://sample.lucimint.org");   
            bean.setUserName("LuciMint" + i);   
            bean.setUuid(20000 + i);   
             //新增索引   
            indexService.add(bean);   
        }   
           
        try {   
            Thread.sleep(3000);   
            System.out.println("*****************************");   
        } catch (InterruptedException e) {   
            e.printStackTrace();   
        }      
           
        //**********************************查询部分   
        //编写查询逻辑   
        String queryString = "url='http://sample.lucimint.org'";   
         //查询索引也只要一句,    
        QueryResults queryResults = indexService.query(queryString, 1, 20, true);   
        System.out.println("PageNo :" + queryResults.getPageNo());   
        System.out.println("PageSize :" + queryResults.getPageSize());   
        System.out.println("TotalHit :" + queryResults.getTotalHit());   
        System.out.println("TotalPage :" + queryResults.getTotalPage());   
        //读取具体的数据列表 , 传入你的bean类型，Luci将帮你封装好结果集   
        List<SampleJavaBean> beanList = queryResults.getResultBeans(SampleJavaBean.class);   
           
           
        for(SampleJavaBean bean : beanList){   
            System.out.println(bean.getUuid() + " | " +bean.getUserName());   
        }   

	}
	
}
