package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindFranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;

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

    R delete(Integer id);

    Franchisee queryByIdFromCache(Integer id);

    R queryList(FranchiseeQuery franchiseeQuery);

    R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery);


    R getElectricityBatteryList(Integer id);


    Franchisee queryByUid(Long uid);

    Franchisee queryByCid(Integer cid);
}
