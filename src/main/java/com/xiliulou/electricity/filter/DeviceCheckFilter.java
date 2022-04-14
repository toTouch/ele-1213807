package com.xiliulou.electricity.filter;

import com.xiliulou.electricity.entity.TenantAppInfo;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

/**
 * @author: Mr Hu
 * @Date: 2022/04/12 14:56
 * @Description:
 */
@Slf4j
@Component()
@Order(100)
public class DeviceCheckFilter extends ApiFilter{
	static final String CUPBOARD_URL = "/outer/checkDevice";

	public DeviceCheckFilter() {
		super(new AntPathRequestMatcher(CUPBOARD_URL, Request.HttpMethod.POST.name()));
	}

	@Override
	public TenantAppInfo getTenantAppInfo(String appId) {

		TenantAppInfo tenantAppInfo=new TenantAppInfo();
		tenantAppInfo.setAppid(appId);
		tenantAppInfo.setCreateTime(System.currentTimeMillis());
		tenantAppInfo.setStatus(TenantAppInfo.TYPE_NORMAL);
		return tenantAppInfo;
	}
}
