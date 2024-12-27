package pub.synx.aop;

import pub.synx.aop.anno.RequireLogin;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author SynX TA
 * @version 2024
 **/
@Aspect
@Component
@Order(1)
public class CheckLoginAspect extends BaseCheckAspect{
    @Before("@annotation(requireLogin)")
    public void checkLogin(RequireLogin requireLogin) {
        System.out.println("进入了checkLogin");
        String token = getRequest().getHeader("Authorization");
        HashMap<String, Object> currentUserInfo = this.getCurrentUserInfo(token);
        if (currentUserInfo.getOrDefault("basicInfo", null) == null) {
            throw new RuntimeException("当前用户未登录，请先进行登录");
        }
    }
}