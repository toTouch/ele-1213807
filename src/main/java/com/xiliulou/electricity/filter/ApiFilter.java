package com.xiliulou.electricity.filter;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TenantAppInfo;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SignUtils;
import com.xiliulou.security.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2021/7/22 11:08 上午
 */
@Slf4j
public abstract class ApiFilter implements Filter {

    private RequestMatcher requiresAuthenticationRequestMatcher;


    public ApiFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        this.requiresAuthenticationRequestMatcher = requiresAuthenticationRequestMatcher;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!this.requiresAuthentication(httpServletRequest, response)) {
            filterChain.doFilter(httpServletRequest, response);
            return;
        }

        try {
            String body = IOUtils.toString(httpServletRequest.getReader());
            if (StrUtil.isEmpty(body)) {
                ResponseUtil.out(response, R.fail("AUTH.1002", "参数错误"));
                return;
            }

            ApiRequestQuery apiRequestQuery = JsonUtil.fromJson(body, ApiRequestQuery.class);
            if (Objects.isNull(apiRequestQuery)) {
                ResponseUtil.out(response, R.fail("AUTH.1002", "参数错误"));
                return;
            }

//            Pair<Boolean, String> checkParamsResult = checkRequestParamsIsLegal(apiRequestQuery);
//            if (!checkParamsResult.getLeft()) {
//                ResponseUtil.out(response, R.fail("AUTH.1002", checkParamsResult.getRight()));
//                return;
//            }

            TenantAppInfo tenantAppInfo = getTenantAppInfo(apiRequestQuery.getAppId());

            if (Objects.isNull(tenantAppInfo)) {
                log.error("APP FILTER ERROR! not found tenantAppInfo! appId={}", apiRequestQuery.getAppId());
                ResponseUtil.out(response, R.fail("AUTH.1004", "签名失败!"));
                return;
            }

            if (tenantAppInfo.getStatus().equals(TenantAppInfo.TYPE_STOP)) {
                ResponseUtil.out(response, R.fail("AUTH.1003", "appId已停用!"));
                return;
            }

//            String signature = SignUtils.getSignature(apiRequestQuery, tenantAppInfo.getAppsecert());
//            if (!apiRequestQuery.getSign().equals(signature)) {
//                ResponseUtil.out(response, R.fail("AUTH.1004", "签名失败!"));
//                return;
//            }

            TenantContextHolder.setTenantId(tenantAppInfo.getTenantId());
            filterChain.doFilter(servletRequest, servletResponse);


        } catch (Exception e) {
            log.error(" FILTER ERROR! ", e);
            ResponseUtil.out(response, R.fail("AUTH.1001", "系统错误"));
        }


    }

    private boolean requiresAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse response) {
        return this.requiresAuthenticationRequestMatcher.matches(httpServletRequest);
    }

    /**
     * 获取相应的租户app信息
     *
     * @param appId
     * @return
     */
    public abstract TenantAppInfo getTenantAppInfo(String appId);


    protected Pair<Boolean, String> checkRequestParamsIsLegal(ApiRequestQuery apiRequestQuery) {
        if (StrUtil.isEmpty(apiRequestQuery.getRequestId()) || apiRequestQuery.getRequestId().length() > 40) {
            return Pair.of(false, "requestId不能为空或者长度超过40");
        }

        if (StrUtil.isEmpty(apiRequestQuery.getCommand())) {
            return Pair.of(false, "command不能为空");
        }

        if (StrUtil.isEmpty(apiRequestQuery.getAppId())) {
            return Pair.of(false, "appId不能为空！");
        }

        if (StrUtil.isEmpty(apiRequestQuery.getSign())) {
            return Pair.of(false, "签名不能为空");
        }

        if (StrUtil.isEmpty(apiRequestQuery.getData())) {
            return Pair.of(false, "data不能为空");
        }

        if (StrUtil.isEmpty(apiRequestQuery.getVersion())) {
            return Pair.of(false, "version不能为空！");
        }

        if (Objects.isNull(apiRequestQuery.getRequestTime())) {
            return Pair.of(false, "requestTime不能为空！");
        }

        if ((apiRequestQuery.getRequestTime() - System.currentTimeMillis()) > 2000) {
            return Pair.of(false, "requestTime不合法");
        }

        if ((System.currentTimeMillis() - apiRequestQuery.getRequestTime()) > 5 * 1000 * 60L) {
            return Pair.of(false, "请求已经过期！");
        }

        return Pair.of(true, null);
    }
}
