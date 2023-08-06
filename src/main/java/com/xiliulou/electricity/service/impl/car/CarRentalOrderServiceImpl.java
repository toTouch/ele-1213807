package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalOrderMapper;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 车辆租赁订单表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalOrderServiceImpl implements CarRentalOrderService {

    @Resource
    private CarRentalOrderMapper carRentalOrderMapper;

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return 车辆租赁订单集
     */
    @Slave
    @Override
    public List<CarRentalOrderPo> list(CarRentalOrderQryModel qryModel) {
        return carRentalOrderMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 车辆租赁订单集
     */
    @Slave
    @Override
    public List<CarRentalOrderPo> page(CarRentalOrderQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalOrderQryModel();
        }

        return carRentalOrderMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(CarRentalOrderQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalOrderQryModel();
        }

        return carRentalOrderMapper.count(qryModel);
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return 车辆租赁订单
     */
    @Slave
    @Override
    public CarRentalOrderPo selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 车辆租赁订单
     */
    @Slave
    @Override
    public CarRentalOrderPo selectById(Long id) {
        if (null == id || id <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalOrderMapper.selectById(id);
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作模型
     * @return 主键ID
     */
    @Override
    public Long insert(CarRentalOrderPo entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 赋值操作人、时间、删除标记、订单编码
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        if (StringUtils.isBlank(entity.getOrderNo())) {
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_CAR, entity.getUid()));
        }


        // 保存入库
        carRentalOrderMapper.insert(entity);

        return entity.getId();
    }
}
