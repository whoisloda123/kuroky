package com.liucan.kuroky.base;

/**
 * @author liucan
 * @date 5/22/21
 */
public interface Base {
    /* *
     *
     *
     *
     *   30.为何byte取值范围是[-128~127],而不是[-127~127]
     *      参考：https://blog.csdn.net/qq_23418393/article/details/57421688
     *      1.计算机里面是用补码进行数字运算
     *      2.原码第一位表示符号, 其余位表示值，人的正常思维 8（0000 1000），-8（1000 1000）
     *      3.反码，正数是本身，负数是符号位不变其余位取反 8（0000 1000）， -8（1111 0111）
     *      4.补码，正数是本身，负数反码+1 8（0000 1000） -8（1111 1000）
     *      5.用原码来进行加减不能得到正确结果，用反码来进行加减可以得到正确结果，范围是[-127~127],但是会出现+0（0000 0000），-0（1000 0000）都表示0
     *      6.而用补码来进行加减，可以得到正确结果，而且补码计算出来的-128（1000 0000）就是用反码表示的-0，计算出来的+0（0000 0000）可以表示0
     *        所以用补码来进行加减运算既能够解决+0和-0问题，而且能够多表示一位数字-128，所以说为什么byte的取值范围是[-128~127]
     *   27.String直接赋值和new区别
     *      参考：https://blog.csdn.net/zqzq310918/article/details/54313262
     *      a.直接赋值是如果不存在常量池（constant pool），就在常量池创建，以后同样的字符串赋值，则直接指向常量池
     *      b.new是每次都在堆中开辟空间
     *      c.常量池:存放字符串常量和基本类型常量（public static final）
     *   26.Reference（强引用，软引用，弱引用，虚引用,引用队列）
     *      参考：https://www.cnblogs.com/huajiezh/p/5835618.html
     *          https://www.cnblogs.com/dreamroute/p/5029899.html
     *          https://blog.csdn.net/woblog/article/details/51332342
     *          http://bylijinnan.iteye.com/blog/2085082
     *          https://blog.csdn.net/qq_33663983/article/details/78349641
     *      a.StrongReference强引用，经常用到，只要强引用还在就GC不会回收，可用赋值null方式手动回收
     *      b.SoftReference软引用,有用但是不是非必须的对象，只有在内存不足的时候才会回收该对象，可以解决OOM内存溢出情况
     *        可用来实现内存敏感的高速缓存,比如网页缓存、图片缓存等。使用软引用能防止内存泄露
     *      c.WeakReference弱引用,弱引用的生命周期较软引用更加短暂,GC进行回收的时候，不管当前内存空间是否足够，都会回收
     *          a.在“引用计数法”的垃圾回收机制中，能避免“循环引用”，因为 Weak references are not counted in reference counting
     *          b."key-value"形式的数据结构中，key 可以是弱引用。例如 WeakHashMap
     *          c.观察者模式（特别是事件处理）中，观察者或者 listener 如果采用弱引用，则不需要显式的移除
     *          d.缓存
     *      d.PhantomReference虚引用，该应用并不能获取到任何对象，也不会影响对象生命周期，主要是和引用队列一起使用，监控对象被回收的时候，做一些额外处理
     *          a.通过虚引用可以知道一个对象何时被清除出内存。事实上，这也是唯一的途径
     *          b.防止对象在 finalize 方法中被重新“救活”（可参考《深入理解 Java 虚拟机》一书）
     *      e.ReferenceQueue引用队列，当引用对象所引用的值被回收了，该引用对象会被放到引用队列里面，不过需要我们手动处理来回收该引用对象，如WeakHashMap
     *        引用队列一般和软引用，弱引用，虚引用一起用
     *
     * 4.String
     *   String类是不可改变的，创建了String对象，值就无法改变了。如需要对字符串做很多修改，使用StringBuffer & StringBuilder 类,并且不产生多个对象
     *   StringBuilder是java5提出来的是线程不安全的，而StringBuffer是线程安全的
     *
     *  17.fail-fast,fail-safe迭代器
     *      参考：https://blog.csdn.net/m0_37907797/article/details/80499422
     *     a.fail-fast:快速失败，在迭代过程中，如果数据结构被修改（增加、删除）了则抛出异常，实现机制是modCount和expectedCount比较不相等则抛异常
     *       而在插入和删除的时候会更新modCount
     *
     *       注意：快速失败的行为并不能得到正确的保证，一般来说，存在非同步的并发修改时，不可能做出任何坚决的保证的，fail-fast只是尽
     *       最大努力来抛出异常，所以fail-fast仅用来检查程序的bug
     *
     *     b.fail-safe：安全失败，在迭代的时候，先拷贝一份，然后在迭代，不会抛出异常，但是内容可能不是最新的
     *     c.java.util里面的容器都是快速失败，不支持异步，而java.util.concurrent包里面的容器都是安全失败，支持异步
     * 7.finalize函数是在对象析构之前调用
     *   a.一般在里面做一些释放操作，并非是java,new出来的内存，比如说文件句柄关闭，释放调用c，c++的来分配空间等
     *   b.调用该函数会引起死锁和线程挂起，因为并不存在任何一种机制可以把资源的释放与对象的生命周期完全绑定在一起，如果处理不好还会耗尽资源。
     *   c.java9已经抛弃了,不建议使用
     *
     *  60.流量控制与拥塞控制
     *  https://blog.csdn.net/ailunlee/article/details/53716367
     *      流量控制：防止a发送太快，导致b接收不过来，通过滑动窗口实现，b每次会告诉a最多能发送多少
     *      拥塞控制：
     *          a.是A与B之间的网络发生堵塞导致传输过慢或者丢包，来不及传输。防止过多的数据注入到网络中
     *              是一个全局性的过程，涉及到所有的主机、路由器，以及与降低网络性能有关的所有因素
     *          b.实现方式
     *              慢开始和拥塞避免
     *              慢慢的增加发送数据的大小
     *
     *
     *
     *  65.https工作流程,ssl协议，对称加密，非对称加密
     *  https://www.cnblogs.com/hai-blog/p/8311671.html
     *      a.对称加密：A（客户端）和B（服务端）都用的是同一个私钥来加密和解密
     *      b.非对称加密：
     *          1.A请求
     *          2.B返回证书和公钥
     *          3.A随机产生一个对称密钥，用公钥对这个对称密钥加密发给B
     *          4.B收到后用私钥解密拿到对称密钥
     *          5.至此A和B都拿到了同一个对称密钥了，相互就可以加密和解密了
     *
     *  66.tcp粘包/拆包，及解决办法
     *  https://www.cnblogs.com/panchanggui/p/9518735.html
     *
     *
     *  74.默认排序算法
     *  https://blog.csdn.net/sinat_35678407/article/details/82974174
     *  timsort算法：基于大部分数据已经是排好序的，采用归并+二分插入算法
     *  如何归并：里面已经排好的数据块不动，没有拍好的进行排序（二分插入），数据块不满规定长度，则用二分插入添加到最小的长度
     *      最后所有数据块进行归并排序
     *
     *  78.Java栈什么时候会发生内存溢出
     *      a.递归调用方法
     *      b.死循环调用，里面有局部变量
     *
     *  79.如何判断链表有环
     *  https://blog.csdn.net/N1314N/article/details/90736225
     *      a.用HashSet
     *      b.用快慢指针，慢指针走一步，快指针走两步，如果有环则2个肯定会相遇（因为快指针会在环里面一直循环，直到相遇），否则快指针会为null
     *
     *  80.HTTP2.0和HTTP1.x的区别
     *  https://www.jianshu.com/p/7fc8ca235c9f
     *
     *  81.架构设计4+1视图模型
     *  https://blog.csdn.net/zxs9999/article/details/38703471
     *  https://www.ibm.com/developerworks/cn/rational/06/r-wenyu/index.html
     *      a.1视图是，场景视图（用例视图）,测试角度，关注的是从用户角度，可以通过测试用例表示
     *      b.逻辑视图，关注的是系统的功能逻辑，产品角度，可以通过uml类图表示
     *      c.开发视图，关注的是软件的静态组织，开发人员角度，包括软件可用性，稳定性等等，可以uml组件图等
     *      d.过程视图（处理视图），关注的是软件动态运行时，同步，并发，分布式等，，可以uml活动图等
     *      e.物理视图，关注的是如何部署机器和网络来配合软件达到可用性，运维人员角度，
     *
     *  82.为什么重写equals必须重写hashcode
     *  https://www.cnblogs.com/wang-meng/p/7501378.html
     *      a.通用约定，概论上来说，既然对象都相等了，hashcode肯定也要相等才符合逻辑
     *      b.hashMap里面就是基于该思想来的，如果重新了equals，但没有重写hashcode会出现问题
     *      c.效率问题，先用hashcode（做数字运算）做出一部分筛选，然后在比较equals
     *
     *  86.http缓存
     *  https://www.jianshu.com/p/227cee9c8d15
     *  https://www.cnblogs.com/ranyonsue/p/8918908.html
     *  浏览器缓存分为强缓存和协商缓存
     *   a.浏览器先根据这个资源的http头信息来判断是否命中强缓存。如果命中则直接加在缓存中的资源，并不会将请求发送到服务器。
     *   b.如果未命中强缓存，则浏览器会将资源加载请求发送到服务器。服务器来判断浏览器本地缓存是否失效。若可以使用(过期时间已经到了，但是服务资源还未更新)
     *   则服务器并不会返回资源信息，浏览器继续从缓存加载资源。
     *   c.如果未命中协商缓存，则服务器会将完整的资源返回给浏览器，浏览器加载新资源，并更新缓存。
     *   d.强缓存cache-control,expires,协商缓存ETag/If-Not-Match 、Last-Modified/If-Modified-Since
     */
}
