

package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigUpdateStatusOptRequest;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 16:46
 */
public interface ProfitSharingConfigService {
    
    
    /**
     * 根据租户id+加盟商id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/22 18:24
     */
    ProfitSharingConfigVO queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    
    /**
     * 根据支付配置id查询
     *
     * @param tenantId
     * @param payParamsId
     * @return
     * @author caobotao.cbt
     * @date 2024/8/22 18:29
     */
    ProfitSharingConfig queryByPayParamsIdFromCache(Integer tenantId, Integer payParamsId);
    
    
    /**
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/22 19:14
     */
    void updateStatus(ProfitSharingConfigUpdateStatusOptRequest request);
    
    
    /**
     * 删除缓存
     *
     * @param tenantId
     * @param payParamsId
     * @author caobotao.cbt
     * @date 2024/8/23 08:47
     */
    void deleteCache(Integer tenantId, Integer payParamsId);
    
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/23 11:27
     */
    void update(ProfitSharingConfigOptRequest request);
    
    
    /**
     * 逻辑删除
     *
     * @param tenantId
     * @param payParamsId
     * @author caobotao.cbt
     * @date 2024/8/23 15:11
     */
    void removeByPayParamId(Integer tenantId, Integer payParamsId);
    
    /**
     * 根据id查询
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/23 16:37
     */
    ProfitSharingConfig queryById(Integer tenantId, Long id);
}