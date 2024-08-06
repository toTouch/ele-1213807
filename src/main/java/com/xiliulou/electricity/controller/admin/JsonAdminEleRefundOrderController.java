package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class JsonAdminEleRefundOrderController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    
    /**
     * 租电池押金退款列表
     */
    @GetMapping("/admin/eleRefundOrder/queryList")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "payType", required = false) Integer payType,
            @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,@RequestParam(value = "paymentChannel", required = false) String paymentChannel) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user ");
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
        
        EleRefundQuery eleRefundQuery = EleRefundQuery.builder().offset(offset).size(size).orderId(orderId).status(status).beginTime(beginTime).endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).storeIds(storeIds).franchiseeIds(franchiseeIds).phone(phone).uid(uid).payType(payType).refundOrderType(refundOrderType)
                .name(name).orderType(orderType).refundOrderNo(refundOrderNo).paymentChannel(paymentChannel).build();
        
        return eleRefundOrderService.queryList(eleRefundQuery);
    }
    
    // 退款列表总数
    @GetMapping("/admin/eleRefundOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderType", required = false) Integer orderType, @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo
            ,@RequestParam(value = "paymentChannel", required = false) String paymentChannel) {
        
        // 用户区分
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
        
        EleRefundQuery eleRefundQuery = EleRefundQuery.builder().orderId(orderId).status(status).storeIds(storeIds).franchiseeIds(franchiseeIds).payType(payType)
                .refundOrderType(refundOrderType).beginTime(beginTime).endTime(endTime).tenantId(TenantContextHolder.getTenantId()).phone(phone).uid(uid).orderType(orderType)
                .refundOrderNo(refundOrderNo).paymentChannel(paymentChannel).build();
        
        return eleRefundOrderService.queryCount(eleRefundQuery);
    }

    /**
     * 电池押金后台退款审核处理
     */
    @PostMapping("/admin/handleRefund")
    @Log(title = "电池押金退款审核")
    public R handleRefund(@RequestParam("refundOrderNo") String refundOrderNo, @RequestParam("status") Integer status,
            @RequestParam(value = "errMsg", required = false) String errMsg, @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam(value = "offlineRefund", required = false) Integer offlineRefund, @RequestParam("uid") Long uid, HttpServletRequest request) {
        return returnTripleResult(eleRefundOrderService.handleRefundOrder(refundOrderNo, errMsg, status, refundAmount, uid, offlineRefund, request));
    }
    
    /**
     * 电池免押后台退款审核处理
     */
    @PostMapping("/admin/battery/free/refund/audit")
    @Log(title = "电池免押退款审核")
    public R batteryFreeDepostRefundAudit(@RequestParam("refundOrderNo") String refundOrderNo, @RequestParam("status") Integer status,
            @RequestParam(value = "errMsg", required = false) String errMsg, @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam("uid") Long uid) {
        return returnTripleResult(eleRefundOrderService.batteryFreeDepostRefundAudit(refundOrderNo, errMsg, status, refundAmount, uid));
    }
    
    
    // 用户电池押金缴纳方式
    @GetMapping("/admin/queryUserDepositPayType")
    public R queryUserDepositPayType(@RequestParam("uid") Long uid) {
        return eleRefundOrderService.queryUserDepositPayType(uid);
    }
    
    // 后台电池线下退款处理
    @PostMapping("/admin/batteryOffLineRefund")
    @Log(title = "电池押金后台线下退款")
    public R batteryOffLineRefund(@RequestParam(value = "errMsg", required = false) String errMsg, @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam("uid") Long uid, @RequestParam("refundType") Integer refundType, @RequestParam(value = "offlineRefund", required = false) Integer offlineRefund) {
        return eleRefundOrderService.batteryOffLineRefund(errMsg, refundAmount, uid, refundType, offlineRefund);
    }
    
    // 后台电池线上退款处理
    @PostMapping("/admin/batteryOnLineRefund")
    @Log(title = "电池押金后台线上退款")
    public R batteryOnLineRefund(@RequestParam(value = "errMsg", required = false) String errMsg, @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
            @RequestParam("uid") Long uid, @RequestParam("refundType") Integer refundType, @RequestParam(value = "offlineRefund", required = false) Integer offlineRefund) {
        return eleRefundOrderService.batteryOffLineRefund(errMsg, refundAmount, uid, refundType, offlineRefund);
    }
    
    /**
     * 电池免押退押金
     */
    @PostMapping("/admin/battery/freeDeposit/refund")
    @Log(title = "电池免押后台退押金")
    public R batteryFreeDepositRefund(@RequestParam(value = "errMsg", required = false) String errMsg, @RequestParam("uid") Long uid) {
        return returnTripleResult(eleRefundOrderService.batteryFreeDepositRefund(errMsg, uid));
    }
}
