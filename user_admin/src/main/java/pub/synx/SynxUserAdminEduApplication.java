package pub.synx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author SynX TA
 * @version 2024
 **/
@SpringBootApplication(scanBasePackages = {"pub.synx"})
@MapperScan("pub.synx.mapper")
@EnableTransactionManagement
@EnableCaching
public class SynxUserAdminEduApplication {
    public static void main(String[] args) {
        SpringApplication.run(SynxUserAdminEduApplication.class, args);
    }
}
