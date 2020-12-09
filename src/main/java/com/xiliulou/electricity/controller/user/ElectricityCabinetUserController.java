package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
public class ElectricityCabinetUserController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    //列表查询
    @GetMapping(value = "/user/electricityCabinet/showInfoByDistance")
    public R showInfoByDistance(@RequestParam(value = "distance", required = false) Double distance,
                       @RequestParam("lon") Double lon,
                       @RequestParam("lat") Double lat) {

        if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .distance(distance)
                .lon(lon)
                .lat(lat).build();

        return electricityCabinetService.showInfoByDistance(electricityCabinetQuery);
    }

    //查询换电柜
    @GetMapping(value = "/user/electricityCabinet/{id}")
    public R queryOne(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        return electricityCabinetService.queryOne(id);
    }

    //用户端首页
    @PostMapping(value = "/user/electricityCabinet/home")
    public R homeOne() {
        return electricityCabinetService.home();
    }

}