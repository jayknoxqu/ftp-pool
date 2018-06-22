package com.zhenjin.ftp.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.*;

/**
 * 实现文件上传下载
 *
 * @author ZhenJin
 */
@Slf4j
public class FtpClientTemplate {

    private GenericObjectPool<FTPClient> ftpClientPool;

    public FtpClientTemplate(FtpClientFactory ftpClientFactory) {
        this.ftpClientPool = new GenericObjectPool<>(ftpClientFactory);
    }

    /***
     * 上传Ftp文件
     *
     * @param localFile 当地文件
     * @param remotePath 上传服务器路径 - 应该以/结束
     */
    public boolean uploadFile(File localFile, String remotePath) {
        FTPClient ftpClient = null;
        BufferedInputStream inStream = null;
        try {
            //从池中获取对象
            ftpClient = ftpClientPool.borrowObject();
            // 改变工作路径
            ftpClient.changeWorkingDirectory(remotePath);
            inStream = new BufferedInputStream(new FileInputStream(localFile));
            log.info(localFile.getName() + "开始上传.....");

            final int retryTimes = 3;

            for (int j = 0; j <= retryTimes; j++) {
                boolean success = ftpClient.storeFile(localFile.getName(), inStream);
                if (success) {
                    log.info(localFile.getName() + "文件上传成功!");
                    return true;
                }
                log.warn("文件上传失败!试图重新上传... 尝试{}次", j);
            }

        } catch (FileNotFoundException e) {
            log.error("系统找不到指定的文件!{}", localFile);
        } catch (Exception e) {
            log.error("上传文件异常!", e);
        } finally {
            IOUtils.closeQuietly(inStream);
            //将对象放回池中
            ftpClientPool.returnObject(ftpClient);
        }
        return false;
    }

    /**
     * 下载文件
     *
     * @param remotePath FTP服务器文件目录
     * @param fileName   需要下载的文件名称
     * @param localPath  下载后的文件路径
     * @return
     */
    public boolean downloadFile(String remotePath, String fileName, String localPath) {
        FTPClient ftpClient = null;
        OutputStream outputStream = null;
        try {
            ftpClient = ftpClientPool.borrowObject();
            // 验证FTP服务器是否登录成功
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                return false;
            }

            ftpClient = ftpClientPool.borrowObject();
            // 切换FTP目录
            ftpClient.changeWorkingDirectory(remotePath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                if (fileName.equalsIgnoreCase(file.getName())) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(localPath).append(File.separator).append(file.getName());
                    File localFile = new File(stringBuilder.toString());
                    outputStream = new FileOutputStream(localFile);
                    ftpClient.retrieveFile(file.getName(), outputStream);
                }
            }
            ftpClient.logout();
            return true;
        } catch (Exception e) {
            log.error("下载文件异常", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            ftpClientPool.returnObject(ftpClient);
        }
        return false;
    }

    /**
     * 删除文件
     *
     * @param remotePath FTP服务器保存目录
     * @param fileName   要删除的文件名称
     * @return
     */
    public boolean deleteFile(String remotePath, String fileName) {
        FTPClient ftpClient = null;
        try {
            ftpClient = ftpClientPool.borrowObject();
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                return false;
            }
            // 切换FTP目录
            ftpClient.changeWorkingDirectory(remotePath);
            int delCode = ftpClient.dele(fileName);
            log.debug("删除文件:服务器返回的code为:{}", delCode);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败!", e);
        } finally {
            ftpClientPool.returnObject(ftpClient);
        }
        return false;
    }


}
