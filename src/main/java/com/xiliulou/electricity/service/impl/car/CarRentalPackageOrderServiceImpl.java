package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
    public R<List<CarRentalPackageOrderPO>> page(CarRentalPackageOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询条件模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderPO> selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderMapper.selectByOrderNo(orderNo));
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
     * 根据ID更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @param remark   备注
     * @param uid      操作人
     * @return
     */
    @Override
    public R<Boolean> updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long uid) {
        if (StringUtils.isBlank(orderNo) || payState == null) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        if (!BasicEnum.isExist(payState, PayStateEnum.class)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        long now = System.currentTimeMillis();
        int num = carRentalPackageOrderMapper.updatePayStateByOrderNo(orderNo, payState, remark, uid, now);
        return R.ok(num >= 0);
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
     * @param optModel 操作数据
     * @return
     */
    @Override
    public R<Long> insert(CarRentalPackageOrderOptModel optModel) {
        CarRentalPackageOrderPO entity = new CarRentalPackageOrderPO();
        BeanUtils.copyProperties(optModel, entity);
        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        // 保存入库
        carRentalPackageOrderMapper.insert(entity);
        return R.ok(entity.getId());
    }
}
