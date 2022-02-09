package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryService extends IService<ElectricityBattery> {


    R saveElectricityBattery(ElectricityBattery electricityBattery);

    R update(ElectricityBattery electricityBattery);

    R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size);

    R queryById(Long electricityBatteryId);

    R deleteElectricityBattery(Long id);

    ElectricityBattery queryByBindSn(String initElectricityBatterySn);

    ElectricityBattery queryByUid(Long uid);

    ElectricityBattery queryBySn(String oldElectricityBatterySn);

    Integer updateByOrder(ElectricityBattery electricityBattery);

	R queryCount(ElectricityBatteryQuery electricityBatteryQuery);

    void handlerBatteryNotInCabinetWarning();

    R batteryOutTimeInfo(Long uid);

    void handlerLowBatteryReminder();

    R queryNotBindList(Long offset, Long size,Integer franchiseeId);

    void insert(ElectricityBattery electricityBattery);

    ElectricityBatteryVO queryInfoByUid(Long uid);
}
