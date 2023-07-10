package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageDepositPayMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐押金缴纳订单表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageDepositPayServiceImpl implements CarRentalPackageDepositPayService {

    @Resource
    private CarRentalPackageDepositPayMapper carRentalPackageDepositPayMapper;

    /**
     * 根据订单编号更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @return
     */
    @Override
    public Boolean updatePayStateByOrderNo(String orderNo, Integer payState) {
        return updatePayStateByOrderNo(orderNo, payState);
    }

    /**
     * 根据订单编号更新支付状态
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
        int num = carRentalPackageDepositPayMapper.updatePayStateByOrderNo(orderNo, payState, remark, uid, now);

        return num >= 0;
    }

    /**
     * 根据租户ID和用户ID查询租车套餐押金缴纳订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageDepositPayPO selectByTenantIdAndUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageDepositPayMapper.selectByTenantIdAndUid(tenantId, uid);
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
    public R<List<CarRentalPackageDepositPayPO>> list(CarRentalPackageDepositPayQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositPayMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageDepositPayPO>> page(CarRentalPackageDepositPayQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositPayMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageDepositPayQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositPayMapper.count(qryModel));
    }

    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageDepositPayPO selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageDepositPayMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageDepositPayPO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageDepositPayMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return
     */
    @Override
    public Long insert(CarRentalPackageDepositPayPO entity) {

        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        // 保存入库
        carRentalPackageDepositPayMapper.insert(entity);

        return entity.getId();
    }
}
