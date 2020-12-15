package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    UserInfoService userInfoService;

    //列表查询
    @GetMapping(value = "/outer/electricityCabinet/showInfoByDistance")
    public R showInfoByDistance(@RequestParam(value = "distance", required = false) Double distance,
                                @RequestParam("lon") Double lon,
                                @RequestParam("lat") Double lat) {

        if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .distance(distance)
                .lon(lon)
                .lat(lat).build();

        return electricityCabinetService.showInfoByDistance(electricityCabinetQuery);
    }

    /**
     * 查询换电柜 按三元组
     *
     * @return
     */
    @GetMapping(value = "/user/electricityCabinet")
    public R queryByDevice(@RequestParam("productKey") String productKey,@RequestParam("deviceName")String deviceName
            ,@RequestParam("deviceSecret") String deviceSecret) {
        return electricityCabinetService.queryByDevice(productKey,deviceName,deviceSecret);
    }

    /**
     * 查询换电柜
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/user/electricityCabinet/{id}")
    public R queryOne(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.queryOne(id);
    }

    /**
     * 用户端首页
     *
     * @return
     */
    @GetMapping(value = "/user/electricityCabinet/home")
    public R homeOne() {
        return electricityCabinetService.home();
    }

    /**
     * 查询套餐信息
     *
     * @return
     */
    @GetMapping(value = "/user/memberCard/info")
    public R getMemberCardInfo() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        return userInfoService.getMemberCardInfo(uid);
    }

}