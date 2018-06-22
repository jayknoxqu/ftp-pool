package com.zhenjin.ftp;

import com.zhenjin.ftp.core.FtpClientTemplate;
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
        ftpTemplate.uploadFile(new File("F:\\2017061315035721.txt"), "/home/test");
    }

    @Test
    public void downloadFile() {
        ftpTemplate.downloadFile("/home/test", "2017061315035721.txt", "F:\\");
    }

    @Test
    public void deleteFile() {
        ftpTemplate.deleteFile("/home/test", "2017061315035721.txt");
    }


}
