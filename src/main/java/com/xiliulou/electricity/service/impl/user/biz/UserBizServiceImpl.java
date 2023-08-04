package com.xiliulou.electricity.service.impl.user.biz;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户业务聚合 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class UserBizServiceImpl implements UserBizService {

    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    /**
     * 退押解绑用户信息
     *
     * @param uid  用户ID
     * @param type 操作类型：0-退电、1-退车、2-退车电
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean depositRefundUnbind(Long uid, Integer type) {
        // TODO 实现
        return false;
    }

    /**
     * 是否是老用户<br />
     * 判定规则：用户是否购买成功过租车套餐 or 换电套餐
     * <pre>
     *     true-老用户
     *     false-新用户
     * </pre>
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    public Boolean isOldUser(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询租车套餐购买成功记录
        CarRentalPackageOrderQryModel queryModel = new CarRentalPackageOrderQryModel();
        queryModel.setTenantId(tenantId);
        queryModel.setUid(uid);
        queryModel.setPayState(PayStateEnum.SUCCESS.getCode());
        Integer count = carRentalPackageOrderService.count(queryModel);
        if (count.intValue() > 0) {
            return true;
        }

        // 查询换电套餐购买记录
        Integer num = electricityMemberCardOrderService.selectCountByUid(tenantId, uid, ElectricityMemberCardOrder.STATUS_SUCCESS);
        if (num.intValue() > 0) {
            return true;
        }

        return false;
    }
}
