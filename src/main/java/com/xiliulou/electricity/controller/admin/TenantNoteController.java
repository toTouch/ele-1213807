package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.tenantNote.TenantRechargeRequest;
import com.xiliulou.electricity.service.TenantNoteService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2023/12/28 9:53
 * @desc 租户短信充值
 */

@RestController
@Slf4j
public class TenantNoteController {
    @Resource
    private TenantNoteService tenantNoteService;
    
    /**
     * 短信充值
     *
     * @param rechargeRequest
     * @return
     */
    @PostMapping("/admin/tenant/note/recharge")
    public R recharge(@RequestBody @Validated TenantRechargeRequest rechargeRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        Triple<Boolean, String, Object> r = tenantNoteService.recharge(rechargeRequest, user.getUid());
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        // 删除缓存
        tenantNoteService.deleteCache(rechargeRequest.getTenantId());
        return R.ok();
    }
}
