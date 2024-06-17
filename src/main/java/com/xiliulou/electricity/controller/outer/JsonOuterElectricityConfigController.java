package com.xiliulou.electricity.controller.outer;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonOuterElectricityConfigController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    private RedisService redisService;
    
    
    //查询平台名称
    @GetMapping(value = "/outer/electricityConfig")
    public R queryOne() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityConfigService.queryFromCacheByTenantId(tenantId));
    }

    /**
     * 根据小程序appId获取tenantId及租户配置信息
     */
    @GetMapping(value = "/outer/tenantConfig")
    public R tenantConfig(@RequestParam("appId") String appId) {
        return R.ok(electricityConfigService.getTenantConfig(appId));
    }
    
    
    @Deprecated
    @GetMapping(value = "/outer/merchant/minPro/config")
    public R merchantMinProConfig(@RequestParam("appId") String appId) {
        return returnTripleResult(electricityPayParamsService.queryByMerchantAppId(appId));
    }
    
    /**
     * @description 微信小程序过审配合
     * @date 2023/11/3 17:18:22
     * @author HeYafeng
     */
    @PostMapping(value = "/outer/weChat/approve/in/cache")
    public R weChatApproveInCache(@RequestParam String msg) {
        redisService.set(CacheConstant.CACHE_WECHAT_APPROVE, msg);
        return R.ok();
    }
    
    /**
     * @description 微信小程序过审配合
     * @date 2023/11/3 17:18:22
     * @author HeYafen
     */
    @GetMapping("/outer/weChat/approve/out/cache")
    public R weChatApproveOutCache() {
        return R.ok(redisService.get(CacheConstant.CACHE_WECHAT_APPROVE));
    }
}
