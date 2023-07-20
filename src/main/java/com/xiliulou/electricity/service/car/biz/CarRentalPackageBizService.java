package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租赁套餐相关的业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageBizService {

    /**
     * 根据套餐ID删除套餐信息
     * @param packageId 套餐ID
     * @param optId 操作人ID
     * @return
     */
    boolean delPackageById(Long packageId, Long optId);

    /**
     * 新增套餐
     * @param optModel
     * @return
     */
    boolean insertPackage(CarRentalPackageOptModel optModel);

    /**
     * 根据车辆型号、用户ID、租户ID获取C端能够展示购买的套餐
     * @param qryReq 查询模型
     * @param uid 用户ID
     */
    List<CarRentalPackagePO> queryByCarModel(CarRentalPackageQryReq qryReq, Long uid);

    /**
     * 计算需要支付的金额
     * @param amount 原金额
     * @param userCouponIds 用户的优惠券ID集合
     * @param uid 用户ID
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的优惠券ID、Boolean（暂无实际意义）
     */
    Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid);

}
