

## 使用commons-pool2实现FTP连接池



### 一. 连接池概述

​	频繁的建立和关闭连接，会极大的降低系统的性能，而连接池会在初始化的时候会创建一定数量的连接，每次访问只需从连接池里获取连接,使用完毕后再放回连接池，并不是直接关闭连接，这样可以保证程序重复使用同一个连接而不需要每次访问都建立和关闭连接， 从而提高系统性能。



### 二. commons-pool2介绍



#### 2.1 pool2的引入

```xml
<!-- 使用commons-pool2 实现ftp连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.5.0</version>
</dependency>

<!-- 引入FTPClient作为池化对象 -->
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.6</version>
</dependency>
```



#### 2.2 pool2的组成

`PooledObject(池化对象)  PooledObjectFactory(对象工厂)  ObjectPool (对象池)`

对应为:

`FTPClient(池化对象)  FTPClientFactory(对象工厂)   FTPClientPool(对象池)`

关系图:

![关系图](https://raw.githubusercontent.com/jayknoxqu/ftp-pool/master/img/840965-e7a5179ac162e8b0.png)



### 三. 实现连接池



####  3.1 配置FtpClient

我们已经有现成的池化对象(FtpClient)了,只需要添加配置即可

```java
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "ftp.client")
public class FtpClientProperties {
    // ftp地址
    private String host;
    // 端口号
    private Integer port = 21;
    // 登录用户
    private String username;
    // 登录密码
    private String password;
    // 被动模式
    private boolean passiveMode = false;
    // 编码
    private String encoding = "UTF-8";
    // 连接超时时间(秒)
    private Integer connectTimeout;
    // 缓冲大小
    private Integer bufferSize = 1024;
    // 传输文件类型
    private Integer transferFileType;
}
```

application.properties配置为:

```
ftp.client.host=127.0.0.1
ftp.client.port=22
ftp.client.username=root
ftp.client.password=root
ftp.client.encoding=utf-8
ftp.client.passiveMode=false
ftp.client.connectTimeout=30000
```



#### 3.2 创建FtpClientFactory

​	

​	在commons-pool2中有两种工厂：PooledObjectFactory 和KeyedPooledObjectFactory，我们使用前者。

```java
public interface PooledObjectFactory<T> {
	//创建对象
    PooledObject<T> makeObject();
	//激活对象
    void activateObject(PooledObject<T> obj);
    //钝化对象
    void passivateObject(PooledObject<T> obj);
    //验证对象
    boolean validateObject(PooledObject<T> obj);
    //销毁对象
    void destroyObject(PooledObject<T> obj);
}
```
​	

​	创建FtpClientFactory只需要继承BasePooledObjectFactory这个抽象类 ,而它则实现了PooledObjectFactory

```java
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

    private FtpClientProperties config;

    public FtpClientFactory(FtpClientProperties config) {
        this.config = config;
    }

    /**
     * 创建FtpClient对象
     */
    @Override
    public FTPClient create() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(config.getEncoding());
        ftpClient.setConnectTimeout(config.getConnectTimeout());
        try {

            ftpClient.connect(config.getHost(), config.getPort());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                log.warn("FTPServer refused connection,replyCode:{}", replyCode);
                return null;
            }

            if (!ftpClient.login(config.getUsername(), config.getPassword())) {
                log.warn("ftpClient login failed... username is {}; password: {}", config.getUsername(), config.getPassword());
            }

            ftpClient.setBufferSize(config.getBufferSize());
            ftpClient.setFileType(config.getTransferFileType());
            if (config.isPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            }

        } catch (IOException e) {
            log.error("create ftp connection failed...", e);
        }
        return ftpClient;
    }

    /**
     * 用PooledObject封装对象放入池中
     */
    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        return new DefaultPooledObject<>(ftpClient);
    }

    /**
     * 销毁FtpClient对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> ftpPooled) {
        if (ftpPooled == null) {
            return;
        }

        FTPClient ftpClient = ftpPooled.getObject();

        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException io) {
            log.error("ftp client logout failed...{}", io);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException io) {
                log.error("close ftp client failed...{}", io);
            }
        }
    }

    /**
     * 验证FtpClient对象
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> ftpPooled) {
        try {
            FTPClient ftpClient = ftpPooled.getObject();
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            log.error("Failed to validate client: {}", e);
        }
        return false;
    }

}
```

#### 3.3 实现FtpClientPool

​	在commons-pool2中预设了三个可以直接使用的对象池：GenericObjectPool、GenericKeyedObjectPool和SoftReferenceObjectPool 

示列:

```java
GenericObjectPool<FTPClient> ftpClientPool = new GenericObjectPool<>(new FtpClientFactory());
```


我们也可以自己实现一个连接池:
```java
public interface ObjectPool<T> extends Closeable {
    // 从池中获取一个对象
    T borrowObject();
	// 归还一个对象到池中
    void returnObject(T obj);
    // 废弃一个失效的对象
    void invalidateObject(T obj); 
    // 添加对象到池
    void addObject();
	// 清空对象池
    void clear();
    // 关闭对象池
    void close();
}
```
通过继承BaseObjectPool去实现ObjectPool
```java
public class FtpClientPool extends BaseObjectPool<FTPClient> {

    private static final int DEFAULT_POOL_SIZE = 8;

    private final BlockingQueue<FTPClient> ftpBlockingQueue;
    private final FtpClientFactory ftpClientFactory;


    /**
     * 初始化连接池，需要注入一个工厂来提供FTPClient实例
     *
     * @param ftpClientFactory ftp工厂
     * @throws Exception
     */
    public FtpClientPool(FtpClientFactory ftpClientFactory) throws Exception {
        this(DEFAULT_POOL_SIZE, ftpClientFactory);
    }

    public FtpClientPool(int poolSize, FtpClientFactory factory) throws Exception {
        this.ftpClientFactory = factory;
        ftpBlockingQueue = new ArrayBlockingQueue<>(poolSize);
        initPool(poolSize);
    }

    /**
     * 初始化连接池，需要注入一个工厂来提供FTPClient实例
     *
     * @param maxPoolSize 最大连接数
     * @throws Exception
     */
    private void initPool(int maxPoolSize) throws Exception {
        for (int i = 0; i < maxPoolSize; i++) {
            // 往池中添加对象
            addObject();
        }
    }

    /**
     * 从连接池中获取对象
     */
    @Override
    public FTPClient borrowObject() throws Exception {
        FTPClient client = ftpBlockingQueue.take();
        if (ObjectUtils.isEmpty(client)) {
            client = ftpClientFactory.create();
            // 放入连接池
            returnObject(client);
            // 验证对象是否有效
        } else if (!ftpClientFactory.validateObject(ftpClientFactory.wrap(client))) {
            // 对无效的对象进行处理
            invalidateObject(client);
            // 创建新的对象
            client = ftpClientFactory.create();
            // 将新的对象放入连接池
            returnObject(client);
        }
        return client;
    }

    /**
     * 返还对象到连接池中
     */
    @Override
    public void returnObject(FTPClient client) {
        try {
            if (client != null && !ftpBlockingQueue.offer(client, 3, TimeUnit.SECONDS)) {
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(client));
            }
        } catch (InterruptedException e) {
            log.error("return ftp client interrupted ...{}", e);
        }
    }

    /**
     * 移除无效的对象
     */
    @Override
    public void invalidateObject(FTPClient client) {
        try {
            client.changeWorkingDirectory("/");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ftpBlockingQueue.remove(client);
        }
    }

    /**
     * 增加一个新的链接，超时失效
     */
    @Override
    public void addObject() throws Exception {
        // 插入对象到队列
        ftpBlockingQueue.offer(ftpClientFactory.create(), 3, TimeUnit.SECONDS);
    }

    /**
     * 关闭连接池
     */
    @Override
    public void close() {
        try {
            while (ftpBlockingQueue.iterator().hasNext()) {
                FTPClient client = ftpBlockingQueue.take();
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(client));
            }
        } catch (Exception e) {
            log.error("close ftp client ftpBlockingQueue failed...{}", e);
        }
    }

}
```

不太赞成自己去实现连接池,这样会带来额外的维护成本...

**具体使用参考测试类:**
https://github.com/jayknoxqu/ftp-pool/blob/master/src/test/java/com/zhenjin/ftp/FtpClientPoolTest.java

### 四. 代码地址: 

​     **GitHub :** https://github.com/jayknoxqu/ftp-pool

​     **码云 :** https://gitee.com/jayknoxqu/ftp-pool


### 五. 使用 Docker 快速搭建 FTP 服务

```
docker run -d -v /home/vsftpd:/home/vsftpd -p 21:21 -e FTP_USER=test -e FTP_PASS=test --name vsftpd fauria/vsftpd
```

### 六. 参考资料:

[FTPClient连接池的实现 ](https://yq.aliyun.com/articles/5904)

[Apache Commons-pool2（整理）](https://www.jianshu.com/p/b0189e01de35)

[commons-pool2 官方案列](http://commons.apache.org/proper/commons-pool/examples.html)


