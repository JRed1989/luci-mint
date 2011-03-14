package org.wltea.luci.rpc.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.codehaus.jackson.map.ObjectMapper;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.luci.index.IndexContext;
import org.wltea.luci.index.IndexContextContainer;
import org.wltea.luci.search.PagedResultSet;
import org.wltea.web.util.HttpRequestHelper;
/**
 * LuciMint索引服务RPC HTTP协议服务端Servelt
 * 提供以下索引服务
 * build ： 新建索引
 * backup ： 备份索引
 * add ： 新增索引
 * update ： 修改索引
 * delete ： 删除索引
 * optimize ： 优化索引
 * query ： 查询索引
 * 
 * @author linliangyi
 *
 */
public class LucIndexServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8716697712905428514L;

	/**
	 * Constructor of the object.
	 */
	public LucIndexServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String errorMessage = null;
		
		//索引名称
		String indexName = request.getParameter("index-name");
		//执行操作
		String operate = request.getParameter("operate");
		
		if(indexName == null){
			errorMessage = "HTTP parameter 'indexName' is null.";
			this.outputError(response , errorMessage);
			return;
		}
		if(operate == null){
			errorMessage = "HTTP parameter 'operate' is null.";
			this.outputError(response , errorMessage);
			return;
		}
		//根据indexName加载索引上下文
		IndexContext indexContext = IndexContextContainer.loadIndexContext(indexName);
		if(indexContext == null){
			errorMessage = "No Found index named '" + indexName + "'";
			this.outputError(response , errorMessage);
			return;
		}
				
		//build ： 新建索引
		if("build".equalsIgnoreCase(operate)){
			this.opBuild(request, response, indexContext);
			return;

		//backup ： 备份索引
		}else if("backup".equalsIgnoreCase(operate)){
			this.opBackup(request, response, indexContext);
			return;
		
		//add ： 新增索引	
		}else if("add".equalsIgnoreCase(operate)){
			this.opAdd(request, response, indexContext);
			return;
			
		//update ： 修改索引	
		}else if("update".equalsIgnoreCase(operate)){
			this.opUpdate(request, response, indexContext);
			return;
			
		//delete ： 删除索引	
		}else if("delete".equalsIgnoreCase(operate)){
			this.opDelete(request, response, indexContext);
			return;
			
		//optimize ： 优化索引
		}else if("optimize".equalsIgnoreCase(operate)){
			this.opOptimize(request, response, indexContext);
			return;

		//optimizeBackup ： 优化备份索引
		}else if("optimizeBackup".equalsIgnoreCase(operate)){
			this.opOptimizeBackup(request, response, indexContext);
			return;
			
		//query ： 查询主索引	
		}else if("query".equalsIgnoreCase(operate)){
			this.opQuery(request, response, indexContext);
			return;
			
		//query ： 查询主索引	
		}else if("queryBackup".equalsIgnoreCase(operate)){
			this.opQueryBackup(request, response, indexContext);
			return;
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}
	
	/**
	 * Build操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opBuild(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		String xmlDataString = request.getParameter("xml-data");
		if(xmlDataString != null){
			indexContext.build(xmlDataString);
		}
	}

	/**
	 * backup操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opBackup(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		String xmlDataString = request.getParameter("xml-data");
		if(xmlDataString != null){
			indexContext.backup(xmlDataString);
		}
	}		
		
	
	/**
	 * add操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opAdd(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		String xmlDataString = request.getParameter("xml-data");
		if(xmlDataString != null){
			indexContext.add(xmlDataString);
		}
	}
	
	/**
	 * update操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opUpdate(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		String xmlDataString = request.getParameter("xml-data");
		if(xmlDataString != null){
			indexContext.update(xmlDataString);
		}
	}		

	/**
	 * Delete操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opDelete(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		String xmlDataString = request.getParameter("xml-data");
		if(xmlDataString != null){
			indexContext.delete(xmlDataString);
		}
	}
	
	/**
	 * optimize操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opOptimize(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		boolean immediately = HttpRequestHelper.getBooleanParameter(request, "right-now", false);
		indexContext.optimize(immediately);
	}
	
	/**
	 * OptimizeBackup操作
	 * @param request
	 * @param response
	 * @param indexContext
	 */
	private void opOptimizeBackup(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext){
		boolean immediately = HttpRequestHelper.getBooleanParameter(request, "right-now", false);
		indexContext.optimizeBackupIndex(immediately);

	}
	
	/**
	 * Query操作
	 * @param request
	 * @param response
	 * @param indexContext
	 * @throws IOException
	 */
	private void opQuery(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext)throws IOException{
		this.query(request, response, indexContext, false);
	}
	
	/**
	 * QueryBackup操作
	 * @param request
	 * @param response
	 * @param indexContext
	 * @throws IOException
	 */
	private void opQueryBackup(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext)throws IOException{
		this.query(request, response, indexContext, true);
	}
		
	/**
	 * 
	 * @param request
	 * @param response
	 * @param indexContext
	 * @throws IOException 
	 */
	private void query(HttpServletRequest request, HttpServletResponse response , IndexContext indexContext , boolean inBackupIndex) throws IOException{
		
		PagedResultSet pagedResultSet = new PagedResultSet();
		
		String queryString = request.getParameter("query");
		if(queryString != null && !"".equals(queryString.trim())){			
			//页码
			int pageNo = HttpRequestHelper.getIntParameter(request, "page-no", 1);
			//每页大小
			int pageSize = HttpRequestHelper.getIntParameter(request, "page-size", 20);
			//是否倒序
			boolean reverse = HttpRequestHelper.getBooleanParameter(request, "sort-reverse", true);
			
			//初始化默认排序方式
			Sort querySort = new Sort(new SortField(null , SortField.DOC ,reverse));
			//排序类型
			String sortType = request.getParameter("sort-type");
			
			if(sortType == null || "DOC".equals(sortType)){
				//使用lucene docid 默认排序				
			}else if("SCORE".equals(sortType)){
				//使用Lucene相识度评分排序
				querySort = new Sort(new SortField(null , SortField.SCORE ,reverse));
			}else {
				//获取指定的排序字段
				String sortFieldName = request.getParameter("sort-by");
				if(sortFieldName == null){
					String errorMessage = "Unkown query mode. 'sortFieldName' is null.";
					this.outputError(response , errorMessage);
					return;
				}
				if("BYTE".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.BYTE ,reverse));
				}else if("SHORT".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.SHORT ,reverse));
				}else if("INT".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.INT ,reverse));
				}else if("LONG".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.LONG ,reverse));
				}else if("FLOAT".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.FLOAT ,reverse));
				}else if("DOUBLE".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.DOUBLE ,reverse));
				}else if("STRING".equals(sortType)){
					querySort = new Sort(new SortField(sortFieldName , SortField.STRING ,reverse));
				}else {
					String errorMessage = "Unkown query mode. 'sortType' is Unkown.";
					this.outputError(response , errorMessage);
					return;
				}				
			}
			//解析query String
			Query query = IKQueryParser.parse(queryString);			
			pagedResultSet = indexContext.search(query, pageNo, pageSize, querySort, inBackupIndex);
		}		
		//输出JSON结果
		this.outputQueryResults(response, pagedResultSet);		
	}

	/**
	 * JSON输出查询结果
	 * @param response
	 * @param results
	 * @throws IOException
	 */
	private void outputQueryResults(HttpServletResponse response ,PagedResultSet pagedResultSet) throws IOException{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		//JSON转化输出
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.writeValue(writer, pagedResultSet);
	}
	
	/**
	 * 反馈错误信息
	 * @param response
	 * @param errorMessage
	 * @throws IOException
	 */
	private void outputError(HttpServletResponse response , String errorMessage) throws IOException{
		response.setContentType("text/html;charset=UTF-8");
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR , errorMessage);
	}	
}
