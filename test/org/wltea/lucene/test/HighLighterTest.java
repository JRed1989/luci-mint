/**
 * 
 */
package org.wltea.lucene.test;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKQueryParser;

/**
 * @author linliangyi
 *
 */
public class HighLighterTest extends TestCase {
  
	public void testFormatter(){
		
//		//根据命名类获取Luci索引器实例
//		IndexService indexService = IndexServiceFactory.getLocalIndexService("BBS");
//		
//		for(int i = 0 ; i <  20 ; i++){
//			//一个需要建索引的JavaBean
//			SampleJavaBean bean = new SampleJavaBean();
//			bean.setCheckFlag(true);
//			bean.setRegistTime(new Date());
//			bean.setUrl("http://sample.lucimint.org");
//			bean.setUserName("福州大学周报的必胜客" + i);
//			bean.setUuid(20000 + i);
//			indexService.add(bean);
//		}
//		
//		try {
//			Thread.sleep(1000);
//			System.out.println("*****************************");
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}			
		
//		Analyzer ika = new StandardAnalyzer(Version.LUCENE_30);
		Analyzer ika = new IKAnalyzer();
		String queryString = "1:'周边福州大学'";
		Query query = IKQueryParser.parse(queryString);
		
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span style='color:red'>" , "</span>");
		Highlighter highlighter = new Highlighter(htmlFormatter,  new QueryScorer(query));	
		
		try {
			String str = highlighter.getBestFragment(ika ,"" , "福州大学周边的必胜客");
			System.out.println(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		
		
	}
	 
}
