package pub.synx.pojo.params;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
@Data
public class ResultMessage {
    //默认返回代码
    private Integer status = 1;
    //默认返回信息
    private String message;
    //默认返回数据
    private Object Data;
}
