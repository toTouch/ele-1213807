package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.RentalPackageOrderFreezeStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageOrderFreezeMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.utils.DateUtils;
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
     * 根据冻结订单编号更新数据
     *
     * @param entity 数据模型
     * @return
     */
    @Override
    public boolean updateByOrderNo(CarRentalPackageOrderFreezePO entity) {
        if (ObjectUtils.allNotNull(entity, entity.getOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = carRentalPackageOrderFreezeMapper.updateByOrderNo(entity);

        return num >= 0;
    }

    /**
     * 根据 uid 和套餐购买订单编码启用冻结订单
     *
     * @param packageOrderNo 套餐购买订单编码
     * @param uid            用户ID
     * @param autoEnable     是否自动启用
     * @param optUid         操作人ID(可为空)
     * @return
     */
    @Override
    public boolean enableFreezeRentOrderByUidAndPackageOrderNo(String packageOrderNo, Long uid, Boolean autoEnable, Long optUid) {
        if (ObjectUtils.allNotNull(packageOrderNo, uid, autoEnable)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageOrderFreezePO freezeEntity = carRentalPackageOrderFreezeMapper.selectFreezeByUidAndPackageOrderNo(uid, packageOrderNo);
        if (ObjectUtils.isEmpty(freezeEntity)) {
            log.error("CarRentalPackageOrderFreezeServiceImpl.enableFreezeRentOrderByUidAndPackageOrderNo error. not found order. uid is {}, packageOrderNo is {}",
                    uid, packageOrderNo);
            // TODO 错误编码
            throw new BizException("", "未找到匹配的订单数据");
        }

        // 实际期限、启用算法
        // 1. 自动启用，启用时间 = 审核通过时间 + 申请期限；实际期限 = 申请期限
        // 2. 手动启动，启用时间 = 当前时间；实际期限 = 当前时间 - 审核通过时间（不足一天按照一天计算）

        // 操作时间
        long nowTime = System.currentTimeMillis();
        // 申请期限
        Integer applyTerm = freezeEntity.getApplyTerm();
        Long auditTime = freezeEntity.getAuditTime();
        // 启用时间：审核通过时间 + 申请期限
        Long enableTime = auditTime + (applyTerm * TimeConstant.DAY_MILLISECOND);
        Integer status = RentalPackageOrderFreezeStatusEnum.AUTO_ENABLE.getCode();
        Integer realTerm = applyTerm;
        // 提前启用
        if (!autoEnable) {
            status = RentalPackageOrderFreezeStatusEnum.EARLY_ENABLE.getCode();
            enableTime = nowTime;
            realTerm = DateUtils.diffDay(nowTime, auditTime);
        }

        int num = carRentalPackageOrderFreezeMapper.enableByUidAndPackageOrderNo(uid, packageOrderNo, status, optUid, nowTime, enableTime, realTerm);

        return num >= 0;
    }

    /**
     * 根据冻结申请单编号，撤销冻结申请
     *
     * @param orderNo 冻结申请单编号
     * @param optUid 操作人ID
     * @return
     */
    @Override
    public Boolean revokeByOrderNo(String orderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(orderNo, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageOrderFreezeMapper.revokeByOrderNo(orderNo, optUid, System.currentTimeMillis());
        return num >= 0;
    }

    /**
     * 根据用户查询待审核的冻结订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageOrderFreezePO selectPendingApprovalByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderFreezeMapper.selectPendingApprovalByUid(tenantId, uid);
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
    public CarRentalPackageOrderFreezePO selectByOrderNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderFreezeMapper.selectByOrderNo(orderNo);
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageOrderFreezePO selectById(Long id) {
        if (null == id || id <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        return carRentalPackageOrderFreezeMapper.selectById(id);
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
