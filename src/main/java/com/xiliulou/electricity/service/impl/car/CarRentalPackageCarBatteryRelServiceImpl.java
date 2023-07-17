package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPO;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageCarBatteryRelMapper;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐车辆电池关联表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageCarBatteryRelServiceImpl implements CarRentalPackageCarBatteryRelService {

    @Resource
    private CarRentalPackageCarBatteryRelMapper carRentalPackageCarBatteryRelMapper;

    /**
     * 根据套餐ID删除(逻辑删除)
     *
     * @param rentalPackageId 套餐ID
     * @param optId 操作人ID
     * @return
     */
    @Override
    public boolean delByRentalPackageId(Long rentalPackageId, Long optId) {
        if (!ObjectUtils.allNotNull(rentalPackageId, optId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        carRentalPackageCarBatteryRelMapper.delByRentalPackageId(rentalPackageId, optId, System.currentTimeMillis());
        return true;
    }

    /**
     * 根据套餐ID查询
     *
     * @param rentalPackageId 套餐ID
     * @return
     */
    @Override
    public List<CarRentalPackageCarBatteryRelPO> selectByRentalPackageId(Long rentalPackageId) {
        if (ObjectUtils.isEmpty(rentalPackageId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageCarBatteryRelMapper.selectByRentalPackageId(rentalPackageId);
    }

    /**
     * 批量新增
     *
     * @param entityList
     * @return
     */
    @Override
    public boolean batchInsert(List<CarRentalPackageCarBatteryRelPO> entityList) {
        if (ObjectUtils.isEmpty(entityList)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        carRentalPackageCarBatteryRelMapper.batchInsert(entityList);

        return true;
    }
}
