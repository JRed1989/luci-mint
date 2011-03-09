/**
 * 
 */
package org.wltea.lucene.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.wltea.luci.client.QueryResults;
import org.wltea.luci.index.IndexConfig;
import org.wltea.luci.index.IndexDataAdapter;
import org.wltea.luci.sample.SampleJavaBean;
import org.wltea.luci.search.PagedResultSet;

/**
 * @author linliangyi
 *
 */
public class SearchResultTest extends TestCase {
	
	
	public void testPagedResultSetToJSON(){
		IndexConfig indexConfig = new IndexConfig();
		indexConfig.setKeyFieldName("uuid");
		IndexDataAdapter indexDataAdapter = new IndexDataAdapter(indexConfig);
		InputStream is = SampleJavaBean.class.getResourceAsStream("sample.xml");
		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(is,"UTF-8");
			List<Document> docs = indexDataAdapter.xmlToDocument(isReader);
			System.out.println(docs.get(0));
			System.out.println(docs.get(1));
			
			System.out.println(" ******************* ");
			
			PagedResultSet prs = new PagedResultSet();
			prs.setPageNo(2);
			prs.setPageSize(20);
			prs.setTotalHit(33);
			prs.setResultDocument(docs.toArray(new Document[0]));
			
			//JSON转化输出
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES , false);
		    
		    try {
			    String jsonString = mapper.writeValueAsString(prs);
				System.out.println(jsonString);
				System.out.println(" ******************* ");

				QueryResults qr = mapper.readValue(jsonString, QueryResults.class);
				
			    System.out.println(qr.getPageNo());
			    System.out.println(qr.getPageSize());
			    System.out.println(qr.getTotalHit());
			    System.out.println(qr.getTotalPage());
			    System.out.println(qr.getResultBeans(SampleJavaBean.class).get(0).getUserName());
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void testSortToJSON(){
		//JSON转化输出
	    ObjectMapper mapper = new ObjectMapper();
		Sort querySort = new Sort(new SortField(null , SortField.DOC ,true) , new SortField("uuid" , SortField.INT ,true));
		try {
			//String jsonString = mapper.writeValueAsString(querySort);
			String jsonString = mapper.writeValueAsString(new SortField(null , SortField.DOC ,true));
			System.out.println(jsonString);
			System.out.println(" ******************* ");
			
			SortField jsonBack = mapper.readValue(jsonString, SortField.class);
			System.out.println(jsonBack);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
