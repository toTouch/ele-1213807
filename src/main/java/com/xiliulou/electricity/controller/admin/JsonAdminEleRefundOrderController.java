package com.xiliulou.electricity.controller.admin;

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
public class JsonAdminEleRefundOrderController {
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


    //退款列表
    @GetMapping("/admin/eleRefundOrder/queryList")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "payType", required = false) Integer payType,
                       @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
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

        EleRefundQuery eleRefundQuery = EleRefundQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .phone(phone)
                .payType(payType)
                .refundOrderType(refundOrderType)
                .name(name).build();

        return eleRefundOrderService.queryList(eleRefundQuery);
    }

    //退款列表总数
    @GetMapping("/admin/eleRefundOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "payType", required = false) Integer payType,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
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

        EleRefundQuery eleRefundQuery = EleRefundQuery.builder()
                .orderId(orderId)
                .status(status)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .payType(payType)
                .refundOrderType(refundOrderType)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .phone(phone).build();

        return eleRefundOrderService.queryCount(eleRefundQuery);
    }

    //后台退款处理
    @PostMapping("/admin/handleRefund")
    @Log(title = "后台退款处理")
    public R handleRefund(@RequestParam("refundOrderNo") String refundOrderNo,
                          @RequestParam("status") Integer status,
                          @RequestParam(value = "errMsg", required = false) String errMsg,
                          @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                          @RequestParam("uid") Long uid,
                          HttpServletRequest request) {
        return eleRefundOrderService.handleRefund(refundOrderNo, errMsg, status, refundAmount, uid, request);
    }

    //后台租车线下退款处理
    @PostMapping("/admin/handleOffLineRefund")
    @Log(title = "后台线下退款处理")
    public R handleOffLineRefund(@RequestParam("refundOrderNo") String refundOrderNo,
                                 @RequestParam("status") Integer status,
                                 @RequestParam(value = "errMsg", required = false) String errMsg,
                                 @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                                 @RequestParam("uid") Long uid,
                                 HttpServletRequest request) {
        return eleRefundOrderService.handleOffLineRefund(refundOrderNo, errMsg, status, refundAmount, uid, request);
    }


    /**
     * 租车押金后台线上退款
     * @return
     */
    @PostMapping("/admin/refundDepositCarByOnline")
    @Log(title = "租车押金后台线上退款")
    public R refundDepositCarByOnline(@RequestParam("refundOrderNo") String refundOrderNo,
                          @RequestParam("status") Integer status,
                          @RequestParam(value = "errMsg", required = false) String errMsg,
                          @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                          @RequestParam("uid") Long uid,
                          HttpServletRequest request) {
        return eleRefundOrderService.handleRefund(refundOrderNo, errMsg, status, refundAmount, uid, request);
    }

    /**
     * 租车押金后台线下退款
     */
    @PostMapping("/admin/refundDepositCarByOffline")
    @Log(title = "租车押金后台线下退款")
    public R refundDepositCarByOffline(@RequestParam("refundOrderNo") String refundOrderNo,
                                 @RequestParam("status") Integer status,
                                 @RequestParam(value = "errMsg", required = false) String errMsg,
                                 @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                                 @RequestParam("uid") Long uid,
                                 HttpServletRequest request) {
        return eleRefundOrderService.handleOffLineRefund(refundOrderNo, errMsg, status, refundAmount, uid, request);
    }


    //用户电池押金缴纳方式
    @GetMapping("/admin/queryUserDepositPayType")
    public R queryUserDepositPayType(@RequestParam("uid") Long uid) {
        return eleRefundOrderService.queryUserDepositPayType(uid);
    }

	//后台电池线下退款处理
	@PostMapping("/admin/batteryOffLineRefund")
    @Log(title = "电池押金后台线下退款")
	public R batteryOffLineRefund(@RequestParam(value = "errMsg", required = false) String errMsg,
								 @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
								 @RequestParam("uid") Long uid,
								  @RequestParam("refundType") Integer refundType) {
		return eleRefundOrderService.batteryOffLineRefund( errMsg,refundAmount, uid, refundType);
	}

    //后台电池线上退款处理
    @PostMapping("/admin/batteryOnLineRefund")
    @Log(title = "电池押金后台线上退款")
    public R batteryOnLineRefund(@RequestParam(value = "errMsg", required = false) String errMsg,
                                  @RequestParam(value = "refundAmount", required = false) BigDecimal refundAmount,
                                  @RequestParam("uid") Long uid,
                                  @RequestParam("refundType") Integer refundType) {
        return eleRefundOrderService.batteryOffLineRefund( errMsg,refundAmount, uid, refundType);
    }
}
