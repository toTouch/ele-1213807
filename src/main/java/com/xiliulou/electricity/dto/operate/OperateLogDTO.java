package com.xiliulou.electricity.dto.operate;


import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ServletUtils;
import com.xiliulou.electricity.utils.WebUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: This class is OperateLogDTO!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/26
 **/
@Data
@Slf4j
public class OperateLogDTO {

    private Long uid;

    private Long tenantId;

    private String username;

    private String ip;

    private Long operateTime;

    private String uri;

    private String method;

    private Map<String,Object> oldValue;

    private Map<String,Object> newValue;

    public static OperateLogDTO ofWebRequest(){
        OperateLogDTO dto = new OperateLogDTO();
        HttpServletRequest request = ServletUtils.getRequest();
        dto.setMethod(request.getMethod());
        dto.setUri(request.getServletPath());
        String ip = WebUtils.getIP(request);
        if (StrUtil.isNotBlank(ip)){
            dto.setIp(ip);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!Objects.isNull(tenantId)){
            dto.setTenantId(tenantId.longValue());
        }
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (!Objects.isNull(userInfo)){
            dto.setUid(userInfo.getUid());
            dto.setUsername(userInfo.getUsername());
        }
        return dto;
    }
    
    public static OperateLogDTO ofStatic(){
        OperateLogDTO dto = new OperateLogDTO();
        dto.setMethod(COMMAND_METHOD);
        dto.setUri(COMMAND_URL);
        return dto;
    }
    
    private static String COMMAND_METHOD="POST";
    private static String COMMAND_URL="/admin/electricityCabinet/command";
}
