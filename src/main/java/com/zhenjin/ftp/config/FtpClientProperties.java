package com.zhenjin.ftp.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.net.ftp.FTP;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ftp客服端连接配置
 *
 * @author ZhenJin
 * @see "https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config-typesafe-configuration-properties"
 */
@Getter
@Setter
@Component
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "ftp.client")
public class FtpClientProperties {

    /**
     * ftp地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port = 21;

    /**
     * 登录用户
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 被动模式
     */
    private boolean passiveMode = false;

    /**
     * 编码
     */
    private String encoding = "UTF-8";

    /**
     * 连接超时时间(秒)
     */
    private Integer connectTimeout;

    /**
     * 传输超时时间(秒)
     */
    private Integer dataTimeout;

    /**
     * 缓冲大小
     */
    private Integer bufferSize = 1024;

    /**
     * 设置keepAlive
     * 单位:秒  0禁用
     * Zero (or less) disables
     */
    private Integer keepAliveTimeout = 0;

    /**
     * 传输文件类型
     * in theory this should not be necessary as servers should default to ASCII
     * but they don't all do so - see NET-500
     */
    private Integer transferFileType = FTP.ASCII_FILE_TYPE;


}
