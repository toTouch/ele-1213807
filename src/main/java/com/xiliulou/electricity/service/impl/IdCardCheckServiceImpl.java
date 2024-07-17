/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.TenantConfig;
import com.xiliulou.electricity.service.IdCardCheckService;
import com.xiliulou.electricity.utils.IdCardValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/17 16:59
 */
@Slf4j
@Service
public class IdCardCheckServiceImpl implements IdCardCheckService {
    
    @Resource
    private TenantConfig tenantConfig;
    
    @Override
    public Triple<Boolean, String, Object> checkIdNumber(Integer tenantId, String idNumber) {
        Map<Integer, Integer> idCardAgeCheck = tenantConfig.getIdCardAgeCheck();
        if (MapUtils.isEmpty(idCardAgeCheck) || !idCardAgeCheck.containsKey(tenantId)) {
            return Triple.of(true,null,null);
        }
        
        if (StringUtils.isBlank(idNumber)) {
            return Triple.of(false,"未填写身份号，暂无法认证",300860);
        }
        
        Integer age = idCardAgeCheck.get(tenantId);
        if (Objects.isNull(age)) {
            age = 18;
        }
        
        if (!IdCardValidator.isOver(idNumber, age)) {
            return Triple.of(false,"未满" + age + "岁，暂无法认证",300861);
        }
    
    
        return Triple.of(true,null,null);
    }
}
