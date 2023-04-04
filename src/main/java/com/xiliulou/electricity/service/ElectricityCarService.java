package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.CarGpsVo;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;

import java.util.List;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarService {

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCar queryByIdFromCache(Integer id);

    R save(ElectricityCarAddAndUpdate electricityCarAddAndUpdate);

    R edit(ElectricityCarAddAndUpdate electricityCarAddAndUpdate);

    R delete(Integer id);

    R queryList(ElectricityCarQuery electricityCarQuery);

    Integer queryByModelId(Integer id);

    R queryCount(ElectricityCarQuery electricityCarQuery);

    R bindUser(ElectricityCarBindUser electricityCarBindUser);

    R unBindUser(ElectricityCarBindUser electricityCarBindUser);

    ElectricityCar queryInfoByUid(Long uid);

    Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds);


    ElectricityCar selectBySn(String sn, Integer tenantId);

    Integer update(ElectricityCar updateElectricityCar);

    Integer carUnBindUser(ElectricityCar updateElectricityCar);
    
    Integer updateLockTypeByIds(List<Long> tempIds, Integer typeLock);
    
    Boolean carLockCtrl(String str, Integer lockType);
    
    R positionReport(CarPositionReportQuery carPositionReportQuery);
    
    List<ElectricityCar> queryByStoreIds(List<Long> storeIds);
    
    R queryElectricityCarOverview(String sn, List<Integer> franchiseeIds);
    
    R batteryStatistical(List<Integer> carIdList, Integer tenantId);

    R attrList(Long beginTime, Long endTime);

    CarAttr queryLastReportPointBySn(String sn);

    Integer isUserBindCar(Long uid, Integer tenantId);
    
    Boolean retryCarLockCtrl(String str, Integer lockType, Integer retryCount);
}
