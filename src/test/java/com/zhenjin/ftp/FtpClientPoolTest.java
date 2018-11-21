package com.zhenjin.ftp;

import com.zhenjin.ftp.core.FtpClientTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;


/**
 * 测试ftp 上传.下载.删除
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FtpApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FtpClientPoolTest {

    @Autowired
    private FtpClientTemplate ftpTemplate;

    @Test
    public void uploadFile() {
        boolean uploadResult = ftpTemplate.uploadFile(new File("F:\\kubernetes-server-linux-amd64.tar.gz"), "/home/test");
        Assert.assertTrue(uploadResult);
    }

    @Test
    public void downloadFile() {
        boolean downloadResult = ftpTemplate.downloadFile("/home/test", "2017061315035721.txt", "F:\\");
        Assert.assertTrue(downloadResult);
    }

    @Test
    public void deleteFile() {
        boolean deleteResult = ftpTemplate.deleteFile("/home/test", "2017061315035721.txt");
        Assert.assertTrue(deleteResult);
    }


}
