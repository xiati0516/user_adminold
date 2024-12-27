package pub.synx.config;

import pub.synx.enums.RedisKeyEnum;
import pub.synx.pojo.db.User;
import pub.synx.util.BaseContext;
import pub.synx.util.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
public class UserInterceptor implements HandlerInterceptor {
    private final RedisUtil redisUtil;

    public UserInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null) {
            User user = (User) redisUtil.get(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg() + token);
            if (user != null) {
                BaseContext.removeCurrentUser();
                BaseContext.setCurrentUser(user);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentUser();
    }
}