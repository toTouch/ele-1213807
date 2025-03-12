package com.xiliulou.electricity.service.impl.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMemberTermMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermExpiredQryModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐会员期限表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermServiceImpl implements CarRentalPackageMemberTermService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private CarRentalPackageMemberTermMapper carRentalPackageMemberTermMapper;
    
    @Resource
    private ElectricityBatteryLabelBizService electricityBatteryLabelBizService;
    
    
    /**
     * 根据用户UID查询套餐购买次数<br /> p.s：不区分删除与否，不走缓存
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return 若不存在，则返回0
     */
    @Override
    public Integer selectPayCountByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermMapper.selectLastByUid(tenantId, uid);
        return ObjectUtils.isEmpty(memberTermPo) ? 0 : memberTermPo.getPayCount();
    }
    
    /**
     * 分页查询过期的会员套餐信息<br /> nowTime 若传入，以传入为准<br /> nowTime 不传入，以系统时间为准
     *
     * @param offset  偏移量
     * @param size    取值数量
     * @param nowTime 当前时间戳(可为空)
     * @return 会员套餐信息集
     */
    @Override
    public List<CarRentalPackageMemberTermPo> pageExpire(Integer offset, Integer size, Long nowTime) {
        offset = ObjectUtils.isEmpty(offset) ? 0 : offset;
        size = ObjectUtils.isEmpty(size) ? 10 : size;
        nowTime = ObjectUtils.isEmpty(nowTime) ? System.currentTimeMillis() : nowTime;
        
        return carRentalPackageMemberTermMapper.pageExpire(offset, size, nowTime);
    }
    
    /**
     * 根据用户ID和套餐购买订单编码进行退租<br /> 用于退掉最后一个订单的时候，即当前正在使用的订单进行退租
     *
     * @param tenantId       租户ID
     * @param uid            用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid         操作人ID （可为空）
     * @return true(成功)、false(失败)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rentRefundByUidAndPackageOrderNo(Integer tenantId, Long uid, String packageOrderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        int num = carRentalPackageMemberTermMapper.rentRefundByUidAndPackageOrderNo(uid, packageOrderNo, optUid, System.currentTimeMillis());
        
        // 删除缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        redisService.delete(cacheKey);
        
        return num >= 0;
    }
    
    /**
     * 根据用户ID和套餐订单编码查询
     *
     * @param tenantId       租户ID
     * @param uid            用户ID
     * @param packageOrderNo 购买套餐订单编码
     * @return 租车套餐会员期限信息
     */
    @Slave
    @Override
    public CarRentalPackageMemberTermPo selectByUidAndPackageOrderNo(Integer tenantId, Long uid, String packageOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageMemberTermMapper.selectByUidAndPackageOrderNo(tenantId, uid, packageOrderNo);
    }
    
    /**
     * 根据用户ID和租户ID删除
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param optId    操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean delByUidAndTenantId(Integer tenantId, Long uid, Long optId) {
        if (ObjectUtils.isEmpty(tenantId) || ObjectUtils.isEmpty(uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageMemberTermMapper.delByUidAndTenantId(tenantId, uid, optId, System.currentTimeMillis());
        
        // 清空缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        redisService.delete(cacheKey);
        
        return num >= 0;
    }
    
    /**
     * 根据用户ID和租户ID更新状态
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param status   状态
     * @param optId    操作人ID（可以为空）
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updateStatusByUidAndTenantId(Integer tenantId, Long uid, Integer status, Long optId) {
        if (!ObjectUtils.allNotNull(tenantId, uid, status)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageMemberTermMapper.updateStatusByUidAndTenantId(tenantId, uid, status, optId, System.currentTimeMillis());
        
        // 清空缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        redisService.delete(cacheKey);
        
        return num >= 0;
    }
    
    /**
     * 根据主键ID更新状态
     *
     * @param id     主键ID
     * @param status 状态
     * @param optId  操作人（可以为空）
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateStatusById(Long id, Integer status, Long optId) {
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(status)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        int num = carRentalPackageMemberTermMapper.updateStatusById(id, status, optId, System.currentTimeMillis());
        
        // 清空缓存
        CarRentalPackageMemberTermPo dbEntity = carRentalPackageMemberTermMapper.selectById(id);
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, dbEntity.getTenantId(), dbEntity.getUid());
        redisService.delete(cacheKey);
        
        return num >= 0;
    }
    
    /**
     * 根据主键ID更新数据
     *
     * @param entity 数据实体
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean updateById(CarRentalPackageMemberTermPo entity) {
        if (ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        entity.setUpdateTime(System.currentTimeMillis());
        
        int num = carRentalPackageMemberTermMapper.updateById(entity);
        
        // 清空缓存
        Integer tenantId = entity.getTenantId();
        Long uid = entity.getUid();
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            CarRentalPackageMemberTermPo dbEntity = carRentalPackageMemberTermMapper.selectById(entity.getId());
            tenantId = dbEntity.getTenantId();
            uid = dbEntity.getUid();
        }
        
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        redisService.delete(cacheKey);
        
        // 异步处理电池标签，不确定外层有没有携带套餐类型，每一次修改都处理
        entity.setUid(uid);
        electricityBatteryLabelBizService.checkRentStatusForLabel(null, entity);
        
        return num >= 0;
    }
    
    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息<br /> 可能返回<code>null</code>
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 租车套餐会员期限信息
     */
    @Slave
    @Override
    public CarRentalPackageMemberTermPo selectByTenantIdAndUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 获取缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        String cacheStr = redisService.get(cacheKey);
        if (StringUtils.isNotBlank(cacheStr)) {
            CarRentalPackageMemberTermPo cacheEntity = JsonUtil.fromJson(cacheStr, CarRentalPackageMemberTermPo.class);
            if (ObjectUtils.isNotEmpty(cacheEntity)) {
                return cacheEntity;
            }
        }
        
        // 获取 DB
        CarRentalPackageMemberTermPo dbEntity = carRentalPackageMemberTermMapper.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(dbEntity)) {
            redisService.set(cacheKey, JSON.toJSONString(dbEntity));
        }
        
        return dbEntity;
    }
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    @Slave
    @Override
    public List<CarRentalPackageMemberTermPo> list(CarRentalPackageMemberTermQryModel qryModel) {
        return carRentalPackageMemberTermMapper.list(qryModel);
    }
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return 租车套餐会员期限信息集
     */
    @Slave
    @Override
    public List<CarRentalPackageMemberTermPo> page(CarRentalPackageMemberTermQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageMemberTermQryModel();
        }
        
        return carRentalPackageMemberTermMapper.page(qryModel);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageMemberTermQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageMemberTermQryModel();
        }
        
        return carRentalPackageMemberTermMapper.count(qryModel);
    }
    
    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 租车套餐会员期限信息
     */
    @Slave
    @Override
    public CarRentalPackageMemberTermPo selectById(Long id) {
        if (null == id || id <= 0) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        CarRentalPackageMemberTermPo dbEntity = carRentalPackageMemberTermMapper.selectById(id);
        
        // 清空缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, dbEntity.getTenantId(), dbEntity.getUid());
        redisService.delete(cacheKey);
        
        return dbEntity;
    }
    
    /**
     * 新增数据，返回主键ID
     *
     * @param entity 操作实体
     * @return 主键ID
     */
    @Override
    public Long insert(CarRentalPackageMemberTermPo entity) {
        if (ObjectUtils.isEmpty(entity)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());
        
        // 保存入库
        carRentalPackageMemberTermMapper.insert(entity);
        
        return entity.getId();
    }
    
    @Slave
    @Override
    public List<UserCarRentalPackageDO> queryUserCarRentalPackageList(UserInfoQuery userInfoQuery) {
        return carRentalPackageMemberTermMapper.queryUserCarRentalPackageList(userInfoQuery);
    }
    
    @Slave
    @Override
    public Integer queryUserCarRentalPackageCount(UserInfoQuery userInfoQuery) {
        
        return carRentalPackageMemberTermMapper.queryUserCarRentalPackageCount(userInfoQuery);
    }
    
    @Slave
    @Override
    public List<CarRentalPackageMemberTermPo> listUserPayCountByUidList(List<Long> uidList) {
        return carRentalPackageMemberTermMapper.selectListUserPayCount(uidList);
    }
    
    @Override
    public void deleteCache(Integer tenantId, Long uid) {
        // 清空缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        redisService.delete(cacheKey);
    }
    
    @Override
    public Integer checkUserByRentalPackageId(Long packageId) {
        return carRentalPackageMemberTermMapper.checkUserByRentalPackageId(packageId);
    }
    
    @Slave
    @Override
    public List<CarRentalPackageMemberTermPo> queryListExpireByParam(CarRentalPackageMemberTermExpiredQryModel qryModel) {
        return carRentalPackageMemberTermMapper.selectListExpireByParam(qryModel);
    }
    
    @Slave
    @Override
    public List<CarRentalPackageMemberTermPo> listByTenantIdAndUidList(Integer tenantId, List<Long> uidList) {
        return carRentalPackageMemberTermMapper.selectListByTenantIdAndUidList(tenantId, uidList);
    }
    
    @Override
    public Integer removeByUid(Integer tenantId, Long uid) {
        Integer remove = carRentalPackageMemberTermMapper.removeByUid(tenantId, uid);
        if (remove > 0) {
            deleteCache(tenantId, uid);
        }
        
        return remove;
    }
}
