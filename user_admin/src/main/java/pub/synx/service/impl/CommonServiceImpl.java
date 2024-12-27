package pub.synx.service.impl;

import pub.synx.enums.RedisKeyEnum;
import pub.synx.enums.ResultMessageEnum;
import pub.synx.enums.ResultStatusEnum;
import pub.synx.exception.DataProcessException;
import pub.synx.mapper.UserMapper;
import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.CommonService;
import pub.synx.util.BaseContext;
import pub.synx.util.PasswordHelper;
import pub.synx.util.RedisUtil;
import pub.synx.util.UUIDTools;
import pub.synx.util.UserTools;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@Service
@Transactional
public class CommonServiceImpl implements CommonService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UUIDTools uuidTools;

    @Autowired
    private PasswordHelper passwordHelper;

    @Autowired
    private UserTools userTools;

    /**
     * 账号密码注册
     * @param user
     * @return
     * @throws DataProcessException
     */
    @Override
    public ResultMessage addUserByAccount(User user) {
        //JSON数据检查
        if (user.getAccount() == null) {
            throw new DataProcessException(ResultMessageEnum.ACCOUNT_NULL.getMessage());
        } else if (user.getPassword() == null) {
            throw new DataProcessException(ResultMessageEnum.PWD_NULL.getMessage());
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", user.getAccount());

        if (userMapper.selectOne(queryWrapper) != null) {
            throw new DataProcessException(ResultMessageEnum.ACCOUNT_EXISTS.getMessage());
        }

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mail", user.getMail());

        if (userMapper.selectOne(queryWrapper) != null) {
            throw new DataProcessException(ResultMessageEnum.MAIL_EXISTS.getMessage());
        }

        //雪花算法生成用户Id
        user.setId(uuidTools.get());
        //用户表添加用户id
        UserTools.setCreatedUserId(user.getId());
        //盐和密码
        passwordHelper.encryptPassword(user);
        //补全用户信息
        userMapper.insert(user);

        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setStatus(ResultStatusEnum.SUCCESS.getCode());
        resultMessage.setMessage(ResultMessageEnum.RESIGTER_SUCCESS.getMessage());
        return resultMessage;
    }


    /**
     * 账号密码登录
     * @param user
     * @return
     * @throws Exception
     */
    @Override
    public ResultMessage loginByAccount(User user) throws Exception {
        //判断账号密码是否为空
        if (user.getAccount() == null) {
            throw new DataProcessException(ResultMessageEnum.ACCOUNT_NULL.getMessage());
        } else if (user.getPassword() == null) {
            throw new DataProcessException(ResultMessageEnum.PWD_NULL.getMessage());
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", user.getAccount());

        User realUser = userMapper.selectOne(wrapper);

        if (realUser == null) {
            throw new DataProcessException(ResultMessageEnum.USER_NOTEXISTS.getMessage());
        } else if (!passwordHelper.encryptPassword(user.getPassword(), realUser.getSalt()).equals(realUser.getPassword())) {
            throw new DataProcessException(ResultMessageEnum.PWD_ERROR.getMessage());
        }

        // 生成唯一的SessionId
        String sessionId = UUID.randomUUID().toString();

        // 将用户信息存储到 Redis 中
        realUser.setPassword(null);
        realUser.setSalt(null);
        redisUtil.set(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg() + sessionId, realUser, 1800);
        // 同时存储一份用户信息到 ThreadLocal 中, 便于获取当前用户信息。
        BaseContext.setCurrentUser(realUser);

        //获取用户分组和权限列表
        Map map = userTools.getUserDetails(realUser.getId(), true, false, true, true);
        //将用户数据放入缓存,设置30min
        map = redisUtil.storeData(map);

        //token
        Map<String,Object> res = new HashMap<>();
        res.put("Authorization",sessionId);
        res.put("Info",map);
        //装配返回值
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setData(res);
        resultMessage.setMessage(ResultMessageEnum.LOGIN_SUCCESS.getMessage());
        return resultMessage;
    }
}
