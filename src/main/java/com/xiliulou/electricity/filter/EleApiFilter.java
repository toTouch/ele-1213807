package com.xiliulou.electricity.filter;

import com.xiliulou.electricity.entity.TenantAppInfo;
import com.xiliulou.electricity.service.TenantAppInfoService;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * @author: Miss.Li
 * @Date: 2021/11/12 14:56
 * @Description:
 */
@Slf4j
@Component()
@Order(101)
public class EleApiFilter extends ApiFilter{
	static final String CUPBOARD_URL = "/outer/api/**";
	@Autowired
	TenantAppInfoService tenantAppInfoService;

	public EleApiFilter() {
		super(new AntPathRequestMatcher(CUPBOARD_URL, Request.HttpMethod.POST.name()));
	}

	@Override
	public TenantAppInfo getTenantAppInfo(String appId) {
		return tenantAppInfoService.queryByAppId(appId, TenantAppInfo.EXCHANGE_NORMAL);
	}
}
