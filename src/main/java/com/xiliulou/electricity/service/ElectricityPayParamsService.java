package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import com.xiliulou.electricity.vo.FranchiseeVO;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface ElectricityPayParamsService extends IService<ElectricityPayParams> {
    
    
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
     * 根据租户id + 加盟商id查询缓存<br/>
     * <p>
     * 1.franchiseeId 不存在配置,则返回运营商默认配置<br/> 2.franchiseeId 存在配置,则返回加盟商配置<br/> 3.如果要查询运营商默认配置，franchiseeId传{@link com.xiliulou.electricity.constant.MultiFranchiseeConstant#DEFAULT_FRANCHISEE}
     * </p>
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 11:16
     */
    ElectricityPayParams queryCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    
    /**
     * 根据租户id + 加盟商id查询缓存<br/>
     * <p>
     * 精确查询配置,传入的franchiseeId是什么，就查询franchiseeId对应的配置。
     * </p>
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/18 09:19
     */
    ElectricityPayParams queryPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    
    /**
     * 根据租户id + 加盟商id集合查询缓存<br/>
     * <p>
     * 精确查询配置,传入的franchiseeId是什么，就查询franchiseeId对应的配置。
     * </p>
     *
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/6/18 09:19
     */
    List<ElectricityPayParams> queryListPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Set<Long> franchiseeIds);
    
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
    
    /**
     * 聚合租户加盟商
     *
     * @param tenantId
     * @return
     * @author caobotao.cbt
     * @date 2024/8/22 17:52
     */
    List<FranchiseeVO> queryFranchisee(Integer tenantId);
    
    /**
     * 根据租户id+商户号查询
     *
     * @param tenantId
     * @param wechatMerchantId
     * @author caobotao.cbt
     * @date 2024/8/26 10:10
     */
    ElectricityPayParams queryByWechatMerchantId(Integer tenantId, String wechatMerchantId);
}
