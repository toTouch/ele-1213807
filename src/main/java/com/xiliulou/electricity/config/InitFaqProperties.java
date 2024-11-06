package com.xiliulou.electricity.config;

import com.xiliulou.electricity.constant.CommonConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * @author HeYafeng
 * @description 初始化faq
 * @date 2024/10/28 21:17:46
 */
@Data
@Configuration
@PropertySource(value = CommonConstant.INIT_FQA_URL)
@ConfigurationProperties(prefix = "faq")
public class InitFaqProperties {
    
    private List<Category> category;
    
    @Data
    public static class Category {
        
        private String type;
        
        private List<Problem> problem;
        
        @Data
        public static class Problem {
            
            private String title;
            
            private List<String> answer;
        }
    }
}
