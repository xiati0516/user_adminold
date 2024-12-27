package pub.synx.controller;


import pub.synx.aop.anno.RequireLogin;
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
import pub.synx.util.PasswordHelper;
import pub.synx.util.RedisUtil;
import pub.synx.util.UserTools;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SynX TA
 * @version 2024
 **/
@RestController
@RequestMapping("/personal")
public class PersonInfoControllor {
    @Autowired
    private PasswordHelper passwordHelper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTools userTools;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserAndGrpMapper userAndGrpMapper;

    /**
     * 查看个人信息
     *
     * @return
     */
    @RequireLogin
    @GetMapping("/infos")
    public ResultMessage getPersonalInfo() throws Exception {
        User user = UserTools.getCurrentUser();
        Map map = userTools.getUserDetails(user.getId(), true, true, true);
        //将用户数据放入缓存,设置30min
        map = redisUtil.storeData(map);
        //装配返回信息
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @param resultMessage
     * @param request
     * @return
     */
    @RequireLogin
    @PutMapping("/infos")
    public ResultMessage updatePersonalInfo(@RequestBody User user, ResultMessage resultMessage, HttpServletRequest request) throws Exception {
        String id = UserTools.getCurrentUserId();
        //判断是否输入有效的用户属性
        if (user.getAccount() == null && user.getGender() == null && user.getDescription() == null
                && user.getExtension() == null && user.getName() == null && user.getIdentity() == null && user.getCode() == null) {
            throw new DataProcessException(ResultMessageEnum.ALL_USELESS.getMessage());
        }
        //更新缓存
        if (redisUtil.hasKey(RedisKeyEnum.USER_INFO.getMsg() + id)) {
            redisUtil.del(RedisKeyEnum.USER_INFO.getMsg() + id);
        }

        //装配
        user.setId(id);
        user.setPassword(null);
        user.setMail(null);
        user.setSalt(null);

        userMapper.updateById(user);

        Map map = userTools.getUserDetails(id, true, false, false);
        //更新缓存
        //对缓存中时间戳进行处理
        redisUtil.updateData(map);

        //渲染返回数据
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UPDATE_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }


    /**
     * 修改密码
     *
     * @param jsonObject
     * @return
     */
    @RequireLogin
    @PutMapping("/password")
    public ResultMessage updatePwdByOldPwd(@RequestBody JSONObject jsonObject) throws Exception {
        String id = UserTools.getCurrentUserId();

        String oldPwd = jsonObject.getString("oldPwd");
        String newPwd = jsonObject.getString("newPwd");
        //获取当前用户
        User user = userMapper.selectById(id);
        //判断输入字段
        if (oldPwd == null) {
            throw new DataProcessException(ResultMessageEnum.OLDPWD_NULL.getMessage());
        } else if (newPwd == null) {
            throw new DataProcessException(ResultMessageEnum.NEWPWD_NULL.getMessage());
        }


        if (!user.getPassword().equals(passwordHelper.encryptPassword(oldPwd, user.getSalt()))) {
            throw new DataProcessException(ResultMessageEnum.OLDPWD_ERROR.getMessage());
        }

        redisUtil.del(RedisKeyEnum.USER_INFO.getMsg() + id);
        //修改密码
        UpdateWrapper<User> wrapper = new UpdateWrapper<User>().eq("id", id).set("password", passwordHelper.encryptPassword(newPwd, user.getSalt()));
        userMapper.update(user, wrapper);
        //更新缓存
        Map map = userTools.getUserDetails(id, true, false, false);
        redisUtil.updateData(map);

        //装配返回值
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.UPDATE_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }

    /**
     * 获取用户属组列表
     *
     * @return 用户属组列表
     */
    @RequireLogin
    @GetMapping("/groups")
    public ResultMessage getGroupList() throws Exception {
        String id = UserTools.getCurrentUserId();
        //获取用户
        Map map = userTools.getUserDetails(id, false, true, false);
        map = redisUtil.storeData(map);
        //装配返回结果
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        resultMessage.setData(map);
        return resultMessage;
    }


    /**
     * 本地注销
     */
    @RequireLogin
    @DeleteMapping("/cancel")
    public ResultMessage deleteAccount(@RequestBody JSONObject jsonObject) throws Exception {
        String id = UserTools.getCurrentUserId();
        //获取输入的待解绑分组列表
        Integer cascade = jsonObject.getInteger("cascade");
        if (cascade == null) {
            throw new DataProcessException(ResultMessageEnum.CASCADE_NULL.getMessage());
        }
        //强制删除时解绑权限和分组
        if (cascade == 1) {
            unbindAll(id);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);

        //更新缓存
        User realUser = new User();
        realUser.setId(id);

        redisUtil.del(RedisKeyEnum.USER_INFO.getMsg() + id);

        userMapper.deleteById(id);

        //装配返回数据
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.DELETE_SUCCESS.getMessage());
        return resultMessage;
    }

    /**
     * 解绑当前用户与分组、权限的关系
     *
     * @param id
     * @return
     */
    private boolean unbindAll(String id) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + id);
            //解绑所有分组
            map = new HashMap<>();
            QueryWrapper<Group> wrapper = new QueryWrapper<>();
            wrapper.eq("id", id);
            List<Group> grouplist = groupMapper.selectList(wrapper);
            List<String> groupIds = new ArrayList<>();
            for (Group group : grouplist) {
                groupIds.add(group.getId());
            }
            if (groupIds.size() != 0) {
                QueryWrapper<UserAndGrp> userAndGrp = new QueryWrapper<>();
                userAndGrp.eq("user_id", id).in("group_id", groupIds);
                userAndGrpMapper.delete(userAndGrp);
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
