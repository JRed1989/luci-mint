/**
 * 
 */
package org.wltea.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 简易HTTP请求客户端
 * @author linliangyi
 *
 */
public class SimpleHttpClient {

	/**
	 * 简单的HTTP POST请求处理
	 * @param httpURL
	 * @param httpParamters
	 * @param encoding
	 * @return
	 * @throws IOException 
	 */
	public static byte[] post(URL httpURL , Map<String ,String> httpParamters , String encoding) throws IOException{
		if(httpURL == null){
			throw new IllegalArgumentException("Parameter 'httpURL' is undefined.");
		}
		if(encoding == null){
			encoding = "UTF-8";
		}
		
		//根据HTTP参数，构造HTTP POST的文本
		StringBuffer postContents = new StringBuffer();
		if(httpParamters != null){
			Set<Entry<String ,String>> entries = httpParamters.entrySet();
			for(Entry<String ,String> entry : entries){
				try {
					if(entry.getValue() != null){
						postContents.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue() , encoding)).append("&");
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			//删除尾部的&号
			postContents.deleteCharAt(postContents.length() - 1);
		}
		
		//开始HTTP请求
		HttpURLConnection urlconn = null;
		OutputStream os = null;
		InputStream in = null;		
	
		try {
			urlconn = (HttpURLConnection)httpURL.openConnection();
			//0.初始化连接
			urlconn.setConnectTimeout(3000);
			//更改ReadTimeOut，30分钟（当索引因为批量写入，可能出现optimization时，conn要支持阻塞）
			urlconn.setReadTimeout(30*60*1000);
			urlconn.setRequestMethod("POST");
			urlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encoding);
			urlconn.setDoInput(true);
			urlconn.setDoOutput(true);
			//1.发起URL连接请求
			urlconn.connect();
			os = urlconn.getOutputStream();
			os.write(postContents.toString().getBytes());
			os.flush();
			
			//2.读取应答
			if(HttpURLConnection.HTTP_OK == urlconn.getResponseCode()){
				in = urlconn.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count = 0;
				while((count = in.read(buffer)) > 0){
					baos.write(buffer, 0, count);
				}
				return baos.toByteArray();
			}else{
				String error = "HTTP " +  urlconn.getResponseCode() + "  error : " + urlconn.getResponseMessage();
				throw new IOException(error);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}finally{
			if(urlconn != null){
				urlconn.disconnect();
			}
			
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		}		
		return null;
	}
}
