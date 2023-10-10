package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import com.xiliulou.electricity.entity.enterprise.UserBehaviorRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseInfoMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.enterprise.UserBehaviorRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CloudBeanGeneralViewVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoPackageVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.vo.enterprise.UserCloudBeanDetailVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 企业用户信息表(EnterpriseInfo)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
@Service("enterpriseInfoService")
@Slf4j
public class EnterpriseInfoServiceImpl implements EnterpriseInfoService {
    
    @Resource
    private EnterpriseInfoMapper enterpriseInfoMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private EnterprisePackageService enterprisePackageService;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    private UserOauthBindService userOauthBindService;
    
    @Autowired
    private ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Autowired
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    private UserBehaviorRecordService userBehaviorRecordService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    private InsuranceOrderService insuranceOrderService;
    
    @Autowired
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    private EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseInfo queryByIdFromDB(Long id) {
        return this.enterpriseInfoMapper.queryById(id);
    }
    
    @Override
    public EnterpriseInfo queryByIdFromCache(Long id) {
        
        EnterpriseInfo cacheEnterpriseInfo = redisService.getWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, EnterpriseInfo.class);
        if (Objects.nonNull(cacheEnterpriseInfo)) {
            return cacheEnterpriseInfo;
        }
        
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(id);
        if (Objects.isNull(enterpriseInfo)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, enterpriseInfo);
        
        return enterpriseInfo;
    }
    
    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseInfo enterpriseInfo) {
        int update = this.enterpriseInfoMapper.update(enterpriseInfo);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseInfo.getId());
            return null;
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
        int delete = this.enterpriseInfoMapper.deleteById(id);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + id);
            return null;
        });
        
        return delete;
    }
    
    @Slave
    @Override
    public List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query) {
        List<EnterpriseInfoPackageVO> list = this.enterpriseInfoMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        return list.stream().map(item -> {
            EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
            BeanUtils.copyProperties(item, enterpriseInfoVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseInfoVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseInfoVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
            enterpriseInfoVO.setPhone(Objects.isNull(userInfo) ? "" : userInfo.getPhone());
            
            Pair<List<Long>, List<String>> listPair = getMembercardNames(item.getId());
            enterpriseInfoVO.setMemcardName(listPair.getRight());
            enterpriseInfoVO.setPackageIds(listPair.getLeft());
            
            return enterpriseInfoVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer selectByPageCount(EnterpriseInfoQuery query) {
        return this.enterpriseInfoMapper.selectByPageCount(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> rechargeForUser(UserCloudBeanRechargeQuery query, HttpServletRequest request) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CLOUD BEAN RECHARGE ERROR! not found user,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CLOUD_BEAN_RECHARGE_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("CLOUD BEAN RECHARGE ERROR! user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("CLOUD BEAN RECHARGE ERROR! user not auth,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            EnterpriseInfo enterpriseInfo = this.selectByUid(userInfo.getUid());
            if (Objects.isNull(enterpriseInfo)) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found enterpriseInfo,uid={}", userInfo.getUid());
                return Triple.of(false, "", "企业配置信息不存在");
            }
            
            if (query.getTotalBeanAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                log.error("CLOUD BEAN RECHARGE ERROR!illegal totalBeanAmount,uid={},totalBeanAmount={}", userInfo.getUid(), query.getTotalBeanAmount());
                return Triple.of(false, "", "支付金额不合法");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(electricityPayParams)) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found pay params,uid={}", userInfo.getUid());
                return Triple.of(false, "", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(userInfo.getUid(), TenantContextHolder.getTenantId());
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found userOauthBind,uid={}", userInfo.getUid());
                return Triple.of(false, "", "未找到用户的第三方授权信息!");
            }
            
            //生成充值订单
            EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
            enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
            enterpriseCloudBeanOrder.setUid(userInfo.getUid());
            enterpriseCloudBeanOrder.setOperateUid(userInfo.getUid());
            enterpriseCloudBeanOrder.setPayAmount(query.getTotalBeanAmount());
            enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
            enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_INIT);
            enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.ONLINE_PAYMENT);
            enterpriseCloudBeanOrder.setType(EnterpriseCloudBeanOrder.TYPE_USER_RECHARGE);
            enterpriseCloudBeanOrder.setRemark("");
            enterpriseCloudBeanOrder.setBeanAmount(query.getTotalBeanAmount());
            enterpriseCloudBeanOrder.setFranchiseeId(userInfo.getFranchiseeId());
            enterpriseCloudBeanOrder.setTenantId(userInfo.getTenantId());
            enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
            
            CommonPayOrder commonPayOrder = CommonPayOrder.builder().orderId(enterpriseCloudBeanOrder.getOrderId()).uid(userInfo.getUid()).payAmount(query.getTotalBeanAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_CLOUD_BEAN_RECHARGE).attach(ElectricityTradeOrder.ATTACH_CLOUD_BEAN_RECHARGE).description("云豆充值")
                    .tenantId(TenantContextHolder.getTenantId()).build();
            
            WechatJsapiOrderResultDTO resultDTO = electricityTradeOrderService
                    .commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (Exception e) {
            log.error("CLOUD BEAN RECHARGE ERROR! recharge fail,uid={}", userInfo.getUid(), e);
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_CLOUD_BEAN_RECHARGE_LOCK_KEY + SecurityUtils.getUid());
        }
        
        return Triple.of(false, "", "充值失败");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery) {
        if (CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
            return Triple.of(false, "", "参数不合法");
        }
        
        EnterpriseInfo enterpriseInfoOld = this.selectByUid(enterpriseInfoQuery.getUid());
        if (Objects.nonNull(enterpriseInfoOld)) {
            return Triple.of(false, "", "用户已存在");
        }
        
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfo);
        enterpriseInfo.setBusinessId(Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + RandomUtil.randomInt(1000, 9999)));
        enterpriseInfo.setRecoveryMode(EnterpriseInfo.RECOVERY_MODE_RETURN);
        enterpriseInfo.setTotalBeanAmount(BigDecimal.ZERO);
        enterpriseInfo.setTenantId(TenantContextHolder.getTenantId());
        enterpriseInfo.setCreateTime(System.currentTimeMillis());
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        this.enterpriseInfoMapper.insert(enterpriseInfo);
        
        List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
            EnterprisePackage enterprisePackage = new EnterprisePackage();
            enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
            enterprisePackage.setPackageId(item);
            enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
            enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
            enterprisePackage.setCreateTime(System.currentTimeMillis());
            enterprisePackage.setUpdateTime(System.currentTimeMillis());
            return enterprisePackage;
        }).collect(Collectors.toList());
        
        enterprisePackageService.batchInsert(packageList);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "", "企业配置不存在");
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfoUpdate);
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        this.deleteById(id);
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> rechargeForAdmin(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(enterpriseCloudBeanRechargeQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "", "企业配置不存在");
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        //云豆订单
        EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
        enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
        enterpriseCloudBeanOrder.setUid(enterpriseInfo.getUid());
        enterpriseCloudBeanOrder.setOperateUid(SecurityUtils.getUid());
        enterpriseCloudBeanOrder
                .setPayAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
        enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
        enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.OFFLINE_PAYMENT);
        enterpriseCloudBeanOrder.setType(enterpriseCloudBeanRechargeQuery.getType());
        enterpriseCloudBeanOrder
                .setBeanAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
        enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setRemark(enterpriseCloudBeanRechargeQuery.getRemark());
        enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
        
        //云豆记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(enterpriseInfo.getUid());
        cloudBeanUseRecord.setType(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount().compareTo(BigDecimal.valueOf(0)) > 0 ? CloudBeanUseRecord.TYPE_ADMIN_RECHARGE
                : CloudBeanUseRecord.TYPE_ADMIN_DEDUCT);
        cloudBeanUseRecord
                .setBeanAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfoUpdate.getTotalBeanAmount());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public EnterpriseInfo selectByUid(Long uid) {
        return this.enterpriseInfoMapper.selectByUid(uid);
    }
    
    @Slave
    @Override
    public EnterpriseInfoVO selectEnterpriseInfoByUid(Long uid) {
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectByUid(uid);
        EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
        BeanUtil.copyProperties(enterpriseInfo, enterpriseInfoVO);
        
        return enterpriseInfoVO;
    }
    
    @Slave
    @Override
    public UserCloudBeanDetailVO cloudBeanDetail() {
        EnterpriseInfo enterpriseInfo = this.selectByUid(SecurityUtils.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("USER CLOUD BEAN DETAIL ERROR!not found enterpriseInfo,uid={}", SecurityUtils.getUid());
            return null;
        }
        
        UserCloudBeanDetailVO userCloudBeanDetailVO = new UserCloudBeanDetailVO();
        userCloudBeanDetailVO.setTotalCloudBean(enterpriseInfo.getTotalBeanAmount());
        
        //已分配云豆数
        Double distributableCloudBean = cloudBeanUseRecordService.selectCloudBeanByUidAndType(SecurityUtils.getUid(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
        userCloudBeanDetailVO.setDistributableCloudBean(Objects.isNull(distributableCloudBean) ? NumberConstant.ZERO_D : distributableCloudBean);
        
        //已回收云豆数
        Double recoveredCloudBean = cloudBeanUseRecordService.selectCloudBeanByUidAndType(SecurityUtils.getUid(), CloudBeanUseRecord.TYPE_RECYCLE);
        userCloudBeanDetailVO.setRecoveredCloudBean(Objects.isNull(recoveredCloudBean) ? NumberConstant.ZERO_D : recoveredCloudBean);
        
        //可回收云豆数  TODO
        
        return userCloudBeanDetailVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> recycleCloudBean(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("RECYCLE WARN! not found user,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("RECYCLE WARN! user is unUsable,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("RECYCLE WARN! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("RECYCLE WARN! user not pay deposit,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0049", "未缴纳押金");
        }
        
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("RECYCLE WARN! user rent battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0045", "已绑定电池");
        }
        
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("RECYCLE WARN! user illegal,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(enterpriseChannelUser.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("RECYCLE CLOUD BEAN ERROR! not found enterpriseInfo,enterpriseId={},uid={}", enterpriseChannelUser.getEnterpriseId(), uid);
            return Triple.of(false, "ELECTRICITY.0019", "企业信息不存在");
        }
        
        //回收套餐
        Triple<Boolean, String, Object> booleanStringObjectTriple = recycleBatteryMembercard(userInfo, enterpriseInfo);
        if (Boolean.FALSE.equals(booleanStringObjectTriple.getLeft())) {
            return booleanStringObjectTriple;
        }
        
        //回收押金
        recycleBatteryDeposit(userInfo, enterpriseInfo);
        
        //清除用户租退电、购买套餐记录
        userBehaviorRecordService.deleteByUid(userInfo.getUid());
        
        //解绑用户绑定信息
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        //更新用户套餐订单为已失效
        electricityMemberCardOrderService
                .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        
        userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
        
        userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
        if (Objects.nonNull(insuranceUserInfo)) {
            insuranceUserInfoService.deleteById(insuranceUserInfo);
            //更新用户保险订单为已失效
            insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
        }
        
        //退押金解绑用户所属加盟商
        userInfoService.unBindUserFranchiseeId(userInfo.getUid());
        
        //更新用户套餐订单为已失效
        electricityMemberCardOrderService
                .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        
        //删除用户电池套餐资源包
        userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
        
        //删除用户电池型号
        userBatteryTypeService.deleteByUid(userInfo.getUid());
        
        //删除用户电池服务费
        serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> recycleBatteryMembercard(UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        List<UserBehaviorRecord> userBehaviorRecords = userBehaviorRecordService.selectByUid(userInfo.getUid());
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(userBehaviorRecords)) {
            return Triple.of(true, null, BigDecimal.ZERO);
        }
        
        //退电记录时间
        List<Long> returnBatteryRecords = userBehaviorRecords.stream().filter(item -> Objects.equals(item.getType(), UserBehaviorRecord.TYPE_RETURN_BATTERY))
                .sorted(Comparator.comparing(UserBehaviorRecord::getCreateTime)).map(UserBehaviorRecord::getCreateTime).collect(Collectors.toList());
        
        //租电记录时间
        List<Long> rentBatteryRecords = userBehaviorRecords.stream().filter(item -> Objects.equals(item.getType(), UserBehaviorRecord.TYPE_RENT_BATTERY))
                .sorted(Comparator.comparing(UserBehaviorRecord::getCreateTime)).map(UserBehaviorRecord::getCreateTime).collect(Collectors.toList());
        
        //套餐
        List<UserBehaviorRecord> membercardRecords = userBehaviorRecords.stream().filter(item -> Objects.equals(item.getType(), UserBehaviorRecord.TYPE_PAY_MEMBERCARD))
                .sorted(Comparator.comparing(UserBehaviorRecord::getCreateTime)).collect(Collectors.toList());
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(membercardRecords)) {
            log.warn("RECYCLE BATTERY MEMBERCARD WARN! membercardRecords is empty,uid={}", userInfo.getUid());
            return Triple.of(true, null, BigDecimal.ZERO);
        }
        
        //没有租退电记录  套餐全部回收
        if (CollectionUtils.isEmpty(returnBatteryRecords) && CollectionUtils.isEmpty(rentBatteryRecords)) {
            for (UserBehaviorRecord membercardRecord : membercardRecords) {
                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(membercardRecord.getOrderId());
                if (Objects.isNull(electricityMemberCardOrder)) {
                    log.warn("RECYCLE BATTERY MEMBERCARD WARN! electricityMemberCardOrder is null,uid={},orderId={}", userInfo.getUid(), membercardRecord.getOrderId());
                    return Triple.of(false, null, BigDecimal.ZERO);
                }
                
                //保存回收记录
                EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
                enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
                enterpriseCloudBeanOrder.setUid(userInfo.getUid());
                enterpriseCloudBeanOrder.setOperateUid(0L);
                enterpriseCloudBeanOrder.setPayAmount(electricityMemberCardOrder.getPayAmount());
                enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
                enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
                enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.RECYCLE_PAYMENT);
                enterpriseCloudBeanOrder.setType(EnterpriseCloudBeanOrder.TYPE_RECYCLE);
                enterpriseCloudBeanOrder.setBeanAmount(electricityMemberCardOrder.getPayAmount());
                enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
                enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
                enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
                enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
                
                CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                cloudBeanUseRecord.setUid(userInfo.getUid());
                cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount());
                cloudBeanUseRecord.setRemainingBeanAmount(BigDecimal.ZERO);
                cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
                cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
                cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                
                //生成退租订单
                BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert = new BatteryMembercardRefundOrder();
                batteryMembercardRefundOrderInsert.setUid(userInfo.getUid());
                batteryMembercardRefundOrderInsert.setPhone(userInfo.getPhone());
                batteryMembercardRefundOrderInsert.setMid(electricityMemberCardOrder.getMemberCardId());
                batteryMembercardRefundOrderInsert.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_BATTERY_MEMBERCARD, userInfo.getUid()));
                batteryMembercardRefundOrderInsert.setMemberCardOrderNo(electricityMemberCardOrder.getOrderId());
                batteryMembercardRefundOrderInsert.setPayAmount(electricityMemberCardOrder.getPayAmount());
                batteryMembercardRefundOrderInsert.setRefundAmount(electricityMemberCardOrder.getPayAmount());
                batteryMembercardRefundOrderInsert.setPayType(electricityMemberCardOrder.getPayType());
                batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_AUDIT);
                batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
                batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
                batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
                batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
                batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
                batteryMembercardRefundOrderInsert.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                batteryMembercardRefundOrderInsert.setRemainingTime(electricityMemberCardOrder.getValidDays().longValue());
                batteryMembercardRefundOrderService.insert(batteryMembercardRefundOrderInsert);
                
                ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
                electricityMemberCardOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
                electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_AUDIT);
                electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
                batteryMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdate);
            }
            
            return Triple.of(true, null, null);
        }
        
        //租退电记录不一致，数据异常不回收
        if (CollectionUtils.isEmpty(returnBatteryRecords) || CollectionUtils.isEmpty(rentBatteryRecords) || returnBatteryRecords.size() != rentBatteryRecords.size()) {
            log.warn("RECYCLE BATTERY MEMBERCARD WARN! user rent battery data illegal,uid={}", userInfo.getUid());
            return Triple.of(false, null, BigDecimal.ZERO);
        }
        
        //租退电时间转换 K-租电，V-退电
        Map<Long, Long> result = IntStream.range(0, rentBatteryRecords.size()).boxed().collect(Collectors.toMap(rentBatteryRecords::get, returnBatteryRecords::get));
        
        Long membercardStartTime;
        for (UserBehaviorRecord membercardRecord : membercardRecords) {
            //当前套餐可回收金额
            BigDecimal recycleMembercard = BigDecimal.ZERO;
            
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(membercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), membercardRecord.getOrderId());
                return Triple.of(false, "", "订单不存在");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Triple.of(false, "", "套餐不存在");
            }
            
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
                return Triple.of(false, "", "用户信息不存在");
            }
            
            //套餐回收单价
            BigDecimal recyclePrice;
            if (Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit())) {
                recyclePrice = batteryMemberCard.getRentPrice().divide(BigDecimal.valueOf(batteryMemberCard.getValidDays()), 2, RoundingMode.HALF_UP);
            } else {
                recyclePrice = batteryMemberCard.getRentPrice().divide(BigDecimal.valueOf(batteryMemberCard.getValidDays()), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(60).multiply(BigDecimal.valueOf(24)));
            }
            
            membercardStartTime = electricityMemberCardOrder.getCreateTime();
            long membercardEndTime = membercardStartTime + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder);
            
            //当前套餐对应的租退电时间记录
            Map<Long, Long> tempMap = Maps.newHashMap();
            Iterator<Map.Entry<Long, Long>> resultMapIterator = result.entrySet().iterator();
            
            //遍历租退电记录
            while (resultMapIterator.hasNext()) {
                Map.Entry<Long, Long> next = resultMapIterator.next();
                Long key = next.getKey();
                Long value = next.getValue();
                if (DateUtils.hasOverlap(membercardStartTime, membercardEndTime, key, value)) {
                    tempMap.put(key, value);
                }
                
                resultMapIterator.remove();
            }
            
            //可回收时间
            long recycleTime = 0L;
            
            //遍历当前套餐对应的租退电时间记录，计算可回收的时间
            Iterator<Map.Entry<Long, Long>> iterator = tempMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Long> next = iterator.next();
                long rentTime = next.getKey();
                long returnTime = next.getValue();
                
                if (membercardStartTime < rentTime) {
                    recycleTime = recycleTime + (rentTime - membercardStartTime);
                }
                
                if (!iterator.hasNext()) {
                    recycleTime = recycleTime + (membercardEndTime - returnTime);
                }
            }
            
            int recycleDay = (int) Math.ceil(recycleTime / 1000 / 60 / 60 / 24.0);
            
            recycleMembercard = recycleMembercard.add(BigDecimal.valueOf(recycleDay).multiply(recyclePrice));
            
            //保存回收记录
            EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
            enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
            enterpriseCloudBeanOrder.setUid(userInfo.getUid());
            enterpriseCloudBeanOrder.setOperateUid(0L);
            enterpriseCloudBeanOrder.setPayAmount(recycleMembercard);
            enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
            enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
            enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.RECYCLE_PAYMENT);
            enterpriseCloudBeanOrder.setType(EnterpriseCloudBeanOrder.TYPE_RECYCLE);
            enterpriseCloudBeanOrder.setBeanAmount(recycleMembercard);
            enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
            enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
            enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
            
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
            cloudBeanUseRecord.setBeanAmount(recycleMembercard);
            cloudBeanUseRecord.setRemainingBeanAmount(BigDecimal.ZERO);
            cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordService.insert(cloudBeanUseRecord);
            
            //生成退租订单
            BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderInsert.setUid(userInfo.getUid());
            batteryMembercardRefundOrderInsert.setPhone(userInfo.getPhone());
            batteryMembercardRefundOrderInsert.setMid(electricityMemberCardOrder.getMemberCardId());
            batteryMembercardRefundOrderInsert.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_BATTERY_MEMBERCARD, userInfo.getUid()));
            batteryMembercardRefundOrderInsert.setMemberCardOrderNo(electricityMemberCardOrder.getOrderId());
            batteryMembercardRefundOrderInsert.setPayAmount(electricityMemberCardOrder.getPayAmount());
            batteryMembercardRefundOrderInsert.setRefundAmount(recycleMembercard);
            batteryMembercardRefundOrderInsert.setPayType(electricityMemberCardOrder.getPayType());
            batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_SUCCESS);
            batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
            batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
            batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
            
            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NOT_USE)) {
                batteryMembercardRefundOrderInsert.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                batteryMembercardRefundOrderInsert.setRemainingTime(electricityMemberCardOrder.getValidDays().longValue());
            }
            
            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
                batteryMembercardRefundOrderInsert.setRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
                batteryMembercardRefundOrderInsert.setRemainingTime(
                        Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis())
                                / 24 / 60 / 60 / 1000 : (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()) / 60 / 1000);
            }
            
            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_EXPIRE)) {
                batteryMembercardRefundOrderInsert.setRemainingNumber(0L);
                batteryMembercardRefundOrderInsert.setRemainingTime(
                        Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? recycleTime / 24 / 60 / 60 / 1000 : recycleTime / 60 / 1000);
            }
            
            batteryMembercardRefundOrderService.insert(batteryMembercardRefundOrderInsert);
            
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_AUDIT);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdate);
            
            membercardStartTime = membercardEndTime;
        }
        
        return Triple.of(true, null, null);
    }
    
    private void recycleBatteryDeposit(UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("RECYCLE BATTERY DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return;
        }
        
        //保存回收记录
        EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
        enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
        enterpriseCloudBeanOrder.setUid(userInfo.getUid());
        enterpriseCloudBeanOrder.setOperateUid(0L);
        enterpriseCloudBeanOrder.setPayAmount(userBatteryDeposit.getBatteryDeposit());
        enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
        enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
        enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.RECYCLE_PAYMENT);
        enterpriseCloudBeanOrder.setType(EnterpriseCloudBeanOrder.TYPE_RECYCLE);
        enterpriseCloudBeanOrder.setBeanAmount(userBatteryDeposit.getBatteryDeposit());
        enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
        enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);
        
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(userInfo.getUid());
        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
        cloudBeanUseRecord.setBeanAmount(userBatteryDeposit.getBatteryDeposit());
        cloudBeanUseRecord.setRemainingBeanAmount(BigDecimal.ZERO);
        cloudBeanUseRecord.setPackageId(userBatteryDeposit.getDid());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
        cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
        cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
        cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
        cloudBeanUseRecordService.insert(cloudBeanUseRecord);
        
        //生成退押订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(userBatteryDeposit.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, userInfo.getUid())).payAmount(userBatteryDeposit.getBatteryDeposit())
                .refundAmount(userBatteryDeposit.getBatteryDeposit()).status(EleRefundOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).memberCardOweNumber(0).build();
        eleRefundOrderService.insert(eleRefundOrder);
    }
    
    @Override
    public Triple<Boolean, String, Object> cloudBeanGeneralView() {
        EnterpriseInfo enterpriseInfo = this.selectByUid(SecurityUtils.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("ENTERPRISE ERROR! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            return null;
        }
        
        CloudBeanGeneralViewVO cloudBeanGeneralViewVO = new CloudBeanGeneralViewVO();
        cloudBeanGeneralViewVO.setCanAllocationCloudBean(enterpriseInfo.getTotalBeanAmount().longValue());
        
        //代付订单
        List<CloudBeanUseRecord> payRecords = cloudBeanUseRecordService.selectByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
        if (CollectionUtils.isEmpty(payRecords)) {
            cloudBeanGeneralViewVO.setAllocationCloudBean(0D);
            cloudBeanGeneralViewVO.setAllocationMembercard(0);
            cloudBeanGeneralViewVO.setAllocationUser(0L);
        } else {
            cloudBeanGeneralViewVO.setAllocationCloudBean(payRecords.stream().mapToDouble(item -> item.getBeanAmount().doubleValue()).sum());
            cloudBeanGeneralViewVO.setAllocationMembercard(payRecords.size());
            cloudBeanGeneralViewVO.setAllocationUser(payRecords.stream().mapToLong(CloudBeanUseRecord::getUid).distinct().sum());
        }
        
        //已回收订单
        List<CloudBeanUseRecord> recycleRecords = cloudBeanUseRecordService.selectByEnterpriseIdAndType(enterpriseInfo.getId(), CloudBeanUseRecord.TYPE_RECYCLE);
        if (CollectionUtils.isEmpty(recycleRecords)) {
            cloudBeanGeneralViewVO.setRecycleCloudBean(0D);
            cloudBeanGeneralViewVO.setRecycleMembercard(0);
            cloudBeanGeneralViewVO.setRecycleUser(0L);
        } else {
            cloudBeanGeneralViewVO.setRecycleCloudBean(recycleRecords.stream().mapToDouble(item -> item.getBeanAmount().doubleValue()).sum());
            cloudBeanGeneralViewVO.setRecycleMembercard(recycleRecords.size());
            cloudBeanGeneralViewVO.setRecycleUser(recycleRecords.stream().mapToLong(CloudBeanUseRecord::getUid).distinct().sum());
        }
        
        //可回收订单
        List<CloudBeanUseRecord> cloudBeanUseRecords = cloudBeanUseRecordService.selectCanRecycleRecord(enterpriseInfo.getId(), System.currentTimeMillis());
        if (CollectionUtils.isEmpty(recycleRecords)) {
            cloudBeanGeneralViewVO.setCanRecycleCloudBean(0D);
            cloudBeanGeneralViewVO.setCanRecycleMembercard(0);
            cloudBeanGeneralViewVO.setCanRecycleUser(0L);
        } else {
            cloudBeanGeneralViewVO.setCanRecycleCloudBean(cloudBeanUseRecords.stream().mapToDouble(item -> item.getBeanAmount().doubleValue()).sum());
            cloudBeanGeneralViewVO.setCanRecycleMembercard(cloudBeanUseRecords.size());
            cloudBeanGeneralViewVO.setCanRecycleUser(cloudBeanUseRecords.stream().mapToLong(CloudBeanUseRecord::getUid).distinct().sum());
        }
        
        return Triple.of(true, null, cloudBeanGeneralViewVO);
    }
    
    @Override
    public Boolean checkUserType() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ENTERPRISE ERROR! not found user info,uid={} ", SecurityUtils.getUid());
            return Boolean.FALSE;
        }
        
        EnterpriseInfo enterpriseInfo = this.selectByUid(userInfo.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("ENTERPRISE ERROR! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            return Boolean.FALSE;
        }
        
        if (Objects.equals(EnterpriseInfo.STATUS_OPEN, enterpriseInfo.getStatus())) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    private Pair<List<Long>, List<String>> getMembercardNames(Long id) {
        
        List<Long> membercardIds = enterprisePackageService.selectByEnterpriseId(id);
        if (CollectionUtils.isEmpty(membercardIds)) {
            return Pair.of(Lists.newArrayList(), Lists.newArrayList());
        }
        
        List<String> list = Lists.newArrayList();
        membercardIds.forEach(e -> {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(e);
            if (Objects.nonNull(batteryMemberCard)) {
                list.add(batteryMemberCard.getName());
            }
        });
        
        return Pair.of(membercardIds, list);
    }
}
