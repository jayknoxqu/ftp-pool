## Use commons-pool2 to implement FTP connection pool



### 1. Overview of connection pool

Frequent establishment and closing of connections will greatly reduce the performance of a system. The connection pool will create a certain number of connections during initialization. Each visit only needs to obtain the connection from the connection pool and put it back after use. The connection pool does not directly close the connection. This can ensure that the program reuses the same connection without the need to establish and close the connection every time it is accessed, thereby improving system performance. 


### 2. Introduction to commons-pool2



#### 2.1 Introduction of pool2

```xml
<!-- Use commons-pool2 to implement ftp connection pool -->
<dependency>
     <groupId>org.apache.commons</groupId>
     <artifactId>commons-pool2</artifactId>
     <version>2.9.0</version>
</dependency>

<!-- Introduce FTPClient as a pooling object -->
<dependency>
     <groupId>commons-net</groupId>
     <artifactId>commons-net</artifactId>
     <version>3.10.0</version>
</dependency>
```



#### 2.2 Composition of pool2
There are 3 main Interfaces to consider:
`PooledObject (pooled object), PooledObjectFactory (object factory) and ObjectPool (object pool)`

The corresponding is:

`FTPClient(pooled object) FTPClientFactory(object factory) FTPClientPool(object pool)`

relation chart:

![Relationship diagram](https://raw.githubusercontent.com/amenski/ftp-pool/master/img/840965-e7a5179ac162e8b0.png)



### 3. Implement connection pool



#### 3.1 Configure FtpClient

We already have a ready-made pooled object (FtpClient), we just need to add configuration

```java
public class FtpClientProperties {
    ....
}

new FtpClientProperties("127.0.0.1", "test", "test", 30000, 30000);
```




#### 3.2 Create FtpClientFactory

​

There are two factories in commons-pool2: PooledObjectFactory and KeyedPooledObjectFactory. We use the former.

```java
public interface PooledObjectFactory<T> {
    //Create object
     PooledObject<T> makeObject();
    //activate object
     void activateObject(PooledObject<T> obj);
     //passivate object
     void passivateObject(PooledObject<T> obj);
     //Verification object
     boolean validateObject(PooledObject<T> obj);
     //Destroy object
     void destroyObject(PooledObject<T> obj);
}
```


To create FtpClientFactory, you only need to inherit the abstract class BasePooledObjectFactory, and it implements PooledObjectFactory

```java
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

     private FtpClientProperties config;

     public FtpClientFactory(FtpClientProperties config) {
         this.config = config;
     }

     /**
      * Create FtpClient object
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
      * Use PooledObject to encapsulate the object and put it into the pool
      */
     @Override
     public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
         return new DefaultPooledObject<>(ftpClient);
     }

     /**
      * Destroy the FtpClient object
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
      * Verify FtpClient object
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

#### 3.3 FtpClientPool

​ Three object pools are preset in commons-pool2 that can be used directly: GenericObjectPool, GenericKeyedObjectPool and SoftReferenceObjectPool

Here we use:
```java
GenericObjectPool<FTPClient> ftpClientPool = new GenericObjectPool<>(new FtpClientFactory());
```


Specific usage reference test class: https://github.com/amenski/ftp-pool/blob/master/src/test/java/com/zhenjin/ftp/FtpClientPoolTest.java
