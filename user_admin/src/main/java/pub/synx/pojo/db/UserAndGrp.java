package pub.synx.pojo.db;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SynX TA
 * @version 2024
 **/
@Data
@NoArgsConstructor
@TableName("tbl_info_and_group")
public class UserAndGrp extends BasePojo{

    //用户Id
    private String userId;

    //分组Id
    private String groupId;

}
