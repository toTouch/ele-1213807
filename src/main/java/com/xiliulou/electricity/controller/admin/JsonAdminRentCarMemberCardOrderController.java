package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CarMemberCardOrderAddAndUpdate;
import com.xiliulou.electricity.query.MemberCardOrderAddAndUpdate;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-21-15:12
 */
@RestController
@Slf4j
public class JsonAdminRentCarMemberCardOrderController extends BaseController {

    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UserDataScopeService userDataScopeService;

    @GetMapping("/admin/rentCarMemberCardOrder/page")
    public R getElectricityMemberCardPage(@RequestParam("size") Long size,
                                          @RequestParam("offset") Long offset,
                                          @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "orderId", required = false) String orderId,
                                          @RequestParam(value = "memberCardType", required = false) String memberCardType,
                                          @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "userName", required = false) String userName,
                                          @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                          @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentCarMemberCardOrderQuery memberCardOrderQuery = RentCarMemberCardOrderQuery.builder()
                .offset(offset)
                .size(size)
                .phone(phone)
                .orderId(orderId)
                .beginTime(queryStartTime)
                .endTime(queryEndTime)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .userName(userName)
                .memberCardType(memberCardType)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds).build();

        return R.ok(carMemberCardOrderService.selectByPage(memberCardOrderQuery));
    }

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("/admin/rentCarMemberCardOrder/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "memberCardType", required = false) String memberCardType,
                        @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                        @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                        @RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "franchiseeName", required = false) String franchiseeName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentCarMemberCardOrderQuery memberCardOrderQuery = RentCarMemberCardOrderQuery.builder()
                .phone(phone)
                .orderId(orderId)
                .beginTime(queryStartTime)
                .endTime(queryEndTime)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .userName(userName)
                .memberCardType(memberCardType)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds).build();

        return R.ok(carMemberCardOrderService.selectByPageCount(memberCardOrderQuery));
    }
    
    /**
     * 编辑用户套餐
     *
     * @return
     */
    @PutMapping(value = "/admin/rentCarMemberCard/editUserMemberCard")
    @Log(title = "编辑用户车辆套餐")
    public R editUserMemberCard(@RequestBody @Validated CarMemberCardOrderAddAndUpdate carMemberCardOrderAddAndUpdate) {
        return carMemberCardOrderService.editUserMemberCard(carMemberCardOrderAddAndUpdate);
    }
}
