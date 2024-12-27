package pub.synx.config;

import pub.synx.util.UserTools;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = "";
        try {
            userId = UserTools.getCurrentUserId();
        } catch (Exception e) {
        }
        this.setFieldValByName("createdTime", new Date().getTime(), metaObject);
        this.setFieldValByName("creatorId", userId, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = "";
        try {
            userId = UserTools.getCurrentUserId();
        } catch (Exception e) {
        }
        this.setFieldValByName("lastUpdateTime", new Date().getTime(), metaObject);
        this.setFieldValByName("lastOperatorId", userId, metaObject);
    }
}
