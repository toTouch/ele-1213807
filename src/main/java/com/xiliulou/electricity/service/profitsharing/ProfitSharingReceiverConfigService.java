/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/23
 */

package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigQryRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigStatusOptRequest;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigDetailsVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigVO;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 14:00
 */
public interface ProfitSharingReceiverConfigService {
    
    
    /**
     * 租户id+分账主表配置id
     *
     * @param tenantId
     * @param profitSharingConfigId
     * @author caobotao.cbt
     * @date 2024/8/23 14:40
     */
    List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigId(Integer tenantId, Long profitSharingConfigId);
    
    
    /**
     * 租户id+分账主表配置id集合
     *
     * @param tenantId
     * @param profitSharingConfigIds
     * @author caobotao.cbt
     * @date 2024/8/23 14:40
     */
    List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigIds(Integer tenantId, List<Long> profitSharingConfigIds);
    
    /**
     * insert
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/23 16:28
     */
    void insert(ProfitSharingReceiverConfigOptRequest request);
    
    /**
     * 获取骑手小程序openid
     *
     * @param phone
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/26 11:03
     */
    String queryWxMiniOpenIdByPhone(String phone, Integer tenantId);
    
    /**
     * 配置更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 11:16
     */
    void update(ProfitSharingReceiverConfigOptRequest request);
    
    /**
     * 状态更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 13:51
     */
    void updateStatus(ProfitSharingReceiverConfigStatusOptRequest request);
    
    /**
     * 逻辑删除
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/26 13:59
     */
    void removeById(Integer tenantId, Long id);
    
    
    /**
     * 详情查询
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/26 14:16
     */
    ProfitSharingReceiverConfigDetailsVO queryDetailsById(Integer tenantId, Long id);
    
    /**
     * 分页
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 14:35
     */
    List<ProfitSharingReceiverConfigVO> pageList(ProfitSharingReceiverConfigQryRequest request);
    
    /**
     * 分页统计
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 15:34
     */
    Integer count(ProfitSharingReceiverConfigQryRequest request);
}
