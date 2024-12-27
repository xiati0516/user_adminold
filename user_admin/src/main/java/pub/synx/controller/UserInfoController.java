package pub.synx.controller;

import pub.synx.aop.anno.RequireLogin;
import pub.synx.enums.ResultMessageEnum;
import pub.synx.exception.DataProcessException;
import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author SynX TA
 * @version 2024
 **/
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserService userService;

    /**
     * 添加用户
     *
     * @param jsonObject
     * @return
     */
    @RequireLogin
    @PostMapping
    public ResultMessage addUser(@RequestBody JSONObject jsonObject) {
        return userService.addUser(jsonObject);
    }

    /**
     * 修改用户
     *
     * @param user
     * @return
     */
    @RequireLogin
    @PutMapping
    public ResultMessage updateUser(@RequestBody User user) throws Exception {
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     *
     * @param jsonObject
     * @return
     */
    @RequireLogin
    @DeleteMapping
    public ResultMessage deleteUser(@RequestBody JSONObject jsonObject) throws Exception {
        return userService.deleteUser(jsonObject);
    }

    /**
     * 查询用户基本信息
     */
    @RequireLogin
    @GetMapping("/brief")
    public ResultMessage queryUser(@RequestBody User user) {
        return userService.queryUser(user);
    }


    /**
     * 查询用户全部信息
     */
    @RequireLogin
    @GetMapping
    public ResultMessage getWholeUser(@RequestBody Map<String, Object> map) throws Exception {
        if (map.get("id") == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        return userService.getWholeUser((String) map.get("id"));
    }

    /**
     * 绑定分组
     *
     * @param jsonObject 分组Id列表 ids
     * @return
     */
    @RequireLogin
    @PutMapping("/groups")
    public ResultMessage bindGroupList(@RequestBody JSONObject jsonObject) throws Exception {
        String id = jsonObject.getString("id");
        return userService.bindGroupList(id, jsonObject);
    }

    /**
     * 解绑分组
     *
     * @param jsonObject 分组Id列表 ids
     * @return
     */
    @RequireLogin
    @DeleteMapping("/groups")
    public ResultMessage unbindGroupList(@RequestBody JSONObject jsonObject) throws Exception {
        String id = jsonObject.getString("id");
        return userService.unbindGroupList(id, jsonObject);
    }
}
