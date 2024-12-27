package pub.synx.aop.anno;

import java.lang.annotation.*;

/**
 * @author SynX TA
 * @version 2024
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}