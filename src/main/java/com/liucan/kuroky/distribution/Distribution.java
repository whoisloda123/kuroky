package com.liucan.kuroky.distribution;

/**
 * @author liucan
 * @date 5/22/21
 */
public interface Distribution {
    /* *
     *
     * 1.分布式锁
     *  a.mysql
     *      使用mysql乐观和悲观锁
     *          优点：简单
     *          缺点：性能不行
     *  b.redis：
     *      1.加锁
     *          正确：set（key, value, nx, px, time）
     *          错误：setnx和setex执行
     *          原因：没有保证2条指令的原子性，setnx后redis挂了，造成锁永远也解锁不了
     *      2.解锁
     *          正确：执行lua脚本语句，if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end
     *          错误：判断key的value和期望的比较相等后，然后在删除key
     *          原因：没有保证2条指令的原子性，在判断value相等后，刚好key过期然后其他地方获取到锁设置了新的value，然后导致删除了其他的锁
     *      3.优点：
     *          a.现成实现框架redisson（里面有watch dog监控key是否过期，如果过期则继续延长，防止执行业务时间过长，而key过期了）
     *          b.性能好
     *        缺点：
     *          a.会一直自旋，耗cpu性能
     *          b.主从模式下，如果获取到锁后主挂了，从变成主了，其他获取到锁了，而之前的主被拉起来后也有锁，
     *              相对于有2个锁了，而zk分布式锁没有这个问题（强一致性），但是性能没有redis高
     *              解决这个问题，官网提供了redlock（有多个lock服务，当一半数以上lock成功，认为成功）,但是性能不好
     *              （redlock）https://cloud.tencent.com/developer/article/1431873
     *  c.zk:
     *      2.创建wirte_lock持久节点，顺序临时子节点
     *      3.lock时候getchild（不需要监控，会有羊群效应）判断 临时最小节点是否是最小的
     *      4.如果是则，获得锁，否则监听比自己小一个节点的exist，事件发生获得锁
     *      5.unlock直接删除节点
     *          优点：
     *              不会出现redis锁，主从都获取到锁的情况（强一致性）
     *              有Apache Curator封装Zookeeper操作的库
     *          缺点：
     *              性能没有redis lock好
     *
     * 45.分布式session实现的几种方式
     *  https://blog.csdn.net/woaigaolaoshi/article/details/50902010
     *      1.粘性session，用户每次请求都只落到某个服务节点上，可通过nginx配置
     *      2.session复制，当session发生变化时广播到其他节点上面，可通过tomcat配置
     *      3.缓存方案，redis，memcached
     *      4.数据库方案
     *
     *  46.forward和redirect的区别
     *      1.forward转发：服务器行为，转发的路径必须是同一个web容器下的url，其不能转向到其他的web路径上去，中间传递的是自己的容器内的request
     *          相对于转发到同一个web容器下的servlet程序处理，request是共享的
     *      2.redirect重定向：客户端行为，服务器返回重定向url，客户端从新请求
     *
     *  47.一致性hash算法（分布式缓存负载均衡算法）
     *      https://www.cnblogs.com/moonandstar08/p/5405991.html
     *      1.负载均衡算法
     *      2.常用的算法：取模算法，HashCode对服务节点取模，这种方式简单高效，
     *          但有个致命问题：当服务节点增加和删除，取模需重新计算，会造成命中率大幅度下降
     *      3.一致性哈希算法：
     *          a.一致性哈希环（0 - (2^32)-1），将服务器节点（可通过ip的hash等）放到环上面
     *          b.数据key的hash也能确定在环上面的位置
     *          c.key的hash值位置顺时针找到第一个服务器节点，即为命中节点
     *          d.增加和删除节点，只会影响节点逆时针到下一个节点之间的数据，影响没有取模大
     *          e.服务节点太少，节点分布不均匀会导致数据倾斜，可为每个节点添加很多虚拟节点来解决
     *
     *  51.负载均衡算法
     *  https://www.cnblogs.com/saixing/p/6730201.html
     *      1.应用服务器:只需要转发请求即可
     *          a.Random 随机:
     *              缺点:随机数的特点是在数据量大到一定量时才能保证均衡，所以如果请求量有限的话，可能会达不到均衡负载的要求
     *          b.轮询和加权轮询:
     *          c.最少连接:记录每个应用服务器正在处理的连接数，然后将新来的请求转发到最少的那台上
     *          d.hash地址:根据ip地址hash之后所有请求都是同一个服务器
     *      2.分布式缓存集群:如redis,memcached
     *          a.取模,HashCode对服务节点取模
     *          b.hash算法,redis是ip对的hash值对16384个哈希槽取模
     *          c.一致性hash算法,memcached client使用
     *
     *  58.分布式事务
     *  https://www.cnblogs.com/savorboard/p/distributed-system-transaction-consistency.html
     *  https://blog.csdn.net/hanruikai/article/details/82659223
     *      a.目的：因为出现数据库分区分表，服务器soa话，但是本质上分布式事务是为了保证不同数据库的数据一致性
     *      b.分布式理论
     *          1.cap理论:web服务器无法同时满足3个属性
     *              数据的一致性
     *              系统的可用性
     *              分区容错性:单个组件无法可用,操作依然可以完成
     *          2.base理论
     *              理论的核心思想就是：我们无法做到强一致，但每个应用都可以根据自身的业务特点，采用适当的方式来使系统达到最终一致性
     *      c.jta:java-transaction-api是对2阶段提交的实现,有spring实现了jta
     *      d.实现方式:
     *          a.强一致性
     *          b.最终一致性
     *      e.方案：
     *          1.2pc:2阶段提交采用XA协议
     *              流程：
     *                  a.事务管理器对每个事务参与的数据库,询问是否可以提交
     *                  b.事务管理器对每个事务参与的数据库,发出提交命令
     *                  c.如果其中某一个参与者在是否可以提交和提交失败都会失败回滚
     *                  d.属于强一致性
     *              缺点：
     *                  一部分事务参与者收到了提交消息，另一部分事务参与者没收到提交消息，那么就会导致节点间数据的不一致问题
     *                  事务参与值执行了commit后挂掉了，而管理者也挂了，新的管理者不知道挂掉的参与者的信息
     *          2.3pc:对2pc的优化
     *              流程：
     *                  a.相对于2pc来说，多了perCommit和超时机制（参与者和管理者都有）
     *          3.tcc(Try-Confirm-Cancel):补偿事务 和2pc流程差不多
     *              a.针对每个操作都要注册一个与其对应的确认和补偿（撤销操作）
     *          4.mq方式：基于本地事务+mq
     *              其实也是基于2阶段提交，预提交，提交过程，只是交给mq处理了
     *              A事务先二阶段执行，mq会有个回调，如果失败需要回滚就执行回调
     *              A事务执行成功，会修改mq里面的一个状态，然后给B事务发送成功消息，B事务开始执行
     *              属于最终一致性
     *          5.本地消息表
     *          https://www.cnblogs.com/savorboard/p/distributed-system-transaction-consistency.html
     *          有点模糊，后续再看下
     *
     *  59.单点登录（single-single-on SSO）
     *  https://www.cnblogs.com/morethink/p/8047711.html
     *  https://www.cnblogs.com/zhuchenglin/p/8968530.html
     *      概念：用户用户名和密码登录了一个a系统后，登录b系统不需要重新输入了
     *      实现方式：
     *          1.cookie方式
     *              a.用户登录a系统，跳转至sso认证中心拿到返回的cookie，去登录b系统
     *              b.缺点：
     *                  不支持跨域请求（一个域名下的cookie不能拿去请求另一个域名）
     *                  不安全，如果cookie可能会被破解
     *          2.服务节点内部认证的方式
     *              a.用户登录a系统，跳转至sso认证中心登录成功
     *              b.sso认证中心创建全局会话，保存的令牌信息,用户信息，用户名，密码等等，将局部令牌发送给a系统
     *              c.认证中心带着令牌跳转到用户最初请求的地址，a系统拿到令牌信息，到sso认证中心验证是否登录了
     *              d.如果登录了，则保存局部会话
     *              e.用户登录b系统，跳转至sso认证中心发现用户已经登录，则执行c步骤
     *              f.局部会话建立后不会在通过sso认证中心，
     *  61.常用的几种分布式id的设计方案
     *  参考：https://www.jianshu.com/p/b2337d954ff0
     *      a.UUID（里面有通过网卡）
     *          优点：简单，高效
     *          缺点：不支持递增，数据比较长
     *      b.数据库自增id
     *          优点：写入效率高
     *          缺点：分布式架构，多个Mysql实例可能会导致ID重复,容易被识破
     *      c.redis
     *          优点：不依赖于数据库，灵活方便，且性能优于数据库
     *          缺点：AOF和RDB依然会存在数据丢失，造成ID重复。
     *      d.zookeeper
     *      e.雪花算法-SnowFlake:机制和UUID差不多，是生成一个long的整数
     *          1.第一位：最高位是符号位0
     *          2.第二部分：41bit时间戳，精确到毫秒级，41位的长度可以使用69年-（一般是当前时间-开始时间）
     *          3.第三部分：10位的机器标识（5位数据标识位，5位机器标识位）
     *          4.第四部分：12位的计数序列号-自增id，最多支持同一毫秒内4096个
     *          优点：高效，有时间，且递增
     *          缺点：依赖与时钟，不能回拨
     *
     *  62.常见限流策略
     *  https://blog.csdn.net/fouy_yun/article/details/81025984
     *      a.令牌桶：以一定速率往令牌桶里面放入令牌，来一个请求拿到令牌，然后处理，拿不到令牌则丢弃
     *      b.漏桶：桶里面以一定速率滴水，入桶的水可以速度很快也很小，反正多了就溢出不管
     *      c.计数器：计算请求的数量，然后限制
     *
     *  67.什么时候索引失效？
     *  https://www.e-learn.cn/content/mysql/1045218
     *      a.like查询%在前面
     *      b.or操作但没有把所有字段加上索引,!=, is null, not in等操作
     *      c.类型是字符串,但是没有用引号包含起来
     *      d.mysql估计全部扫描的时间,比使用索引的时间快时(数据量很大,查询出20%-30%数据,因索引会查找2次才能查到数据)
     *
     *  68.如何防止sql注入？
     *  https://www.cnblogs.com/jiangxueqiao/p/7444127.html
     *      a.转义字符特殊处理，在mysql里面具有特殊含义的字符转义一下
     *      b.Prepared Statement预处理语句(如mybatis#{} 预编译阶段用?代替,然后正在执行的时候在替换)
     *  69.高可用高并发一般方案是什么？7种
     *  https://blog.csdn.net/qq_37651267/article/details/93368908
     *      a.在开发高并发系统时有三把利器用来保护系统：缓存、降级和限流
     *      b.缓存：如redis等提高容量
     *        降级：当用户量大了，选择暂时屏蔽掉某些功能
     *        限流：限制访问的请求数，一般的方案如：令牌桶，漏桶，计数器等
     *        集群：
     *        负载均衡：
     *        mq流量消峰：
     *        数据库分库分表：
     */
}
