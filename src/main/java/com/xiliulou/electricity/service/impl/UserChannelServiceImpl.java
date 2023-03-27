package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserChannel;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserChannelMapper;
import com.xiliulou.electricity.query.UserChannelQuery;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserChannelService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserChannelVo;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserChannel)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-22 15:34:57
 */
@Service("userChannelService")
@Slf4j
public class UserChannelServiceImpl implements UserChannelService {
    
    @Resource
    private UserChannelMapper userChannelMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ChannelActivityHistoryService channelActivityHistoryService;
    
    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserChannel queryByUidFromDB(Long id) {
        return this.userChannelMapper.queryByUidFromDB(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     */
    @Override
    public UserChannel queryByUidFromCache(Long uid) {
        UserChannel userChannel = redisService.getWithHash(CacheConstant.CACHE_USER_CHANNEL + uid, UserChannel.class);
        if (Objects.nonNull(userChannel)) {
            return userChannel;
        }
    
        UserChannel userChannelFromDb = queryByUidFromDB(uid);
        if (Objects.isNull(userChannelFromDb)) {
            return null;
        }
    
        redisService.saveWithHash(CacheConstant.CACHE_USER_CHANNEL + uid, userChannelFromDb);
        redisService.expire(CacheConstant.CACHE_USER_CHANNEL, CacheConstant.CACHE_EXPIRE_MONTH, false);
    
        return userChannelFromDb;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<UserChannel> queryAllByLimit(int offset, int limit) {
        return this.userChannelMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserChannel insert(UserChannel userChannel) {
        this.userChannelMapper.insertOne(userChannel);
        return userChannel;
    }
    
    /**
     * 修改数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserChannel userChannel) {
        return this.userChannelMapper.update(userChannel);
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userChannelMapper.deleteById(id) > 0;
    }
    
    @Override
    @Slave
    public Triple<Boolean, String, Object> queryList(Long offset, Long size, String name, String phone) {
        List<UserChannel> queryList = this.userChannelMapper.queryList(offset, size, name, phone);
        List<UserChannelVo> voList = new ArrayList<>();
    
        Optional.ofNullable(queryList).orElse(new ArrayList<>()).forEach(item -> {
            UserChannelVo vo = new UserChannelVo();
            BeanUtils.copyProperties(item, vo);
    
            UserInfo userInfo = userInfoService.queryByUidFromDb(item.getUid());
            if (Objects.nonNull(userInfo)) {
                vo.setName(userInfo.getName());
                vo.setPhone(userInfo.getPhone());
            }
    
            User user = userService.queryByUidFromCache(item.getOperateUid());
            if (Objects.nonNull(user)) {
                vo.setOperateName(user.getName());
            }
    
            voList.add(vo);
        });
    
        return Triple.of(true, null, voList);
    }
    
    @Override
    @Slave
    public Triple<Boolean, String, Object> queryCount(String name, String phone) {
        Long count = this.userChannelMapper.queryCount(name, phone);
        return Triple.of(true, null, count);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> saveOne(Long uid) {
        Long currUid = SecurityUtils.getUid();
        if (Objects.isNull(currUid)) {
            log.error("USER CHANNEL ERROR! not found user");
            return Triple.of(false, "100001", "用户不存在");
        }
    
        User user = userService.queryByUidFromCache(currUid);
        if (Objects.isNull(user)) {
            log.error("USER CHANNEL ERROR! not found user, uid={}", currUid);
            return Triple.of(false, "100001", "用户不存在");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("USER CHANNEL ERROR! not found user, uid", uid);
            return Triple.of(false, "100001", "用户不存在");
        }
        
        //购买过套餐
        //        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
        //                .selectByUidFromCache(userInfo.getUid());
        //        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
        //                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
        //            log.error("USER CHANNEL ERROR! user haven't memberCard uid={}", user.getUid());
        //            return Triple.of(false, "100210", "用户未开通套餐");
        //        }
    
        if (userBuyMemberCardCheck(userInfo.getUid())) {
            log.error("USER CHANNEL ERROR! user haven't memberCard uid={}", user.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        //不是渠道人，
        UserChannel userChannel = queryByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userChannel)) {
            log.error("USER CHANNEL ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100453", "该用户已是渠道用户，请勿重复添加");
        }
    
        // 没邀请记录，
        Long inviteCount = channelActivityHistoryService.queryInviteCount(userInfo.getUid());
        if (Objects.nonNull(inviteCount) && !Objects.equals(inviteCount, 0L)) {
            log.error("USER CHANNEL ERROR! user exist invite users！uid={}", userInfo.getUid());
            return Triple.of(false, "100454", "用户邀请过他人，不可添加为渠道用户");
        }
    
        //不是受别人邀请
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory)) {
            log.error("USER CHANNEL ERROR! user is invited by others！uid={}", userInfo.getUid());
            return Triple.of(false, "100455", "用户存在邀请人，不可添加为渠道用户");
        }
    
        UserChannel updateUserChannel = new UserChannel();
        updateUserChannel.setOperateUid(currUid);
        updateUserChannel.setUid(userInfo.getUid());
        updateUserChannel.setTenantId(tenantId);
        updateUserChannel.setCreateTime(System.currentTimeMillis());
        updateUserChannel.setUpdateTime(System.currentTimeMillis());
        insert(updateUserChannel);
        return Triple.of(true, "", "");
    }
    
    private boolean userBuyMemberCardCheck(Long uid) {
        boolean batteryMemberCard = true;
        boolean carMemberCard = true;
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects
                .equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            batteryMemberCard = false;
        }
        
        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService
                .queryLastPayMemberCardTimeByUid(uid, null, TenantContextHolder.getTenantId());
        if (Objects.isNull(carMemberCardOrder)) {
            carMemberCard = false;
        }
        
        return batteryMemberCard || carMemberCard;
    }
}
