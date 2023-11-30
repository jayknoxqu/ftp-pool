package com.zhenjin.ftp.core;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

import static com.zhenjin.ftp.StringUtils.isAnyBlank;
import static com.zhenjin.ftp.StringUtils.isBlank;

/**
 * Implement file upload and download
 *
 * @author ZhenJin
 * @author Amanuel
 */
public class FtpClientTemplate {

    private final static Logger log = LoggerFactory.getLogger(FtpClientTemplate.class);

    //  Uses "lazy loading".
    //  The pool doesn't create objects until they are requested,
    //  and it maintains a minimum number of idle objects
    private final ObjectPool<FTPClient> objectPool;

    private final int retryCount;

    public FtpClientTemplate(FtpClientFactory ftpClientFactory) {
        this(3, ftpClientFactory);
    }

    public FtpClientTemplate(int retryCount, FtpClientFactory ftpClientFactory) {
        // For further tuning the number of objects in the pool,
        // custom GenericObjectPoolConfig can be passed to the GenericObjectPool
        this(retryCount, new GenericObjectPool<>(ftpClientFactory));
    }

    public FtpClientTemplate(int retryCount, ObjectPool<FTPClient> objectPool) {
        this.objectPool = objectPool;
        this.retryCount = retryCount;
    }

    /***
     * Upload Ftp files
     *
     * @param localFile local file
     * @param remotePath Upload server path - should end with /
     * @return true or false
     */
    public boolean uploadFile(File localFile, String remotePath) {
        if (null == localFile || isBlank(remotePath)) throw new IllegalArgumentException();
        FTPClient ftpClient = null;
        BufferedInputStream inStream = null;
        try {
            ftpClient = objectPool.borrowObject(); // Get or create client using FtpClientFactory
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("ftpServer refused connection, replyCode: {}", replyCode);
                return false;
            }

            ftpClient.changeWorkingDirectory(remotePath);
            inStream = new BufferedInputStream(new FileInputStream(localFile));
            log.info("start upload... {}", localFile.getName());

            for (int j = 0; j <= retryCount; j++) {
                boolean success = ftpClient.storeFile(localFile.getName(), inStream);
                if (success) {
                    log.info("upload file success! {}", localFile.getName());
                    return true;
                }
                log.warn("upload file failure! try uploading again... {} times", j);
            }

        } catch (FileNotFoundException e) {
            log.error("file not found! {}", localFile);
        } catch (Exception e) {
            log.error("upload file failure!", e);
        } finally {
            IOUtils.closeQuietly(inStream, (e) -> log.error("Error closing stream: ", e));
            if (ftpClient != null) {
                try {
                    // Put the object back into the pool
                    objectPool.returnObject(ftpClient);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return false;
    }

    /**
     * download file
     *
     * @param remotePath FTP server file directory
     * @param fileName   The name of the file to be downloaded
     * @param localPath  file path after downloading
     * @return true or false
     */
    public boolean downloadFile(String remotePath, String fileName, String localPath) throws Exception {
        if (isAnyBlank(remotePath, fileName, localPath)) throw new IllegalArgumentException();
        FTPClient ftpClient = null;
        OutputStream outputStream = null;
        try {
            ftpClient = objectPool.borrowObject();
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("ftpServer refused connection, replyCode:{}", replyCode);
                return false;
            }

            ftpClient.changeWorkingDirectory(remotePath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                if (fileName.equalsIgnoreCase(file.getName())) {
                    File localFile = new File(localPath + File.separator + file.getName());
                    outputStream = Files.newOutputStream(localFile.toPath());
                    ftpClient.retrieveFile(file.getName(), outputStream);
                }
            }
            ftpClient.logout();
            return true;
        } catch (Exception e) {
            log.error("download file failure!", e);
        } finally {
            IOUtils.closeQuietly(outputStream, (e) -> log.error("Error closing stream: ", e));
            objectPool.returnObject(ftpClient);
        }
        return false;
    }

    /**
     * Delete Files
     *
     * @param remotePath FTP server saving directory
     * @param fileName   The name of the file to be deleted
     * @return true or false
     */
    public boolean deleteFile(String remotePath, String fileName) throws Exception {
        if (isAnyBlank(remotePath, fileName)) throw new IllegalArgumentException();
        FTPClient ftpClient = null;
        try {
            ftpClient = objectPool.borrowObject();
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("ftpServer refused connection, replyCode: {}", replyCode);
                return false;
            }

            ftpClient.changeWorkingDirectory(remotePath);
            int delCode = ftpClient.dele(fileName);
            log.debug("delete file reply code: {}", delCode);
            return true;
        } catch (Exception e) {
            log.error("delete file failure!", e);
        } finally {
            objectPool.returnObject(ftpClient);
        }
        return false;
    }
}
