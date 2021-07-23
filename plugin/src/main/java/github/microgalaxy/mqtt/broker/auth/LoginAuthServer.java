package github.microgalaxy.mqtt.broker.auth;


import org.springframework.stereotype.Service;

/**
 * @author Microgalaxy
 */
@Service
public class LoginAuthServer implements LoginAuthInterface {
    @Override
    public boolean loginAuth(LoginAuth loginAuth) {
//        throw new RuntimeException("账号密码错误");
        return true;
    }
}
