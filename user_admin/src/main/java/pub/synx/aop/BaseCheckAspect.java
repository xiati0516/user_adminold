package pub.synx.aop;

import pub.synx.enums.RedisKeyEnum;
import pub.synx.pojo.db.User;
import pub.synx.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
public class BaseCheckAspect {
    @Autowired
    private RedisUtil redisUtil;

    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public HashMap<String, Object> getCurrentUserInfo(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("当前用户未登录，请先进行登录");
        }
        User user = (User) redisUtil.get(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg() + token);
        if (user == null) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        HashMap<String, Object> curUserInfo = new HashMap<>();
        curUserInfo.put("basicInfo", user);
        curUserInfo.put("groupInfo", redisUtil.hget(RedisKeyEnum.GROUP_INFO.getMsg() + user.getId(), "grpInfo"));

        return curUserInfo;
    }
}