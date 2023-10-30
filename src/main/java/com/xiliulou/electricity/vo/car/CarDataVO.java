package com.xiliulou.electricity.vo.car;

import com.google.common.collect.Lists;
import com.xiliulou.electricity.entity.car.CarDataEntity;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 车辆运营数据
 */
@Data
public class CarDataVO {

    private Long uid;

    private String carSn;  // 车辆序列号

    private String model;  // 车辆型号

    private String franchiseeName;  // 加盟商名称

    private String storeName;  // 门店名称

    private String userName;  // 用户名称

    private String phone;  // 用户电话

    private String carStatus;  // 车辆状态

    private String batterySn;  // 电池序列号

    private double power;  // 电池电量

    private double voltage;  // 电池电压

    private Long updateTime;  // 更新时间

    private Long createTime;  // 创建时间

    private Long dueTime; // 套餐到期时间

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    public static CarDataVO carDataEntityToCarDataVO(CarDataEntity carDataEntity) {
        if (carDataEntity == null) {
            return null;
        }
        CarDataVO carDataVO = new CarDataVO();
        carDataVO.setUid(carDataEntity.getUid());
        carDataVO.setCarSn(carDataEntity.getCarSn());
        carDataVO.setModel(carDataEntity.getModel());
        carDataVO.setFranchiseeName(carDataEntity.getFranchiseeName());
        carDataVO.setStoreName(carDataEntity.getStoreName());
        carDataVO.setUserName(carDataEntity.getUserName());
        carDataVO.setPhone(carDataEntity.getPhone());
        carDataVO.setCarStatus(carDataEntity.getCarStatus());
        carDataVO.setUpdateTime(carDataEntity.getUpdateTime());
        carDataVO.setCreateTime(carDataEntity.getCreateTime());
        carDataVO.setDueTime(carDataEntity.getDueTime());
        carDataVO.setLongitude(carDataEntity.getLongitude());
        carDataVO.setLatitude(carDataEntity.getLatitude());
        return carDataVO;
    }

    public static List<CarDataVO> carDataEntityListToCarDataVOList(List<CarDataEntity> carDataEntityList) {
        if (CollectionUtils.isEmpty(carDataEntityList)) {
            return Lists.newArrayList();
        }
        List<CarDataVO> carDataVOList = Lists.newArrayList();
        for (CarDataEntity carDataEntity : carDataEntityList) {
            carDataVOList.add(carDataEntityToCarDataVO(carDataEntity));
        }
        return carDataVOList;
    }


}
