package pub.synx.mapper;

import pub.synx.pojo.db.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author SynX TA
 * @version 2024
 **/
@Repository
public interface UserMapper extends BaseMapper<User> {

    //获取所有用户信息 非权限
    List<User> getUserlist(@Param("groupId") String groupId);
    //删除指定Id的用户
    int deleteUser(Map map);
    //查询用户
    List<User> queryUser(User user);
    //添加用户列表 内置权限
    int addUserlist(List<User> userlist);

}
