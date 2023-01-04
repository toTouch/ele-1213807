package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonUserStoreController {
    /**
     * 服务对象
     */
    @Autowired
    StoreService storeService;

    //列表查询
    @GetMapping(value = "/outer/store/showInfoByDistance")
    public R showInfoByDistance(@RequestParam(value = "distance", required = false) Double distance,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam("lon") Double lon,
                                @RequestParam("lat") Double lat) {

        if (lon <= 0.0 || lat <= 0.0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        StoreQuery storeQuery = StoreQuery.builder()
                .distance(distance)
                .lon(lon)
                .lat(lat)
                .tenantId(tenantId)
                .name(name).build();

        return storeService.showInfoByDistance(storeQuery);
    }

    /**
     * 门店列表
     */
	@GetMapping(value = "/user/store/listByDistance")
    public R storeListByDistance(@RequestParam("size") Long size,
                                 @RequestParam("offset") Long offset,
                                 @RequestParam(value = "franchiseeId" , required = false) Long franchiseeId,
                                 @RequestParam(value = "distance", required = false) Double distance,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam("lon") Double lon,
                                 @RequestParam("lat") Double lat) {

        if (lon <= 0.0 || lat <= 0.0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        StoreQuery storeQuery = StoreQuery.builder()
				.size(size)
				.offset(offset)
                .distance(distance)
                .lon(lon)
                .lat(lat)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .name(name).build();

        return R.ok(storeService.selectListByDistance(storeQuery));
    }

    /**
     * 门店详情
     */
    @GetMapping(value = "/user/store/{id}")
    public R storeDetail(@PathVariable("id") Long id){
        return R.ok(storeService.selectDetailById(id));
    }



}
