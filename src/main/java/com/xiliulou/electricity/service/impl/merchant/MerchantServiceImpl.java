package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.MerchantOverdueUserCountBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantChannelEmployeeBindHistoryConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantChannelEmployeeBindHistory;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseCloudBeanOrderMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantJoinRecordMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.merchant.*;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantChannelEmployeeBindHistoryService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.QrCodeUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageVO;
import com.xiliulou.electricity.vo.merchant.*;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:10
 * @desc
 */
@Service("merchantService")
@Slf4j
public class MerchantServiceImpl implements MerchantService {
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantPlaceMapService merchantPlaceMapService;
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    @Resource
    private MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private MerchantAttrService merchantAttrService;
    
    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @Resource
    RedisService redisService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("MERCHANT-DATA-SCREEN-THREAD-POOL", 6, "merchantDataScreenThread:");
    
    @Resource
    private EnterprisePackageService enterprisePackageService;
    
    @Resource
    private MerchantUserAmountService merchantUserAmountService;
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Resource
    private MerchantLevelService merchantLevelService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecService;
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private MerchantChannelEmployeeBindHistoryService merchantChannelEmployeeBindHistoryService;
    
    @Resource
    private EnterpriseChannelUserMapper enterpriseChannelUserMapper;
    
    @Resource
    private EnterpriseCloudBeanOrderMapper enterpriseCloudBeanOrderMapper;

    @Resource
    private MerchantJoinRecordMapper merchantJoinRecordMapper;

    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Resource
    private ElectricityBatteryLabelService electricityBatteryLabelService;
    
    /**
     * 商户保存
     *
     * @param merchantSaveRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> save(MerchantSaveRequest merchantSaveRequest) {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_SAVE_UID + tokenUser.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getBindFranchiseeIdList()) && !merchantSaveRequest.getBindFranchiseeIdList().contains(merchantSaveRequest.getFranchiseeId())) {
            log.info("merchant save info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        // 检测商户名称是否存在用户表中
        User user = userService.queryByUserName(merchantSaveRequest.getName());
        if (Objects.nonNull(user)) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测商户名称是否存在
        Integer nameCount = merchantMapper.existsByName(merchantSaveRequest.getName(), tenantId, null);
        if (nameCount > 0) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测手机号
        User userPhone = userService.checkPhoneExist(null, merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, null);
        if (Objects.nonNull(userPhone)) {
            return Triple.of(false, "120201", "手机号已经存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(),
                MerchantConstant.DISABLE)) {
            return Triple.of(false, "120202", "推广权限，站点代付权限，必须选一个");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            return Triple.of(false, "120204", "商户等级不存在");
        }
        
        
        if (Objects.nonNull(merchantSaveRequest.getFranchiseeId()) && !Objects.equals(merchantSaveRequest.getFranchiseeId(), merchantLevel.getFranchiseeId())) {
            return Triple.of(false, "120241", "商户等级的加盟商和选中的加盟商不一致");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(merchantSaveRequest.getChannelEmployeeUid());
            
            if (Objects.isNull(channelEmployeeVO)) {
                return Triple.of(false, "120205", "渠道员不存在");
            }
            
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                return Triple.of(false, "120206", "渠道员的加盟商和选中的加盟商不一致");
            }
        }
        
        // 检测企业套餐是否存在
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(merchantSaveRequest.getFranchiseeId())
                    .status(BatteryMemberCard.STATUS_UP).idList(merchantSaveRequest.getEnterprisePackageIdList()).businessType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                    .delFlag(BatteryMemberCard.DEL_NORMAL).build();
            
            List<BatteryMemberCard> packageList = batteryMemberCardService.listMemberCardsByIdList(query);
            if (ObjectUtils.isEmpty(packageList)) {
                log.info("merchant save info, package is not exist name={}, packageId={}", merchantSaveRequest.getName(), merchantSaveRequest.getEnterprisePackageIdList());
                return Triple.of(false, "120207", "企业套餐不存在");
            }
            
            Set<Long> memberCardIdList = packageList.stream().map(BatteryMemberCard::getId).collect(Collectors.toSet());
            if (!Objects.equals(packageList.size(), merchantSaveRequest.getEnterprisePackageIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getEnterprisePackageIdList().stream().filter(item -> !memberCardIdList.contains(item)).collect(Collectors.toList());
                log.info("merchant save info,franchiseeId = {}, merchant name={},diff package Id ={}", merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getName(),
                        diffIdList);
                return Triple.of(false, "120207", "企业套餐不存在");
            }
        }
        
        // 检测绑定的场地数量是否大于20
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            if (merchantSaveRequest.getPlaceIdList().size() > 20) {
                return Triple.of(false, "120208", "绑定的场地数量不能大于20");
            }
            
            MerchantPlaceQueryModel placeQueryModel = MerchantPlaceQueryModel.builder().idList(merchantSaveRequest.getPlaceIdList())
                    .franchiseeId(merchantSaveRequest.getFranchiseeId()).tenantId(tenantId).build();
            
            List<MerchantPlace> placeList = merchantPlaceService.queryList(placeQueryModel);
            if (ObjectUtils.isEmpty(placeList)) {
                log.info("merchant save info, place is not exist merchant name={}, placeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getPlaceIdList());
                return Triple.of(false, "120209", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                log.info("merchant save info,merchant name={},diff place Id={}", merchantSaveRequest.getName(), diffIdList);
                return Triple.of(false, "120209", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                return Triple.of(false, "120210", "场地已经被绑定");
            }
        }
        
        long timeMillis = System.currentTimeMillis();
        // 用户信息
        User user1 = User.builder().avatar("").salt("").createTime(timeMillis).delFlag(User.DEL_NORMAL).name(merchantSaveRequest.getName()).lockFlag(User.USER_UN_LOCK)
                .phone(merchantSaveRequest.getPhone()).updateTime(timeMillis).userType(User.TYPE_USER_MERCHANT).salt("").tenantId(tenantId).build();
        
        // 如果是禁用则用户默认锁定
        if (Objects.equals(merchantSaveRequest.getStatus(), MerchantConstant.DISABLE)) {
            user1.setLockFlag(User.USER_LOCK);
        }
        
        // 保存用户
        userService.insert(user1);
        
        // 保存企业信息
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().uid(user1.getUid()).name(merchantSaveRequest.getName())
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth()).packageType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.saveMerchantEnterprise(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            String msg = "";
            
            if (ObjectUtils.isNotEmpty(enterpriseSaveRes.getRight())) {
                msg = (String) enterpriseSaveRes.getRight();
                log.error("merchant save enterprise error,name={}, msg={}", merchantSaveRequest.getName(), msg);
            }
            
            throw new BizException("120211", msg);
        }
        
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantSaveRequest, merchant);
        merchant.setEnterpriseId(enterpriseInfoQuery.getId());
        merchant.setUid(user1.getUid());
        merchant.setCreateTime(timeMillis);
        merchant.setDelFlag(MerchantConstant.DEL_NORMAL);
        merchant.setExistPlaceFee(MerchantConstant.EXISTS_PLACE_FEE_NO);
        merchant.setUpdateTime(timeMillis);
        merchant.setTenantId(tenantId);
        
        // 保存商户信息
        int i = merchantMapper.insert(merchant);
        
        // 如果有绑定渠道员 设置商户渠道员绑定时间 小程序商户首页需要使用该字段统计
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory = MerchantChannelEmployeeBindHistory.builder().merchantUid(merchant.getUid()).bindTime(timeMillis)
                    .channelEmployeeUid(merchantSaveRequest.getChannelEmployeeUid()).bindStatus(MerchantChannelEmployeeBindHistoryConstant.BIND).createTime(timeMillis)
                    .updateTime(timeMillis).tenantId(tenantId).build();
            merchantChannelEmployeeBindHistoryService.insertOne(merchantChannelEmployeeBindHistory);
        }
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            List<MerchantPlaceMap> merchantPlaceMapList = new ArrayList<>();
            List<MerchantPlaceBind> merchantPlaceBindList = new ArrayList<>();
            
            merchantSaveRequest.getPlaceIdList().stream().forEach(placeId -> {
                // 商户场地映射
                MerchantPlaceMap merchantPlaceMap = MerchantPlaceMap.builder().merchantId(merchant.getId()).placeId(placeId).tenantId(tenantId).delFlag(MerchantPlaceMap.DEL_NORMAL)
                        .updateTime(timeMillis).createTime(timeMillis).build();
                
                merchantPlaceMapList.add(merchantPlaceMap);
                
                // 商户场地绑定历史
                MerchantPlaceBind merchantPlaceBind = MerchantPlaceBind.builder().merchantId(merchant.getId()).placeId(placeId).bindTime(timeMillis)
                        .delFlag(MerchantPlaceMap.DEL_NORMAL).type(MerchantPlaceConstant.BIND).merchantMonthSettlement(MerchantPlaceConstant.MONTH_SETTLEMENT_NO)
                        .merchantMonthSettlementPower(MerchantPlaceConstant.MONTH_SETTLEMENT_POWER_NO).tenantId(tenantId).createTime(timeMillis).updateTime(timeMillis).build();
                merchantPlaceBindList.add(merchantPlaceBind);
            });
            
            // 批量保存商户场地映射
            merchantPlaceMapService.batchInsert(merchantPlaceMapList);
            
            // 批量保存商户场地绑定
            merchantPlaceBindService.batchInsert(merchantPlaceBindList);
            
            // 判断绑定的场地是否存在柜机 且修改修改存在场地费标识
            dealPlaceFee(merchant.getId(), merchantSaveRequest.getPlaceIdList());
        }
        
        // 创建商户余额
        MerchantUserAmount merchantUserAmount = new MerchantUserAmount();
        merchantUserAmount.setUid(merchant.getUid());
        merchantUserAmount.setTenantId(tenantId);
        merchantUserAmount.setTotalIncome(BigDecimal.ZERO);
        merchantUserAmount.setBalance(BigDecimal.ZERO);
        merchantUserAmount.setWithdrawAmount(BigDecimal.ZERO);
        merchantUserAmount.setCreateTime(timeMillis);
        merchantUserAmount.setUpdateTime(timeMillis);
        merchantUserAmount.setDelFlag(MerchantConstant.DEL_NORMAL);
        
        merchantUserAmountService.save(merchantUserAmount);
        
        // 调用开户账号
        return Triple.of(true, "", "");
    }
    
    public Triple<Boolean, String, Object> saveV2(MerchantSaveRequest merchantSaveRequest, Integer tenantId) {
        // 检测商户名称是否存在用户表中
        User user = userService.queryByUserName(merchantSaveRequest.getName());
        if (Objects.nonNull(user)) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测商户名称是否存在
        Integer nameCount = merchantMapper.existsByName(merchantSaveRequest.getName(), tenantId, null);
        if (nameCount > 0) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测手机号
        User userPhone = userService.checkPhoneExist(null, merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, null);
        if (Objects.nonNull(userPhone)) {
            return Triple.of(false, "120201", "手机号已经存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(),
                MerchantConstant.DISABLE)) {
            return Triple.of(false, "120202", "推广权限，站点代付权限，必须选一个");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            return Triple.of(false, "120204", "商户等级不存在");
        }
        
        long timeMillis = System.currentTimeMillis();
        // 用户信息
        User user1 = User.builder().avatar("").salt("").createTime(timeMillis).delFlag(User.DEL_NORMAL).name(merchantSaveRequest.getName()).lockFlag(User.USER_UN_LOCK)
                .phone(merchantSaveRequest.getPhone()).updateTime(timeMillis).userType(User.TYPE_USER_MERCHANT).salt("").tenantId(tenantId).build();
        
        // 如果是禁用则用户默认锁定
        if (Objects.equals(merchantSaveRequest.getStatus(), MerchantConstant.DISABLE)) {
            user1.setLockFlag(User.USER_LOCK);
        }
        
        // 保存用户
        userService.insert(user1);
        
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantSaveRequest, merchant);
        merchant.setUid(user1.getUid());
        merchant.setCreateTime(timeMillis);
        merchant.setDelFlag(MerchantConstant.DEL_NORMAL);
        merchant.setExistPlaceFee(MerchantConstant.EXISTS_PLACE_FEE_NO);
        merchant.setUpdateTime(timeMillis);
        merchant.setTenantId(tenantId);
        
        // 保存商户信息
        int i = merchantMapper.insert(merchant);
        
        // 创建商户余额
        MerchantUserAmount merchantUserAmount = new MerchantUserAmount();
        merchantUserAmount.setUid(merchant.getUid());
        merchantUserAmount.setTenantId(tenantId);
        merchantUserAmount.setTotalIncome(BigDecimal.ZERO);
        merchantUserAmount.setBalance(BigDecimal.ZERO);
        merchantUserAmount.setWithdrawAmount(BigDecimal.ZERO);
        merchantUserAmount.setCreateTime(timeMillis);
        merchantUserAmount.setUpdateTime(timeMillis);
        merchantUserAmount.setDelFlag(MerchantConstant.DEL_NORMAL);
        
        merchantUserAmountService.save(merchantUserAmount);
        
        // 调用开户账号
        return Triple.of(true, "", merchant);
    }
    
    /**
     * 处理场地所在柜机是否设置场地费
     *
     * @param id
     * @param placeIdList
     */
    private void dealPlaceFee(Long id, List<Long> placeIdList) {
        if (ObjectUtils.isEmpty(placeIdList)) {
            return;
        }
        
        Integer count = merchantPlaceMapService.existsPlaceFeeByPlaceIdList(placeIdList);
        if (Objects.isNull(count) || Objects.equals(count, NumberConstant.ZERO)) {
            return;
        }
        
        // 修改商户存在场地费
        Merchant updateMerchant = Merchant.builder().id(id).existPlaceFee(MerchantConstant.EXISTS_PLACE_FEE_YES).updateTime(System.currentTimeMillis()).build();
        
        merchantMapper.updateById(updateMerchant);
        
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> update(MerchantSaveRequest merchantSaveRequest) {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_UPDATE_UID + tokenUser.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Merchant merchant = this.merchantMapper.selectById(merchantSaveRequest.getId());
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        // 判断修改的加盟商是否有改变
        if (!Objects.equals(merchantSaveRequest.getFranchiseeId(), merchant.getFranchiseeId())) {
            log.info("merchant update info, franchisee not allow change id={}, franchiseeId={}, updateFranchiseeId={}", merchantSaveRequest.getId(), merchant.getFranchiseeId(), merchantSaveRequest.getFranchiseeId());
            return Triple.of(false, "120239", "商户加盟商不允许修改");
        }
        
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getBindFranchiseeIdList()) && !merchantSaveRequest.getBindFranchiseeIdList().contains(merchantSaveRequest.getFranchiseeId())) {
            log.info("merchant update info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", merchantSaveRequest.getId(), merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(),
                MerchantConstant.DISABLE)) {
            return Triple.of(false, "120202", "推广权限，站点代付权限，必须选一个");
        }
    
        // 检测商户名称是否存在用户表中
        User user = userService.queryByUserName(merchantSaveRequest.getName());
        if (Objects.nonNull(user) && !Objects.equals(user.getUid(), merchant.getUid())) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测商户名称是否存在
        Integer nameCount = merchantMapper.existsByName(merchantSaveRequest.getName(), tenantId, merchantSaveRequest.getId());
        if (nameCount > 0) {
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测手机号
        User userPhone = userService.checkPhoneExist(null, merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, merchant.getUid());
        if (Objects.nonNull(userPhone)) {
            return Triple.of(false, "120201", "手机号已经存在");
        }
        
        // 检测加盟商是否存在
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            return Triple.of(false, "120204", "商户等级不存在");
        }
        
        if (Objects.nonNull(merchantSaveRequest.getFranchiseeId()) && !Objects.equals(merchantSaveRequest.getFranchiseeId(), merchantLevel.getFranchiseeId())) {
            return Triple.of(false, "120241", "商户等级的加盟商和选中的加盟商不一致");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(merchantSaveRequest.getChannelEmployeeUid());
            if (Objects.isNull(channelEmployeeVO)) {
                log.info("merchant update info, channel us is not find id={}, channelUserId={}", merchantSaveRequest.getId(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120205", "渠道员不存在");
            }
            
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                log.info("merchant update info, channel us is not find id={}, franchiseeId={}, channelUserFranchiseeId={}, channelUserId={}", merchantSaveRequest.getId(),
                        merchantSaveRequest.getFranchiseeId(), channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120206", "渠道员的加盟商和选中的加盟商不一致");
            }
        }
        
        // 检测企业套餐是否存在
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(merchantSaveRequest.getFranchiseeId())
                    .status(BatteryMemberCard.STATUS_UP).idList(merchantSaveRequest.getEnterprisePackageIdList()).businessType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                    .delFlag(BatteryMemberCard.DEL_NORMAL).build();
            
            List<BatteryMemberCard> packageList = batteryMemberCardService.listMemberCardsByIdList(query);
            
            if (ObjectUtils.isEmpty(packageList)) {
                return Triple.of(false, "120207", "企业套餐不存在");
            }
            
            Set<Long> memberCardIdList = packageList.stream().map(BatteryMemberCard::getId).collect(Collectors.toSet());
            if (!Objects.equals(packageList.size(), merchantSaveRequest.getEnterprisePackageIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getEnterprisePackageIdList().stream().filter(item -> !memberCardIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant update error,franchiseeId = {}, merchant merchantId={},diff package Id ={}", merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getId(),
                        diffIdList);
                return Triple.of(false, "120207", "企业套餐不存在");
            }
        }
        
        // 检测绑定的场地数量是否大于20
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            if (merchantSaveRequest.getPlaceIdList().size() > 20) {
                return Triple.of(false, "120208", "绑定的场地数量不能大于20");
            }
            
            MerchantPlaceQueryModel placeQueryModel = MerchantPlaceQueryModel.builder().idList(merchantSaveRequest.getPlaceIdList())
                    .franchiseeId(merchantSaveRequest.getFranchiseeId()).tenantId(tenantId).build();
            
            List<MerchantPlace> placeList = merchantPlaceService.queryList(placeQueryModel);
            if (ObjectUtils.isEmpty(placeList)) {
                return Triple.of(false, "120209", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                return Triple.of(false, "120209", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                return Triple.of(false, "120210", "场地已经被绑定");
            }
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = new MerchantDeleteCacheDTO();
        long timeMillis = System.currentTimeMillis();
        User updateUser = new User();
        boolean flag = false;
        // 判断手机号是否有变化
        if (!Objects.equals(merchant.getPhone(), merchantSaveRequest.getPhone())) {
            flag = true;
            updateUser.setPhone(merchantSaveRequest.getPhone());
            // 手机号变更用户禁用
            //            updateUser.setLockFlag(User.USER_LOCK);
        }
        
        // 判断是否为禁用
        if (!Objects.equals(merchant.getStatus(), merchantSaveRequest.getStatus())) {
            flag = true;
            
            if (Objects.equals(merchantSaveRequest.getStatus(), MerchantConstant.ENABLE)) {
                updateUser.setLockFlag(User.USER_UN_LOCK);
            } else {
                updateUser.setLockFlag(User.USER_LOCK);
            }
        }
    
        // 判断名称是否有变化
        if (!Objects.equals(merchant.getName(), merchantSaveRequest.getName())) {
            flag = true;
            updateUser.setName(merchantSaveRequest.getName());
        }
        
        if (flag) {
            // 修改用户的手机号或者名称
            updateUser.setUid(merchant.getUid());
            updateUser.setUpdateTime(timeMillis);
            userService.updateMerchantUser(updateUser);
            
            // 删除用户缓存
            merchantDeleteCacheDTO.setDeleteUserFlag(true);
            merchantDeleteCacheDTO.setUser(updateUser);
        }
        
        // 修改企业信息
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().uid(merchant.getUid()).name(merchantSaveRequest.getName()).id(merchant.getEnterpriseId())
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth()).packageType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        
        // 同步企业信息数据
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.updateMerchantEnterprise(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            String msg = "";
            
            if (ObjectUtils.isNotEmpty(enterpriseSaveRes.getRight())) {
                msg = (String) enterpriseSaveRes.getRight();
                log.error("merchant update enterprise error,id={}, msg={}", merchantSaveRequest.getId(), msg);
            }
            
            throw new BizException("120213", msg);
        }
        
        // 删除企业缓存
        merchantDeleteCacheDTO.setEnterpriseInfoId(merchant.getEnterpriseId());
        
        Merchant merchantUpdate = new Merchant();
        BeanUtils.copyProperties(merchantSaveRequest, merchantUpdate);
        merchantUpdate.setUpdateTime(timeMillis);
        
        // 修改商户信息
        merchantMapper.update(merchantUpdate);
        
        // 如果更新的渠道员和绑定的渠道员不一致才操作
        if (!Objects.equals(merchant.getChannelEmployeeUid(), merchantSaveRequest.getChannelEmployeeUid())) {
            //如果解绑渠道员，则merchantSaveRequest.getChannelEmployeeUid为null
            if (Objects.isNull(merchantSaveRequest.getChannelEmployeeUid())) {
                MerchantChannelEmployeeBindHistory updateBindHistory = MerchantChannelEmployeeBindHistory.builder().merchantUid(merchant.getUid()).unBindTime(timeMillis)
                        .bindStatus(MerchantChannelEmployeeBindHistoryConstant.UN_BIND).updateTime(timeMillis).tenantId(tenantId).build();
                merchantChannelEmployeeBindHistoryService.updateUnbindTimeByMerchantUid(updateBindHistory);
            } else if (Objects.isNull(merchant.getChannelEmployeeUid())) {
                MerchantChannelEmployeeBindHistory insertBindHistory = MerchantChannelEmployeeBindHistory.builder().merchantUid(merchant.getUid())
                        .channelEmployeeUid(merchantSaveRequest.getChannelEmployeeUid()).bindTime(timeMillis).bindStatus(MerchantChannelEmployeeBindHistoryConstant.BIND)
                        .createTime(timeMillis).updateTime(timeMillis).tenantId(tenantId).build();
                merchantChannelEmployeeBindHistoryService.insertOne(insertBindHistory);
            } else {
                // 更新原有记录为解绑
                MerchantChannelEmployeeBindHistory updateBindHistory = MerchantChannelEmployeeBindHistory.builder().merchantUid(merchant.getUid()).unBindTime(timeMillis)
                        .bindStatus(MerchantChannelEmployeeBindHistoryConstant.UN_BIND).updateTime(timeMillis).tenantId(tenantId).build();
                merchantChannelEmployeeBindHistoryService.updateUnbindTimeByMerchantUid(updateBindHistory);
                
                // 新增绑定记录
                MerchantChannelEmployeeBindHistory insertBindHistory = MerchantChannelEmployeeBindHistory.builder().merchantUid(merchant.getUid())
                        .channelEmployeeUid(merchantSaveRequest.getChannelEmployeeUid()).bindTime(timeMillis).bindStatus(MerchantChannelEmployeeBindHistoryConstant.BIND)
                        .createTime(timeMillis).updateTime(timeMillis).tenantId(tenantId).build();
                merchantChannelEmployeeBindHistoryService.insertOne(insertBindHistory);
            }
        }
        
        // 删除商户缓存
        merchantDeleteCacheDTO.setMerchantId(merchant.getId());
        
        // 查询商户已经绑定的场地
        MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().merchantId(merchantSaveRequest.getId()).eqFlag(MerchantPlaceMapQueryModel.EQ).build();
        List<MerchantPlaceMap> existsPlaceList = merchantPlaceMapService.queryList(queryModel);
        
        Set<Long> bindPlaceSet = new HashSet<>();
        if (ObjectUtils.isNotEmpty(existsPlaceList)) {
            bindPlaceSet = existsPlaceList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
        }
        
        Set<Long> unBindList = new HashSet<>();
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            // 新增场地
            Set<Long> finalBindPlaceSet = bindPlaceSet;
            
            List<Long> addPlaceIdList = merchantSaveRequest.getPlaceIdList().stream().filter(placeId -> !finalBindPlaceSet.contains(placeId)).collect(Collectors.toList());
            
            // 解绑场地
            unBindList = finalBindPlaceSet.stream().filter(item -> !merchantSaveRequest.getPlaceIdList().contains(item)).collect(Collectors.toSet());
            
            List<MerchantPlaceMap> merchantPlaceMapList = new ArrayList<>();
            List<MerchantPlaceBind> merchantPlaceBindList = new ArrayList<>();
            addPlaceIdList.stream().forEach(placeId -> {
                // 场地映射
                MerchantPlaceMap merchantPlaceMap = MerchantPlaceMap.builder().merchantId(merchant.getId()).placeId(placeId).tenantId(tenantId).delFlag(MerchantPlaceMap.DEL_NORMAL)
                        .createTime(timeMillis).updateTime(timeMillis).build();
                
                merchantPlaceMapList.add(merchantPlaceMap);
                
                // 场地绑定历史
                MerchantPlaceBind merchantPlaceBind = MerchantPlaceBind.builder().merchantId(merchant.getId()).placeId(placeId).bindTime(timeMillis)
                        .delFlag(MerchantPlaceMap.DEL_NORMAL).type(MerchantPlaceConstant.BIND).merchantMonthSettlement(MerchantPlaceConstant.MONTH_SETTLEMENT_NO)
                        .merchantMonthSettlementPower(MerchantPlaceConstant.MONTH_SETTLEMENT_POWER_NO).tenantId(tenantId).createTime(timeMillis).updateTime(timeMillis).build();
                
                merchantPlaceBindList.add(merchantPlaceBind);
            });
            
            // 批量保存场地映射
            if (ObjectUtils.isNotEmpty(merchantPlaceMapList)) {
                merchantPlaceMapService.batchInsert(merchantPlaceMapList);
                
                // 不存在场地费，处理场地费
                if (Objects.equals(merchant.getExistPlaceFee(), MerchantConstant.EXISTS_PLACE_FEE_NO)) {
                    // 判断绑定的场地是否存在柜机 且修改修改存在场地费标识
                    dealPlaceFee(merchant.getId(), addPlaceIdList);
                }
            }
            
            // 批量保存绑定历史
            if (ObjectUtils.isNotEmpty(merchantPlaceBindList)) {
                merchantPlaceBindService.batchInsert(merchantPlaceBindList);
            }
            
        } else if (ObjectUtils.isNotEmpty(existsPlaceList)) {
            unBindList = bindPlaceSet;
        }
        
        if (ObjectUtils.isNotEmpty(unBindList)) {
            // 批量解绑场地
            merchantPlaceBindService.batchUnBind(unBindList, merchant.getId(), System.currentTimeMillis());
            
            // 删除解绑的场地映射
            merchantPlaceMapService.batchDeleteByMerchantId(merchantSaveRequest.getId(), unBindList);
            
            // 处理解绑场地下关联的员工
            dealPlaceEmployee(unBindList);
        }
        
        //异步添加渠道员变更操作记录
        operateRecordUtil.asyncRecord(new HashMap<String,Object>(),new HashMap<String,Object>(),merchantSaveRequest,merchant,(merchantReq,oldMerchant,operateLogDTO)->{
            //设置修改后的值
            if (Objects.nonNull(merchantReq.getChannelEmployeeUid())){
                Map<String, Object> newValue = operateLogDTO.getNewValue();
                ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(merchantReq.getChannelEmployeeUid());
                if (!Objects.isNull(channelEmployeeVO) && StringUtils.isNotBlank(channelEmployeeVO.getName())){
                    newValue.put("name", channelEmployeeVO.getName());
                    newValue.put("merchantName", merchantReq.getName());
                    operateLogDTO.setNewValue(newValue);
                }
            }
            //设置修改前的值
            if (Objects.nonNull(oldMerchant.getChannelEmployeeUid())){
                Map<String, Object> oldValue = operateLogDTO.getOldValue();
                ChannelEmployeeVO employeeVO = channelEmployeeService.queryByUid(oldMerchant.getChannelEmployeeUid());
                if (!Objects.isNull(employeeVO) && StringUtils.isNotBlank(employeeVO.getName())){
                    oldValue.put("name", employeeVO.getName());
                    oldValue.put("merchantName", oldMerchant.getName());
                    operateLogDTO.setOldValue(oldValue);
                }
            }
            return operateLogDTO;
        });
        
        return Triple.of(true, "", merchantDeleteCacheDTO);
    }
    
    private void dealPlaceEmployee(Set<Long> unBindList) {
        if (ObjectUtils.isEmpty(unBindList)) {
            return;
        }
        
        List<Long> placeIdList = new ArrayList<>(unBindList);
        // 根据场地id查询员工
        List<MerchantEmployee> merchantEmployeeList = merchantEmployeeService.queryListByPlaceId(placeIdList);
        
        if (ObjectUtils.isEmpty(merchantEmployeeList)) {
            return;
        }
        
        List<Long> uidList = merchantEmployeeList.stream().map(MerchantEmployee::getUid).collect(Collectors.toList());
        // 批量解绑场地员工
        merchantEmployeeService.batchUnbindPlaceId(uidList);
    }
    
    @Override
    public void deleteCache(MerchantDeleteCacheDTO merchantDeleteCacheDTO) {
        // 删除商户缓存
        redisService.delete(CacheConstant.CACHE_MERCHANT + merchantDeleteCacheDTO.getMerchantId());
        redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + merchantDeleteCacheDTO.getEnterpriseInfoId());
        
        // 删除用户缓存
        if (merchantDeleteCacheDTO.isDeleteUserFlag() && Objects.nonNull(merchantDeleteCacheDTO.getUser())) {
            User user = merchantDeleteCacheDTO.getUser();
            
            redisService.delete(CacheConstant.CACHE_USER_UID + user.getUid());
            redisService.delete(CacheConstant.CACHE_USER_PHONE + TenantContextHolder.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
        }
        
        // 批量删除场地员工对应的管理员列表的缓存
        if (ObjectUtils.isNotEmpty(merchantDeleteCacheDTO.getUidList())) {
            merchantDeleteCacheDTO.getUidList().stream().forEach(user -> {
                if (Objects.nonNull(user)) {
                    redisService.delete(CacheConstant.CACHE_USER_UID + user.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_PHONE + TenantContextHolder.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
                }
            });
        }
    }
    
    @Transactional
    @Override
    public Triple<Boolean, String, Object> remove(Long id, List<Long> bindFranchiseeIdList) {
        // 检测商户是否存在
        Integer tenantId = TenantContextHolder.getTenantId();
        Merchant merchant = this.merchantMapper.selectById(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        if (ObjectUtils.isNotEmpty(bindFranchiseeIdList) && !bindFranchiseeIdList.contains(merchant.getFranchiseeId())) {
            log.info("merchant delete info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", id, merchant.getFranchiseeId(), bindFranchiseeIdList);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        // 判断商户的余额：t_merchant_user_amount：balance
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(merchant.getUid());
        if (Objects.nonNull(merchantUserAmount) && merchantUserAmount.getBalance().compareTo(BigDecimal.ZERO) == 1) {
            return Triple.of(false, "120214", "该商户下还有可提现金额，请先处理后操作");
        }
        
        // 检测商户下的所有场地是否存在绑定电柜
        MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().merchantId(id).eqFlag(MerchantPlaceMapQueryModel.EQ).build();
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            List<Long> placeIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toList());
            MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel = MerchantPlaceCabinetBindQueryModel.builder().placeIdList(placeIdList).status(MerchantPlaceConstant.BIND)
                    .build();
            
            List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryList(placeCabinetBindQueryModel);
            if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
                log.info("merchant delete info, cabinet is bind merchantId={}, cabinetId={}", id, merchantPlaceCabinetBinds);
                return Triple.of(false, "120215", "请先解绑换电柜后操作");
            }
        }
        
        // 删除企业
        Triple<Boolean, String, Object> triple = enterpriseInfoService.deleteMerchantEnterprise(merchant.getEnterpriseId());
        if (!triple.getLeft()) {
            String msg = "";
            
            if (ObjectUtils.isNotEmpty(triple.getRight())) {
                msg = (String) triple.getRight();
                log.error("merchant delete enterprise error,id={}, msg={}", id, msg);
            }
            
            throw new BizException("120216", msg);
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = new MerchantDeleteCacheDTO();
        long timeMillis = System.currentTimeMillis();
        
        User oldUser = userService.queryByUidFromCache(merchant.getUid());
        
        // 判断用户是否被删除
        if (ObjectUtils.isNotEmpty(oldUser)) {
            // 让商户登录状态失效
            User updateUser = new User();
            updateUser.setUid(merchant.getUid());
            updateUser.setUpdateTime(timeMillis);
            updateUser.setLockFlag(User.USER_LOCK);
            updateUser.setDelFlag(User.DEL_DEL);
            userService.updateMerchantUser(updateUser);
            
            // 删除用户
            userService.removeById(merchant.getUid(), timeMillis);
            
            // 删除用户绑定关系
            // userOauthBindService.deleteByUid(merchant.getUid(), tenantId);
            
            // 删除用户缓存
            merchantDeleteCacheDTO.setDeleteUserFlag(true);
            merchantDeleteCacheDTO.setUser(oldUser);
            
            // 删除商户
            Merchant deleteMerchant = new Merchant();
            deleteMerchant.setUpdateTime(timeMillis);
            deleteMerchant.setId(id);
            deleteMerchant.setDelFlag(MerchantConstant.DEL_DEL);
            merchantMapper.removeById(deleteMerchant);
        }
        
        // 删除商户和场地的关联表
        merchantPlaceMapService.batchDeleteByMerchantId(id, null);
        
        // 删除商户与场地的绑定关系
        merchantPlaceBindService.batchUnBind(null, id, timeMillis);
        
        // 删除商户认证关系
        userOauthBindService.deleteByUid(merchant.getUid(), tenantId);
        
        // 检测商户和员工是否有绑定关系
        List<MerchantEmployee> merchantEmployeeList = merchantEmployeeService.queryListByMerchantUid(merchant.getUid(), tenantId);
        
        // 解绑商户渠道员绑定记录表
        MerchantChannelEmployeeBindHistory updateHistory = new MerchantChannelEmployeeBindHistory();
        updateHistory.setMerchantUid(merchant.getUid());
        updateHistory.setUnBindTime(System.currentTimeMillis());
        updateHistory.setTenantId(TenantContextHolder.getTenantId());
        updateHistory.setUpdateTime(System.currentTimeMillis());
        updateHistory.setBindStatus(MerchantChannelEmployeeBindHistoryConstant.UN_BIND);
        merchantChannelEmployeeBindHistoryService.updateUnbindTimeByMerchantUid(updateHistory);
        
        if (ObjectUtils.isNotEmpty(merchantEmployeeList)) {
            // 批量删除员工
            List<Long> employeeUidList = merchantEmployeeList.stream().map(MerchantEmployee::getUid).collect(Collectors.toList());
            merchantEmployeeService.batchRemoveByUidList(employeeUidList, timeMillis);
            
            // 批量查询admin数据
            List<User> userList = userService.queryListByUidList(employeeUidList, tenantId);
            
            if (ObjectUtils.isNotEmpty(userList)) {
                // 批量删除admin表的员工数据
                List<Long> uidList = userList.stream().map(User::getUid).collect(Collectors.toList());
                // 批量删除用户
                userService.batchRemoveByUidList(uidList, timeMillis);
                
                // 批量删除缓存
                merchantDeleteCacheDTO.setUidList(userList);
            }
        }
        
        merchantDeleteCacheDTO.setMerchantId(id);
        merchantDeleteCacheDTO.setEnterpriseInfoId(merchant.getEnterpriseId());
        merchantDeleteCacheDTO.setTenantId(tenantId);
        
        return Triple.of(true, null, merchantDeleteCacheDTO);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPageRequest merchantPageRequest) {
        MerchantQueryModel merchantQueryModel = new MerchantQueryModel();
        BeanUtils.copyProperties(merchantPageRequest, merchantQueryModel);
        
        return merchantMapper.countTotal(merchantQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantVO> listByPage(MerchantPageRequest merchantPageRequest) {
        List<MerchantVO> resList = new ArrayList<>();
        MerchantQueryModel queryModel = new MerchantQueryModel();
        BeanUtils.copyProperties(merchantPageRequest, queryModel);
        
        List<Merchant> merchantList = this.merchantMapper.selectListByPage(queryModel);
        
        if (ObjectUtils.isEmpty(merchantList)) {
            return resList;
        }
        
        Set<Long> merchantIdList = new HashSet<>();
        List<Long> uidList = new ArrayList<>();
        List<Long> enterpriseIdList = new ArrayList<>();
        List<Long> levelIdList = new ArrayList<>();
        
        for (Merchant merchant : merchantList) {
            MerchantVO merchantVO = new MerchantVO();
            BeanUtil.copyProperties(merchant, merchantVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(merchant.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                merchantVO.setFranchiseeName(franchisee.getName());
            }
            
            // 渠道员
            User user = userService.queryByUidFromCache(merchant.getChannelEmployeeUid());
            if (ObjectUtils.isNotEmpty(user)) {
                merchantVO.setChannelUserName(user.getName());
            }
            
            // 查询用户的手机号
            User merchantUser = userService.queryByUidFromCache(merchant.getUid());
            if (ObjectUtils.isNotEmpty(merchantUser)) {
                merchantVO.setPhone(merchantUser.getPhone());
            }
            
            merchantIdList.add(merchant.getId());
            uidList.add(merchant.getUid());
            levelIdList.add(merchant.getMerchantGradeId());
            enterpriseIdList.add(merchant.getEnterpriseId());
            resList.add(merchantVO);
        }
        
        // 等级名称
        CompletableFuture<Void> merchantLevelInfo = CompletableFuture.runAsync(() -> {
            List<MerchantLevel> merchantLevels = merchantLevelService.queryListByIdList(levelIdList);
            
            if (ObjectUtils.isNotEmpty(merchantLevels)) {
                Map<Long, MerchantLevel> merchantLevelNameMap = merchantLevels.stream().collect(Collectors.toMap(MerchantLevel::getId, Function.identity(), (key, key1) -> key1));
                
                resList.stream().forEach(item -> {
                    MerchantLevel merchantLevel = merchantLevelNameMap.get(item.getMerchantGradeId());
                    
                    if (ObjectUtils.isNotEmpty(merchantLevel)) {
                        item.setGradeName(merchantLevel.getName());
                        item.setMerchantLevel(merchantLevel.getLevel());
                    }
                });
            }
            
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query level error!", e);
            return null;
        });
        
        // 查询商户对应的场地数
        CompletableFuture<Void> placeInfo = CompletableFuture.runAsync(() -> {
            MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().merchantIdList(merchantIdList).build();
            List<MerchantPlaceMapVO> merchantPlaceMaps = merchantPlaceMapService.countByMerchantIdList(placeMapQueryModel);
            
            //  改为用商户id统计数量
            Map<Long, Integer> placeMap = new HashMap<>();
            
            if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
                placeMap = merchantPlaceMaps.stream().collect(Collectors.toMap(MerchantPlaceMapVO::getMerchantId, MerchantPlaceMapVO::getCount, (key, key1) -> key1));
            }
            
            Map<Long, Integer> finalPlaceMap = placeMap;
            resList.stream().forEach(item -> {
                if (ObjectUtils.isNotEmpty(finalPlaceMap.get(item.getId()))) {
                    item.setPlaceCount(finalPlaceMap.get(item.getId()));
                } else {
                    item.setPlaceCount(NumberConstant.ZERO);
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query place error!", e);
            return null;
        });
        
        // 查询商户下的用户数
        CompletableFuture<Void> channelUserInfo = CompletableFuture.runAsync(() -> {
            List<Long> collect = new ArrayList<>(merchantIdList);
            
            //  改为用商户id统计数量
            MerchantJoinRecordQueryMode joinRecordQueryMode = MerchantJoinRecordQueryMode.builder().tenantId(merchantPageRequest.getTenantId()).merchantIdList(collect)
                    .status(MerchantJoinRecordConstant.STATUS_SUCCESS).build();
            List<MerchantJoinRecordVO> merchantJoinRecordList = merchantJoinRecordService.countByMerchantIdList(joinRecordQueryMode);
            
            Map<Long, Integer> userMap = new HashMap<>();
            
            if (ObjectUtils.isNotEmpty(merchantJoinRecordList)) {
                userMap = merchantJoinRecordList.stream()
                        .collect(Collectors.toMap(MerchantJoinRecordVO::getMerchantId, MerchantJoinRecordVO::getMerchantUserNum, (key, key1) -> key1));
            }
            
            Map<Long, Integer> finalUserMap = userMap;
            
            resList.stream().forEach(item -> {
                if (ObjectUtils.isNotEmpty(finalUserMap.get(item.getId()))) {
                    item.setUserCount(finalUserMap.get(item.getId()));
                } else {
                    item.setUserCount(NumberConstant.ZERO);
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query user error!", e);
            return null;
        });
        
        // 查询商户的提现余额
        CompletableFuture<Void> merchantUserAmountInfo = CompletableFuture.runAsync(() -> {
            List<Long> collect = new ArrayList<>(uidList);
            
            MerchantUserAmountQueryMode userAmountQueryMode = MerchantUserAmountQueryMode.builder().uidList(collect).tenantId(merchantPageRequest.getTenantId()).build();
            List<MerchantUserAmount> merchantUserAmounts = merchantUserAmountService.queryList(userAmountQueryMode);
            
            Map<Long, MerchantUserAmount> userAmountMap = new HashMap<>();
            
            if (ObjectUtils.isNotEmpty(merchantUserAmounts)) {
                userAmountMap = merchantUserAmounts.stream().collect(Collectors.toMap(MerchantUserAmount::getUid, Function.identity(), (key1, key2) -> key2));
            }
            
            Map<Long, MerchantUserAmount> finalUserAmountMap = userAmountMap;
            
            resList.forEach(item -> {
                MerchantUserAmount merchantUserAmount = finalUserAmountMap.get(item.getUid());
                if (ObjectUtils.isNotEmpty(merchantUserAmount)) {
                    item.setWithdrawAmount(merchantUserAmount.getWithdrawAmount());
                    item.setBalance(merchantUserAmount.getBalance());
                } else {
                    item.setWithdrawAmount(BigDecimal.ZERO);
                    item.setBalance(BigDecimal.ZERO);
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query user amount error!", e);
            return null;
        });
        
        // 查询商户的企业云豆
        CompletableFuture<Void> enterpriseInfo = CompletableFuture.runAsync(() -> {
            List<EnterpriseInfo> enterpriseInfoList = enterpriseInfoService.queryListByIdList(enterpriseIdList);
            
            Map<Long, EnterpriseInfo> enterpriseInfoMap = new HashMap<>();
            
            if (ObjectUtils.isNotEmpty(enterpriseInfoList)) {
                enterpriseInfoMap = enterpriseInfoList.stream().collect(Collectors.toMap(EnterpriseInfo::getId, Function.identity(), (key1, key2) -> key2));
            }
            
            Map<Long, EnterpriseInfo> finalEnterpriseInfoMap = enterpriseInfoMap;
            
            resList.forEach(item -> {
                EnterpriseInfo enterprise = finalEnterpriseInfoMap.get(item.getEnterpriseId());
                
                if (Objects.isNull(enterprise) || Objects.isNull(enterprise.getTotalBeanAmount())) {
                    item.setTotalCloudBeanAmount(BigDecimal.ZERO);
                    return;
                }
                
                item.setTotalCloudBeanAmount(enterprise.getTotalBeanAmount());
            });
            
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query enterprise error!", e);
            return null;
        });

        // 逾期用户数量
        CompletableFuture<Void> overdueUserInfo = CompletableFuture.runAsync(() -> {
            List<MerchantOverdueUserCountBO> merchantOverdueUserCountBOList = merchantJoinRecordService.listOverdueUserCount(merchantIdList, System.currentTimeMillis());
            Map<Long, MerchantOverdueUserCountBO> merchantOverdueUserCountBOMap = new HashMap<>();
            if (ObjectUtils.isNotEmpty(merchantOverdueUserCountBOList)) {
                merchantOverdueUserCountBOMap = merchantOverdueUserCountBOList.stream().filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getMerchantId()))
                        .collect(Collectors.toMap(MerchantOverdueUserCountBO::getMerchantId, Function.identity(), (v1, v2) -> v2));
            }

            Map<Long, MerchantOverdueUserCountBO> finalMerchantOverdueUserCountBOMap = merchantOverdueUserCountBOMap;

            resList.forEach(item -> {
                MerchantOverdueUserCountBO merchantOverdueUserCountBO = finalMerchantOverdueUserCountBOMap.get(item.getId());
                item.setOverdueUserCount(Objects.nonNull(merchantOverdueUserCountBO) ? merchantOverdueUserCountBO.getOverdueUserCount() : NumberConstant.ZERO);
            });
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query enterprise error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(placeInfo, channelUserInfo, merchantUserAmountInfo, merchantLevelInfo, enterpriseInfo, overdueUserInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for merchant query", e);
        }
        
        return resList;
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryById(Long id, List<Long> franchiseeIdList) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Merchant merchant = merchantMapper.selectById(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        if (ObjectUtils.isNotEmpty(franchiseeIdList) && !franchiseeIdList.contains(merchant.getFranchiseeId())) {
            log.info("MERCHANT QUERY INFO! franchisee is not different, id={}, franchiseeId={}", "商户不存在", id, franchiseeIdList);
            return Triple.of(false, "120212", "商户不存在");
        }
        
        MerchantUpdateShowVO vo = new MerchantUpdateShowVO();
        BeanUtils.copyProperties(merchant, vo);
        
        User user = userService.queryByUidFromCache(vo.getUid());
        if (ObjectUtils.isNotEmpty(user)) {
            vo.setPhone(user.getPhone());
        }
        
        // 查询企业套餐
        List<EnterprisePackageVO> enterprisePackageList = enterprisePackageService.queryListByEnterpriseId(merchant.getEnterpriseId());
        if (ObjectUtils.isNotEmpty(enterprisePackageList)) {
            vo.setEnterprisePackageList(enterprisePackageList);
        }
        
        // 查询选中的场地
        List<MerchantPlaceSelectVO> merchantPlaceSelectVOList = merchantPlaceMapService.queryListByMerchantId(id);
        if (ObjectUtils.isNotEmpty(merchantPlaceSelectVOList)) {
            vo.setPlaceList(merchantPlaceSelectVOList);
        }
        
        // 查询渠道员
        if (ObjectUtils.isNotEmpty(merchant.getChannelEmployeeUid())) {
            User channelUser = userService.queryByUidFromCache(merchant.getChannelEmployeeUid());
            
            Optional.ofNullable(channelUser).ifPresent(channelUserTemp -> {
                vo.setChannelUserName(channelUserTemp.getName());
            });
        }
        
        // 查询加盟商名称
        if (ObjectUtils.isNotEmpty(merchant.getFranchiseeId())) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(merchant.getFranchiseeId());
            
            Optional.ofNullable(franchisee).ifPresent(franchiseeTemp -> {
                vo.setFranchiseeName(franchiseeTemp.getName());
            });
        }
        
        //查询openid
        UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(merchant.getUid(), merchant.getTenantId(),UserOauthBind.SOURCE_WX_PRO);
        if (!Objects.isNull(userOauthBind) && Objects.nonNull(userOauthBind.getThirdId())){
            vo.setOpenId(userOauthBind.getThirdId());
        }
        return Triple.of(true, "", vo);
    }
    
    @Override
    public Merchant queryByIdFromCache(Long id) {
        Merchant merchant = redisService.getWithHash(CacheConstant.CACHE_MERCHANT + id, Merchant.class);
        if (Objects.nonNull(merchant)) {
            return merchant;
        }
        
        merchant = merchantMapper.selectById(id);
        if (Objects.isNull(merchant)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_MERCHANT + id, merchant);
        
        return merchant;
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryByIdList(List<Long> idList) {
        if (ObjectUtils.isEmpty(idList)) {
            return Triple.of(true, null, null);
        }
        
        MerchantQueryModel queryModel = new MerchantQueryModel();
        queryModel.setIdList(idList);
        List<Merchant> merchantList = this.merchantMapper.selectListByPage(queryModel);
        
        return Triple.of(true, null, merchantList);
    }
    
    @Slave
    @Override
    public List<MerchantVO> queryList(MerchantPageRequest merchantPageRequest) {
        List<MerchantVO> resList = new ArrayList<>();
        MerchantQueryModel queryModel = new MerchantQueryModel();
        BeanUtils.copyProperties(merchantPageRequest, queryModel);
        List<Merchant> merchantList = this.merchantMapper.selectListByPage(queryModel);
        
        if (ObjectUtils.isEmpty(merchantList)) {
            return Collections.emptyList();
        }
        
        merchantList.stream().forEach(item -> {
            MerchantVO vo = new MerchantVO();
            BeanUtils.copyProperties(item, vo);
            resList.add(vo);
        });
        
        return resList;
    }
    
    /**
     * 根据uid查询商户
     *
     * @param uid
     * @return
     */
    @Slave
    @Override
    public Merchant queryByUid(Long uid) {
        Merchant merchant = merchantMapper.selectByUid(uid);
        
        return merchant;
    }
    
    @Slave
    @Override
    public List<Merchant> queryByChannelEmployeeUid(Long channelEmployeeId) {
        return merchantMapper.selectByChannelEmployeeUid(channelEmployeeId);
    }
    
    /**
     * 小程序：员工添加下拉框场地选择
     *
     * @param merchantUid
     * @param employeeUid
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceSelectVO> queryPlaceListByUid(Long merchantUid, Long employeeUid) {
        Merchant merchant = merchantMapper.selectByUid(merchantUid);
        
        if (Objects.isNull(merchant)) {
            log.error("merchant query place list error, merchant is not find, uid={}", merchantUid);
            return Collections.EMPTY_LIST;
        }
        
        List<MerchantPlaceSelectVO> merchantPlaceUserVOList = merchantPlaceMapService.queryListByMerchantId(merchant.getId());
        // 查询是否已经绑定了员工
        Map<Long, Long> userMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceUserVOList)) {
            List<Long> placeIdList = merchantPlaceUserVOList.stream().map(MerchantPlaceSelectVO::getPlaceId).collect(Collectors.toList());
            List<MerchantEmployee> merchantEmployees = merchantEmployeeService.queryListByPlaceId(placeIdList);
            if (ObjectUtils.isNotEmpty(merchantEmployees)) {
                userMap = merchantEmployees.stream().collect(Collectors.toMap(MerchantEmployee::getPlaceId, MerchantEmployee::getUid, (key, key1) -> key1));
            }
        }
        
        //如果商户员工UID不为空，则查询渠道员信息，获取当前渠道员绑定的场地ID
        Long placeId = null;
        if (Objects.nonNull(employeeUid)) {
            MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeService.queryMerchantEmployeeByUid(employeeUid);
            placeId = merchantEmployeeVO.getPlaceId();
        }
        
        for (MerchantPlaceSelectVO merchantPlaceSelectVO : merchantPlaceUserVOList) {
            if (ObjectUtils.isNotEmpty(userMap.get(merchantPlaceSelectVO.getPlaceId()))) {
                // 被绑定设置为禁用
                merchantPlaceSelectVO.setStatus(MerchantPlaceConstant.DISABLE);
            } else {
                merchantPlaceSelectVO.setStatus(MerchantPlaceConstant.ENABLE);
            }
            
            if (Objects.equals(merchantPlaceSelectVO.getPlaceId(), placeId)) {
                merchantPlaceSelectVO.setSelected(true);
            }
            
        }
        
        return merchantPlaceUserVOList;
    }
    
    @Override
    public Integer updateById(Merchant merchant) {
        Integer integer = merchantMapper.updateById(merchant);
        
        // 删除缓存
        DbUtils.dbOperateSuccessThenHandleCache(integer, i -> {
            redisService.delete(CacheConstant.CACHE_MERCHANT + merchant.getId());
        });
        
        return integer;
    }
    
    @Slave
    @Override
    public MerchantUserVO queryMerchantUserDetail() {
        User user = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(user)) {
            return null;
        }
        
        MerchantUserVO merchantUserVO = new MerchantUserVO();
        BeanUtils.copyProperties(user, merchantUserVO);
        
        if (Objects.equals(user.getUserType(), User.TYPE_USER_MERCHANT)) {
            Merchant merchant = merchantMapper.selectByUid(SecurityUtils.getUid());
            if (Objects.isNull(merchant) || Objects.isNull(merchant.getMerchantGradeId())) {
                return merchantUserVO;
            }
            
            merchantUserVO.setMerchantId(merchant.getId());
            merchantUserVO.setMerchantUid(merchant.getUid());
            merchantUserVO.setType(MerchantConstant.MERCHANT_QR_CODE_TYPE);
            String code = merchant.getId() + ":" + merchant.getUid() + ":" + MerchantConstant.MERCHANT_QR_CODE_TYPE;
            merchantUserVO.setCode(QrCodeUtils.codeEnCoder(code));
            
            MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
            merchantUserVO.setMerchantLevelName(Objects.nonNull(merchantLevel) ? merchantLevel.getName() : "");
            merchantUserVO.setMerchantLevel(Objects.nonNull(merchantLevel) ? merchantLevel.getLevel() : "");
            return merchantUserVO;
        }
        
        return merchantUserVO;
    }
    
    @Slave
    @Override
    public MerchantQrCodeVO getMerchantQrCode(Long uid, Long merchantId) {
        MerchantQrCodeVO vo = new MerchantQrCodeVO();
        vo.setMerchantId(merchantId);
        vo.setMerchantUid(uid);
        vo.setType(MerchantConstant.MERCHANT_QR_CODE_TYPE);
        String code = merchantId + ":" + uid + ":" + MerchantConstant.MERCHANT_QR_CODE_TYPE;
        vo.setCode(QrCodeUtils.codeEnCoder(code));
        return vo;
    }
    
    @Override
    public void deleteCacheById(Long id) {
        redisService.delete(CacheConstant.CACHE_MERCHANT + id);
    }
    
    @Override
    public Integer batchUpdateExistPlaceFee(List<Long> merchantIdList, Integer existsPlaceFee, Long updateTime) {
        return merchantMapper.batchUpdateExistPlaceFee(merchantIdList, existsPlaceFee, updateTime);
    }
    
    @Slave
    @Override
    public List<Merchant> listAllByIds(List<Long> merchantIdList, Integer tenantId) {
        return merchantMapper.selectListAllByIds(merchantIdList, tenantId);
    }
    
    @Transactional
    @Override
    public void repairEnterprise(List<Long> enterpriseIds, List<Long> merchantIds, Integer queryTenantId) {
        log.info("repair enterprise start");
        
        // 查询状态为开启的企业
        List<EnterpriseInfo> enterpriseInfos = enterpriseInfoService.queryList(queryTenantId);
        if (ObjectUtils.isEmpty(enterpriseInfos)) {
            log.error("repair enterprise error, enterprise info is empty");
            return;
        }
        
        long currentTimeMillis = System.currentTimeMillis();
        enterpriseInfos.forEach(enterpriseInfo -> {
            Integer tenantId = enterpriseInfo.getTenantId();
            
            // 判断商户是否存在
            int num = this.existsEnterpriseByEnterpriseId(enterpriseInfo.getId());
            if (num > 0) {
                log.error("repair enterprise error, merchant enterprise is exists,id={}", enterpriseInfo.getId());
                return;
            }
    
            List<MerchantLevel> merchantLevels = merchantLevelService.listByFranchiseeId(tenantId, enterpriseInfo.getFranchiseeId());
            Long levelId = NumberConstant.ZERO_L;
            if (ObjectUtils.isNotEmpty(merchantLevels)) {
                MerchantLevel merchantLevel = merchantLevels.stream().sorted(Comparator.comparing(MerchantLevel::getLevel).reversed()).findFirst().orElse(null);
                if (Objects.nonNull(merchantLevel)) {
                    levelId = merchantLevel.getId();
                }
            }
            
            // 查询商户等级配置默认为五级
            MerchantSaveRequest merchantSaveRequest = new MerchantSaveRequest();
            merchantSaveRequest.setName(enterpriseInfo.getName());
            merchantSaveRequest.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            UserInfo user = userInfoService.queryByUidFromCache(enterpriseInfo.getUid());
            if (Objects.isNull(user)) {
                log.error("repair enterprise error! not find user id={}", enterpriseInfo.getId());
                return;
            }
            merchantSaveRequest.setPhone(user.getPhone());
            merchantSaveRequest.setStatus(0);
            merchantSaveRequest.setInviteAuth(1);
            merchantSaveRequest.setEnterprisePackageAuth(0);
            merchantSaveRequest.setPurchaseAuthority(0);
            merchantSaveRequest.setAutoUpGrade(0);
//            merchantSaveRequest.setMerchantGradeId(levelId);
            merchantSaveRequest.setEnterpriseId(enterpriseInfo.getId());
            
            // 为该企业创建商户
            Triple<Boolean, String, Object> triple = this.saveV2(merchantSaveRequest, tenantId);
            log.info("repair enterprise trip={}", triple.getRight());
            if (!triple.getLeft()) {
                log.error("repair enterprise error, enterprise saveV2 is error={}, enterpriseId={}", triple.getRight(), enterpriseInfo.getId());
                return;
            }
            Merchant merchant = (Merchant) triple.getRight();
           
            // 将原企业表的uid改变为商户对应的uid
            EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
            enterpriseInfoUpdate.setId(enterpriseInfo.getId());
            enterpriseInfoUpdate.setUpdateTime(currentTimeMillis);
            enterpriseInfoUpdate.setUid(merchant.getUid());
            enterpriseInfoService.update(enterpriseInfoUpdate);
    
            EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
            enterpriseChannelUser.setEnterpriseId(enterpriseInfo.getId());
            enterpriseChannelUser.setInviterId(merchant.getUid());
            enterpriseChannelUser.setUpdateTime(currentTimeMillis);
            enterpriseChannelUserMapper.updateByEnterpriseId(enterpriseChannelUser);
    
            List<EnterpriseCloudBeanOrder> enterpriseCloudBeanOrderList = enterpriseCloudBeanOrderMapper.selectListByEnterpriseId(enterpriseInfo.getId());
            if (ObjectUtils.isNotEmpty(enterpriseCloudBeanOrderList)) {
                enterpriseCloudBeanOrderList.stream().forEach(enterpriseCloudBeanOrder -> {
                    EnterpriseCloudBeanOrder enterpriseCloudBeanOrderUpdate  = new EnterpriseCloudBeanOrder();
                    enterpriseCloudBeanOrderUpdate.setId(enterpriseCloudBeanOrder.getId());
                    enterpriseCloudBeanOrderUpdate.setEnterpriseId(enterpriseInfo.getId());
                    enterpriseCloudBeanOrderUpdate.setUid(merchant.getUid());
                    // 如果操作人是站长自己则需要修改uid
                    if (Objects.equals(enterpriseCloudBeanOrder.getOperateUid(), enterpriseInfo.getUid())) {
                        enterpriseCloudBeanOrderUpdate.setOperateUid(merchant.getUid());
                    }
                    enterpriseCloudBeanOrderUpdate.setUpdateTime(currentTimeMillis);
    
                    enterpriseCloudBeanOrderMapper.updateOneById(enterpriseCloudBeanOrder);
                });
            }
    
            enterpriseIds.add(enterpriseInfo.getId());
            
            merchantIds.add(merchant.getId());
            log.info("repair enterprise success, enterpriseId={}", enterpriseInfo.getId());
        });
        
        log.info("repair enterprise end");
    }
    
    @Slave
    public int existsEnterpriseByEnterpriseId(Long id) {
        return merchantMapper.existsEnterpriseByEnterpriseId(id);
    }
    
    @Override
    public void deleteCacheForRepairEnterprise(List<Long> enterpriseIds, List<Long> merchantIds) {
        if (ObjectUtils.isNotEmpty(enterpriseIds)) {
            enterpriseIds.forEach(enterpriseId -> {
                redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseId);
            });
        }
        
        if (ObjectUtils.isNotEmpty(merchantIds)) {
            merchantIds.stream().forEach(merchantId -> {
                redisService.delete(CacheConstant.CACHE_MERCHANT + merchantId);
            });
            
        }
    }
    
    @Override
    @Transactional
    public Pair<Boolean, Object> unbindOpenId(MerchantUnbindReq params) {
        UserOauthBind userOauthBind = userOauthBindService.queryOauthByOpenIdAndUid(params.getId(), params.getOpenId(), TenantContextHolder.getTenantId());
        if (Objects.isNull(userOauthBind)) {
            return Pair.of(false,"解绑失败,请联系客服处理");
        }
    
        Long franchiseeId = NumberConstant.ZERO_L;
    
        if (Objects.equals(params.getType(), MerchantConstant.UN_BIND_MERCHANT_USER_TYPE)) {
            // 检测用户所属的商户是否存在
            Merchant merchant = this.queryByUid(userOauthBind.getUid());
            if (Objects.isNull(merchant)) {
                return Pair.of(false,"解绑商户不存在,请联系客服处理");
            }
        
            franchiseeId = merchant.getFranchiseeId();
        }
    
        if (Objects.equals(params.getType(), MerchantConstant.UN_BIND_CHANNEL_EMPLOYEE_USER_TYPE)) {
            // 检测用户所属的渠道员是否存在
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(userOauthBind.getUid());
            if (Objects.isNull(channelEmployeeVO)) {
                return Pair.of(false,"解绑渠道员不存在,请联系客服处理");
            }
        
            franchiseeId = channelEmployeeVO.getFranchiseeId();
        }
        
        if (ObjectUtils.isNotEmpty(params.getBindFranchiseeIdList()) && !params.getBindFranchiseeIdList().contains(franchiseeId)) {
            log.info("merchant un bind open id info, franchisee is not different uid={}, franchiseeId={}, bindFranchiseeId={}", userOauthBind.getUid(), franchiseeId, params.getBindFranchiseeIdList());
            return Pair.of(false,  "当前加盟商无权限操作");
        }
    
        boolean delete = userOauthBindService.deleteById(userOauthBind.getId());
        if (delete){
            // 强制下线
            userInfoService.clearUserOauthBindToken(List.of(userOauthBind), CacheConstant.MERCHANT_CLIENT_ID);
            operateRecordUtil.record(null,params);
            return Pair.of(true, "解绑成功");
        }
        return Pair.of(false, "解绑失败,请联系客服处理");
    }
    
    @Override
    @Slave
    public List<Merchant> listByEnterpriseList(List<Long> enterpriseIdList) {
        return merchantMapper.listByEnterpriseList(enterpriseIdList);
    }

    @Override
    @Slave
    public Integer countOverdueUserTotal(MerchantJoinUserQueryMode merchantJoinUserQueryMode) {
        merchantJoinUserQueryMode.setCurrentTime(System.currentTimeMillis());

        return merchantJoinRecordMapper.countOverdueUserTotal(merchantJoinUserQueryMode);
    }

    @Override
    @Slave
    public List<MerchantJoinUserVO> listOverdueUserByPage(MerchantJoinUserQueryMode merchantJoinUserQueryMode) {
        merchantJoinUserQueryMode.setCurrentTime(System.currentTimeMillis());

        //获取当前商户下的用户列表信息
        List<MerchantJoinUserVO> merchantJoinUserVOS = merchantJoinRecordMapper.selectJoinUserList(merchantJoinUserQueryMode);
        if (CollectionUtils.isEmpty(merchantJoinUserVOS)) {
            return Collections.emptyList();
        }


        merchantJoinUserVOS.forEach(merchantJoinUserVO -> {
            Long packageId = merchantJoinUserVO.getPackageId();
            if (Objects.nonNull(packageId) && packageId != 0) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
                if (Objects.nonNull(batteryMemberCard)) {
                    merchantJoinUserVO.setPackageName(batteryMemberCard.getName());
                }
            }

            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectLatestByUid(merchantJoinUserVO.getJoinUid());
            if (Objects.nonNull(electricityMemberCardOrder)) {
                merchantJoinUserVO.setPurchasedTime(electricityMemberCardOrder.getCreateTime());
            }

            ElectricityMemberCardOrder firstMemberCardOrder = electricityMemberCardOrderService.selectFirstMemberCardOrder(merchantJoinUserVO.getJoinUid());
            if (Objects.nonNull(firstMemberCardOrder)) {
                merchantJoinUserVO.setFirstPurchasedTime(firstMemberCardOrder.getCreateTime());
            }

        });

        return merchantJoinUserVOS;
    }
    
    @Override
    public R<Integer> countReceived(Long uid) {
        Merchant merchant = queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("MERCHANT COUNT RECEIVED WARN! merchant is null");
            return R.fail("120212", "商户不存在");
        }
        
        return R.ok(electricityBatteryLabelService.countReceived(merchant.getId()));
    }
    
    @Slave
    @Override
    public List<Merchant> queryListByUidList(Set<Long> uidList, Integer tenantId) {
        return merchantMapper.selectListByUidList(uidList, tenantId);
    }
}
