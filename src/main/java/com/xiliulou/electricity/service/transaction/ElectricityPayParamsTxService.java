/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/13
 */

package com.xiliulou.electricity.service.transaction;

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
}