package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserCheckVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 14:14
 */

@Slf4j
@Service("enterpriseChannelUserService")
public class EnterpriseChannelUserServiceImpl implements EnterpriseChannelUserService {
    @Resource
    private EnterpriseChannelUserMapper enterpriseChannelUserMapper;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    @Resource
    TenantService tenantService;
    @Resource
    ElectricityBatteryService batteryService;
    @Resource
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Resource
    ElectricityCabinetService electricityCabinetService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseChannelUserQuery query) {
        log.info("add new user by enterprise start, channel user = {}", JsonUtil.toJson(query));
        //检查当前用户是否可用
        Triple<Boolean, String, Object> result = verifyUserInfo(query);
        if(Boolean.FALSE.equals(result.getLeft())){
            return Triple.of(false, result.getMiddle(), result.getRight());
        }
    
        EnterpriseInfo enterpriseInfo = (EnterpriseInfo) result.getRight();
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        BeanUtil.copyProperties(query, enterpriseChannelUser);
        enterpriseChannelUser.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
        enterpriseChannelUser.setInviterId(SecurityUtils.getUid());
        enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
        
        enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);
        
        log.info("add new user by enterprise end, enterprise channel user = {}", enterpriseChannelUser.getId());
        
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
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
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
        if(Objects.isNull(enterpriseInfo)){
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
        }else{
            EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
            enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
            enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
            //默认设置骑手不自主续费
            enterpriseChannelUser.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode());
            enterpriseChannelUser.setFranchiseeId(enterpriseInfo.getFranchiseeId());
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
        
        Triple<Boolean, String, Object> result = verifyUserInfo(query);
        if (Boolean.FALSE.equals(result.getLeft())) {
            return result;
        }
        
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setId(query.getId());
        enterpriseChannelUser.setUid(query.getUid());
        enterpriseChannelUser.setRenewalStatus(query.getRenewalStatus());
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());
        
        enterpriseChannelUserMapper.update(enterpriseChannelUser);
        
        return Triple.of(true, "", enterpriseChannelUser);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> checkUserExist(Long id, Long uid) {
       /* if (!ObjectUtils.allNotNull(id, uid)) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }*/
        EnterpriseChannelUserCheckVO enterpriseChannelUserVO = new EnterpriseChannelUserCheckVO();
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectChannelUserByIdAndUid(id, uid);
        if (Objects.isNull(enterpriseChannelUser) || Objects.isNull(enterpriseChannelUser.getUid())) {
            enterpriseChannelUserVO.setIsExist(Boolean.FALSE);
            return Triple.of(true, "300062", enterpriseChannelUserVO);
        }
        enterpriseChannelUserVO.setIsExist(Boolean.TRUE);
        enterpriseChannelUserVO.setUid(enterpriseChannelUser.getUid());
        
        return Triple.of(true, "", enterpriseChannelUserVO);
    }
    
    @Slave
    @Override
    public EnterpriseChannelUserVO selectUserByEnterpriseIdAndUid(Long enterpriseId, Long uid) {
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectChannelUserByEnterpriseIdAndUid(enterpriseId, uid);
        EnterpriseChannelUserVO enterpriseChannelUserVO = new EnterpriseChannelUserVO();
        if(Objects.nonNull(enterpriseChannelUser)){
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
        if(Objects.nonNull(enterpriseChannelUser)){
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
     * @param enterpriseChannelUserQuery
     * @return
     */
    @Slave
    @Override
    public Boolean checkUserRenewalStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
    
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(enterpriseChannelUserQuery.getUid());
        if(Objects.nonNull(enterpriseChannelUser) && RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())){
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * 根据骑手UID，检查当前用户是否为自主续费状态， FALSE-企业代付， TRUE-自主续费
     * @param uid
     * @return
     */
    @Override
    public Boolean checkRenewalStatusByUid(Long uid) {
    
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectByUid(uid);
        if(Objects.nonNull(enterpriseChannelUser) && RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())){
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
    public Integer updateCloudBeanStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setId(enterpriseChannelUserQuery.getId());
        enterpriseChannelUser.setCloudBeanStatus(enterpriseChannelUserQuery.getCloudBeanStatus());
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
    
    @Override
    public ElectricityUserBatteryVo queryBatteryByUid(Long uid) {
        Triple<Boolean, String, Object> batteryTriple = batteryService.queryInfoByUid(uid, BatteryInfoQuery.NEED);
        if (!batteryTriple.getLeft()) {
            log.error("query battery for enterprise channel user error, uid = {}", uid);
            throw new BizException(batteryTriple.getMiddle(), (String) batteryTriple.getRight());
        }
        ElectricityUserBatteryVo userBatteryVo = (ElectricityUserBatteryVo) batteryTriple.getRight();
        
        //查询换电时间及柜机信息
        //Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityCabinetOrderVO electricityCabinetOrderVO = electricityCabinetOrderService.selectLatestOrderAndCabinetInfo(uid);
        if(Objects.nonNull(electricityCabinetOrderVO)){
            //ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
            userBatteryVo.setElectricityCabinetId(electricityCabinetOrderVO.getElectricityCabinetId());
            userBatteryVo.setElectricityCabinetName(electricityCabinetOrderVO.getElectricityCabinetName());
            userBatteryVo.setBatteryExchangeTime(electricityCabinetOrderVO.getCreateTime());
        }
        
        return userBatteryVo;
    }
    
    public Triple<Boolean, String, Object> verifyUserInfo(EnterpriseChannelUserQuery query) {
        
        //检查当前用户是否可用
        
        // 0. 添加的骑手不能是企业站长
        EnterpriseInfo enterpriseData = enterpriseInfoService.selectByUid(query.getUid());
        if(Objects.nonNull(enterpriseData)){
            log.error("add user to enterprise failed. current user is enterprise director. uid = {}, enterprise director uid ", query.getUid(), SecurityUtils.getUid());
            return Triple.of(false, "300062", "待添加用户为企业负责人，无法添加");
        }
        
        // 1. 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("add user to enterprise failed. Not found user. uid = {} ", query.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        // 2. 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("add user to enterprise failed. User is unUsable. uid = {} ", query.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 3. 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("add user to enterprise failed. User not auth. uid = {}", query.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "用户尚未实名认证");
        }
        
        //根据当前企业ID查询企业信息
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        
        // 4. 检查当前用户是否隶属于企业渠道所属加盟商
        /*if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userInfo.getFranchiseeId().equals(enterpriseInfo.getFranchiseeId())) {
            log.error("add user to enterprise failed. add new user's franchiseeId = {}. enterprise franchiseeId = {}", userInfo.getFranchiseeId(),
                    enterpriseInfo.getFranchiseeId());
            return Triple.of(false, "300036", "所属机构不匹配");
        }*/
        
        // 5. 检查用户是否购买过线上套餐(包含换电, 租车, 车电一体套餐)
        if (userInfo.getPayCount() > 0) {
            log.info("Exist package pay count for current user, uid = {}", userInfo.getUid());
            return Triple.of(false, "300060", "当前用户已购买过线上套餐");
        }
        
        // 6. 当前用户不能属于其他企业
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectUsedChannelUser(uid, null);
        if (Objects.nonNull(enterpriseChannelUser)) {
            log.info("The user already belongs to another enterprise, enterprise id = {}", enterpriseChannelUser.getEnterpriseId());
            return Triple.of(false, "300061", "当前用户已加入其他企业, 无法重复添加");
        }
        
        //7. 骑手手机号不能重复添加
        if(Objects.nonNull(query.getPhone())){
            EnterpriseChannelUser channelUser = enterpriseChannelUserMapper.selectChannelUserByPhone(query.getPhone());
            if(Objects.nonNull(channelUser)){
                log.info("The user already used in current enterprise, enterprise id = {}, phone = {}", channelUser.getEnterpriseId(), query.getPhone());
                return Triple.of(false, "300061", "当前用户手机号已存在, 无法重复添加");
            }
        }
        
        return Triple.of(true, "", enterpriseInfo);
    }
    
}
