package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.multipart.MultipartFile;

public interface ElectricityPayParamsService extends IService<ElectricityPayParams> {
    
    @Deprecated
    R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams);
    
    
    /**
     * 新增
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 16:18
     */
    R insert(ElectricityPayParamsRequest request);
    
    @Deprecated
    ElectricityPayParams queryFromCache(Integer tenantId);
    
    /**
     * 上传支付证书
     *
     * @param file file
     * @param type type
     * @return R
     */
    R uploadFile(MultipartFile file, Integer type);
    
    
    ElectricityPayParams selectTenantId(String appId);
    
    
    Triple<Boolean, String, Object> queryByMerchantAppId(String appId);
    
    
    /**
     * 根据租户id + 加盟商id查询缓存
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 11:16
     */
    ElectricityPayParams queryCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
}
