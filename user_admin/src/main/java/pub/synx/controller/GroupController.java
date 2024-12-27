package pub.synx.controller;

import pub.synx.aop.anno.RequireLogin;
import pub.synx.enums.ResultMessageEnum;
import pub.synx.exception.DataProcessException;
import pub.synx.pojo.db.Group;
import pub.synx.pojo.params.ResultMessage;
import pub.synx.service.GroupService;
import pub.synx.service.UserService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    /**
     * 添加分组
     *
     * @param jsonObject 分组列表 ids
     * @return
     */
    @RequireLogin
    @PostMapping
    public ResultMessage addGroup(@RequestBody JSONObject jsonObject) {
        return groupService.addGroup(jsonObject);
    }

    /**
     * 修改分组
     *
     * @param group
     * @return
     */
    @RequireLogin
    @PutMapping
    public ResultMessage updateGroup(@RequestBody Group group) throws Exception {
        return groupService.updateGroup(group);
    }

    /**
     * 删除分组
     *
     * @param jsonObject
     * @return
     */
    @RequireLogin
    @DeleteMapping
    public ResultMessage deleteGroup(@RequestBody JSONObject jsonObject) throws Exception {
        return groupService.deleteGroup(jsonObject);
    }

    /**
     * 查询分组
     */
    @RequireLogin
    @GetMapping
    public ResultMessage queryGroup(@RequestBody Group group) {
        return groupService.queryGroup(group);
    }

    /**
     * 绑定用户列表
     *
     * @param jsonObject 用户Id列表 ids
     * @return
     */
    @RequireLogin
    @PutMapping("/users")
    public ResultMessage bindUserList(@RequestBody JSONObject jsonObject) throws Exception {
        String id = jsonObject.getString("id");
        return groupService.bindUserList(id, jsonObject);
    }


    /**
     * 解绑用户列表
     *
     * @param jsonObject 用户Id列表 ids
     * @return
     */
    @RequireLogin
    @DeleteMapping("/users")
    public ResultMessage unbindUserList(@RequestBody JSONObject jsonObject) throws Exception {
        String id = jsonObject.getString("id");
        return groupService.unbindUserList(id, jsonObject);
    }

    /**
     * 获取分组的用户列表
     *
     * @return
     */
    @RequireLogin
    @GetMapping("/users")
    public ResultMessage getUserList(@RequestBody Map<String, Object> map) {
        if (map.get("id") == null) {
            throw new DataProcessException(ResultMessageEnum.ID_NULL.getMessage());
        }
        return groupService.getUserList((String) map.get("id"));
    }
}
