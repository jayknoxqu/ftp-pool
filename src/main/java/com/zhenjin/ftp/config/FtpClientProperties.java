package com.zhenjin.ftp.config;

import org.apache.commons.net.ftp.FTP;

/**
 * ftp properties
 *
 * @author ZhenJin
 * @author Amanuel
 */
public class FtpClientProperties {

    private final String host;
    private final String username;
    private final String password;
    private final Integer dataTimeout;
    private final Integer connectTimeout;
    private final Integer port;
    private final Integer bufferSize;
    private final String encoding;
    private final boolean passiveMode;
    private final Integer keepAliveTimeout;
    private final Integer transferFileType;

    // Default values
    private static final int DEFAULT_PORT = 21;
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final boolean DEFAULT_PASSIVE_MODE = true;

    /**
     * Set keepAlive
     * Unit: second 0 disabled
     * Zero (or less) disables
     */
    private static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 0;

    /**
     * Transfer file type
     * in theory this should not be necessary as servers should default to ASCII
     * but, they don't all do so - see NET-500
     */
    private static final int DEFAULT_TRANSFER_FILE_TYPE = FTP.ASCII_FILE_TYPE;


    public FtpClientProperties(String host, String username, String password, Integer dataTimeout, Integer connectTimeout) {
       this(host, username, password, dataTimeout, connectTimeout, DEFAULT_PORT, DEFAULT_BUFFER_SIZE, DEFAULT_ENCODING, DEFAULT_PASSIVE_MODE, DEFAULT_KEEP_ALIVE_TIMEOUT, DEFAULT_TRANSFER_FILE_TYPE);
    }

    public FtpClientProperties(String host, String username, String password, Integer dataTimeout, Integer connectTimeout, Integer port, Integer bufferSize, String encoding, boolean passiveMode, Integer keepAliveTimeout, Integer transferFileType) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.dataTimeout = dataTimeout;
        this.connectTimeout = connectTimeout;
        this.port = port;
        this.bufferSize = bufferSize;
        this.encoding = encoding;
        this.passiveMode = passiveMode;
        this.keepAliveTimeout = keepAliveTimeout;
        this.transferFileType = transferFileType;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getDataTimeout() {
        return dataTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    public Integer getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public Integer getTransferFileType() {
        return transferFileType;
    }
}
