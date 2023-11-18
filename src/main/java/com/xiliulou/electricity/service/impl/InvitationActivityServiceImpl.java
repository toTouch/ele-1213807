package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.InvitationActivityMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.InvitationActivityMemberCardVO;
import com.xiliulou.electricity.vo.InvitationActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * (InvitationActivity)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-01 15:55:48d
 */
@Service("invitationActivityService")
@Slf4j
public class InvitationActivityServiceImpl implements InvitationActivityService {
    @Resource
    private InvitationActivityMapper invitationActivityMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private InvitationActivityMemberCardService invitationActivityMemberCardService;

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    @Override
    public List<InvitationActivity> selectBySearch(InvitationActivityQuery query) {
        return this.invitationActivityMapper.selectBySearch(query);
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromDB(Long id) {
        return this.invitationActivityMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromCache(Long id) {

        InvitationActivity cacheInvitationActivity = redisService.getWithHash(CacheConstant.CACHE_INVITATION_ACTIVITY + id, InvitationActivity.class);
        if (Objects.nonNull(cacheInvitationActivity)) {
            return cacheInvitationActivity;
        }

        InvitationActivity invitationActivity = this.queryByIdFromDB(id);
        if (Objects.isNull(invitationActivity)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_INVITATION_ACTIVITY + id, invitationActivity);

        return invitationActivity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(InvitationActivityQuery query) {
//        Integer usableActivityCount = invitationActivityMapper.checkUsableActivity(TenantContextHolder.getTenantId());
//        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_UP) && Objects.nonNull(usableActivityCount)) {
//            return Triple.of(false, "", "已存在上架的活动");
//        }

        //检查是否有选择（换电,租车,车电一体）套餐信息
        if(CollectionUtils.isEmpty(query.getBatteryPackages())
                && CollectionUtils.isEmpty(query.getCarRentalPackages())
                && CollectionUtils.isEmpty(query.getCarWithBatteryPackages())){
            return Triple.of(false, "110201", "请选择套餐信息");
        }

        Triple<Boolean, String, Object> verifyResult = verifySelectedPackages(query);
        if(Boolean.FALSE.equals(verifyResult.getLeft())){
            return verifyResult;
        }

        InvitationActivity invitationActivity = new InvitationActivity();
        BeanUtils.copyProperties(query, invitationActivity);
        invitationActivity.setDiscountType(InvitationActivity.DISCOUNT_TYPE_FIXED_AMOUNT);
        invitationActivity.setDelFlag(InvitationActivity.DEL_NORMAL);
        invitationActivity.setOperateUid(SecurityUtils.getUid());
        invitationActivity.setType(InvitationActivity.TYPE_DEFAULT);
        invitationActivity.setTenantId(TenantContextHolder.getTenantId());
        invitationActivity.setCreateTime(System.currentTimeMillis());
        invitationActivity.setUpdateTime(System.currentTimeMillis());
        Integer insert = this.insert(invitationActivity);

        if (insert > 0) {
            //List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(invitationActivity.getId(), query.getMembercardIds());

            //创建套餐信息并保存至数据库
            List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityPackages(invitationActivity.getId(), query);
            log.info("Add the invitation activity, selected packages = {}", JsonUtil.toJson(shareActivityMemberCards));

            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                invitationActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
        }

        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> verifySelectedPackages(InvitationActivityQuery invitationActivityQuery){
        List<Long> electricityPackages = invitationActivityQuery.getBatteryPackages();
        for(Long packageId : electricityPackages){
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "110202", "换电套餐不存在");
            }
        }

        List<Long> carRentalPackages = invitationActivityQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "110203", "租车套餐不存在");
            }
        }

        List<Long> carElectricityPackages = invitationActivityQuery.getCarWithBatteryPackages();
        for(Long packageId : carElectricityPackages){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "110204", "车电一体套餐不存在");
            }
        }
        return Triple.of(true, "", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(InvitationActivityQuery query) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100390", "活动不存在");
        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();
        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setName(query.getName());
        invitationActivityUpdate.setHours(query.getHours());
        invitationActivityUpdate.setDescription(query.getDescription());
        invitationActivityUpdate.setFirstReward(query.getFirstReward());
        invitationActivityUpdate.setOtherReward(query.getOtherReward());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(invitationActivityUpdate);

        if (update > 0) {
            //删除绑定的套餐
            invitationActivityMemberCardService.deleteByActivityId(query.getId());

            //List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(query.getId(), query.getMembercardIds());
            //获取已选择的套餐信息
            List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityPackages(invitationActivity.getId(), query);

            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                invitationActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
        }

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateStatus(InvitationActivityStatusQuery query) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100390", "活动不存在");
        }

//        Integer usableActivityCount = invitationActivityMapper.checkUsableActivity(TenantContextHolder.getTenantId());
//        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_UP) && Objects.nonNull(usableActivityCount)) {
//            return Triple.of(false, "", "已存在上架的活动");
//        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();

        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setStatus(query.getStatus());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(invitationActivityUpdate);

        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_DOWN) && update > 0) {
            invitationActivityJoinHistoryService.updateStatusByActivityId(query.getId(), InvitationActivityJoinHistory.STATUS_OFF);
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Integer checkUsableActivity(Integer tenantId) {
        return invitationActivityMapper.checkUsableActivity(tenantId);
    }

    @Override
    public List<InvitationActivityVO> selectByPage(InvitationActivityQuery query) {

        List<InvitationActivity> invitationActivities = invitationActivityMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Collections.emptyList();
        }

        return invitationActivities.parallelStream().map(item -> {
            InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
            BeanUtils.copyProperties(item, invitationActivityVO);

            //List<Long> membercardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(item.getId());
            /*if (!CollectionUtils.isEmpty(membercardIds)) {
                List<ElectricityMemberCard> memberCardList = Lists.newArrayList();
                for (Long membercardId : membercardIds) {
                    ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercardId.intValue());
                    if (Objects.nonNull(electricityMemberCard)) {
                        memberCardList.add(electricityMemberCard);
                    }
                }

//                invitationActivityVO.setMemberCardList(memberCardList);
            }*/

            invitationActivityVO.setBatteryPackages(getBatteryPackages(item.getId()));
            invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(item.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
            invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(item.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));

            return invitationActivityVO;
        }).collect(Collectors.toList());

    }

    @Override
    public Integer selectByPageCount(InvitationActivityQuery query) {
        return invitationActivityMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(InvitationActivity invitationActivity) {
        return this.invitationActivityMapper.insertOne(invitationActivity);
    }

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivity invitationActivity) {
        int update = this.invitationActivityMapper.update(invitationActivity);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_INVITATION_ACTIVITY + invitationActivity.getId());
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
        int delete = this.invitationActivityMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_INVITATION_ACTIVITY + id);
        });
        return delete;
    }

    @Override
    public List<InvitationActivity> selectUsableActivity(Integer tenantId) {
        return invitationActivityMapper.selectUsableActivity(tenantId);
    }

    @Override
    public Triple<Boolean, String, Object> activityInfo() {
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            return Triple.of(true, null, null);
        }
        
        List<InvitationActivityVO> invitationActivityVOList = invitationActivityUserList.stream().map(invitationActivityUser -> {
            InvitationActivity invitationActivity = this.queryByIdFromCache(invitationActivityUser.getActivityId());
            InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
            if (Objects.nonNull(invitationActivity)) {
                BeanUtils.copyProperties(invitationActivity, invitationActivityVO);
                
                invitationActivityVO.setBatteryPackages(getBatteryPackages(invitationActivity.getId()));
                invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }
            return invitationActivityVO;
        }).collect(Collectors.toList());
        
        return Triple.of(true, null, invitationActivityVOList);
    }

    @Override
    public Triple<Boolean, String, Object> findActivityById(Long id) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(id);
        InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
        BeanUtils.copyProperties(invitationActivity, invitationActivityVO);

        invitationActivityVO.setBatteryPackages(getBatteryPackages(id));

        invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
        invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));

        return  Triple.of(true, null, invitationActivityVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> selectActivityByUser(InvitationActivityQuery query, Long uid) {
    
        // 获取已上架的所有活动
        List<InvitationActivity> invitationActivities = selectBySearch(query);
    
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Triple.of(false, "ELECTRICITY.0069", "未找到活动");
        }
        log.info("invitationActivities1 size = {}", invitationActivities.size());
    
        // 对相同套餐的活动做唯一处理
        Map<Long, List<Long>> activityIdMemCardIdsMap = new HashMap<>();
        List<InvitationActivity> removeList = new ArrayList<>();
        for (int i = 0; i < invitationActivities.size(); i++) {
            InvitationActivity activity1 = invitationActivities.get(i);
            List<Long> memCardIds1 = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activity1.getId());
            activityIdMemCardIdsMap.put(activity1.getId(), memCardIds1);
        
            for (int j = 0; j < i; j++) {
                InvitationActivity activity2 = invitationActivities.get(j);
                List<Long> memCardIds2 = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activity2.getId());
                if (memCardIds1.stream().anyMatch(memCardIds2::contains)) {
                    removeList.add(activity2);
                }
            }
        
        }
    
        invitationActivities.removeAll(removeList);
    
        log.info("invitationActivities2 size = {}", invitationActivities.size());
    
        // 获取邀请人已绑定的活动
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(uid);
        if (CollectionUtils.isNotEmpty(invitationActivityUserList)) {
            //根据已绑定活动的套餐对待选活动做唯一处理
            Set<Long> boundActivityIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toSet());
            List<InvitationActivity> removeList2 = new ArrayList<>();
            for (InvitationActivity activity : invitationActivities) {
                List<Long> memCardIdsList1 = activityIdMemCardIdsMap.get(activity.getId());
            
                for (Long activityId : boundActivityIds) {
                    List<Long> memCardIdsList2 = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activityId);
                    if (memCardIdsList1.stream().anyMatch(memCardIdsList2::contains)) {
                        removeList2.add(activity);
                    }
                }
            }
            invitationActivities.removeAll(removeList2);
        }
    
        log.info("invitationActivities3 size = {}", invitationActivities.size());
    
        List<InvitationActivityMemberCardVO> collect = invitationActivities.stream().map(item -> {
            Long activityId = item.getId();
            String activityName = this.queryByIdFromCache(activityId).getName();
            List<Long> memCardIdList = activityIdMemCardIdsMap.get(activityId);
        
            InvitationActivityMemberCardVO invitationActivityMemberCardVO = new InvitationActivityMemberCardVO();
            invitationActivityMemberCardVO.setId(activityId);
            invitationActivityMemberCardVO.setName(activityName);
            invitationActivityMemberCardVO.setMemberCardIdList(memCardIdList);
        
            return invitationActivityMemberCardVO;
        
        }).collect(Collectors.toList());
    
        return Triple.of(true, null, collect);
    }
    
    private List<BatteryMemberCardVO> getBatteryPackages(Long activityId) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<InvitationActivityMemberCard> invitationActivityMemberCards = invitationActivityMemberCardService.selectPackagesByActivityIdAndType(activityId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());

        for(InvitationActivityMemberCard invitationActivityMemberCard : invitationActivityMemberCards){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(invitationActivityMemberCard.getMid());
            if(Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())){
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }

        return memberCardVOList;
    }

    private List<BatteryMemberCardVO> getCarBatteryPackages(Long activityId, Integer packageType){
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<InvitationActivityMemberCard> invitationActivityMemberCards = invitationActivityMemberCardService.selectPackagesByActivityIdAndType(activityId, packageType);
        for(InvitationActivityMemberCard invitationActivityMemberCard : invitationActivityMemberCards){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(invitationActivityMemberCard.getMid());
            if(Objects.nonNull(carRentalPackagePO) && CommonConstant.DEL_N.equals(carRentalPackagePO.getDelFlag())){
                batteryMemberCardVO.setId(carRentalPackagePO.getId());
                batteryMemberCardVO.setName(carRentalPackagePO.getName());
                batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
                memberCardVOList.add(batteryMemberCardVO);
            }
        }

        return memberCardVOList;
    }


    private List<InvitationActivityMemberCard> buildShareActivityMemberCard(Long id, List<Long> membercardIds) {
        List<InvitationActivityMemberCard> list = Lists.newArrayList();

        for (Long membercardId : membercardIds) {
            InvitationActivityMemberCard invitationActivityMemberCard = new InvitationActivityMemberCard();
            invitationActivityMemberCard.setActivityId(id);
            invitationActivityMemberCard.setMid(membercardId);
            invitationActivityMemberCard.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityMemberCard.setCreateTime(System.currentTimeMillis());
            invitationActivityMemberCard.setUpdateTime(System.currentTimeMillis());
            list.add(invitationActivityMemberCard);
        }

        return list;
    }

    private List<InvitationActivityMemberCard> buildShareActivityPackages(Long activityId, InvitationActivityQuery invitationActivityQuery) {
        List<InvitationActivityMemberCard> invitationActivityMemberCards = Lists.newArrayList();
        List<Long> batteryPackages = invitationActivityQuery.getBatteryPackages();
        for(Long packageId : batteryPackages){
            InvitationActivityMemberCard batteryPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            invitationActivityMemberCards.add(batteryPackage);
        }

        List<Long> carRentalPackages = invitationActivityQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            InvitationActivityMemberCard carRentalPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
            invitationActivityMemberCards.add(carRentalPackage);
        }

        List<Long> carWithBatteryPackages = invitationActivityQuery.getCarWithBatteryPackages();
        for(Long packageId : carWithBatteryPackages){
            InvitationActivityMemberCard carWithBatteryPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
            invitationActivityMemberCards.add(carWithBatteryPackage);
        }

        return invitationActivityMemberCards;
    }

    private InvitationActivityMemberCard buildShareActivityMemberCard(Long activityId, Long packageId, Integer packageType){
        InvitationActivityMemberCard invitationActivityMemberCard = new InvitationActivityMemberCard();
        invitationActivityMemberCard.setActivityId(activityId);
        invitationActivityMemberCard.setMid(packageId);
        invitationActivityMemberCard.setPackageType(packageType);
        invitationActivityMemberCard.setTenantId(TenantContextHolder.getTenantId());
        invitationActivityMemberCard.setCreateTime(System.currentTimeMillis());
        invitationActivityMemberCard.setUpdateTime(System.currentTimeMillis());

        return invitationActivityMemberCard;
    }

}
