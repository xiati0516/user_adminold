package pub.synx.aop.anno;

import java.lang.annotation.*;

/**
 * @author SynX TA
 * @version 2024
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireGroup {
    /**
     * 返回访问注解方法所需的用户组代码数组。
     *
     * @return 用户组代码数组
     */
    String[] value();
}