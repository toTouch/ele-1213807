package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalOrderPO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.mapper.car.CarRentalOrderMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalOrderOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalOrderQryModel;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalOrderPO>> list(CarRentalOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalOrderMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalOrderPO>> page(CarRentalOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalOrderMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalOrderQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalOrderMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public R<CarRentalOrderPO> selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalOrderMapper.selectByOrderNo(orderNo));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalOrderPO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalOrderMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Long> insert(CarRentalOrderOptModel optModel) {
        CarRentalOrderPO entity = new CarRentalOrderPO();
        BeanUtils.copyProperties(optModel, entity);

        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        // 保存入库
        carRentalOrderMapper.insert(entity);

        return R.ok(entity.getId());
    }
}
