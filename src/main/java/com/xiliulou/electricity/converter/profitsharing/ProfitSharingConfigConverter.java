

package com.xiliulou.electricity.converter.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;

import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 18:33
 */
public class ProfitSharingConfigConverter {
    
    /**
     * 查询转换
     *
     * @author caobotao.cbt
     * @date 2024/8/22 18:34
     */
    public static ProfitSharingConfigVO qryEntityToVo(ProfitSharingConfig profitSharingConfig) {
        if (Objects.isNull(profitSharingConfig)) {
            return null;
        }
        ProfitSharingConfigVO profitSharingConfigVO = new ProfitSharingConfigVO();
        profitSharingConfigVO.setId(profitSharingConfig.getId());
        profitSharingConfigVO.setTenantId(profitSharingConfig.getTenantId());
        profitSharingConfigVO.setFranchiseeId(profitSharingConfig.getFranchiseeId());
        profitSharingConfigVO.setPayParamId(profitSharingConfig.getPayParamId());
        profitSharingConfigVO.setConfigStatus(profitSharingConfig.getConfigStatus());
        profitSharingConfigVO.setOrderType(profitSharingConfig.getOrderType());
        profitSharingConfigVO.setAmountLimit(profitSharingConfig.getAmountLimit());
        profitSharingConfigVO.setProfitSharingType(profitSharingConfig.getProfitSharingType());
        profitSharingConfigVO.setScaleLimit(profitSharingConfig.getScaleLimit());
        profitSharingConfigVO.setCycleType(profitSharingConfig.getCycleType());
        profitSharingConfigVO.setCreateTime(profitSharingConfig.getCreateTime());
        profitSharingConfigVO.setUpdateTime(profitSharingConfig.getUpdateTime());
        return profitSharingConfigVO;
        
    }
    
    /**
     *
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/23 11:31
     */
    public static ProfitSharingConfig optRequestToEntity(ProfitSharingConfigOptRequest request) {
        ProfitSharingConfig profitSharingConfig = new ProfitSharingConfig();
        profitSharingConfig.setId(request.getId());
        profitSharingConfig.setTenantId(request.getTenantId());
        profitSharingConfig.setOrderType(request.getOrderType());
        profitSharingConfig.setAmountLimit(request.getAmountLimit());
        profitSharingConfig.setProfitSharingType(request.getProfitSharingType());
        profitSharingConfig.setScaleLimit(request.getScaleLimit());
        profitSharingConfig.setCycleType(request.getCycleType());
        return profitSharingConfig;
    }
}
