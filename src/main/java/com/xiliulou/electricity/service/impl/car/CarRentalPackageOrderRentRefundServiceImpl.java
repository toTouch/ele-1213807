package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderRentRefundMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐订单租金退款表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderRentRefundServiceImpl implements CarRentalPackageOrderRentRefundService {

    @Resource
    private CarRentalPackageOrderRentRefundMapper carRentalPackageOrderRentRefundMapper;

    /**
     * 根据用户UID查询退款成功的总金额
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 总金额
     */
    @Slave
    @Override
    public BigDecimal selectRefundSuccessAmountTotal(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        BigDecimal refundSuccessAmountTotal = carRentalPackageOrderRentRefundMapper.selectRefundSuccessAmountTotal(tenantId, uid);

        if (ObjectUtils.isEmpty(refundSuccessAmountTotal)) {
            refundSuccessAmountTotal = BigDecimal.ZERO;
        }
        return refundSuccessAmountTotal;
    }

    /**
     * 根据退租申请单编码进行更新
     *
     * @param entity 实体数据
     * @return
     */
    @Override
    public boolean updateByOrderNo(CarRentalPackageOrderRentRefundPo entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        entity.setUpdateTime(System.currentTimeMillis());

        int num = carRentalPackageOrderRentRefundMapper.updateByOrderNo(entity);

        return num >= 0;
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderRentRefundPo> list(CarRentalPackageOrderRentRefundQryModel qryModel) {
        return carRentalPackageOrderRentRefundMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderRentRefundPo> page(CarRentalPackageOrderRentRefundQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderRentRefundQryModel();
        }

        return carRentalPackageOrderRentRefundMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageOrderRentRefundQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderRentRefundQryModel();
        }

        return carRentalPackageOrderRentRefundMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageOrderRentRefundPo selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderRentRefundMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageOrderRentRefundPo selectById(Long id) {
        if (null == id || id <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderRentRefundMapper.selectById(id);
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return
     */
    @Override
    public Long insert(CarRentalPackageOrderRentRefundPo entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人、时间、删除标记、订单编号
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        if (StringUtils.isBlank(entity.getOrderNo())) {
            String orderNo = OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_CAR_MEMBERCARD, entity.getUid());
            entity.setOrderNo(orderNo);
        }

        // 保存入库
        carRentalPackageOrderRentRefundMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 根据购买订单号，查询退租订单信息
     * @param rentalPackageOrderNo 购买订单号
     * @return
     */
    @Override
    public CarRentalPackageOrderRentRefundPo selectLatestByPurchaseOrderNo(String rentalPackageOrderNo) {
        return carRentalPackageOrderRentRefundMapper.selectLatestByPurchaseOrderNo(rentalPackageOrderNo);
    }
}
