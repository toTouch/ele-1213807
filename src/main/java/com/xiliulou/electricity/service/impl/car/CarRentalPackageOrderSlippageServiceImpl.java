package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderSlippageMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐订单逾期表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderSlippageServiceImpl implements CarRentalPackageOrderSlippageService {

    @Resource
    private CarRentalPackageOrderSlippageMapper carRentalPackageOrderSlippageMapper;

    /**
     * 根据主键ID进行更新
     *
     * @param entity 操作实体
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateById(CarRentalPackageOrderSlippagePo entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageOrderSlippageMapper.updateById(entity);
        return num >= 0;
    }

    /**
     * 根据套餐购买订单编号和逾期订单类型，查询未支付的逾期订单信息
     *
     * @param rentalPackageOrderNo 套餐购买订单编码
     * @param type                 逾期订单类型：1-过期、2-冻结
     * @return 逾期订单信息
     */
    @Slave
    @Override
    public CarRentalPackageOrderSlippagePo selectByPackageOrderNoAndType(String rentalPackageOrderNo, Integer type) {
        if (!ObjectUtils.allNotNull(rentalPackageOrderNo, type)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderSlippageMapper.selectByPackageOrderNoAndType(rentalPackageOrderNo, type);
    }

    /**
     * 根据用户ID查询未支付的逾期订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 逾期订单信息集
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderSlippagePo> selectUnPayByByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderSlippageMapper.selectUnPaidByByUid(tenantId, uid);
    }

    /**
     * 距当前时间，是否存在未缴纳的逾期订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return true(存在)、false(不存在)
     */
    @Slave
    @Override
    public boolean isExitUnpaid(Integer tenantId, Long uid) {
        return carRentalPackageOrderSlippageMapper.isExitUnpaid(tenantId, uid, System.currentTimeMillis()) > 0;
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return 逾期订单信息集
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderSlippagePo> list(CarRentalPackageOrderSlippageQryModel qryModel) {
        return carRentalPackageOrderSlippageMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 逾期订单信息集
     */
    @Slave
    @Override
    public List<CarRentalPackageOrderSlippagePo> page(CarRentalPackageOrderSlippageQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderSlippageQryModel();
        }
        return carRentalPackageOrderSlippageMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageOrderSlippageQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageOrderSlippageQryModel();
        }

        return carRentalPackageOrderSlippageMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return 逾期订单信息
     */
    @Slave
    @Override
    public CarRentalPackageOrderSlippagePo selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderSlippageMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 逾期订单信息
     */
    @Slave
    @Override
    public CarRentalPackageOrderSlippagePo selectById(Long id) {
        if (null == id || id <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderSlippageMapper.selectById(id);
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return 主键ID
     */
    @Override
    public Long insert(CarRentalPackageOrderSlippagePo entity) {
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
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_STAGNATE, entity.getUid()));
        }

        // 保存入库
        carRentalPackageOrderSlippageMapper.insert(entity);

        return entity.getId();
    }
}
