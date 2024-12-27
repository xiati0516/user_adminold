package pub.synx.mapper;

import pub.synx.pojo.db.Group;
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
public interface GroupMapper extends BaseMapper<Group> {
    //获取分组列表
    List<Group> getGrouplist(@Param("userId") String userId);

    //添加分组列表 内置权限
    int addGrouplist(List<Group> grouplist);

    //删除分组
    int deleteGroup(Map map);

    //根据属性查询分组
    List<Group> queryGroup(Group group);
}
