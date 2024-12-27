package pub.synx.service;


import pub.synx.pojo.db.User;
import pub.synx.pojo.params.ResultMessage;
import com.alibaba.fastjson.JSONObject;

/**
 * @author SynX TA
 * @version 2024
 **/
public interface UserService {
    ResultMessage addUser(JSONObject jsonObject);

    ResultMessage updateUser(User user) throws Exception;

    ResultMessage deleteUser(JSONObject jsonObject) throws Exception;

    ResultMessage queryUser(User user);

    ResultMessage getWholeUser(String id) throws Exception;

    ResultMessage bindGroupList(String id, JSONObject jsonObject) throws Exception;

    ResultMessage unbindGroupList(String id, JSONObject jsonObject) throws Exception;
}