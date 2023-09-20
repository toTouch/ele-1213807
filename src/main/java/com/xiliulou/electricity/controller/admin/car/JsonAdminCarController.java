package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/v2")
public class JsonAdminCarController {

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    /**
     * 车辆解绑用户
     * @param uid 用户UID
     * @return true(成功)、false(失败)
     */
    @GetMapping("/unBindUser")
    public R<Boolean> unBindUser(Long uid) {
        if (ObjectUtils.isEmpty(uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalOrderBizService.unBindingCar(tenantId, uid, user.getUid()));
    }

    /**
     * 车辆绑定用户
     * @param carSn 车辆SN码
     * @param uid 用户UID
     * @return true(成功)、false(失败)
     */
    @GetMapping("/bindUser")
    public R<Boolean> bindUser(String carSn, Long uid) {
        if (StringUtils.isBlank(carSn) || ObjectUtils.isEmpty(uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalOrderBizService.bindingCar(tenantId, uid, carSn, user.getUid()));
    }

}
