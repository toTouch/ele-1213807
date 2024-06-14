package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ElectricityPayParamsService extends IService<ElectricityPayParams> {
    
    @Deprecated
    R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams);
    
    
    @Deprecated
    ElectricityPayParams queryFromCache(Integer tenantId);
    
    
    R getTenantId(String appId);
    
    /**
     * 上传支付证书
     *
     * @param file file
     * @param type type
     * @return R
     */
    R uploadFile(MultipartFile file, Integer type, Long franchiseeId);
    
    
    ElectricityPayParams selectTenantId(String appId);
    
    @Deprecated
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
    
    /**
     * 新增
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 16:18
     */
    R insert(ElectricityPayParamsRequest request);
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 17:09
     */
    R update(ElectricityPayParamsRequest request);
    
    /**
     * 配置删除
     *
     * @param id
     * @author caobotao.cbt
     * @date 2024/6/12 17:55
     */
    R delete(Long id);
    
    /**
     * 根据租户id查询
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/6/12 18:09
     */
    List<ElectricityPayParamsVO> queryByTenantId(Integer tenantId);
}
