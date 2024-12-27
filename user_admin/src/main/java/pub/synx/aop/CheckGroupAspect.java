package pub.synx.aop;

import pub.synx.aop.anno.RequireGroup;
import pub.synx.pojo.db.Group;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author SynX TA
 * @version 2024
 **/
@Aspect
@Component
@Order(2)
public class CheckGroupAspect extends BaseCheckAspect {
    @Before("@annotation(requireGroup)")
    public void checkGroup(RequireGroup requireGroup) {
        String token = getRequest().getHeader("Authorization");
        HashMap<String, Object> currentUserInfo = this.getCurrentUserInfo(token);
        List<Group> groups = (List<Group>) currentUserInfo.getOrDefault("groupInfo", null);

        if (groups == null) {
            throw new RuntimeException("当前用户分组权限不足，请检查是否具备分组权限");
        }

        for (String requiredGroup : requireGroup.value()) {
            if (groups.stream().noneMatch(group -> group.getName().equals(requiredGroup))) {
                throw new RuntimeException("当前用户分组权限不足，请检查是否具备分组权限");
            }
        }
    }
}