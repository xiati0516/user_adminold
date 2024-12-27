package pub.synx.service.impl;

import pub.synx.enums.RedisKeyEnum;
import pub.synx.enums.ResultMessageEnum;
import pub.synx.enums.ResultStatusEnum;
import pub.synx.exception.DataProcessException;
import pub.synx.mapper.GroupMapper;
import pub.synx.mapper.UserAndGrpMapper;
import pub.synx.mapper.UserMapper;
import pub.synx.pojo.db.Group;
import pub.synx.pojo.db.User;
import pub.synx.pojo.db.UserAndGrp;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.GroupService;
import pub.synx.util.RedisUtil;
import pub.synx.util.UUIDTools;
import pub.synx.util.UserTools;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UUIDTools uuidTools;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserTools userTools;

    @Autowired
    private UserAndGrpMapper userAndGrpMapper;

    @Override
    public ResultMessage addGroup(JSONObject jsonObject) {
        JSONArray list = jsonObject.getJSONArray("grouplist");
        if (list == null) {
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NULL.getMessage());
        }
        String s = JSONObject.toJSONString(list, SerializerFeature.WriteClassName);

        //获取grouplist
        List<Group> grouplist = JSONArray.parseArray(s, Group.class);
        List<String> groupIds = new ArrayList<>();
        for (Group group : grouplist) {
            //雪花算法生成分组Id
            group.setId(uuidTools.get());
            groupIds.add(group.getId());
        }
        //通过userlist来添加
        groupMapper.addGrouplist(grouplist);

        //装配返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.RESIGTER_SUCCESS.getMessage());
        resultMessage.setData(groupIds);
        return resultMessage;
    }

    @Override
    public ResultMessage updateGroup(Group group) throws Exception {
        if (group.getName() == null && group.getManagerIds() == null && group.getDescription() == null &&
                group.getExtension() == null) {
            throw new DataProcessException(ResultMessageEnum.ALL_USELESS.getMessage());
        }
        if (group.getId() == null) {
            throw new DataProcessException(ResultMessageEnum.GROUPID_NULL.getMessage());
        }
        if (groupMapper.selectById(group.getId()) == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }
        //直接删除缓存
        Set<String> userIds = delGrpByGrpId(group.getId());

        groupMapper.update(group, null);
        //是否再把用户信息写回到缓存中？可以用异步
        addGrpRedis(userIds);

        //渲染返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UPDATE_SUCCESS.getMessage());
        resultMessage.setData(groupMapper.selectById(group.getId()));
        return resultMessage;
    }

    @Override
    public ResultMessage deleteGroup(JSONObject jsonObject) throws Exception {
        //获取输入的待解绑分组列表
        String s = jsonObject.getString("ids");

        if (s == null) {
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);

        //获取输入的待解绑分组列表
        Integer cascade = jsonObject.getInteger("cascade");
        if (cascade == null) {
            throw new DataProcessException(ResultMessageEnum.CASCADE_NULL.getMessage());
        }
        Set<String> userIds = new HashSet<>();
        for (String id : ids) {
            Set<String> idSet = delGrpRedisInfoByGrpId(id);
            userIds.addAll(idSet);
        }
        if (cascade == 1) {
            unbindAll(ids);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("ids", ids);

        groupMapper.deleteGroup(map);

        //是否再把用户信息写回到缓存中？可以用异步
        addGrpRedisInfo(userIds);
        //渲染返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.DELETE_SUCCESS.getMessage());
        return resultMessage;
    }

    @Override
    public ResultMessage deleteGroupAndUser(JSONObject jsonObject) throws Exception {
        //获取输入的待解绑分组列表
        String s = jsonObject.getString("ids");

        if (s == null) {
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);

        //获取输入的待解绑分组列表
        Integer cascade = jsonObject.getInteger("cascade");
        if (cascade == null) {
            throw new DataProcessException(ResultMessageEnum.CASCADE_NULL.getMessage());
        }
        Set<String> userIds = new HashSet<>();
        for (String id : ids) {
            Set<String> idSet = delGrpRedisInfoByGrpId(id);
            userIds.addAll(idSet);
        }
        if (cascade == 1) {
            unbindAll(ids);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("ids", ids);
        groupMapper.deleteGroup(map);

        //是否再把用户信息写回到缓存中？可以用异步
        addGrpRedisInfo(userIds);
        //渲染返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.DELETE_SUCCESS.getMessage());
        resultMessage.setData(userIds);
        return resultMessage;
    }

    @Override
    public ResultMessage queryGroup(Group group) {
        List<Group> groups = groupMapper.queryGroup(group);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(groups);
        return resultMessage;
    }

    @Override
    public ResultMessage getUserList(String id) {
        if (id == null) {
            throw new DataProcessException(ResultMessageEnum.GROUPID_NULL.getMessage());
        }
        if (groupMapper.selectById(id) == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }

        List<User> userlist = userMapper.getUserlist(id);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(userlist);

        return resultMessage;
    }

    @Override
    public ResultMessage bindUserList(String id, JSONObject jsonObject) throws Exception {
        //用户Id不能为空
        if (id == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        //分组Id不存在
        if (groupMapper.selectById(id) == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }
        //获取输入的待解绑分组列表
        String s = jsonObject.getString("ids");
        if (s == null) {
            throw new DataProcessException(ResultMessageEnum.IDS_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);
        //获得有效分组Id
        List<User> userlist = userMapper.getUserlist(null);
        ArrayList<String> allUserList = new ArrayList<>();
        for (User user1 : userlist) {
            allUserList.add(user1.getId());
        }
        //取交集
        ids.retainAll(allUserList);
        allUserList.clear();
        //获取分组已绑定用户
        List<User> userlist1 = userMapper.getUserlist(id);
        for (User user1 : userlist1) {
            allUserList.add(user1.getId());
        }
        //取差集
        ids.removeAll(allUserList);
        if (ids.size() == 0) {
            throw new DataProcessException(ResultMessageEnum.USERLIST_NOTEXISTS.getMessage());
        }
        HashMap<String, Object> map = new HashMap<>();
        //删除redis的用户信息
        for (String id1 : ids) {
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + id1);
        }

        bindUser(id, ids);

        Set<String> idsSet = new HashSet<>();
        for (String id1 : ids) {
            idsSet.add(id1);
        }
        addGrpRedisInfo(idsSet);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.BIND_SUCCESS.getMessage());
        return resultMessage;
    }


    @Override
    public ResultMessage unbindUserList(String id, JSONObject jsonObject) throws Exception {
        //用户Id不能为空
        if (id == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        //分组Id不存在
        if (groupMapper.selectById(id) == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }
        //获取输入的待解绑分组列表
        String s = jsonObject.getString("ids");
        if (s == null) {
            throw new DataProcessException(ResultMessageEnum.IDS_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);
        //获得有效分组Id
        List<User> userlist = userMapper.getUserlist(null);
        ArrayList<String> allUserList = new ArrayList<>();
        for (User user1 : userlist) {
            allUserList.add(user1.getId());
        }
        //取交集
        ids.retainAll(allUserList);
        //获取分组已绑定用户
        allUserList.clear();
        List<User> userlist1 = userMapper.getUserlist(id);
        for (User user1 : userlist1) {
            allUserList.add(user1.getId());
        }
        //取差集
        ids.retainAll(allUserList);
        if (ids.size() == 0) {
            throw new DataProcessException(ResultMessageEnum.USERLIST_NOTEXISTS.getMessage());
        }
        //删除redis的用户信息
        Set<String> idsSet = delGrpRedisInfoByGrpId(id);

        unbindUser(id, ids);

        addGrpRedisInfo(idsSet);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UNBIND_SUCCESS.getMessage());
        return resultMessage;
    }

    /**
     * 解绑分组集中分组与权限、用户的关系
     * @param ids
     * @return
     */
    private boolean unbindAll(List<String> ids) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            for (String id : ids) {
                //解绑所有用户
                map = new HashMap<>();
                List<User> userlist = userMapper.getUserlist(id);
                List<String> userIds = new ArrayList<>();
                for (User user : userlist) {
                    userIds.add(user.getId());
                }

                if (userIds.size() != 0) {

                    unbindUser(id, userIds);
                }

                for (String userId : userIds) {
                    redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + userId);
                }
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 添加用户信息
     * @param userIds
     */
    //@Async
    private void addGrpRedisInfo(Set<String> userIds) throws Exception {
        for (String userId : userIds) {
            Map map = userTools.getUserDetails(userId, false, true, true);
            redisUtil.storeData(map);
        }
    }

    private void addGrpRedis(Set<String> userIds) throws Exception {
        for (String userId : userIds) {
            Map map = userTools.getUserDetails(userId, false, true, false);
            redisUtil.storeData(map);
        }
    }

    /**
     * 删除分组对应的用户在Redis中的信息
     * @param id
     */
    private Set<String> delGrpRedisInfoByGrpId(String id) {
        //直接删除缓存
        Set<String> userIds = new HashSet<>();
        QueryWrapper<UserAndGrp> wrapper = new QueryWrapper<>();

        wrapper.eq("group_id", id);
        wrapper.select("user_id");
        List<UserAndGrp> userAndGrps = userAndGrpMapper.selectList(wrapper);

        for (UserAndGrp userAndGrp : userAndGrps) {
            userIds.add(userAndGrp.getUserId());
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + userAndGrp.getUserId());
        }
        return userIds;
    }




    private Set<String> delGrpByGrpId(String id) {
        //直接删除缓存
        Set<String> userIds = new HashSet<>();
        QueryWrapper<UserAndGrp> wrapper = new QueryWrapper<>();

        wrapper.eq("group_id", id);
        wrapper.select("user_id");
        List<UserAndGrp> userAndGrps = userAndGrpMapper.selectList(wrapper);

        for (UserAndGrp userAndGrp : userAndGrps) {
            userIds.add(userAndGrp.getUserId());
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + userAndGrp.getUserId());
        }
        return userIds;
    }

    private void bindUser(String groupId, List<String> userIds) {
        for (String userId : userIds) {
            UserAndGrp userAndGrp = new UserAndGrp();
            userAndGrp.setGroupId(groupId);
            userAndGrp.setUserId(userId);
            userAndGrpMapper.insert(userAndGrp);
        }
    }


    private void unbindUser(String groupId, List<String> userIds) {
        for (String userId : userIds) {
            QueryWrapper<UserAndGrp> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("group_id", groupId);
            userAndGrpMapper.delete(wrapper);
        }
    }

}
