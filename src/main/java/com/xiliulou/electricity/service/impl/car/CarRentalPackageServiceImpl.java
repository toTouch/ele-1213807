package com.xiliulou.electricity.service.impl.car;

import com.alibaba.fastjson.JSON;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarCouponNamePO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageNameReq;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.car.CarRentalPackageSearchVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车套餐表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Service
@Slf4j
public class CarRentalPackageServiceImpl implements CarRentalPackageService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Autowired
    private OperateRecordUtil operateRecordUtil;
    
    @Resource
    private CarRentalPackageMapper carRentalPackageMapper;
    
    @Autowired
    private UserInfoGroupService userInfoGroupService;
    
    @Autowired
    private CouponService couponService;
    
    @Resource
    private PxzConfigService pxzConfigService;
    
    @Resource
    private FyConfigService fyConfigService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @Autowired
    private StoreService storeService;
    
    /**
     * 根据主键ID查询，不区分是否删除
     *
     * @param ids 主键ID集
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePo> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        
        return carRentalPackageMapper.selectByIds(ids);
    }
    
    /**
     * 根据条件查询<br />
     * 可带分页
     * @param qryModel 查询条件
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePo> listByCondition(CarRentalPackageQryModel qryModel) {
        if (!ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageMapper.list(qryModel);
    }
    
    /**
     * 检测唯一：租户ID+套餐名称
     *
     * @param tenantId 租户ID
     * @param name     套餐名称
     * @return true(存在)、false(不存在)
     */
    @Slave
    @Override
    public Boolean uqByTenantIdAndName(Integer tenantId, String name) {
        if (!ObjectUtils.allNotNull(tenantId, name)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        int num = carRentalPackageMapper.uqByTenantIdAndName(tenantId, name);
        
        return num > 0;
    }
    
    /**
     * 根据ID修改上下架状态
     *
     * @param id     主键ID
     * @param status 上下架状态
     * @param uid    操作人ID
     * @return
     */
    @Override
    public Boolean updateStatusById(Long id, Integer status, Long uid) {
        if (!ObjectUtils.allNotNull(id, status, uid) || !BasicEnum.isExist(status, UpDownEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        CarRentalPackagePo rentalPackagePo = this.selectById(id);
        rentalPackagePo.setStatus(status);
        
        int num = carRentalPackageMapper.updateStatusById(id, status, uid, System.currentTimeMillis());
        
        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));
        operateRecordUtil.record(num, rentalPackagePo);
        return num >= 0;
    }
    
    /**
     * 根据ID删除
     *
     * @param id  主键ID
     * @param uid 操作人ID
     * @return
     */
    @Override
    public Boolean delById(Long id, Long uid) {
        if (!ObjectUtils.allNotNull(id, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 校验能否删除
        if (carRentalPackageOrderService.checkByRentalPackageId(id)) {
            log.warn("CarRentalPackageService.delById, Purchase order record already exists, deletion not allowed. packageId is {}", id);
            throw new BizException("300023", "当前套餐有用户使用，暂不支持删除");
        }
        
        int num = carRentalPackageMapper.delById(id, uid, System.currentTimeMillis());
        
        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));
        
        return num >= 0;
    }
    
    /**
     * 条件查询列表<br /> 全表扫描，慎用
     * 可带分页
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePo> list(CarRentalPackageQryModel qryModel) {
        return carRentalPackageMapper.list(qryModel);
    }
    
    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePo> page(CarRentalPackageQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageQryModel();
        }
        
        return carRentalPackageMapper.page(qryModel);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageQryModel qryModel) {
        if (ObjectUtils.isEmpty(qryModel)) {
            qryModel = new CarRentalPackageQryModel();
        }
        
        return carRentalPackageMapper.count(qryModel);
    }
    
    /**
     * 根据ID查询<br /> 优先查询缓存，缓存没有查询DB，懒加载缓存<br /> 可能返回<code>null</code>
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackagePo selectById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            return null;
        }
        
        // 获取缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id);
        String cacheStr = redisService.get(cacheKey);
        CarRentalPackagePo cacheEntity = JSON.parseObject(cacheStr, CarRentalPackagePo.class);
        if (ObjectUtils.isNotEmpty(cacheEntity)) {
            return cacheEntity;
        }
        
        // 查询 DB
        CarRentalPackagePo dbEntity = carRentalPackageMapper.selectById(id);
        
        // 存入缓存
        redisService.set(cacheKey, JSON.toJSONString(dbEntity));
        
        return dbEntity;
    }
    
    /**
     * 根据ID修改数据
     *
     * @param entity 实体数据
     * @return
     */
    @Override
    public Boolean updateById(CarRentalPackagePo entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getId(), entity.getUpdateUid(), entity.getTenantId(), entity.getName())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 检测原始套餐状态
        CarRentalPackagePo oriEntity = carRentalPackageMapper.selectById(entity.getId());
        if (oriEntity == null || DelFlagEnum.DEL.getCode().equals(oriEntity.getDelFlag())) {
            log.warn("CarRentalPackageService.updateById, not found car_rental_package. packageId is {}", entity.getId());
            throw new BizException("300000", "数据有误");
        }
        if (UpDownEnum.UP.getCode().equals(oriEntity.getStatus())) {
            log.warn("CarRentalPackageService.updateById, The data status is up. packageId is {}", entity.getId());
            throw new BizException("300021", "请先下架套餐再进行编辑操作");
        }
        
        Integer tenantId = entity.getTenantId();
        String name = entity.getName();
        
        // 检测唯一
        if (!oriEntity.getName().equals(name) && carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            log.warn("CarRentalPackageService.updateById, Package name already exists.");
            throw new BizException("300022", "套餐名称已存在");
        }
        
        // 免押套餐校验免押配置
        checkFreeDepositConfig(entity.getFreeDeposit());
        
        // 适配优惠券多张更新
        if (StringUtils.hasText(entity.getCouponArrays()) && !Objects.isNull(oriEntity.getCouponId()) && !entity.getCouponArrays()
                .contains(String.valueOf(oriEntity.getCouponId()))) {
            entity.setCouponId(-1L);
        }
        
        entity.setUpdateTime(System.currentTimeMillis());
        
        int num = carRentalPackageMapper.updateById(entity);
        
        // 删除缓存
        String cacheEky = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, entity.getId());
        redisService.delete(cacheEky);
        
        operateRecordUtil.asyncRecord(oriEntity, entity, userInfoGroupService, couponService, (userInfoGroupService, couponService, operateLogDTO) -> {
            List<Long> oldUserGroupIds = JsonUtil.fromJsonArray((String) operateLogDTO.getOldValue().getOrDefault("userGroupIds", "[]"), Long.class);
            List<UserInfoGroupBO> oldUserGroups = userInfoGroupService.listByIds(oldUserGroupIds);
            operateLogDTO.getOldValue().put("userGroups",Collections.emptyList());
            if (!CollectionUtils.isEmpty(oldUserGroups)) {
                operateLogDTO.getOldValue().put("userGroups", oldUserGroups.stream().map(UserInfoGroupBO::getGroupName).collect(Collectors.toList()));
            }
            
            List<Long> userGroupIds = JsonUtil.fromJsonArray((String) operateLogDTO.getNewValue().getOrDefault("userGroupIds", "[]"), Long.class);
            List<UserInfoGroupBO> userGroups = userInfoGroupService.listByIds(userGroupIds);
            operateLogDTO.getNewValue().put("userGroups",Collections.emptyList());
            if (!CollectionUtils.isEmpty(userGroups)) {
                operateLogDTO.getNewValue().put("userGroups", userGroups.stream().map(UserInfoGroupBO::getGroupName).collect(Collectors.toList()));
            }
            
            List<Long> oldCouponIds = JsonUtil.fromJsonArray((String) operateLogDTO.getOldValue().getOrDefault("couponArrays", "[]"), Long.class);
            List<CarCouponNamePO> oldCoupons = couponService.queryListByIdsFromCache(oldCouponIds);
            operateLogDTO.getOldValue().put("coupons",Collections.emptyList());
            if (!CollectionUtils.isEmpty(oldCoupons)) {
                operateLogDTO.getOldValue().put("coupons", oldCoupons.stream().map(CarCouponNamePO::getName).collect(Collectors.toList()));
            }
            
            List<Long> couponIds = JsonUtil.fromJsonArray((String) operateLogDTO.getNewValue().getOrDefault("couponArrays", "[]"), Long.class);
            List<CarCouponNamePO> coupons = couponService.queryListByIdsFromCache(couponIds);
            operateLogDTO.getNewValue().put("coupons",Collections.emptyList());
            if (!CollectionUtils.isEmpty(coupons)) {
                operateLogDTO.getNewValue().put("coupons", coupons.stream().map(CarCouponNamePO::getName).collect(Collectors.toList()));
            }
            return operateLogDTO;
        });
        return num >= 0;
    }
    
    private void checkFreeDepositConfig(Integer freeDeposit) {
        if (Objects.equals(freeDeposit, BatteryMemberCard.FREE_DEPOSIT)) {
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            
            if (Objects.isNull(pxzConfig) && Objects.isNull(fyConfig)) {
                throw new BizException("100380", "请先完成免押配置，再新增免押套餐");
            }
        }
    }
    
    /**
     * 新增数据，返回主键ID<br /> 若为车电一体，则会联动调用换电套餐的逻辑
     *
     * @param entity 实体数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(CarRentalPackagePo entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getCreateUid(), entity.getTenantId(), entity.getName())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer tenantId = entity.getTenantId();
        String name = entity.getName();
        
        // 检测唯一
        if (carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            log.info("CarRentalPackageService.updateById, Package name already exists.");
            throw new BizException("300022", "套餐名称已存在");
        }
        
        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setSortParam(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());
        
        carRentalPackageMapper.insert(entity);
        
        return entity.getId();
    }
    
    @Override
    public List<CarRentalPackagePo> findByCouponId(Long couponId) {
        return carRentalPackageMapper.selectByCouponId(String.valueOf(couponId));
    }
    
    /**
     * <p>
     * Description: queryToSearchByName 14.4 套餐购买记录（2条优化项）
     * </p>
     *
     * @param rentalPackageNameReq rentalPackageNameReq
     * @return java.util.List<com.xiliulou.electricity.vo.car.CarRentalPackageSearchVo>
     * <p>Project: CarRentalPackageServiceImpl</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @Slave
    @Override
    public List<CarRentalPackageSearchVO> queryToSearchByName(CarRentalPackageNameReq rentalPackageNameReq) {
        return this.carRentalPackageMapper.queryToSearchByName(rentalPackageNameReq);
    }
    
    /**
     * 删除缓存
     *
     * @param key
     */
    private void delCache(String key) {
        redisService.delete(key);
    }
    
    @Override
    public Integer batchUpdateSortParam(List<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries) {
        
        if (Objects.isNull(sortParamQueries)) {
            return null;
        }
        
        return carRentalPackageMapper.batchUpdateSortParam(sortParamQueries);
    }
    
    @Slave
    @Override
    public List<CarRentalPackagePo> listCarRentalPackageForSort(TokenUser tokenUser) {
        List<Integer> franchiseeIds = null;
        if (Objects.equals(tokenUser.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> longIds = userDataScopeService.selectDataIdByUid(tokenUser.getUid());
            if (CollectionUtils.isEmpty(longIds)) {
                return Collections.emptyList();
            }
            franchiseeIds = longIds.stream().map(Long::intValue).collect(Collectors.toList());
        }
        
        if (Objects.equals(tokenUser.getDataType(), User.DATA_TYPE_STORE)) {
            List<Long> storeIds = userDataScopeService.selectDataIdByUid(tokenUser.getUid());
            if (!CollectionUtils.isEmpty(storeIds)) {
                franchiseeIds = storeIds.stream().map(storeId -> storeService.queryByIdFromCache(storeId).getFranchiseeId().intValue()).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return Collections.emptyList();
            }
        }
        
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setTenantId(TenantContextHolder.getTenantId());
        qryModel.setFranchiseeIdList(franchiseeIds);
        
        return carRentalPackageMapper.listCarRentalPackageForSort(qryModel);
    }
}
