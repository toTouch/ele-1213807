package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 车辆 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/v2")
public class JsonUserCarController {

    @Resource
    private ElectricityCarService carService;


    /**
     * 根据车辆SN获取车辆型号ID
     * @param sn 车辆SN码
     * @return
     */
    @GetMapping("/queryCarModelBySn")
    public R<Integer> queryCarModelBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityCar electricityCar = carService.selectBySn(sn, tenantId);
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            return R.ok(electricityCar.getModelId());
        }

        return R.ok();
    }
}
