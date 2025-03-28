package com.xiliulou.electricity.service.impl.car;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.domain.car.UserDepositPayTypeDO;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageDepositPayMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车套餐押金缴纳订单表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageDepositPayServiceImpl implements CarRentalPackageDepositPayService {
    
    @Resource
    private CarRentalPackageDepositPayMapper carRentalPackageDepositPayMapper;
    
    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    /**
     * 查询用户最后一次的免押订单生成信息
     *
     * @param tenantId
     * @param uid
     * @return
     */
    @Override
    public CarRentalPackageDepositPayPo queryLastFreeOrderByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageDepositPayMapper.selectLastFreeOrderByUid(tenantId, uid);
    }
    
    /**
     * 根据用户ID和租户ID查询最后一条押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金支付订单
     */
    @Override
    public CarRentalPackageDepositPayPo selectLastByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageDepositPayMapper.selectLastByUid(tenantId, uid);
    }
    
    /**
     * 同步免押状态
     *
     * @param orderNo 押金缴纳订单编码
     * @param optUid  操作人ID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean syncFreeState(String orderNo, Long optUid) {
        return false;
    }
    
    /**
     * 根据订单编号更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @return
     */
    @Override
    public Boolean updatePayStateByOrderNo(String orderNo, Integer payState) {
        return updatePayStateByOrderNo(orderNo, payState, null, null);
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
     * 根据用户ID和租户ID查询支付成功的最后一条押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金支付订单
     */
    @Slave
    @Override
    public CarRentalPackageDepositPayPo selectLastPaySucessByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        return carRentalPackageDepositPayMapper.selectUnRefundCarDeposit(tenantId, uid);
    }
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackageDepositPayPo> list(CarRentalPackageDepositPayQryModel qryModel) {
        return carRentalPackageDepositPayMapper.list(qryModel);
    }
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackageDepositPayPo> page(CarRentalPackageDepositPayQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageDepositPayQryModel();
        }
        
        return carRentalPackageDepositPayMapper.page(qryModel);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageDepositPayQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageDepositPayQryModel();
        }
        
        return carRentalPackageDepositPayMapper.count(qryModel);
    }
    
    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    @Slave
    @Override
    public CarRentalPackageDepositPayPo selectByOrderNo(String orderNo) {
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
    public CarRentalPackageDepositPayPo selectById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        return carRentalPackageDepositPayMapper.selectById(id);
    }
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return
     */
    @Override
    public Long insert(CarRentalPackageDepositPayPo entity) {
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
            entity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, entity.getUid()));
        }
        
        // 保存入库
        carRentalPackageDepositPayMapper.insert(entity);
        
        return entity.getId();
    }
    
    @Slave
    @Override
    public Map<String, Integer> selectPayTypeByOrders(Collection<String> ordersOn) {
        if (CollectionUtil.isEmpty(ordersOn)) {
            return MapUtil.empty();
        }
        List<UserDepositPayTypeDO> list = this.carRentalPackageDepositPayMapper.selectPayTypeByOrders(ordersOn);
        if (CollectionUtil.isEmpty(list)) {
            return MapUtil.empty();
        }
        return list.stream().collect(Collectors.toMap(UserDepositPayTypeDO::getOrderNo, UserDepositPayTypeDO::getPayType,(k1,k2)->k1));
    }
    
    @Slave
    @Override
    public List<CarRentalPackageDepositPayPo> listByOrders(Integer tenantId, List<String> orderNoList) {
        return carRentalPackageDepositPayMapper.selectListByOrders(tenantId, orderNoList);
    }

    @Override
    public Boolean isCarDepositRefund(CarRentalPackageMemberTermPo carRentalPackageMemberTermPo) {
        boolean depositRefundFlag = true;

        if (Objects.isNull(carRentalPackageMemberTermPo)) {
            depositRefundFlag = false;
        } else {
            String depositPayOrderNo = carRentalPackageMemberTermPo.getDepositPayOrderNo();
            if (StringUtils.isBlank(depositPayOrderNo)) {
                depositRefundFlag = false;
            } else {
                CarRentalPackageDepositPayPo carRentalPackageDepositPayPo = this.selectByOrderNo(depositPayOrderNo);
                if (Objects.isNull(carRentalPackageDepositPayPo)) {
                    depositRefundFlag = false;
                } else {
                    if (!Objects.equals(carRentalPackageDepositPayPo.getPayState(), PayStateEnum.SUCCESS.getCode())) {
                        depositRefundFlag = false;
                    } else {
                        // 查询当前订单是否存在退押的状态
                        CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(depositPayOrderNo);
                        if (Objects.nonNull(depositRefundEntity)) {
                            // 正在退押或已退押->不可退
                            if (Objects.equals(depositRefundEntity.getRefundState(), RefundStateEnum.REFUNDING.getCode()) || Objects.equals(depositRefundEntity.getRefundState(),
                                    RefundStateEnum.SUCCESS.getCode())) {
                                depositRefundFlag = false;
                            }
                        }
                    }
                }
            }
        }

        return depositRefundFlag;
    }


    @Override
    @Slave
    public CarRentalPackageDepositPayPo queryDepositOrderByUid(Long uid) {
        return carRentalPackageDepositPayMapper.selectDepositOrderByUid(uid);
    }



}
