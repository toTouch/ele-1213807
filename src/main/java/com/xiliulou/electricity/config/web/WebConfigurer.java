package com.xiliulou.electricity.config.web;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: eclair
 * @Date: 2019/11/4 11:42
 * @Description:
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

	}
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
		List<MediaType> mediaTypes = new ArrayList<>(16);
		mediaTypes.add(MediaType.APPLICATION_ATOM_XML);
		mediaTypes.add(MediaType.APPLICATION_CBOR);
		mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		mediaTypes.add(MediaType.APPLICATION_JSON);
		mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		converter.setSupportedMediaTypes(mediaTypes);
		converters.add(converter);
	}
}
