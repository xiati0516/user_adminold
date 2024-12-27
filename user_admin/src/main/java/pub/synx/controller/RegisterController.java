package pub.synx.controller;

import pub.synx.exception.DataProcessException;
import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private CommonService commonService;

    /**
     * 账号密码注册
     *
     * @param user
     * @return
     * @throws DataProcessException
     */
    @PostMapping("/account")
    public ResultMessage addUserByAccount(@RequestBody User user) {
        return commonService.addUserByAccount(user);
    }
}
