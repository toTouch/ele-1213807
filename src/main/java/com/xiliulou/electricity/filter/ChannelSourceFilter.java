/**
 * Create date: 2024/7/30
 */

package com.xiliulou.electricity.filter;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.HeaderConstant;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/30 13:38
 */
@Slf4j
@Component
@Order(14)
public class ChannelSourceFilter implements Filter {
    
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String channel = request.getHeader(HeaderConstant.CHANNEL_SOURCE);
        
        if (StrUtil.isNotBlank(channel)) {
            ChannelSourceContextHolder.set(channel);
        } else {
            ChannelSourceContextHolder.set(ChannelEnum.WECHAT.getCode());
        }
        
        try {
            filterChain.doFilter(request, servletResponse);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
