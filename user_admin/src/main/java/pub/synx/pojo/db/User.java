package pub.synx.pojo.db;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author SynX TA
 * @version 2024
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("tbl_info")
public class User extends BasePojo{

    private static final long serialVersionUID = 1L;

    /**
     * Id
     */
    private String id;
    /*
     * 名称
     */
    private String name;

    // 账号
    private String account;

    //密码
    private String password;

    //性别， 0表示男，1表示女
    private Integer gender;

    //用户邮箱
    private String mail;

    //用户状态
    private Integer status;

    //用户code
    private String code;

    //身份类型
    private String identity;

    //盐
    private String salt;

    public User(String id, String account, String name, Integer gender, String description,
                String mail, Integer status,String code,String identity, String extension, String creatorId, Long createdTime,
                Long lastUpdateTime, String lastOperatorId){
        this.id = id;
        this.name = name;
        this.account = account;
        this.gender =  gender;
        this.description = description;
        this.mail = mail;
        this.code = code;
        this.identity = identity;
        this.extension = extension;
        this.createdTime = createdTime;
        this.creatorId = creatorId;
        this.status = status;
        this.lastOperatorId = lastOperatorId;
        this.lastUpdateTime = lastUpdateTime;
    }

}
