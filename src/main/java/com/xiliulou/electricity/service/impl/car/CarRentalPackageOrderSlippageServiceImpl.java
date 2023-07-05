package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderSlippageMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderSlippageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageOrderSlippagePO>> list(CarRentalPackageOrderSlippageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderSlippageMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageOrderSlippagePO>> page(CarRentalPackageOrderSlippageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderSlippageMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageOrderSlippageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderSlippageMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderSlippagePO> selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderSlippageMapper.selectByOrderNo(orderNo));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageOrderSlippagePO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return R.ok(carRentalPackageOrderSlippageMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Long> insert(CarRentalPackageOrderSlippageOptModel optModel) {
        CarRentalPackageOrderSlippagePO entity = new CarRentalPackageOrderSlippagePO();
        BeanUtils.copyProperties(optModel, entity);
        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        // 保存入库
        carRentalPackageOrderSlippageMapper.insert(entity);
        return R.ok(entity.getId());
    }
}
