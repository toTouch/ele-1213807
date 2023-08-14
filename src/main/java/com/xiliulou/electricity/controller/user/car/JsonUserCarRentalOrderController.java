package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 车辆租赁订单 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/rentCar")
public class JsonUserCarRentalOrderController {

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    /**
     * 还车申请
     * @return true(成功)、false(失败)
     */
    @GetMapping("/refundCarOrderApply")
    public R<Boolean> refundCarOrderApply() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(carRentalOrderBizService.refundCarOrderApply(tenantId, user.getUid()));
    }

    /**
     * 扫码绑定车辆
     * @param sn 车辆SN码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/scanQR")
    public R<Boolean> scanQR(String sn) {
        if (StringUtils.isBlank(sn)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalOrderBizService.bindingCarByQR(tenantId, user.getUid(), sn, user.getUid()));
    }
}
