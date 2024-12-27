package pub.synx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author SynX TA
 * @version 2024
 **/
@AllArgsConstructor
@Getter
public enum RedisKeyEnum {
    //用户基本信息
    USER_INFO("user:info:"),
    //用户分组
    GROUP_INFO("user:grp:"),
    //用户版本
    VERSION("version"),
    //session前缀
    SESSION_REDIS_PREFIX("user:session:");

    private String msg;


}
