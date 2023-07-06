package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 保险订单表(TInsuranceOrder)表控制层
 *
 * @author makejava
 * @since 2022-11-04 10:56:56
 */
@RestController
@Slf4j
public class JsonAdminInsuranceOrderController {
    /**
     * 服务对象
     */
    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    UserDataScopeService userDataScopeService;

    //保险订单查询
    @GetMapping("/admin/insuranceOrder/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "userName", required = false) String userName,
                       @RequestParam(value = "payType", required = false) Integer payType) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InsuranceOrderQuery insuranceOrderQuery=InsuranceOrderQuery.builder()
                .orderId(orderId)
                .beginTime(beginTime)
                .endTime(endTime)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .franchiseeName(franchiseeName)
                .tenantId(tenantId)
                .phone(phone)
                .status(status)
                .userName(userName)
                .offset(offset)
                .size(size)
                .payType(payType).build();

        return insuranceOrderService.queryList(insuranceOrderQuery);
    }

    //保险订单查询
    @GetMapping("/admin/insuranceOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "payType", required = false) Integer payType) {

        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InsuranceOrderQuery insuranceOrderQuery=InsuranceOrderQuery.builder()
                .orderId(orderId)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .franchiseeName(franchiseeName)
                .tenantId(tenantId)
                .phone(phone)
                .userName(userName)
                .payType(payType).build();

        return insuranceOrderService.queryCount(insuranceOrderQuery);
    }


}
