package pub.synx.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SynX TA
 * @version 2024
 **/
@Data
@NoArgsConstructor
@TableName("tbl_group")
public class Group extends BasePojo {

    /**
     * Id
     */
    @TableId
    private String id;
    /*
     * 名称
     */
    private String name;

    //分组管理员Ids
    private String managerIds;


    public Group(String id, String name, String managerIds, String description, String extension, String creatorId,Long createdTime, Long lastUpdateTime, String lastOperatorId) {
        super(createdTime, lastUpdateTime, creatorId, lastOperatorId, description, extension);
        this.id = id;
        this.name = name;
        this.managerIds = managerIds;
    }
}
