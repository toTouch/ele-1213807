package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 租车套餐押金缴纳订单表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageDepositPayService {
    
    /**
     * 查询用户最后一次的免押订单生成信息
     * @return
     */
    CarRentalPackageDepositPayPo queryLastFreeOrderByUid(Integer tenantId, Long uid);
    
    /**
     * 根据用户ID和租户ID查询最后一条押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金支付订单
     */
    CarRentalPackageDepositPayPo selectLastByUid(Integer tenantId, Long uid);
    
    /**
     * 同步免押状态
     *
     * @param orderNo 押金缴纳订单编码
     * @param optUid  操作人ID
     * @return true(成功)、false(失败)
     */
    boolean syncFreeState(String orderNo, Long optUid);
    
    /**
     * 根据订单编号更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState);
    
    /**
     * 根据订单编号更新支付状态
     *
     * @param orderNo  订单编码
     * @param payState 支付状态
     * @param remark   备注
     * @param uid      操作人
     * @return true(成功)、false(失败)
     */
    Boolean updatePayStateByOrderNo(String orderNo, Integer payState, String remark, Long uid);
    
    /**
     * 根据用户ID和租户ID查询支付成功的最后一条押金信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金支付订单
     */
    CarRentalPackageDepositPayPo selectLastPaySucessByUid(Integer tenantId, Long uid);
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositPayPo> list(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageDepositPayPo> page(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageDepositPayQryModel qryModel);
    
    /**
     * 根据订单编码查询
     *
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageDepositPayPo selectByOrderNo(String orderNo);
    
    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    CarRentalPackageDepositPayPo selectById(Long id);
    
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return
     */
    Long insert(CarRentalPackageDepositPayPo entity);
    
    /**
     * <p>
     * Description: selectPayTypeByOrders 根据订单数组查询该订单的支付类型
     * </p>
     *
     * @param ordersOn ordersOn
     * @return java.util.Map<java.lang.String, java.lang.Integer>
     * <p>Project: CarRentalPackageDepositPayService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#Nffzd1GUWoZOWAxqnV9cXzk2nQh">15.1  实名用户列表（16条优化项）</a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/13
     */
    Map<String, Integer> selectPayTypeByOrders(Collection<String> ordersOn);
    
    List<CarRentalPackageDepositPayPo> listByOrders(Integer tenantId, List<String> orderNoList);

    CarRentalPackageDepositPayPo queryDepositOrderByUid(Long uid);

}
