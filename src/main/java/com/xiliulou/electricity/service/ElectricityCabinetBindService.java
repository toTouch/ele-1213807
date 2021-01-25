package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;

/**
 * (ElectricityCabinetBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetBindService {

    void deleteByUid(Long id);

    void insert(ElectricityCabinetBind electricityCabinetBind);

}