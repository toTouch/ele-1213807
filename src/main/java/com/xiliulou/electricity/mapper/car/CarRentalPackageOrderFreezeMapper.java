package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐订单冻结表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageOrderFreezeMapper {

    /**
     * 根据用户UID查询最后一笔冻结订单
     * @param uid 用户UID
     * @return 冻结订单
     */
    CarRentalPackageOrderFreezePo selectLastFreeByUid(@Param("uid") Long uid);

    /**
     * 根据用户ID查询冻结中的订单
     * @param uid 用户ID
     * @return
     */
    CarRentalPackageOrderFreezePo selectFreezeByUid(@Param("uid") Long uid);

    /**
     * 根据订单编码进行数据更新
     * @param entity 实体数据
     * @return
     */
    int updateByOrderNo(CarRentalPackageOrderFreezePo entity);

    /**
     * 根据用户ID和购买订单编号启用冻结订单
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编号
     * @param status 状态
     * @param optUid 操作人(可为空)
     * @param optTime 操作时间
     * @param enableTime 启用时间
     * @return int
     * @author xiaohui.song
     **/
    int enableByUidAndPackageOrderNo(@Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo, @Param("status") Integer status, @Param("optUid") Long optUid,
                                     @Param("optTime") Long optTime, @Param("enableTime") Long enableTime);

    /**
     * 根据用户ID和套餐购买订单编号查询冻结中的订单
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @return
     */
    CarRentalPackageOrderFreezePo selectFreezeByUidAndPackageOrderNo(@Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo);

    /**
     * 根据冻结申请单编号，撤销冻结申请
     * @param orderNo 冻结申请单编号
     * @param optUid 操作人ID
     * @param optTime 操作时间
     * @return
     */
    int revokeByOrderNo(@Param("orderNo") String orderNo, @Param("optUid") Long optUid, @Param("optTime") Long optTime);

    /**
     * 根据用户查询待审核的冻结订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    CarRentalPackageOrderFreezePo selectPendingApprovalByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderFreezePo> list(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return
     */
    List<CarRentalPackageOrderFreezePo> page(CarRentalPackageOrderFreezeQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
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
     * 插入
     * @param entity 实体类
     * @return
     */
    int insert(CarRentalPackageOrderFreezePo entity);

}
