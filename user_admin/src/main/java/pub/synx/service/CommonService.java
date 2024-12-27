package pub.synx.service;

import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;

/**
 * @author SynX TA
 * @version 2024
 **/
public interface CommonService {
    /**
     * 账号密码注册
     * @param user
     * @return
     */
    ResultMessage addUserByAccount(User user);

    /**
     * 账号密码登录
     * @param user
     * @return
     */
    ResultMessage loginByAccount(User user) throws Exception;
}
