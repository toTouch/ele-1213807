package com.xiliulou.electricity.filter;

import com.xiliulou.electricity.entity.TenantAppInfo;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

/**
 * @author : eclair
 * @date : 2023/3/6 13:31
 */
@Slf4j
@Component()
@Order(200)
public class BatteryApiFilter extends ApiOuterFilter{
    
    static final String BATTERY_API_URL = "/outer/battery/**";
    
    private static final String APP_ID = "battery";
    
    private static final String APP_SECRET = "asegfHVZfFLTa0buIAUvU3VoidGdlERz+lf1HqVfR4s=";
    
    public BatteryApiFilter() {
        super(new AntPathRequestMatcher(BATTERY_API_URL, Request.HttpMethod.POST.name()));
    }
    
    @Override
    public TenantAppInfo getTenantAppInfo(String appId) {
        TenantAppInfo tenantAppInfo = new TenantAppInfo();
        tenantAppInfo.setAppid(APP_ID);
        tenantAppInfo.setAppsecert(APP_SECRET);
        tenantAppInfo.setTenantId(-1);
        return tenantAppInfo;
    }
}
