package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;

import java.util.List;

/**
 * 租车套餐订单冻结表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderFreezeService {

    /**
     * 根据冻结订单编号更新数据
     * @param entity 数据模型
     * @return
     */
    boolean updateByOrderNo(CarRentalPackageOrderFreezePO entity);

    /**
     * 根据 uid 和套餐购买订单编码启用冻结订单
     * @param packageOrderNo 套餐购买订单编码
     * @param uid 用户ID
     * @param autoEnable 是否自动启用
     * @param optUid 操作人ID(可为空)
     * @return
     */
    boolean enableFreezeRentOrderByUidAndPackageOrderNo(String packageOrderNo, Long uid, Boolean autoEnable, Long optUid);

    /**
     * 根据冻结申请单编号，撤销冻结申请
     * @param orderNo 冻结申请单编号
     * @param optUid 操作人ID
     * @return
     */
    Boolean revokeByOrderNo(String orderNo, Long optUid);

    /**
     * 查询待审核的冻结订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    CarRentalPackageOrderFreezePO selectPendingApprovalByUid(Integer tenantId, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderFreezePO>> list(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    R<List<CarRentalPackageOrderFreezePO>> page(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    R<Integer> count(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderFreezePO selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderFreezePO selectById(Long id);

    /**
     * 新增数据，返回主键ID
     * @param entity 实体模型
     * @return
     */
    Long insert(CarRentalPackageOrderFreezePO entity);
    
}
