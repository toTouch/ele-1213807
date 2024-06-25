package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.query.UserRentCarOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 租车记录
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-21-15:12
 */
@RestController
@Slf4j
public class JsonAdminRentCarOrderController extends BaseController {

    @Autowired
    private RentCarOrderService rentCarOrderService;

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 后台新增租车记录
     */
    @PostMapping("/admin/rentCarOrder/save")
    @Log(title = "线下增加租车记录")
    public R save(@RequestBody @Validated RentCarOrderQuery rentCarOrderQuery){
        return returnTripleResult(rentCarOrderService.save(rentCarOrderQuery));
    }


    //列表查询
    @GetMapping(value = "/admin/rentCarOrder/page")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "carSn", required = false) String carSn,
                       @RequestParam(value = "carModel", required = false) String carModel,
                       @RequestParam(value = "payType", required = false) Integer payType,
                       @RequestParam(value = "storeName", required = false) String storeName) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentCarOrderQuery rentCarOrderQuery = RentCarOrderQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .carModel(carModel)
                .franchiseeName(franchiseeName)
                .carSn(carSn)
                .payType(payType)
                .franchiseeIds(franchiseeIds).build();
        return R.ok(rentCarOrderService.selectByPage(rentCarOrderQuery));
    }

    //列表查询
    @GetMapping(value = "/admin/rentCarOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "carSn", required = false) String carSn,
                        @RequestParam(value = "carModel", required = false) String carModel,
                        @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                        @RequestParam(value = "payType", required = false) Integer payType,
                        @RequestParam(value = "storeName", required = false) String storeName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentCarOrderQuery rentCarOrderQuery = RentCarOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .carModel(carModel)
                .franchiseeName(franchiseeName)
                .carSn(carSn)
                .payType(payType)
                .franchiseeIds(franchiseeIds).build();

        return R.ok(rentCarOrderService.selectPageCount(rentCarOrderQuery));
    }



}
