/**
 * 
 */
package org.wltea.luci.index;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import com.sun.xml.internal.stream.XMLInputFactoryImpl;

/**
 * 索引数据适配器
 * 转化索引数据格式
 * @author linliangyi
 *
 */
public class IndexDataAdapter {
	
	//Field常量，用来代表一个可以被忽略的Field
	private static org.apache.lucene.document.Field 
			IGNORE_FIELD = new org.apache.lucene.document.Field("Luci.IGNORE_FIELD","",Store.YES,Index.NO);
	/*
	 * 使用sun的默认实现,初始化XML解析器工
	 */
	private XMLInputFactory xmlInputFactory;
	/*
	 * 索引配置实例
	 */
	private IndexConfig indexConfig;
	
	public IndexDataAdapter(IndexConfig indexConfig){
		this.indexConfig = indexConfig;
		this.xmlInputFactory = new XMLInputFactoryImpl();
	}
	
	/**
	 * 从XML String ，并生成Document 
	 * @param xmlDataString
	 * @return
	 */
	public List<Document> xmlToDocument(String xmlDataString){
		if(xmlDataString == null){
			return new ArrayList<Document>(0);
		}
		StringReader xmlReader = new StringReader(xmlDataString);
		return this.xmlToDocument(xmlReader);
	}

	/**
	 * 从Reader接口读取XML，并生成Document 
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
	 * @param xmlReader
	 * @return
	 */
	public List<Document> xmlToDocument(Reader xmlReader){		
		
		List<Document> docs = new ArrayList<Document>();
		
		XMLStreamReader streamReader = null;
		try {
			boolean foundPkey = false;
			streamReader = xmlInputFactory.createXMLStreamReader(xmlReader);
			int event = 0;
			Stack<Object> xmlElements = new Stack<Object>();
			
			while (streamReader.hasNext()) {
				event = streamReader.next();
				switch (event) {
				case XMLStreamConstants.START_DOCUMENT:
					//文档开始
					break;
				case XMLStreamConstants.START_ELEMENT:
					//xml标签开始
					//记录当前的标签
					String elementName = streamReader.getName().toString();
					if("index-data".equals(elementName)){
						//XML根节点
					}else if("document".equals(elementName)){
						//如果是一个Record的开始，根据docType属性，创建一个Record
						Document doc = new Document();
						xmlElements.push(doc);
						foundPkey = false;

					}else if("field".equals(elementName)){
						//读取name\pkey\index\store属性						
						String name = null;
						boolean pkey = false;
						boolean store = false;
						String index = null;
						int attrCount = streamReader.getAttributeCount();
						for(int i = 0 ; i < attrCount ; i++){
							String attrName = streamReader.getAttributeName(i).toString();
							String attrValue = streamReader.getAttributeValue(i);
							
							if("name".equals(attrName)){
								name = attrValue;
									
							}else if(!foundPkey && "pkey".equals(attrName)){
								pkey = Boolean.parseBoolean(attrValue);
								
							}else if("store".equals(attrName)){
								store = Boolean.parseBoolean(attrValue);
								
							}else if("index".equals(attrName)){
								index = attrValue;
								
							}
						}
						
						//根据XML属性，设定Lucene Field的属性
						String docFieldName = name;	
						Store docFieldStore = null;
						Index docFieldIndex = null;
						if(pkey){
							foundPkey = true;
							//校验PKey FieldName
							if(docFieldName == null 
									|| this.indexConfig.getKeyFieldName() == null
									|| !docFieldName.equals(this.indexConfig.getKeyFieldName())
									){
								throw new IllegalArgumentException("数据对象PKey属性校验失败，名称为空或不匹配!");
							}
							//PKey必须存储
							docFieldStore = Store.YES;
							//PKey索引，不切分
							docFieldIndex = Index.NOT_ANALYZED_NO_NORMS;					
							
						}else{
							if(store){
								docFieldStore = Store.YES;
							}else{
								docFieldStore = Store.NO;
							}
							if(index != null){
								if("ANALYZED".equals(index)){
									docFieldIndex = Index.ANALYZED_NO_NORMS;
								}else{
									docFieldIndex = Index.NOT_ANALYZED_NO_NORMS;
								}
							}else{
								docFieldIndex = Index.NO;
							}
						}
						
						//构造Lucene Field,暂时不设定值
						org.apache.lucene.document.Field docField = null;
						if(docFieldStore == Store.NO && docFieldIndex == Index.NO){	
							docField = IGNORE_FIELD;
						}else{
							docField = new org.apache.lucene.document.Field(
									docFieldName , 
									"",
									docFieldStore,
									docFieldIndex);
						}
						//把当前的docField压入栈顶
						xmlElements.push(docField);	
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					//xml标签CDATA内容
					//跳过空格
					if (streamReader.isWhiteSpace()){
						break;
					}
					//读出栈顶的Field
					org.apache.lucene.document.Field docField 
							= (org.apache.lucene.document.Field)xmlElements.peek();
					if(IGNORE_FIELD != docField){
						//读出xml field值
						String docFieldValue = streamReader.getText();
						docField.setValue(docFieldValue);
					}
			
					break;		 		
				case XMLStreamConstants.END_ELEMENT:
					//XML元素结束
					elementName = streamReader.getName().toString();
					if("field".equals(elementName)){
						//弹出出栈顶的Field（必须的）
						docField = (org.apache.lucene.document.Field)xmlElements.pop();
						if(IGNORE_FIELD != docField){
							//读出栈顶的Document（不是弹出哦！！）
							Document doc = (Document)xmlElements.peek();
							//加入当前的docField
							doc.add(docField);
						}
						
					}else if("document".equals(elementName)){
						if(!foundPkey){
							throw new IllegalArgumentException("数据对象缺少PKey属性!");
						}
						//弹出栈顶的Document
						Document doc = (Document)xmlElements.pop();
						//加入结果集
						docs.add(doc);
						
					}else if("index-data".equals(elementName)){
						//XML根元素退出
					}
					break;		 		
				case XMLStreamConstants.END_DOCUMENT:
					//文档结束
					break;
				}			
			}			
			
		} catch (XMLStreamException e) {
			e.printStackTrace();	
		}finally {
			if(streamReader != null){
				try {
					streamReader.close();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}
		}		
		return docs;		
	}	
}
