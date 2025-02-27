package com.xiliulou.electricity.service.impl.lostuser;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.lostuser.LostUserFirstEnum;
import com.xiliulou.electricity.enums.lostuser.PackageTypeEnum;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.lostuser.LostUserBizService;
import com.xiliulou.electricity.service.lostuser.LostUserRecordService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/10/9 18:15
 * @desc
 */

@Service("lostUserBizService")
@Slf4j
public class LostUserBizServiceImpl implements LostUserBizService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Resource
    private LostUserRecordService lostUserRecordService;
    
    @Resource
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Resource
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Resource
    InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;
    
    @Resource
    ChannelActivityHistoryService channelActivityHistoryService;
    
    @Autowired
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Override
    public void checkLostUser() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.info("check lost user info! not find user");
            return;
        }
        
        Integer tenantId = user.getTenantId();
        
        //限频
        if (!redisService.setNx(CacheConstant.CACHE_LOST_USER_CHECK_LOCK + user.getUid(), "1", 3 * 1000L, false)) {
            log.info("check lost user info! operate frequency");
            return;
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig)) {
            log.info("check lost user info! electricity config is not find");
            return;
        }
        
        // 流失用户拉新 是否关闭
        if (Objects.equals(electricityConfig.getLostUserFirst(), LostUserFirstEnum.CLOSE.getCode())) {
            log.info("check lost user info! lost user first is close, tenantId = {}, uid = {}", tenantId, user.getUid());
            return;
        }
        
        if (Objects.isNull(electricityConfig.getLostUserDays())) {
            log.info("check lost user info! lost user days is null, tenantId = {}, uid = {}", tenantId, user.getUid());
            return;
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.info("check lost user info! not find user user info, uid = {}", user.getUid());
            return;
        }
        
        // 用户扩展信息是否存在
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.info("check lost user info! user info extra is , uid = {}", user.getUid());
            return;
        }
        
        // 用户认证信息是否存在
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.info("check lost user info! user not auth pass, uid = {}", user.getUid());
            return;
        }
        
        // 用户是否为流失用户
        if (Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
            log.info("check lost user info! user is already lost user, uid = {}", user.getUid());
            return;
        }
        
        // 检测是否购买过换电套餐
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectLatestByUid(user.getUid());
        
        // 检测是否购买过车对应的订单
        CarRentalPackageOrderPo carRentalPackageOrderPo = carRentalPackageOrderService.selectLastPaySuccessByUid(tenantId, user.getUid());
        
        if (Objects.isNull(carRentalPackageOrderPo) && Objects.isNull(electricityMemberCardOrder)) {
            log.info("check lost user info! user pay success order is empty, uid = {}", user.getUid());
            return;
        }
        
        boolean existNotFinishOrder = false;
        if (Objects.nonNull(electricityMemberCardOrder)) {
            // 检测用户支付成功的换电订单中是否存在未使用，使用中的订单
            existNotFinishOrder = electricityMemberCardOrderService.existNotFinishOrderByUid(user.getUid());
        }
        
        boolean existNotFinishCarOrder = false;
        if (Objects.nonNull(carRentalPackageOrderPo)) {
            // 检测用户支付成功的换电订单中是否存在未使用，使用中的订单
            existNotFinishCarOrder = carRentalPackageOrderService.existNotFinishOrderByUid(user.getUid());
        }
        
        if (existNotFinishOrder) {
            log.info("check lost user info! user exists not finish order, uid = {}", user.getUid());
            return;
        }
        
        if (existNotFinishCarOrder) {
            log.info("check lost user info! user exists not finish car order, uid = {}", user.getUid());
            return;
        }
        
        // 计算用户最近一笔支付订单成功的时间
        Long payTime = null;
        Integer packageType = null;
        String orderId = null;
        
        // 车套餐存在，电不存在
        if (Objects.nonNull(carRentalPackageOrderPo) && Objects.isNull(electricityMemberCardOrder)) {
            payTime = carRentalPackageOrderPo.getCreateTime();
            packageType = PackageTypeEnum.CAR.getCode();
            orderId = carRentalPackageOrderPo.getOrderNo();
        }
        
        if (Objects.nonNull(electricityMemberCardOrder) && Objects.isNull(carRentalPackageOrderPo)) {
            payTime = electricityMemberCardOrder.getCreateTime();
            packageType = PackageTypeEnum.ELECTRICITY.getCode();
            orderId = electricityMemberCardOrder.getOrderId();
        }
        
        if (Objects.nonNull(electricityMemberCardOrder) && Objects.nonNull(carRentalPackageOrderPo)) {
            if (electricityMemberCardOrder.getCreateTime() > carRentalPackageOrderPo.getCreateTime()) {
                payTime = electricityMemberCardOrder.getCreateTime();
                packageType = PackageTypeEnum.ELECTRICITY.getCode();
                orderId = electricityMemberCardOrder.getOrderId();
            } else {
                payTime = carRentalPackageOrderPo.getCreateTime();
                packageType = PackageTypeEnum.CAR.getCode();
                orderId = carRentalPackageOrderPo.getOrderNo();
            }
        }
        
        // 支付时间加一天的凌晨
        long startTimeByTimeStamp = DateUtils.getStartTimeByTimeStamp(payTime + TimeConstant.DAY_MILLISECOND);
        // 计算流失用户的预估时间
        long prospectTime = startTimeByTimeStamp + electricityConfig.getLostUserDays() * TimeConstant.DAY_MILLISECOND;
        // 比较预估和当前时间
        if (System.currentTimeMillis() <= prospectTime) {
            log.info("check lost user info! prospect time less than now, uid = {}", user.getUid());
            return;
        }
        
        // 修改用户的扩展信息
        lostUserRecordService.doLostUser(user, userInfoExtra, prospectTime, packageType, orderId);
        
        // 删除用户扩展信息缓存
        redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + user.getUid());
    }
    
    @Override
    public void updateLostUserStatusAndUnbindActivity(Integer tenantId, Long uid, MerchantInviterVO successInviterVO) {
        
        // 修改流失用户为老用户
        UserInfoExtra userInfoExtraUpdate = UserInfoExtra.builder().lostUserStatus(YesNoEnum.NO.getCode()).updateTime(System.currentTimeMillis()).uid(uid).build();
        userInfoExtraService.updateByUid(userInfoExtraUpdate);
        
        if (Objects.isNull(successInviterVO)) {
            log.info("update lost user status and unbind activity! success inviter is null, uid：{}",uid);
            return;
        }
        
        Integer inviterSource = successInviterVO.getInviterSource();
        
        // 逻辑删除旧的记录
        switch (inviterSource) {
            case 1:
                // 邀请返券
                joinShareActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                break;
            case 2:
                // 邀请返现
                joinShareMoneyActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                break;
            case 3:
                // 套餐返现：多个活动对应的多条参与记录的delFlag都改为1
                invitationActivityJoinHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                break;
            case 4:
                // 渠道邀请
                channelActivityHistoryService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                break;
            case 5:
                // 商户邀请
                merchantJoinRecordService.removeByJoinUid(uid, System.currentTimeMillis(), tenantId);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void updateLostUserNotActivity(Long uid) {
        int update = userInfoExtraService.updateUserNotActivityByUid(uid);
    
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + uid);
        });
    }
}
