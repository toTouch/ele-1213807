package com.xiliulou.electricity.service.impl.idempotent;

import com.xiliulou.common.sentinel.entity.IdempotentInfo;
import com.xiliulou.common.sentinel.service.IdempotentInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class IdempotentInfoServiceImpl implements IdempotentInfoService {

    @Override
    public IdempotentInfo getIdempotentInfo() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return null;
        }

        return IdempotentInfo.builder().tenantId(user.getTenantId()).uid(user.getUid()).build();
    }
}
