package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.EleBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryService extends IService<ElectricityBattery> {


    R saveElectricityBattery(EleBatteryQuery electricityBattery);

    Integer update(ElectricityBattery electricityBattery);
    
    R updateForAdmin(EleBatteryQuery electricityBattery);

    R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size);

    R queryById(Long electricityBatteryId);

    R deleteElectricityBattery(Long id, Integer isNeedSync);

    ElectricityBattery queryByBindSn(String initElectricityBatterySn);

    ElectricityBattery queryByUid(Long uid);

    ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn);
    
    ElectricityBattery queryPartAttrBySnFromCache(String sn);
    
    
    ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn, Integer tenantId);

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
    
    BigEleBatteryVo queryMaxPowerByElectricityCabinetId(Integer electricityCabinetId);

    ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn);

    List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery);

    R batteryStatistical(ElectricityBatteryQuery electricityBatteryQuery);

    R bindFranchisee(BindElectricityBatteryQuery bindElectricityBatteryQuery);

    List<ElectricityBattery> selectByBatteryIds(List<Long> batteryIds);

    ElectricityBattery selectByBatteryIdAndFranchiseeId(Long batteryId,Long franchiseeId);
    
    List<ElectricityBattery> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery);
    
    boolean checkBatteryIsExchange(String batteryName, Double fullyCharged);

    Integer isFranchiseeBindBattery(Long id,Integer tenantId);

    Triple<Boolean, String, Object> selectUserLatestBatteryType();

    Triple<Boolean, String, Object> queryBatteryInfoBySn(String sn);

    Triple<Boolean, String, Object> queryBatteryMapList(Integer offset, Integer size, List<Long> franchiseeIds);


    Integer isUserBindBattery(Long uid, Integer tenantId);

    Integer insertBatch(List<ElectricityBattery> saveList);
}
