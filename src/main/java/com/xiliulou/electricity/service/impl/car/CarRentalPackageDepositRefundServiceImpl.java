package com.xiliulou.electricity.service.impl.car;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.mapper.car.CarRentalPackageDepositRefundMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;

import lombok.extern.slf4j.Slf4j;

/**
 * 租车套餐押金退款表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageDepositRefundServiceImpl implements CarRentalPackageDepositRefundService {

    @Resource
    private CarRentalPackageDepositRefundMapper carRentalPackageDepositRefundMapper;

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageDepositRefundPO>> list(CarRentalPackageDepositRefundQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositRefundMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageDepositRefundPO>> page(CarRentalPackageDepositRefundQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositRefundMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageDepositRefundQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositRefundMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageDepositRefundPO> selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositRefundMapper.selectByOrderNo(orderNo));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Override
    public R<CarRentalPackageDepositRefundPO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositRefundMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Long> insert(CarRentalPackageDepositRefundOptModel optModel) {
        CarRentalPackageDepositRefundPO entity = new CarRentalPackageDepositRefundPO();
        BeanUtils.copyProperties(optModel, entity);

        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        // 保存入库
        carRentalPackageDepositRefundMapper.insert(entity);

        return R.ok(entity.getId());
    }
}
