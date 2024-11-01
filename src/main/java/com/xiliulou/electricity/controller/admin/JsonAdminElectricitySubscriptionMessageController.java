package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricitySubscriptionMessage;
import com.xiliulou.electricity.query.ServicePhoneQuery;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricitySubscriptionMessageService;
import com.xiliulou.electricity.service.ServicePhoneService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.ValidList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Resource
    private ServicePhoneService servicePhoneService;
    
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
     * 更新小程序客服配置
     *
     * @param status 打开微信客服 0-是 1-否
     * @return R.ok()
     */
    @PutMapping(value = "/admin/tenantConfig/wxCustomer")
    public R updateTenantConfigWxCustomer(@RequestParam("status") Integer status) {
        electricityConfigService.updateTenantConfigWxCustomer(status);
        return R.ok();
    }
    
    /**
     * 获取 微信客服配置
     */
    @GetMapping(value = "/admin/tenantConfig/wxCustomer")
    public R queryTenantConfigWxCustomer() {
        return R.ok(electricityConfigService.queryTenantConfigWxCustomer());
    }
    
    /**
     * 获取 小程序服务信息
     *
     * @return
     */
    @Deprecated
    @GetMapping("admin/servicePhone")
    public R getServicePhone() {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(redisService.get(CacheConstant.CACHE_SERVICE_PHONE + tenantId));
    }
    
    /**
     * 设置小程序服务信息
     *
     * @return
     */
    @Deprecated
    @PostMapping("admin/servicePhone")
    public R getServicePhone(@RequestBody ServicePhoneQuery servicePhoneQuery) {
        // 强制走新的接口
        return R.fail("120152", "网络不佳,请刷新页面重试！");
    }
    
    @GetMapping("admin/servicePhone/all")
    public R getServicePhones() {
        return R.ok(servicePhoneService.listByTenantIdFromCache(TenantContextHolder.getTenantId()));
    }
    
    @PostMapping("admin/servicePhone/update")
    public R insertOrUpdate(@RequestBody @Validated ValidList<ServicePhoneRequest> requestList) {
        return servicePhoneService.insertOrUpdate(requestList);
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
        return electricitySubscriptionMessageService.getElectricitySubscriptionMessagePage(type, tenantId);
    }
    
}
