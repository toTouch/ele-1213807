package com.xiliulou.electricity.filter;

import com.xiliulou.electricity.entity.TenantAppInfo;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

/**
 * @author zgw
 * @date 2023/2/16 10:49
 * @mood
 */
@Slf4j
@Component()
@Order(200)
public class Jt808ElectricityCarFilterApi extends ApiOuterFilter {
    
    static final String CUPBOARD_URL = "/outer/jt808/**";
    
    private final String app_Id = "jt808";
    
    private final String APP_SECRET = "fgfifHVZfFLTa0buIAUvU3VoidGdlERz+lf1HqVfR4s=";
    
    public Jt808ElectricityCarFilterApi() {
        super(new AntPathRequestMatcher(CUPBOARD_URL, Request.HttpMethod.POST.name()));
    }
    
    @Override
    public TenantAppInfo getTenantAppInfo(String appId) {
        TenantAppInfo tenantAppInfo = new TenantAppInfo();
        tenantAppInfo.setAppid(app_Id);
        tenantAppInfo.setAppsecert(APP_SECRET);
        tenantAppInfo.setTenantId(-1);
        return tenantAppInfo;
    }
}
