package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleUserOperateHistoryQueryModel;
import com.xiliulou.electricity.service.EleUserOperateHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Description: EleUserOperateHistoryController
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2024/1/4 21:20
 */

@Slf4j
@RestController
public class JsonAdminEleUserOperateHistoryController {
    
    @Autowired
    EleUserOperateHistoryService eleUserOperateHistoryService;
    
    @GetMapping("/admin/userInfo/userOperateHistory")
    public R queryUserOperateHistory(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam("uid") Long uid) {
        if (Objects.isNull(size) || size < 0 || size > 5) {
            size = 5L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        EleUserOperateHistoryQueryModel queryModel = EleUserOperateHistoryQueryModel.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).uid(uid)
                .build();
        return eleUserOperateHistoryService.listEleUserOperateHistory(queryModel);
    }
}
