package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageDepositRefundMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
     * 根据退押申请单编码进行更新
     *
     * @param entity 实体数据
     * @return true/false
     */
    @Override
    public boolean updateByOrderNo(CarRentalPackageDepositRefundPO entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        entity.setUpdateTime(System.currentTimeMillis());

        int num = carRentalPackageDepositRefundMapper.updateByOrderNo(entity);

        return num >= 0;
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return 押金返还订单数据集
     */
    @Slave
    @Override
    public List<CarRentalPackageDepositRefundPO> list(CarRentalPackageDepositRefundQryModel qryModel) {
        return carRentalPackageDepositRefundMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 押金返回订单数据集
     */
    @Slave
    @Override
    public List<CarRentalPackageDepositRefundPO> page(CarRentalPackageDepositRefundQryModel qryModel) {
        if (!ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageDepositRefundMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageDepositRefundQryModel qryModel) {
        if (!ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageDepositRefundMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return 押金返还订单信息
     */
    @Slave
    @Override
    public CarRentalPackageDepositRefundPO selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageDepositRefundMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 押金返还订单信息
     */
    @Slave
    @Override
    public CarRentalPackageDepositRefundPO selectById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageDepositRefundMapper.selectById(id);
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 实体数据
     * @return 主键ID
     */
    @Override
    public Long insert(CarRentalPackageDepositRefundPO entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getTenantId(), entity.getUid())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        // 订单编号
        if (StringUtils.isBlank(entity.getOrderNo())) {
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT_REFUND, entity.getId()));
        }

        // 保存入库
        carRentalPackageDepositRefundMapper.insert(entity);

        return entity.getId();
    }
}
