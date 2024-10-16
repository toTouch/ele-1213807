package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.BatteryMemberCardConstants;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.BatteryModelDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarCouponNamePO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardSearchVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.CouponSearchVo;
import com.xiliulou.electricity.vo.SearchVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_MAX_VALID_DAYS;
import static com.xiliulou.electricity.entity.BatteryMemberCard.BUSINESS_TYPE_INSTALLMENT_BATTERY;

/**
 * (BatteryMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
@Service("batteryMemberCardService")
@Slf4j
public class BatteryMemberCardServiceImpl implements BatteryMemberCardService {
    
    @Resource
    private BatteryMemberCardMapper batteryMemberCardMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private BatteryModelService batteryModelService;
    
    @Autowired
    private UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    private EnterprisePackageService enterprisePackageService;
    
    @Autowired
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Autowired
    private UserInfoGroupService userInfoGroupService;
    
    @Resource
    private TenantService tenantService;
    
    @Autowired
    private UserDataScopeServiceImpl userDataScopeService;
    
    @Resource
    private StoreService storeService;
    
    @Autowired
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Resource
    private PxzConfigService pxzConfigService;
    
    @Resource
    private FyConfigService fyConfigService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromDB(Long id) {
        return this.batteryMemberCardMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromCache(Long id) {
        BatteryMemberCard cacheBatteryMemberCard = redisService.getWithHash(CacheConstant.CACHE_BATTERY_MEMBERCARD + id, BatteryMemberCard.class);
        if (Objects.nonNull(cacheBatteryMemberCard)) {
            return cacheBatteryMemberCard;
        }
        
        BatteryMemberCard batteryMemberCard = this.queryByIdFromDB(id);
        if (Objects.isNull(batteryMemberCard)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_BATTERY_MEMBERCARD + id, batteryMemberCard);
        
        return batteryMemberCard;
    }
    
    @Override
    public Integer insert(BatteryMemberCard batteryMemberCard) {
        return this.batteryMemberCardMapper.insert(batteryMemberCard);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insertBatteryMemberCardAndBatteryType(BatteryMemberCard batteryMemberCard, List<String> batteryModels) {
        int insert = this.batteryMemberCardMapper.insert(batteryMemberCard);
        
        if (CollectionUtils.isNotEmpty(batteryModels)) {
            memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(batteryModels, batteryMemberCard.getId()));
        }
        
        return insert;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryMemberCard batteryMemberCard) {
        int update = this.batteryMemberCardMapper.update(batteryMemberCard);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MEMBERCARD + batteryMemberCard.getId());
        });
        
        return update;
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.batteryMemberCardMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MEMBERCARD + id);
        });
        
        return delete;
    }
    
    @Override
    @Slave
    public List<BatteryMemberCardSearchVO> searchV2(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectBySearchV2(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardSearchVO batteryMemberCardVO = new BatteryMemberCardSearchVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
    
    @Deprecated
    @Override
    @Slave
    public List<BatteryMemberCardSearchVO> search(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectBySearch(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardSearchVO batteryMemberCardVO = new BatteryMemberCardSearchVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<BatteryMemberCardVO> selectUserBatteryMembercardList(BatteryMemberCardQuery query) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Collections.emptyList();
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("USER BATTERY MEMBER CARD WARN!not found franchisee,uid={},franchiseeId={}", userInfo.getUid(), query.getFranchiseeId());
            return Collections.emptyList();
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        List<UserInfoGroupNamesBO> userInfoGroupNamesBOs = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(query.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
        
        // 处理新租套餐重复购买的问题，如果先购买了租车套餐，则用户属于老用户，则只能购买租赁类型为不限或者续租的套餐
        if (Objects.isNull(userBatteryMemberCard) && userInfo.getPayCount() <= 0) {
            // 新租
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
            // 为了兼容免押后购买套餐
            if (Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE)
                    && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                if (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES)) {
                    query.setDeposit(userBatteryDeposit.getBeforeModifyDeposit());
                } else {
                    query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                }
            }
            
        } else if (Objects.isNull(userBatteryMemberCard) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            // 非新租 购买押金套餐
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
            
            if (Objects.nonNull(userBatteryDeposit) && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                if (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES)) {
                    query.setDeposit(userBatteryDeposit.getBeforeModifyDeposit());
                } else {
                    query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                }
            }
            
        } else {
            // 续费
            BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("USER BATTERY MEMBERCARD ERROR!not found batteryMemberCard,uid={},mid={}", SecurityUtils.getUid(), userBatteryMemberCard.getMemberCardId());
                return Collections.emptyList();
            }
            
            query.setDeposit(batteryMemberCard.getDeposit());
            
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setBatteryV(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()) : null);
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
        }
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectByPageForUser(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        // 用户绑定的电池型号串数
        List<String> userBindBatteryType = null;
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            userBindBatteryType = userBatteryTypeService.selectByUid(userInfo.getUid());
            if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                userBindBatteryType = userBindBatteryType.stream().map(item -> item.substring(item.lastIndexOf("_") + 1)).collect(Collectors.toList());
            }
        }
        
        List<BatteryMemberCardVO> result = new ArrayList<>();
        for (BatteryMemberCardAndTypeVO item : list) {
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                List<String> number = null;
                if (CollectionUtils.isNotEmpty(item.getBatteryType())) {
                    // 套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType()))
                            .map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
                }
                
                if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                    if (!(CollectionUtils.isNotEmpty(number) && CollectionUtils.containsAll(number, userBindBatteryType))) {
                        continue;
                    }
                }
            }
            
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            
            if (Objects.nonNull(item.getCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(item.getCouponId());
                batteryMemberCardVO.setCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
            }
            
            // 设置分期套餐期数、剩余每期费用
            if (Objects.equals(batteryMemberCardVO.getBusinessType(), BUSINESS_TYPE_INSTALLMENT_BATTERY)) {
                Integer installmentNo = batteryMemberCardVO.getValidDays() / 30;
                batteryMemberCardVO.setInstallmentNo(installmentNo);
                
                BigDecimal rentPrice = batteryMemberCardVO.getRentPrice();
                BigDecimal downPayment = batteryMemberCardVO.getDownPayment();
                batteryMemberCardVO.setRemainingCost(rentPrice.subtract(downPayment).divide(new BigDecimal((installmentNo).toString()), 2, RoundingMode.DOWN));
            }
            
            result.add(batteryMemberCardVO);
        }
        
        return result;
    }
    
    @Override
    public List<BatteryMemberCardVO> selectListByQuery(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByQuery(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public List<BatteryMemberCard> selectListByCouponId(Long couponId) {
        return this.batteryMemberCardMapper.selectList(new LambdaQueryWrapper<BatteryMemberCard>().eq(BatteryMemberCard::getBusinessType, BatteryMemberCard.BUSINESS_TYPE_BATTERY)
                .eq(BatteryMemberCard::getCouponId, couponId).eq(BatteryMemberCard::getDelFlag, BatteryMemberCard.DEL_NORMAL));
    }
    
    @Slave
    @Override
    public Integer isMemberCardBindFranchinsee(Long id, Integer tenantId) {
        return this.batteryMemberCardMapper.isMemberCardBindFranchinsee(id, tenantId);
    }
    
    @Slave
    @Override
    public List<BatteryMemberCard> listMemberCardsByIdList(BatteryMemberCardQuery query) {
        return batteryMemberCardMapper.listByIdList(query);
    }
    
    @Slave
    @Override
    public List<BatteryMemberCard> queryListByIdList(List<Long> ids) {
        return batteryMemberCardMapper.selectListByIds(ids);
    }
    
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public Integer batchUpdateSortParam(List<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries) {
        if (CollectionUtils.isEmpty(sortParamQueries)) {
            return null;
        }
        
        return batteryMemberCardMapper.batchUpdateSortParam(sortParamQueries);
    }
    
    @Slave
    @Override
    public List<BatteryMemberCardVO> selectByPage(BatteryMemberCardQuery query) {
        
        // 当前端传递型号为字符串0时，为标准型号即套餐不分型号，t_member_card_battery_type中未存关联数据
        if (StringUtils.isNotEmpty(query.getBatteryModel()) && !(BatteryMemberCardConstants.REGARDLESS_OF_MODEL.equals(query.getBatteryModel()))) {
            query.setOriginalBatteryModel(query.getBatteryModel());
            query.setBatteryModel(null);
        }
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectByPage(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                batteryMemberCardVO.setFranchiseeName(franchisee.getName());
            }
            
            // 设置电池型号
            if (!item.getBatteryType().isEmpty()) {
                
                List<String> originalBatteryModels = item.getBatteryType().stream().map(MemberCardBatteryType::getBatteryType).distinct().collect(Collectors.toList());
                batteryMemberCardVO.setBatteryModels(batteryModelService.selectShortBatteryType(originalBatteryModels, item.getTenantId()));
            }
            
            // 设置优惠券
            if (Objects.equals(item.getSendCoupon(), BatteryMemberCard.SEND_COUPON_YES)) {
                List<CouponSearchVo> coupons = new ArrayList<>();
                HashSet<Integer> couponIdsSet = new HashSet<>();
                if (Objects.nonNull(item.getCouponId())) {
                    couponIdsSet.add(item.getCouponId());
                }
                if (StringUtils.isNotBlank(item.getCouponIds())) {
                    couponIdsSet.addAll(JsonUtil.fromJsonArray(item.getCouponIds(), Integer.class));
                }
                
                if (!CollectionUtils.isEmpty(couponIdsSet)) {
                    couponIdsSet.forEach(couponId -> {
                        CouponSearchVo couponSearchVo = new CouponSearchVo();
                        Coupon coupon = couponService.queryByIdFromCache(couponId);
                        if (Objects.nonNull(coupon)) {
                            BeanUtils.copyProperties(coupon, couponSearchVo);
                            couponSearchVo.setId(coupon.getId().longValue());
                            coupons.add(couponSearchVo);
                        }
                    });
                }
                batteryMemberCardVO.setCoupons(coupons);
            }
            
            // 设置用户分组
            if (StringUtils.isNotBlank(item.getUserInfoGroupIds())) {
                List<SearchVo> userInfoGroups = new ArrayList<>();
                List<Long> userInfoGroupIds = JsonUtil.fromJsonArray(item.getUserInfoGroupIds(), Long.class);
                
                if (CollectionUtils.isNotEmpty(userInfoGroupIds)) {
                    for (Long userInfoGroupId : userInfoGroupIds) {
                        SearchVo searchVo = new SearchVo();
                        UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(userInfoGroupId);
                        if (Objects.nonNull(userInfoGroup)) {
                            BeanUtils.copyProperties(userInfoGroup, searchVo);
                            userInfoGroups.add(searchVo);
                        }
                    }
                }
                batteryMemberCardVO.setUserInfoGroups(userInfoGroups);
            }
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Slave
    public List<BatteryMemberCardVO> selectByQuery(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByQuery(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
        
    }
    
    @Override
    public List<BatteryMemberCardVO> selectByPageForUser(BatteryMemberCardQuery query) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("USER BATTERY MEMBER CARD WARN!not found franchisee,uid={},franchiseeId={}", SecurityUtils.getUid(), query.getFranchiseeId());
            return Collections.emptyList();
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(SecurityUtils.getUid());
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        
        List<UserInfoGroupNamesBO> userInfoGroupNamesBOs = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
        
        // 处理新租套餐重复购买的问题，如果先购买了租车套餐，则用户属于老用户，则只能购买租赁类型为不限或者续租的套餐
        if (Objects.isNull(userBatteryMemberCard) && userInfo.getPayCount() <= 0) {
            // 新租
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
            
            // 为了兼容免押后购买套餐
            if (Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE)
                    && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                if (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES)) {
                    query.setDeposit(userBatteryDeposit.getBeforeModifyDeposit());
                } else {
                    query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                }
            }
        } else if (Objects.isNull(userBatteryMemberCard) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            // 非新租 购买押金套餐
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
            
            // 增加判断用户退押后，无法选择其他伏数套餐的问题。如果押金已缴纳，则需要根据押金金额过滤套餐
            if (Objects.nonNull(userBatteryDeposit) && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                if (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES)) {
                    query.setDeposit(userBatteryDeposit.getBeforeModifyDeposit());
                } else {
                    query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                }
            }
            
        } else {
            // 续费
            BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("USER BATTERY MEMBERCARD ERROR!not found batteryMemberCard,uid={},mid={}", SecurityUtils.getUid(), userBatteryMemberCard.getMemberCardId());
                return Collections.emptyList();
            }
            
            query.setDeposit(batteryMemberCard.getDeposit());
            
            if (CollectionUtils.isEmpty(userInfoGroupNamesBOs)) {
                query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            } else {
                // 用户绑定了用户分组
                query.setUserInfoGroupIdsForSearch(
                        userInfoGroupNamesBOs.stream().map(userInfoGroupNamesBO -> userInfoGroupNamesBO.getGroupId().toString()).collect(Collectors.toList()));
            }
            
            query.setBatteryV(
                    Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(SecurityUtils.getUid()) : null);
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(
                    userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES : null);
        }
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectByPageForUser(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        // 用户绑定的电池型号串数
        List<String> userBindBatteryType = null;
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            userBindBatteryType = userBatteryTypeService.selectByUid(SecurityUtils.getUid());
            if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                userBindBatteryType = userBindBatteryType.stream().map(item -> item.substring(item.lastIndexOf("_") + 1)).collect(Collectors.toList());
            }
        }

        List<BatteryMemberCardVO> result = new ArrayList<>();
        for (BatteryMemberCardAndTypeVO item : list) {
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                List<String> number = null;
                if (CollectionUtils.isNotEmpty(item.getBatteryType())) {
                    // 套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType()))
                            .map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
                }

                if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                    if (!(CollectionUtils.isNotEmpty(number) && CollectionUtils.containsAll(number, userBindBatteryType))) {
                        continue;
                    }
                }
            }
            
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            
            // 设置优惠券
            if (Objects.nonNull(item.getCouponId()) || StringUtils.isNotBlank(item.getCouponIds())) {
                dealCouponSearchVo(item.getCouponId(), item.getCouponIds(), batteryMemberCardVO);
            }
            
            // 设置分期套餐期数、剩余每期费用
            if (!Objects.equals(batteryMemberCardVO.getBusinessType(), BUSINESS_TYPE_INSTALLMENT_BATTERY)) {
                result.add(batteryMemberCardVO);
                continue;
            }
            
            int installmentNo = batteryMemberCardVO.getValidDays() / 30;
            batteryMemberCardVO.setInstallmentNo(installmentNo);
            if (installmentNo > 1) {
                BigDecimal rentPrice = batteryMemberCardVO.getRentPrice();
                BigDecimal downPayment = batteryMemberCardVO.getDownPayment();
                batteryMemberCardVO.setRemainingCost(rentPrice.subtract(downPayment).divide(new BigDecimal(String.valueOf(installmentNo - 1)), 2, RoundingMode.DOWN));
            } else {
                batteryMemberCardVO.setRemainingCost(new BigDecimal("0"));
            }
            result.add(batteryMemberCardVO);
        }
        
        return result;
    }
    
    @Override
    public List<String> selectMembercardBatteryV(BatteryMemberCardQuery query) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR!not found userInfo,uid={}", SecurityUtils.getUid());
            return Collections.emptyList();
        }
        
        // 未缴纳押金
        if (!Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, userInfo.getBatteryDepositStatus())) {
            List<BatteryMemberCardVO> list = this.batteryMemberCardMapper.selectMembercardBatteryV(query);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            
            return list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(NumberConstant.ZERO, userBatteryMemberCard.getCardPayCount())) {
            List<BatteryMemberCardVO> list = this.batteryMemberCardMapper.selectMembercardBatteryV(query);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.emptyList();
            }
            
            return list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
        }
        
        String batteryType = userBatteryTypeService.selectUserSimpleBatteryType(SecurityUtils.getUid());
        if (StringUtils.isBlank(batteryType)) {
            return Collections.emptyList();
        }
        
        return Collections.singletonList(batteryType);
    }
    
    @Slave
    @Override
    public Integer selectByPageCount(BatteryMemberCardQuery query) {
        
        // 当前端传递型号为字符串0时，为标准型号即套餐不分型号，t_member_card_battery_type中未存关联数据
        if (StringUtils.isNotEmpty(query.getBatteryModel()) && !("0".equals(query.getBatteryModel()))) {
            
            query.setOriginalBatteryModel(query.getBatteryModel());
            query.setBatteryModel(null);
        }
        
        return this.batteryMemberCardMapper.selectByPageCount(query);
    }
    
    @Override
    @Slave
    public List<BatteryMemberCardVO> selectCarRentalAndElectricityPackages(CarRentalPackageQryModel qryModel) {
        List<CarRentalPackagePo> carRentalPackagePOList = carRentalPackageService.list(qryModel);
        if (CollectionUtils.isEmpty(carRentalPackagePOList)) {
            return Collections.emptyList();
        }
        
        List<BatteryMemberCardVO> batteryMemberCardVOList = Lists.newArrayList();
        for (CarRentalPackagePo carRentalPackagePO : carRentalPackagePOList) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            batteryMemberCardVO.setId(carRentalPackagePO.getId());
            batteryMemberCardVO.setName(carRentalPackagePO.getName());
            batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
            batteryMemberCardVOList.add(batteryMemberCardVO);
        }
        
        return batteryMemberCardVOList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateStatus(BatteryMemberCardStatusQuery batteryModelQuery) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(batteryModelQuery.getId());
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        batteryMemberCard.setStatus(batteryModelQuery.getStatus());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        this.update(batteryMemberCard);
        operateRecordUtil.record(null, batteryMemberCard);
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(id);
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        if (Objects.nonNull(userBatteryMemberCardService.checkUserByMembercardId(id))) {
            return Triple.of(false, "100272", "当前套餐有用户使用，暂不支持删除");
        }
        
        if (Objects.nonNull(electricityMemberCardOrderService.checkOrderByMembercardId(id))) {
            return Triple.of(false, "100272", "当前套餐有用户使用，暂不支持删除");
        }
        
        // 套餐是否绑定企业
        if (Objects.nonNull(enterprisePackageService.selectByPackageId(id))) {
            return Triple.of(false, "100272", "删除失败，该套餐已绑定企业");
        }
        
        BatteryMemberCard batteryMemberCardUpdate = new BatteryMemberCard();
        batteryMemberCardUpdate.setId(id);
        batteryMemberCardUpdate.setDelFlag(BatteryMemberCard.DEL_DEL);
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(batteryMemberCardUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(BatteryMemberCardQuery query) {
        // 套餐名称长度最大为14
        if (Objects.nonNull(query.getName()) && query.getName().length() > 14) {
            return Triple.of(false, "100377", "参数校验错误");
        }
        
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        if (!Objects.equals(query.getName(), batteryMemberCard.getName()) && Objects.nonNull(
                this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }
        
        if (Objects.equals(query.getStatus(), BatteryMemberCard.STATUS_UP)) {
            return Triple.of(false, "100271", "请先下架套餐再进行编辑操作");
        }
        
        Triple<Boolean, String, Object> triple = checkFreeDepositConfig(query);
        if (triple.getLeft()) {
            return Triple.of(false, triple.getMiddle(), triple.getRight());
        }
        
        if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode().equals(query.getBusinessType()) && query.getValidDays() > INSTALLMENT_MAX_VALID_DAYS) {
            return Triple.of(false, "301022", "可分期数最多支持24期，请修改租期");
        }
        
        BatteryMemberCard batteryMemberCardUpdate = new BatteryMemberCard();
        batteryMemberCardUpdate.setId(batteryMemberCard.getId());
        batteryMemberCardUpdate.setName(query.getName());
        batteryMemberCardUpdate.setDeposit(query.getDeposit());
        batteryMemberCardUpdate.setRentPrice(query.getRentPrice());
        batteryMemberCardUpdate.setRentPriceUnit(query.getRentPriceUnit());
        batteryMemberCardUpdate.setValidDays(query.getValidDays());
        batteryMemberCardUpdate.setRentUnit(query.getRentUnit());
        batteryMemberCardUpdate.setRentType(query.getRentType());
        batteryMemberCardUpdate.setSendCoupon(query.getSendCoupon());
        batteryMemberCardUpdate.setCouponId(null);
        batteryMemberCardUpdate.setStatus(query.getStatus());
        batteryMemberCardUpdate.setUseCount(query.getUseCount());
        batteryMemberCardUpdate.setIsRefund(query.getIsRefund());
        batteryMemberCardUpdate.setRefundLimit(query.getRefundLimit());
        batteryMemberCardUpdate.setFreeDeposite(query.getFreeDeposite());
        batteryMemberCardUpdate.setServiceCharge(query.getServiceCharge());
        batteryMemberCardUpdate.setRemark(query.getRemark());
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardUpdate.setUserInfoGroupIds(Objects.isNull(query.getUserInfoGroupIdsTransfer()) ? null : JsonUtil.toJson(query.getUserInfoGroupIdsTransfer()));
        batteryMemberCardUpdate.setGroupType(query.getGroupType());
        batteryMemberCardUpdate.setBusinessType(query.getBusinessType());
        if (Objects.equals(query.getSendCoupon(), BatteryMemberCard.SEND_COUPON_NO)) {
            batteryMemberCardUpdate.setCouponIds(null);
        } else {
            batteryMemberCardUpdate.setCouponIds(CollectionUtils.isEmpty(query.getCouponIdsTransfer()) ? null : JsonUtil.toJson(query.getCouponIdsTransfer()));
        }
        batteryMemberCardUpdate.setInstallmentServiceFee(Objects.nonNull(query.getInstallmentServiceFee()) ? query.getInstallmentServiceFee() : null);
        if (Objects.equals(batteryMemberCard.getBusinessType(), BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode())) {
            batteryMemberCardUpdate.setDownPayment(Objects.equals(batteryMemberCardUpdate.getValidDays() / 30, 1) ? batteryMemberCardUpdate.getRentPrice() : query.getDownPayment());
        }
        
        this.update(batteryMemberCardUpdate);
        
        operateRecordUtil.asyncRecord(batteryMemberCard, batteryMemberCardUpdate, userInfoGroupService, couponService, (userInfoGroupService, couponService, operateLogDTO) -> {
            List<Long> oldUserGroupIds = JsonUtil.fromJsonArray((String) operateLogDTO.getOldValue().getOrDefault("userInfoGroupIds", "[]"), Long.class);
            List<UserInfoGroupBO> oldUserGroups = userInfoGroupService.listByIds(oldUserGroupIds);
            operateLogDTO.getOldValue().put("userGroups", Collections.emptyList());
            if (!org.springframework.util.CollectionUtils.isEmpty(oldUserGroups)) {
                operateLogDTO.getOldValue().put("userGroups", oldUserGroups.stream().map(UserInfoGroupBO::getGroupName).collect(Collectors.toList()));
            }
            
            List<Long> userGroupIds = JsonUtil.fromJsonArray((String) operateLogDTO.getNewValue().getOrDefault("userInfoGroupIds", "[]"), Long.class);
            List<UserInfoGroupBO> userGroups = userInfoGroupService.listByIds(userGroupIds);
            operateLogDTO.getNewValue().put("userGroups", Collections.emptyList());
            if (!org.springframework.util.CollectionUtils.isEmpty(userGroups)) {
                operateLogDTO.getNewValue().put("userGroups", userGroups.stream().map(UserInfoGroupBO::getGroupName).collect(Collectors.toList()));
            }
            
            List<Long> oldCouponIds = JsonUtil.fromJsonArray((String) operateLogDTO.getOldValue().getOrDefault("couponIds", "[]"), Long.class);
            List<CarCouponNamePO> oldCoupons = couponService.queryListByIdsFromCache(oldCouponIds);
            operateLogDTO.getOldValue().put("coupons", Collections.emptyList());
            if (!org.springframework.util.CollectionUtils.isEmpty(oldCoupons)) {
                operateLogDTO.getOldValue().put("coupons", oldCoupons.stream().map(CarCouponNamePO::getName).collect(Collectors.toList()));
            }
            
            List<Long> couponIds = JsonUtil.fromJsonArray((String) operateLogDTO.getNewValue().getOrDefault("couponIds", "[]"), Long.class);
            List<CarCouponNamePO> coupons = couponService.queryListByIdsFromCache(couponIds);
            operateLogDTO.getNewValue().put("coupons", Collections.emptyList());
            if (!org.springframework.util.CollectionUtils.isEmpty(coupons)) {
                operateLogDTO.getNewValue().put("coupons", coupons.stream().map(CarCouponNamePO::getName).collect(Collectors.toList()));
            }
            return operateLogDTO;
        });
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(BatteryMemberCardQuery query) {
        // 套餐名称长度最大为14
        if (Objects.nonNull(query.getName()) && query.getName().length() > 14) {
            return Triple.of(false, "100377", "参数校验错误");
        }
        
        if (Objects.nonNull(this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "100106", "加盟商不存在");
        }
        
        Triple<Boolean, String, Object> verifyBatteryMemberCardResult = verifyBatteryMemberCardQuery(query, franchisee);
        if (Boolean.FALSE.equals(verifyBatteryMemberCardResult.getLeft())) {
            return verifyBatteryMemberCardResult;
        }
        
        Triple<Boolean, String, Object> triple = checkFreeDepositConfig(query);
        if (triple.getLeft()) {
            return Triple.of(false, triple.getMiddle(), triple.getRight());
        }
        
        // 套餐数量最多150个，仅对换电套餐做限制
        List<Integer> typeList = Arrays.asList(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode(),
                BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode());
        if (Objects.equals(query.getBusinessType(), BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode())) {
            BatteryMemberCardQuery queryCount = BatteryMemberCardQuery.builder().businessTypes(typeList)
                    .tenantId(TenantContextHolder.getTenantId()).delFlag(BatteryMemberCard.DEL_NORMAL).build();
            
            if (selectByPageCount(queryCount) >= BatteryMemberCardConstants.MAX_BATTERY_MEMBER_CARD_NUM) {
                return Triple.of(false, "100378", "换电套餐新增已达最大上限，可删除多余套餐后操作");
            }
        }
        
        if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode().equals(query.getBusinessType()) && query.getValidDays() > INSTALLMENT_MAX_VALID_DAYS) {
            return Triple.of(false, "301022", "可分期数最多支持24期，请修改租期");
        }
        
        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        BeanUtils.copyProperties(query, batteryMemberCard);
        batteryMemberCard.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCard.setCreateTime(System.currentTimeMillis());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        batteryMemberCard.setSortParam(System.currentTimeMillis());
        batteryMemberCard.setTenantId(TenantContextHolder.getTenantId());
        batteryMemberCard.setUserInfoGroupIds(CollectionUtils.isEmpty(query.getUserInfoGroupIdsTransfer()) ? null : JsonUtil.toJson(query.getUserInfoGroupIdsTransfer()));
        if (Objects.equals(query.getSendCoupon(), BatteryMemberCard.SEND_COUPON_NO)) {
            batteryMemberCard.setCouponIds(null);
        } else {
            batteryMemberCard.setCouponIds(CollectionUtils.isEmpty(query.getCouponIdsTransfer()) ? null : JsonUtil.toJson(query.getCouponIdsTransfer()));
        }
        
        // 校验并设置套餐的业务类型，以及相关业务参数
        if (Objects.equals(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode(), query.getBusinessType())) {
            batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode());
        } else if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(query.getBusinessType())) {
            batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode());
        } else if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode().equals(query.getBusinessType())) {
            batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_INSTALLMENT_BATTERY.getCode());
            batteryMemberCard.setInstallmentServiceFee(Objects.nonNull(query.getInstallmentServiceFee()) ? query.getInstallmentServiceFee() : null);
            batteryMemberCard.setDownPayment(Objects.equals(batteryMemberCard.getValidDays() / 30, 1) ? batteryMemberCard.getRentPrice() : query.getDownPayment());
        } else {
            return Triple.of(false, "100107", "业务类型参数不正确");
        }
        
        this.batteryMemberCardMapper.insert(batteryMemberCard);
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && CollectionUtils.isNotEmpty(query.getBatteryModels())) {
            memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(query.getBatteryModels(), batteryMemberCard.getId()));
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkFreeDepositConfig(BatteryMemberCardQuery query) {
        // 免押套餐校验免押配置
        if (Objects.equals(query.getFreeDeposite(), BatteryMemberCard.FREE_DEPOSIT)) {
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            
            if (Objects.isNull(pxzConfig) && Objects.isNull(fyConfig)) {
                return Triple.of(true, "100380", "请先完成免押配置，再新增免押套餐");
            }
        }
        return Triple.of(false, null, null);
    }
    
    @Override
    public Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder) {
        return Objects.equals(BatteryMemberCard.RENT_UNIT_MINUTES, batteryMemberCard.getRentUnit()) ? memberCardOrder.getValidDays() * 60 * 1000L
                : memberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
    }
    
    /**
     * 计算套餐有效时长
     */
    @Override
    public Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, Long validDays) {
        return Objects.equals(BatteryMemberCard.RENT_UNIT_MINUTES, batteryMemberCard.getRentUnit()) ? validDays * 60 * 1000L : validDays * 24 * 60 * 60 * 1000L;
    }
    
    @Override
    @Slave
    public List<BatteryMemberCardVO> listMemberCardForSort(TokenUser tokenUser) {
        List<Long> franchiseeIds = null;
        if (Objects.equals(tokenUser.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = (userDataScopeService.selectDataIdByUid(tokenUser.getUid()));
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return Collections.emptyList();
            }
        }
        
        if (Objects.equals(tokenUser.getDataType(), User.DATA_TYPE_STORE)) {
            List<Long> storeIds = userDataScopeService.selectDataIdByUid(tokenUser.getUid());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(storeIds)) {
                franchiseeIds = storeIds.stream().map(storeId -> storeService.queryByIdFromCache(storeId).getFranchiseeId()).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return Collections.emptyList();
            }
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).build();
        return batteryMemberCardMapper.selectListMemberCardForSort(query);
    }
    
    @Override
    @Slave
    public List<BatteryMemberCardVO> listSuperAdminPage(BatteryMemberCardQuery query) {
        // 当前端传递型号为字符串0时，为标准型号即套餐不分型号，t_member_card_battery_type中未存关联数据
        if (StringUtils.isNotEmpty(query.getBatteryModel()) && !(BatteryMemberCardConstants.REGARDLESS_OF_MODEL.equals(query.getBatteryModel()))) {
            query.setOriginalBatteryModel(query.getBatteryModel());
            query.setBatteryModel(null);
        }
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectListSuperAdminPage(query);
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            
            if (Objects.nonNull(item.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(item.getTenantId());
                batteryMemberCardVO.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                batteryMemberCardVO.setFranchiseeName(franchisee.getName());
            }
            
            // 设置电池型号
            if (!item.getBatteryType().isEmpty()) {
                
                List<String> originalBatteryModels = item.getBatteryType().stream().map(MemberCardBatteryType::getBatteryType).distinct().collect(Collectors.toList());
                batteryMemberCardVO.setBatteryModels(batteryModelService.selectShortBatteryType(originalBatteryModels, item.getTenantId()));
            }
            
            // 设置优惠券
            if (Objects.equals(item.getSendCoupon(), BatteryMemberCard.SEND_COUPON_YES)) {
                List<CouponSearchVo> coupons = new ArrayList<>();
                HashSet<Integer> couponIdsSet = new HashSet<>();
                if (Objects.nonNull(item.getCouponId())) {
                    couponIdsSet.add(item.getCouponId());
                }
                if (StringUtils.isNotBlank(item.getCouponIds())) {
                    couponIdsSet.addAll(JsonUtil.fromJsonArray(item.getCouponIds(), Integer.class));
                }
                
                if (!CollectionUtils.isEmpty(couponIdsSet)) {
                    couponIdsSet.forEach(couponId -> {
                        CouponSearchVo couponSearchVo = new CouponSearchVo();
                        Coupon coupon = couponService.queryByIdFromCache(couponId);
                        if (Objects.nonNull(coupon)) {
                            BeanUtils.copyProperties(coupon, couponSearchVo);
                            couponSearchVo.setId(coupon.getId().longValue());
                            coupons.add(couponSearchVo);
                        }
                    });
                }
                batteryMemberCardVO.setCoupons(coupons);
            }
            
            // 设置用户分组
            if (StringUtils.isNotBlank(item.getUserInfoGroupIds())) {
                List<SearchVo> userInfoGroups = new ArrayList<>();
                List<Long> userInfoGroupIds = JsonUtil.fromJsonArray(item.getUserInfoGroupIds(), Long.class);
                
                if (CollectionUtils.isNotEmpty(userInfoGroupIds)) {
                    for (Long userInfoGroupId : userInfoGroupIds) {
                        SearchVo searchVo = new SearchVo();
                        UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(userInfoGroupId);
                        if (Objects.nonNull(userInfoGroup)) {
                            BeanUtils.copyProperties(userInfoGroup, searchVo);
                            userInfoGroups.add(searchVo);
                        }
                    }
                }
                batteryMemberCardVO.setUserInfoGroups(userInfoGroups);
            }
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
    
    private List<MemberCardBatteryType> buildMemberCardBatteryTypeList(List<String> batteryModels, Long mid) {
        
        List<MemberCardBatteryType> memberCardBatteryTypeList = Lists.newArrayList();
        
        for (String batteryModel : batteryModels) {
            MemberCardBatteryType memberCardBatteryType = new MemberCardBatteryType();
            memberCardBatteryType.setBatteryType(batteryModel);
            memberCardBatteryType.setBatteryV(
                    batteryModel.substring(batteryModel.indexOf("_") + 1).substring(0, batteryModel.substring(batteryModel.indexOf("_") + 1).indexOf("_")));
            memberCardBatteryType.setMid(mid);
            memberCardBatteryType.setTenantId(TenantContextHolder.getTenantId());
            memberCardBatteryType.setCreateTime(System.currentTimeMillis());
            memberCardBatteryType.setUpdateTime(System.currentTimeMillis());
            
            memberCardBatteryTypeList.add(memberCardBatteryType);
        }
        
        return memberCardBatteryTypeList;
    }
    
    private Triple<Boolean, String, Object> verifyBatteryMemberCardQuery(BatteryMemberCardQuery query, Franchisee franchisee) {
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            return Triple.of(true, null, null);
        }
        
        List<String> list = query.getBatteryModels().stream().map(item -> item.substring(item.indexOf("_") + 1).substring(0, item.substring(item.indexOf("_") + 1).indexOf("_")))
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list) || list.size() != 1) {
            return Triple.of(false, "100273", "套餐电池型号电压不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    private void dealCouponSearchVo(Integer couponId, String couponIds, BatteryMemberCardVO batteryMemberCardVO) {
        LinkedHashSet<Integer> couponIdSet = new LinkedHashSet<>();
        ArrayList<CouponSearchVo> couponSearchVos = new ArrayList<>();
        batteryMemberCardVO.setAmount(BigDecimal.ZERO);
        if (Objects.nonNull(couponId)) {
            couponIdSet.add(couponId);
        }
        if (StringUtils.isNotBlank(couponIds)) {
            couponIdSet.addAll(JsonUtil.fromJsonArray(couponIds, Integer.class));
        }
        
        couponIdSet.forEach(couponIdFromSet -> {
            CouponSearchVo couponSearchVo = new CouponSearchVo();
            Coupon coupon = couponService.queryByIdFromCache(couponIdFromSet);
            if (Objects.nonNull(coupon)) {
                BeanUtils.copyProperties(coupon, couponSearchVo);
                couponSearchVo.setId(coupon.getId().longValue());
            }
            
            // 兼容旧版本小程序，取优惠金额最大的优惠券的金额与name展示
            if (Objects.nonNull(couponSearchVo.getAmount()) && couponSearchVo.getAmount().compareTo(batteryMemberCardVO.getAmount()) > 0) {
                batteryMemberCardVO.setCouponId(couponSearchVo.getId().intValue());
                batteryMemberCardVO.setAmount(couponSearchVo.getAmount());
                batteryMemberCardVO.setCouponName(couponSearchVo.getName());
            }
            couponSearchVos.add(couponSearchVo);
        });
        
        batteryMemberCardVO.setCoupons(couponSearchVos);
    }
    
    @Override
    public List<BatteryMemberCardVO> selectByPageForMerchant(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByPageForMerchant(query);
        
        // 减少查询数据库次数，降低接口响应时间
        List<Long> memberCardIds = list.parallelStream().map(BatteryMemberCard::getId).collect(Collectors.toList());
        Map<Long, List<String>> batteryModels;
        if (CollectionUtils.isNotEmpty(memberCardIds)) {
            batteryModels = batteryModelService.listShortBatteryTypeByMemberIds(memberCardIds, TenantContextHolder.getTenantId()).parallelStream()
                    .collect(Collectors.toMap(BatteryModelDTO::getMid, BatteryModelDTO::getBatteryModels));
        } else {
            batteryModels = null;
        }
        
        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            batteryMemberCardVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            
            if (Objects.nonNull(franchisee) && Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && MapUtils.isNotEmpty(batteryModels)) {
                batteryMemberCardVO.setBatteryModels(batteryModels.get(item.getId()));
            }
            
            if (Objects.nonNull(item.getCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(item.getCouponId());
                batteryMemberCardVO.setCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
            }
            
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }
}
