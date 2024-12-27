package pub.synx.util;

import pub.synx.pojo.db.User;

/**
 * @author SynX TA
 * @version 2024
 **/
public class BaseContext {

    public static ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        threadLocal.set(user);
    }

    public static User getCurrentUser() {
        return threadLocal.get();
    }

    public static void removeCurrentUser() {
        threadLocal.remove();
    }

}

