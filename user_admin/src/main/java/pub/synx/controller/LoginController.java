package pub.synx.controller;

import pub.synx.aop.anno.RequireLogin;
import pub.synx.enums.RedisKeyEnum;
import pub.synx.enums.ResultMessageEnum;
import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.CommonService;
import pub.synx.util.BaseContext;
import pub.synx.util.RedisUtil;
import pub.synx.util.UserTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private CommonService commonService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 账号密码登录
     *
     * @param user 封装账号密码
     * @return 返回登录用户信息及token
     */
    @PostMapping("/account")
    public ResultMessage loginByAccount(@RequestBody User user) throws Exception {
        return commonService.loginByAccount(user);
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    @GetMapping("/getCurrentUser")
    public ResultMessage getCurrentUser() {
        User user = UserTools.getCurrentUser();
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setMessage(ResultMessageEnum.GET_SUCCESS.getMessage());
        if (user == null) {
            resultMessage.setMessage(ResultMessageEnum.USER_NULL.getMessage());
        }
        resultMessage.setData(user);
        return resultMessage;
    }

    /**
     * 登出系统
     *
     * @return
     */
    @RequireLogin
    @DeleteMapping("/logout")
    public ResultMessage logout(HttpServletRequest request) {
        User user = UserTools.getCurrentUser();
        String id = user.getId();
        ResultMessage resultMessage = new ResultMessage();
        resultMessage.setMessage(ResultMessageEnum.USER_NULL.getMessage());

        if (redisUtil.hasKey(RedisKeyEnum.USER_INFO.getMsg() + id)) {
            redisUtil.del(RedisKeyEnum.USER_INFO.getMsg() + id);
        }
        if (redisUtil.hasKey(RedisKeyEnum.GROUP_INFO.getMsg() + id)) {
            redisUtil.del(RedisKeyEnum.GROUP_INFO.getMsg() + id);
        }
        String sessionId = request.getHeader("Authorization");

        if (redisUtil.hasKey(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg() + sessionId)) {
            redisUtil.del(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg() + sessionId);
        }

        // 登出时移除Threadlocal的内容
        BaseContext.removeCurrentUser();

        resultMessage.setMessage(ResultMessageEnum.LOGOUT_SUCCESS.getMessage());
        return resultMessage;
    }
}
