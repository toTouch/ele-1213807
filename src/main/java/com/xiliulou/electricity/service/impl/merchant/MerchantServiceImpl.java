package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
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
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageVO;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantJoinRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;
import com.xiliulou.electricity.vo.merchant.MerchantQrCodeVO;
import com.xiliulou.electricity.vo.merchant.MerchantUpdateShowVO;
import com.xiliulou.electricity.vo.merchant.MerchantUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("MERCHANT-DATA-SCREEN-THREAD-POOL", 3, "merchantDataScreenThread:");
    
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
        
        // 检测商户名称是否存在用户表中
        User user = userService.queryByUserName(merchantSaveRequest.getName());
        if (Objects.nonNull(user)) {
            log.error("merchant save error, name is exit user name={}", merchantSaveRequest.getName());
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测商户名称是否存在
        Integer nameCount = merchantMapper.existsByName(merchantSaveRequest.getName(), tenantId, null);
        if (nameCount > 0) {
            log.error("merchant save error, name is exit merchant name={}", merchantSaveRequest.getName());
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测手机号
        User userPhone = userService.checkMerchantExist(null, merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, null);
        if (Objects.nonNull(userPhone)) {
            log.error("merchant save error, phone is exit name={}", merchantSaveRequest.getPhone());
            return Triple.of(false, "120201", "手机号已经存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(),
                MerchantConstant.DISABLE)) {
            log.error("merchant save error, invite auth and enterprise package auth select at least one, name={}, inviteAuth={}, enterprisePackageAuth={}",
                    merchantSaveRequest.getName(), merchantSaveRequest.getInviteAuth(), merchantSaveRequest.getEnterprisePackageAuth());
            return Triple.of(false, "120202", "推广权限，站点代付权限，必须选一个");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant save error, franchisee is null name={}, franchiseeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getFranchiseeId());
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            log.error("merchant save error, merchant level is null name={}, merchantLevelId={}", merchantSaveRequest.getName(), merchantSaveRequest.getMerchantGradeId());
            return Triple.of(false, "120204", "商户等级不存在");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(merchantSaveRequest.getChannelEmployeeUid());
            
            if (Objects.isNull(channelEmployeeVO)) {
                log.error("merchant save error, channel us is not find name={}, channelEmployeeUid={}", merchantSaveRequest.getName(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120205", "渠道员不存在");
            }
            
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                log.error("merchant save error, channel us is not find name={}, franchiseeId={}, channelUserFranchiseeId={}, channelEmployeeUid={}", merchantSaveRequest.getName(),
                        merchantSaveRequest.getFranchiseeId(), channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120206", "渠道员的加盟商和选中的加盟商不一致");
            }
        }
        
        // 检测企业套餐是否存在
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(merchantSaveRequest.getFranchiseeId())
                    .status(BatteryMemberCard.STATUS_UP).idList(merchantSaveRequest.getEnterprisePackageIdList()).businessType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                    .delFlag(BatteryMemberCard.DEL_NORMAL).build();
            
            List<BatteryMemberCard> packageList = batteryMemberCardService.queryListByIdList(query);
            if (ObjectUtils.isEmpty(packageList)) {
                log.error("merchant save error, package is not exist name={}, packageId={}", merchantSaveRequest.getName(), merchantSaveRequest.getEnterprisePackageIdList());
                return Triple.of(false, "120207", "企业套餐不存在");
            }
            
            Set<Long> memberCardIdList = packageList.stream().map(BatteryMemberCard::getId).collect(Collectors.toSet());
            if (!Objects.equals(packageList.size(), merchantSaveRequest.getEnterprisePackageIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getEnterprisePackageIdList().stream().filter(item -> !memberCardIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant save error,franchiseeId = {}, merchant name={},diff package Id ={}", merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getName(),
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
                log.error("merchant save error, place is not exist merchant name={}, placeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getPlaceIdList());
                return Triple.of(false, "120209", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant save error,merchant name={},diff place Id={}", merchantSaveRequest.getName(), diffIdList);
                return Triple.of(false, "120209", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                log.error("merchant save error,merchant name={}, place is bind placeId={}", merchantSaveRequest.getName(), collect);
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
        
        // 生产企业信息
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        enterpriseInfo.setName(merchantSaveRequest.getName());
        enterpriseInfo.setFranchiseeId(merchantSaveRequest.getFranchiseeId());
        enterpriseInfo.setStatus(merchantSaveRequest.getEnterprisePackageAuth());
        enterpriseInfo.setUid(user1.getUid());
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().uid(user1.getUid()).name(merchantSaveRequest.getName())
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth()).packageType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.saveNew(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            String msg = "保存企业信息出错";
            
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
        merchant.setUpdateTime(timeMillis);
        merchant.setTenantId(tenantId);
        
        // 保存商户信息
        int i = merchantMapper.insert(merchant);
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            List<MerchantPlaceMap> merchantPlaceMapList = new ArrayList<>();
            List<MerchantPlaceBind> merchantPlaceBindList = new ArrayList<>();
            
            merchantSaveRequest.getPlaceIdList().stream().forEach(placeId -> {
                // 商户场地映射
                MerchantPlaceMap merchantPlaceMap = MerchantPlaceMap.builder().merchantId(merchant.getId()).placeId(placeId).tenantId(tenantId).delFlag(MerchantPlaceMap.DEL_NORMAL)
                        .createTime(timeMillis).build();
                merchantPlaceMapList.add(merchantPlaceMap);
                
                // 商户场地绑定历史
                MerchantPlaceBind merchantPlaceBind = MerchantPlaceBind.builder().merchantId(merchant.getId()).placeId(placeId).bindTime(timeMillis)
                        .delFlag(MerchantPlaceMap.DEL_NORMAL).type(MerchantPlaceConstant.BIND).merchantMonthSettlement(MerchantPlaceConstant.MONTH_SETTLEMENT_NO)
                        .merchantMonthSettlementPower(MerchantPlaceConstant.MONTH_SETTLEMENT_POWER_NO).tenantId(tenantId).createTime(timeMillis).build();
                merchantPlaceBindList.add(merchantPlaceBind);
            });
            
            // 批量保存商户场地映射
            merchantPlaceMapService.batchInsert(merchantPlaceMapList);
            // 批量保存商户场地绑定
            merchantPlaceBindService.batchInsert(merchantPlaceBindList);
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
            log.error("merchant update error, merchant is not exit id={}, tenantId", merchantSaveRequest.getId(), tenantId);
            return Triple.of(false, "120212", "商户不存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(),
                MerchantConstant.DISABLE)) {
            log.error("merchant update error, invite auth and enterprise package auth select at least one, id={}, inviteAuth={}, enterprisePackageAuth={}",
                    merchantSaveRequest.getId(), merchantSaveRequest.getInviteAuth(), merchantSaveRequest.getEnterprisePackageAuth());
            return Triple.of(false, "120202", "推广权限，站点代付权限，必须选一个");
        }
        
        // 检测商户名称是否存在
        Integer nameCount = merchantMapper.existsByName(merchantSaveRequest.getName(), tenantId, merchantSaveRequest.getId());
        if (nameCount > 0) {
            log.error("merchant update error, name is exit id={}, name={}", merchantSaveRequest.getId(), merchantSaveRequest.getName());
            return Triple.of(false, "120233", "商户名称重复，请修改");
        }
        
        // 检测手机号
        User userPhone = userService.checkMerchantExist(null, merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, merchant.getUid());
        if (Objects.nonNull(userPhone)) {
            log.error("merchant update error, phone is exit id={}, phone={}", merchantSaveRequest.getId(), merchantSaveRequest.getPhone());
            return Triple.of(false, "120201", "手机号已经存在");
        }
        
        // 检测加盟上是否存在
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant update error, franchisee is null id={}, franchiseeId={}", merchantSaveRequest.getId(), merchantSaveRequest.getFranchiseeId());
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            log.error("merchant update error, merchant level is null id={}, merchantLevelId={}", merchantSaveRequest.getId(), merchantSaveRequest.getMerchantGradeId());
            return Triple.of(false, "120204", "商户等级不存在");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryById(merchantSaveRequest.getChannelEmployeeUid());
            if (Objects.isNull(channelEmployeeVO)) {
                log.error("merchant update error, channel us is not find id={}, channelUserId={}", merchantSaveRequest.getId(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120205", "渠道员不存在");
            }
            
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                log.error("merchant update error, channel us is not find id={}, franchiseeId={}, channelUserFranchiseeId={}, channelUserId={}", merchantSaveRequest.getId(),
                        merchantSaveRequest.getFranchiseeId(), channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getChannelEmployeeUid());
                return Triple.of(false, "120206", "渠道员的加盟商和选中的加盟商不一致");
            }
        }
        
        // 检测企业套餐是否存在
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(merchantSaveRequest.getFranchiseeId())
                    .status(BatteryMemberCard.STATUS_UP).idList(merchantSaveRequest.getEnterprisePackageIdList()).businessType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                    .delFlag(BatteryMemberCard.DEL_NORMAL).build();
            List<BatteryMemberCard> packageList = batteryMemberCardService.queryListByIdList(query);
            if (ObjectUtils.isEmpty(packageList)) {
                log.error("merchant update error, package is not exist id={}, packageId={}", merchantSaveRequest.getId(), merchantSaveRequest.getEnterprisePackageIdList());
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
                log.error("merchant update error, place is not exist merchant id={}, placeId={}", merchantSaveRequest.getId(), merchantSaveRequest.getPlaceIdList());
                return Triple.of(false, "120209", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant update error,merchant merchantId={},diff place diffIdList={}", merchantSaveRequest.getId(), diffIdList);
                return Triple.of(false, "120209", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                log.error("merchant update error,merchant id={}, place is bind placeId={}", merchantSaveRequest.getId(), collect);
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
            updateUser.setLockFlag(User.USER_LOCK);
        }
        
        // 判断是否为禁用
        if (!Objects.equals(merchant.getStatus(), merchantSaveRequest.getStatus())) {
            if (Objects.equals(merchantSaveRequest.getStatus(), MerchantConstant.ENABLE)) {
                updateUser.setLockFlag(User.USER_UN_LOCK);
            } else {
                updateUser.setLockFlag(User.USER_LOCK);
            }
        }
        
        if (flag) {
            // 修改用户的手机号或者名称
            User oldUser = userService.queryByUidFromCache(merchant.getUid());
            updateUser.setUid(oldUser.getUid());
            updateUser.setUpdateTime(timeMillis);
            userService.updateMerchantUser(updateUser);
            // 删除用户缓存
            merchantDeleteCacheDTO.setDeleteUserFlag(true);
            merchantDeleteCacheDTO.setUser(updateUser);
        }
        
        // 生产企业信息
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        enterpriseInfo.setName(merchantSaveRequest.getName());
        enterpriseInfo.setFranchiseeId(merchantSaveRequest.getFranchiseeId());
        enterpriseInfo.setStatus(merchantSaveRequest.getEnterprisePackageAuth());
        enterpriseInfo.setUid(merchant.getUid());
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().uid(merchant.getUid()).name(merchantSaveRequest.getName()).id(merchant.getEnterpriseId())
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth()).packageType(BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        // 同步企业信息数据
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.modify(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            String msg = "修改企业信息出错";
            
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
        
        // 删除商户缓存
        merchantDeleteCacheDTO.setMerchantId(merchant.getId());
        
        // 查询商户已经绑定的场地
        MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().merchantId(merchantSaveRequest.getId()).eqFlag(MerchantPlaceMapQueryModel.EQ).build();
        List<MerchantPlaceMap> existsPlaceList = merchantPlaceMapService.queryList(queryModel);
        Map<Long, Long> bindMap = existsPlaceList.stream().collect(Collectors.toMap(MerchantPlaceMap::getPlaceId, MerchantPlaceMap::getId, (key, key1) -> key1));
        
        Set<Long> unBindList = new HashSet<>();
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            // 新增场地
            List<Long> addPlaceIdList = merchantSaveRequest.getPlaceIdList().stream().filter(placeId -> !bindMap.containsKey(placeId)).collect(Collectors.toList());
            // 解绑场地
            unBindList = bindMap.keySet().stream().filter(item -> !merchantSaveRequest.getPlaceIdList().contains(item)).collect(Collectors.toSet());
            
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
            merchantPlaceMapService.batchInsert(merchantPlaceMapList);
            merchantPlaceBindService.batchInsert(merchantPlaceBindList);
        } else if (ObjectUtils.isNotEmpty(existsPlaceList)) {
            unBindList = bindMap.keySet();
        }
        
        if (ObjectUtils.isNotEmpty(unBindList)) {
            // 批量解绑场地
            merchantPlaceBindService.batchUnBind(unBindList, merchant.getId(), System.currentTimeMillis());
            // 删除解绑的场地映射
            merchantPlaceMapService.batchDeleteByMerchantId(merchantSaveRequest.getId(), unBindList);
        }
        
        return Triple.of(true, "", merchantDeleteCacheDTO);
    }
    
    @Override
    public void deleteCache(MerchantDeleteCacheDTO merchantDeleteCacheDTO) {
        // 删除商户缓存
        redisService.delete(CacheConstant.CACHE_MERCHANT + merchantDeleteCacheDTO.getMerchantId());
        redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + merchantDeleteCacheDTO.getEnterpriseInfoId());
        // 删除用户缓存
        if (merchantDeleteCacheDTO.isDeleteUserFlag()) {
            User user = merchantDeleteCacheDTO.getUser();
            redisService.delete(CacheConstant.CACHE_USER_UID + user.getUid());
            redisService.delete(CacheConstant.CACHE_USER_PHONE + TenantContextHolder.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
        }
    }
    
    @Transactional
    @Override
    public Triple<Boolean, String, Object> remove(Long id) {
        // 检测商户是否存在
        Integer tenantId = TenantContextHolder.getTenantId();
        Merchant merchant = this.merchantMapper.selectById(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            log.error("merchant delete error, merchant is not exit id={}, tenantId", id, tenantId);
            return Triple.of(false, "120212", "商户不存在");
        }
        
        // 判断商户的余额：t_merchant_user_amount：balance
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(merchant.getUid());
        if (Objects.nonNull(merchantUserAmount) && merchantUserAmount.getBalance().compareTo(BigDecimal.ZERO) == 1) {
            log.error("merchant delete error, merchant balance greater than zero  id={}, tenantId", id, tenantId);
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
                log.error("merchant delete error, cabinet is bind merchantId={}, cabinetId={}", id, merchantPlaceCabinetBinds);
                return Triple.of(false, "120215", "请先解绑换电柜后操作");
            }
        }
        
        // 删除企业
        Triple<Boolean, String, Object> triple = enterpriseInfoService.delete(merchant.getEnterpriseId());
        if (!triple.getLeft()) {
            String msg = "删除企业信息出错";
            
            if (ObjectUtils.isNotEmpty(triple.getRight())) {
                msg = (String) triple.getRight();
                log.error("merchant delete enterprise error,id={}, msg={}", id, msg);
            }
    
            throw new BizException("120216", msg);
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = new MerchantDeleteCacheDTO();
        long timeMillis = System.currentTimeMillis();
        
        // 让商户登录状态失效
        User oldUser = userService.queryByUidFromCache(merchant.getUid());
        User updateUser = new User();
        updateUser.setUid(merchant.getUid());
        updateUser.setUpdateTime(timeMillis);
        updateUser.setLockFlag(User.USER_LOCK);
        updateUser.setDelFlag(User.DEL_DEL);
        userService.updateMerchantUser(updateUser);
        
        // 删除用户
        userService.removeById(merchant.getUid(), timeMillis);
        
        // 删除用户缓存
        merchantDeleteCacheDTO.setDeleteUserFlag(true);
        merchantDeleteCacheDTO.setUser(oldUser);
        
        // 删除商户
        Merchant deleteMerchant = new Merchant();
        deleteMerchant.setUpdateTime(timeMillis);
        deleteMerchant.setId(id);
        deleteMerchant.setDelFlag(MerchantConstant.DEL_DEL);
        merchantMapper.removeById(deleteMerchant);
        
        // 删除商户和场地的关联表
        merchantPlaceMapService.batchDeleteByMerchantId(id, null);
        
        // 删除商户与场地的绑定关系
        merchantPlaceBindService.batchUnBind(null, id, timeMillis);
        
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
                placeMap = merchantPlaceMaps.stream()
                        .collect(Collectors.toMap(MerchantPlaceMapVO::getMerchantId, MerchantPlaceMapVO::getCount, (key, key1) -> key1));
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
                    .status(MerchantJoinRecord.STATUS_SUCCESS).build();
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
            
            MerchantUserAmountQueryMode joinRecordQueryMode = MerchantUserAmountQueryMode.builder().uidList(collect).tenantId(merchantPageRequest.getTenantId()).build();
            List<MerchantUserAmount> merchantJoinRecordList = merchantUserAmountService.queryList(joinRecordQueryMode);
            
            Map<Long, MerchantUserAmount> userAmountMap = new HashMap<>();
            
            if (ObjectUtils.isNotEmpty(merchantJoinRecordList)) {
                userAmountMap = merchantJoinRecordList.stream()
                        .collect(Collectors.toMap(MerchantUserAmount::getUid, Function.identity(), (key1, key2) -> key2));
            }
            
            Map<Long, MerchantUserAmount> finalUserAmountMap = userAmountMap;
            
            resList.forEach(item -> {
                MerchantUserAmount merchantUserAmount = finalUserAmountMap.get(item.getId());
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
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(placeInfo, channelUserInfo, merchantUserAmountInfo, merchantLevelInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for merchant query", e);
        }
        
        return resList;
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryById(Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Merchant merchant = merchantMapper.selectById(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
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
        
        return Triple.of(true, "", vo);
    }
    
    @Override
    public Merchant queryByIdFromCache(Long id) {
        Merchant merchant  = redisService.getWithHash(CacheConstant.CACHE_MERCHANT + id, Merchant.class);
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
     * @param uid
     * @return
     */
    @Slave
    @Override
    public Merchant queryByUid(Long uid) {
        Merchant merchant = merchantMapper.selectByUid(uid);
        
        return merchant;
    }
    
    /**
     * 小程序：员工添加下拉框场地选择
     *
     * @param uid
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceSelectVO> queryPlaceListByUid(Long uid, Long merchantEmployeeUid) {
        Merchant merchant = merchantMapper.selectByUid(uid);
        
        if (Objects.isNull(merchant)) {
            log.error("merchant query place list error, merchant is not find, uid={}", uid);
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
    
        Map<Long, Long> finalUserMap = userMap;
        merchantPlaceUserVOList.stream().forEach(item -> {
            if (ObjectUtils.isNotEmpty(finalUserMap.get(item.getPlaceId()))) {
                // 被绑定设置为禁用
                item.setStatus(MerchantPlaceSelectVO.disable);
            } else {
                item.setStatus(MerchantPlaceSelectVO.enable);
            }
        });
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
            merchantUserVO.setCode(MerchantJoinRecordServiceImpl.codeEnCoder(merchant.getId(), merchant.getUid(), 1));
        
            MerchantLevel merchantLevel = merchantLevelService.queryById(merchant.getMerchantGradeId());
            merchantUserVO.setMerchantLevel(Objects.nonNull(merchantLevel) ? merchantLevel.getName() : "");
            return merchantUserVO;
        }
    
        return merchantUserVO;
    }
    
    @Slave
    @Override
    public Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel queryModel) {
        return merchantMapper.countMerchantNumByTime(queryModel);
    }
    
    @Slave
    @Override
    public MerchantQrCodeVO getMerchantQrCode(Long uid, Long merchantId) {
        MerchantQrCodeVO vo = new MerchantQrCodeVO();
        vo.setMerchantId(merchantId);
        vo.setMerchantUid(uid);
        vo.setType(MerchantConstant.MERCHANT_QR_CODE_TYPE);
        vo.setCode(MerchantJoinRecordServiceImpl.codeEnCoder(merchantId, uid, 1));
//        vo.setTenantCode(tenant.getCode());
        return vo;
    }
}
