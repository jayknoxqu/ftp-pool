package com.zhenjin.ftp.config;

import com.zhenjin.ftp.core.FtpClientFactory;
import com.zhenjin.ftp.core.FtpClientTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FTPClient配置类，封装了FTPClient的相关配置
 *
 * @author ZhenJin
 *
 */
@Configuration
@EnableConfigurationProperties(FtpClientProperties.class)
public class FtpClientConfigure {

    private FtpClientProperties ftpClientProperties;

    @Autowired
    public void setFtpClientProperties(FtpClientProperties ftpClientProperties) {
        this.ftpClientProperties = ftpClientProperties;
    }

    @Bean
    public FtpClientFactory getFtpClientFactory() {
        return new FtpClientFactory(ftpClientProperties);
    }

    @Bean
    public FtpClientTemplate getFtpTemplate(){
        return new FtpClientTemplate(getFtpClientFactory());
    }

}
