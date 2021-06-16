/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */

package com.xiliulou.electricity.filter;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.utils.ResponseUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 *
 */
@Slf4j
@Component
@Order(12)
public class TenantCheckFilter extends GenericFilterBean {

    @Autowired
    TenantService tenantService;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (null == userInfo) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
            log.error("TENANT ERROR! header' teantId not equals user'tenantId,h={},u={}", tenantId, userInfo.getTenantId());
            response.setStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value());
            ResponseUtil.out(response, R.fail("AUTH.0003", "租户信息不匹配"));
            return;
        }

        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            response.setStatus(HttpStatus.OK.value());
            ResponseUtil.out(response, R.fail("AUTH.0004", "租户信息不存在"));
        }

        if (tenant.getExpireTime() < System.currentTimeMillis()) {
            response.setStatus(HttpStatus.OK.value());
            ResponseUtil.out(response, R.fail("AUTH.0005", "租户信息过期"));
        }

        filterChain.doFilter(servletRequest, servletResponse);
        return;
    }
}
