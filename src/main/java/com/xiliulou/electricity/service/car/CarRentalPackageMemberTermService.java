package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;

import java.util.List;

/**
 * 租车套餐会员期限表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermService {

    /**
     * 分页查询过期的会员套餐信息<br />
     * nowTime 若传入，以传入为准<br />
     * nowTime 不传入，以系统时间为准
     * @param offset 偏移量
     * @param size 取值数量
     * @param nowTime 当前时间戳(可为空)
     * @return 会员套餐信息集
     */
    List<CarRentalPackageMemberTermPO> pageExpire(Integer offset, Integer size, Long nowTime);

    /**
     * 根据用户ID和套餐购买订单编码进行退租<br />
     * 用于退掉最后一个订单的时候，即当前正在使用的订单进行退租
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID
     * @return true(成功)、false(失败)
     */
    boolean rentRefundByUidAndPackageOrderNo(Long uid, String packageOrderNo, Long optUid);

    /**
     * 根据用户ID和套餐订单编码查询
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买套餐订单编码
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPO selectByUidAndPackageOrderNo(Integer tenantId, Long uid, String packageOrderNo);

    /**
     * 根据用户ID和租户ID删除
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optId 操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    Boolean delByUidAndTenantId(Integer tenantId, Long uid, Long optId);

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param status 状态
     * @param optId 操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    Boolean updateStatusByUidAndTenantId(Integer tenantId, Long uid, Integer status, Long optId);

    /**
     * 根据主键ID更新状态
     * @param id 主键ID
     * @param status 状态
     * @param optId 操作人（可以为空）
     * @return true(成功)、false(失败)
     */
    boolean updateStatusById(Long id, Integer status, Long optId);

    /**
     * 根据主键ID更新数据
     * @param entity 实体数据
     * @return true(成功)、false(失败)
     */
    Boolean updateById(CarRentalPackageMemberTermPO entity);

    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息<br />
     * 优先查询缓存，缓存没有查询DB，懒加载缓存<br />
     * 可能返回<code>null</code>
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPO selectByTenantIdAndUid(Integer tenantId, Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    List<CarRentalPackageMemberTermPO> list(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    List<CarRentalPackageMemberTermPO> page(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return 总数
     */
    Integer count(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPO selectById(Long id);


    /**
     * 新增数据，返回主键ID
     * @param entity 操作实体
     * @return 主键ID
     */
    Long insert(CarRentalPackageMemberTermPO entity);
    
}
