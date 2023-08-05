package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@RestController
@Slf4j
public class JsonUserEleDepositOrderController {
    /**
     * 服务对象
     */
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;


    //退还押金
    @PostMapping("/user/returnDeposit")
    public R returnDeposit(HttpServletRequest request) {
        return eleDepositOrderService.returnDeposit(request);
    }

    //查询缴纳押金状态
    @PostMapping("/user/eleDepositOrder/queryStatus")
    public R queryStatus(@RequestParam("orderId") String orderId) {
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderId);
        if (Objects.isNull(eleDepositOrder)) {
            log.error("ELECTRICITY  ERROR! not " +
                    "" +
                    " order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        return R.ok(eleDepositOrder.getStatus());
    }

    //用户查询缴纳押金
    @Deprecated
    @GetMapping(value = "/user/queryUserDeposit")
    public R queryUserDeposit() {
        return eleDepositOrderService.queryUserDeposit();
    }

    @GetMapping(value = "/user/batteryDeposit/info")
    public R selectUserBatteryDeposit() {
        return eleDepositOrderService.selectUserBatteryDeposit();
    }

    //用户查询押金
    @GetMapping(value = "/user/queryDeposit")
    public R queryDeposit(@RequestParam(value = "productKey", required = false) String productKey,
                          @RequestParam(value = "deviceName", required = false) String deviceName,
                          @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        return eleDepositOrderService.queryDeposit(productKey, deviceName, franchiseeId);
    }


    //用户查询押金
    @GetMapping(value = "/user/queryFranchiseeDeposit")
    public R queryFranchiseeDeposit(@RequestParam(value = "productKey", required = false) String productKey,
                                    @RequestParam(value = "deviceName", required = false) String deviceName,
                                    @RequestParam(value = "franchiseeId") Long franchiseeId) {
        return eleDepositOrderService.queryFranchiseeDeposit(productKey, deviceName, franchiseeId);
    }


    //用户查询缴纳押金
    @GetMapping(value = "/user/queryModelType")
    public R queryModelType(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return eleDepositOrderService.queryModelType(productKey, deviceName);
    }


    //列表查询
    @GetMapping(value = "/user/eleDepositOrder/list")
    public R queryList(@RequestParam("refundOrderType") Integer refundOrderType) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .uid(user.getUid())
                .tenantId(tenantId)
                .refundOrderType(refundOrderType)
                .offset(0L)
                .size(10L).build();

        return eleDepositOrderService.queryListToUser(eleDepositOrderQuery);
    }


    //列表查询
    @GetMapping(value = "/user/payDepositOrder/list")
    public R payDepositOrderList(@RequestParam("size") Long size,
                                 @RequestParam("offset") Long offset) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .uid(user.getUid())
                .tenantId(tenantId)
                .offset(offset)
                .size(size).build();

        return eleDepositOrderService.payDepositOrderList(eleDepositOrderQuery);
    }

    /**
     * 缴纳电池服务费
     *
     * @return
     */
    @PostMapping("/user/payBatteryServiceFee")
    public R payBatteryServiceFee(HttpServletRequest request) {
        return eleDepositOrderService.payBatteryServiceFee(request);
    }

    //用户查询租车押金
    @GetMapping(value = "/user/queryRentCarDeposit")
    @Deprecated
    public R queryRentCarDeposit() {
        return eleDepositOrderService.queryRentCarDeposit();
    }

    /**
     * 退租车押金
     *
     * @return
     */
    @PostMapping("/user/refundRentCarDeposit")
    @Deprecated
    public R refundRentCarDeposit(HttpServletRequest request) {
        return eleDepositOrderService.refundRentCarDeposit(request);
    }
    
    /**
     * 新退租车押金，可审核退线下缴纳押金
     *
     * @return
     */
    @PostMapping("/user/refundCarDeposit")
    public R refundCarDeposit() {
        return eleDepositOrderService.refundCarDeposit();
    }

    /**
     * 用户端退款订单分页
     */
    @GetMapping(value = "/user/batteryDeposit/refund")
    public R batteryDepositRefund(@RequestParam("size") long size, @RequestParam("offset") long offset) {

        EleRefundQuery eleRefundQuery = EleRefundQuery.builder()
                .offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(SecurityUtils.getUid())
                .build();

        return eleRefundOrderService.queryList(eleRefundQuery);
    }
}

