package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import com.xiliulou.electricity.query.exchange.ElectricityCabinetBoxLockPageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ElectricityCabinetBoxLockMapper {


    void insertEleLockBox(@Param("boxLock") ElectricityCabinetBoxLock boxLock);

    ElectricityCabinetBoxLock selectBoxLockByEidAndCell(@Param("eid") Integer eid, @Param("cellNo") Integer cellNo);

    void updateEleLockBox(@Param("boxLock") ElectricityCabinetBoxLock updateBoxLock);

    List<ElectricityCabinetBoxLock> listCabinetBoxLock(ElectricityCabinetBoxLockPageQuery query);

    Long countCabinetBoxLock(ElectricityCabinetBoxLockPageQuery query);
}
