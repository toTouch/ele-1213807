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
     * 获取用户可以购买的套餐
     * @param qryReq 查询模型
     * @param uid 用户ID
     * @return 可购买的套餐数据集
     */
    List<CarRentalPackagePO> queryCanPurchasePackage(CarRentalPackageQryReq qryReq, Long uid);

    /**
     * 根据套餐ID删除套餐信息
     * @param packageId 套餐ID
     * @param optId 操作人ID
     * @return true(成功)、false(失败)
     */
    boolean delPackageById(Long packageId, Long optId);

    /**
     * 新增套餐
     * @param optModel 操作数据模型
     * @return true(成功)、false(失败)
     */
    boolean insertPackage(CarRentalPackageOptModel optModel);

    /**
     * 计算需要支付的金额
     * @param amount 原金额
     * @param userCouponIds 用户的优惠券ID集合
     * @param uid 用户ID
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的优惠券ID、Boolean（暂无实际意义）
     */
    Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid);

}
