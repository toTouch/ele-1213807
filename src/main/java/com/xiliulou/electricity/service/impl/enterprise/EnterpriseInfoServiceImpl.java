package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
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
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseBatteryPackageMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseInfoMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
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
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CloudBeanGeneralViewVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoPackageVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePurchasedPackageResultVO;
import com.xiliulou.electricity.vo.enterprise.UserCloudBeanDetailVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    
    @Resource
    EnterpriseBatteryPackageMapper enterpriseBatteryPackageMapper;
    
    @Autowired
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Autowired
    private EnterpriseRentRecordService enterpriseRentRecordService;
    
    
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
                return Triple.of(false, "100315", "企业配置信息不存在");
            }
            
            if (query.getTotalBeanAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                log.error("CLOUD BEAN RECHARGE ERROR!illegal totalBeanAmount,uid={},totalBeanAmount={}", userInfo.getUid(), query.getTotalBeanAmount());
                return Triple.of(false, "100314", "支付金额不合法");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(electricityPayParams)) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found pay params,uid={}", userInfo.getUid());
                return Triple.of(false, "100314", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(userInfo.getUid(), TenantContextHolder.getTenantId());
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("CLOUD BEAN RECHARGE ERROR!not found userOauthBind,uid={}", userInfo.getUid());
                return Triple.of(false, "100314", "未找到用户的第三方授权信息!");
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
        
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit)) {
            return Triple.of(false, "", "企业已存在");
        }
        
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfo);
        enterpriseInfo.setBusinessId(Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + RandomUtil.randomInt(1000, 9999)));
        enterpriseInfo.setRecoveryMode(EnterpriseInfo.RECOVERY_MODE_RETURN);
        enterpriseInfo.setTotalBeanAmount(BigDecimal.ZERO);
        enterpriseInfo.setDelFlag(EnterpriseInfo.DEL_NORMAL);
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
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "100315", "企业配置不存在");
        }
        
        EnterpriseInfo enterpriseInfoOld = this.selectByUid(enterpriseInfoQuery.getUid());
        if (Objects.nonNull(enterpriseInfoOld) && !Objects.equals(enterpriseInfoOld.getUid(), enterpriseInfo.getUid())) {
            return Triple.of(false, "", "用户已存在");
        }
        
        EnterpriseInfo enterpriseInfoExit = this.selectByName(enterpriseInfoQuery.getName());
        if (Objects.nonNull(enterpriseInfoExit) && !Objects.equals(enterpriseInfoExit.getId(), enterpriseInfo.getId())) {
            return Triple.of(false, "", "企业已存在");
        }
        
        enterprisePackageService.deleteByEnterpriseId(enterpriseInfo.getId());
        if (!CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
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
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfoUpdate);
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(id);
        if (Objects.isNull(enterpriseInfo) || !Objects.equals(TenantContextHolder.getTenantId(), enterpriseInfo.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        //校验企业用户云豆是否都已回收
        if (enterpriseChannelUserService.queryNotRecycleUserCount(id) > 0) {
            return Triple.of(false, null, "企业用户云豆未回收");
        }
        
        if (BigDecimal.ZERO.compareTo(enterpriseInfo.getTotalBeanAmount()) >= 0) {
            return Triple.of(false, null, "企业云豆未结算");
        }
        
        enterpriseChannelUserService.deleteByEnterpriseId(id);
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setDelFlag(EnterpriseInfo.DEL_DEL);
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> rechargeForAdmin(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(enterpriseCloudBeanRechargeQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "100315", "企业配置不存在");
        }
        
        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        if (Objects.equals(EnterpriseCloudBeanOrder.TYPE_ADMIN_DEDUCT, enterpriseCloudBeanRechargeQuery.getType())) {
            enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().subtract(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        } else {
            enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        }
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
        cloudBeanUseRecord.setType(enterpriseCloudBeanRechargeQuery.getType());
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
    public EnterpriseInfo selectByName(String name) {
        return this.enterpriseInfoMapper.selectOne(new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getName, name).last("limit 0,1"));
    }
    
    @Override
    public EnterpriseInfoVO selectDetailByUid(Long uid) {
        EnterpriseInfo enterpriseInfo = this.enterpriseInfoMapper.selectByUid(uid);
        if (Objects.isNull(enterpriseInfo)) {
            return null;
        }
        
        EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
        BeanUtils.copyProperties(enterpriseInfo, enterpriseInfoVO);
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(enterpriseInfo.getUid());
        enterpriseInfoVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
        
        return enterpriseInfoVO;
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
        
        //解绑用户相关信息
        unbindUserData(userInfo, enterpriseChannelUser);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public void unbindUserData(UserInfo userInfo, EnterpriseChannelUser enterpriseChannelUser) {
        //清除用户租退电、购买套餐记录
        anotherPayMembercardRecordService.deleteByUid(userInfo.getUid());
        
        enterpriseRentRecordService.deleteByUid(userInfo.getUid());
        
        //更新用户云豆状态为已回收
        EnterpriseChannelUser enterpriseChannelUserUpdate = new EnterpriseChannelUser();
        enterpriseChannelUserUpdate.setId(enterpriseChannelUser.getId());
        enterpriseChannelUserUpdate.setCloudBeanStatus(CloudBeanStatusEnum.RECOVERED.getCode());
        enterpriseChannelUserUpdate.setUpdateTime(System.currentTimeMillis());
        enterpriseChannelUserService.update(enterpriseChannelUserUpdate);
        
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
    }
    
    @Override
    public Triple<Boolean, String, Object> recycleBatteryMembercard(UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        BigDecimal result = BigDecimal.ZERO;
        
        List<AnotherPayMembercardRecord> anotherPayMembercardRecords = anotherPayMembercardRecordService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(anotherPayMembercardRecords)) {
            return Triple.of(true, null, result);
        }
        
        //套餐总的云豆数
        BigDecimal totalCloudBean = BigDecimal.ZERO;
        for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(anotherPayMembercardRecord.getOrderId());
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                continue;
            }
            
            totalCloudBean = totalCloudBean.add(electricityMemberCardOrder.getPayAmount());
        }
        
        //租退电记录
        List<EnterpriseRentRecord> enterpriseRentRecords = enterpriseRentRecordService.selectByUid(userInfo.getUid());
        
        //若未租退电
        if (CollectionUtils.isEmpty(enterpriseRentRecords)) {
            //生成退租记录、回收记录
            for (AnotherPayMembercardRecord anotherPayMembercardRecord : anotherPayMembercardRecords) {
                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(anotherPayMembercardRecord.getOrderId());
                if (Objects.isNull(electricityMemberCardOrder)) {
                    log.warn("RECYCLE BATTERY MEMBERCARD WARN! not found electricityMemberCardOrder,uid={},orderId={}", userInfo.getUid(), anotherPayMembercardRecord.getOrderId());
                    continue;
                }
                
                CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
                cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
                cloudBeanUseRecord.setUid(userInfo.getUid());
                cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
                cloudBeanUseRecord.setBeanAmount(electricityMemberCardOrder.getPayAmount());
                cloudBeanUseRecord.setRemainingBeanAmount(BigDecimal.ZERO);
                cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
                cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
                cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
                cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
                cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
                cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
                cloudBeanUseRecordService.insert(cloudBeanUseRecord);
                
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
            }
            return Triple.of(true, null, totalCloudBean);
        }
        
        //每个租退电消耗的云豆数
        BigDecimal totalUsedCloudBean = BigDecimal.ZERO;
        
        //若存在租退电
        for (EnterpriseRentRecord enterpriseRentRecord : enterpriseRentRecords) {
            //完整的租退
            if (StringUtils.isNotBlank(enterpriseRentRecord.getRentMembercardOrderId()) && StringUtils.isNotBlank(enterpriseRentRecord.getReturnMembercardOrderId())) {
                //租退电时间包含的套餐消耗的云豆
                BigDecimal containMembercardUsedCloudBean = cloudBeanUseRecordService
                        .getContainMembercardUsedCloudBean(userInfo, enterpriseInfo, enterpriseRentRecord, anotherPayMembercardRecords);
                totalUsedCloudBean = totalUsedCloudBean.add(containMembercardUsedCloudBean);
                
                //租电时间所在的套餐消耗的云豆
                BigDecimal rentBatteryMembercardUsedCloudBean = cloudBeanUseRecordService
                        .getRentBatteryMembercardUsedCloudBean(userInfo, enterpriseInfo, enterpriseRentRecord, anotherPayMembercardRecords);
                totalUsedCloudBean = totalUsedCloudBean.add(rentBatteryMembercardUsedCloudBean);
                
                //退电时间所在的套餐消耗的云豆
                BigDecimal returnBatteryMembercardUsedCloudBean = cloudBeanUseRecordService
                        .getReturnBatteryMembercardUsedCloudBean(userInfo, enterpriseInfo, enterpriseRentRecord, anotherPayMembercardRecords);
                totalUsedCloudBean = totalUsedCloudBean.add(returnBatteryMembercardUsedCloudBean);
            }
            
            //有退没有租
            if (Objects.isNull(enterpriseRentRecord.getRentTime()) || Objects.nonNull(enterpriseRentRecord.getReturnTime())) {
                log.error("RECYCLE BATTERY MEMBERCARD ERROR! illegal enterpriseRentRecord,uid={},id={}", userInfo.getUid(), enterpriseRentRecord.getId());
            }
        }
        
        //待回收云豆=总的支付的云豆-总消耗的云豆
        return Triple.of(true, null, result.add(totalCloudBean.subtract(totalUsedCloudBean)));
    }
    
    @Override
    public void recycleBatteryDeposit(UserInfo userInfo, EnterpriseInfo enterpriseInfo) {
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("RECYCLE BATTERY DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return;
        }
        
        //保存回收记录
        CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
        cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
        cloudBeanUseRecord.setUid(userInfo.getUid());
        cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_RECYCLE);
        cloudBeanUseRecord.setBeanAmount(userBatteryDeposit.getBatteryDeposit());
        cloudBeanUseRecord.setRemainingBeanAmount(BigDecimal.ZERO);
        cloudBeanUseRecord.setPackageId(userBatteryDeposit.getDid());
        cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        cloudBeanUseRecord.setRef(userBatteryDeposit.getOrderId());
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
            cloudBeanGeneralViewVO.setAllocationUser(payRecords.stream().map(CloudBeanUseRecord::getUid).distinct().count());
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
            cloudBeanGeneralViewVO.setRecycleUser(recycleRecords.stream().map(CloudBeanUseRecord::getUid).distinct().count());
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
            cloudBeanGeneralViewVO.setCanRecycleUser(cloudBeanUseRecords.stream().map(CloudBeanUseRecord::getUid).distinct().count());
        }
        
        return Triple.of(true, null, cloudBeanGeneralViewVO);
    }
    
    @Slave
    @Override
    public List<EnterprisePurchasedPackageResultVO> queryPurchasedPackageCount(EnterprisePurchaseOrderQuery query) {
        
        List<EnterprisePurchasedPackageResultVO> enterprisePurchasedPackageResultVOList = Lists.newArrayList();
        
        EnterprisePurchasedPackageResultVO expiredPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer expiredPackageCount = enterpriseBatteryPackageMapper.queryExpiredPackageOrderCount(query);
        expiredPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        expiredPackageResult.setRecordSize(expiredPackageCount);
        enterprisePurchasedPackageResultVOList.add(expiredPackageResult);
        
        EnterprisePurchasedPackageResultVO unpaidPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer unpaidPackageCount = enterpriseBatteryPackageMapper.queryUnpaidPackageOrderCount(query);
        unpaidPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode());
        unpaidPackageResult.setRecordSize(unpaidPackageCount);
        enterprisePurchasedPackageResultVOList.add(unpaidPackageResult);
        
        EnterprisePurchasedPackageResultVO paidPackageResult = new EnterprisePurchasedPackageResultVO();
        Integer paidPackageCount = enterpriseBatteryPackageMapper.queryPaidPackageOrderCount(query);
        paidPackageResult.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_SUCCESS.getCode());
        paidPackageResult.setRecordSize(paidPackageCount);
        enterprisePurchasedPackageResultVOList.add(paidPackageResult);
        
        return enterprisePurchasedPackageResultVOList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateAllRenewalStatus(EnterpriseInfoQuery enterpriseInfoQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Long uid = SecurityUtils.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("update all renewal status error! not found user info, uid = {} ", uid);
            throw new BizException("300075", "当前用户不存在");
        }
        
        EnterpriseInfo enterpriseInfo = this.selectByUid(uid);
        if (Objects.isNull(enterpriseInfo)) {
            log.error("update all renewal status error! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            throw new BizException("300076", "用户所属企业不存在");
        }
        
        /*if(!enterpriseInfo.getId().equals(enterpriseInfoQuery.getId())){
            log.error("update all renewal status error! enterprise director not match current user, request params = {}, current user id = {} ", enterpriseInfoQuery.getId(), uid);
            throw new BizException("300077", "企业负责人和当前用户不匹配");
        }*/
        
        Integer renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode();
        if (RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode().equals(enterpriseInfoQuery.getRenewalStatus())) {
            renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode();
        } else if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseInfoQuery.getRenewalStatus())) {
            renewalStatus = RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode();
        }
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().enterpriseId(enterpriseInfoQuery.getId()).renewalStatus(renewalStatus)
                .tenantId(tenantId.longValue()).build();
        
        List<EnterpriseChannelUser> enterpriseChannelUserList = enterpriseChannelUserService.queryChannelUserList(enterpriseChannelUserQuery);
        
        List<Long> enterpriseUserIds = enterpriseChannelUserList.stream().map(EnterpriseChannelUser::getId).collect(Collectors.toList());
        
        if (CollectionUtil.isNotEmpty(enterpriseUserIds)) {
            enterpriseChannelUserService.batchUpdateRenewStatus(enterpriseUserIds, enterpriseInfoQuery.getRenewalStatus());
        }
        EnterpriseInfo enterprise = new EnterpriseInfo();
        enterprise.setId(enterpriseInfo.getId());
        enterprise.setRenewalStatus(enterpriseInfoQuery.getRenewalStatus());
        enterprise.setUpdateTime(System.currentTimeMillis());
        
        log.info("Update all renewal status = {}", enterpriseInfoQuery.getRenewalStatus());
        
        Integer result = enterpriseInfoMapper.update(enterprise);
        
        return result;
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
    
    @Override
    public Triple<Boolean, String, Object> refund(String orderId, HttpServletRequest request) {
        try {
            
            EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = enterpriseCloudBeanOrderService.selectByOrderId(orderId);
            if (Objects.isNull(enterpriseCloudBeanOrder)) {
                return Triple.of(false, null, "订单不存在!");
            }
            
            RefundOrder refundOrder = RefundOrder.builder().orderId(orderId)
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, enterpriseCloudBeanOrder.getUid()))
                    .payAmount(enterpriseCloudBeanOrder.getPayAmount()).refundAmount(enterpriseCloudBeanOrder.getPayAmount()).build();
            
            return Triple.of(true, "", eleRefundOrderService.commonCreateRefundOrder(refundOrder, request));
        } catch (WechatPayException e) {
            log.error("REFUND ORDER ERROR! wechat v3 refund  error! ", e);
        }
        
        return Triple.of(true, null, "退款成功!");
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
