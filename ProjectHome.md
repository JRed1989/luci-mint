LuciMint(Lucene Index薄荷糖)是一个基于Lucene的全文索引小组件，它封装了Lucene的底层对Document对象的操作，提供了一个面向用户数据格式的，轻量级的索引操作接口。

> 采用异步请求方式，LuciMint实现了 “高并发的实时索引”，“实时搜索”，“多套索引配置”等核心功能。专门的，针对Java语言客户端，LuciMint设计了Annotation标签，通过Java Bean属性的简单的标注，用户可以直接将Bean生成Lucene索引.
同时，LuciMint结合了IKAnalyzer3.2.8版本的了“简易的搜索表达式”功能，实现了统一的HTTP搜索服务接口。

> LuciMint适用于基于Lucene的，一般性的，大中型企业知识库、文档中心，或者中小型的互联网论坛，地图POI信息搜索等项目。