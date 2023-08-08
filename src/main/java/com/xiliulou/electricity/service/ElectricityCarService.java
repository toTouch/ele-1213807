package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.query.ElectricityCarAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCarBindUser;
import com.xiliulou.electricity.query.ElectricityCarMoveQuery;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.service.impl.car.biz.CarRentalOrderBizServiceImpl;

import java.util.List;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarService {

    /**
     * 根据用户ID查询车辆信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 车辆信息
     */
    ElectricityCar selectByUid(Integer tenantId, Long uid);

    /**
     * 根据车辆型号ID判定，是否存在未租车辆
     * @param carModelId 车辆型号ID
     * @return true(存在)、false(不存在)
     */
    boolean checkUnleasedByCarModelId(Integer carModelId);

    /**
     * 根据车辆型号ID，判定是否进行绑定
     * @param carModelId 车辆型号ID
     * @return true(绑定)、false(未绑定)
     */
    boolean checkBindingByCarModelId(Integer carModelId);

    /**
     * 根据 uid 查询车辆信息<br />
     * 复合查询，车辆、门店、车辆经纬度
     * @param tenantId 租户ID
     * @param carId 车辆ID
     * @return
     */
    CarInfoDO queryByCarId(Integer tenantId, Long carId);

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

    /**
     * 废弃此方法 <br />
     * 替代方法：{@link CarRentalOrderBizServiceImpl#unBindingCar}
     */
    @Deprecated
    R bindUser(ElectricityCarBindUser electricityCarBindUser);

    /**
     * 废弃此方法 <br />
     * 替代方法：{@link CarRentalOrderBizServiceImpl#bindingCar}
     */
    @Deprecated
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
    
    R queryElectricityCarMove(Long storeId, String sn, Long size, Long offset);
    
    R electricityCarMove(ElectricityCarMoveQuery electricityCarMoveQuery);
    
    Boolean retryCarLockCtrl(String str, Integer lockType, Integer retryCount);
}
