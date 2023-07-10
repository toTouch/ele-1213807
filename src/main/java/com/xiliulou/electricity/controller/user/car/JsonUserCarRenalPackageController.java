package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.car.biz.RentalPackageBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 租车套餐相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackage")
public class JsonUserCarRenalPackageController {

    @Resource
    private RentalPackageBizService rentalPackageBizService;

    /**
     * 根据车辆型号获取C端能够展示的套餐
     */
    @GetMapping("/queryByCarModel")
    public R queryByCarModel(Integer carModelId) {
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        // TODO 获取C端可展示的套餐
        rentalPackageBizService.queryByCarModel(tenantId, user.getUid(), carModelId);
        // TODO 判定押金置灰（免押或者金额）
        // TODO 返回C端展示
        return null;
    }

}
