package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;
import com.xiliulou.electricity.query.ServicePhoneQuery;
import com.xiliulou.electricity.service.ElectricitySubscriptionMessageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 17:56
 **/
@RestController
@Slf4j
public class JsonAdminElectricitySubscriptionMessageController {
    @Autowired
    ElectricitySubscriptionMessageService electricitySubscriptionMessageService;
    @Autowired
    RedisService redisService;

    /**
     * 新增
     *
     * @return
     */
    @PostMapping("admin/subscriptionMessage")
    public R saveElectricitySubscriptionMessage(@RequestBody @Validated ElectricitySubscriptionMessage electricitySubscriptionMessage) {
        return electricitySubscriptionMessageService.saveElectricitySubscriptionMessage(electricitySubscriptionMessage);
    }

    /**
     * 获取 小程序服务信息
     *
     * @return
     */
    @GetMapping("admin/servicePhone")
    public R getServicePhone() {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(redisService.get(CacheConstant.CACHE_SERVICE_PHONE+tenantId));
    }

    /**
     * 设置小程序服务信息
     *
     * @return
     */
    @PostMapping("admin/servicePhone")
    public R getServicePhone(@RequestBody ServicePhoneQuery servicePhoneQuery) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        redisService.set(CacheConstant.CACHE_SERVICE_PHONE+tenantId, servicePhoneQuery.getPhone());
        return R.ok();
    }


    /**
     * 修改
     *
     * @return
     */
    @PutMapping("/admin/subscriptionMessage")
    public R updateElectricitySubscriptionMessage(@RequestBody @Validated ElectricitySubscriptionMessage electricitySubscriptionMessage) {
        if (Objects.isNull(electricitySubscriptionMessage)) {
            return R.failMsg("请求参数Id不能为空!");
        }
        return electricitySubscriptionMessageService.updateElectricitySubscriptionMessage(electricitySubscriptionMessage);
    }


    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/subscriptionMessage/list")
    public R getElectricityMemberCardPage(@RequestParam(value = "type", required = false) Integer type) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        return electricitySubscriptionMessageService.getElectricitySubscriptionMessagePage(type,tenantId);
    }

}
