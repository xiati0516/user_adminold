package pub.synx.util;

import pub.synx.enums.ResultMessageEnum;
import pub.synx.mapper.GroupMapper;
import pub.synx.mapper.UserAndGrpMapper;
import pub.synx.mapper.UserMapper;
import pub.synx.pojo.db.Group;
import pub.synx.pojo.db.User;
import pub.synx.pojo.db.UserAndGrp;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
public class UserTools {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserAndGrpMapper userAndGrpMapper;

    @Setter
    private static String createdUserId = "";


    public static User getCurrentUser() {
        User currentUser = BaseContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        return currentUser;
    }

    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 获取指定ID的用户的分组与权限列表
     * @param id    用户ID
     * @param basic 是否获取基础信息
     * @param grp   是否获取分组信息
     * @return
     */
    public Map getUserDetails(String id, Boolean basic, Boolean grp, Boolean perm) {
        return getUserDetails(id, basic, false, grp, perm);
    }

    /**
     * 获取指定Id的用户的分组与权限列表
     * @param id
     * @return
     */
    public Map getUserDetails(String id, Boolean basic, Boolean isShow, Boolean grp, Boolean perm) {

        Map<String, Object> res = new HashMap<>();
        User user = userMapper.selectById(id);
        List<Group> grouplist = new ArrayList<>();
        List<String> groupIds = null;
        if (basic) {
            if (!isShow) {
                user.setSalt(ResultMessageEnum.MESSAGE_HIDE.getMessage());
                user.setPassword(ResultMessageEnum.MESSAGE_HIDE.getMessage());
            }
            res.put("userInfo", user);
        }
        //获取用户分组和权限列表
        //获取用户分组列表
        if (grp) {
            QueryWrapper<UserAndGrp> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", id);
            List<UserAndGrp> userAndGrps = userAndGrpMapper.selectList(wrapper);
            groupIds = new ArrayList<>();
            for (UserAndGrp userAndGrp : userAndGrps) {
                groupIds.add(userAndGrp.getGroupId());
            }
            if (groupIds.size() > 0) {
                QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
                groupQueryWrapper.in("id", groupIds);
                grouplist = groupMapper.selectList(groupQueryWrapper);
            }
            res.put("grpInfo", grouplist);
        }
        res.put("id", id);
        return res;
    }
}
