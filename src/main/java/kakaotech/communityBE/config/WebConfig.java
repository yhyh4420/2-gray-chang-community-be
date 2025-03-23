package kakaotech.communityBE.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 서버에 저장된 이미지 제공
        registry.addResourceHandler("/uploads/profile/**") // 브라우저에서 접근할 URL
                .addResourceLocations("file:uploads/profile/"); // 실제 저장 경로
        registry.addResourceHandler("/uploads/postImage/**")
                .addResourceLocations("file:uploads/postImage/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5500")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
