package pub.synx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author SynX TA
 * @version 2024
 **/
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
//返回状态代码
public enum ResultStatusEnum {
    //0,成功，结果正常
    SUCCESS(1,"操作执行成功"),
    //1, 操作执行失败
    FAILURE(0,"操作执行失败");

    private int code;

    private String message;
}
