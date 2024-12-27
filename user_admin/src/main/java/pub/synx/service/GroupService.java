package pub.synx.service;

import pub.synx.pojo.db.Group;
import pub.synx.pojo.params.ResultMessage;
import com.alibaba.fastjson.JSONObject;

/**
 * @author SynX TA
 * @version 2024
 **/
public interface GroupService {

    ResultMessage addGroup(JSONObject jsonObject);

    ResultMessage updateGroup(Group group) throws Exception;

    ResultMessage deleteGroup(JSONObject jsonObject) throws Exception;

    ResultMessage deleteGroupAndUser(JSONObject jsonObject) throws Exception;

    ResultMessage queryGroup(Group group);

    ResultMessage getUserList(String id);

    ResultMessage bindUserList(String id, JSONObject jsonObject) throws Exception;

    ResultMessage unbindUserList(String id, JSONObject jsonObject) throws Exception;

}
