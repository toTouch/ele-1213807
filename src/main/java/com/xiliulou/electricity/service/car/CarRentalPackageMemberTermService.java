package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermExpiredQryModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;
import com.xiliulou.electricity.query.UserInfoQuery;

import java.util.List;

/**
 * 租车套餐会员期限表 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermService {
    
    /**
     * 根据用户UID查询套餐购买次数<br /> p.s：不区分删除与否，不走缓存
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return 若不存在，则返回0
     */
    Integer selectPayCountByUid(Integer tenantId, Long uid);
    
    /**
     * 分页查询过期的会员套餐信息<br /> nowTime 若传入，以传入为准<br /> nowTime 不传入，以系统时间为准
     *
     * @param offset  偏移量
     * @param size    取值数量
     * @param nowTime 当前时间戳(可为空)
     * @return 会员套餐信息集
     */
    List<CarRentalPackageMemberTermPo> pageExpire(Integer offset, Integer size, Long nowTime);
    
    /**
     * 根据用户ID和套餐购买订单编码进行退租<br /> 用于退掉最后一个订单的时候，即当前正在使用的订单进行退租
     *
     * @param tenantId       租户ID
     * @param uid            用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid         操作人ID （可为空）
     * @return true(成功)、false(失败)
     */
    boolean rentRefundByUidAndPackageOrderNo(Integer tenantId, Long uid, String packageOrderNo, Long optUid);
    
    /**
     * 根据用户ID和套餐订单编码查询
     *
     * @param tenantId       租户ID
     * @param uid            用户ID
     * @param packageOrderNo 购买套餐订单编码
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPo selectByUidAndPackageOrderNo(Integer tenantId, Long uid, String packageOrderNo);
    
    /**
     * 根据用户ID和租户ID删除
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param optId    操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    Boolean delByUidAndTenantId(Integer tenantId, Long uid, Long optId);
    
    /**
     * 根据用户ID和租户ID更新状态
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param status   状态
     * @param optId    操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    Boolean updateStatusByUidAndTenantId(Integer tenantId, Long uid, Integer status, Long optId);
    
    /**
     * 根据主键ID更新状态
     *
     * @param id     主键ID
     * @param status 状态
     * @param optId  操作人（可以为空）
     * @return true(成功)、false(失败)
     */
    boolean updateStatusById(Long id, Integer status, Long optId);
    
    /**
     * 根据主键ID更新数据
     *
     * @param entity 实体数据
     * @return true(成功)、false(失败)
     */
    Boolean updateById(CarRentalPackageMemberTermPo entity);
    
    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息<br /> 优先查询缓存，缓存没有查询DB，懒加载缓存<br /> 可能返回<code>null</code>
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPo selectByTenantIdAndUid(Integer tenantId, Long uid);
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    List<CarRentalPackageMemberTermPo> list(CarRentalPackageMemberTermQryModel qryModel);
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    List<CarRentalPackageMemberTermPo> page(CarRentalPackageMemberTermQryModel qryModel);
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    Integer count(CarRentalPackageMemberTermQryModel qryModel);
    
    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 租车套餐会员期限信息
     */
    CarRentalPackageMemberTermPo selectById(Long id);
    
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return 主键ID
     */
    Long insert(CarRentalPackageMemberTermPo entity);
    
    /**
     * 会员列表统计会员租车信息
     *
     * @param userInfoQuery
     * @return
     */
    List<UserCarRentalPackageDO> queryUserCarRentalPackageList(UserInfoQuery userInfoQuery);
    
    /**
     * 会员列表统计会员租车信息总数
     *
     * @param userInfoQuery
     * @return
     */
    Integer queryUserCarRentalPackageCount(UserInfoQuery userInfoQuery);
    
    /**
     * 会员列表统计会员购买次数
     *
     * @return
     */
    List<CarRentalPackageMemberTermPo> listUserPayCountByUidList(List<Long> uidList);
    
    /**
     * 删除缓存信息
     *
     * @param tenantId
     * @param uid
     */
    void deleteCache(Integer tenantId, Long uid);
    
    /**
     * 当前套餐是否有用户使用
     *
     * @param packageId 套餐id
     * @return 是否存在
     */
    Integer checkUserByRentalPackageId(Long packageId);
    
    /**
     *
     * @param qryModel
     * @author caobotao.cbt
     * @date 2024/11/25 17:05
     */
    List<CarRentalPackageMemberTermPo> queryListExpireByParam(CarRentalPackageMemberTermExpiredQryModel qryModel);
    
    Integer removeByUid(Integer tenantId, Long uid);
}
