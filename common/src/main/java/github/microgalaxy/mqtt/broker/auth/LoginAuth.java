package github.microgalaxy.mqtt.broker.auth;

import java.io.Serializable;

/**
 * 登录认证信息
 *
 * @author Microgalaxy （https://github.com/micro-galaxy）
 */
public final class LoginAuth implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientId;
    private final String username;
    private final String password;
    private final String ipAddress;
    private final String protocols;
    private final Integer port;

    public LoginAuth(String clientId, String username, String password, String ipAddress, String protocols, Integer port) {
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.ipAddress = ipAddress;
        this.protocols = protocols;
        this.port = port;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getProtocols() {
        return protocols;
    }

    public Integer getPort() {
        return port;
    }
}
