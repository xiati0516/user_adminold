package pub.synx.util;

import pub.synx.pojo.db.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author SynX TA
 * @version 2024
 **/
@Component
@SuppressWarnings("unused")
public class PasswordHelper {
    private final int hashIterations = 3;
    private SecureRandom secureRandom = new SecureRandom();
    private String algorithmName = "MD5";

    /**
     * Encrypt password.
     *
     * @param user the user
     */
    public void encryptPassword(User user) {
        if (StringUtils.isEmpty(user.getSalt())) {
            user.setSalt(generateSalt());
        }
        String newPassword = hashPassword(user.getPassword(), user.getSalt());
        user.setPassword(newPassword);
    }

    public String encryptPassword(String password, String salt) {
        return hashPassword(password, salt);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return bytesToHex(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithmName);
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            for (int i = 1; i < hashIterations; i++) {
                md.reset();
                hashedPassword = md.digest(hashedPassword);
            }
            return bytesToHex(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 密码必须要有字母、数字、特殊字符中的两种且长度为8-20
     *
     * @param password the password
     *
     * @return 0 ->验证失败 1->密码必须要有字母、数字、特殊字符中的两种 2->密码长度需为8-20
     */
    public int verifyPassword(String password) {
        if (StringUtils.isEmpty(password)) return 0;
        int result = 3;

        String regex1 = "^.*[a-zA-Z]+.*$";
        String regex2 = "^.*[0-9]+.*$";
        String regex3 = "^.*[/^/$/.//,;:'!@#%&/*/|/?/+/(/)/[/]/{/}]+.*$";
        String regex4 = "^.{8,20}$";

        boolean english = password.matches(regex1);
        boolean digital = password.matches(regex2);
        boolean character = password.matches(regex3);
        boolean length = password.matches(regex4);

        if (!(english && digital || english && character || digital && character)) {
            result = 1;
        } else {
            if (!length) {
                result = 2;
            }
        }
        return result;
    }

    public String getPassword(Integer length) {
        Random rand = new Random();
        // 按照 1：大写字母 2：小写字母 3：数字 4：下划线，随机选取其中三种
        char[] indexArray = {'1', '2', '3', '4'};
        List<Character> indexes = new ArrayList<>();
        for (char c : indexArray) {
            indexes.add(c);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 4; i >= 2; i--) {
            int index = rand.nextInt(i);
            builder.append(getChar(indexes.get(index)));
            indexes.remove(index);
        }
        int i = length - 3;
        while (i > 0) {
            builder.append(getChar(indexArray[rand.nextInt(4)]));
            i--;
        }
        return builder.toString();
    }

    private static Character getChar(char index) {
        Random rand = new Random();
        char upper = (char) (65 + rand.nextInt(26));
        char lower = (char) (97 + rand.nextInt(26));
        char number = (char) (48 + rand.nextInt(10));
        char underline = '_';
        switch (index) {
            case '1':
                return upper;
            case '2':
                return lower;
            case '3':
                return number;
            case '4':
                return underline;
            default:
                return null;
        }
    }
}