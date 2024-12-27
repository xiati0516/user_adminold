package pub.synx.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author SynX TA
 * @version 2024
 **/
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserInterceptor userInterceptor;

    public WebMvcConfig(UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogCostInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(userInterceptor).addPathPatterns("/**");
    }
}
