package pub.synx.service.impl;

import pub.synx.enums.EndVersionEnum;
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
import pub.synx.service.UserService;
import pub.synx.util.PasswordHelper;
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
public class UserServiceImpl implements UserService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTools userTools;

    @Autowired
    private UUIDTools uuidTools;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserAndGrpMapper userAndGrpMapper;

    @Autowired
    private PasswordHelper passwordHelper;


    /**
     * 用户添加
     * @param jsonObject
     * @return
     */
    @Override
    public ResultMessage addUser(JSONObject jsonObject) {
        JSONArray list = jsonObject.getJSONArray("userlist");
        if(list == null){
            throw new DataProcessException(ResultMessageEnum.USERLIST_NULL.getMessage());
        }

        String s = JSONObject.toJSONString(list, SerializerFeature.WriteClassName);

        //获取userlist
        List<User> userlist = JSONArray.parseArray(s, User.class);
        if(userlist.size() == 0){
            ResultMessage resultMessage = new ResultMessage();
            resultMessage.setData(new ArrayList<>());
            return resultMessage;
        }
        Set<String> accountSet = new HashSet<>();
        Set<String> mailSet = new HashSet<>();
        Boolean isNotRepeat = true;

        for (User user : userlist) {
            if(user.getAccount() != null){
                isNotRepeat = accountSet.add(user.getAccount());
                if(!isNotRepeat){
                    throw new DataProcessException(ResultMessageEnum.ATTRIBUTE_EXISTS.getMessage()+"输入的数据中存在多个用户关联同一个手机号"+user.getAccount());
                }
            }
            if(user.getMail() != null){
                isNotRepeat = mailSet.add(user.getMail());
                if(!isNotRepeat){
                    throw new DataProcessException(ResultMessageEnum.ATTRIBUTE_EXISTS.getMessage()+"输入的数据中存在多个用户关联同一个邮箱"+user.getAccount());
                }
            }
        }

        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            if(user.getAccount() != null){
                isNotRepeat = accountSet.add(user.getAccount());
                if(!isNotRepeat){
                    throw new DataProcessException(ResultMessageEnum.ATTRIBUTE_EXISTS.getMessage()+"手机号"+user.getAccount()+"已被绑定！");
                }
            }
            if(user.getMail() != null){
                isNotRepeat = mailSet.add(user.getMail());
                if(!isNotRepeat){
                    throw new DataProcessException(ResultMessageEnum.ATTRIBUTE_EXISTS.getMessage()+"邮箱"+user.getMail()+"已被绑定！");
                }
            }
        }

        List<String> res = new ArrayList<>();
        //id设置
        List<String> resIds = uuidTools.getString(userlist.size());
        int size = 0;

        for (User user : userlist) {
//            if(user.getAccount() == null && user.getMail() == null){
//                throw new DataProcessException(ResultMessageEnum.MAILORPHONE_NOTEXISTS.getMessage());
//            }
            //雪花算法生成用户Id
            user.setId(resIds.get(size));
            size++;
            //MD5加密用户
            user.setSalt(String.valueOf(System.currentTimeMillis()));
            if(user.getPassword() != null){
                user.setPassword(passwordHelper.encryptPassword(user.getPassword(),user.getSalt()));
            }
            res.add(user.getId());
        }
        List<User> existUsers = userMapper.selectList(null);
        for (User user : userlist) {
            for(User user1 : existUsers){
                if(user.getAccount() != null && user.getAccount().equals(user1.getAccount())){
                    throw new DataProcessException("账号"+user.getAccount()+"已被绑定！");
                }
                if(user.getMail() != null && user.getMail().equals(user1.getMail())){
                    throw new DataProcessException("邮箱"+user.getMail()+"已被绑定！");
                }
                if(user.getCode() != null && user.getCode().equals(user1.getCode())){
                    throw new DataProcessException("Code "+user.getCode()+"已被绑定！");
                }
            }
        }
        //通过userlist来添加
        userMapper.addUserlist(userlist);

        //装配返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.RESIGTER_SUCCESS.getMessage());
        resultMessage.setData(res);
        return resultMessage;
    }

    /**
     * 修改用户
     * @param user
     * @return
     * @throws Exception
     */
    public ResultMessage updateUser(User user) throws Exception {
        if (user.getId() == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }

        //获取待修改用户
        User realUser = userMapper.selectById(user.getId());
        //判断用户是否存在
        if (realUser == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }

        //判断是否输入有效的用户属性
//        if (user.getAccount() == null && user.getGender() == null && user.getCode() == null &&
//                user.getDescription() == null && user.getExtension() == null && user.getName() == null && user.getPassword() == null && user.getSalt() == null) {
//            throw new DataProcessException(ResultMessageEnum.ALL_USELESS.getMessage());
//        }

        user.setLastOperatorId(null);
        user.setLastUpdateTime(null);
        user.setCreatedTime(null);
        user.setCreatorId(null);

        //删除缓存
        redisUtil.del(RedisKeyEnum.USER_INFO.getMsg()+user.getId());

        //如果修改信息包含密码部分
        if (user.getPassword() != null) {
            user.setPassword(passwordHelper.encryptPassword(user.getPassword(), user.getSalt()));
        }
        //修改用户信息
        userMapper.updateById(user);

        //更新缓存
        Map map = userTools.getUserDetails(user.getId(), true, false, false);

        redisUtil.updateData(map);

        //渲染返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UPDATE_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }

    /**
     * 删除用户
     * @param jsonObject
     * @return
     * @throws Exception
     */
    @Override
    public ResultMessage deleteUser(JSONObject jsonObject) throws Exception {
        //获取当前用户
        String s = jsonObject.getString("ids");
        if(s == null){
            throw new DataProcessException(ResultMessageEnum.IDS_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);
        //获取输入的待解绑分组列表
        Integer cascade = jsonObject.getInteger("cascade");
        if(cascade == null){
            throw new DataProcessException(ResultMessageEnum.CASCADE_NULL.getMessage());
        }
        List<User> userlist = userMapper.selectList(null);
        List<String> userIds = new ArrayList<>();
        for (User user : userlist) {
            userIds.add(user.getId());
        }
        //求交集
        ids.retainAll(userIds);

        if(ids.size() == 0){
            ResultMessage resultMessage = new ResultMessage();
            resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
            resultMessage.setMessage(ResultMessageEnum.DELETE_SUCCESS.getMessage());
            return resultMessage;
//            throw new DataProcessException(ResultMessageEnum.USERLIST_NOTEXISTS.getMessage());
        }

        if(cascade == 1){
            unbindAll(ids);
        }

        for (String userId : ids) {
            redisUtil.del(RedisKeyEnum.USER_INFO.getMsg()+userId);
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg()+userId);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("ids",ids);
        userMapper.deleteUser(map);

        for (String userId : ids) {
            //修改Redis信息
            User user = new User();
            user.setId(userId);
            user.setLastUpdateTime(EndVersionEnum.END_VERSION);
            map = new HashMap<>();
            map.put("userInfo",user);
            map.put("id",userId);
            redisUtil.updateData(map);
        }

        //渲染返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.DELETE_SUCCESS.getMessage());
        return resultMessage;
    }

    /**
     * 查询用户
     * @param user
     * @return
     */
    public ResultMessage queryUser(User user){

        List<User> users = userMapper.queryUser(user);
        for (User u : users) {
            u.setPassword(ResultMessageEnum.MESSAGE_HIDE.getMessage());
            u.setSalt(ResultMessageEnum.MESSAGE_HIDE.getMessage());
        }
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(users);
        return resultMessage;
    }


    /**
     * 查询某个用户的全部信息
     * @param id
     * @return
     * @throws Exception
     */
    public ResultMessage getWholeUser(String id) throws Exception{
        String currentId = UserTools.getCurrentUserId();

        //用户Id不能为空
        if(id == null){
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }

        if(userMapper.selectById(id) == null){
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }


        Map map = userTools.getUserDetails(id, true, true, true);

        //如果获取的是用户自己的信息
        if(currentId.equals(id)){
            //存储信息
            map = redisUtil.storeData(map);
        }
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }

    /**
     * 绑定分组列表
     * @param id
     * @param jsonObject
     * @return
     * @throws Exception
     */
    @Override
    public ResultMessage bindGroupList(String id, JSONObject jsonObject) throws Exception {
        //用户Id不能为空
        if(id == null){
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        //用户Id不存在
        User realUser = userMapper.selectById(id);
        if(realUser == null){
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }

        //获取输入的待绑分组列表
        String s = jsonObject.getString("ids");

        if(s == null){
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);
        //获得有效分组Id
        List<Group> grouplist = groupMapper.getGrouplist(null);
        ArrayList<String> allGroupList = new ArrayList<>();
        for (Group group : grouplist) {
            allGroupList.add(group.getId());
        }
        //取交集
        ids.retainAll(allGroupList);
        allGroupList.clear();
        //获取用户已绑定分组Id
        List<Group> grouplist1 = groupMapper.getGrouplist(id);
        for (Group group : grouplist1) {
            allGroupList.add(group.getId());
        }
        //取差集
        ids.removeAll(allGroupList);
        if(ids.size() == 0){
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NOTEXISTS.getMessage());
        }
        Map<String, Object> map = new HashMap<>();

        //删除Redis中相关用户信息
        redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg()+id);
        //绑定分组
        bindGroup(id,ids);
        map = userTools.getUserDetails(id, false, true, false);
        redisUtil.updateData(map);
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.BIND_SUCCESS.getMessage());
        return resultMessage;
    }

    /**
     * 解绑分组
     * @param id
     * @param jsonObject
     * @return
     * @throws Exception
     */
    @Override
    public ResultMessage unbindGroupList(String id, JSONObject jsonObject) throws Exception {
        //用户Id不能为空
        if(id == null){
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        //用户Id不存在
        User realUser = userMapper.selectById(id);
        if(realUser == null){
            throw new DataProcessException(ResultMessageEnum.ID_NOTEXIST.getMessage());
        }
        //获取输入的待解绑分组列表
        String s = jsonObject.getString("ids");
        if(s == null){
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NULL.getMessage());
        }
        List<String> ids = JSONArray.parseArray(s, String.class);
        //获得有效分组Id
        List<Group> grouplist = groupMapper.getGrouplist(null);
        ArrayList<String> allGroupList = new ArrayList<>();
        for (Group group : grouplist) {
            allGroupList.add(group.getId());
        }
        //取交集
        ids.retainAll(allGroupList);
        //获取分组已绑定用户
        allGroupList.clear();
        List<Group> grouplist1 = groupMapper.getGrouplist(id);
        for (Group group : grouplist1) {
            allGroupList.add(group.getId());
        }
        ids.retainAll(allGroupList);
        if(ids.size() == 0){
            throw new DataProcessException(ResultMessageEnum.GROUPLIST_NOTEXISTS.getMessage());
        }
        Map<String, Object> map = new HashMap<>();

        //为自己解绑分组
        redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg()+id);

        unbindGroup(id,ids);

        //更新缓存
        map = userTools.getUserDetails(id, false, true, false);
        redisUtil.updateData(map);

        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UNBIND_SUCCESS.getMessage());
        return resultMessage;
    }


    /**
     * 解绑用户集中用户与分组、权限的关系
     * @param ids
     * @return
     */
    private boolean unbindAll(List<String> ids){
        //删除Redis中相关用户信息
        for (String id : ids) {
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg()+id);
        }

        Map<String, Object> map;
        try{
            for (String id : ids) {
                //解绑所有分组
                map = new HashMap<>();
                List<Group> grouplist = groupMapper.getGrouplist(id);
                List<String> groupIds = new ArrayList<>();
                for (Group group : grouplist) {
                    groupIds.add(group.getId());
                }
                if(groupIds.size() != 0){
                    unbindGroup(id,groupIds);
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * 用户绑定分组
     * @return
     */
    private void bindGroup(String userId,List<String> groupIds) {
        for (String groupId : groupIds) {
            UserAndGrp userAndGrp = new UserAndGrp();
            userAndGrp.setGroupId(groupId);
            userAndGrp.setUserId(userId);
            userAndGrpMapper.insert(userAndGrp);
        }
    }


    /**
     * 解绑用户和分组
     * @return
     */
    private void unbindGroup(String userId,List<String> groupIds) {
        for (String groupId : groupIds) {
            QueryWrapper<UserAndGrp> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",userId).eq("group_id",groupId);
            userAndGrpMapper.delete(wrapper);
        }
    }

}
