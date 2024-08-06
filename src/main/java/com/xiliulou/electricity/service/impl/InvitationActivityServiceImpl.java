package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.entity.InvitationActivityMemberCard;
import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.InvitationActivityMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.InvitationActivityMemberCardVO;
import com.xiliulou.electricity.vo.InvitationActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserInfoService userInfoService;
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("shareActivityHandlerExecutor", 1, "SHARE_ACTIVITY_HANDLER_EXECUTOR");
    
    @Override
    @Slave
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
        // 加盟商一致性校验
        Long franchiseeId = query.getFranchiseeId();
        List<Long> franchiseeIds = query.getFranchiseeIds();
        if (CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("Insert invitationActivity WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        if (ObjectUtil.isEmpty(query.getHours()) && ObjectUtil.isEmpty(query.getMinutes())) {
            return Triple.of(false, "110209", "有效时间不能为空");
        }
        
        //检查是否有选择（换电,租车,车电一体）套餐信息
        if (CollectionUtils.isEmpty(query.getBatteryPackages()) && CollectionUtils.isEmpty(query.getCarRentalPackages()) && CollectionUtils.isEmpty(
                query.getCarWithBatteryPackages())) {
            return Triple.of(false, "110201", "请选择套餐信息");
        }
        
        Triple<Boolean, String, Object> verifyResult = verifySelectedPackages(query);
        if (Boolean.FALSE.equals(verifyResult.getLeft())) {
            return verifyResult;
        }
        
        InvitationActivity invitationActivity = new InvitationActivity();
        BeanUtils.copyProperties(query, invitationActivity);
        invitationActivity.setDiscountType(InvitationActivity.DISCOUNT_TYPE_FIXED_AMOUNT);
        invitationActivity.setDelFlag(InvitationActivity.DEL_NORMAL);
        invitationActivity.setOperateUid(SecurityUtils.getUid());
        invitationActivity.setType(InvitationActivity.TYPE_FRANCHINSEE);
        invitationActivity.setTenantId(TenantContextHolder.getTenantId());
        invitationActivity.setCreateTime(System.currentTimeMillis());
        invitationActivity.setUpdateTime(System.currentTimeMillis());
        invitationActivity.setHours(Objects.isNull(query.getHours()) ? NumberConstant.ZERO : (query.getHours()));
        invitationActivity.setMinutes(Objects.isNull(query.getMinutes()) ? NumberConstant.ZERO : (query.getMinutes()));
        
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
    
    private Triple<Boolean, String, Object> verifySelectedPackages(InvitationActivityQuery invitationActivityQuery) {
        List<Long> electricityPackages = invitationActivityQuery.getBatteryPackages();
        for (Long packageId : electricityPackages) {
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "110202", "换电套餐不存在");
            }
        }
        
        List<Long> carRentalPackages = invitationActivityQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "110203", "租车套餐不存在");
            }
        }
        
        List<Long> carElectricityPackages = invitationActivityQuery.getCarWithBatteryPackages();
        for (Long packageId : carElectricityPackages) {
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
        
        // 加盟商一致性校验
        Long franchiseeId = invitationActivity.getFranchiseeId();
        List<Long> franchiseeIds = query.getFranchiseeIds();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("Update invitationActivity WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        InvitationActivity invitationActivityUpdate = new InvitationActivity();
        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setName(query.getName());
        invitationActivityUpdate.setDescription(query.getDescription());
        invitationActivityUpdate.setFirstReward(query.getFirstReward());
        invitationActivityUpdate.setOtherReward(query.getOtherReward());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 设置有效期 兼容 小时和分钟：小时修改为小时或分钟修改为小时
        if (Objects.nonNull(query.getHours()) && !Objects.equals(query.getHours(), NumberConstant.ZERO)) {
            invitationActivityUpdate.setHours(query.getHours());
            invitationActivityUpdate.setMinutes(NumberConstant.ZERO);
        }
        // 设置有效期 兼容 小时和分钟：分钟修改为分钟或小时修改为分钟
        if (Objects.nonNull(query.getMinutes()) && !Objects.equals(query.getMinutes(), NumberConstant.ZERO)) {
            invitationActivityUpdate.setMinutes(query.getMinutes());
            invitationActivityUpdate.setHours(NumberConstant.ZERO);
        }
        
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
        
        // 加盟商一致性校验
        Long franchiseeId = invitationActivity.getFranchiseeId();
        List<Long> franchiseeIds = query.getFranchiseeIds();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("Update invitationActivity WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
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
    @Slave
    public List<InvitationActivity> queryOnlineActivity(Integer tenantId, Long franchiseeId) {
        List<InvitationActivity> activityList = invitationActivityMapper.selectUsableActivity(tenantId);
        if (CollectionUtils.isEmpty(activityList)) {
            return Collections.emptyList();
        }
        
        List<InvitationActivity> list;
        // 如果有加盟商，则查加盟商的活动
        list = activityList.stream().filter(activity -> Objects.nonNull(activity.getFranchiseeId()) && !Objects.equals(franchiseeId, NumberConstant.ZERO_L) && Objects.equals(
                activity.getFranchiseeId(), franchiseeId)).collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(list)) {
            // 如果没有加盟商，则查租户的活动
            list = activityList.stream().filter(activity -> Objects.isNull(activity.getFranchiseeId()) || Objects.equals(franchiseeId, NumberConstant.ZERO_L))
                    .collect(Collectors.toList());
        }
        
        return list;
    }
    
    @Override
    @Slave
    public List<InvitationActivityVO> selectByPage(InvitationActivityQuery query) {
        
        List<InvitationActivity> invitationActivities = invitationActivityMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Collections.emptyList();
        }
        
        return invitationActivities.parallelStream().map(item -> {
            InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
            BeanUtils.copyProperties(item, invitationActivityVO);
            
            // 如果单位小时，timeType=1  如果单位分钟，timeType=2
            if (Objects.nonNull(item.getHours()) && !Objects.equals(item.getHours(), NumberConstant.ZERO)) {
                invitationActivityVO.setHours(item.getHours().doubleValue());
                invitationActivityVO.setTimeType(NumberConstant.ONE);
            } else {
                invitationActivityVO.setMinutes(Objects.isNull(item.getMinutes()) ? NumberConstant.ZERO_L : item.getMinutes().longValue());
                invitationActivityVO.setTimeType(NumberConstant.TWO);
            }
            
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
            
            Long franchiseeId = item.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                invitationActivityVO.setFranchiseeId(franchiseeId);
                invitationActivityVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            
            return invitationActivityVO;
        }).collect(Collectors.toList());
        
    }
    
    @Override
    @Slave
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
    @Slave
    public List<InvitationActivity> selectUsableActivity(Integer tenantId) {
        return invitationActivityMapper.selectUsableActivity(tenantId);
    }
    
    @Override
    public Triple<Boolean, String, Object> activityInfo() {
        //        InvitationActivityUser invitationActivityUser = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        //        if(Objects.isNull(invitationActivityUser)){
        //            return Triple.of(true, null, null);
        //        }
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            return Triple.of(true, null, null);
        }
        // 因为线上小程序版本问题，回退改接口，此处临时解决处理：返回第一个
        InvitationActivityUser invitationActivityUser = invitationActivityUserList.get(0);
        
        InvitationActivity invitationActivity = this.queryByIdFromCache(invitationActivityUser.getActivityId());
        if (Objects.isNull(invitationActivity)) {
            return Triple.of(true, null, null);
        }
        
        InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
        BeanUtils.copyProperties(invitationActivity, invitationActivityVO);

        /*List<Long> membercardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(invitationActivity.getId());
        if (!CollectionUtils.isEmpty(membercardIds)) {
            List<ElectricityMemberCard> memberCardList = Lists.newArrayList();
            for (Long membercardId : membercardIds) {
                ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercardId.intValue());
                if (Objects.nonNull(electricityMemberCard)) {
                    memberCardList.add(electricityMemberCard);
                }
            }

//            invitationActivityVO.setMemberCardList(memberCardList);
        }*/
        
        invitationActivityVO.setBatteryPackages(getBatteryPackages(invitationActivity.getId()));
        invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
        invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
        
        // 兼容旧版小程序
        if (Objects.nonNull(invitationActivity.getHours()) && !Objects.equals(invitationActivity.getHours(), NumberConstant.ZERO)) {
            invitationActivityVO.setHours(invitationActivity.getHours().doubleValue());
            invitationActivityVO.setMinutes(invitationActivity.getHours() * TimeConstant.HOURS_MINUTE);
            invitationActivityVO.setTimeType(NumberConstant.ONE);
        }
        if (Objects.nonNull(invitationActivity.getMinutes()) && !Objects.equals(invitationActivity.getMinutes(), NumberConstant.ZERO)) {
            invitationActivityVO.setMinutes(invitationActivity.getMinutes().longValue());
            invitationActivityVO.setHours(
                    Math.round((double) invitationActivity.getMinutes().longValue() / TimeConstant.HOURS_MINUTE * NumberConstant.ONE_HUNDRED_D) / NumberConstant.ONE_HUNDRED_D);
            invitationActivityVO.setTimeType(NumberConstant.TWO);
        }
        
        return Triple.of(true, null, invitationActivityVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> activityInfoV2() {
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("INVITATION ACTIVITY WARN! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        List<InvitationActivity> invitationActivities = selectUsableActivity(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Triple.of(true, null, null);
        }
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(uid);
        if (CollectionUtils.isEmpty(invitationActivityUserList)) {
            return Triple.of(true, null, null);
        }
        
        //过滤掉未上架的
        Set<Long> activityIdSet = invitationActivities.stream().map(InvitationActivity::getId).collect(Collectors.toSet());
        List<InvitationActivityUser> newInvitationActivityUserList = invitationActivityUserList.stream()
                .filter(activityUser -> activityIdSet.contains(activityUser.getActivityId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newInvitationActivityUserList)) {
            return Triple.of(true, null, null);
        }
        
        List<InvitationActivityVO> invitationActivityVOList = newInvitationActivityUserList.stream().map(invitationActivityUser -> {
            InvitationActivity invitationActivity = this.queryByIdFromCache(invitationActivityUser.getActivityId());
            InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
            if (Objects.nonNull(invitationActivity)) {
                BeanUtils.copyProperties(invitationActivity, invitationActivityVO);
                
                invitationActivityVO.setBatteryPackages(getBatteryPackages(invitationActivity.getId()));
                invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(invitationActivity.getId(), PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
                
                // 兼容旧版小程序
                if (Objects.nonNull(invitationActivity.getHours()) && !Objects.equals(invitationActivity.getHours(), NumberConstant.ZERO)) {
                    invitationActivityVO.setHours(invitationActivity.getHours().doubleValue());
                    invitationActivityVO.setMinutes(invitationActivity.getHours() * TimeConstant.HOURS_MINUTE);
                    invitationActivityVO.setTimeType(NumberConstant.ONE);
                }
                if (Objects.nonNull(invitationActivity.getMinutes()) && !Objects.equals(invitationActivity.getMinutes(), NumberConstant.ZERO)) {
                    invitationActivityVO.setMinutes(invitationActivity.getMinutes().longValue());
                    invitationActivityVO.setHours(Math.round((double) invitationActivity.getMinutes().longValue() / TimeConstant.HOURS_MINUTE * NumberConstant.ONE_HUNDRED_D)
                            / NumberConstant.ONE_HUNDRED_D);
                    invitationActivityVO.setTimeType(NumberConstant.TWO);
                }
                
                Long franchiseeId = invitationActivity.getFranchiseeId();
                if (Objects.nonNull(franchiseeId)) {
                    invitationActivityVO.setFranchiseeName(
                            Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
                }
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
        
        // 如果单位小时，timeType=1  如果单位分钟，timeType=2
        if (Objects.nonNull(invitationActivity.getHours()) && !Objects.equals(invitationActivity.getHours(), NumberConstant.ZERO)) {
            invitationActivityVO.setHours(invitationActivity.getHours().doubleValue());
            invitationActivityVO.setTimeType(NumberConstant.ONE);
        } else {
            invitationActivityVO.setMinutes(Objects.isNull(invitationActivity.getMinutes()) ? NumberConstant.ZERO_L : invitationActivity.getMinutes().longValue());
            invitationActivityVO.setTimeType(NumberConstant.TWO);
        }
        
        invitationActivityVO.setBatteryPackages(getBatteryPackages(id));
        
        invitationActivityVO.setCarRentalPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
        invitationActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
        
        Long franchiseeId = invitationActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            invitationActivityVO.setFranchiseeId(franchiseeId);
            invitationActivityVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return Triple.of(true, null, invitationActivityVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> selectActivityByUser(InvitationActivityQuery query, Long uid) {
        
        // 获取已上架的所有活动
        List<InvitationActivity> invitationActivities = selectBySearch(query);
        
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Triple.of(false, "100397", "暂无活动");
        }
        
        // 获取邀请人已绑定的活动
        Map<Long, List<Long>> activityIdMemCardIdsMap = new HashMap<>(invitationActivities.size());
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(uid);
        if (CollectionUtils.isNotEmpty(invitationActivityUserList)) {
            //根据已绑定活动的套餐对待选活动做唯一处理
            Set<InvitationActivity> removeSet = new HashSet<>();
            Set<Long> boundActivityIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toSet());
            for (InvitationActivity activity : invitationActivities) {
                List<Long> memCardIds1 = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activity.getId());
                activityIdMemCardIdsMap.put(activity.getId(), memCardIds1);
                
                for (Long activityId : boundActivityIds) {
                    List<Long> memCardIds2 = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activityId);
                    if (memCardIds1.stream().anyMatch(memCardIds2::contains)) {
                        removeSet.add(activity);
                    }
                }
            }
            
            invitationActivities.removeAll(removeSet);
        }
        
        List<InvitationActivityMemberCardVO> collect = invitationActivities.stream().map(item -> {
            Long activityId = item.getId();
            String activityName = this.queryByIdFromCache(activityId).getName();
            List<Long> memCardIdList;
            if (activityIdMemCardIdsMap.containsKey(activityId)) {
                memCardIdList = activityIdMemCardIdsMap.get(activityId);
            } else {
                memCardIdList = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activityId);
            }
            
            InvitationActivityMemberCardVO invitationActivityMemberCardVO = new InvitationActivityMemberCardVO();
            invitationActivityMemberCardVO.setId(activityId);
            invitationActivityMemberCardVO.setName(activityName);
            invitationActivityMemberCardVO.setMemberCardIdList(memCardIdList);
            
            return invitationActivityMemberCardVO;
            
        }).collect(Collectors.toList());
        
        return Triple.of(true, null, collect);
    }
    
    /**
     * <p>
     * Description: delete 9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     *
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @Override
    public R<?> removeById(Long id, List<Long> franchiseeIds) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(id);
        if (Objects.isNull(invitationActivity)) {
            log.warn("Delete Activity WARN! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }
        
        // 租户一致性校验
        if (!Objects.equals(TenantContextHolder.getTenantId(), invitationActivity.getTenantId())) {
            return R.ok();
        }
        
        // 加盟商一致性校验
        Long franchiseeId = invitationActivity.getFranchiseeId();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("Remove invitationActivity WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        int count = this.invitationActivityMapper.removeById(id, TenantContextHolder.getTenantId().longValue());
        if (Objects.equals(invitationActivity.getStatus(), ShareActivity.STATUS_OFF)) {
            return R.ok(count);
        }
        
        DbUtils.dbOperateSuccessThenHandleCache(Math.toIntExact(id), (identifier) -> {
            redisService.delete(CacheConstant.CACHE_INVITATION_ACTIVITY + identifier);
        });
        
        executorService.submit(() -> {
            //修改邀请状态
            invitationActivityJoinHistoryService.updateStatusByActivityId(id, InvitationActivityJoinHistory.STATUS_OFF);
        });
        
        return R.ok(count);
    }
    
    private List<BatteryMemberCardVO> getBatteryPackages(Long activityId) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<InvitationActivityMemberCard> invitationActivityMemberCards = invitationActivityMemberCardService.selectPackagesByActivityIdAndType(activityId,
                PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        
        for (InvitationActivityMemberCard invitationActivityMemberCard : invitationActivityMemberCards) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(invitationActivityMemberCard.getMid());
            if (Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())) {
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }
        
        return memberCardVOList;
    }
    
    private List<BatteryMemberCardVO> getCarBatteryPackages(Long activityId, Integer packageType) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<InvitationActivityMemberCard> invitationActivityMemberCards = invitationActivityMemberCardService.selectPackagesByActivityIdAndType(activityId, packageType);
        for (InvitationActivityMemberCard invitationActivityMemberCard : invitationActivityMemberCards) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(invitationActivityMemberCard.getMid());
            if (Objects.nonNull(carRentalPackagePO) && CommonConstant.DEL_N.equals(carRentalPackagePO.getDelFlag())) {
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
        for (Long packageId : batteryPackages) {
            InvitationActivityMemberCard batteryPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            invitationActivityMemberCards.add(batteryPackage);
        }
        
        List<Long> carRentalPackages = invitationActivityQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            InvitationActivityMemberCard carRentalPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
            invitationActivityMemberCards.add(carRentalPackage);
        }
        
        List<Long> carWithBatteryPackages = invitationActivityQuery.getCarWithBatteryPackages();
        for (Long packageId : carWithBatteryPackages) {
            InvitationActivityMemberCard carWithBatteryPackage = buildShareActivityMemberCard(activityId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode());
            invitationActivityMemberCards.add(carWithBatteryPackage);
        }
        
        return invitationActivityMemberCards;
    }
    
    private InvitationActivityMemberCard buildShareActivityMemberCard(Long activityId, Long packageId, Integer packageType) {
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
