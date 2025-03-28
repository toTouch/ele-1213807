package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.MemberCardOrderAddAndUpdate;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.UserBatteryDepositAndMembercardQuery;
import com.xiliulou.electricity.query.UserBatteryMembercardQuery;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class JsonAdminElectricityMemberCardOrderController extends BaseController {
    
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
    public R getElectricityMemberCardPage(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "memberCardType", required = false) Integer cardType,
            @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "useStatus", required = false) Integer useStatus, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "payCount", required = false) Integer payCount, @RequestParam(value = "refId", required = false) Long refId,
            @RequestParam(value = "queryStartTime", required = false) Long queryStartTime, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "cardId", required = false) Long cardId,
            @RequestParam(value = "externalAgreementNo", required = false) String externalAgreementNo,
            @RequestParam(value = "paymentChannel", required = false) String paymentChannel,
            @RequestParam(value = "model", required = false) String model) {
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder().payType(payType).phone(phone).orderId(orderId).cardType(cardType).queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime).offset(offset).size(size).tenantId(TenantContextHolder.getTenantId()).status(status).uid(uid).useStatus(useStatus).source(source)
                .payType(payType).refId(refId).cardModel(memberCardModel).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).storeIds(storeIds).cardPayCount(payCount)
                .userName(userName).payType(payType).cardId(cardId).paymentChannel(paymentChannel).externalAgreementNo(externalAgreementNo).model(model).build();
        
        return electricityMemberCardOrderService.queryList(memberCardOrderQuery);
    }
    
    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCardOrder/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "memberCardType", required = false) Integer cardType, @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "useStatus", required = false) Integer useStatus,
            @RequestParam(value = "payCount", required = false) Integer payCount, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "refId", required = false) Long refId, @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "cardId", required = false) Long cardId,
            @RequestParam(value = "paymentChannel", required = false) String paymentChannel,
            @RequestParam(value = "externalAgreementNo", required = false) String externalAgreementNo, @RequestParam(value = "model", required = false) String model) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder().payType(payType).phone(phone).orderId(orderId).cardType(cardType).queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime).tenantId(TenantContextHolder.getTenantId()).status(status).uid(uid).useStatus(useStatus).source(source).payType(payType).refId(refId)
                .cardModel(memberCardModel).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).storeIds(storeIds).cardPayCount(payCount).userName(userName).payType(payType)
                .cardId(cardId).paymentChannel(paymentChannel).externalAgreementNo(externalAgreementNo).model(model).build();
        
        return electricityMemberCardOrderService.queryCount(memberCardOrderQuery);
    }
    
    //换电柜购卡订单导出报表
    @Deprecated
    @GetMapping("/admin/electricityMemberCardOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "memberCardType", required = false) Integer cardType,
            @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "payCount", required = false) Integer payCount,
            @RequestParam(value = "refId", required = false) Long refId, @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
            HttpServletResponse response) {}
    
    /**
     * 暂停用户套餐
     *
     * @return
     */
    @PutMapping("/admin/memberCard/disableUserMemberCard")
    @Log(title = "暂停用户套餐")
    public R adminDisableMemberCard(@RequestParam("uid") Long uid, @RequestParam("days") Integer days) {
        return electricityMemberCardOrderService.adminDisableMemberCard(uid, days);
    }
    
    /**
     * 启用用户套餐
     *
     * @return
     */
    @PutMapping("/admin/memberCard/enableUserMemberCard")
    @Log(title = "启用用户套餐")
    public R adminEnableMemberCard(@RequestParam("uid") Long uid) {
        return electricityMemberCardOrderService.adminEnableMemberCard(uid);
    }
    
    /**
     * 清除用户电池服务费
     *
     * @param uid
     * @return
     */
    @PutMapping("/admin/memberCard/cleanBatteryServiceFee")
    @Log(title = "清除用户电池服务费")
    public R cleanBatteryServiceFee(@RequestParam("uid") Long uid) {
        return electricityMemberCardOrderService.cleanBatteryServiceFee(uid);
    }
    
    /**
     * 用户交押金绑定套餐(3.0)
     */
    @PostMapping(value = "/admin/electricityMemberCard/addUserDepositAndMemberCard")
    @Log(title = "用户交押金绑定套餐")
    public R addUserDepositAndMemberCard(@RequestBody @Validated UserBatteryDepositAndMembercardQuery query) {
        return returnTripleResult(electricityMemberCardOrderService.addUserDepositAndMemberCard(query));
    }
    
    /**
     * 编辑用户套餐(3.0)
     */
    @PutMapping(value = "/admin/electricityMemberCard/editUserBatteryMemberCard")
    @Log(title = "编辑用户套餐")
    public R editUserBatteryMemberCard(@RequestBody @Validated UserBatteryMembercardQuery query) {
        return returnTripleResult(electricityMemberCardOrderService.editUserBatteryMemberCard(query));
    }
    
    /**
     * 续费用户套餐(3.0)
     */
    @PutMapping(value = "/admin/electricityMemberCard/renewalUserBatteryMemberCard")
    @Log(title = "用户套餐续费")
    public R renewalUserBatteryMemberCard(@RequestBody @Validated UserBatteryMembercardQuery query) {
        return returnTripleResult(electricityMemberCardOrderService.renewalUserBatteryMemberCard(query));
    }
    
    /**
     * 获取用户当前绑定的套餐详情
     */
    @GetMapping(value = "/admin/electricityMemberCard/userBatteryMembercardInfo")
    public R userBatteryMembercardInfo(@RequestParam("uid") Long uid) {
        return returnTripleResult(electricityMemberCardOrderService.userBatteryMembercardInfo(uid));
    }


    @GetMapping(value = "/admin/electricityMemberCard/getBatteryMode")
    public R getBatteryMode(@RequestParam("size") Long size, @RequestParam("offset") Long offset) {
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(electricityMemberCardOrderService.getBatteryMode(size, offset));
    }

}
