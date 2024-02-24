package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MerchantConstant;
import com.xiliulou.electricity.constant.MerchantPlaceConstant;
import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
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
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantUpdateVO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        
        // 检测商户名称是否存在
        User user = userService.checkMerchantExist(merchantSaveRequest.getName(), merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, null);
        if (Objects.nonNull(user) && Objects.equals(user.getName(), merchantSaveRequest.getName())) {
            log.error("merchant save error, name is exit name={}", merchantSaveRequest.getName());
            return Triple.of(false, "", "商户名称已经存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(), MerchantConstant.DISABLE)) {
            log.error("merchant save error, invite auth and enterprise package auth select at least one, name={}, inviteAuth={}, enterprisePackageAuth={}",
                    merchantSaveRequest.getName(), merchantSaveRequest.getInviteAuth(), merchantSaveRequest.getEnterprisePackageAuth());
            return Triple.of(false, "", "推广权限，站点代付权限，必须选一个");
        }
        
        // 检测手机号
        if (Objects.nonNull(user) && Objects.equals(user.getPhone(), merchantSaveRequest.getPhone())) {
            log.error("merchant save error, phone is exit name={}", merchantSaveRequest.getPhone());
            return Triple.of(false, "", "手机号已经存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant save error, franchisee is null name={}, franchiseeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getFranchiseeId());
            return Triple.of(false, "", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.nonNull(merchantLevel) || Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            log.error("merchant save error, merchant level is null name={}, merchantLevelId={}", merchantSaveRequest.getName(), merchantSaveRequest.getMerchantGradeId());
            return Triple.of(false, "", "商户等级不存在");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryByUid(merchantSaveRequest.getChannelEmployeeUid());
            if (Objects.isNull(channelEmployeeVO)) {
                log.error("merchant save error, channel us is not find name={}, channelEmployeeUid={}", merchantSaveRequest.getName(), merchantSaveRequest.getChannelEmployeeUid());
            }
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                log.error("merchant save error, channel us is not find name={}, franchiseeId={}, channelUserFranchiseeId={}, channelEmployeeUid={}", merchantSaveRequest.getName(), merchantSaveRequest.getFranchiseeId(), channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getChannelEmployeeUid());
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
                return Triple.of(false, "", "企业套餐不存在");
            }
            
            Set<Long> memberCardIdList = packageList.stream().map(BatteryMemberCard::getId).collect(Collectors.toSet());
            if (!Objects.equals(packageList.size(), merchantSaveRequest.getEnterprisePackageIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getEnterprisePackageIdList().stream().filter(item -> !memberCardIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant save error,franchiseeId = {}, merchant name={},diff package Id ={}", merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getName(),
                        diffIdList);
                return Triple.of(false, "", "企业套餐不存在");
            }
        }
        
        // 检测绑定的场地数量是否大于20
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            if (merchantSaveRequest.getPlaceIdList().size() > 20) {
                return Triple.of(false, "", "绑定的场地数量不能大于20");
            }
            
            MerchantPlaceQueryModel placeQueryModel = MerchantPlaceQueryModel.builder().idList(merchantSaveRequest.getPlaceIdList())
                    .franchiseeId(merchantSaveRequest.getFranchiseeId()).tenantId(tenantId).build();
            
            List<MerchantPlace> placeList = merchantPlaceService.queryList(placeQueryModel);
            if (ObjectUtils.isEmpty(placeList)) {
                log.error("merchant save error, place is not exist merchant name={}, placeId={}", merchantSaveRequest.getName(), merchantSaveRequest.getPlaceIdList());
                return Triple.of(false, "", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant save error,merchant name={},diff place Id={}", merchantSaveRequest.getName(), diffIdList);
                return Triple.of(false, "", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                log.error("merchant save error,merchant name={}, place is bind placeId={}", merchantSaveRequest.getName(), collect);
                return Triple.of(false, "", "场地已经被绑定");
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
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth())
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.save(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            return enterpriseSaveRes;
        }
        
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantSaveRequest, merchant);
        merchant.setEnterpriseId(enterpriseInfoQuery.getId());
        merchant.setUid(user1.getUid());
        merchant.setCreateTime(timeMillis);
        merchant.setUpdateTime(timeMillis);
        
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
                        .delFlag(MerchantPlaceMap.DEL_NORMAL).type(MerchantPlaceConstant.BIND).merchantMonthSettlement(MerchantPlaceConstant.MONTH_SETTLEMENT_NO).tenantId(tenantId)
                        .createTime(timeMillis).build();
                merchantPlaceBindList.add(merchantPlaceBind);
            });
            
            // 批量保存商户场地映射
            merchantPlaceMapService.batchInsert(merchantPlaceMapList);
            merchantPlaceBindService.batchInsert(merchantPlaceBindList);
        }
        
        // 初始化商户升级配置
        merchantAttrService.initMerchantAttr(merchant.getId(), tenantId);
        
        // 创建商户余额
        MerchantUserAmount merchantUserAmount = new MerchantUserAmount();
        merchantUserAmount.setUid(merchant.getUid());
        merchantUserAmount.setTenantId(tenantId);
        merchantUserAmount.setTotalIncome(BigDecimal.ZERO);
        merchantUserAmount.setBalance(BigDecimal.ZERO);
        merchantUserAmount.setWithdrawAmount(BigDecimal.ZERO);
        merchantUserAmount.setCreateTime(timeMillis);
        merchantUserAmount.setUpdateTime(timeMillis);
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
        
        Merchant merchant = this.merchantMapper.select(merchantSaveRequest.getId());
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            log.error("merchant update error, merchant is not exit id={}, tenantId", merchantSaveRequest.getId(), tenantId);
            return Triple.of(false, "", "商户不存在");
        }
        
        // 判断邀请权限和站点代付权限是否都没有选中
        if (Objects.equals(merchantSaveRequest.getInviteAuth(), MerchantConstant.DISABLE) && Objects.equals(merchantSaveRequest.getEnterprisePackageAuth(), MerchantConstant.DISABLE)) {
            log.error("merchant update error, invite auth and enterprise package auth select at least one, id={}, inviteAuth={}, enterprisePackageAuth={}",
                    merchantSaveRequest.getId(), merchantSaveRequest.getInviteAuth(), merchantSaveRequest.getEnterprisePackageAuth());
            return Triple.of(false, "", "推广权限，站点代付权限，必须选一个");
        }
        
        // 检测商户名称是否存在
        User user = userService.checkMerchantExist(merchantSaveRequest.getName(), merchantSaveRequest.getPhone(), User.TYPE_USER_MERCHANT, tenantId, merchant.getUid());
        if (Objects.nonNull(user) && Objects.equals(user.getName(), merchantSaveRequest.getName())) {
            log.error("merchant update error, name is exit id={}, name={}", merchantSaveRequest.getId(), merchantSaveRequest.getName());
            return Triple.of(false, "", "商户名称已经存在");
        }
        
        // 检测手机号
        if (Objects.nonNull(user) && Objects.equals(user.getPhone(), merchantSaveRequest.getPhone())) {
            log.error("merchant update error, phone is exit id={}, phone={}", merchantSaveRequest.getId(), merchantSaveRequest.getPhone());
            return Triple.of(false, "", "手机号已经存在");
        }
        
        // 检测加盟上是否存在
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            log.error("merchant update error, franchisee is null id={}, franchiseeId={}", merchantSaveRequest.getId(), merchantSaveRequest.getFranchiseeId());
            return Triple.of(false, "", "加盟商不存在");
        }
        
        // 检测商户等级是否存在
        MerchantLevel merchantLevel = merchantLevelService.queryById(merchantSaveRequest.getMerchantGradeId());
        if (Objects.nonNull(merchantLevel) || Objects.equals(merchantLevel.getTenantId(), tenantId)) {
            log.error("merchant update error, merchant level is null id={}, merchantLevelId={}", merchantSaveRequest.getId(), merchantSaveRequest.getMerchantGradeId());
            return Triple.of(false, "", "商户等级不存在");
        }
        
        // 检测渠道员是否存在
        if (Objects.nonNull(merchantSaveRequest.getChannelEmployeeUid())) {
            ChannelEmployeeVO channelEmployeeVO = channelEmployeeService.queryById(merchantSaveRequest.getChannelEmployeeUid());
            if (Objects.isNull(channelEmployeeVO)) {
                log.error("merchant update error, channel us is not find id={}, channelUserId={}", merchantSaveRequest.getId(), merchantSaveRequest.getChannelEmployeeUid());
            }
            if (Objects.nonNull(channelEmployeeVO) && !Objects.equals(channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getFranchiseeId())) {
                log.error("merchant update error, channel us is not find id={}, franchiseeId={}, channelUserFranchiseeId={}, channelUserId={}", merchantSaveRequest.getId(), merchantSaveRequest.getFranchiseeId(), channelEmployeeVO.getFranchiseeId(), merchantSaveRequest.getChannelEmployeeUid());
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
                return Triple.of(false, "", "企业套餐不存在");
            }
            
            Set<Long> memberCardIdList = packageList.stream().map(BatteryMemberCard::getId).collect(Collectors.toSet());
            if (!Objects.equals(packageList.size(), merchantSaveRequest.getEnterprisePackageIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getEnterprisePackageIdList().stream().filter(item -> !memberCardIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant update error,franchiseeId = {}, merchant merchantId={},diff package Id ={}", merchantSaveRequest.getFranchiseeId(), merchantSaveRequest.getId(),
                        diffIdList);
                return Triple.of(false, "", "企业套餐不存在");
            }
        }
        
        // 检测绑定的场地数量是否大于20
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getPlaceIdList())) {
            if (merchantSaveRequest.getPlaceIdList().size() > 20) {
                return Triple.of(false, "", "绑定的场地数量不能大于20");
            }
            
            MerchantPlaceQueryModel placeQueryModel = MerchantPlaceQueryModel.builder().idList(merchantSaveRequest.getPlaceIdList())
                    .franchiseeId(merchantSaveRequest.getFranchiseeId()).tenantId(tenantId).build();
            
            List<MerchantPlace> placeList = merchantPlaceService.queryList(placeQueryModel);
            if (ObjectUtils.isEmpty(placeList)) {
                log.error("merchant update error, place is not exist merchant id={}, placeId={}", merchantSaveRequest.getId(), merchantSaveRequest.getPlaceIdList());
                return Triple.of(false, "", "场地不存在");
            }
            
            List<Long> placeIdList = placeList.stream().map(MerchantPlace::getId).collect(Collectors.toList());
            if (!Objects.equals(placeList.size(), merchantSaveRequest.getPlaceIdList().size())) {
                List<Long> diffIdList = merchantSaveRequest.getPlaceIdList().stream().filter(item -> !placeIdList.contains(item)).collect(Collectors.toList());
                log.error("merchant update error,merchant merchantId={},diff place diffIdList={}", merchantSaveRequest.getId(), diffIdList);
                return Triple.of(false, "", "场地不存在");
            }
            
            MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).merchantId(merchantSaveRequest.getId())
                    .eqFlag(MerchantPlaceMapQueryModel.NO_EQ).build();
            
            // 检测场地是否已经被绑定
            List<MerchantPlaceMap> placeMapList = merchantPlaceMapService.queryList(queryModel);
            if (ObjectUtils.isNotEmpty(placeMapList)) {
                Set<Long> collect = placeMapList.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toSet());
                log.error("merchant update error,merchant id={}, place is bind placeId={}", merchantSaveRequest.getId(), collect);
                return Triple.of(false, "", "场地已经被绑定");
            }
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = new MerchantDeleteCacheDTO();
        long timeMillis = System.currentTimeMillis();
        User updateUser = new User();
        boolean flag = false;
        // 判断手机号和名称是否有变化
        if (!Objects.equals(merchant.getName(), merchantSaveRequest.getName())) {
            flag = true;
            updateUser.setName(merchantSaveRequest.getName());
        }
        
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
        EnterpriseInfoQuery enterpriseInfoQuery = EnterpriseInfoQuery.builder().uid(merchant.getUid()).name(merchantSaveRequest.getName())
                .franchiseeId(merchantSaveRequest.getFranchiseeId()).status(merchantSaveRequest.getEnterprisePackageAuth())
                .purchaseAuthority(merchantSaveRequest.getPurchaseAuthority()).build();
        
        if (ObjectUtils.isNotEmpty(merchantSaveRequest.getEnterprisePackageIdList())) {
            Set<Long> collect = merchantSaveRequest.getEnterprisePackageIdList().stream().collect(Collectors.toSet());
            enterpriseInfoQuery.setPackageIds(collect);
        }
        // 同步企业信息数据
        Triple<Boolean, String, Object> enterpriseSaveRes = enterpriseInfoService.modify(enterpriseInfoQuery);
        if (!enterpriseSaveRes.getLeft()) {
            return enterpriseSaveRes;
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
                        .delFlag(MerchantPlaceMap.DEL_NORMAL).type(MerchantPlaceConstant.BIND).merchantMonthSettlement(MerchantPlaceConstant.MONTH_SETTLEMENT_NO).tenantId(tenantId)
                        .createTime(timeMillis).updateTime(timeMillis).build();
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
        Merchant merchant = this.merchantMapper.select(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            log.error("merchant delete error, merchant is not exit id={}, tenantId", id, tenantId);
            return Triple.of(false, "", "商户不存在");
        }
        
        // 判断商户的余额：t_merchant_user_amount：balance
        
        // 检测商户下的所有场地是否存在绑定电柜
        MerchantPlaceMapQueryModel queryModel = MerchantPlaceMapQueryModel.builder().merchantId(id).eqFlag(MerchantPlaceMapQueryModel.EQ).build();
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            List<Long> placeIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toList());
            MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel = MerchantPlaceCabinetBindQueryModel.builder().placeIdList(placeIdList)
                    .status(MerchantPlaceConstant.BIND).build();
            
            List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryList(placeCabinetBindQueryModel);
            if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
                log.error("merchant delete error, cabinet is bind merchantId={}, cabinetId={}", id, merchantPlaceCabinetBinds);
                return Triple.of(false, "", "请先解绑换电柜后操作");
            }
        }
        
        // 删除企业
        Triple<Boolean, String, Object> triple = enterpriseInfoService.delete(merchant.getEnterpriseId());
        if (!triple.getLeft()) {
            return triple;
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = new MerchantDeleteCacheDTO();
        User oldUser = userService.queryByUidFromCache(merchant.getUid());
        // 删除用户缓存
        merchantDeleteCacheDTO.setDeleteUserFlag(true);
        merchantDeleteCacheDTO.setUser(oldUser);
        
        long timeMillis = System.currentTimeMillis();
        // 删除用户
        userService.removeById(merchant.getUid(), timeMillis);
        
        // todo 让商户登录状态失效
        
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
        
        merchantAttrService.deleteByMerchantId(id);
        
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
        for (Merchant merchant : merchantList) {
            MerchantVO merchantVO = new MerchantVO();
            BeanUtil.copyProperties(merchant, merchantVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(merchant.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                merchantVO.setFranchiseeName(franchisee.getName());
            }
            
            // 商户等级
            
            // 渠道员
            User user = userService.queryByUidFromCache(merchant.getChannelEmployeeUid());
            if (ObjectUtils.isNotEmpty(user)) {
                merchantVO.setChannelName(user.getName());
            }
            
            merchantIdList.add(merchant.getId());
            uidList.add(merchant.getUid());
            
            resList.add(merchantVO);
        }
        
        // 查询商户对应的场地数
        CompletableFuture<Void> placeInfo = CompletableFuture.runAsync(() -> {
            MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().merchantIdList(merchantIdList).build();
            List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(placeMapQueryModel);
            if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
                Map<Long, List<Long>> placeMap = merchantPlaceMaps.stream()
                        .collect(Collectors.groupingBy(MerchantPlaceMap::getMerchantId, Collectors.collectingAndThen(Collectors.toList(), e -> {
                            return e.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toList());
                        })));
                
                resList.forEach(item -> {
                    if (ObjectUtils.isNotEmpty(placeMap.get(item.getId()))) {
                        item.setPlaceCount(placeMap.get(item.getId()).size());
                        item.setPlaceIdList(placeMap.get(item.getId()));
                    }
                });
            }
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query place error!", e);
            return null;
        });
        
        // 查询商户下的用户数
        CompletableFuture<Void> channelUserInfo = CompletableFuture.runAsync(() -> {
            List<Long> collect = merchantIdList.stream().collect(Collectors.toList());
            MerchantJoinRecordQueryMode joinRecordQueryMode = MerchantJoinRecordQueryMode.builder().tenantId(merchantPageRequest.getTenantId()).merchantIdList(collect).status(MerchantJoinRecord.STATUS_SUCCESS).build();
            List<MerchantJoinRecord> merchantJoinRecordList = merchantJoinRecordService.queryList(joinRecordQueryMode);
            if (ObjectUtils.isNotEmpty(merchantJoinRecordList)) {
                Map<Long, List<Long>> userMap = merchantJoinRecordList.stream()
                        .collect(Collectors.groupingBy(MerchantJoinRecord::getMerchantId, Collectors.collectingAndThen(Collectors.toList(), e -> {
                            return e.stream().map(MerchantJoinRecord::getJoinUid).collect(Collectors.toList());
                        })));
            
                resList.forEach(item -> {
                    if (ObjectUtils.isNotEmpty(userMap.get(item.getId()))) {
                        item.setUserCount(userMap.get(item.getId()).size());
                        item.setUserIdList(userMap.get(item.getId()));
                    }
                });
            }
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query user error!", e);
            return null;
        });
        
        // 查询商户的提现余额
        CompletableFuture<Void> merchantUserAmountInfo = CompletableFuture.runAsync(() -> {
            List<Long> collect = uidList.stream().collect(Collectors.toList());
            MerchantUserAmountQueryMode joinRecordQueryMode = MerchantUserAmountQueryMode.builder().uidList(collect).tenantId(merchantPageRequest.getTenantId()).build();
            List<MerchantUserAmount> merchantJoinRecordList = merchantUserAmountService.queryList(joinRecordQueryMode);
            if (ObjectUtils.isNotEmpty(merchantJoinRecordList)) {
                Map<Long, MerchantUserAmount> userAmountMap = merchantJoinRecordList.stream()
                        .collect(Collectors.toMap(MerchantUserAmount::getUid, Function.identity(), (key1, key2) -> key2));
            
                resList.forEach(item -> {
                    MerchantUserAmount merchantUserAmount = userAmountMap.get(item.getId());
                    if (ObjectUtils.isNotEmpty(merchantUserAmount)) {
                        item.setWithdrawAmount(merchantUserAmount.getWithdrawAmount());
                        item.setBalance(merchantUserAmount.getBalance());
                    }
                });
            }
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT QUERY ERROR! query user amount error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(placeInfo, channelUserInfo, merchantUserAmountInfo);
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
        
        Merchant merchant = merchantMapper.select(id);
        if (Objects.isNull(merchant) || !Objects.equals(merchant.getTenantId(), tenantId)) {
            return Triple.of(false, "", "商户不存在");
        }
    
        MerchantUpdateVO vo = new MerchantUpdateVO();
        BeanUtils.copyProperties(merchant, vo);
        
        // 查询企业套餐
        List<Long> packageIdList = enterprisePackageService.selectByEnterpriseId(id);
        vo.setEnterprisePackageIdList(packageIdList);
        
        Set<Long> merchantIdList = new HashSet<>();
        merchantIdList.add(id);
        MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().merchantIdList(merchantIdList).build();
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(placeMapQueryModel);
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            List<Long> placeIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getPlaceId).collect(Collectors.toList());
            vo.setPlaceIdList(placeIdList);
        }
        
        return Triple.of(true, "", vo);
    }
    
    @Override
    public Merchant queryFromCacheById(Long id) {
        Merchant merchant = null;
        merchant = redisService.getWithHash(CacheConstant.CACHE_MERCHANT + id, Merchant.class);
        if (Objects.isNull(merchant)) {
            merchant = merchantMapper.select(id);
            if (Objects.nonNull(merchant)) {
                redisService.saveWithHash(CacheConstant.CACHE_MERCHANT + id, merchant);
            }
        }
        
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
    public List<MerchantVO> getDict(MerchantPageRequest merchantPageRequest) {
        List<MerchantVO> resList = new ArrayList<>();
        MerchantQueryModel queryModel = new MerchantQueryModel();
        BeanUtils.copyProperties(merchantPageRequest, queryModel);
        List<Merchant> merchantList = this.merchantMapper.selectListByPage(queryModel);
        
        if (ObjectUtils.isEmpty(merchantList)) {
            return Collections.EMPTY_LIST;
        }
        
        merchantList.stream().forEach(item -> {
            MerchantVO vo = new MerchantVO();
            BeanUtils.copyProperties(item, vo);
            resList.add(vo);
        });
        
        return resList;
    }
    
    @Slave
    @Override
    public Merchant queryByUid(Long uid) {
        
        return merchantMapper.selectByUid(uid);
    }
    
    /**	 小程序：员工添加下拉框场地选择
     * @param uid
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceUserVO> queryPlaceListByUid(Long uid) {
        Merchant merchant = merchantMapper.selectByUid(uid);
        
        if (Objects.isNull(merchant)) {
            log.error("merchant query place list error, merchant is not find, uid={}", uid);
            return Collections.EMPTY_LIST;
        }
    
        List<MerchantPlaceUserVO> merchantPlaceUserVOS = merchantPlaceMapService.queryListByMerchantId(merchant.getId());
        // 查询是否已经绑定了员工
        
        return merchantPlaceUserVOS;
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
    
}
