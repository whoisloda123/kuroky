package com.liucan.kuroky.other;

/**
 * @author liucan
 * @version 19-7-29
 */
public class XiangMu {
    /*
     *
     *
     * javawiress
     * 1.thrift
     * 3.common
     * 7.扭蛋活动
     * 8.kafka
     * 9.下单抽免单
     * 10.加购抽奖-大促活动
     *      介绍：用户将商品加入购物车，即可抽奖，然后优先发发加入购物车商品对应的有库存的专场券，其次发b类运营给的有库存的券
     *  a.kafka消费用户的加入购物车消息，保存到mysql
     *  b.redis保存有券库存的信息
     *  c.通过quartz任务调度系统执行定时任务
     *  d.通过thrift调用券服务的查询券信息和发券
     *  e.通过mysql-mybatis，jooq保存用户发券记录
     *  f.活动的一些信息，如开始结束时间，券配置保存在zk里面
     *  g.后台管理系统配置券信息freemarker
     *  h.设计到的难点和需要考虑到的地方：
     *          1.缓存和数据库一致性：保存用户一天玩的次数（redis和mysql）
     *          2.分布式事务：扣积分+发券
     *          3.高并发高可用
     *              a.缓存
     *              b.限流
     *              c.降级
     *              d.mq流量削峰
     *              e.分布式集群
     *              f.分库分表
     *              q.负债均衡
     *
     *  oversea-competitor
     *      介绍：抓取亚马逊网站上面的数据，和自营店铺的数据，入es库，然后同步hive
     *  1.分为core，crawler,web3个模块
     *  2.core:公共模块，包括mybatis-generator相关的文件,公用的类
     *  3.web,spring-boot，提供api接口和后台管理页面
     *  4.crawler抓取模块：包括
     *      定时任务模块
     *      kafka消费模块
     *      评论抓取模块
     *  5.设计到的难点和需要考虑到的地方
     *      a.抓太多，速度比较慢
     *          刚开始用的多线程，但是后面发现太多了，需要抓取了，每天有几万的量，而且自营店铺的要一天抓取完，后面采用kafka的方式来处理
     *      b.抓太多，会被视为机器人
     *          刚开始时采用暴力重试机制，后面采用当失败了再换另外的肉鸡
     *
     *  今目标
     *      a.目标学院：
     *      很多课程，后台有管理页面，然后显示，用户可以付款
     *      我负责里面有个今课堂的后台，包括后台管理系统，以及给前端的接口
     *      每个课堂：介绍，视频，是否收费等，评论等等
     *      b.im后端的
     *  1.说一下自己的优缺点？
     *      缺点：
     *          a.有时候碰到再解决一个问题的时候，在一个很小的细节上面路上会想，睡觉的时候会想--想的时候会被干扰效率也低，而且用处不大
     *          b.有时候把任务布置给别人，觉着不放心或者达不到自己的要求，会去亲力亲为
     *      优点：
     *          a.适应能力强、上手快,
     *          b.坚持学习，乐于分享，代码洁癖
     *  2.说一下碰到最有难度的问题，如何解决？
     *      a.大促高并发的时候，发现有些用户反应请求比较慢
     *      b.通过
     *      c.随着抓取的量越来越多，抓太多，会被视为机器人概率越来越高，一直没有找到办法，各种方法都试过了，包括请求
     *  3.个人平时怎么提升技术？
     *      回家有时间看下技术方面的，在项目上面运用
     *  4.在工作中和生活中遇见最大的挑战是什么？
     *      a.一天事情比较多，但是又必须自己解决，然后有的事情必须在某个时间点完成，
     *      b.通过分先后级别，然后不重要的先不做
     *  5.什么机缘下转的java-------之前是搞c++的，现在web服务
     *      a.之前搞c++的，然后做个c++服务器编程，公司之前搞从c++的不多，刚好老大问我转不转，然后就转了
     *  6.说有offer，然后带个人
     *  7.说挑战性的项目和精通哪些-很重要
        9.看hashmap源码，各种锁的核心源码流程，提升深度，看mybtis核心源码
         10.看所有的面试题，并深入知道里面的流程
         * 3.如果问比较德行的一些中间件等？怎么回答？
         * 可以说一下发券系统怎么做的
     *  笔试题目->计算机网络，排序，算法的等->项目经验->简历
     *  写今目标的项目-写一个现在做的项目
     *  https://www.cnblogs.com/szlbm/p/5437498.html
     *  http://youzhixueyuan.com/各种干货
     */
}
