/**
 * 
 */
package org.wltea.luci.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import org.wltea.luci.search.PagedResultSet;


/**
 * 用户中心玩家信息索引上下文
 * 
 * 每个索引索引控制器上下文包含一类（docType）的索引目录
 * 同一类索引目录包括：内存及时索引 ； 主索引 两个部分
 * 
 * ---内存及时索引
 * 提供及时搜索功能
 * 在记录归并到文件索引后，及时删除对应记录
 * 
 * ---主索引
 * 文件索引保存近期的索引记录
 * 支持三种触发条件
 * 1.队列中，任务数达到触发点（例如：500条）
 * 2.索引间隔时间到达触发点（例如：60秒）
 * 3.管理员手动触发
 * 
 * ---备份索引
 * 涵盖主索引内容和之前的全部内容
 *  
 * @author 林良益（linliangyi2005@gmail.com)
 * Sep 7, 2010
 */
public class IndexContext {
	/*
	 * 索引配置参数
	 */
	private IndexConfig indexConfig;
	/*
	 * 内存实时索引目录
	 */
	private Directory memIndexDir;
	/*
	 * 内存索引控制器
	 */
	private MemoryIndexController memoryIndexController;
	/*
	 * 磁盘索引控制器
	 */
	private MainIndexController mainIndexController;
	/*
	 * 备份索引控制器
	 */
	private BackupIndexController backupIndexController;
	
	

	/*
	 * 内存索引reader
	 */
	private IndexReader memoryReader;
	private Object memoryReaderLock = new Object();		

	/*
	 * 主索引reader
	 */
	private IndexReader mainReader;
	private Object mainReaderLock = new Object();
	
	/*
	 * 备份索引reader
	 */
	private IndexReader backupReader;
	private Object backupReaderLock;	

	/*
	 * 主索引优化标志
	 */
	private boolean memoryIndexOptFlag;
	/*
	 * 主索引优化标志
	 */
	private boolean mainIndexOptFlag;
	
	/*
	 * 备份引优化标志
	 */
	private boolean backupIndexOptFlag;

	/*
	 * 索引优化，清理定时器
	 */
	private Timer indexTimer;
	/*
	 * 主索引迁移（清理）标志
	 */
	private boolean cleanupFlag;	
	
	/**
	 * 构造函数
	 * @param indexConfig 索引参数配置
	 */
	public IndexContext(IndexConfig indexConfig){
		this.indexConfig = indexConfig;
		//初始化内存索引目录
		this.memIndexDir = new RAMDirectory(); 
		//初始化内存索引控制器
		this.memoryIndexController = new MemoryIndexController(this);
		this.memoryIndexOptFlag = false;
		//初始化主文件索引控制器
		this.mainIndexController = new MainIndexController(this);
		this.mainIndexOptFlag = false;
		
		//如果开启了备份索引，则初始化
		if(indexConfig.isEnableBackup()){
			this.backupIndexController = new BackupIndexController(this);
			this.backupReaderLock = new Object();
			this.backupIndexOptFlag = false;
		}
		

		//初始化定时迁移任务
		this.indexTimer = new Timer(true);
		this.indexTimer.schedule(new IndexMaintianTimerTask(this) ,
				this.indexConfig.getMaintainTaskIdlePeriod() ,
				this.indexConfig.getMaintainTaskIdlePeriod());
	}
	
	/**
	 * 关闭整个context上下文
	 */
	public void close(){
		//关闭内存索引
		synchronized(this.memoryReaderLock){
			if(this.memoryReader != null){
				try {
					this.memoryReader.close();
					this.memoryReader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.memoryIndexController.stopService();
		
		//关闭主索引
		synchronized(this.mainReaderLock){
			if(this.mainReader != null){
				try {
					this.mainReader.close();
					this.mainReader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.mainIndexController.stopService();
		
		//关闭备份索引
		if(indexConfig.isEnableBackup()){
			synchronized(this.backupReaderLock){
				if(this.backupReader != null){
					try {
						this.backupReader.close();
						this.backupReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			this.backupIndexController.stopService();
		}
		//关闭定时器
		this.indexTimer.cancel();
	}	
	
	public IndexConfig getIndexConfig() {
		return indexConfig;
	}
	
	public Directory getMemIndexDir() {
		return memIndexDir;
	}
	
	public MemoryIndexController getMemoryIndexController() {
		return memoryIndexController;
	}
	
	public MainIndexController getMainIndexController() {
		return mainIndexController;
	}
	
	public BackupIndexController getBackupIndexController(){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		return backupIndexController;
	}
	
	/**
	 * 建立主索引
	 * @param xmlDataString 
	 */
	public void build(String xmlDataString){
		IndexDataAdapter dataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> indexDocs = dataAdapter.xmlToDocument(xmlDataString);
		for(Document doc : indexDocs){
			this.build(doc);
		}
	}
	
	/**
	 * 建立主索引
	 * @param doc 
	 */
	public void build(Document doc){
		if(doc == null){
			return;
		}
		IndexCommand command = new IndexCommand(IndexCommand.Operate.BUILD , doc);
		this.mainIndexController.sendCommand(command, false);
	}	
	
	/**
	 * 备份索引(直接对备份索引插入数据)
	 * @param xmlDataString 
	 */
	public void backup(String xmlDataString){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		IndexDataAdapter dataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> indexDocs = dataAdapter.xmlToDocument(xmlDataString);
		for(Document doc : indexDocs){
			this.backup(doc);
		}
	}
	
	/**
	 * 建立备份索引
	 * @param doc 
	 */
	public void backup(Document doc){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		if(doc == null){
			return;
		}
		IndexCommand command = new IndexCommand(IndexCommand.Operate.BUILD , doc);
		this.backupIndexController.sendCommand(command, false);
	}		
	
	/**
	 * 新增索引（可以批量）
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
	 * @param xmlDataString
	 */
	public void add(String xmlDataString){
		IndexDataAdapter dataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> indexDocs = dataAdapter.xmlToDocument(xmlDataString);
		for(Document doc : indexDocs){
			this.add(doc);
		}
	}
	
	/**
	 * 新增索引
	 * @param doc
	 */
	public void add(Document doc){
		if(doc == null){
			return;
		}
		IndexCommand command = new IndexCommand(IndexCommand.Operate.ADD , doc);
		this.memoryIndexController.sendCommand(command);
	}	

	/**
	 * 修改索引
	 * @param xmlDataString
	 */
	public void update(String xmlDataString){
		IndexDataAdapter dataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> indexDocs = dataAdapter.xmlToDocument(xmlDataString);
		for(Document doc : indexDocs){
			this.update(doc);
		}
	}	

	/**
	 * 修改索引
	 * @param doc
	 */
	public void update(Document doc){
		if(doc == null){
			return;
		}
		IndexCommand command = new IndexCommand(IndexCommand.Operate.MODIFY , doc);
		this.memoryIndexController.sendCommand(command);
	}	
	
	/**
	 * 删除文档
	 * @param xmlDataString
	 */
	public void delete(String xmlDataString){
		IndexDataAdapter dataAdapter = new IndexDataAdapter(indexConfig);
		List<Document> indexDocs = dataAdapter.xmlToDocument(xmlDataString);
		for(Document doc : indexDocs){
			this.delete(doc);
		}
	}

	/**
	 * 删除文档
	 * @param doc
	 */
	public void delete(Document doc){
		if(doc == null){
			return;
		}
		IndexCommand command = new IndexCommand(IndexCommand.Operate.DELETE , doc);
		this.memoryIndexController.sendCommand(command);
	}	

	/**
	 * 优化主索引
	 * @param immediately
	 */
	public void optimize(boolean immediately){
		IndexCommand command = new IndexCommand(IndexCommand.Operate.OPTIMIZE , null);
		this.mainIndexController.sendCommand(command , immediately);
	}
	
	/**
	 * 优化备份索引
	 * @param immediately
	 */
	public void optimizeBackupIndex(boolean immediately){
		IndexCommand command = new IndexCommand(IndexCommand.Operate.OPTIMIZE , null);
		this.backupIndexController.sendCommand(command , immediately);
	}	

	/**
	 * 搜索索引，返回带翻页的文档集
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @param sort
	 * @param inBackupIndex
	 * @return
	 */
	public PagedResultSet search(Query query , int pageNo , int pageSize , Sort sort, boolean inBackupIndex){
		PagedResultSet pagedResultSet = new PagedResultSet();
		//1.参数校验
		if(query == null){
			return pagedResultSet;
		}
		if(sort == null){
			sort  = new Sort(new SortField(null , SortField.DOC ,true));
		}
		if(pageNo <=0 ){
			pageNo = 1;
		}
		pagedResultSet.setPageNo(pageNo);
		if(pageSize <= 0){
			pageSize = 20;
		}
		pagedResultSet.setPageSize(pageSize);
		
		//2.计算搜索规模
		long searchScale = pageNo * pageSize;
		//搜索规模过大
		if(searchScale >= Integer.MAX_VALUE){
			throw new IllegalArgumentException("搜索范围过大");
		}
		
		//3.获取查询器
		Searcher seeker = null;
		if(inBackupIndex && indexConfig.isEnableBackup()){
			seeker = this.getBackupIndexSearcher();
		}else{
			seeker = this.getIndexSearcher();
		}
	
		if(seeker == null){
			return pagedResultSet;
		}
		
		//4.计算结果集起始位置
		int resultBegin = (pageNo - 1) * pageSize;
		int resultEnd = (int)searchScale;
		
		try{
			//5.执行搜索
			TopDocs topDocs = seeker.search(query , null , (int)searchScale , sort);
			pagedResultSet.setTotalHit(topDocs.totalHits);
			//起始位置越界
			if(resultBegin > topDocs.totalHits){
				return pagedResultSet;
			}
			//计算结束位置
			if(resultEnd > topDocs.totalHits){
				resultEnd = topDocs.totalHits;
			}			
			//读取结果集
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			Document[] docs = new Document[resultEnd - resultBegin];
			for (int i = resultBegin; i < resultEnd; i++){
				Document resultDoc = seeker.doc(scoreDocs[i].doc);
				docs[i - resultBegin] = resultDoc;
			}
			pagedResultSet.setResultDocument(docs);
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			closeSearcher(seeker);
		}		
		return pagedResultSet;
	}
	
	/**
	 * 获取MemoryIndexReader
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public IndexReader getMemoryIndexReader(){
		IndexReader cloneReader = null;
		try{
			synchronized(this.memoryReaderLock){
				if(this.memoryReader == null){
					//索引存在，则构建读取器
					if(IndexReader.indexExists(this.memIndexDir)){
						System.out.println(new Date()+ " openMemoryIndexReader");
						this.memoryReader = IndexReader.open(this.memIndexDir , true);
					}				
					
				}else if(!this.memoryIndexOptFlag){
					IndexReader oldReader = this.memoryReader;
					//更新reader
					this.memoryReader = this.memoryReader.reopen(true);
					//System.out.println(new Date()+ " reopenMemoryIndexReader");
					if(this.memoryReader != oldReader){
						//关闭旧reader
						oldReader.close();
					}			
				}
				
				//克隆一份当前的reader， 该cloneReader会在外部的search中被关闭
				if(this.memoryReader != null){
					cloneReader = this.memoryReader.clone(true);
				}
			}
		} catch (CorruptIndexException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return cloneReader;
	}
	
	/**
	 * 获取主索引reader
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public IndexReader getMainIndexReader(){
		IndexReader cloneReader = null;
		try{
			synchronized(this.mainReaderLock){
				if(this.mainReader == null){
					//打开索引目录
					Directory indexDir = FSDirectory.open(this.indexConfig.getMainDirectory());
					//索引存在，则构建读取器
					if(IndexReader.indexExists(indexDir)){
						System.out.println(new Date()+ " openMainIndexReader");
						//打开只读reader
						this.mainReader = IndexReader.open(indexDir, true);
					}				
					
				}else if(!this.mainIndexOptFlag){
					IndexReader oldReader = this.mainReader;
					//更新reader
					this.mainReader = this.mainReader.reopen(true);
					//System.out.println(new Date()+ " reopenMainIndexReader");
					if(this.mainReader != oldReader){
						//关闭旧reader
						oldReader.close();
					}			
				}
				
				if(this.mainReader != null){
					cloneReader = this.mainReader.clone(true);
				}
			}
		} catch (CorruptIndexException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return cloneReader;
	}
	
	/**
	 * 获取备份索引reader
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public IndexReader getBackupIndexReader(){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		
		IndexReader cloneReader = null;
		try{
			synchronized(this.backupReaderLock){
				if(this.backupReader == null){
					//打开索引目录
					Directory indexDir = FSDirectory.open(this.indexConfig.getBackupDirectory());
					//索引存在，则构建读取器
					if(IndexReader.indexExists(indexDir)){
						System.out.println(new Date()+ " openBackupIndexReader");
						//打开只读reader
						this.backupReader = IndexReader.open(indexDir, true);
					}				
					
				}else if(!this.backupIndexOptFlag){
					IndexReader oldReader = this.backupReader;
					//更新reader
					this.backupReader = this.backupReader.reopen(true);
					//System.out.println(new Date()+ " reopenMainIndexReader");
					if(this.backupReader != oldReader){
						//关闭旧reader
						oldReader.close();
					}			
				}
				
				if(this.backupReader != null){
					cloneReader = this.backupReader.clone(true);
				}
			}
		} catch (CorruptIndexException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return cloneReader;
	}	

	public boolean isMemoryIndexOptimizing() {
		return memoryIndexOptFlag;
	}

	public boolean isMainIndexOptimizing() {
		return mainIndexOptFlag;
	}

	public boolean isBackupIndexOptimizing() {
		return backupIndexOptFlag;
	}
	
	public boolean isCleaning() {
		return cleanupFlag;
	}

	/**
	 * 获取文档主键查询条件
	 */
	Term keyTerm(Document doc){
		if(doc != null){
			String keyFieldName =  indexConfig.getKeyFieldName();
			String keyFieldValue = doc.get(keyFieldName);
			Term keyTerm = new Term(keyFieldName , keyFieldValue);
			return keyTerm;
		}
		return null;
	}

	/**
	 * 通知/解除内存索引优化状态
	 * 
	 * @param optimizing
	 */
	void notifyMemoryIndexOpt(boolean optimize){
		synchronized(this.memoryReaderLock){
			if(this.memoryReader != null){
				try {
					this.memoryReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.memoryReader = null;
			}
			this.memoryIndexOptFlag = optimize; 
		}
	}
	
	/**
	 * 通知/解除主索引优化状态
	 * 关闭当前的mainReader，尽可能节省优化时使用的磁盘空间
	 * @param optimizing
	 */
	void notifyMainIndexOpt(boolean optimize){
		synchronized(this.mainReaderLock){
			if(this.mainReader != null){
				try {
					this.mainReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.mainReader = null;
			}
			this.mainIndexOptFlag = optimize; 
		}
	}
	
	/**
	 * 通知/解除主索引优化状态
	 * 关闭当前的bakcupReader，尽可能节省优化时使用的磁盘空间
	 * @param optimizing
	 */
	void notifyBackupIndexOpt(boolean optimize){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		
		synchronized(this.backupReaderLock){
			if(this.backupReader != null){
				try {
					this.backupReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.backupReader = null;
			}
			this.backupIndexOptFlag = optimize; 
		}
	}	
	
	/**
	 * 获取索引搜索器
	 * @return
	 */
	private Searcher getIndexSearcher(){
		//1.获取内存索引读取器
		IndexReader memReader = this.getMemoryIndexReader();
		Searcher memSearcher = null;
		if(memReader != null){
			memSearcher = new IndexSearcher(memReader);
		}
		//2.获取主索引读取器
		IndexReader mainReader = this.getMainIndexReader();
		Searcher mainSearcher = null;
		if(mainReader != null){
			mainSearcher = new IndexSearcher(mainReader);
		}
		
		Searcher theSearcher = null;
		if(memSearcher != null && mainSearcher != null){
			try {
				//发现一个索引的排序问题，跟searcher的排序有关
				theSearcher = new MultiSearcher(new Searcher[]{mainSearcher , memSearcher});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(memSearcher != null){
			theSearcher = memSearcher;
		}else {
			theSearcher = mainSearcher;
		}		
		
		return theSearcher;
	}
	
	/**
	 * 获取备份索引搜索器
	 * @return
	 */
	private Searcher getBackupIndexSearcher(){
		if(!indexConfig.isEnableBackup()){
			throw new UnsupportedOperationException("没有可用的备份索引器!");
		}
		//1.获取索引读取器
		IndexReader backupReader = this.getBackupIndexReader();
		Searcher backupSearcher = null;
		if(backupReader != null){
			backupSearcher = new IndexSearcher(backupReader);
		}		
		return backupSearcher;
	}
	
	/**
	 * 递归关闭查询器
	 * 关闭查询器中的reader
	 * @param searchable
	 */
	private void closeSearcher(Searchable searchable){
		if(searchable != null){
			if(searchable instanceof MultiSearcher){
				Searchable[] searchables = ((MultiSearcher)searchable).getSearchables();
				for(Searchable s : searchables){
					closeSearcher(s);
				}
			}
			if(searchable instanceof IndexSearcher){
				IndexReader reader = ((IndexSearcher)searchable).getIndexReader();
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}	

	/**
	 * 从主索引删除过期的索引
	 * 每次迁移的文档数量不超过 indexConfig.getMaxMigrateDocs()
	 * 保持主索引容量的大小
	 * 
	 * 该方法仅在备份索引生效时执行
	 */
	public synchronized void  cleanup(){ 
		
		IndexEliminatePolicy policy = indexConfig.getEliminatePolicy();
		
		if(policy == null || this.cleanupFlag ){
			//正在执行迁移操作，忽略再次调用
			return;
		}
		//设置迁移标示
		this.cleanupFlag = true;
		//查询过期文档
		List<Document> overDueDocuments = queryOverDueDocuments(policy.getEliminateCondition(indexConfig));
		for(Document doc : overDueDocuments){
			//将文档从主索引移除
			this.mainIndexController.sendCommand(new IndexCommand(IndexCommand.Operate.CLEAR , doc), false);			
		}			
		this.cleanupFlag = false;
	}
	
	
	/**
	 * 根据Query查询主索引,查找要清理的文档
	 * 查询结果最多不超过 indexConfig.getMaxMigrateDocs()
	 * @param query 查询条件
	 * @return
	 * @throws IOException 
	 */
	private List<Document> queryOverDueDocuments(Query query){
		if(query != null){
			IndexReader mainIndexReader = null;
			try{
				//获取主索引读取器
				mainIndexReader = this.getMainIndexReader();
				if(mainIndexReader != null){
					List<Document> docs = new ArrayList<Document>();
					//构建搜索器
					Searcher searcher = new IndexSearcher(mainIndexReader);
					//搜索文档ID
					TopDocs topDocs = searcher.search(query, this.indexConfig.getMaxMigrateDocs());
					//取文档内容
					for(ScoreDoc scoreDoc : topDocs.scoreDocs){
						//取出文档内容
						Document doc = mainIndexReader.document(scoreDoc.doc);
						docs.add(doc);
					}
					return 	docs;				
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				if(mainIndexReader != null){
					try {
						mainIndexReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return new ArrayList<Document>(0);
	}

		
	/**
	 * 索引维护定时任务,
	 * 索引维护包含
	 * 1.每日的索引归档
	 * 2.主索引优化
	 * 3.历史索引优化
	 *  
	 * @author linliangyi
	 * Apr 13, 2010
	 */
	private class IndexMaintianTimerTask extends TimerTask{
		private IndexContext context;
		
		IndexMaintianTimerTask(IndexContext context){
			this.context = context;
		}
		
		@Override
		public void run() {	
			try{
				 Calendar rightNow = Calendar.getInstance();
				 //获取日期
				 //int dayOfMonth = rightNow.get(Calendar.DATE);
				 //获取小时
				 int hourOfDay = rightNow.get(Calendar.HOUR_OF_DAY);
				 //每天的1:00-4:00间，触发索引迁移
				 if(hourOfDay >= 1 && hourOfDay <4){
					 //如果主索引正在优化，则不允许执行迁移
					 if(!mainIndexOptFlag){
						 System.out.println(new Date() + " : " + indexConfig.getIndexName() + " begin migrate... ");
						 if(indexConfig.getEliminatePolicy() != null){
							 //主索引过期清理
							 this.context.cleanup();
						 }
						 //优化主索引
						 this.context.optimize(false);
					 }
					 //如果历史索引正在优化，则不再一次发起优化
					 if(indexConfig.isEnableBackup() && !backupIndexOptFlag){
						 //优化历史索引
						 this.context.optimizeBackupIndex(false);
					 }
				 }
			}catch(Exception ex){
				//捕获所有异常，避免线程中断
				ex.printStackTrace();
			}
		}		
	}

}
