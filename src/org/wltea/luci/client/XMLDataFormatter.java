/**
 * 
 */
package org.wltea.luci.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.wltea.luci.annotation.FieldIndex;
import org.wltea.luci.annotation.FieldStore;
import org.wltea.luci.annotation.PKey;


/**
 * 索引数据格式转换
 * Bean 转 XML
 * @author linliangyi
 *
 */
public class XMLDataFormatter extends BasicDataFormatter{

	/**
	 * 从javabean列表生成XML String
	 *  XML DEMO :
	 * <index-data> 
	 *		<document>
	 * 			<field name="uuid"  pkey="true">
	 * 				10000
	 * 			</field>
	 * 	    	<field name="userName"  store="true" index="ANALYZED">
	 * 				LuciMint的首个用户
	 * 			</field>
	 * 			<field name="url" store="true">
	 * 				<![CDATA[http://www.sohu.com]]>
	 *      	</field>
	 *  		<field name="registTime" store="true" index="NO_ANALYZED" >
	 *  			10000
	 *  		</field>
	 * 		</document>
	 * </index-data> 
	 * @param dataBeans
	 * @return
	 */
	public static String createXMLfromBeans(List<?> dataBeans){
		Elemet_IndexData eleIndexData = new Elemet_IndexData();
		if(dataBeans != null){
			for(Object dataBean : dataBeans){
				Element_Document document = beanToXML(dataBean);
				eleIndexData.addDocument(document);
			}			
		}
		return eleIndexData.toXML();
	}
	
	/**
	 * 解析单个Bean对象
	 * @param dataBean
	 * @return
	 */
	private static Element_Document beanToXML(Object dataBean){
		Element_Document eleDoc = new Element_Document();
		//获取data bean的类对象
		Class<?> dataBeanClass = dataBean.getClass();
		//获取当前Bean中声明的所有属性（不包括继承的类）
		Field[] fields = dataBeanClass.getDeclaredFields();
		//将Bean的属性转成lucene document的Field
		boolean foundPkey = false;
		for(Field beanField : fields){
			beanField.setAccessible(true);
			//忽略serialVersionUID属性
			if (beanField.getName().equals("serialVersionUID")){
				continue;
			}
			//忽略没有索引相关注释的属性
			if(beanField.getAnnotation(PKey.class) == null
					&& beanField.getAnnotation(FieldStore.class) == null
					&& beanField.getAnnotation(FieldIndex.class) == null){
				continue;
			}
			String docFieldValue = readDocFieldValue(beanField , dataBean);
			//对非空值属性，添加到索引中,忽略null值属性
			if(docFieldValue != null){
				Element_Field eleField = new Element_Field();
				eleField.name = beanField.getName();
				eleField.value = escapeHTMLTag(regularizeXmlString(docFieldValue));
					
				//处理PKey属性
				PKey pKeyAnno = beanField.getAnnotation(PKey.class);
				if(!foundPkey && pKeyAnno != null){
					foundPkey = true;
					eleField.pkey = true;
				}else{
					eleField.store = readDocFieldStore(beanField);
					eleField.index = readDocFieldIndex(beanField);
				}	
				
				eleDoc.addField(eleField);
			}
		}

		//没有找到主键，抛异常
		if(!foundPkey){
			throw new IllegalArgumentException("数据对象缺少PKey属性!");
		}
		return eleDoc;
	}
	

	/**
	 * XML <index-data> 元素
	 * @author linliangyi
	 *
	 */
	static class Elemet_IndexData{
		private List<Element_Document> documents;
		
		public Elemet_IndexData(){
			this.documents =  new ArrayList<Element_Document>();
		}
		
		public void addDocument(Element_Document document){
			this.documents.add(document);
		}
		
		public String toXML(){
			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\r\n");
			sb.append("<index-data>").append("\r\n");
			for(Element_Document document : this.documents){
				sb.append(document.toXML());
			}
			sb.append("</index-data>").append("\r\n");
			return sb.toString();
		}
	}
	
	
	/**
	 * XML <document> 元素
	 * @author linliangyi
	 *
	 */
	static class Element_Document{
		private List<Element_Field> fields;
		
		public Element_Document(){
			this.fields = new ArrayList<Element_Field>();
		}
		
		public void addField(Element_Field field){
			this.fields.add(field);
		}
		
		public String toXML(){
			StringBuffer sb = new StringBuffer();
			sb.append("\t").append("<document>").append("\r\n");
			for(Element_Field field : this.fields){
				sb.append("\t").append(field.toXML());
			}
			sb.append("\t").append("</document>").append("\r\n");
			return sb.toString();
		}
	}

	/**
	 * XML <field> 元素
	 * @author linliangyi
	 *
	 */
	static class Element_Field{
		private String name;
		private String value;
		private boolean pkey;
		private String store;
		private String index;
		
		public Element_Field(){};
		
		public String toXML(){
			StringBuffer sb = new StringBuffer();
			//name
			sb.append("\t").append("<field ")
				.append("name=\"").append(name).append("\" ");
			//pkey
			if(pkey){
				sb.append("pkey=\"").append(pkey).append("\" ");
			}
			//store
			if(FieldStore.YES.equals(store)){
				sb.append("store=\"").append(true).append("\" ");
			}
			//index
			if(FieldIndex.NOT_ANALYZED.equals(index)){
				sb.append("index=\"").append(FieldIndex.NOT_ANALYZED).append("\" ");
			}else if(FieldIndex.ANALYZED.equals(index)){
				sb.append("index=\"").append(FieldIndex.ANALYZED).append("\" ");				
			}
			//value
			sb.append(" >")
				.append("<![CDATA[").append(value).append("]]>")
				.append("</field>").append("\r\n");
			
			return sb.toString();
		}
	}
	
	/**
	 * 过滤XML中的非法字符
	 * @param strInput
	 * @return
	 */
	public static String regularizeXmlString(String strInput){
		String emptyString = "";
		if(strInput == null || strInput.length() == 0){
			return emptyString;
		}
		String result = strInput.replaceAll("[\\x00-\\x08|\\x0b-\\x0c|\\x0e-\\x1f]",emptyString);
		return result;
	}
	
	/**
	 * 过来HTML标签
	 * @param strInput
	 * @return
	 */
	public static String escapeHTMLTag(String strInput){
		String emptyString = "";
		if(strInput == null || strInput.length() == 0){
			return emptyString;
		}
		String result = strInput.replaceAll("<[^>]*>",emptyString);
		return result;
	}
	
}
