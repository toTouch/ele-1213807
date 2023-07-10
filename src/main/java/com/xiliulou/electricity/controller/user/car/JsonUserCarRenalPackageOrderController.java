package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 租车套餐订单相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackageOrder")
public class JsonUserCarRenalPackageOrderController {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    /**
     * <code>C</code>端用户取消套餐订单<br />
     * 用于如下情况
     * <pre>
     *     1. 用户未支付（主动终止流程，未曾真正调用微信支付系统）
     * </pre>
     * @param orderNo 购买套餐订单编号
     * @return com.xiliulou.core.web.R<java.lang.Boolean>
     * @author xiaohui.song
     **/
    @GetMapping("/cancelRentalPackageOrder")
    public R<Boolean> cancelRentalPackageOrder(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean cancelFlag = carRentalPackageOrderBizService.cancelRentalPackageOrder(orderNo, tenantId, user.getUid());

        return R.ok(cancelFlag);
    }

    /**
     * <code>C</code>端用户购买租车套餐订单
     * @param buyOptModel 购买参数模型
     * @param request HTTP请求
     * @return com.xiliulou.core.web.R
     * @author xiaohui.song
     */
    @PostMapping("/buyRentalPackageOrder")
    public R<?> buyRentalPackageOrder(@RequestBody CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
        // 参数基本校验
        if (ObjectUtils.allNotNull(buyOptModel, buyOptModel.getRentalPackageId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 从上下文中重新赋值参数
        buyOptModel.setTenantId(tenantId);
        buyOptModel.setUid(user.getUid());

        return carRentalPackageOrderBizService.buyRentalPackageOrder(buyOptModel, request);
    }

}
