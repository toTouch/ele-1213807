package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐订单表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Service
@Slf4j
public class CarRentalPackageOrderServiceImpl implements CarRentalPackageOrderService {

    @Resource
    private CarRentalPackageOrderMapper carRentalPackageOrderMapper;

    /**
     * 支付成功订单的总计剩余时间，退租使用<br />
     * 此方法使用慎重
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return 支付成功订单的总计剩余时间
     */
    @Slave
    @Override
    public Long dueTimeTotal(Integer tenantId, Long uid) {

        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(uid);
        qryModel.setPayState(PayStateEnum.SUCCESS.getCode());
        qryModel.setUseState(UseStateEnum.UN_USED.getCode());

        List<CarRentalPackageOrderPo> packageOrderPoList = carRentalPackageOrderMapper.list(qryModel);
        if (CollectionUtils.isEmpty(packageOrderPoList)) {
            return null;
        }

        long dueTimeTotal = 0L;

        for (CarRentalPackageOrderPo carRentalPackageOrderPo : packageOrderPoList) {

            Integer tenancy = carRentalPackageOrderPo.getTenancy();
            Integer tenancyUnit = carRentalPackageOrderPo.getTenancyUnit();

            long currDueTimeTotal = 0L;

            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                currDueTimeTotal = tenancy * TimeConstant.DAY_MILLISECOND;
            }

            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                currDueTimeTotal = tenancy * TimeConstant.MINUTE_MILLISECOND;

            }
            dueTimeTotal = dueTimeTotal + currDueTimeTotal;
        }

        return dueTimeTotal == 0L ? null : dueTimeTotal;
    }

    /**
     * 根据用户UID查询支付成功的总金额际支付金额)
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 总金额
     */
    @Slave
    @Override
    public BigDecimal selectPaySuccessAmountTotal(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        BigDecimal paySuccessAmountTotal = carRentalPackageOrderMapper.selectPaySuccessAmountTotal(tenantId, uid);

        if (ObjectUtils.isEmpty(paySuccessAmountTotal)) {
            paySuccessAmountTotal = BigDecimal.ZERO;
        }
        return paySuccessAmountTotal;
    }

    /**
     * 根据用户ID查找最后一条未支付成功的购买记录信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 购买订单信息
     */
    @Slave
    @Override
    public CarRentalPackageOrderPo selectLastUnPayByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderMapper.selectLastUnPayByUid(tenantId, uid);
    }

    /**
     * 根据用户ID查询第一条未使用的支付成功的订单信息
     *
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 套餐购买订单
     */
    @Slave
    @Override
    public CarRentalPackageOrderPo selectFirstUnUsedAndPaySuccessByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderMapper.selectFirstUnUsedAndPaySuccessByUid(tenantId, uid);
    }

    /**
     * 根据用户ID查找最后一条支付成功的购买记录信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 购买订单信息
     */
    @Slave
    @Override
    public CarRentalPackageOrderPo selectLastPaySuccessByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderMapper.selectLastPaySuccessByUid(tenantId, uid);
    }

    /**
     * 根据用户ID进行退押操作<br />
     * 将使用中、未使用的订单全部设置为已失效
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param optId    操作人ID（可为空）
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean refundDepositByUid(Integer tenantId, Long uid, Long optId) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer num = carRentalPackageOrderMapper.refundDepositByUid(tenantId, uid, optId, System.currentTimeMillis());

        return num >= 0;
    }

    /**
     * 根据订单编号更改支付状态、使用状态、使用时间
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @param useState 使用状态
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updateStateByOrderNo(String orderNo, Integer payState, Integer useState) {
        if (!ObjectUtils.allNotNull(orderNo, payState, useState) || !BasicEnum.isExist(payState, PayStateEnum.class) || !BasicEnum.isExist(payState, UseStateEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        Integer num = carRentalPackageOrderMapper.updateStateByOrderNo(orderNo, payState, useState, System.currentTimeMillis());
        return num >= 0;
    }

    /**
     * 根据用户ID查询未使用状态的订单总条数<br />
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 总数
     */
    @Slave
    @Override
    public Integer countByUnUseByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderMapper.countByUnUseByUid(tenantId, uid);
    }

    /**
     * 根据用户ID查询是否存在未使用且可退的订单<br />
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param rentRebateEndTime      可退截止时间，可为空，若为空，则默认取当前时间
     * @return true(存在未使用的订单)、false(不存在未使用的订单)
     */
    @Slave
    @Override
    public boolean isExitUnUseAndRefund(Integer tenantId, Long uid, Long rentRebateEndTime) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        if (ObjectUtils.isEmpty(rentRebateEndTime)) {
            rentRebateEndTime = System.currentTimeMillis();
        }
        Integer count = carRentalPackageOrderMapper.isExitUnUseAndRefund(tenantId, uid, rentRebateEndTime);
        return count > 0;
    }

    /**
     * 根据用户ID查询是否存在未使用状态的订单<br />
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return true(存在未使用的订单)、false(不存在未使用的订单)
     */
    @Slave
    @Override
    public boolean isExitUnUseByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        Integer count = carRentalPackageOrderMapper.countByUnUseByUid(tenantId, uid);
        return count > 0;
    }

    /**
     * 根据套餐ID查询是否存在购买订单
     *
     * @param rentalPackageId 套餐ID
     * @return true(存在)、false(不存在)
     */
    @Slave
    @Override
    public Boolean checkByRentalPackageId(Long rentalPackageId) {
        if (ObjectUtils.isEmpty(rentalPackageId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer num = carRentalPackageOrderMapper.countByRentalPackageId(rentalPackageId);

        return num > 0;
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderPo> list(CarRentalPackageOrderQryModel qryModel) {
        return carRentalPackageOrderMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询条件模型
     * @return 套餐购买订单集
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderPo> page(CarRentalPackageOrderQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderQryModel();
        }

        return carRentalPackageOrderMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询条件模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageOrderQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderQryModel();
        }

        return carRentalPackageOrderMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return 套餐购买订单
     */
    @Slave
    @Override
    public CarRentalPackageOrderPo selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }

        return carRentalPackageOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 套餐购买订单
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderPo> selectById(Long id) {
        if (id == null || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderMapper.selectById(id));
    }

    /**
     * 根据订单编号更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updatePayStateByOrderNo(String orderNo, Integer payState) {
        return updatePayStateByOrderNo(orderNo, payState, null, null);
    }

    /**
     * 根据订单编码更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @param remark   备注
     * @param uid      操作人
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long uid) {
        if (StringUtils.isBlank(orderNo) || !BasicEnum.isExist(payState, PayStateEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updatePayStateByOrderNo(orderNo, payState, remark, uid, now);

        return num >= 0;
    }

    /**
     * 根据ID更新支付状态
     *
     * @param id       主键ID
     * @param payState 支付状态
     * @param remark   备注
     * @param uid      操作人
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updatePayStateById(Long id, Integer payState, String remark, Long uid) {
        if (!ObjectUtils.allNotNull(id, payState) || !BasicEnum.isExist(payState, PayStateEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updatePayStateById(id, payState, remark, uid, now);

        return num >= 0;
    }

    /**
     * 根据ID更新使用状态
     *
     * @param orderNo  订单编码
     * @param useState 使用状态
     * @param optUid      操作人ID（可为空）
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updateUseStateByOrderNo(String orderNo, Integer useState, Long optUid) {
        if (!ObjectUtils.allNotNull(orderNo, useState) || !BasicEnum.isExist(useState, UseStateEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updateUseStateByOrderNo(orderNo, useState, optUid, now);

        return num >= 0;
    }

    /**
     * 根据ID更新使用状态
     *
     * @param id       主键ID
     * @param useState 使用状态
     * @param uid      操作人
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updateUseStateById(Long id, Integer useState, Long uid) {
        if (!ObjectUtils.allNotNull(id, useState) || !BasicEnum.isExist(useState, UseStateEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updateUseStateById(id, useState, uid, now);

        return num >= 0;
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return 主键ID
     */
    @Override
    public Long insert(CarRentalPackageOrderPo entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人、时间、删除标记、订单编号
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        if (StringUtils.isBlank(entity.getOrderNo())) {
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_MEMBERCARD, entity.getUid()));
        }

        carRentalPackageOrderMapper.insert(entity);

        return entity.getId();
    }
}
