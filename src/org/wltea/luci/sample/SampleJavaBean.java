/**
 * 
 */
package org.wltea.luci.sample;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.wltea.luci.annotation.FieldIndex;
import org.wltea.luci.annotation.FieldStore;
import org.wltea.luci.annotation.PKey;
import org.wltea.luci.client.XMLDataFormatter;
import org.wltea.luci.index.IndexConfig;
import org.wltea.luci.index.IndexDataAdapter;

/**
 * @author linliangyi
 *
 */
public class SampleJavaBean extends TestCase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7153417317917298956L;
	
	@PKey
	private int uuid;

	@FieldStore
	@FieldIndex("ANALYZED")
	private String userName;

	@FieldStore
	private boolean checkFlag;
	
	@FieldIndex
	private String url;
	
	@FieldStore
	@FieldIndex
	private Date registTime;

	public int getUuid() {
		return uuid;
	}

	public void setUuid(int uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isCheckFlag() {
		return checkFlag;
	}

	public void setCheckFlag(boolean checkFlag) {
		this.checkFlag = checkFlag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getRegistTime() {
		return registTime;
	}

	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}
	
	public void testBeanToXMLToDocument(){
		List<SampleJavaBean> beans = new ArrayList<SampleJavaBean>();
		
		SampleJavaBean bean = new SampleJavaBean();
		bean.setCheckFlag(true);
		bean.setRegistTime(new Date());
		bean.setUrl("http://www.sohu.com");
		bean.setUserName("林良益");
		bean.setUuid(10000);
		
		beans.add(bean);
		
		bean = new SampleJavaBean();
		bean.setCheckFlag(true);
		bean.setRegistTime(new Date());
		bean.setUrl("http://www.sohu.com");
		bean.setUserName("蔡剑锋");
		bean.setUuid(10001);
		
		beans.add(bean);

	
		String xmlString = XMLDataFormatter.createXMLfromBeans(beans);
		System.out.println(xmlString);
		
		System.out.println(" ******************* ");
		
		IndexConfig indexConfig = new IndexConfig();
		indexConfig.setKeyFieldName("uuid");
		IndexDataAdapter indexDataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> docs = indexDataAdapter.xmlToDocument(xmlString);
		System.out.println(docs.get(0));
		System.out.println(docs.get(1));
		
	}	
	
}
