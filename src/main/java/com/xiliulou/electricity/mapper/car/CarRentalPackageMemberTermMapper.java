package com.xiliulou.electricity.mapper.car;

import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;
import com.xiliulou.electricity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车套餐会员期限表 Mapper
 *
 * @author xiaohui.song
 **/
@Mapper
public interface CarRentalPackageMemberTermMapper {

    /**
     * 根据用户UID查询最后一条数据<br />
     * p.s：不区分删除与否，不走缓存
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return
     */
    CarRentalPackageMemberTermPo selectLastByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 分页查询过期的会员套餐信息<br />
     * nowTime 若传入，以传入为准<br />
     * nowTime 不传入，以系统时间为准
     * @param offset 偏移量
     * @param size   取值数量
     * @param nowTime 当前时间戳
     * @return 会员套餐信息集
     */
    List<CarRentalPackageMemberTermPo> pageExpire(@Param("offset") Integer offset, @Param("size") Integer size, @Param("nowTime") Long nowTime);

    /**
     * 根据用户ID和套餐购买订单编码进行退租<br >
     * 用于退掉最后一个订单的时候，即当前正在使用的订单进行退租
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID（可为空）
     * @param optTime 操作时间
     * @return 操作总条数
     */
    int rentRefundByUidAndPackageOrderNo(@Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo, @Param("optUid") Long optUid, @Param("optTime") Long optTime);

    /**
     * 根据用户ID和套餐订单编码查询
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 套餐购买订单编码
     * @return 会员套餐信息
     */
    CarRentalPackageMemberTermPo selectByUidAndPackageOrderNo(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("packageOrderNo") String packageOrderNo);

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optId 操作人ID（可以为空）
     * @param optTime 操作时间
     * @return 操作条数
     */
    int delByUidAndTenantId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据用户ID和租户ID更新状态
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param status 状态
     * @param optId 操作人ID（可以为空）
     * @param optTime 操作时间
     * @return 操作条数
     */
    int updateStatusByUidAndTenantId(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("status") Integer status, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据主键ID更新状态
     * @param id 主键ID
     * @param status 状态
     * @param optId 操作人（可以为空）
     * @param optTime 操作时间
     * @return 操作条数
     */
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("optId") Long optId, @Param("optTime") Long optTime);

    /**
     * 根据主键ID更新数据
     * @param entity 实体数据
     * @return 操作条数
     */
    int updateById(CarRentalPackageMemberTermPo entity);

    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 操作条数
     */
    CarRentalPackageMemberTermPo selectByTenantIdAndUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询条件模型
     * @return 会员套餐信息集
     */
    List<CarRentalPackageMemberTermPo> list(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询分页
     * @param qryModel 查询条件模型
     * @return 会员套餐信息集
     */
    List<CarRentalPackageMemberTermPo> page(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 条件查询总数
     * @param qryModel 查询条件模型
     * @return 总数
     */
    Integer count(CarRentalPackageMemberTermQryModel qryModel);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return 会员套餐信息
     */
    CarRentalPackageMemberTermPo selectById(Long id);

    /**
     * 插入
     * @param entity 实体类
     * @return 操作条数
     */
    int insert(CarRentalPackageMemberTermPo entity);

    /**
     * 会员列表查询租车用户信息
     * @param userInfoQuery
     * @return
     */
    List<UserCarRentalPackageDO> queryUserCarRentalPackageList(@Param("query") UserInfoQuery userInfoQuery);

    /**
     * 会员列表查询租车用户信息总数
     * @param userInfoQuery
     * @return
     */
    Integer queryUserCarRentalPackageCount(@Param("query") UserInfoQuery userInfoQuery);

}
