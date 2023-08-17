package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 租车套餐订单冻结表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderFreezeService {

    /**
     * 根据用户UID查询最后一笔冻结订单
     * @param uid 用户UID
     * @return 冻结订单
     */
    CarRentalPackageOrderFreezePo selectLastFreeByUid(Long uid);

    /**
     * 根据用户ID查询冻结中的订单
     * @param uid 用户ID
     * @return 冻结订单
     */
    CarRentalPackageOrderFreezePo selectFreezeByUid(Long uid);

    /**
     * 根据用户ID和套餐购买订单编号查询冻结中的订单
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @return 冻结订单
     */
    CarRentalPackageOrderFreezePo selectFreezeByUidAndPackageOrderNo(Long uid, String packageOrderNo);

    /**
     * 根据冻结订单编号更新数据
     * @param entity 数据模型
     * @return
     */
    boolean updateByOrderNo(CarRentalPackageOrderFreezePo entity);

    /**
     * 计算实际冻结期限(时间戳，单位：天)
     * @param applyTerm 申请期限
     * @param applyTime 申请时间
     * @param autoEnable 是否自动启用
     * @return 启用时间戳，实际冻结期限(时间戳，单位：天)
     */
    Pair<Long, Integer> calculateRealTerm(Integer applyTerm, Long applyTime, boolean autoEnable);

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
    CarRentalPackageOrderFreezePo selectPendingApprovalByUid(Integer tenantId, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageOrderFreezePo> list(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    List<CarRentalPackageOrderFreezePo> page(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    Integer count(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 根据订单编码查询
     * @param orderNo 订单编码
     * @return
     */
    CarRentalPackageOrderFreezePo selectByOrderNo(String orderNo);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return
     */
    CarRentalPackageOrderFreezePo selectById(Long id);

    /**
     * 新增数据，返回主键ID
     * @param entity 实体模型
     * @return
     */
    Long insert(CarRentalPackageOrderFreezePo entity);

}
