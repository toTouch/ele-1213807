

package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import org.apache.commons.lang3.tuple.Triple;

/**
 * description: 身份证校验
 *
 * @author caobotao.cbt
 * @date 2024/7/17 16:57
 */
public interface IdCardCheckService {
    
    
    /**
     * 身份证号码校验
     *
     * @param tenantId
     * @param idNumber
     * @author caobotao.cbt
     * @date 2024/7/17 16:59
     * @return 校验失败的错误信息
     */
    Triple<Boolean, String, Object> checkIdNumber(Integer tenantId, String idNumber);
    
}