package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardSearchVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserBatteryTypeService userBatteryTypeService;

    @Autowired
    private FranchiseeService franchiseeService;

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
            log.error("USER BATTERY MEMBERCARD ERROR!not found franchisee,uid={},franchiseeId={}", userInfo.getUid(), query.getFranchiseeId());
            return Collections.emptyList();
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() <= 0) {
            //新租
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
        } else if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            //非新租 购买押金套餐
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));

            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userBatteryMemberCard.getUid());
            if (Objects.nonNull(userBatteryDeposit)) {
                query.setDeposit(userBatteryDeposit.getBatteryDeposit());
            }

        } else {
            //续费
            BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("USER BATTERY MEMBERCARD ERROR!not found batteryMemberCard,uid={},mid={}", SecurityUtils.getUid(), userBatteryMemberCard.getMemberCardId());
                return Collections.emptyList();
            }

            query.setDeposit(batteryMemberCard.getDeposit());
            query.setLimitCount(batteryMemberCard.getLimitCount());
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setBatteryV(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()) : null);
        }

        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectByPageForUser(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        //用户绑定的电池型号串数
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
                    //套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType())).map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
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

            result.add(batteryMemberCardVO);
        }

        return result;

/*
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Collections.emptyList();
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(query.getUid());
        if (Objects.nonNull(userBatteryDeposit)) {
            query.setDeposit(userBatteryDeposit.getBatteryDeposit());
        }

        query.setFranchiseeId(userInfo.getFranchiseeId());

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            query.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
            query.setRentTypes(userBatteryMemberCard.getCardPayCount() > 0 ? Arrays.asList(BatteryMemberCard.RENT_TYPE_UNLIMIT, BatteryMemberCard.RENT_TYPE_OLD) : Arrays.asList(BatteryMemberCard.RENT_TYPE_UNLIMIT, BatteryMemberCard.RENT_TYPE_NEW));
        }

        query.setBatteryV(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));

        //获取用户可购买套餐列表
        List<BatteryMemberCardAndTypeVO> batteryMemberCardList = this.batteryMemberCardMapper.selectByPageForUser(query);
        if (CollectionUtils.isEmpty(batteryMemberCardList)) {
            log.error("ELE ERROR!batteryMemberCardList is empty,uid={}", userInfo.getUid());
            return Collections.emptyList();
        }

        return batteryMemberCardList.stream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());

        */
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
        return this.batteryMemberCardMapper.selectList(new LambdaQueryWrapper<BatteryMemberCard>().eq(BatteryMemberCard::getBusinessType, BatteryMemberCard.BUSINESS_TYPE_BATTERY).eq(BatteryMemberCard::getCouponId, couponId).eq(BatteryMemberCard::getDelFlag, BatteryMemberCard.DEL_NORMAL));
    }

    @Slave
    @Override
    public Integer isMemberCardBindFranchinsee(Long id, Integer tenantId) {
        return this.batteryMemberCardMapper.isMemberCardBindFranchinsee(id,tenantId);
    }

    @Slave
    @Override
    public List<BatteryMemberCardVO> selectByPage(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByPage(query);

        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            batteryMemberCardVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            if (Objects.nonNull(franchisee) && Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                batteryMemberCardVO.setBatteryModels(batteryModelService.selectShortBatteryType(memberCardBatteryTypeService.selectBatteryTypeByMid(item.getId()), item.getTenantId()));
            }

            if (Objects.nonNull(item.getCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(item.getCouponId());
                batteryMemberCardVO.setCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
            }

            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }

    @Override
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
            log.error("USER BATTERY MEMBERCARD ERROR!not found franchisee,uid={},franchiseeId={}", SecurityUtils.getUid(), query.getFranchiseeId());
            return Collections.emptyList();
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(SecurityUtils.getUid());

        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() <= 0) {
            //新租
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
        } else if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            //非新租 购买押金套餐
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));

            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userBatteryMemberCard.getUid());
            if (Objects.nonNull(userBatteryDeposit)) {
                query.setDeposit(userBatteryDeposit.getBatteryDeposit());
            }

        } else {
            //续费
            BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("USER BATTERY MEMBERCARD ERROR!not found batteryMemberCard,uid={},mid={}", SecurityUtils.getUid(), userBatteryMemberCard.getMemberCardId());
                return Collections.emptyList();
            }

            query.setDeposit(batteryMemberCard.getDeposit());
            query.setLimitCount(batteryMemberCard.getLimitCount());
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setBatteryV(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(SecurityUtils.getUid()) : null);
        }

        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectByPageForUser(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        //用户绑定的电池型号串数
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
                    //套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType())).map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
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
                batteryMemberCardVO.setAmount(Objects.isNull(coupon) ? null : coupon.getAmount());
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

        //未缴纳押金
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
        return this.batteryMemberCardMapper.selectByPageCount(query);
    }

    @Override
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

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(id);
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }

        if (Objects.nonNull(electricityMemberCardOrderService.checkOrderByMembercardId(id))) {
            return Triple.of(false, "100272", "删除失败，该套餐已生成订单");
        }
        
        //套餐是否绑定企业
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
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }

        if (!Objects.equals(query.getName(), batteryMemberCard.getName()) && Objects.nonNull(this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }

        if (Objects.equals(query.getStatus(), BatteryMemberCard.STATUS_UP)) {
            return Triple.of(false, "100271", "请先下架套餐再进行编辑操作");
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
        batteryMemberCardUpdate.setCouponId(Objects.equals(query.getSendCoupon(), BatteryMemberCard.NO) ? null : query.getCouponId());
        batteryMemberCardUpdate.setStatus(query.getStatus());
        batteryMemberCardUpdate.setUseCount(query.getUseCount());
        batteryMemberCardUpdate.setIsRefund(query.getIsRefund());
        batteryMemberCardUpdate.setRefundLimit(query.getRefundLimit());
        batteryMemberCardUpdate.setFreeDeposite(query.getFreeDeposite());
        batteryMemberCardUpdate.setServiceCharge(query.getServiceCharge());
        batteryMemberCardUpdate.setRemark(query.getRemark());
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(batteryMemberCardUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(BatteryMemberCardQuery query) {
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

        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        BeanUtils.copyProperties(query, batteryMemberCard);
        batteryMemberCard.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCard.setCreateTime(System.currentTimeMillis());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        batteryMemberCard.setTenantId(TenantContextHolder.getTenantId());
        
        //适配企业渠道添加套餐业务
        if(Objects.isNull(query.getBusinessType())){
            batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode());
        }else if(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(query.getBusinessType())){
            batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode());
        }else{
            return Triple.of(false, "100107", "业务类型参数不正确");
        }

        this.batteryMemberCardMapper.insert(batteryMemberCard);

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && CollectionUtils.isNotEmpty(query.getBatteryModels())) {
            memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(query.getBatteryModels(), batteryMemberCard.getId()));
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder) {
        return Objects.equals(BatteryMemberCard.RENT_UNIT_MINUTES, batteryMemberCard.getRentUnit()) ? memberCardOrder.getValidDays() * 60 * 1000L : memberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
    }

    /**
     * 计算套餐有效时长
     */
    @Override
    public Long transformBatteryMembercardEffectiveTime(BatteryMemberCard batteryMemberCard, Long validDays) {
        return Objects.equals(BatteryMemberCard.RENT_UNIT_MINUTES, batteryMemberCard.getRentUnit()) ? validDays * 60 * 1000L : validDays * 24 * 60 * 60 * 1000L;
    }

    private List<MemberCardBatteryType> buildMemberCardBatteryTypeList(List<String> batteryModels, Long mid) {

        List<MemberCardBatteryType> memberCardBatteryTypeList = Lists.newArrayList();

        for (String batteryModel : batteryModels) {
            MemberCardBatteryType memberCardBatteryType = new MemberCardBatteryType();
            memberCardBatteryType.setBatteryType(batteryModel);
            memberCardBatteryType.setBatteryV(batteryModel.substring(batteryModel.indexOf("_") + 1).substring(0, batteryModel.substring(batteryModel.indexOf("_") + 1).indexOf("_")));
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

        List<String> list = query.getBatteryModels().stream().map(item -> item.substring(item.indexOf("_") + 1).substring(0, item.substring(item.indexOf("_") + 1).indexOf("_"))).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list) || list.size() != 1) {
            return Triple.of(false, "100273", "套餐电池型号电压不一致");
        }

        return Triple.of(true, null, null);
    }

    private Long calculateValidDays(BatteryMemberCardQuery query) {
        return Objects.equals(query.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) ? query.getValidDays() * 24 * 60 * 60 * 1000L : query.getValidDays() * 60 * 1000L;
    }

}
