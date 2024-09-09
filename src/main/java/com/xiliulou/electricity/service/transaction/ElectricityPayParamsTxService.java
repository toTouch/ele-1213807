/**
 *  Create date: 2024/6/13
 */

package com.xiliulou.electricity.service.transaction;

import com.xiliulou.electricity.entity.ElectricityPayParams;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/13 10:23
 */
public interface ElectricityPayParamsTxService {
    
    /**
     * 删除事务
     *
     * @param id
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/6/13 10:24
     */
    void delete(Long id, Integer tenantId);
    
    /**
     * 更新事务
     *
     * @param update
     * @param franchiseePayParamIds
     * @author caobotao.cbt
     * @date 2024/6/13 17:07
     */
    void update(ElectricityPayParams update, List<Integer> franchiseePayParamIds);
}