package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
     * 根据套餐ID查询是否存在购买订单
     *
     * @param rentalPackageId
     * @return
     */
    @Slave
    @Override
    public R<Boolean> checkByRentalPackageId(Long rentalPackageId) {
        if (rentalPackageId == null || rentalPackageId <=0 ) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer num = carRentalPackageOrderMapper.countByRentalPackageId(rentalPackageId);

        return R.ok(num > 0);
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询条件模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageOrderPO>> list(CarRentalPackageOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询条件模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderPO> page(CarRentalPackageOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询条件模型
     * @return
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageOrderPO selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }

        return carRentalPackageOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderPO> selectById(Long id) {
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
     * @return
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
     * @return
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
     * @return
     */
    @Override
    public R<Boolean> updatePayStateById(Long id, Integer payState, String remark, Long uid) {
        if (id == null || id <= 0 || payState == null) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(payState, PayStateEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updatePayStateById(id, payState, remark, uid, now);

        return R.ok(num >= 0);
    }

    /**
     * 根据ID更新使用状态
     *
     * @param orderNo  订单编码
     * @param useState 使用状态
     * @param uid      操作人
     * @return
     */
    @Override
    public R<Boolean> updateUseStateByOrderNo(String orderNo, Integer useState, Long uid) {
        if (StringUtils.isBlank(orderNo) || useState == null) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(useState, PayStateEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updateUseStateByOrderNo(orderNo, useState, uid, now);

        return R.ok(num >= 0);
    }

    /**
     * 根据ID更新使用状态
     *
     * @param id       主键ID
     * @param useState 使用状态
     * @param uid      操作人
     * @return
     */
    @Override
    public R<Boolean> updateUseStateById(Long id, Integer useState, Long uid) {
        if (id == null || id <= 0 || useState == null) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(useState, UseStateEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updateUseStateById(id, useState, uid, now);

        return R.ok(num >= 0);
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return
     */
    @Override
    public Long insert(CarRentalPackageOrderPO entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        carRentalPackageOrderMapper.insert(entity);

        return entity.getId();
    }
}
