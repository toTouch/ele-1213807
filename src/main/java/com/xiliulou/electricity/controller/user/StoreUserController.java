package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class StoreUserController {
    /**
     * 服务对象
     */
    @Autowired
    StoreService storeService;

    //列表查询
    @GetMapping(value = "/outer/store/showInfoByDistance")
    public R showInfoByDistance(@RequestParam(value = "distance", required = false) Double distance,
                                @RequestParam("lon") Double lon,
                                @RequestParam("lat") Double lat) {

        if (Objects.isNull(lon) || lon <= 0.0 || Objects.isNull(lat) || lat <= 0.0) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }

        StoreQuery storeQuery = StoreQuery.builder()
                .distance(distance)
                .lon(lon)
                .lat(lat).build();

        return storeService.showInfoByDistance(storeQuery);
    }


}