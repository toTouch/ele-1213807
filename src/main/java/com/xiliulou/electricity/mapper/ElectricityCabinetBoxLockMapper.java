package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import org.apache.ibatis.annotations.Param;


public interface ElectricityCabinetBoxLockMapper {


    void insertEleLockBox(@Param("boxLock") ElectricityCabinetBoxLock boxLock);

    ElectricityCabinetBoxLock selectBoxLockByEidAndCell(@Param("eid") Integer eid,@Param("cellNo") String cellNo);

    void updateEleLockBox(ElectricityCabinetBoxLock updateBoxLock);
}
