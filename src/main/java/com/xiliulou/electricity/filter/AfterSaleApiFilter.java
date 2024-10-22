package com.xiliulou.electricity.filter;

import com.xiliulou.electricity.entity.TenantAppInfo;
import feign.Request;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Component()
@Order(300)
public class AfterSaleApiFilter extends ApiOuterFilter {
    
    static final String AFTER_SALE_API_URL = "/outer/afterSale/**";
    
    private static final String APP_ID = "afterSale";
    
    private static final String APP_SECRET = "asegfHVZfFLTa0buIAUvU3VoidGdlERz+lf1HqVfR4s=";
    
    public AfterSaleApiFilter() {
        super(new AntPathRequestMatcher(AFTER_SALE_API_URL, Request.HttpMethod.POST.name()));
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
