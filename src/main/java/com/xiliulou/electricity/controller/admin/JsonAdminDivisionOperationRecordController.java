package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.DivisionAccountOperationRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.DivisionAccountOperationRecordQuery;
import com.xiliulou.electricity.service.DivisionAccountOperationRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@RestController
public class JsonAdminDivisionOperationRecordController {

    @Autowired
    private DivisionAccountOperationRecordService divisionAccountOperationRecordService;

    /**
     * 对分账的操作记录
     *
     * @return
     */
    @GetMapping(value = "/admin/division/account/operation/list")
    public R insertDivisionAccountRecord(@RequestParam("size") long size, @RequestParam("offset") long offset,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "divisionAccountId") Integer divisionAccountId) {

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

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        DivisionAccountOperationRecordQuery divisionAccountOperationRecord = DivisionAccountOperationRecordQuery.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId())
                .divisionAccountId(divisionAccountId)
                .build();

        return  R.ok(divisionAccountOperationRecordService.queryList(divisionAccountOperationRecord));
    }


    /**
     * 对分账的操作总记录数
     *
     * @return
     */
    @GetMapping(value = "/admin/division/account/operation/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "accountMemberCard", required = false) String accountMemberCard,
                        @RequestParam(value = "divisionAccountId") Integer divisionAccountId){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        DivisionAccountOperationRecordQuery divisionAccountOperationRecord = DivisionAccountOperationRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .name(name)
                .tenantId(TenantContextHolder.getTenantId())
                .divisionAccountId(divisionAccountId)
                .build();

        return  R.ok(divisionAccountOperationRecordService.queryCount(divisionAccountOperationRecord));
    }

}
