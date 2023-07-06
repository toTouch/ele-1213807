package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserAmountQuery;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminUserAmountController {
    /**
     * 服务对象
     */
    @Resource
    private UserAmountService userAmountService;

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 用户余额列表
     */
    @GetMapping(value = "/admin/userAmount/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "phone", required = false) String phone) {

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

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserAmountQuery userAmountQuery = UserAmountQuery.builder()
                .offset(offset)
                .size(size)
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userAmountService.queryList(userAmountQuery);
    }

    /**
     * 用户余额列表
     */
    @GetMapping(value = "/admin/userAmount/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserAmountQuery userAmountQuery = UserAmountQuery.builder()
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userAmountService.queryCount(userAmountQuery);
    }

}
