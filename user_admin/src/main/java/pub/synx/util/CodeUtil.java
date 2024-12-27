package pub.synx.util;

/**
 * @author SynX TA
 * @version 2024
 **/
public class CodeUtil {
    /**
     * 生成随机验证码
     * @return String
     */
    public static Integer getRandNum() {
        int randNum = 100000 + (int)(Math.random() * ((999999 - 100000) + 1));
        return randNum;
    }
}
