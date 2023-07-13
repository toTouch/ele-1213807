package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderFreezeMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐订单冻结表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderFreezeServiceImpl implements CarRentalPackageOrderFreezeService {

    @Resource
    private CarRentalPackageOrderFreezeMapper carRentalPackageOrderFreezeMapper;

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageOrderFreezePO>> list(CarRentalPackageOrderFreezeQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderFreezeMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageOrderFreezePO>> page(CarRentalPackageOrderFreezeQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderFreezeMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageOrderFreezeQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderFreezeMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderFreezePO> selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderFreezeMapper.selectByOrderNo(orderNo));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderFreezePO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageOrderFreezeMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 实体模型
     * @return
     */
    @Override
    public Long insert(CarRentalPackageOrderFreezePO entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        if (StringUtils.isBlank(entity.getOrderNo())) {
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_STAGNATE, entity.getUid()));
        }

        // 保存入库
        carRentalPackageOrderFreezeMapper.insert(entity);

        return entity.getId();
    }
}
