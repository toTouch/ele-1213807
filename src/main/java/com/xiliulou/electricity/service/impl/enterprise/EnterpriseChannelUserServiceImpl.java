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
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserHistory;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserHistoryMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.request.enterprise.EnterpriseUserExitCheckRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.UserInfoSearchVo;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserCheckVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            return Triple.of(false, "ELECTRICITY.0001", "未查询到该用户，请确保用户已登录小程序并完成实名认证");
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
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
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
            enterpriseChannelUser.setInviterId(SecurityUtils.getUid());
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
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
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
            if(Objects.isNull(channelUserEntity)){
                log.error("query enterprise channel record failed after QR scan,  uid = {}, channel user record id", uid, channelUserId);
                return Triple.of(false, "300082", "企业信息不存在, 添加失败");
            }
            
            if(Objects.nonNull(channelUserEntity.getUid())){
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
        if(Objects.isNull(enterpriseChannelUser)){
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
        if (!ObjectUtils.allNotNull(query.getUid(), query.getId(), query.getRenewalStatus())) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
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
                return result;
            }
            
            // 非企业用户
            Triple<Boolean, String, Object> result2 = doNoEnterpriseUser(query, channelUser, uid, channelUserId, channelUserEntity, enterpriseChannelUser);
            if (Boolean.FALSE.equals(result2.getLeft())) {
                return result;
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
     * @param query
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> addUserNew(EnterpriseChannelUserQuery query) {
        log.info("add new user by enterprise start, channel user = {}", JsonUtil.toJson(query));
    
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY + query.getUid(), "1", 3 * 1000L, false)) {
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
            Triple<Boolean, String, Object> result1 = doEnterpriseUserByPhone(query, channelUser, uid, channelUserEntity, enterpriseChannelUser);
            if (Boolean.FALSE.equals(result1.getLeft())) {
                return result;
            }
    
            // 非企业用户
            Triple<Boolean, String, Object> result2 = doNoEnterpriseUser(query, channelUser, uid, channelUserId, channelUserEntity, enterpriseChannelUser);
            if (Boolean.FALSE.equals(result2.getLeft())) {
                return result;
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
    
    /**
     * 1.1 若产生滞纳金：不可开启，提示“该用户未缴纳滞纳金，将影响云豆回收，请联系缴纳后操作”；
     * 1.2 若套餐冻结/审核中，不可开启，提示“该用户套餐已冻结，将影响云豆回收，请联系启用后操作” / "该用户已申请套餐冻结，将影响云豆回收，请联系解除后操作";
     * 1.3 若未退还电池，不可开启，提示“该用户未退还电池，将影响云豆回收，请联系退还后操作”
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> channelUserExitCheck(EnterpriseUserExitCheckRequest request) {
        Long uid = request.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("enterprise channel switch user WARN! userBatteryMemberCard is null,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }
    
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("enterprise channel switch user WARN! user stop member card review,uid={}", uid);
            return Triple.of(false, "100211", "需您提前解除套餐冻结申请");
        }
    
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("enterprise channel switch user WARN! member card is disable userId={}", uid);
            return Triple.of(false, "ELECTRICITY.100004", "需您提前启用套餐冻结服务");
        }
    
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("enterprise channel switch user WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
    
        // 判断滞纳金
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("enterprise channel switch user WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "需您提前缴纳滞纳金");
        }
    
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("enterprise channel switch user WARN! user rent battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0045", "需您提前退还租赁的电池");
        }
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 骑手自主续费
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> channelUserExit(EnterpriseUserExitCheckRequest request) {
        Triple<Boolean, String, Object> triple = enterpriseInfoService.recycleCloudBean(request.getUid());
        if (!triple.getLeft()) {
            return triple;
        }
        
        // 增加退出记录
        EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectByUid(request.getUid());
        // 添加操作记录, 新记录和原站点的记录
        EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
        BeanUtils.copyProperties(channelUser, history);
        history.setExitTime(System.currentTimeMillis());
        history.setType(EnterpriseChannelUserHistory.EXIT);
    
        channelUserHistoryMapper.insertOne(history);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> channelUserExitCheckAll(EnterpriseUserExitCheckRequest request) {
        if (!(Objects.equals(request.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN) || Objects.equals(request.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN))) {
            log.error("channel user exit check renewal error uid={}, renewalStatus={}", request.getUid(), request.getRenewalStatus());
            return Triple.of(false, "300834", "自主续费状态错误");
        }
        
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(request.getUid());
        if (Objects.isNull(enterpriseInfoVO)) {
            log.error("channel user exit check  enterprise not exists, uid={}", request.getUid());
            return Triple.of(false, "300082", "企业信息不存在, 添加失败");
        }
        
        
        return null;
    }
    
    private Triple<Boolean, String, Object> doEnterpriseUserByPhone(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid, EnterpriseChannelUser channelUserEntity, EnterpriseChannelUser enterpriseChannelUser) {
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(), query.getEnterpriseId())) {
            log.info("enterprise channel user by phone repeat, enterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getUid());
            return Triple.of(false, "300830", "已是该渠道用户，无需重复添加");
        }
    
        if (Objects.nonNull(channelUser)  && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && !Objects.equals(channelUser.getEnterpriseId(), query.getEnterpriseId())) {
            log.info("enterprise channel user by phone station diff, oldEnterpriseId={}, newEnterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getEnterpriseId(), query.getUid());
            return Triple.of(false, "300832", "切换渠道卡片，进行面对面添加");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> doNoEnterpriseUser(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid, Long channelUserId, EnterpriseChannelUser channelUserEntity,
            EnterpriseChannelUser enterpriseChannelUser) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        EleDepositOrder depositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        boolean isMember = Objects.equals(depositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
      
        // 判断用户是否为会员用户
        if (isMember && (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_REFUNDING))) {
            // 检测企业代付开关是否开启
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
            if (Objects.isNull(enterpriseInfo.getPurchaseAuthority()) || Objects.equals(enterpriseInfo.getPurchaseAuthority(), 0)) {
                log.error("enterprise user is platform user not join, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                return Triple.of(false, "300082", "已是平台会员，无法加入企业渠道");
            }
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO) && Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryLastPayDepositTimeByUid(query.getUid(), null, null, null);
            Integer tenantId = TenantContextHolder.getTenantId();
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            // 查询退押订单
            EleRefundOrder refundOrder = refundOrderService.queryLastByUid(query.getUid());
            if (Objects.nonNull(eleDepositOrder) && Objects.equals(eleDepositOrder.getOrderType(), EleDepositOrder.ORDER_TYPE_COMMON)
                    && Objects.nonNull(electricityConfig) && Objects.nonNull(electricityConfig.getChannelTimeLimit()) && Objects.nonNull(refundOrder)) {
                long l = DateUtils.diffDayV2(refundOrder.getUpdateTime(), System.currentTimeMillis());
                if (l <= electricityConfig.getChannelTimeLimit()) {
                    log.error("enterprise user is channel time limit user not join, enterpriseId={}, uid={}", query.getEnterpriseId(), query.getUid());
                    return Triple.of(false, "300082", "渠道保护期内，暂无法加入企业渠道");
                }
            }
        }
        
        if (Objects.isNull(channelUser)) {
            //检查是否已经有用户被关联至当前企业
            if(Objects.isNull(channelUserEntity)){
                log.error("query enterprise channel record failed after QR scan,  uid = {}, channel user record id", uid, channelUserId);
                return Triple.of(false, "300082", "企业信息不存在, 添加失败");
            }
        
            if(Objects.nonNull(channelUserEntity.getUid())){
                log.error("user already exist after QR scan,  uid = {}, channel user record id", uid, channelUserId);
                return Triple.of(false, "300083", "已添加其他用户, 请重新扫码");
            }
            log.info("enterprise channel add new user");
            enterpriseChannelUser.setId(channelUserId);
            enterpriseChannelUser.setUid(uid);
            enterpriseChannelUser.setRenewalStatus(query.getRenewalStatus());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
        
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
        
            // 添加用户加盟商信息
            EnterpriseChannelUser channelUser1 = enterpriseChannelUserMapper.selectByUid(uid);
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(channelUser1.getFranchiseeId());
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
    
            // 添加操作记录, 新记录和原站点的记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            BeanUtils.copyProperties(enterpriseChannelUser, history);
            history.setExitTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
            
            channelUserHistoryMapper.insertOne(history);
        }
    
        // 老企业用户重新加入
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_OPEN)) {
            log.info("enterprise channel update user");
            // 修改新站点的信息
            enterpriseChannelUser.setId(channelUser.getId());
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(SecurityUtils.getUid());
            enterpriseChannelUser.setUid(query.getUid());
            enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
            enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
            enterpriseChannelUserMapper.update(enterpriseChannelUser);
        
            // 添加操作记录, 新记录
            EnterpriseChannelUserHistory history = new EnterpriseChannelUserHistory();
            BeanUtils.copyProperties(enterpriseChannelUser, history);
            history.setExitTime(System.currentTimeMillis());
            history.setType(EnterpriseChannelUserHistory.JOIN);
    
            channelUserHistoryMapper.insertOne(history);
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> doEnterpriseUser(EnterpriseChannelUserQuery query, EnterpriseChannelUser channelUser, Long uid,
            EnterpriseChannelUser channelUserEntity, EnterpriseChannelUser enterpriseChannelUser) {
        if (Objects.nonNull(channelUser) && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(), query.getEnterpriseId())) {
            log.info("enterprise channel user by scan repeat, enterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getUid());
            return Triple.of(false, "300830", "已是该渠道用户，无需重复添加");
        }
        
        if (Objects.nonNull(channelUser)  && Objects.equals(channelUser.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE) && Objects.equals(channelUser.getEnterpriseId(), query.getEnterpriseId())) {
            log.info("enterprise channel switch user");
            // 切换站点
            // 判断两个加盟商是否一致
            if (Objects.equals(channelUser.getFranchiseeId(), channelUserEntity.getFranchiseeId())) {
                log.info("enterprise channel switch user station diff, oldEnterpriseId={}, newEnterpriseId={}, uid={}", channelUser.getEnterpriseId(), query.getEnterpriseId(), query.getUid());
                return Triple.of(false, "300831", "切换站点的加盟商必须一致");
            }
        
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("enterprise channel switch user WARN! userBatteryMemberCard is null,uid={}", uid);
                return Triple.of(false, "100247", "用户信息不存在");
            }
        
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("enterprise channel switch user WARN! user stop member card review,uid={}", uid);
                return Triple.of(false, "100211", "需您提前解除套餐冻结申请");
            }
        
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("enterprise channel switch user WARN! member card is disable userId={}", query.getUid());
                return Triple.of(false, "ELECTRICITY.100004", "需您提前启用套餐冻结服务");
            }
        
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("enterprise channel switch user WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
            }
        
            // 判断滞纳金
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                    .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("enterprise channel switch user WARN! user exist battery service fee,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.100000", "需您提前缴纳滞纳金");
            }
        
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("enterprise channel switch user WARN! user rent battery,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0045", "需您提前退还租赁的电池");
            }
        
            // 回收云豆
            Triple<Boolean, String, Object> triple = enterpriseInfoService.recycleCloudBean(query.getUid());
            if (!triple.getLeft()) {
                return triple;
            }
    
            // 修改新站点的信息
            enterpriseChannelUser.setId(channelUser.getId());
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
            enterpriseChannelUser.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
            enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
            enterpriseChannelUser.setInviterId(SecurityUtils.getUid());
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
            channelUserList.add(history);
    
            EnterpriseChannelUserHistory channelUserHistory = new EnterpriseChannelUserHistory();
            BeanUtils.copyProperties(enterpriseChannelUser, channelUserHistory);
            channelUserHistory.setJoinTime(System.currentTimeMillis());
            channelUserHistory.setType(EnterpriseChannelUserHistory.JOIN);
            channelUserList.add(channelUserHistory);
            channelUserHistoryMapper.batchInsert(channelUserList);
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserInfo(EnterpriseChannelUserQuery query) {
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
    
        return Triple.of(true, "", enterpriseInfo);
    }
    
    @Override
    public ElectricityUserBatteryVo queryBatteryByUid(Long uid) {
        Triple<Boolean, String, Object> batteryTriple = batteryService.queryInfoByUid(uid, BatteryInfoQuery.NEED);
        ElectricityUserBatteryVo userBatteryVo = new ElectricityUserBatteryVo();
        if (!batteryTriple.getLeft()) {
            log.error("query battery for enterprise channel user error, uid = {}", uid);
            throw new BizException(batteryTriple.getMiddle(), (String) batteryTriple.getRight());
        }
        
        if(Objects.nonNull(batteryTriple.getRight())){
            userBatteryVo = (ElectricityUserBatteryVo) batteryTriple.getRight();
        }
        
        //查询换电时间及柜机信息
        //Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityCabinetOrderVO electricityCabinetOrderVO = electricityCabinetOrderService.selectLatestOrderAndCabinetInfo(uid);
        if (Objects.nonNull(electricityCabinetOrderVO)) {
            //ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
            userBatteryVo.setElectricityCabinetId(electricityCabinetOrderVO.getElectricityCabinetId());
            userBatteryVo.setElectricityCabinetName(electricityCabinetOrderVO.getElectricityCabinetName());
            userBatteryVo.setBatteryExchangeTime(electricityCabinetOrderVO.getUpdateTime());
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
