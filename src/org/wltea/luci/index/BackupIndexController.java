/**
 * 
 */
package org.wltea.luci.index;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import org.wltea.luci.index.IndexCommand.Status;


/**
 * 全库备份索引控制器
 * @author linliangyi
 * Apr 20, 2010
 */
class BackupIndexController implements Runnable {
	/*
	 * 控制器名称
	 */
	private String name;
	
	/*
	 * 索引控制器上下文
	 */
	private IndexContext context; 
	
	/*
	 * 等待索引的文档队列 
	 */
	private IndexCommandQueue commandQueue;
	
	/*
	 * 待删除的文档
	 */
	private List<IndexCommand> toBeDeleted;
	
	/*
	 * 待新增的文档
	 */
	private List<IndexCommand> toBeAdded;
	
	/*
	 * 线程停止标识 
	 */
	private boolean stopFlag;
	
	/*
	 * 索引优化标志
	 */
	private boolean optimization;
	
	/**
	 * 
	 * @param context 索引控制器上下文
	 */
	BackupIndexController(IndexContext context){
		this.context = context;
		this.name = getClass().getSimpleName() + " for " + context.getIndexConfig().getIndexName();
		this.init();
	}
	
	/**
	 * 初始化索引控制器
	 */
	private void init(){
		this.stopFlag = false;
		this.optimization = false;
		//初始化指令队列
		this.commandQueue = new IndexCommandQueue(this.context);
		this.toBeDeleted = new LinkedList<IndexCommand>();
		this.toBeAdded = new LinkedList<IndexCommand>();
		//启动执行线程
		new Thread(this , this.name).start();
		System.out.println(this.name + " start." );
	}	

	/**
	 * 发送索引变更指令
	 * @param task
	 */
	void sendCommand(IndexCommand command , boolean immediately){
		synchronized(this.commandQueue){
			//超过容量上限
			while(this.commandQueue.size() >= this.context.getIndexConfig().getQueueHoldLimited()){
				//要求等待
				try {
					this.commandQueue.wait();
					if(this.stopFlag){
						//服务已停止
						return;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//初始化命令执行状态
			command.setStatus(Status.TODO);
			this.commandQueue.addCommand(command);
			/*
			 * 队列中的指令数量达到触发临界，
			 * 或者immediately = true
			 * 则唤醒消费者（处理）线程
			 */
			if(immediately ||
					this.commandQueue.size() >= this.context.getIndexConfig().getQueueTriggerCritical()){
				//立即唤醒消费者（处理）线程
				this.commandQueue.notifyAll();
			}
		}		
	}	
	
	/**
	 * 停止线程服务
	 */
	void stopService(){
		this.stopFlag = true;
		this.optimization = false;
		synchronized(this.commandQueue){
			this.commandQueue.clear();
			this.commandQueue.notifyAll();
		}
		this.toBeAdded.clear();
		this.toBeDeleted.clear();
	}		
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(!this.stopFlag){
			IndexCommand[] commands = null;
			//同步队列
			synchronized(this.commandQueue){
				while(!this.stopFlag && this.commandQueue.isEmpty()){
					//当队列为空，线程等待
					try {
						this.commandQueue.wait(this.context.getIndexConfig().getQueuePollPeriod());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(this.stopFlag){
					//终止当前线程的处理
					return;
				}
				//线程被唤醒，取出所有的任务
				commands = this.commandQueue.pollALL();
				//唤醒生产者线程
				this.commandQueue.notifyAll();
			}
			
			if(commands != null){
				try{
					//执行索引变更指令
					this.processIndexCommands(commands);
				}catch(Exception allEx){
					//捕获所以异常，保证线程的run可以继续循环执行
					allEx.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 执行索引变更指令
	 * @param  commands 索引变更指令数组
	 * @throws IOException 
	 * @throws IOException 
	 */
	private void processIndexCommands(IndexCommand[] commands){
		/*
		 * 分离指令，构造删除、新增两个指令队列	
		 * 对于HistoryIndexController, 要负责处理BLD , ADD , MOD , DEL , OPT
		 */	
		for(IndexCommand command : commands){
			switch(command.getOperate()){
			case BUILD :
				this.toBeAdded.add(command);
				break;
			case ADD :	
				this.toBeAdded.add(command);
				break;
			case MODIFY :
				this.toBeDeleted.add(command);
				this.toBeAdded.add(command);
				break;
			case DELETE :	
				this.toBeDeleted.add(command);
				break;
			case OPTIMIZE :	
				this.optimization = true;
				command.setStatus(Status.DONE);
				break;				
			}
		}

		//变更索引		
		Directory dir = null;
		try {
			//获取索引目录
			dir = FSDirectory.open(this.context.getIndexConfig().getBackupDirectory());;
			//判断索引是否已经存在
			boolean exists = IndexReader.indexExists(dir);
			//执行删除索引任务
			if(exists && !toBeDeleted.isEmpty()){
				this.removeIndex(toBeDeleted , dir);
			}
			//执行新增索引任务
			if(!toBeAdded.isEmpty()){
				this.addIndex(toBeAdded, dir, !exists);
			}
			//如果存在索引优化指令，且指令队列中没有其他指令，则执行优化
			if(exists && this.optimization && this.commandQueue.isEmpty()){
				this.optimizeIndex(dir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			//关闭当前索引目录
			if(dir != null){
				try {
					dir.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		
			//清空索引任务队列
			this.toBeDeleted.clear();
			this.toBeAdded.clear();
		}		
	}

	/**
	 * 执行删除索引操作
	 * @param commands
	 * @param dir
	 */
	private void removeIndex(List<IndexCommand> commands , Directory dir){
		//构造IndexReader
		IndexReader indexReader = null;
		try{
			indexReader = IndexReader.open(dir , false);
			//批量删除
			for(IndexCommand command : commands){
				Term keyTerm = this.context.keyTerm(command.getDocument());
				//查找当索引中PKey对应的文档
				TermDocs termDocs = indexReader.termDocs(keyTerm);
				/*
				 * PKey是唯一的，则该termDocs.next()只执行一次
				 */
				if(termDocs.next()){
					//删除PKey对应的文档
					indexReader.deleteDocument(termDocs.doc());
					//变更command操作状态
					switch(command.getOperate()){
					case MODIFY :
						command.setStatus(Status.DELETED);
						break;
					case DELETE :	
						command.setStatus(Status.DONE);
						break;
					}
				}
			}
			indexReader.flush();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			//关闭reader 提交删除的文档
			if(indexReader != null){
				try {
					indexReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	/**
	 * 新增索引
	 * @param commands
	 * @param dir
	 * @param create
	 */
	private void addIndex(List<IndexCommand> commands , Directory dir , boolean create){
		//构造IndexWriter
		IndexWriter indexWriter = null;
		try{
			indexWriter = this.openWriter(dir , create);
			//批量添加文档
			for(IndexCommand command : commands){
				Document doc = command.getDocument();
				switch(command.getOperate()){
				case BUILD :					
					indexWriter.addDocument(doc);
					//变更command操作状态
					command.setStatus(Status.DONE);
					break;
				case ADD :
					indexWriter.addDocument(doc);
					//变更command操作状态
					command.setStatus(Status.DONE);					
					break;
				case MODIFY :
					/*
					 * 如果指令时修改文档，在新增文档前，需要判断
					 * IndexCommand.OPSTATUS_DELETED == command.getOpStatus()
					 * 查看文档是否在实时索引中且已经被删除
					 * 如果IndexCommand.OPSTATUS_DELETED != command.getOpStatus()
					 * 说明文档不在索引中，则不能新增
					 */
					if(Status.DELETED == command.getStatus()){
						indexWriter.addDocument(doc);
						//变更command操作状态
						command.setStatus(Status.DONE);
					}
					break;
				}
			}
			//提交事务
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();	
			if(indexWriter != null){
				try {
					indexWriter.rollback();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}finally{
			if(indexWriter != null){
				try {
					indexWriter.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	/**
	 * 优化索引
	 * @param dir
	 */
	private void optimizeIndex(Directory dir){
		IndexWriter indexWriter = null;
		try{
			indexWriter = this.openWriter(dir , false);
			long begin = System.currentTimeMillis();
			System.out.println(this.name + " optimization beign at " + new Date(begin));
			//通知context，备份索引开始优化
			this.context.notifyBackupIndexOpt(true);
			indexWriter.optimize();
			System.out.println(this.name + " optimization end at " + new Date(begin) + " cost " + (System.currentTimeMillis() - begin) + " ms.");
		}catch (IOException e) {
			e.printStackTrace();	
		}finally{
			this.optimization = false;
			//通知context，备份索引优化结束
			this.context.notifyBackupIndexOpt(false);
			if(indexWriter != null){
				try {
					indexWriter.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}	
	
	/**
	 * 打开内存索引的写入器
	 * @param dir 索引目录
	 * @param create 是否重建索引
	 * @return IndexWriter
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	private IndexWriter openWriter(Directory dir , boolean create) throws CorruptIndexException, LockObtainFailedException, IOException{
		IndexWriter indexWriter = new IndexWriter(dir ,	context.getIndexConfig().getLuceneAnalyzer(), create , IndexWriter.MaxFieldLength.LIMITED);
		//是否将多个segment合并
		indexWriter.setUseCompoundFile(false);
		//设置文档中Field的最大可容纳Term的数目
		indexWriter.setMaxFieldLength(this.context.getIndexConfig().getMaxFieldLength());
		//设置索引时，内存的最大缓冲文档数目
		indexWriter.setMaxBufferedDocs(this.context.getIndexConfig().getBufferedDocs());
		//设置索引时，内存的最大缓冲
		indexWriter.setRAMBufferSizeMB(this.context.getIndexConfig().getRAMBufferSizeMB());					
		//设置合并参数,History库是Main的两倍
		indexWriter.setMergeFactor(this.context.getIndexConfig().getMergeFactor() * 2);
		//设置每个index segment的最大文档数目
		indexWriter.setMaxMergeDocs(this.context.getIndexConfig().getMaxMergeDocs());
		return indexWriter;
	}		

}
