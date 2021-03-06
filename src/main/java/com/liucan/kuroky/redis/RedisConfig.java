package com.liucan.kuroky.redis;

/**
 *
 * 一.redis集群哨兵
 *  参考：https://zhuanlan.zhihu.com/p/62947738
 *  1.哨兵
 *      a.集群监控：负责监控Redis master(读写)和slave(读)进程是否正常工作
 *      b.消息通知：如果某个Redis实例有故障，那么哨兵负责发送消息作为报警通知给管理员
 *      c.故障转移：如果master node挂掉了，会自动转移到slave node上
 *          1. 过滤掉主观下线的节点
 *          2. 选择slave-priority/ replica-priority最高的节点，（replica-priority 0的不选择）如果由则返回没有就继续选择
 *          3. 选择出复制偏移量最大的系节点，因为复制偏移量越大则数据复制的越完整，如果由就返回了，没有就继续
 *          4. 选择run_id最小的节点
 *      e.哨兵也是个集群，每隔1秒每个哨兵会向整个集群：Master主服务器+Slave从服务器+其他Sentinel（哨兵）进程，发送一次ping命令做一次心跳检测来获取节点信息
 *      f.半数以上的哨兵主观认为下线了，那就客观认为该节点下线了，然后哨兵内部会通过选举（raft）出一个哨兵来进行将slave转换为master
 *      g.哨兵leader选举：
 *          每个在线的哨兵节点都可以成为领导者，每个哨兵向其他哨兵发送命令让自己成为leader，其他可以同意或拒绝，如果半数以上的同意了，则可以成为领导
 *  2.集群
 *      a.即使使用哨兵，redis每个实例也是全量存储，每个redis存储的内容都是完整的数据，浪费内存
 *      b.解决单机Redis容量有限的问题，将数据按一定的规则分配到多台机器
 *      c.每个key会对应一个slot，总共16384个slot均分到不同的节点上面(每个节点也有主从),至少需要3主3从
 *      e.副本漂移:当一个master节点没有从，而其他master节点有2个以上的从时，该节点下面的一个从节点会漂移为之前master的slave节点
 *      d.节点失效判断:集群中所有master参与投票,如果半数以上master节点与其中一个master节点通信超过(cluster-node-timeout),认为该master节点挂掉
 *      e.集群失效判断
 *          如果集群任意master挂掉,且当前master没有slave，则集群进入fail状态。也可以理解成集群的[0-16383]slot映射不完全时进入fail状态。
 *          如果集群超过半数以上master挂掉，无论是否有slave，集群进入fail状态
 *  3.投票选举
 *      a.所有master参与，每个master都和其他master连接上了的，如果半数以上的master认为某个节点挂掉(故至少需要3主3从)，就真的挂掉了
 *      b.根据各个slave最后一次同步master信息的时间，越新表示slave的数据越新，竞选的优先级越高，就更有可能选中.
 *
 * 二.主从复制
 *  http://blog.itpub.net/31545684/viewspace-2213629/
 *      a.全量复制：从数据库启动时，向主数据库发送sync命令，主数据库接收到sync后开始在快照rdb，在保存快照期间受到的命名缓存起来，
 *          快照完成时，主数据库会将快照和缓存的命令一块发送给从
 *      b.增量复制：主每收到1个写命令就同步发送给从
 *
 * 三.补充数据
 *      a.bitmap(底层类型string):setbit,bitcount,(bitop and/or newkey key1 key2)
 *          1.可用来实现布隆过滤器，统计每天用户签到信息（用户id为key），连续几天访问的用户（bitop and）
 *      b.hyperloglog(基于bitmap):pfadd pfcount pfmerge，是基数统计（统计不重复个数），和set差不多
 *          https://www.jianshu.com/p/55defda6dcd2，实现和概率学有关系
 *          1.loglog是基于基数估计的算法，伯努利抛硬币算法，空间复杂度log2log2(N)，hyperloglog差不多，只需要12k就可以统计2^64个数
 *          2.有一定误差0.81%，适用于巨量统计 不能保存原始数据
 *          3.用途：记录网站IP注册数，每日访问的IP数，页面实时UV、在线用户人数
 *      c.Geospatial（底层类型zset）
 *          1.保存地理位置，可以用于找到最近的位置
 *          2.GEOADD key 经度 维度 名称,
 *
 * 二.单线程为何还快：
 *  https://www.cnblogs.com/javastack/p/12848446.html
 *  1.基于内存操作,内存操作非常快，所以说没有必要多线程
 *  2.不存在多进程或者多线程导致的切换而消耗CPU，而多线程会处理锁
 *  3.使用epoll多路 I/O 复用模型来处理多并发客户端
 *  4.执行命令的核心模块是单线程的，如果删除大数据的时候会比较慢
 *  4.6.0加入了多线程，如异步删除大数据key，网络数据的读写和协议解析等
 *
 * 三.持久化
 *  1.RDB快照
 *      a.默认开启，fork子进程，按照配置时间（save命令）将内存中的所有数据快照到dump.rdb，快照完后替换久的rdb文件。
 *  2.AOF
 *      a.每隔一段时间（默认每一秒）或者有写操作时将执行写命令append写入文件，启动时会根据日志从头到尾全部执行一遍以完成数据的恢复工作。包括flushDB也会执行
 *      b.随着redis一直运行，aof文件会变的很大，可通过BGREWRITEAOF命令或者设置参数redis会启动aof文件缩减命令
 *          1.里面的有些多条指令可以合并
 *          2.无效的命令可以删除掉，如del了key的命令可以删除掉
 *  3.混合方式，4.0之后支持的且默认开启，2种方式都有，因为rdb可能会造成数据丢失，而aof性能慢
 *  4.当两种方式同时开启时，一般情况下只要使用默认开启的RDB即可，RDB便于进行数据库备份，并且恢复数据集的速度也要快很多
 *
 * 五.缓存问题
 *  1.缓存雪崩：缓存同一时间大面积的失效，这个时候又来了一波请求，结果请求都怼到数据库上，从而导致数据库连接异常
 *      解决方法：给缓存的失效时间，加上一个随机值，避免集体失效。
 *  3.缓存击穿：一个key失效，刚好很多请求来了
 *      解决方法：一般可以用，只对几个请求区处理数据库，然后其他的等待，或者直接返回空,可以用加锁的方式实现，如setnx
 *  2.缓存穿透：黑客故意去请求缓存中不存在的数据，导致所有的请求都怼到数据库上，从而数据库连接异常
 *      解决方法：
 *          a.为空也保存到内存里面,但是需要设置比较短的过期时间，因为也不确定是攻击者发起的
 *          b.采用bloom filter来判断是否不存在，如果不存在直接返回
 *
 * 六.事务
 *   1.集群不支持事务
 *      a.事务里面操作可以在不同节点,对于多个key的操作,保证不了不会被其他命令插进来,无法保证其原子性,但集群里边的每个节点支持事务，
 *      b.事务不支持,因为redis不是关系型数据库，是key-value不关心业务，没有必要
 *   2.集群不支持pipeline管道
 *      a.批量操作位于不同的slot的时候会失败,因为slot在不同的node,但也支持单个节点
 *      b.单个节点操作：通过key计算出slot，然后通过slot找到对应的节点，然后在节点上面操作
 *   3.集群不支持对所有键进行scan操作
 *   4.事务是支持原子的，通过watch命令，当在执行事务的时候，被其他修改，就执行失败
 *   5.事务不支持回滚操作，事务过程中有一条执行失败了，剩下的会继续执行
 *   6.过程是：先watch，然后multi,执行命令，然后exec
 *
 * 七.多个系统并发操作一个key
 *      a.系统A（下单），B（支付），C（退款）按照顺序操作一个key，如果出现A网络抖动，B，C在A前面执行则会出现问题
 *      b.用zk分布式锁（因为是强一致性，不会出现redis分布式锁的问题），来保证同一时刻只有一个操作，如果A系统拿到了，后面B，C只能等待
 *      c.如果要写入数据库的时候，判断一下要写入的时间是否比数据库里面的时间新，如果是则可以，否者不行
 *
 * 八.分布式锁
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
 * 七.内存回收策略
 *   1.当使用内存达到一定大小的时候,会实行回收策略，默认关闭，max memory参数被注释了
 *   2.策略方式:
 *      a.voltile-lru：从已设置过期时间的数据集中挑选最近最少使用的数据淘汰
 *      b.volatile-ttl：从已设置过期时间的数据集中挑选将要过期的数据淘汰
 *      c.volatile-random：从已设置过期时间的数据集中任意选择数据淘汰
 *      d.allkeys-lru：从数据集中挑选最近最少使用的数据淘汰
 *      e.allkeys-random：从数据集中任意选择数据淘汰
 *      f.no-enviction（驱逐）：禁止驱逐数据(默认)
 *      4.0之后新增了lfu算法回收
 *
 * 八.发布订阅
 *   1.消息订阅者，即subscribe客户端，需要独占链接，即进行subscribe期间，redis-client无法穿插其他操作，
 *      此时client以阻塞的方式等待“publish端”的消息；甚至需要在额外的线程中使用
 *   2.消息发布者，即publish客户端，无需独占链接
 *   3.Pub/Sub功能缺点是消息不是持久化的，发送就没有了
 *
 * 九.redis 延迟队列
 *   1.就是将消息放入zset里面，score为过期时间，然后有专门的线程去取最近的时间，拿出来消费，然后在删除掉
 *   2.应用场景：下单成功，30分钟之后不支付自动取消，等
 *
 * 十.Redis的rehash
 *  https://blog.csdn.net/qq_38262266/article/details/107727116
 *      1.hash表结构，不仅是对外的hash类型的结构，而且redis数据库使用的使用的数据结构
 *      2.redis是单线程，K很多时次性将键值对全部rehash，庞大的计算量会影响服务器性能，
 *      3.渐进式的rehash:
 *          a.里面dict有2个hash表和rehashIndex,正常情况rehashIndex默认-1，hash[1]为null，数据保存在hash[0]
 *          b.在需要扩容的时候，每执行增删查改的时候，都会将索引为rehashIndex的桶的数据从hash0[rehashIndex]] -> hash1[rehashIndex],完成后rehashIndex + 1
 *
 * 11.redis底层数据结构和常用类型用的数据结构
 *    https://cloud.tencent.com/developer/article/1690533
 *    a.5种数据集类型都是通过RedisObject结构体存储的，里面有类型，编码类型（数据结构），lru（最近使用时间，用来内存满清除过期数据的），引用计数，数据
 *    b.每个类型至少有2种编码类型
 *      string:int（当类型为int时）,embstr,raw,后面2个用的sds简单动态字符串
 *      list:ziplist（数据量少的时候），双端链表
 *      map：ziplist（数据量少的时候），hashtable
 *          a.hash表里面的dic结构，有2个hash表，hash[0](正常用)，hash[1]和rehashIndex都是扩容，rehash的时候用
 *          b.hash表结构，不仅是对外的hash类型的结构，而且redis数据库使用的使用的数据结构
 *      set：intset（数据量少的时候）,hashtable,value为null
 *      zset：ziplist（数据量少的时候），skiplist（跳跃表，积分排序）和map（通过value获取对应的sorce）
 *
 *  12.redis集群扩容/收缩
 *      https://www.jianshu.com/p/47410585b0b2
 *      a.在扩容/收缩过程中整个集群都是可用状态的，可读可写
 *      b.先加入集群，迁移slot（16384个slot）和数据，主要就是迁移slot里面的数据
 *      c.先确定slot迁移计划，确定哪些节点的哪些slot需迁移到新的节点，且要保证每个节点的slot数量分布均匀
 *      d.迁移数据，逐个节点逐个slot进行的
 *          1.对目标节点发送cluster setslot {slot} importing {sourceNodeId}命令，让目标节点准备导入槽数据
 *          2.对源节点发送cluster setslot {slot} migrating {targetNodeId}命令，让源节点准备迁出槽数据
 *          3.把获取的键通过流水线(pipeline)机制批量迁移到目标节点，复制源节点slot key数据，迁移到目标接口slot里面，复制完成后，再删除源节点key
 *          4.完成后，向集群内所有master发送cluster setslot {slot_id} node {targetNodeId}命令，通知他们哪些槽被迁移到了哪些master上，让它们更新自己的信息
 *      e.收缩过程和扩容差不多
 */
public class RedisConfig {


}
