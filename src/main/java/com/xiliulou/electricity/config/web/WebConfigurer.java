package com.xiliulou.electricity.config.web;

import com.xiliulou.electricity.interceptor.AdminSupperInterceptor;
import com.xiliulou.electricity.interceptor.EnterprisePackageInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


/**
 * @Auther: eclair
 * @Date: 2019/11/4 11:42
 * @Description:
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {

	@Resource
	private AdminSupperInterceptor adminSupperInterceptor;
	
	@Resource
	private EnterprisePackageInterceptor enterprisePackageInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(adminSupperInterceptor).addPathPatterns("/admin/inner/super/**");
		registry.addInterceptor(enterprisePackageInterceptor).addPathPatterns("/user/cloudBeanUse/**", "/user/enterprise/**", "/user/enterpriseInfo/**", "/user/cloudBean/**").excludePathPatterns("/user/enterprise/addUserByScanNew", "/user/enterprise/addUserByScanNewCheck");
	}

}
