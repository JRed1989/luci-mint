LuciMint特性

1.通过异步任务队列处理方式，LuciMint解决了，在高并发索引请求下的索引同步操作问题。

2.实现了Lucene索引信息的“实时写入，实时搜索”。

3.提供了HTTP + XML格式的web访问接口，实现了Lucene在分布式群集系统中的接入使用。

4.对于Java用户而言，LuciMint提供了简单的Annotation来辅助对Bean实现的索引创建。通过在Java Bean上标注Annotation，并传入相应的API，即可完成索引的创建。对于非Java用户而言，LuciMint也提供了介于HTTP和XML的web服务接口。

5.LuciMint为一般性用户提供了一个基于HTTP的搜索接口，用户通过“简易搜索表达式”，可以方便的进行搜索查询操作。有特殊搜索需求的用户，也可以尝试在LuciMint的基础上，构建专用的搜索接口，来实现高级搜索功能。