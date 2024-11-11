package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.query.ElectricityConfigWxCustomerQuery;
import com.xiliulou.electricity.vo.TenantConfigVO;
import org.apache.commons.lang3.tuple.Triple;


/**
 * 用户列表(ElectricityConfig)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface ElectricityConfigService extends IService<ElectricityConfig> {
    
    R edit(ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery);
    
    ElectricityConfig queryFromCacheByTenantId(Integer tenantId);
    
    void insertElectricityConfig(ElectricityConfig electricityConfig);
    
    TenantConfigVO getTenantConfig(String appId);
    
    Triple<Boolean, String, Object> editWxCustomer(ElectricityConfigWxCustomerQuery electricityConfigAddAndUpdateQuery);
    
    void updateTenantConfigWxCustomer(Integer status);
    
    ElectricityConfig queryTenantConfigWxCustomer();
    
    TenantConfigVO queryTenantConfigByAppId(String appId, String appType);
    
    /**
     * 校验是否可自动审核
     *
     * @param tenantId 租户id
     * @param days     申请冻结天数
     * @return true-可自动审核，false-需人工审核
     */
    Boolean checkFreezeAutoReview(Integer tenantId, Integer days);
    
    /**
     * 校验用户端申请冻结时，申请冻结次数与天数是否符合配置
     *
     * @param tenantId 租户id
     * @param count    本月内申请冻结次数
     * @param days     被校验的冻结申请的冻结天数
     * @return 校验结果
     */
    R<Object> checkFreeCountAndDays(Integer tenantId, Integer count, Integer days);
}
