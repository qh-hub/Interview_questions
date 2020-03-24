# Interview_questions
面试知识点总结
## 目录
- [MySQL](#mysql)
    - [引擎](#引擎)
        - [两者比较](#两者比较)
    - [MVCC](#MVCC)
    - [《mysql高性能》上有这样一句话](#《mysql高性能》上有这样一句话)
    - [索引](#索引)
    - [缓存](#缓存)
    - [事务](#事务)
        - [并发事务带来哪些问题？](#并发事务带来哪些问题？)
        - [事务的隔离级别有哪些?MySQL的默认的隔离级别？](#事务的隔离级别有哪些?MySQL的默认的隔离级别？)
    - [表级锁和行级锁的对比](#表级锁和行级锁的对比)
    - [大表优化](#大表优化)
    - [数据库优化](#数据库优化)
        - [数据库命名规范](#数据库命名规范)
        - [数据库基本设计规范](#数据库基本设计规范)
        - [数据库字段设计规范](#数据库字段设计规范)
        - [索引设计规范](#索引设计规范)
        - [sql开发规范](#Mysql开发规范)
- [线程相关](#线程相关)
    - [使用线程池的好处](#使用线程池的好处)
    - [阿里编程规范](#阿里编程规范)
    - [Executors返回的线程池对象弊端如下](#Executors返回的线程池对象弊端如下)
    - [ThreadPoolExecutor类简单介绍](#ThreadPoolExecutor类简单介绍)        
- [抽象类和接口](#抽象类和接口)
    - [抽象类](#抽象类)
    - [接口](#接口)
- [设计模式](#设计模式)
    - [单例](#单例)
- [static修饰](#关键字static修饰)
- [JDK新特性](#JDK新特性)
 
    
    
# MySQL
关系型数据库
开源
稳定性有保障
#### 引擎
1.InnoDB 支持事务，5.5版本后默认是InnoDB.
2.MyISAM 5.5 之前默认是MyISAM不支持事务，不支持行级锁。最大的缺陷是崩溃之后无法安全恢复。

#### 两者比较
1.是否支持行级锁  
2.是否支持事务和崩溃后的安全恢复  
3.是否支持外键：MyISAM不支持 InnoDB支持  
4.是否支持MVCC：InnoDB支持。应对高并发事务,MVCC比单纯的加锁更加高效;MVCC只在READ COMMITTED 和  
REPEATABLE READ 两个隔离级别下工作；MVCC可以使用乐观锁和悲观锁来实现。  
#### MVCC
MVCC(Mutil-Version Concurrency Control)，就是多版本并发控制。MVCC 是一种并发控制的方法，  
一般在数据库管理系统中，实现对数据库的并发访问。  
####《mysql高性能》上有这样一句话
不要轻易相信MyISAM比InnoDB快之类的经验之谈，这个结论往往不是绝对的，在很多我们已知的场景中，  
InnoDB的速度都可以让MyISAM望尘莫及，  
尤其是用到了聚簇索引，或者需要访问的数据都可以放入内存的应用。  
一般情况我们选择InnoDB是没有问题的，但是在某些情况下，你并不在乎可扩展和并发能力，  
也不需要事务支持，也不在乎崩溃后的安全恢复问题的话，  
选择MyISAM也是一个不错的选择，但是一般情况，我们都是要考虑到这些问题的。  
#### 索引
mysql索引使用的数据结构主要有Btree索引和hash索引。对于hash索引来说，底层的数据结构就是hash表，  
因此在绝大多数需求为单条记录的查询的时候，可以选择hash索引，查询性能最快;其余大部分场景，建议选择BTree索引。  
MySQL的BTree索引使用的是B树中的B+树，但对于两种主要的存储引擎的实现方式是不同的。
1.MyISAM:B+树的节点的data域存放的是数据记录的地址。在索引检索的时候，首先按照B+Tree的搜索算发搜索索引，  
如果指定的key存在，则取出其data域的值，然后以data域的值为地址读取相应的数据记录。这被称之为“非聚簇索引”  
2.InnoDB:其数据文件本身就是索引文件。相比于MyISAM,索引和数据文件是分离的，其表数据文件本身就是按B+Tree组织的一个索引结构，  
树的叶节点保存了完整的数据结构。  
这个索引的key是数据表的主键，因此InnDB表数据文件本身就是主索引。这被称为聚簇索引。而其余的索引都作为辅助索引，  
辅助索引的data域存储相应记录主键的值而不是地址，这也是  
和MyISAM不同的地方。在根据主索引搜索时，直接找到key所在的节点即可去除数据；在根据辅助索引查找时，需要先取出主键的值，
再走一遍主索引。  
因此再设计表的时候，不建议使用过长的字段作为主键.  
### 缓存
执行查询语句的时候,会先查询缓存。不过，MySQL 8.0之后移除了缓存，因为这个功能不适用。  
因为缓存虽然能提升数据库的查询性能，但是缓存也带来了额外的开销，每次查询后要做一次缓存操作，  
失效之后还有销毁。因为，开启缓存查询要谨慎，尤其对于写密集  
的应用来说更是如此。如果开启，要注意合理控制缓存空间的大小，一班设置为几十MB比较合适。  
此外还可以通过sql_cache和sql_no_cache来控制某个查询语句是否需要缓存：  
select sql_no_cache count(*) from user;

### 事务
事务就是逻辑上的一组操作，要么都执行，要么都不执行。  
ACID   
原子性：Atomicity 事务时最小的执行单位，不允许分割。事务的原子性确保动作要么全部完成，要么完全不起作用。  
一致性：Consistency 执行事务前后，数据保持一致性，多个事务对同一个数据读取的结果时相同的。  
隔离性：Isolation 并发访问数据库时，一个用户的事务不被其它事务所干扰，并发事务之间的数据库时独立的。  
持久性：Durability 一个事务被提交之后。他对数据库的数据改变是持久的，即使数据库发生故障也不应该对其有任何影响。  

##### 并发事务带来哪些问题？
在典型的应用程序中，多个事务并发运行，经常会操作相同的数据来完成各自的任务（多个用户对同一数据进行操作）。  
并发虽然是必须的，但是可能会导致以下的问题。  
1.脏读：A事务对数据的更改且未提交的数据，被B事务读到  
2.不可重复读：A事务多次读取一个数据，在A还没结束的时候，另一个事务B访问了该数据并修改了该数据，  
那么A前后读取的数据就不一致。这样就发生了在一个事务中读到了两次不一样的数据。  
3.幻读：幻读和不可重复度类似。A事务读取了几行数据，另一个事务B插入了一些数据。在随后的查询中，  
A事务发现会多了些原本不存在的数据，就像发生了幻读。  
不可重复读的和幻读很容易混淆，不可重复读侧重于修改，幻读侧重于新增或删除。  
解决不可重复读的问题只需锁住满足条件的行，解决幻读需要锁表  
#####事务的隔离级别有哪些?MySQL的默认的隔离级别？
READ_UNCOMMITTED 读未提交 ：最低的隔离级别，允许读取尚未提交的数据变更，可能会导致脏读，幻读，不可重复读  
READ_COMMITTED   读已提交：允许读取并发事务已提交的事务，可以阻止脏读，但是幻读和不可重复读仍有可能发生。  
REPEATABLE_READ  可重复读：对同一字段的多次读取结果都是一致的，除非数据被自己本身修改，可以组织脏读和不可重复读，但幻读仍有可能发生。  
SERIALIZABLE     串行化：最高的隔离级别，完全服从ACID的隔离级别，所有事务逐个一次执行。可防脏读幻读不可重复读。  

隔离级别  | 脏读 |   不可重复读|  幻读
----     |----  |     ----   |---- 
读未提交  |   √  |      √     |     √
读已提交  |   ×  |      √     |     √
可重复读  |   ×  |      ×     |     √
串行化    |   ×  |      ×     |     ×  
#### 表级锁和行级锁的对比
表级锁：颗粒度大的一种锁，对当前操作的整张表加锁，实现简单，资源消耗比较少，加锁快，不会出现思索。  
其锁定粒度最大，出发锁冲突的概率最高，并发度最低，  
MyISAM和InnoDB引擎都支持表级锁。  
行级锁：锁定粒度最小的一种锁，只针对当前操作的行进行枷锁。行级锁能大大减少数据库操作的冲突。  
其枷锁粒度最小，并发度搞，但加锁的开销大，加锁慢，会出现死锁。  
#### 大表优化
当单表数据记录过大时，数据库的CRUD性能明显下降，一些常见的优化措施如下：  
1.限定数据的范围：务必禁止不带任何范围的查询语句；  
2.读写分离：经典的数据库拆分方案，主库负责写，从库负责读  
3.垂直分区：根据数据库里面数据表的相关性进行拆分。例如，用户表中既有用户的登录信息又有用户的基本信息，  
可以将用户表拆分成两个单独的表，甚至放到单独的库做分库。  
垂直拆分的优点：可以使数据列变小，在查询时减少读取的block数，减少I/O次数。此外，垂直分区可以简化表的结构，易于维护。 
垂直拆分的缺点：主键会出现冗余，需要管理冗余列，并会引起join操作，可以通过在应用曾进行join来解决。  
4.水平分区：保持数据表结构不变，通过某种策略存储数据分片，这样每一片数据分数到不同的表或者库中，  
达到了分布式的目的。水平拆分可以支撑非常大的数据量。  
数据库拆分方案：Sharding-JDBC.  


#### 数据库优化
##### 数据库命名规范
1.所有的数据库对象名必须使用小写+下划线分割  
2.所有数据库对象名禁止使用Mysql保留字  
3.数据库对象的明明要能做到见名知意，并且最好不要超过32个字符  
4.临时表必须以temp_为前缀以日期为后缀，备份表以bak_为前缀以日期为后缀（时间戳）  
5.所有存储相同数据的列明和类型必须一致。（一般 作为关联表，如果查询时关联列类型不一致  
会自动进行数据类型隐形转换，会造成列上的索引失效，降低查询效率）  
##### 数据库基本设计规范
1.如无特殊要求，所有表必须用InnoDB引擎  
2.数据库和表的字符集统一使用utf8mb4  
3.所有字段和表添加注释  
4.尽量控制单表大小不要超过500w  
5.msql限制每个表最多存储4096行，并且一行数据的大小不能超过65536字节，  
6.禁止在表中建立预留字段。key  
7.禁止越大在表中存储图片文件  
8.禁止在线上做数据库压测  
9.禁止从开发环境，测试环境直接连接生产数据库  
##### 数据库字段设计规范
1.优先选择符合存储需要的最小的数据类型 字段的列越大，建立索引时需要的空间也就越大。性能会越差。  
2.对于非负型的数据，有限使用无符号整型来存储。 原因：无符号相对于有符号可以多出一倍的存储空间.  
3.避免使用TEXT,BLOB数据类型，最常见的TEXT类型可以存储64k的数据  建议：把BLOB或者TEXT分离到单独的扩展表中.TEXT或者BLOB类型只能使用前缀索引。  
4.尽可能把所有列定义为NOT NULL ，因为索引NULL 列需要额外的空间来保存，所以需要占用更多的空间，进行比较和计算时要对NULL值做特别的处理。  
5.使用TIMESTAMP(4字节)或者DATETIME类型（8字节）存储时间  
6.财务相关的要用decimal类型  
##### 索引设计规范
1.每张表限制索引不超过5个  
2.禁止给表中的每一列都建立单独的索引。一个sql只能用到一个表中的一个索引，如需多个，使用联合索引的查询方式比较好。  
3.每个Innodb表必须有主键  
4.常见索引列建议：出现在select，update，delete语句的where从句中的列。包含在order by，group by distinct中的字段   
多个字段尽量使用联合索引 ，多表join的关联列  
5.不使用外键。外键会降低性能，用程序保持外键关系。  
##### Mysql开发规范
1.建议使用预编译语句进行数据库操作，只传参数，比传sql语句更高效，一次解析，多次使用，提高处理效率  
2.避免数据类型的隐形转换  
3.充分使用表上已经存在的索引 尽量避免'%%'，左'%'是可以用上索引的   
4.禁止select *   
5.禁止使用insert into values("A","B","C","D","E");  
6.避免使用join关联太多的表  
7.减少和数据库的交互次数  
8.能用in代替or就代替，in 后 值别超过500，or用不到索引  
9.where从句中禁止对列进行函数转换和计算。  
# 线程相关
#### 使用线程池的好处
线程池.数据库连接池，http连接池等都是这个思想。  
好处：  
1.降低资源消耗：通过重复利用已创建的线程降低线程创建和销毁所造成的消耗  
2.提高相应速度。当任务到达时，任务可以不需要等到线程创建就能立即执行。  
3.提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以同一分配，调优和监控。  
#### 阿里编程规范
1.创建线程池时指定有意义的线程名称。  
2.线程资源必须通过线程池提供，不允许在应用中自行显性创建线程  
3.线程池不允许通过Executors去创建，而是通过ThreadPoolExecutor的方式，  
这种方式让写的人更加明确线程池的运行规则，规避资源耗尽的风险。
#### Executors返回的线程池对象弊端如下
1.FixedThreadPool和SingleThreadPool:  
允许的请求队列长度为Integer.MAX_VALUE,可能会堆积大量的请求，从而导致OOM.  
2.CachedThreadPool:  
允许创建的线程数量为Integer.MAX_VALUE,可能会创建大量的线程，从而导致OOM。  
3.自定义的ThreadLocal手动清理掉  
#### ThreadPoolExecutor类简单介绍  
线程池实现类ThreadPoolExecutor是Executor框架最核心的类。  
ThreadPoolExecutor参数  
1.corePoolSize:核心线程数定义了最小可以同时运行的线程数量  
2.maximumPoolSize:当队列中存放的任务打到队列容量的时候，当前可以运行的线程数量变为最大线程数  
3.workQueue:当新任务来的时候会先判断当前运行的线程数量是否达到核心线程数，如果达到的话，新任务就会被存放在队列中。  
4.keepAliveTime:当线程池中的线程数量大于corePoolSize的时候，如果没有新任务提交，核心线程外的线程不会被立即销毁，  
而是会继续等待，等待时间超过了keepAliveTime时才会销毁;  
5.unit:keepAliveTime参数的时间单位  
6.threadFactory:executor创建新线程的时候会用到  
7.handler:拒绝策略  
7.1 ThreadPoolExecutor.AbortPolicy:抛出RejectedExecutionException来拒绝新任务的处理  
7.2 ThreadPoolExecutor.CallerRunsPolicy:调用执行自己的线程运行任务，也就是直接在调用execute方法的线程中  
运行被拒绝的任务，如果程序已经关闭，则丢弃该任务。  
7.3 ThreadPoolExecutor.DiscardPolicy:不处理新任务，直接丢弃  
7.4 ThreadPoolExecutor.DiscardOldestPolicy:此策略将丢弃最早的未处理的任务请求。
# 锁

# jvm相关

# mybatis

# 消息队列

# redis缓存

# springCloud 

# 索引 ElasticSearch

# 抽象类和接口
#### 抽象类
抽象类是对事物的抽象，而接口是对行为的抽象；  
1、抽象类使用abstract修饰；  
2、抽象类不能实例化，即不能使用new关键字来实例化对象；  
3、含有抽象方法（使用abstract关键字修饰的方法）的类是抽象类，必须使用abstract关键字修饰；  
4、抽象类可以含有抽象方法，也可以不包含抽象方法，抽象类中可以有具体的方法；  
5、如果一个子类实现了父类（抽象类）的所有抽象方法，那么该子类可以不必是抽象类，否则就是抽象类；  
6、抽象类中的抽象方法只有方法体，没有具体实现；  
#### 接口
1.接口使用interface修饰  
2.接口不能被实例化  
3.一个类只能继承一个类，但是可以实现多个接口  
4.接口中的方法均为抽象方法；  
5.接口不能包含实例域或静态方法  

抽象类可以有默认的方法，接口不能有实例方法  
抽象类被继承必须实现抽象方法，除非是抽象类  
抽象类不能被实例化，其它和正常类一样  



# 设计模式
 #### 单例
  ###### 饿汉模式 
  在类装载时构建
  ###### 懒汉模式 
  在第一次被使用时构建
  ###### 枚举的模式
  实现单例(推荐使用)
  private 私有化构造方法
    公共的获取方法。

# Java的三大特性
#### 封装
属性私有化 private 提供 getter，setter  
#### 继承
继承方法，代码复用  
#### 多态
多态指的是程序中定义的引用变量所指向的具体类型和通过该引用变脸发出的方法调用再  
编译期时是不确定的，而是再方法的运行期才确定的，即一个引用变量到底是只想哪个类的实例对象  

# JDK新特性

#### Lambda表达式

#### 流 

#### 时间/日期的改进
LocalDate/LocalTime/LocalDateTime

#### 本地变量类型推断
var hello = "hello word";

#### 字符串加强
"".isBlank();
" java ".strip();
#### 集合加强
java 9开始 jdk里面为集合 List,Set,Map都添加了of 和copyOf()  
方法，他们两个都用来创建不可变的集合  
List list = List.of("java","python");  
List copy = List.copyOf(list);  
System.out.println(list == copy); //true  

# 关键字static修饰
 #### 修饰类
  ######静态内部类
  内部类可以访问其所在类的属性，内部类创建自身对象需要先创建其所在类的对象。
  静态内部类中一个比较特殊的情况，一旦内部类使用static修饰，那么这个内部类就升级为顶级类。
 
 #### 方法 
 这个方法属于类本身，不需要创建实例就可以通过类调用  
 #### 变量
 这个变量属于类本身，不需要创建实例就可以获取到值  
 #### 代码块
 只加载一次。



