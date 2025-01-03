package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.exception.BizException;
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
     * @param tenantId  租户id
     * @param days      申请冻结天数
     * @param uid       申请冻结用户uid
     * @param hasAssets 有无资产，true-有资产；false-无资产
     * @return 自动审核校验结果，true-自动审核，false-人工审核
     * @throws BizException 若捕获到BizException，直接将其code、message组合成失败结果返回给前端即可
     */
    Boolean checkFreezeAutoReviewAndDays(Integer tenantId, Integer days, Long uid, boolean hasAssets) throws BizException;
}
