package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.tenantNote.TenantRechargePageRequest;
import com.xiliulou.electricity.service.TenantNoteRechargeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2023/12/28 9:53
 * @desc
 */

@RestController
@Slf4j
public class TenantNoteRechargeController {
    @Resource
    private TenantNoteRechargeService noteRechargeService;
    
    /**
     * @param
     * @description 短信充值记录数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/tenantNote/recharge/pageCount")
    public R pageCount(@RequestParam(value = "tenantId") Integer tenantId) {
        if (ObjectUtils.isEmpty(tenantId)) {
            return R.fail("300824", "租户id不能为空");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        TenantRechargePageRequest request = TenantRechargePageRequest.builder().tenantId(tenantId).build();
        return R.ok(noteRechargeService.countTotal(request));
    }
    
    /**
     * @param
     * @description 短信充值记录拨分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/tenantNote/recharge/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "tenantId") Integer tenantId) {
        if (ObjectUtils.isEmpty(tenantId)) {
            return R.fail("300824", "租户id不能为空");
        }
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        TenantRechargePageRequest request = TenantRechargePageRequest.builder().tenantId(tenantId).build();
    
        return R.ok(noteRechargeService.listByPage(request));
    }
}
