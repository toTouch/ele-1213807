package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderHistory;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserExit;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserHistory;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserExitMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserHistoryMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.queryModel.enterprise.EnterpriseChannelUserExitQueryModel;
import com.xiliulou.electricity.request.enterprise.EnterpriseUserAdminExitCheckRequest;
import com.xiliulou.electricity.request.enterprise.EnterpriseUserExitCheckRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.UserInfoSearchVo;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserCheckVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserExitVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserScanCheckVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 14:14
 */

@Slf4j
@Service("enterpriseChannelUserService")
public class EnterpriseChannelUserServiceImpl implements EnterpriseChannelUserService {
    
    @Resource
    TenantService tenantService;
    
    @Resource
    ElectricityBatteryService batteryService;
    
    @Resource
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Resource
    ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private EnterpriseChannelUserMapper enterpriseChannelUserMapper;
    
    @Resource
    private EnterpriseChannelUserExitMapper channelUserExitMapper;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private EleDepositOrderService eleDepositOrderService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private EleRefundOrderService refundOrderService;
    
    @Resource
    private EnterpriseChannelUserHistoryMapper channelUserHistoryMapper;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Resource
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Resource
    private ElectricityCabinetOrderHistoryService electricityCabinetOrderHistoryService;
    
    @Resource
    private UserDelRecordService userDelRecordService;

    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseChannelUserQuery query) {
        log.info("add new user by enterprise start, channel user = {}", JsonUtil.toJson(query));
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY + query.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            //检查当前用户是否可用
            Triple<Boolean, String, Object> result = verifyUserInfo(query);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return Triple.of(false, result.getMiddle(), result.getRight());
            }
            
            EnterpriseInfo enterpriseInfo = (EnterpriseInfo) result.getRight();
            EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
            BeanUtil.copyProperties(query, enterpriseChannelUser);
            enterpriseChannelUser.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setInviterId(SecurityUtils.getUid());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            
            enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);
            
            // 添加用户加盟商信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(query.getUid());
            userInfo.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            userInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfo);
            
            log.info("add new user by enterprise end, enterprise channel user = {}", enterpriseChannelUser.getId());
            
        } catch (Exception e) {
            log.error("add new user by phone error, uid = {}, ex = {}", query.getUid(), e);
            throw new BizException("300067", "添加用户失败");
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY + query.getUid());
        }
        
        return Triple.of(true, "", null);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryUser(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        UserInfo userInfo = userInfoService.queryUserInfoByPhone(enterpriseChannelUserQuery.getPhone(), tenantId);
        
        //返回查询到的用户信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = new EnterpriseChannelUserVO();
        if (Objects.isNull(userInfo)) {
            log.error("query enterprise user by phone failed. Not found user. phone is {}, franchisee id = {} ", enterpriseChannelUserQuery.getPhone(),
                    enterpriseChannelUserQuery.getFranchiseeId());
            return Triple.of(false, "300079", "未查询到该用户，请确保用户已登录小程序并完成实名认证");
        }
        
        enterpriseChannelUserVO.setUid(userInfo.getUid());
        enterpriseChannelUserVO.setName(userInfo.getName());
        enterpriseChannelUserVO.setPhone(userInfo.getPhone());
        enterpriseChannelUserVO.setAuthStatus(userInfo.getAuthStatus());
        
        return Triple.of(true, "", enterpriseChannelUserVO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> generateChannelUser(EnterpriseChannelUserQuery query) {
        if (!ObjectUtils.allNotNull(query.getEnterpriseId())) {
            return Triple.of(false, "ELECTRICITY.0007", "请求数据有误，请检查后操作");
        }
        log.info("generate channel user start, enterprise channel info = {}", query.getEnterpriseId());
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.info("The enterprise info is not exist, enterprise id = {}", query.getEnterpriseId());
            return Triple.of(false, "", "企业信息不存在");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        log.info("query tenant info by tenant id, tenant info  = {}", tenant);
        
        EnterpriseChannelUserVO enterpriseChannelUserVO = new EnterpriseChannelUserVO();
        //先查找库中是否存在已经创建好的基础用户数据，如果存在，则使用已存在的数据，若不存在，则创建新的。
        EnterpriseChannelUser existEnterpriseChannelRecord = enterpriseChannelUserMapper.selectUnusedChannelUser(query.getEnterpriseId(), tenantId.longValue());
        if (Objects.nonNull(existEnterpriseChannelRecord)) {
            BeanUtil.copyProperties(existEnterpriseChannelRecord, enterpriseChannelUserVO);
        } else {
            EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo()));
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);
            log.info("generate channel user end, channel user info = {}", JsonUtil.toJson(enterpriseChannelUser));
            BeanUtil.copyProperties(enterpriseChannelUser, enterpriseChannelUserVO);
        }
        
        if (Objects.nonNull(tenant)) {
            enterpriseChannelUserVO.setTenantCode(tenant.getCode());
        }
        log.info("exist channel user record end, channel user info = {}", JsonUtil.toJson(enterpriseChannelUserVO));
        
        return Triple.of(true, "", enterpriseChannelUserVO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateUserAfterQRScan(EnterpriseChannelUserQuery query) {
        if (!ObjectUtils.allNotNull(query.getUid(), query.getId(), query.getRenewalStatus())) {
            return Triple.of(false, "ELECTRICITY.0007", "请求数据有误，请检查后操作");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_UPDATE_AFTER_SCAN_LOCK_KEY + query.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        try {
            Triple<Boolean, String, Object> result = verifyUserInfo(query);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }
            
            Long uid = query.getUid();
            Long channelUserId = query.getId();
            
            //检查是否已经有用户被关联至当前企业
            EnterpriseChannelUser channelUserEntity = enterpriseChannelUserMapper.queryById(channelUserId);
            if (Objects.isNull(channelUserEntity)) {
                log.error("query enterprise channel record failed after QR scan,  uid = {}, channel user record id", uid, channelUserId);
                return Triple.of(false, "300082", "企业信息不存在, 添加失败");
            }
            
            if (Objects.nonNull(channelUserEntity.getUid())) {
                log.error("user already exist after QR scan,  uid = {}, channel user record id", uid, channelUserId);
                return Triple.of(false, "300083", "已添加其他用户, 请重新扫码");
            }
            
            enterpriseChannelUser.setId(channelUserId);
            enterpriseChannelUser.setUid(uid);
            enterpriseChannelUser.setRenewalStatus(query.getRenewalStatus());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
            
            // 添加用户加盟商信息
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(uid);
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(uid);
            userInfo.setFranchiseeId(channelUser.getFranchiseeId());
            userInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfo);
            
            log.info("Add user success for QR scan, uid = {}, enterprise channel user id = {}", uid, channelUserId);
            
        } catch (Exception e) {
            log.error("add new user after QR scan error, uid = {}, ex = {}", query.getUid(), e);
            throw new BizException("300068", "扫码添加用户失败");
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_UPDATE_AFTER_SCAN_LOCK_KEY + query.getUid());
        }
        return Triple.of(true, "", enterpriseChannelUser);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> checkUserExist(Long id, Long uid) {
       /* if (!ObjectUtils.allNotNull(id, uid)) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }*/
        //uid 默认不传，根据id来查找刚添加成功的用户
        log.info("check user is exist after QR scan start, id = {}, uid = {}", id, uid);
        EnterpriseChannelUserCheckVO enterpriseChannelUserVO = new EnterpriseChannelUserCheckVO();
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectChannelUserByIdAndUid(id, uid);
        if (Objects.isNull(enterpriseChannelUser) || Objects.isNull(enterpriseChannelUser.getUid())) {
            enterpriseChannelUserVO.setIsExist(Boolean.FALSE);
            return Triple.of(true, "300062", enterpriseChannelUserVO);
        }
        enterpriseChannelUserVO.setIsExist(Boolean.TRUE);
        enterpriseChannelUserVO.setUid(enterpriseChannelUser.getUid());
        
        log.info("check user is exist after QR scan end, id = {}, uid = {}, isExist = {}", id, enterpriseChannelUser.getUid(), enterpriseChannelUserVO.getIsExist());
        return Triple.of(true, "", enterpriseChannelUserVO);
    }
    
    @Slave
    @Override
    public EnterpriseChannelUserVO selectUserByEnterpriseIdAndUid(Long enterpriseId, Long uid) {
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectChannelUserByEnterpriseIdAndUid(enterpriseId, uid);
        EnterpriseChannelUserVO enterpriseChannelUserVO = new EnterpriseChannelUserVO();
        if (Objects.nonNull(enterpriseChannelUser)) {
            BeanUtils.copyProperties(enterpriseChannelUser, enterpriseChannelUserVO);
        }
        return enterpriseChannelUserVO;
    }
    
    @Slave
    @Override
    public EnterpriseChannelUser selectByUid(Long uid) {
        return enterpriseChannelUserMapper.selectByUid(uid);
    }
    
    @Slave
    @Override
    public EnterpriseChannelUserVO queryEnterpriseChannelUser(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(uid);
        EnterpriseChannelUserVO enterpriseChannelUserVO = null;
        if (Objects.nonNull(enterpriseChannelUser)) {
            enterpriseChannelUserVO = new EnterpriseChannelUserVO();
            BeanUtil.copyProperties(enterpriseChannelUser, enterpriseChannelUserVO);
        }
        return enterpriseChannelUserVO;
    }
    
    @Slave
    @Override
    public EnterpriseChannelUserVO queryUserRelatedEnterprise(Long uid) {
        
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserMapper.selectUserRelatedEnterprise(uid);
        
        return enterpriseChannelUserVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateRenewStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        enterpriseChannelUserQuery.setUpdateTime(System.currentTimeMillis());
        Integer result = enterpriseChannelUserMapper.updateRenewStatus(enterpriseChannelUserQuery);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchUpdateRenewStatus(List<Long> channelUserIds, Integer renewalStatus) {
        Long updateTime = System.currentTimeMillis();
        return enterpriseChannelUserMapper.batchUpdateRenewStatus(channelUserIds, renewalStatus, updateTime);
    }
    
    /**
     * 检查骑手是否为自主续费，true 则为自主续费， false 则为非自主续费
     *
     * @param enterpriseChannelUserQuery
     * @return
     */
    @Slave
    @Override
    public Boolean checkUserRenewalStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(enterpriseChannelUserQuery.getUid());
        if (Objects.nonNull(enterpriseChannelUser) && RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())) {
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * 根据骑手UID，检查当前用户是否为自主续费状态， FALSE-企业代付， TRUE-自主续费
     *
     * @param uid
     * @return
     */
    @Override
    public Boolean checkRenewalStatusByUid(Long uid) {
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(uid);
        if (Objects.nonNull(enterpriseChannelUser) && RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())) {
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    @Override
    public List<EnterpriseChannelUser> queryChannelUserList(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setEnterpriseId(enterpriseChannelUserQuery.getEnterpriseId());
        enterpriseChannelUser.setTenantId(enterpriseChannelUserQuery.getTenantId());
        enterpriseChannelUser.setRenewalStatus(enterpriseChannelUserQuery.getRenewalStatus());
        
        List<EnterpriseChannelUser> enterpriseChannelUserList = enterpriseChannelUserMapper.queryAll(enterpriseChannelUser);
        
        return enterpriseChannelUserList;
    }
    
    @Override
    public Integer updateChannelUserStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setId(enterpriseChannelUserQuery.getId());
        enterpriseChannelUser.setCloudBeanStatus(enterpriseChannelUserQuery.getCloudBeanStatus());
        enterpriseChannelUser.setPaymentStatus(enterpriseChannelUserQuery.getPaymentStatus());
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
        
        return enterpriseChannelUserMapper.update(enterpriseChannelUser);
    }
    
    @Override
    public int update(EnterpriseChannelUser enterpriseChannelUserUpdate) {
        return enterpriseChannelUserMapper.update(enterpriseChannelUserUpdate);
    }
    
    @Override
    public int deleteByEnterpriseId(Long enterpriseId) {
        return enterpriseChannelUserMapper.deleteByEnterpriseId(enterpriseId);
    }
    
    @Override
    public int deleteByUid(Long uid) {
        return enterpriseChannelUserMapper.deleteByUid(uid);
    }
    
    @Override
    public Integer queryNotRecycleUserCount(Long id) {
        return enterpriseChannelUserMapper.queryNotRecycleUserCount(id);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> enterpriseChannelUserSearch(EnterpriseChannelUserQuery query) {
        List<UserInfoSearchVo> list = enterpriseChannelUserMapper.enterpriseChannelUserSearch(query);
        if (CollectionUtils.isEmpty(list)) {
            return Triple.of(true, null, Collections.emptyList());
        }
        
        List<UserInfoSearchVo> collect = list.stream().peek(item -> item.setNameAndPhone(item.getName() + "/" + item.getPhone())).collect(Collectors.toList());
        
        return Triple.of(true, null, collect);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePaymentStatusByUid(Long uid, Integer paymentStatus) {
        log.info("update payment status by uid, uid = {}, payment status = {}", uid, paymentStatus);
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setUid(uid);
        enterpriseChannelUser.setPaymentStatus(paymentStatus);
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
        
        return enterpriseChannelUserMapper.updateChannelUserByUid(enterpriseChannelUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatusForRefundDeposit(Long uid, Integer paymentStatus) {
        log.info("update payment status for refund deposit, uid = {}, payment status = {}", uid, paymentStatus);
        EnterpriseChannelUser enterpriseChannelUser = this.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            return;
        }
        updatePaymentStatusByUid(uid, paymentStatus);
    }
    
    @Slave
    @Override
    public List<EnterpriseChannelUser> listByEnterpriseId(Set<Long> enterpriseIdList) {
        return enterpriseChannelUserMapper.selectListByEnterpriseId(enterpriseIdList);
    }
    
    @Transactional
    @Override
    public Triple<Boolean, String, Object> updateUserAfterQRScanNew(EnterpriseChannelUserQuery query) {
        if (!ObjectUtils.allNotNull(query.getUid(), query.getId())) {
            return Triple.of(false, "ELECTRICITY.0007", "请求数据有误，请检查后操作");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_UPDATE_AFTER_SCAN_LOCK_KEY + query.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        try {
            Triple<Boolean, String, Object> result = checkUserInfo(query);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }
            
            //7. 骑手手机号不能重复添加
            if (Objects.nonNull(query.getPhone())) {
                EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectChannelUserByPhone(query.getPhone(), query.getUid());
                if (Objects.nonNull(channelUser)) {
                    log.warn("The user already used in current enterprise, enterprise id = {}, phone = {}", channelUser.getEnterpriseId(), query.getPhone());
                    return Triple.of(false, "300078", "当前用户手机号已存在, 无法重复添加");
                }
            }
            
            // 查询用户当前企业
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(query.getUid());
            
            Long uid = query.getUid();
            Long channelUserId = query.getId();
            EnterpriseChannelUser channelUserEntity = enterpriseChannelUserMapper.queryById(channelUserId);
            // 企业用户
            Triple<Boolean, String, Object> result1 = doEnterpriseUser(query, channelUser, uid, channelUserEntity, enterpriseChannelUser);
            if (Boolean.FALSE.equals(result1.getLeft())) {
                return result1;
            }
            
            // 非企业用户
            Triple<Boolean, String, Object> result2 = doNoEnterpriseUser(query, channelUser, uid, channelUserId, channelUserEntity, enterpriseChannelUser);
            if (Boolean.FALSE.equals(result2.getLeft())) {
                return result2;
            }
            
            log.info("Add user success for QR scan, uid = {}, enterprise channel user id = {}", uid, channelUserId);
            
        } catch (Exception e) {
            log.error("add new user after QR scan error, uid = {}, ex = {}", query.getUid(), e);
            throw new BizException("300068", "扫码添加用户失败");
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_UPDATE_AFTER_SCAN_LOCK_KEY + query.getUid());
        }
        return Triple.of(true, "", enterpriseChannelUser);
    }
    
    /**
     * 通过手机号添加用户
     *
     * @param query
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> addUserNew(EnterpriseChannelUserQuery query) {
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY + query.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        try {
            Triple<Boolean, String, Object> result = checkUserInfo(query);
            if (!result.getLeft()) {
                log.info("add new user check user info error, channel userid = {},msg={}", query.getUid(), result.getRight());
                return result;
            }
            
            //7. 骑手手机号不能重复添加
            if (Objects.nonNull(query.getPhone())) {
                EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectChannelUserByPhone(query.getPhone(), query.getUid());
                if (Objects.nonNull(channelUser)) {
                    log.warn("The user already used in current enterprise, enterprise id = {}, phone = {}", channelUser.getEnterpriseId(), query.getPhone());
                    return Triple.of(false, "300078", "当前用户手机号已存在, 无法重复添加");
                }
            }
            
            // 查询用户当前企业
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(query.getUid());
            
            Long uid = query.getUid();
            
            // 企业用户
            Triple<Boolean, String, Object> result1 = doEnterpriseUserByPhone(query, channelUser);
            if (!result1.getLeft()) {
                return result1;
            }
            
            // 非企业用户
            Triple<Boolean, String, Object> result2 = doNoEnterpriseUserByPhone(query, channelUser, uid, enterpriseChannelUser);
            if (!result2.getLeft()) {
                return result2;
            }
            
            log.info("add new user by enterprise end, enterprise channel user = {}", enterpriseChannelUser.getId());
            
        } catch (Exception e) {
            log.error("add new user by phone error, uid = {}, ex = {}", query.getUid(), e);
            throw new BizException("300067", "添加用户失败");
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY + query.getUid());
        }
        
        return Triple.of(true, "", null);
    }
    
    private Triple<Boolean, String, Object> doNoEnterpriseUserByPhone(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid,
            EnterpriseChannelUser enterpriseChannelUser) {
        log.info("do No Enterprise User start uid = {},msg={}", query.getUid());
        
        boolean isMember = false;
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        // 如果是车电一体的会员则不允许进行添加
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.error("channel user by phone not allow car and battery, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
            return Triple.of(false, "120309", "您已购买车电一体套餐，暂无法支持代付");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryDeposit)) {
            EleDepositOrder depositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            isMember = Objects.equals(depositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
        }
        
        // 判断用户是否为会员用户
        if (isMember && (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_REFUNDING))) {
            // 检测企业代付开关是否开启
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
            if (Objects.isNull(enterpriseInfo.getPurchaseAuthority()) || Objects.equals(enterpriseInfo.getPurchaseAuthority(), EnterpriseInfo.PURCHASE_AUTHORITY_CLOSE)) {
                log.error("enterprise user is platform user not join, enterpriseId={}, uid={}, enterpriseInfo={}", query.getEnterpriseId(), query.getUid(), enterpriseInfo);
                return Triple.of(false, "300082", "已是平台会员，无法加入企业渠道");
            }
        }
    
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("check User Enable Exit! user rent battery,uid={}", uid);
            return Triple.of(false, "120316", "请归还电池后操作");
        }
        
        // 查询当前加入的企业的加盟商
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        boolean isModify = false;
        
        // 如果电池押金或者单车押金已经存在则校验加盟商和即将加入的加盟商是否一致
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_REFUNDING) || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            if (!Objects.equals(userInfo.getFranchiseeId(), enterpriseInfo.getFranchiseeId())) {
                log.error("channel user by phone franchisee diff, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                return Triple.of(false, "120310", "所属加盟商不一致，请退押后操作");
            }
            
            isModify = true;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO) && Objects.nonNull(userBatteryMemberCard) && Objects.equals(
                userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryLastPayDepositTimeByUid(query.getUid(), null, null, null);
            
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(query.getTenantId().intValue());
            
            // 查询退押订单
            EleRefundOrder refundOrder = null;
            
            if (Objects.nonNull(eleDepositOrder)) {
                refundOrder = refundOrderService.queryLastByOrderId(eleDepositOrder.getOrderId());
            }
            
            if (Objects.nonNull(eleDepositOrder) && Objects.equals(eleDepositOrder.getOrderType(), EleDepositOrder.ORDER_TYPE_COMMON) && Objects.nonNull(electricityConfig)
                    && Objects.nonNull(electricityConfig.getChannelTimeLimit()) && Objects.nonNull(refundOrder)) {
                
                Long l = DateUtils.diffDayV2(refundOrder.getUpdateTime(), System.currentTimeMillis());
                if (l.intValue() <= electricityConfig.getChannelTimeLimit()) {
                    log.error("enterprise user by phone is channel time limit user not join, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                    return Triple.of(false, "120306", "渠道保护期内，暂无法加入企业渠道");
                }
            }
        }
        
        if (Objects.isNull(channelUser)) {
            log.info("enterprise channel phone add new user");
            
            // 手机添加
            BeanUtil.copyProperties(query, enterpriseChannelUser);
            enterpriseChannelUser.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setInviterId(merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo()));
            enterpriseChannelUser.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
            enterpriseChannelUser.setCloudBeanStatus(EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT);
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            
            enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);
            
            Long channelUserId = enterpriseChannelUser.getId();
            
            if (!isModify) {
                // 添加用户加盟商信息
                EnterpriseChannelUser channelUser1 = enterpriseChannelUserMapper.selectByUid(uid);
                UserInfo userInfoUpdate = new UserInfo();
                userInfoUpdate.setUid(uid);
                userInfoUpdate.setFranchiseeId(channelUser1.getFranchiseeId());
                userInfoUpdate.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(userInfoUpdate);
            }
            
            // 添加操作记录, 新记录和原站点的记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            EnterpriseChannelUser user = enterpriseChannelUserMapper.queryById(channelUserId);
            BeanUtils.copyProperties(user, history);
            history.setJoinTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
            history.setId(null);
            history.setCreateTime(System.currentTimeMillis());
            history.setUpdateTime(System.currentTimeMillis());
            
            channelUserHistoryMapper.insertOne(history);
        }
        
        // 老企业用户重新加入
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN)) {
            log.info("enterprise channel phone update user");
            // 修改新站点的信息
            enterpriseChannelUser.setId(channelUser.getId());
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setCloudBeanStatus(EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT);
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo()));
            enterpriseChannelUser.setUid(query.getUid());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
            
            // 添加操作记录, 新记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            EnterpriseChannelUser user = enterpriseChannelUserMapper.queryById(channelUser.getId());
            BeanUtils.copyProperties(user, history);
            history.setJoinTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
            history.setId(null);
            history.setCreateTime(System.currentTimeMillis());
            history.setUpdateTime(System.currentTimeMillis());
            
            channelUserHistoryMapper.insertOne(history);
        }
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 1.1 若产生滞纳金：不可开启，提示“该用户未缴纳滞纳金，将影响云豆回收，请联系缴纳后操作”； 1.2 若套餐冻结/审核中，不可开启，提示“该用户套餐已冻结，将影响云豆回收，请联系启用后操作” / "该用户已申请套餐冻结，将影响云豆回收，请联系解除后操作"; 1.3
     * 若未退还电池，不可开启，提示“该用户未退还电池，将影响云豆回收，请联系退还后操作”
     *
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> channelUserExitCheck(EnterpriseUserExitCheckRequest request) {
        // 判断用户是否存在当前站长的企业内
        // 查询当前用户是否为站长
        Long id = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(id);
        if (Objects.isNull(enterpriseInfoVO) || Objects.isNull(enterpriseInfoVO.getId())) {
            log.error("channel User Exit Check  enterprise not exists, uid={}", id);
            return Triple.of(false, "120311", "该用户无法操作");
        }
        
        Long uid = request.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("channel User Exit Check WARN! userInfo is null,uid={},id={}", uid, id);
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        EnterpriseChannelUser user = enterpriseChannelUserMapper.selectByUid(uid);
        if (Objects.isNull(user)) {
            log.error("channel User Exit Check  user not exists, uid={}", uid);
            return Triple.of(false, "120312", "骑手不存在");
        }
        
        if (!Objects.equals(user.getEnterpriseId(), enterpriseInfoVO.getId())) {
            log.error("channel User Exit Check user diff exists, ,uid={},id={}", uid, id);
            return Triple.of(false, "120313", "当前企业下不存在该骑手");
        }
        
        if (!Objects.equals(user.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.error("channel User Exit Check user renewal Status close, uid={}", uid);
            return Triple.of(false, "120305", "当前状态无法操作");
        }
    
        // 已回收的骑手不进行校验
        if (Objects.equals(user.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_RECYCLE)) {
            return Triple.of(true, null, null);
        }
    
        // 未代付骑手不进行校验
        if (Objects.equals(user.getPaymentStatus(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode())) {
            log.warn("channel User Exit Check! user payment not pay,uid={}, id={}", uid, id);
            return Triple.of(true, null, null);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("channel User Exit Check! user rent battery,uid={}", uid);
                return Triple.of(false, "120316", "该用户未退还电池，将影响云豆回收，请联系归还后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("channel User Exit Check! user stop member card review,uid={}", uid);
                return Triple.of(false, "100211", "该用户已申请套餐冻结，将影响云豆回收，请联系解除后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("channel User Exit Check! member card is disable userId={}", uid);
                return Triple.of(false, "120314", "该用户套餐已冻结，将影响云豆回收，请联系启用后操作");
            }
            
            if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
                return Triple.of(true, null, null);
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("channel User Exit Check! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return Triple.of(false, "300003", "套餐不存在");
            }
            
            // 判断滞纳金
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("channel User Exit Check! user exist battery service fee,uid={}", userInfo.getUid());
                return Triple.of(false, "120315", "该用户未缴纳滞纳金，将影响云豆回收，请联系缴纳后操作");
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 骑手自主续费
     *
     * @param request
     * @return
     */
    @Override
    @Transactional
    public Triple<Boolean, String, Object> channelUserExit(EnterpriseUserExitCheckRequest request) {
        Long uid = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_USER_EXIT_LOCK + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        // 自主续费不能为空
        if (Objects.isNull(request.getRenewalStatus())) {
            log.error("channel User Exit renewal Status is null, uid={}", request.getUid());
            return Triple.of(false, "ELECTRICITY.0007", "请求数据有误，请检查后操作");
        }
        
        // 自主续费不能为空
        if (!(Objects.equals(request.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) || Objects.equals(request.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN))) {
            log.error("channel User Exit renewal Status is error, uid={}, renewalStatus={}", request.getUid(), request.getRenewalStatus());
            return Triple.of(false, "ELECTRICITY.0007", "请求数据有误，请检查后操作");
        }
        
        // 企业代付则直接返回成功
        if (Objects.equals(request.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            return Triple.of(true, null, null);
        }
        
        EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(request.getUid());
        if (Objects.isNull(channelUser)) {
            log.error("channel User Exit Check  user not exists, uid={}", request.getUid());
            return Triple.of(false, "120312", "骑手不存在");
        }
        
        // 检测骑手的续费状态是否为关闭
        if (!Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.error("channel User Exit renewal Status diff, uid={}, userRenewalStatus={}, renewalStatus={}", request.getUid(), channelUser.getRenewalStatus(),
                    request.getRenewalStatus());
            return Triple.of(false, "120305", "当前状态无法操作");
        }
        
        // 检测是否能退出
        Triple<Boolean, String, Object> triple = this.channelUserExitCheck(request);
        if (!triple.getLeft()) {
            log.error("channel User Exit Check fail, uid={}, msg={}", request.getUid(), triple.getRight());
            return triple;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(request.getUid());
    
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(request.getUid());
        boolean isMember = false;
        if (Objects.nonNull(userBatteryDeposit)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
        }
        
        boolean existPayRecord = anotherPayMembercardRecordService.existPayRecordByUid(request.getUid());
        boolean isEnterpriseFreeDepositNoPay = true;
        // 企业免押用户，且不存在代付记录
        if (!isMember && Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && !existPayRecord) {
            isEnterpriseFreeDepositNoPay = false;
        }
        
        // 免押用户 不存在代付记录 则单独进行押金回收
        if (!isEnterpriseFreeDepositNoPay) {
            Triple<Boolean, String, Object> tripleRecycle = enterpriseInfoService.recycleCloudBeanForFreeDeposit(request.getUid(), SecurityUtils.getUid());
            if (!tripleRecycle.getLeft()) {
                log.error("channel user exit recycle Cloud Bean error,uid={}, msg={}", request.getUid(), tripleRecycle.getRight());
                return tripleRecycle;
            }
        } else if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(channelUser.getCloudBeanStatus(), EnterpriseChannelUser.NO_RECYCLE)) {
            Triple<Boolean, String, Object> tripleRecycle = enterpriseInfoService.recycleCloudBean(request.getUid(), SecurityUtils.getUid());
            if (!tripleRecycle.getLeft()) {
                log.error("channel user exit recycle Cloud Bean error,uid={}, msg={}", request.getUid(), tripleRecycle.getRight());
                return tripleRecycle;
            }
        }
        
        // 增加退出记录
        // 添加操作记录, 新记录和原站点的记录
        EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
        BeanUtils.copyProperties(channelUser, history);
        history.setExitTime(System.currentTimeMillis());
        history.setType(EnterpriseChannelUserHistory.EXIT);
        
        channelUserHistoryMapper.insertOne(history);
        
        // 修改骑手为开启
        EnterpriseChannelUser enterpriseChannelUserUpdate = new EnterpriseChannelUser();
        enterpriseChannelUserUpdate.setId(channelUser.getId());
        enterpriseChannelUserUpdate.setRenewalStatus(EnterpriseChannelUser.RENEWAL_OPEN);
        enterpriseChannelUserUpdate.setUpdateTime(System.currentTimeMillis());
        update(enterpriseChannelUserUpdate);
        
        // 判断用户是否存在与企业渠道用户然后站长退出的表中，类型未未处理或者是处理失败
        List<Integer> typeList = new ArrayList<>();
        typeList.add(EnterpriseChannelUserExit.TYPE_INIT);
        List<Long> uidList = new ArrayList<>();
        uidList.add(request.getUid());
        EnterpriseChannelUserExitQueryModel queryModel = EnterpriseChannelUserExitQueryModel.builder().uidList(uidList).typeList(typeList).build();
        List<EnterpriseChannelUserExit> channelUserList = channelUserExitMapper.list(queryModel);
        if (ObjectUtils.isNotEmpty(channelUserList)) {
            List<Long> idList = channelUserList.stream().map(EnterpriseChannelUserExit::getId).collect(Collectors.toList());
            
            // 修改用户退出成功
            channelUserExitMapper.batchUpdateById(null, EnterpriseChannelUserExit.TYPE_SUCCESS, idList, System.currentTimeMillis());
        }
        
        log.info("channel User Exit Check success, uid={}, msg={}", request.getUid());
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> channelUserExitCheckAll(EnterpriseUserExitCheckRequest request) {
        // 查询当前用户是否为站长
        Long uid = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(uid);
        if (Objects.isNull(enterpriseInfoVO)) {
            log.error("channel user exit  all  enterprise not exists, uid={}", uid);
            return Triple.of(false, "120311", "该用户无法操作");
        }
        
        if (!Objects.equals(enterpriseInfoVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.error("channel user exit  all  enterprise station not exists, uid={}", uid);
            return Triple.of(false, "120305", "当前状态无法操作");
        }
        
        // 查询当前用户下的所有的骑手
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setEnterpriseId(enterpriseInfoVO.getId());
        enterpriseChannelUser.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
        List<EnterpriseChannelUser> enterpriseChannelUserList = this.enterpriseChannelUserMapper.queryAll(enterpriseChannelUser);
        if (ObjectUtils.isEmpty(enterpriseChannelUserList)) {
            log.error("channel user exit all  user data user is empty, uid={}", uid);
            return Triple.of(true, null, null);
        }
        
        for (EnterpriseChannelUser channelUser : enterpriseChannelUserList) {
            if (Objects.equals(channelUser.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_RECYCLE)) {
                continue;
            }
    
            // 未代付骑手不进行校验（会员用户未代付的情况不需要进行检测）
            if (Objects.equals(channelUser.getPaymentStatus(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode())) {
                continue;
            }
            
            // 检测用户能否退出
            Triple<Boolean, String, Object> tripleCheck = checkUserEnableExit(channelUser.getUid());
            if (!tripleCheck.getLeft()) {
                return tripleCheck;
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryEnterpriseChannelUserList() {
        Long uid = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(uid);
        if (Objects.isNull(enterpriseInfoVO)) {
            log.error("query channel user   enterprise not exists, uid={}", uid);
            return Triple.of(false, "120311", "该用户无法操作");
        }
        
        // 查询当前用户下的所有的骑手
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setEnterpriseId(enterpriseInfoVO.getId());
        enterpriseChannelUser.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
        List<EnterpriseChannelUser> enterpriseChannelUserList = this.enterpriseChannelUserMapper.queryAll(enterpriseChannelUser);
        if (ObjectUtils.isEmpty(enterpriseChannelUserList)) {
            log.error("query channel user data is empty, uid={}", uid);
            return Triple.of(false, "300082", "未找到骑手信息");
        }
        
        List<EnterpriseChannelUser> channelUserList = enterpriseChannelUserList.stream().filter(item -> Objects.nonNull(item.getUid())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(channelUserList)) {
            log.error("query channel user data user is empty, uid={}", uid);
            return Triple.of(false, "300082", "未找到骑手信息");
        }
        
        List<EnterpriseChannelUserExitVO> list = new ArrayList<>();
        for (EnterpriseChannelUser user : enterpriseChannelUserList) {
            EnterpriseChannelUserExitVO vo = new EnterpriseChannelUserExitVO();
            Long uid1 = user.getUid();
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid1);
            if (Objects.isNull(userInfo)) {
                log.warn("query Enterprise Channel User! userInfo is null,uid={}", uid1);
                continue;
            }
            
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("query Enterprise Channel User! userBatteryMemberCard is null,uid={}", uid1);
                continue;
            }
            
            vo.setUserName(userInfo.getName());
            vo.setPhone(userInfo.getPhone());
            //设置电池编码
            ElectricityBattery electricityBattery = batteryService.queryByUid(uid1);
            if (Objects.nonNull(electricityBattery)) {
                vo.setBatterySn(electricityBattery.getSn());
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("query Enterprise Channel User! user stop member card review,uid={}", uid);
                vo.setStatus(EnterpriseChannelUserExitVO.FREE_APPLY);
                list.add(vo);
                continue;
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("query Enterprise Channel User! member card is disable userId={}", uid);
                vo.setStatus(EnterpriseChannelUserExitVO.FREE);
                list.add(vo);
                continue;
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("query Enterprise Channel User! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                continue;
            }
            
            // 判断滞纳金
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("query Enterprise Channel User! user exist battery service fee,uid={}", userInfo.getUid());
                vo.setStatus(EnterpriseChannelUserExitVO.SERVICE_FEE);
                list.add(vo);
                continue;
            }
            
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("query Enterprise Channel User! user rent battery,uid={}", uid);
                vo.setStatus(EnterpriseChannelUserExitVO.BATTERY_EXIT);
                list.add(vo);
            }
        }
        
        return Triple.of(true, null, list);
    }
    
    @Transactional
    @Override
    public Triple<Boolean, String, Object> channelUserExitAll(EnterpriseUserExitCheckRequest request) {
        // 查询当前用户是否为站长
        Long uid = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_USER_EXIT_ALL_LOCK + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(uid);
        if (Objects.isNull(enterpriseInfoVO) || Objects.isNull(enterpriseInfoVO.getId())) {
            log.warn("channel user exit all enterprise not exists, uid={}", uid);
            return Triple.of(false, "120311", "该用户无法操作");
        }
        
        if (!Objects.equals(enterpriseInfoVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.warn("channel user exit all enterprise station not exists, uid={}", uid);
            return Triple.of(false, "120305", "当前状态无法操作");
        }
        
        // 查询当前用户下的所有的骑手
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setEnterpriseId(enterpriseInfoVO.getId());
        enterpriseChannelUser.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
        List<EnterpriseChannelUser> enterpriseChannelUserList = this.enterpriseChannelUserMapper.queryAll(enterpriseChannelUser);
        if (ObjectUtils.isEmpty(enterpriseChannelUserList)) {
            log.warn("channel user exit all user data user is null, uid={}", uid);
            // 修改站长本身的状态为
            Long id = enterpriseInfoVO.getId();
            EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
            enterpriseInfo.setId(id);
            enterpriseInfo.setRenewalStatus(EnterpriseChannelUser.RENEWAL_OPEN);
            enterpriseInfo.setUpdateTime(System.currentTimeMillis());
            enterpriseInfoService.update(enterpriseInfo);
            return Triple.of(true, null, null);
        }
        
        request.setRenewalStatus(1);
        
        // 检测用户能否退出
        Triple<Boolean, String, Object> tripleCheck = channelUserExitCheckAll(request);
        if (!tripleCheck.getLeft()) {
            log.warn("channel user exit all check warn uid={}, msg={}", uid, tripleCheck.getRight());
            return tripleCheck;
        }
        
        List<Long> channelUserIds = new ArrayList<>();
        List<EnterpriseChannelUserExit> addList = new ArrayList<>();
        for (EnterpriseChannelUser item : enterpriseChannelUserList) {
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(item.getUid());
            
            boolean isMember = false;
            if (Objects.nonNull(userBatteryDeposit)) {
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
            }
            
            boolean existPayRecord = anotherPayMembercardRecordService.existPayRecordByUid(item.getUid());
            boolean isEnterpriseFreeDepositNoPay = true;
            // 企业免押用户，且不存在代付记录
            if (!isMember && Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && !existPayRecord) {
                isEnterpriseFreeDepositNoPay = false;
            }
            
            // cloudBeanStatus=（云豆状态（0-初始态, 1-未回收, 2-已回收）） 初始态，已回收的
            if ((isEnterpriseFreeDepositNoPay && Objects.equals(item.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT)) || Objects.equals(item.getCloudBeanStatus(),
                    EnterpriseChannelUser.CLOUD_BEAN_STATUS_RECYCLE)) {
                channelUserIds.add(item.getId());
            }
            
            // 云豆未回收的
            if (Objects.equals(item.getCloudBeanStatus(), EnterpriseChannelUser.NO_RECYCLE) || (!isEnterpriseFreeDepositNoPay && Objects.equals(item.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT))) {
                EnterpriseChannelUserExit exit = new EnterpriseChannelUserExit();
                exit.setChannelUserId(item.getId());
                exit.setEnterpriseId(item.getEnterpriseId());
                exit.setTenantId(item.getTenantId());
                exit.setType(EnterpriseChannelUserExit.TYPE_INIT);
                exit.setFranchiseeId(item.getFranchiseeId());
                exit.setCreateTime(System.currentTimeMillis());
                exit.setUid(item.getUid());
                exit.setOperateUid(SecurityUtils.getUid());

                addList.add(exit);
            }
        }
        
        // 批量修改骑手的状态未续费打开
        if (ObjectUtils.isNotEmpty(channelUserIds)) {
            log.error("channel user exit all batch Update Renew Status, channelUserIds={},uid={}", channelUserIds, uid);
            enterpriseChannelUserMapper.batchUpdateRenewStatus(channelUserIds, EnterpriseChannelUser.RENEWAL_OPEN, System.currentTimeMillis());
        }
        
        // 添加记录
        if (ObjectUtils.isNotEmpty(addList)) {
            log.error("channel user exit all batch insert history,  uid={}", uid);
            channelUserExitMapper.batchInsert(addList);
        }
        
        // 修改站长本身的状态为
        Long id = enterpriseInfoVO.getId();
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        enterpriseInfo.setId(id);
        enterpriseInfo.setRenewalStatus(EnterpriseChannelUser.RENEWAL_OPEN);
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        enterpriseInfoService.update(enterpriseInfo);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> channelUserClose(EnterpriseUserExitCheckRequest request) {
        // 查询当前用户是否为站长
        Long uid = merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo());
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(uid);
        if (Objects.isNull(enterpriseInfoVO)) {
            log.error("channel user close  enterprise not exists, uid={}", request.getUid());
            return Triple.of(false, "120311", "该用户无法操作");
        }
        
        if (!Objects.equals(enterpriseInfoVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN)) {
            log.error("channel user close  enterprise station not exists, uid={}", request.getUid());
            return Triple.of(false, "120305", "当前状态无法操作");
        }
        
        // 修改站长本身的状态为
        Long id = enterpriseInfoVO.getId();
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        enterpriseInfo.setId(id);
        enterpriseInfo.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        enterpriseInfoService.update(enterpriseInfo);
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 检测
     *
     * @param query
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> addUserByScanNewCheck(EnterpriseChannelUserQuery query) {
        EnterpriseChannelUserScanCheckVO vo = new EnterpriseChannelUserScanCheckVO();
        if (!ObjectUtils.allNotNull(query.getUid(), query.getId())) {
            return Triple.of(true, null, vo);
        }
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        try {
            Triple<Boolean, String, Object> result = checkUserInfo(query);
            vo.setEnterpriseId(query.getEnterpriseId());
            vo.setEnterpriseName(query.getEnterpriseName());
            if (Boolean.FALSE.equals(result.getLeft())) {
                return Triple.of(true, null, vo);
            }
            
            //7. 骑手手机号不能重复添加
            if (Objects.nonNull(query.getPhone())) {
                EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectChannelUserByPhone(query.getPhone(), query.getUid());
                if (Objects.nonNull(channelUser)) {
                    log.error("add user by scan new check error! phone replete uid={}, phone={}", query.getUid(), query.getPhone());
                    return Triple.of(true, null, vo);
                }
            }
            
            // 查询用户当前企业
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(query.getUid());
            Long uid = query.getUid();
            Long channelUserId = query.getId();
            EnterpriseChannelUser channelUserEntity = enterpriseChannelUserMapper.queryById(channelUserId);
            // 企业用户
            doEnterpriseUserCheck(query, channelUser, uid, channelUserEntity, enterpriseChannelUser, vo);
            return Triple.of(true, null, vo);
            
        } catch (Exception e) {
            log.error("add new user after QR scan check error, uid = {}, ex = {}", query.getUid(), e);
            throw new BizException("300068", "扫码添加用户失败");
            
        }
    }
    
    /**
     * 运营商解绑企业用户
     * @param request
     * @return
     */
    @Override
    @Transactional
    public Triple<Boolean, String, Object> channelUserExitForAdmin(EnterpriseUserAdminExitCheckRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long uid = SecurityUtils.getUid();
        
        // 检测骑手是否为企业用户
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_USER_ADMIN_EXIT_LOCK + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(request.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            log.warn("channel user admin exit check warn, userAccount is cancelling, uid={}", request.getUid());
            return Triple.of(false, "120163", "账号处于注销缓冲期内，无法操作");
        }
        
        EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(request.getUid());
        
        // 检测是否能退出
        Triple<Boolean, String, Object> triple = this.channelUserAdminExitCheck(request, channelUser, tenantId);
        if (!triple.getLeft()) {
            log.warn("channel user admin exit check warn, uid={}, msg={}", request.getUid(), triple.getRight());
            return triple;
        }
    
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(channelUser.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.warn("channel user admin exit warn, uid={}", request.getUid());
            return Triple.of(false, "120212", "商户不存在");
        }
        
        request.setEnterpriseId(enterpriseInfo.getId());
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(request.getUid());
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(request.getUid());
    
        boolean isMember = false;
        if (Objects.nonNull(userBatteryDeposit)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
        }
        
        boolean existPayRecord = anotherPayMembercardRecordService.existPayRecordByUid(request.getUid());
        boolean isEnterpriseFreeDepositNoPay = true;
        // 企业免押用户，且不存在代付记录
        if (!isMember && Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && !existPayRecord) {
            isEnterpriseFreeDepositNoPay = false;
        }
        
        // 免押用户 不存在代付记录 则单独进行押金回收
        if (!isEnterpriseFreeDepositNoPay) {
            Triple<Boolean, String, Object> tripleRecycle = enterpriseInfoService.recycleCloudBeanForFreeDeposit(request.getUid(), uid);
            if (!tripleRecycle.getLeft()) {
                log.warn("channel user admin exit recycle Cloud Bean warn,uid={}, msg={}", request.getUid(), tripleRecycle.getRight());
                return tripleRecycle;
            }
        } else if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(channelUser.getCloudBeanStatus(), EnterpriseChannelUser.NO_RECYCLE)) {
            // 云豆回收
            Triple<Boolean, String, Object> tripleRecycle = enterpriseInfoService.recycleCloudBean(request.getUid(), uid);
            if (!tripleRecycle.getLeft()) {
                log.warn("channel user admin exit recycle Cloud Bean warn,uid={}, msg={}", request.getUid(), tripleRecycle.getRight());
                return tripleRecycle;
            }
        }
        
        // 增加退出记录
        // 添加操作记录, 新记录和原站点的记录
        EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
        BeanUtils.copyProperties(channelUser, history);
        history.setExitTime(System.currentTimeMillis());
        history.setType(EnterpriseChannelUserHistory.EXIT);
        
        channelUserHistoryMapper.insertOne(history);
        
        // 修改骑手为开启
        EnterpriseChannelUser enterpriseChannelUserUpdate = new EnterpriseChannelUser();
        enterpriseChannelUserUpdate.setId(channelUser.getId());
        enterpriseChannelUserUpdate.setRenewalStatus(EnterpriseChannelUser.RENEWAL_OPEN);
        enterpriseChannelUserUpdate.setUpdateTime(System.currentTimeMillis());
        update(enterpriseChannelUserUpdate);
        
        // 判断用户是否存在与企业渠道用户然后站长退出的表中，类型未未处理或者是处理失败
        List<Integer> typeList = new ArrayList<>();
        typeList.add(EnterpriseChannelUserExit.TYPE_INIT);
        List<Long> uidList = new ArrayList<>();
        uidList.add(request.getUid());
        EnterpriseChannelUserExitQueryModel queryModel = EnterpriseChannelUserExitQueryModel.builder().uidList(uidList).typeList(typeList).build();
        List<EnterpriseChannelUserExit> channelUserList = channelUserExitMapper.list(queryModel);
        if (ObjectUtils.isNotEmpty(channelUserList)) {
            List<Long> idList = channelUserList.stream().map(EnterpriseChannelUserExit::getId).collect(Collectors.toList());
            
            // 修改用户退出成功
            channelUserExitMapper.batchUpdateById(null, EnterpriseChannelUserExit.TYPE_SUCCESS, idList, System.currentTimeMillis());
        }
        
        // 操作记录
        Map<String, String> operateRecordMap = new HashMap<>();
        operateRecordMap.put("merchantName", enterpriseInfo.getName());
        operateRecordMap.put("reason", request.getReason());
        operateRecordUtil.record(null, operateRecordMap);
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 存在续费关闭用户
     * @param id
     * @return
     */
    @Override
    @Slave
    public int existsRenewCloseUser(Long id) {
        return enterpriseChannelUserMapper.existsRenewCloseUser(id);
    }
    
    @Slave
    @Override
    public List<EnterpriseChannelUserVO> listByUidList(List<Long> uidList, Integer tenantId) {
        return enterpriseChannelUserMapper.selectListByUidList(uidList, tenantId);
    }
    
    private Triple<Boolean, String, Object> channelUserAdminExitCheck(EnterpriseUserAdminExitCheckRequest request, EnterpriseChannelUser channelUser, Integer tenantId) {
        // 判断用户是否存在当前站长的企业内
        Long uid = request.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("channel user admin exit Check WARN! userInfo is null,uid={},id={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.isNull(channelUser)) {
            log.error("channel user exit Check  user not exists, uid={}", request.getUid());
            return Triple.of(false, "120312", "骑手不存在");
        }
    
        if (Objects.isNull(channelUser.getTenantId()) || !Objects.equals(tenantId, channelUser.getTenantId().intValue())) {
            log.error("channel user admin exit Check  user not exists, tenant not equal, uid={}, tenantId={}", request.getUid(), tenantId);
            return Triple.of(false, "120312", "骑手不存在");
        }
        
        // 检测骑手的续费状态是否为关闭
        if (!Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.error("channel user admin exit renewal Status diff, uid={}, userRenewalStatus={}", request.getUid(), channelUser.getRenewalStatus());
            return Triple.of(false, "120305", "当前状态无法操作");
        }
        
        // 已经回收云豆直接返回正确的状态
        if (Objects.equals(channelUser.getCloudBeanStatus(), EnterpriseChannelUser.CLOUD_BEAN_STATUS_RECYCLE)) {
            return Triple.of(true, null, null);
        }
    
        // 未代付骑手不进行校验
        if (Objects.equals(channelUser.getPaymentStatus(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode())) {
            log.warn("channel user admin Exit Check! user payment not pay,uid={}", uid);
            return Triple.of(true, null, null);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("channel user admin exit check! user rent battery,uid={}", uid);
                return Triple.of(false, "120316", "该用户未退还电池，将影响云豆回收，请联系归还后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("channel user admin exit check! user stop member card review,uid={}", uid);
                return Triple.of(false, "100211", "该用户已申请套餐冻结，将影响云豆回收，请联系解除后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("channel user admin exit check! member card is disable userid={}", uid);
                return Triple.of(false, "120314", "该用户套餐已冻结，将影响云豆回收，请联系启用后操作");
            }
            
            // 退租用户直接返回正确状态
            if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
                return Triple.of(true, null, null);
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("channel user admin exit check! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return Triple.of(false, "300003", "套餐不存在");
            }
            
            // 判断滞纳金
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("channel user admin exit check! user exist battery service fee,uid={}", userInfo.getUid());
                return Triple.of(false, "120315", "该用户未缴纳滞纳金，将影响云豆回收，请联系缴纳后操作");
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> doEnterpriseUserCheck(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid,
            EnterpriseChannelUser channelUserEntity, EnterpriseChannelUser enterpriseChannelUser, EnterpriseChannelUserScanCheckVO vo) {
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            vo.setOldEnterpriseId(channelUser.getEnterpriseId());
            return Triple.of(false, "120300", "已是该渠道用户，无需重复添加");
        }
        
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && !Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            vo.setOldEnterpriseId(channelUser.getEnterpriseId());
            // 判断两个加盟商是否一致
            if (!Objects.equals(channelUser.getFranchiseeId(), query.getFranchiseeId())) {
                log.error("add user by scan new check error! franchiseeId different uid={}, franchiseeId={}", query.getUid(), channelUser.getFranchiseeId());
                return Triple.of(false, "120301", "切换站点的加盟商必须一致");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(userBatteryMemberCard)) {
                // 检测用户能否退出
                Triple<Boolean, String, Object> tripleCheck = checkUserEnableExit(uid);
                if (Objects.equals(tripleCheck.getMiddle(), "100211")) {
                    vo.setStatus("4");
                }
                if (Objects.equals(tripleCheck.getMiddle(), "120314")) {
                    vo.setStatus("3");
                }
                if (Objects.equals(tripleCheck.getMiddle(), "120315")) {
                    vo.setStatus("1");
                }
                if (Objects.equals(tripleCheck.getMiddle(), "120316")) {
                    vo.setStatus("2");
                }
            }
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> doEnterpriseUserByPhone(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser) {
        log.info("do Enterprise User By Phone start uid = {},msg={}", query.getUid());
        
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            log.info("enterprise channel user by phone repeat, enterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getUid());
            return Triple.of(false, "120300", "已是该渠道用户，无需重复添加");
        }
        
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && !Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            log.info("enterprise channel user by phone station diff, oldEnterpriseId={}, newEnterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getEnterpriseId(),
                    query.getUid());
            return Triple.of(false, "120302", "如用户需切换站点，请切换到面对面添加扫码操作");
        }
        log.info("do Enterprise User By Phone end uid = {},msg={}", query.getUid());
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> doNoEnterpriseUser(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid, Long channelUserId,
            EnterpriseChannelUser channelUserEntity, EnterpriseChannelUser enterpriseChannelUser) {
        log.info("scan code enterprise user start uid = {},msg={}", query.getUid());
        
        boolean isMember = false;
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        // 如果是车电一体的会员则不允许进行添加
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.error("scan code channel user not allow car and battery, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
            return Triple.of(false, "120309", "您已购买车电一体套餐，暂无法支持代付");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryDeposit)) {
            EleDepositOrder depositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.nonNull(depositOrder)) {
                isMember = Objects.equals(depositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
            }
        }
        
        // 判断用户是否为会员用户
        if (isMember && (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_REFUNDING))) {
            // 检测企业代付开关是否开启
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
            if (Objects.isNull(enterpriseInfo.getPurchaseAuthority()) || Objects.equals(enterpriseInfo.getPurchaseAuthority(), EnterpriseInfo.PURCHASE_AUTHORITY_CLOSE)) {
                log.error("scan code enterprise user is platform user not join, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                return Triple.of(false, "300082", "已是平台会员，无法加入企业渠道");
            }
        }
    
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("check User Enable Exit! user rent battery,uid={}", uid);
            return Triple.of(false, "120316", "请归还电池后操作");
        }
        
        boolean isModify = false;
        // 如果电池押金或者单车押金已经存在则校验加盟商和即将加入的加盟商是否一致
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_REFUNDING) || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            if (!Objects.equals(userInfo.getFranchiseeId(), query.getFranchiseeId())) {
                log.error("scan code channel user franchisee diff, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                return Triple.of(false, "120310", "所属加盟商不一致，请退押后操作");
            }
            isModify = true;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        log.info("userBatteryMemberCard ={}, userInfo={}", userBatteryMemberCard, userInfo);
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO) && Objects.nonNull(userBatteryMemberCard) && Objects.equals(
                userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryLastPayDepositTimeByUid(query.getUid(), null, null, null);
            
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(query.getTenantId().intValue());
            
            // 查询退押订单
            // 查询退押订单
            EleRefundOrder refundOrder = null;
            
            if (Objects.nonNull(eleDepositOrder)) {
                refundOrder = refundOrderService.queryLastByOrderId(eleDepositOrder.getOrderId());
            }
            log.info("eleDepositOrder ={}, refundOrder={}, electricityConfig={}", eleDepositOrder, refundOrder, electricityConfig);
            if (Objects.nonNull(eleDepositOrder) && Objects.equals(eleDepositOrder.getOrderType(), EleDepositOrder.ORDER_TYPE_COMMON) && Objects.nonNull(electricityConfig)
                    && Objects.nonNull(electricityConfig.getChannelTimeLimit()) && Objects.nonNull(refundOrder)) {
                
                Long l = DateUtils.diffDayV2(refundOrder.getUpdateTime(), System.currentTimeMillis());
                log.info("doNoEnterpriseUser uid={}, days={}", query.getUid(), l);
                if (l.intValue() <= electricityConfig.getChannelTimeLimit()) {
                    log.error("scan code enterprise user is channel time limit user not join, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                    return Triple.of(false, "120306", "渠道保护期内，暂无法加入企业渠道");
                }
            }
        }
        
        //检查是否已经有用户被关联至当前企业
        if (Objects.isNull(channelUserEntity)) {
            log.error("scan code query enterprise channel record failed after QR scan,  uid = {}, channel user record id={}", uid, channelUserId);
            return Triple.of(false, "300082", "企业信息不存在, 请检测后操作");
        }
        
        if (Objects.nonNull(channelUserEntity.getUid())) {
            log.error("scan code user already exist after QR scan,  uid = {}, channel user record id={}", uid, channelUserId);
            return Triple.of(false, "300083", "已添加其他用户, 请重新扫码");
        }
        
        if (Objects.isNull(channelUser)) {
            log.info("enterprise channel scan add new user");
            // 扫码添加
            enterpriseChannelUser.setId(channelUserId);
            enterpriseChannelUser.setUid(uid);
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setRenewalStatus(EnterpriseChannelUser.RENEWAL_CLOSE);
            enterpriseChannelUser.setCloudBeanStatus(EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT);
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
            
            // 如果换电套餐已经缴纳的
            if (!isModify) {
                // 添加用户加盟商信息
                EnterpriseChannelUser channelUser1 = enterpriseChannelUserMapper.selectByUid(uid);
                UserInfo userInfoUpdate = new UserInfo();
                userInfoUpdate.setUid(uid);
                userInfoUpdate.setFranchiseeId(channelUser1.getFranchiseeId());
                userInfoUpdate.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(userInfoUpdate);
            }
            
            // 添加操作记录, 新记录和原站点的记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            EnterpriseChannelUser user = enterpriseChannelUserMapper.queryById(channelUserId);
            BeanUtils.copyProperties(user, history);
            history.setId(null);
            history.setCreateTime(System.currentTimeMillis());
            history.setUpdateTime(System.currentTimeMillis());
            history.setJoinTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
            
            channelUserHistoryMapper.insertOne(history);
        }
        
        // 老企业用户重新加入
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN)) {
            log.info("enterprise channel scan update user");
            // 修改新站点的信息
            enterpriseChannelUser.setId(channelUser.getId());
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setCloudBeanStatus(EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT);
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo()));
            enterpriseChannelUser.setUid(query.getUid());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
            
            // 添加操作记录, 新记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            EnterpriseChannelUser user = enterpriseChannelUserMapper.queryById(channelUser.getId());
            BeanUtils.copyProperties(user, history);
            history.setJoinTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
            history.setId(null);
            history.setCreateTime(System.currentTimeMillis());
            history.setUpdateTime(System.currentTimeMillis());
            
            channelUserHistoryMapper.insertOne(history);
        }
        
        return Triple.of(true, null, null);
    }
    
    /**
     * @param query
     * @param channelUser            ：当前骑手本身的uid
     * @param uid
     * @param channelUserEntity:     扫码空的channelUser
     * @param enterpriseChannelUser: 企业空的uid
     * @return
     */
    private Triple<Boolean, String, Object> doEnterpriseUser(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid, EnterpriseChannelUser channelUserEntity,
            EnterpriseChannelUser enterpriseChannelUser) {
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            log.info("enterprise channel user by scan repeat, enterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getUid());
            return Triple.of(false, "120300", "已是该渠道用户，无需重复添加");
        }
        
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && !Objects.equals(channelUser.getEnterpriseId(),
                query.getEnterpriseId())) {
            log.info("enterprise channel switch user, uid={}", query.getUid());
            // 切换站点
            // 判断两个加盟商是否一致
            if (!Objects.equals(channelUser.getFranchiseeId(), query.getFranchiseeId())) {
                log.info("enterprise channel switch user station diff, oldEnterpriseId={}, newEnterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getEnterpriseId(),
                        query.getUid());
                return Triple.of(false, "120301", "切换站点的加盟商必须一致");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
    
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            boolean isMember = false;
            if (Objects.nonNull(userBatteryDeposit)) {
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
            }
            
            boolean existPayRecord = anotherPayMembercardRecordService.existPayRecordByUid(userInfo.getUid());
            boolean isEnterpriseFreeDepositNoPay = true;
            // 企业免押用户，且不存在代付记录
            if (!isMember && Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && !existPayRecord) {
                isEnterpriseFreeDepositNoPay = false;
            }
            
            // 免押用户 不存在代付记录 则单独进行押金回收
            if (!isEnterpriseFreeDepositNoPay) {
                Triple<Boolean, String, Object> tripleRecycle = enterpriseInfoService.recycleCloudBeanForFreeDeposit(userInfo.getUid(), SecurityUtils.getUid());
                if (!tripleRecycle.getLeft()) {
                    log.warn("enterprise channel switch user recycle cloud bean warn,uid={}, msg={}", userInfo.getUid(), tripleRecycle.getRight());
                    return tripleRecycle;
                }
            } else if (Objects.nonNull(userBatteryMemberCard)) {
                // 检测用户能否退出
                Triple<Boolean, String, Object> tripleCheck = checkUserEnableExit(uid);
                if (!tripleCheck.getLeft()) {
                    log.warn("enterprise channel switch user check error, uid={},msg={}", userInfo.getUid(), tripleCheck.getRight());
                    return tripleCheck;
                }
    
                // 未回收的云豆的情况进行云豆回收
                if (Objects.equals(channelUser.getCloudBeanStatus(), EnterpriseChannelUser.NO_RECYCLE)) {
                    // 回收云豆
                    Triple<Boolean, String, Object> triple = enterpriseInfoService.recycleCloudBean(query.getUid(), SecurityUtils.getUid());
                    if (!triple.getLeft()) {
                        log.warn("enterprise channel switch user recycle cloud bean error, uid={},msg={}", userInfo.getUid(), triple.getRight());
                        return triple;
                    }
                }
            }
            
            // 修改新站点的信息
            enterpriseChannelUser.setId(channelUser.getId());
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setCloudBeanStatus(EnterpriseChannelUser.CLOUD_BEAN_STATUS_INIT);
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(merchantEmployeeService.getCurrentMerchantUid(SecurityUtils.getUserInfo()));
            enterpriseChannelUser.setUid(query.getUid());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
            
            // 添加操作记录, 新记录和原站点的记录
            List<EnterpriseChannelUserHistory> channelUserList = new ArrayList<>();
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            BeanUtils.copyProperties(channelUser, history);
            history.setExitTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.EXIT);
            history.setCreateTime(System.currentTimeMillis());
            history.setUpdateTime(System.currentTimeMillis());
            history.setId(null);
            channelUserList.add(history);
            
            EnterpriseChannelUserHistory channelUserHistory = new EnterpriseChannelUserHistory();
            BeanUtils.copyProperties(enterpriseChannelUser, channelUserHistory);
            channelUserHistory.setJoinTime(System.currentTimeMillis());
            channelUserHistory.setType(EnterpriseChannelUserHistory.JOIN);
            channelUserHistory.setId(null);
            channelUserHistory.setCreateTime(System.currentTimeMillis());
            channelUserHistory.setUpdateTime(System.currentTimeMillis());
            channelUserList.add(channelUserHistory);
            
            channelUserHistoryMapper.batchInsert(channelUserList);
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserEnableExit(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("check User Enable Exit! user rent battery,uid={}", uid);
                return Triple.of(false, "120316", "该用户未退还电池，将影响云豆回收，请联系归还后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("check User Enable Exit! user stop member card review,uid={}", uid);
                return Triple.of(false, "100211", "该用户已申请套餐冻结，将影响云豆回收，请联系解除后操作");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("check User Enable Exit! member card is disable userId={}", uid);
                return Triple.of(false, "120314", "该用户套餐已冻结，将影响云豆回收，请联系启用后操作");
            }
            
            if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
                return Triple.of(true, null, null);
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("check User Enable Exit! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
            }
            
            // 判断滞纳金
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("check User Enable Exit! user exist battery service fee,uid={}", userInfo.getUid());
                return Triple.of(false, "120315", "该用户未缴纳滞纳金，将影响云豆回收，请联系缴纳后操作");
            }
        } else {
            log.warn("check User Enable Exit! userBatteryMemberCard is null,uid={}", uid);
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserInfo(EnterpriseChannelUserQuery query) {
        if (Objects.nonNull(query.getId())) {
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.queryById(query.getId());
            if (Objects.isNull(channelUser)) {
                log.warn("add user to enterprise failed. current user is enterprise director. uid = {}, enterprise director uid ", query.getUid(), SecurityUtils.getUid());
                return Triple.of(false, "120307", "二维码已失效，请刷新页面后操作");
            }
            query.setEnterpriseId(channelUser.getEnterpriseId());
        }
        
        // 0. 添加的骑手不能是企业站长
        EnterpriseInfo enterpriseData = enterpriseInfoService.selectByUid(query.getUid());
        if (Objects.nonNull(enterpriseData)) {
            log.warn("add user to enterprise failed. current user is enterprise director. uid = {}, enterprise director uid ", query.getUid(), SecurityUtils.getUid());
            return Triple.of(false, "300062", "待添加用户为企业负责人，无法添加");
        }
        
        // 1. 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("add user to enterprise failed. Not found user. uid = {} ", query.getUid());
            return Triple.of(false, "300079", "未查询到该用户，请确保用户已登录小程序并完成实名认证");
        }
        
        // 2. 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("add user to enterprise failed. User is unUsable. uid = {} ", query.getUid());
            return Triple.of(false, "300080", "用户已被禁用");
        }
        
        // 3. 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("add user to enterprise failed. User not auth. uid = {}", query.getUid());
            return Triple.of(false, "300081", "该用户尚未完成实名认证，请确保用户完成实名认证");
        }
        
        //根据当前企业ID查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        
        // 判断骑手和企业是否在统一租户内
        if (Objects.isNull(enterpriseInfo)) {
            log.warn("add user to enterprise failed. not find enterprise. uid = {}", query.getUid());
            return Triple.of(false, "120212", "商户不存在");
        }
        
        if (!Objects.equals(enterpriseInfo.getTenantId(), userInfo.getTenantId())) {
            log.warn("add user to enterprise failed. not find user. uid = {}", query.getUid());
            return Triple.of(false, "120312", "骑手不存在");
        }
        
        query.setEnterpriseName(enterpriseInfo.getName());
        query.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        query.setTenantId(Long.valueOf(enterpriseInfo.getTenantId()));
        
        return Triple.of(true, "", enterpriseInfo);
    }
    
    @Slave
    @Override
    public ElectricityUserBatteryVo queryBatteryByUid(Long uid) {
        Triple<Boolean, String, Object> batteryTriple = batteryService.queryInfoByUid(uid, BatteryInfoQuery.NEED);
        ElectricityUserBatteryVo userBatteryVo = new ElectricityUserBatteryVo();
        if (!batteryTriple.getLeft()) {
            log.error("query battery for enterprise channel user error, uid = {}", uid);
            throw new BizException(batteryTriple.getMiddle(), (String) batteryTriple.getRight());
        }
        
        if (Objects.nonNull(batteryTriple.getRight())) {
            userBatteryVo = (ElectricityUserBatteryVo) batteryTriple.getRight();
        }
        
        //查询换电时间及柜机信息
        ElectricityCabinetOrder cabinetOrder = electricityCabinetOrderService.selectLatestByUidV2(uid);
        if (Objects.isNull(cabinetOrder)) {
            ElectricityCabinetOrderHistory cabinetOrderHistory = electricityCabinetOrderHistoryService.selectLatestByUidV2(uid);
            if (Objects.nonNull(cabinetOrderHistory)){
                cabinetOrder = BeanUtil.copyProperties(cabinetOrderHistory, ElectricityCabinetOrder.class);
            }
        }
       
        if (Objects.nonNull(cabinetOrder)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(cabinetOrder.getElectricityCabinetId());
            userBatteryVo.setElectricityCabinetId(electricityCabinet.getId());
            userBatteryVo.setElectricityCabinetName(electricityCabinet.getName());
            userBatteryVo.setBatteryExchangeTime(cabinetOrder.getUpdateTime());
        }
        
        return userBatteryVo;
    }
    
    public Triple<Boolean, String, Object> verifyUserInfo(EnterpriseChannelUserQuery query) {
        
        //检查当前用户是否可用
        
        // 0. 添加的骑手不能是企业站长
        EnterpriseInfo enterpriseData = enterpriseInfoService.selectByUid(query.getUid());
        if (Objects.nonNull(enterpriseData)) {
            log.warn("add user to enterprise failed. current user is enterprise director. uid = {}, enterprise director uid ", query.getUid(), SecurityUtils.getUid());
            return Triple.of(false, "300062", "待添加用户为企业负责人，无法添加");
        }
        
        // 1. 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("add user to enterprise failed. Not found user. uid = {} ", query.getUid());
            return Triple.of(false, "300079", "未查询到该用户，请确保用户已登录小程序并完成实名认证");
        }
        
        // 2. 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("add user to enterprise failed. User is unUsable. uid = {} ", query.getUid());
            return Triple.of(false, "300080", "用户已被禁用");
        }
        
        // 3. 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("add user to enterprise failed. User not auth. uid = {}", query.getUid());
            return Triple.of(false, "300081", "该用户尚未完成实名认证，请确保用户完成实名认证");
        }
        
        //根据当前企业ID查询企业信息
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        
        // 4. 检查当前用户是否隶属于企业渠道所属加盟商,新用户暂无加盟商
        /*if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userInfo.getFranchiseeId().equals(enterpriseInfo.getFranchiseeId())) {
            log.error("add user to enterprise failed. add new user's franchiseeId = {}. enterprise franchiseeId = {}", userInfo.getFranchiseeId(),
                    enterpriseInfo.getFranchiseeId());
            return Triple.of(false, "300036", "所属机构不匹配");
        }*/
        
        // 5. 检查用户是否购买过线上套餐(包含换电, 租车, 车电一体套餐)
        if (userInfo.getPayCount() > 0) {
            log.warn("Exist package pay count for current user, uid = {}", userInfo.getUid());
            return Triple.of(false, "300060", "该骑手已是平台用户，无法再次添加");
        }
        
        // 6. 当前用户不能属于其他企业
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(uid);
        if (Objects.nonNull(enterpriseChannelUser)) {
            log.warn("The user already belongs to another enterprise, enterprise id = {}", enterpriseChannelUser.getEnterpriseId());
            return Triple.of(false, "300061", "当前用户已加入其他企业, 无法重复添加");
        }
        
        //7. 骑手手机号不能重复添加
        if (Objects.nonNull(query.getPhone())) {
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectChannelUserByPhone(query.getPhone(), null);
            if (Objects.nonNull(channelUser)) {
                log.warn("The user already used in current enterprise, enterprise id = {}, phone = {}", channelUser.getEnterpriseId(), query.getPhone());
                return Triple.of(false, "300078", "当前用户手机号已存在, 无法重复添加");
            }
        }
        
        return Triple.of(true, "", enterpriseInfo);
    }
    
}
