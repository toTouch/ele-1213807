package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.MemberCardOrderAddAndUpdate;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:19
 **/
@RestController
@Slf4j
public class JsonAdminElectricityMemberCardOrderController {
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCardOrder/page")
    public R getElectricityMemberCardPage(@RequestParam("size") Long size,
                                          @RequestParam("offset") Long offset,
                                          @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "orderId", required = false) String orderId,
                                          @RequestParam(value = "memberCardType", required = false) Integer cardType,
                                          @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
		                                  @RequestParam(value = "userName", required = false) String userName,
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
        
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
                .phone(phone)
                .orderId(orderId)
                .cardType(cardType)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .cardModel(memberCardModel)
                .franchiseeName(franchiseeName)
		        .userName(userName)
		        .franchiseeIds(franchiseeIds).build();

        return electricityMemberCardOrderService.queryList(memberCardOrderQuery);
    }

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCardOrder/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "memberCardType", required = false) Integer cardType,
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
    
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
                .phone(phone)
                .orderId(orderId)
                .cardType(cardType)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .cardModel(memberCardModel)
                .franchiseeName(franchiseeName)
		        .userName(userName)
		        .franchiseeIds(franchiseeIds).build();

        return electricityMemberCardOrderService.queryCount(memberCardOrderQuery);
    }

    //换电柜购卡订单导出报表
    @GetMapping("/admin/electricityMemberCardOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "orderId", required = false) String orderId,
                            @RequestParam(value = "memberCardType", required = false) Integer cardType,
                            @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                            HttpServletResponse response) {

		Double days = (Double.valueOf(queryEndTime - queryStartTime)) / 1000 / 3600 / 24;
		if (days > 33) {
			throw new CustomBusinessException("搜索日期不能大于31天");
		}

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            throw new CustomBusinessException("查不到订单");
        }
    
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            throw new CustomBusinessException("订单不存在");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                throw new CustomBusinessException("订单不存在！");
            }
        }

        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
                .phone(phone)
                .orderId(orderId)
                .cardType(cardType)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .tenantId(TenantContextHolder.getTenantId())
                .cardModel(ElectricityMemberCardOrder.BATTERY_MEMBER_CARD)
                .franchiseeIds(franchiseeIds).build();
        electricityMemberCardOrderService.exportExcel(memberCardOrderQuery, response);
    }

    /**
     * 新增用户套餐
     *
     * @return
     */
    @PostMapping(value = "/admin/electricityMemberCard/addUserMemberCard")
    public R addUserMemberCard(@RequestBody @Validated MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate) {
        return electricityMemberCardOrderService.addUserMemberCard(memberCardOrderAddAndUpdate);
    }

    /**
     * 编辑用户套餐
     *
     * @return
     */
    @PutMapping(value = "/admin/electricityMemberCard/editUserMemberCard")
    public R editUserMemberCard(@RequestBody @Validated MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate) {
        return electricityMemberCardOrderService.editUserMemberCard(memberCardOrderAddAndUpdate);
    }

    /**
     * 暂停用户套餐
     *
     * @param usableStatus
     * @return
     */
    @PutMapping("/admin/memberCard/openOrDisableMemberCard")
    public R adminOpenOrDisableMemberCard(@RequestParam("usableStatus") Integer usableStatus, @RequestParam("uid") Long uid) {
        return electricityMemberCardOrderService.adminOpenOrDisableMemberCard(usableStatus, uid);
    }

    /**
     * 清除用户电池服务费
     *
     * @param uid
     * @return
     */
    @PutMapping("/admin/memberCard/cleanBatteryServiceFee")
    public R cleanBatteryServiceFee(@RequestParam("uid") Long uid) {
        return electricityMemberCardOrderService.cleanBatteryServiceFee(uid);
    }

}
