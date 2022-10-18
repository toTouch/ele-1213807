package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;

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

    ElectricityBattery queryBySn(String oldElectricityBatterySn, Integer tenantId);

    Integer updateBatteryById(ElectricityBattery electricityBattery);

    Integer updateBatteryUser(ElectricityBattery electricityBattery);

    Integer updateBatteryStatus(ElectricityBattery electricityBattery);

    R queryCount(ElectricityBatteryQuery electricityBatteryQuery);

    void handlerBatteryNotInCabinetWarning();

    R batteryOutTimeInfo(Long uid);

    void handlerLowBatteryReminder();

    R queryBindListByPage(Long offset, Long size, Long franchiseeId);

    void insert(ElectricityBattery electricityBattery);

    ElectricityBatteryVO queryInfoByUid(Long uid);


    Integer querySumCount(ElectricityBatteryQuery electricityBatteryQuery);

    //BigEleBatteryVo queryMaxPowerByElectricityCabinetId(Integer electricityCabinetId);

    ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn);

    List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery);

    R batteryStatistical(Integer tenantId);

    R bindFranchisee(BindElectricityBatteryQuery bindElectricityBatteryQuery);

    List<ElectricityBattery> selectByBatteryIds(List<Long> batteryIds);

    ElectricityBattery selectByBatteryIdAndFranchiseeId(Long batteryId,Long franchiseeId);
}
