package com.xiliulou.electricity.filter;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.utils.ThirdMallConfigHolder;
import com.xiliulou.security.utils.ResponseUtil;
import com.xiliulou.thirdmall.constant.meituan.virtualtrade.VirtualTradeConstant;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.util.meituan.MeiTuanRiderMallUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/12 13:49:11
 */

@Slf4j
@Component("thirdMallCheckFilter")
@Order(15)
public class ThirdMallCheckFilter implements Filter {
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    private RequestMatcher requiresAuthenticationRequestMatcher;
    
    
    public ThirdMallCheckFilter() {
        this.requiresAuthenticationRequestMatcher = new AntPathRequestMatcher("/outer/batteryMemberCard/**");
    }
    
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        if (!this.requiresAuthentication(httpServletRequest, response)) {
            filterChain.doFilter(httpServletRequest, response);
            return;
        }
        
        String appId = httpServletRequest.getAttribute(VirtualTradeConstant.APP_ID).toString();
        String appKey = httpServletRequest.getAttribute(VirtualTradeConstant.TIMESTAMP).toString();
        String sign = httpServletRequest.getAttribute(VirtualTradeConstant.SIGN).toString();
        
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(VirtualTradeConstant.TIMESTAMP, httpServletRequest.getAttribute(VirtualTradeConstant.TIMESTAMP));
        paramMap.put(VirtualTradeConstant.APP_ID, appId);
        paramMap.put(VirtualTradeConstant.APP_KEY, appKey);
        paramMap.put(VirtualTradeConstant.ACCOUNT, httpServletRequest.getAttribute(VirtualTradeConstant.ACCOUNT));
        paramMap.put(VirtualTradeConstant.PROVIDER_SKU_ID, httpServletRequest.getAttribute(VirtualTradeConstant.PROVIDER_SKU_ID));
    
        log.info("ThirdMall request param: {}", paramMap);
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByConfigFromCache(MeiTuanRiderMallConfig.builder().appId(appId).appKey(appKey).build());
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            log.error("ThirdMall request error! meiTuanRiderMallConfig is null, appId={}, appKey={}", appId, appKey);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseUtil.out(response, R.fail(VirtualTradeStatusEnum.FAIL_APP_CONFIG.getDesc()));
        }
        
        Boolean checkSign = MeiTuanRiderMallUtil.checkSign(paramMap, meiTuanRiderMallConfig.getSecret(), sign);
        if (!checkSign) {
            log.error("ThirdMall request error! checkSign fail, appId={}, appKey={}, sign={}", appId, appKey, sign);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseUtil.out(response, R.fail(VirtualTradeStatusEnum.FAIL_CHECK_SIGN.getDesc()));
        }
        
        ThirdMallConfigHolder.setTenantId(meiTuanRiderMallConfig.getTenantId());
    
        log.info("ThirdMall tenantId={}", ThirdMallConfigHolder.getTenantId());
        
        try {
            filterChain.doFilter(servletRequest, response);
        } finally {
            ThirdMallConfigHolder.clear();
        }
    }
    
    private boolean requiresAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse response) {
        return this.requiresAuthenticationRequestMatcher.matches(httpServletRequest);
    }
    
}
