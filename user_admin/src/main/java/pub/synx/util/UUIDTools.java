package pub.synx.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SynX TA
 * @version 2024
 **/
@SuppressWarnings({"unused", "unchecked"})
@Component
@Slf4j
public class UUIDTools {

    private IdWorker idWorker = new IdWorker(0L, 0L);


    /**
     * Get string.
     *
     * @return the string
     */
    public String get() {
        String s = String.valueOf(idWorker.nextId(1));
        log.info("雪花ID:"+s);
        return s;
    }

    public List<Long> get(int num) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            ids.add(idWorker.nextId(num));
        }
        return ids;
    }

    public List<String> getString(int num) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String s = String.valueOf(idWorker.nextId(num));
            log.info("雪花ID:"+s);
            ids.add(s);
        }
        return ids;
    }
}
