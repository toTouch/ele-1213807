package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.ElectricityBatteryBind;

/**
 * (ElectricityBatteryBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityBatteryBindService {

    void deleteByFranchiseeId(Long franchiseeId);

    void insert(ElectricityBatteryBind electricityBatteryBind);
}