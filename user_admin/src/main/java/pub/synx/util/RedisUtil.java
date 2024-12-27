package pub.synx.util;

import pub.synx.enums.EndVersionEnum;
import pub.synx.enums.RedisKeyEnum;
import pub.synx.pojo.db.Group;
import pub.synx.pojo.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author SynX TA
 * @version 2024
 **/
@Component
public final class RedisUtil {


    @Autowired
    @Qualifier("redisStandalone")
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }


    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */

    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }


    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }


    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }


    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }


    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }


    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }


    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 获取set缓存的长度
     *
     * @param key 键
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */

    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */

    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取所有键
     *
     * @return
     */
    public List<String> getAllKey() {
        //key("*") 获取所有键
        Set<String> keys = redisTemplate.keys("*");
        List<String> keyList = new ArrayList<>();
        for (String s : keys) {
            if (s.startsWith(RedisKeyEnum.USER_INFO.getMsg()) || s.startsWith(RedisKeyEnum.GROUP_INFO.getMsg())
                    || s.startsWith(RedisKeyEnum.SESSION_REDIS_PREFIX.getMsg())) {
                keyList.add(s);
            }
        }
        return keyList;
    }

    /**
     * 存储用户基本信息
     *
     * @param map 用户基本信息Map
     * @return
     * @throws Exception
     */
    public Map storeUserInfo(Map map) {
        User user = (User) map.get("userInfo");
        Long lastUpdateTime = user.getLastUpdateTime();
        Long createdTime = user.getCreatedTime();
        Long lastTime = lastUpdateTime == null ? createdTime : lastUpdateTime;
        String userId = user.getId();
        map.put(RedisKeyEnum.VERSION.getMsg(), lastTime);

        if (this.hasKey(RedisKeyEnum.USER_INFO.getMsg() + userId)) {
            Map getMap = (Map) this.hmget(RedisKeyEnum.USER_INFO.getMsg() + userId);
            Long time = (Long) getMap.get(RedisKeyEnum.VERSION.getMsg());

            //必须要>=,考虑用户的基本信息不变，但是用户解绑了分组，但用户的最终修改时间不变
            if (lastTime > time) {
                this.hmset(RedisKeyEnum.USER_INFO.getMsg() + userId, map, 60 * 30);
            } else {
                map = getMap;
            }
        } else {
            this.hmset(RedisKeyEnum.USER_INFO.getMsg() + userId, map, 60 * 30);
        }
        map.remove(RedisKeyEnum.VERSION.getMsg());

        return map;
    }

    /**
     * 存储分组信息
     *
     * @param map
     * @return
     */
    public Map storeGrpInfo(Map map) {
        List<Group> grplist = (ArrayList<Group>) map.get("grpInfo");
        Long version = 0L;
        List<String> grpIds = new ArrayList<>();
        for (Group group : grplist) {
            Long lastTime = group.getLastUpdateTime() == null ? group.getCreatedTime() : group.getLastUpdateTime();
            version += lastTime;
            grpIds.add(group.getId());
        }
        grpIds.sort(Comparator.comparing(String::hashCode));
        String userId = (String) map.get("userId");
        map.remove(userId);
        map.put(RedisKeyEnum.VERSION.getMsg(), version.toString());

        if (this.hasKey(RedisKeyEnum.GROUP_INFO.getMsg() + userId)) {
            Map getMap = (Map) this.hmget(RedisKeyEnum.GROUP_INFO.getMsg() + userId);
            String versionStr = (String) getMap.get(RedisKeyEnum.VERSION.getMsg());
            Long version1 = Long.parseLong(versionStr);
            List<Group> grplist1 = (ArrayList<Group>) getMap.get("grpInfo");
            List<String> grpIds1 = new ArrayList<>();
            for (Group group : grplist1) {
                grpIds1.add(group.getId());
            }
            grpIds1.sort(Comparator.comparing(String::hashCode));
            if (!grpIds.toString().equals(grpIds1.toString()) || version != version1) {
                this.hmset(RedisKeyEnum.GROUP_INFO.getMsg() + userId, map, 60 * 30);
            } else {
                map = getMap;
            }
        } else {
            this.hmset(RedisKeyEnum.GROUP_INFO.getMsg() + userId, map, 60 * 30);
        }
        map.remove(RedisKeyEnum.VERSION.getMsg());
        return map;
    }

    /**
     * 存储数据
     *
     * @param redisMap
     * @throws IllegalAccessException
     */
    public Map storeData(Map redisMap) throws Exception {
        //将用户数据放入缓存,设置30min
        Map resMap = new HashMap<>();
        String userId = (String) redisMap.get("id");

        User user = (User) redisMap.get("userInfo");
        List grplist = (ArrayList<Group>) redisMap.get("grpInfo");

        if (user != null) {
            Map map = new HashMap<>();
            map.put("userInfo", user);
            map = storeUserInfo(map);
            resMap.put("userInfo", map.get("userInfo"));
        }

        if (grplist != null) {
            Map map = new HashMap<>();
            map.put("grpInfo", grplist);
            map.put("userId", userId);
            map = storeGrpInfo(map);
            resMap.put("grpInfo", map.get("grpInfo"));
        }
        //返回用户信息
        return resMap;
    }

    /**
     * 修改数据对Redis操作
     *
     * @param redisMap
     * @throws Exception
     */
    public void updateData(Map redisMap) throws Exception {
        //将用户数据放入缓存,设置30min
        Map resMap = new HashMap<>();
        String userId = (String) redisMap.get("id");
        User user = (User) redisMap.get("userInfo");
        List grplist = (ArrayList<Group>) redisMap.get("grpInfo");

        if (user != null) {
            Map map = new HashMap<>();
            map.put("userInfo", user);
            map = updateUserInfo(map);
            resMap.put("userInfo", map);
            if (user.getLastUpdateTime().equals(EndVersionEnum.END_VERSION)) {
                Map tempMap = new HashMap<>();
                tempMap.put(RedisKeyEnum.VERSION.getMsg(), EndVersionEnum.END_VERSION);
                this.hmset(RedisKeyEnum.GROUP_INFO.getMsg() + userId, map, 60);
            }
        }

        if (grplist != null) {
            Map map = new HashMap<>();
            map.put("userId", userId);
            map.put("grpInfo", grplist);
            resMap.put("grpInfo", storeGrpInfo(map));
        }

        if (redisMap.get("id") != null) {
            redisMap.remove("id");
        }
    }

    /**
     * 修改用户信息
     *
     * @param map
     */
    public Map updateUserInfo(Map map) {

        //如果是普通修改，设置30分钟，如果是删除则设置1分钟
        User user = (User) map.get("userInfo");
        String userId = user.getId();
        Long lastUpdateTime = user.getLastUpdateTime();
        Long createdTime = user.getCreatedTime();
        int overTime = EndVersionEnum.END_VERSION.equals(lastUpdateTime) ? 1 : 30;
        Long lastTime = lastUpdateTime == null ? createdTime : lastUpdateTime;
        map.put(RedisKeyEnum.VERSION.getMsg(), lastTime);
        if (this.hasKey(RedisKeyEnum.USER_INFO.getMsg() + userId)) {
            Map getMap = (Map) this.hmget(RedisKeyEnum.USER_INFO.getMsg() + userId);
            Long time = (Long) getMap.get(RedisKeyEnum.VERSION.getMsg());
            if (lastTime > time) {
                //this.del(realUser.getId() + RedisKeyEnum.INFO.getMsg());
                this.hmset(RedisKeyEnum.USER_INFO.getMsg() + userId, map, 60 * overTime);
            } else {
                map = getMap;
            }
        } else {
            this.hmset(RedisKeyEnum.USER_INFO.getMsg() + userId, map, 60 * overTime);
        }

        //去除版本号
        map.remove(RedisKeyEnum.VERSION.getMsg());

        return map;
    }


}
