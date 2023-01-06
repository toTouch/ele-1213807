package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityMemberCardVO;
import com.xiliulou.electricity.vo.OldUserActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:28
 **/
@Service
@Slf4j
public class ElectricityMemberCardServiceImpl extends ServiceImpl<ElectricityMemberCardMapper, ElectricityMemberCard> implements ElectricityMemberCardService {

    @Autowired
    RedisService redisService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    UserService userService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    StoreService storeService;

    @Autowired
    OldUserActivityService oldUserActivityService;

    @Autowired
    CouponService couponService;

    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    UserBatteryService userBatteryService;
    @Autowired
    UserCarService userCarService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    /**
     * 新增卡包
     *
     * @param electricityMemberCard
     * @return
     */
    @Override
    public R add(ElectricityMemberCard electricityMemberCard) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Integer count = baseMapper.queryCount(null, electricityMemberCard.getType(), tenantId, null, null, electricityMemberCard.getName());
        if (count > 0) {
            log.error("ELE ERROR! create memberCard fail,there are same memberCardName,memberCardName={}", electricityMemberCard.getName());
            return R.fail("100104", "套餐名称已存在！");
        }

        //校验参数
        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
        }

        //填充参数
        electricityMemberCard.setCreateTime(System.currentTimeMillis());
        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        electricityMemberCard.setStatus(ElectricityMemberCard.STATUS_UN_USEABLE);
        electricityMemberCard.setTenantId(tenantId);
        electricityMemberCard.setDelFlag(ElectricityMemberCard.DEL_NORMAL);
        if (StringUtils.isNotEmpty(electricityMemberCard.getBatteryType())) {
            electricityMemberCard.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(electricityMemberCard.getBatteryType())));
        }

        Integer insert = baseMapper.insert(electricityMemberCard);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            return null;
        });

        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 修改月卡
     *
     * @param electricityMemberCard
     * @return
     */
    @Override
    public R update(ElectricityMemberCard electricityMemberCard) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityMemberCard oldElectricityMemberCard = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getId, electricityMemberCard.getId()).eq(ElectricityMemberCard::getTenantId, tenantId));

        if (Objects.isNull(oldElectricityMemberCard)) {
            return R.ok();
        }

        Integer count = baseMapper.queryCount(null, electricityMemberCard.getType(), tenantId, null, null, electricityMemberCard.getName());

        if (count > 0 && !Objects.equals(oldElectricityMemberCard.getName(), electricityMemberCard.getName())) {
            log.error("ELE ERROR! create memberCard fail,there are same memberCardName,memberCardName={}", electricityMemberCard.getName());
            return R.fail("100104", "套餐名称已存在！");
        }

        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(electricityMemberCard.getLimitCount())) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
            }
        }

        if (StringUtils.isNotEmpty(electricityMemberCard.getBatteryType())) {
            electricityMemberCard.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(electricityMemberCard.getBatteryType())));
        }

        Integer update = baseMapper.update(electricityMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //先删再改
            redisService.delete(CacheConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * @param id
     * @return
     */
    @Override
    public R delete(Integer id) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //判断是否有用户绑定该套餐
//        List<FranchiseeUserInfo> franchiseeUserInfoList = franchiseeUserInfoService.selectByMemberCardId(id, tenantId);

        List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectByMemberCardId(id, tenantId);
        if (!CollectionUtils.isEmpty(userBatteryMemberCardList)) {
            log.error("ELE ERROR! delete memberCard fail,there are user use memberCard,memberCardId={}", id);
            return R.fail(queryByCache(id), "100100", "删除失败，该套餐已有用户使用！");
        }

        ElectricityMemberCard electricityMemberCard = new ElectricityMemberCard();
        electricityMemberCard.setId(id);
        electricityMemberCard.setDelFlag(ElectricityMemberCard.DEL_DEL);
        electricityMemberCard.setTenantId(tenantId);
        Integer update = baseMapper.update(electricityMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(CacheConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 分页
     *
     * @param offset
     * @param size
     * @return
     */
    @Override
    @DS("slave_1")
    public R queryList(Long offset, Long size, Integer status, Integer type, Integer tenantId, Integer cardModel, List<Long> franchiseeIds) {
        List<ElectricityMemberCardVO> electricityMemberCardList = baseMapper.queryList(offset, size, status, type, tenantId, cardModel, franchiseeIds, null);
        if (ObjectUtil.isEmpty(electricityMemberCardList)) {
            return R.ok(electricityMemberCardList);
        }
        List<ElectricityMemberCardVO> electricityMemberCardVOList = new ArrayList<>();
        for (ElectricityMemberCardVO electricityMemberCard : electricityMemberCardList) {
            ElectricityMemberCardVO electricityMemberCardVO = new ElectricityMemberCardVO();
            BeanUtils.copyProperties(electricityMemberCard, electricityMemberCardVO);

            if (StringUtils.isNotEmpty(electricityMemberCardVO.getBatteryType())) {
                electricityMemberCardVO.setBatteryType(BatteryConstant.acquireBattery(electricityMemberCardVO.getBatteryType()).toString());
            }

            if (Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCard.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());
                if (Objects.nonNull(oldUserActivity)) {

                    OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
                    BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);

                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {

                        Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                        if (Objects.nonNull(coupon)) {
                            oldUserActivityVO.setCoupon(coupon);
                        }

                    }
                    electricityMemberCardVO.setOldUserActivityVO(oldUserActivityVO);
                }
            }
            electricityMemberCardVOList.add(electricityMemberCardVO);
        }
        return R.ok(electricityMemberCardVOList);
    }

    @Override
    public R queryUserList(Long offset, Long size, String productKey, String deviceName, Long franchiseeId) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.isNull(franchiseeId) && Objects.nonNull(productKey) && Objects.nonNull(deviceName)) {
            //换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(electricityCabinet)) {
                log.error("rentBattery  ERROR! not found electricityCabinet ！productKey={},deviceName={}", productKey, deviceName);
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }

            //3、查出套餐
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("queryByDevice  ERROR! not found store ！electricityCabinetId={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("queryByDevice  ERROR! not found store ！storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("queryByDevice  ERROR! not found Franchisee ！storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            franchiseeId = store.getFranchiseeId();
        }

        //判断用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("rentBattery  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("rentBattery  ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("rentBattery  ERROR! not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("rentBattery  ERROR! not pay deposit! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (Objects.nonNull(franchiseeId) && !Objects.equals(franchiseeId, userInfo.getFranchiseeId())) {
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), franchiseeId, userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }

        franchiseeId = userInfo.getFranchiseeId();

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE ERROR! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }

        List<ElectricityMemberCard> electricityMemberCardList = new ArrayList<>();
        //多电池型号查询套餐
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBattery)) {
                log.error("ELE ERROR! not found userBattery,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0033", "用户未绑定电池型号");
            }

            if (Objects.isNull(userBattery.getBatteryType())) {
                return R.ok();
            }
            electricityMemberCardList = baseMapper.queryUserList(offset, size, franchiseeId, userBattery.getBatteryType(), ElectricityMemberCard.ELECTRICITY_MEMBER_CARD);
        } else {
            electricityMemberCardList = baseMapper.queryUserList(offset, size, franchiseeId, null, ElectricityMemberCard.ELECTRICITY_MEMBER_CARD);
        }

        if (ObjectUtil.isEmpty(electricityMemberCardList)) {
            return R.ok(electricityMemberCardList);
        }

        List<ElectricityMemberCardVO> electricityMemberCardVOList = new ArrayList<>();
        for (ElectricityMemberCard electricityMemberCard : electricityMemberCardList) {
            ElectricityMemberCardVO electricityMemberCardVO = new ElectricityMemberCardVO();
            BeanUtils.copyProperties(electricityMemberCard, electricityMemberCardVO);

            if (Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCard.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());
                if (Objects.nonNull(oldUserActivity)) {

                    OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
                    BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);

                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {

                        Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                        if (Objects.nonNull(coupon)) {
                            oldUserActivityVO.setCoupon(coupon);
                        }

                    }
                    electricityMemberCardVO.setOldUserActivityVO(oldUserActivityVO);
                }
            }

            electricityMemberCardVOList.add(electricityMemberCardVO);
        }

        return R.ok(electricityMemberCardVOList);
    }


    @Override
    public R queryFirstPayMemberCard(Long offset, Long size, String productKey, String deviceName, Long franchiseeId, Integer model) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // TODO: 2022/12/21 判断空串 删掉

        //判断用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("rentBattery  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("rentBattery  ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }


        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE ERROR! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }


        List<ElectricityMemberCard> electricityMemberCardList;

        //多电池型号查询套餐
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            electricityMemberCardList = baseMapper.queryUserList(offset, size, franchiseeId, BatteryConstant.acquireBatteryShort(model), ElectricityMemberCard.ELECTRICITY_MEMBER_CARD);
        } else {
            electricityMemberCardList = baseMapper.queryUserList(offset, size, franchiseeId, null, ElectricityMemberCard.ELECTRICITY_MEMBER_CARD);
        }

        if (ObjectUtil.isEmpty(electricityMemberCardList)) {
            return R.ok(electricityMemberCardList);
        }

        List<ElectricityMemberCardVO> electricityMemberCardVOList = new ArrayList<>();
        for (ElectricityMemberCard electricityMemberCard : electricityMemberCardList) {
            ElectricityMemberCardVO electricityMemberCardVO = new ElectricityMemberCardVO();
            BeanUtils.copyProperties(electricityMemberCard, electricityMemberCardVO);

            if (Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCard.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());
                if (Objects.nonNull(oldUserActivity)) {

                    OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
                    BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);

                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {

                        Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                        if (Objects.nonNull(coupon)) {
                            oldUserActivityVO.setCoupon(coupon);
                        }

                    }
                    electricityMemberCardVO.setOldUserActivityVO(oldUserActivityVO);
                }
            }

            electricityMemberCardVOList.add(electricityMemberCardVO);
        }

        return R.ok(electricityMemberCardVOList);
    }

    @Override
    public R queryRentCarMemberCardList(Long offset, Long size) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentCar  ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("rentCar  ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("rentCar  ERROR! user is unUsable,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("rentCar  ERROR! not auth! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        UserCar userCar = userCarService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCar)) {
            log.error("rentCar ERROR! not found userCar,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未找到用户
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("rentCar  ERROR! not found user! userId:{}", user.getUid());
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//
//        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("rentCar  ERROR! not pay car deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //用户押金缴纳的车辆型号所属的加盟商
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("rentCar ERROR! franchisee not carModel,carModelId={}", userCar.getCarModel());
            return R.fail("100011", "加盟商没有对应的车辆型号");
        }

        List<Long> franchiseeIds = Arrays.asList(electricityCarModel.getFranchiseeId());

        return R.ok(baseMapper.queryList(offset, size, ElectricityMemberCard.STATUS_USEABLE, null, userInfo.getTenantId(), ElectricityMemberCard.RENT_CAR_MEMBER_CARD, franchiseeIds, userCar.getCarModel().intValue()));
    }

    @Override
    public List<ElectricityMemberCard> queryByFranchisee(Long id) {
        return baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getFranchiseeId, id));
    }

    @Override
    public List<ElectricityMemberCard> getElectricityUsableBatteryList(Long id, Integer tenantId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getFranchiseeId, id)
                .eq(ElectricityMemberCard::getDelFlag, ElectricityMemberCard.DEL_NORMAL).eq(ElectricityMemberCard::getStatus, ElectricityMemberCard.STATUS_USEABLE)
                .eq(ElectricityMemberCard::getTenantId, tenantId)
                .eq(ElectricityMemberCard::getCardModel, ElectricityMemberCard.ELECTRICITY_MEMBER_CARD));
    }

    @Override
    public List<ElectricityMemberCard> selectByFranchiseeId(Long id, Integer tenantId) {
        return baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getFranchiseeId, id)
                .eq(ElectricityMemberCard::getTenantId, tenantId)
                .eq(ElectricityMemberCard::getDelFlag, ElectricityMemberCard.DEL_NORMAL)
                .eq(ElectricityMemberCard::getCardModel, ElectricityMemberCard.ELECTRICITY_MEMBER_CARD));
    }

    @Override
    public R queryCount(Integer status, Integer type, Integer tenantId, Integer cardModel, List<Long> franchiseeId) {
        return R.ok(baseMapper.queryCount(status, type, tenantId, cardModel, franchiseeId, null));
    }

    @Override
    public R listByFranchisee(Long offset, Long size, Integer status, Integer type, Integer tenantId, List<Long> franchiseeIds) {
        return R.ok(baseMapper.listByFranchisee(offset, size, status, type, tenantId, franchiseeIds));
    }

    @Override
    public R listCountByFranchisee(Integer status, Integer type, Integer tenantId, List<Long> franchiseeIds) {
        return R.ok(baseMapper.listCountByFranchisee(status, type, tenantId, franchiseeIds));
    }

    @Override
    public ElectricityMemberCard selectUserMemberCardById(Integer id) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getId, id)
                .eq(ElectricityMemberCard::getStatus, ElectricityMemberCard.STATUS_USEABLE));
    }

    @Override
    public void unbindActivity(Integer id) {
        List<ElectricityMemberCard> electricityMemberCardList = baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>()
                .eq(ElectricityMemberCard::getActivityId, id));

        if (ObjectUtil.isEmpty(electricityMemberCardList)) {
            return;
        }

        for (ElectricityMemberCard electricityMemberCard : electricityMemberCardList) {
            baseMapper.unbindActivity(electricityMemberCard.getId());

            //删除缓存
            redisService.delete(CacheConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId());
        }

    }

    @Override
    public R queryDisableMemberCardList(Long offset, Long size) {
        return null;
    }

    /**
     * 获取套餐
     *
     * @param id
     * @return
     */
    @Override
    public ElectricityMemberCard queryByCache(Integer id) {
        ElectricityMemberCard electricityMemberCard = null;
        electricityMemberCard = redisService.getWithHash(CacheConstant.CACHE_MEMBER_CARD + id, ElectricityMemberCard.class);
        if (Objects.isNull(electricityMemberCard)) {
            electricityMemberCard = baseMapper.selectById(id);
            if (Objects.nonNull(electricityMemberCard)) {
                redisService.saveWithHash(CacheConstant.CACHE_MEMBER_CARD + id, electricityMemberCard);
            }
        }
        return electricityMemberCard;
    }

}
