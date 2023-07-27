package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.service.carmodel.CarModelBizService;
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
 * 车辆型号 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/carModel")
public class JsonUserCarModelController extends BasicController {

    @Resource
    private CarModelBizService carModelBizService;

    /**
     * 检测是否允许购买此车辆型号
     * @param carModelId 车辆型号ID
     * @return true(允许)、false(不允许)
     */
    @GetMapping("/checkBuyByCarModelId")
    public R<Boolean> checkBuyByCarModelId(Integer carModelId) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carModelBizService.checkBuyByCarModelId(tenantId, user.getUid(), carModelId));
    }

}
