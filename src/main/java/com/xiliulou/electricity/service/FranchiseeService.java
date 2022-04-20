package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeSetSplitQuery;

import java.util.List;

/**
 * (Franchisee)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeService {

    R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate);

    R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate);

    R delete(Long id);

    Franchisee queryByIdFromDB(Long id);

    R queryList(FranchiseeQuery franchiseeQuery);

    R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery);

    R getElectricityBatteryList(Long id);

    Franchisee queryByUid(Long uid);

	R queryCount(FranchiseeQuery franchiseeQuery);

    void deleteByUid(Long uid);

	Integer queryByFanchisee(Long uid);

	R setSplit(List<FranchiseeSetSplitQuery> franchiseeSetSplitQueryList);

    Franchisee queryByElectricityBatteryId(Long id);

	R queryByTenantId(Integer tenantId);

	R queryByCabinetId(Integer cabinetId);


}
