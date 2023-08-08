package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 车辆租赁订单业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalOrderBizServiceImpl implements CarRentalOrderBizService {

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 解绑用户车辆
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean unBindingCar(Integer tenantId, Long uid, Long optUid) {
        // 生成租赁订单
        // 更改用户租赁状态
        // 增加车辆型号的已租数量
        return true;
    }

    /**
     * 给用户绑定车辆
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @param carSn    车辆SN码
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean bindingCar(Integer tenantId, Long uid, String carSn, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, carSn, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询租车会员信
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermPo) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermPo.getStatus())) {
            log.error("bindingCar, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        if (ObjectUtils.isEmpty(memberTermPo.getRentalPackageId())) {
            log.error("bindingCar, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("300037", "该用户下无套餐订单，请先绑定套餐");
        }

        // 通过套餐找到套餐



        // 生成租赁订单
        // 更改用户租赁状态
        // 增加车辆型号的已租数量


        return true;
    }
}
