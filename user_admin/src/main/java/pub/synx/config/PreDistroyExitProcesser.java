package pub.synx.config;

import pub.synx.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author SynX TA
 * @version 2024
 **/
@Slf4j
@Component
public class PreDistroyExitProcesser implements DisposableBean {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void destroy() throws Exception {
        log.info("服务关闭中，处理缓存信息");
        List<String> keys = redisUtil.getAllKey();
        for (String key : keys) {
            redisUtil.del(key);
        }
    }
}
