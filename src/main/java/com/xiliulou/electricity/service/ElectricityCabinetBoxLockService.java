package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;

/**
 * 换电柜仓门表(TElectricityCabinetBoxLock)表服务接口
 *
 * @author renhang
 */
public interface ElectricityCabinetBoxLockService {

    void insertElectricityCabinetBoxLock(ElectricityCabinetBoxLock electricityCabinetBoxLock);

    void updateElectricityCabinetBoxLock(Integer eid, String cellNo);
}
