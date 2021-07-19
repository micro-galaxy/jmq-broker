package github.microgalaxy.mqtt.broker.auth;


import org.springframework.stereotype.Service;

/**
 * @author Microgalaxy
 */
@Service
public class LoginAuthServer implements LoginAuthInterface {
    @Override
    public boolean loginAuth(LoginAuth loginAuth) {
        return true;
    }
}
