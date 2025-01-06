package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.MerchantOverdueUserCountBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.mapper.merchant.MerchantJoinRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantAllPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantJoinRecordPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantJoinScanRequest;
import com.xiliulou.electricity.request.merchant.MerchantScanCodeRecordPageRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.QrCodeUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;
import com.xiliulou.electricity.vo.merchant.MerchantJoinRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantJoinUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantScanCodeRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 18:04:22
 */
@Slf4j
@Service
public class MerchantJoinRecordServiceImpl implements MerchantJoinRecordService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private MerchantAttrService merchantAttrService;
    
    @Resource
    private MerchantJoinRecordMapper merchantJoinRecordMapper;
    
    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private ShareActivityService shareActivityService;
    
    @Resource
    private ShareMoneyActivityService shareMoneyActivityService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private AssertPermissionService assertPermissionService;
    
    @Override
    public R joinScanCode(MerchantJoinScanRequest request) {
        Long joinUid = SecurityUtils.getUid();
        
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid, "1", 2000L, false)) {
            return R.fail(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("MERCHANT JOIN WARN! not found tenant, tenantId={}", TenantContextHolder.getTenantId());
                return R.fail("ELECTRICITY.00101", "找不到租户");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(joinUid);
            if (Objects.isNull(userInfo)) {
                log.warn("MERCHANT JOIN WARN! not found userInfo, joinUid={}", joinUid);
                return R.fail(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("MERCHANT JOIN WARN! user usable, joinUid={}", joinUid);
                return R.fail(false, "120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(joinUid);
            if (Objects.isNull(userInfoExtra)) {
                log.warn("MERCHANT JOIN WARN! Not found userInfoExtra, joinUid={}", joinUid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            // 530活动互斥判断
            R canJoinActivity = canJoinActivity(userInfo, userInfoExtra, null, null);
            if (!canJoinActivity.isSuccess()) {
                return canJoinActivity;
            }
            
            // 已过保护期+已参与状态 的记录，需要更新为已失效，才能再扫码
            MerchantJoinRecord needUpdatedToInvalidRecord = null;
            List<MerchantJoinRecord> joinRecordList = this.listByJoinUidAndStatus(joinUid, List.of(MerchantJoinRecordConstant.STATUS_INIT));
            
            if (CollectionUtils.isNotEmpty(joinRecordList)) {
                MerchantJoinRecord joinRecord = joinRecordList.get(NumberConstant.ZERO);
                if (Objects.nonNull(joinRecord)) {
                    Long protectionTime = joinRecord.getProtectionTime();
                    // 没有保护期
                    if (Objects.isNull(protectionTime) || Objects.equals(protectionTime, NumberConstant.ZERO_L)) {
                        needUpdatedToInvalidRecord = joinRecord;
                    } else {
                        //未过保护期
                        if (protectionTime >= System.currentTimeMillis()) {
                            log.warn("MERCHANT JOIN WARN! in protectionTime, merchantId={}, inviterUid={}, joinUid={}", joinRecord.getMerchantId(), joinRecord.getInviterUid(),
                                    joinUid);
                            
                            return R.fail(false, "120104", "商户保护期内，请稍后再试");
                        } else {
                            // 已过保护期
                            needUpdatedToInvalidRecord = joinRecord;
                        }
                    }
                }
            }
            
            // 解析code
            String code = request.getCode();
            String decrypt = null;
            try {
                decrypt = QrCodeUtils.codeDeCoder(code);
            } catch (Exception e) {
                log.error("MERCHANT JOIN ERROR! decode fail, joinUid={}, code={}", joinUid, code, e);
            }
            
            if (StringUtils.isBlank(decrypt)) {
                log.warn("MERCHANT JOIN WARN! merchant code decrypt error,code={}, joinUid={}", code, joinUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            log.info("MERCHANT JOIN INFO! joinScanCode decrypt={}", decrypt);
            
            String[] split = decrypt.split(String.valueOf(StrUtil.C_COLON));
            if (ArrayUtils.isEmpty(split) || split.length != NumberConstant.THREE) {
                log.warn("MERCHANT JOIN WARN! illegal code! code={}, joinUid={}", code, joinUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            String merchantIdStr = split[NumberConstant.ZERO];
            String inviterUidStr = split[NumberConstant.ONE];
            String inviterTypeStr = split[NumberConstant.TWO];
            if (StringUtils.isBlank(merchantIdStr) || StringUtils.isBlank(inviterUidStr) || StringUtils.isBlank(inviterTypeStr)) {
                log.warn("MERCHANT JOIN WARN! illegal code! code={}, joinUid={}", code, joinUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            Long merchantId;
            Long inviterUid;
            Integer inviterType;
            try {
                merchantId = Long.parseLong(merchantIdStr);
                inviterUid = Long.parseLong(inviterUidStr);
                inviterType = Integer.parseInt(inviterTypeStr);
            } catch (NumberFormatException e) {
                log.error("MERCHANT JOIN ERROR! Invalid format, joinUid={}, merchantIdStr={}, inviterUidStr={}, inviterTypeStr={}", joinUid, merchantIdStr, inviterUidStr,
                        inviterTypeStr, e);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 判断商户是否存在或被禁用
            Merchant merchant = merchantService.queryByIdFromCache(merchantId);
            if (Objects.isNull(merchant)) {
                log.warn("MERCHANT JOIN WARN! not found merchant, merchantId={}", merchantId);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            if (Objects.equals(merchant.getStatus(), MerchantConstant.DISABLE)) {
                log.warn("MERCHANT JOIN WARN! merchant disable, merchantId={}", merchantId);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 判断邀请人是否存在或被禁用
            User inviterUser = userService.queryByUidFromDB(inviterUid);
            if (Objects.isNull(inviterUser)) {
                log.warn("MERCHANT JOIN WARN! not found inviterUser, inviterUid={}", inviterUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            if (inviterUser.isLock()) {
                log.warn("MERCHANT JOIN WARN! inviterUser locked, inviterUid={}", inviterUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 扫自己码
            if (Objects.equals(userInfo.getUid(), inviterUid)) {
                log.warn("MERCHANT JOIN ERROR! illegal operate! Can not scan own QR, inviterUid={}, joinUid={}", inviterUid, joinUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 邀请人类型
            if (!Objects.equals(inviterType, MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF) && !Objects.equals(inviterType,
                    MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE)) {
                log.warn("MERCHANT JOIN WARN! illegal operate! Inviter is illegal, inviterUid={}, inviterType={}, joinUid={}", inviterUid, inviterType, joinUid);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 获取商户保护期和有效期
            MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(merchant.getFranchiseeId());
            if (Objects.isNull(merchantAttr)) {
                log.warn("MERCHANT JOIN WARN! not found merchantAttr, merchantId={}", merchantId);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 渠道员uid
            Long channelEmployeeUid = merchant.getChannelEmployeeUid();
            
            // 获取场地员工所绑定的场地
            Long placeId = Optional.ofNullable(merchantEmployeeService.queryMerchantEmployeeByUid(inviterUid)).orElse(new MerchantEmployeeVO()).getPlaceId();
            
            // 保存参与记录
            MerchantJoinRecord record = this.assembleRecord(merchantId, inviterUid, inviterType, joinUid, channelEmployeeUid, placeId, merchantAttr, tenant.getId(),
                    merchant.getFranchiseeId());
            Integer result = merchantJoinRecordMapper.insertOne(record);
            
            // 将旧的已参与记录改为已失效
            if (Objects.nonNull(needUpdatedToInvalidRecord) && Objects.nonNull(result)) {
                this.updateStatusById(needUpdatedToInvalidRecord.getId(), MerchantJoinRecordConstant.STATUS_INVALID, System.currentTimeMillis());
            }
            
            // 530会员扩展表更新最新参与活动类型
            userInfoExtraService.updateByUid(UserInfoExtra.builder().uid(joinUid).latestActivitySource(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode()).build());
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid);
        }
    }
    
    @Override
    public Integer insertOne(MerchantJoinRecord record) {
        return merchantJoinRecordMapper.insertOne(record);
    }
    
    private MerchantJoinRecord assembleRecord(Long merchantId, Long inviterUid, Integer inviterType, Long joinUid, Long channelEmployeeUid, Long placeId, MerchantAttr merchantAttr,
            Integer tenantId, Long franchiseeId) {
        long nowTime = System.currentTimeMillis();
        Integer protectionTime = merchantAttr.getInvitationProtectionTime();
        Integer protectionTimeUnit = merchantAttr.getProtectionTimeUnit();
        Integer validTime = merchantAttr.getInvitationValidTime();
        Integer validTimeUnit = merchantAttr.getValidTimeUnit();
        
        // 保护期过期时间
        long protectionExpireTime = nowTime;
        //分钟转毫秒
        if (Objects.equals(protectionTimeUnit, CommonConstant.TIME_UNIT_MINUTES)) {
            protectionExpireTime += protectionTime * TimeConstant.MINUTE_MILLISECOND;
        }
        //小时转毫秒
        if (Objects.equals(protectionTimeUnit, CommonConstant.TIME_UNIT_HOURS)) {
            protectionExpireTime += protectionTime * TimeConstant.HOURS_MILLISECOND;
        }
        
        // 参与有效期过期时间
        long expiredTime = nowTime;
        //分钟转毫秒
        if (Objects.equals(validTimeUnit, CommonConstant.TIME_UNIT_MINUTES)) {
            expiredTime += validTime * TimeConstant.MINUTE_MILLISECOND;
        }
        //小时转毫秒
        if (Objects.equals(validTimeUnit, CommonConstant.TIME_UNIT_HOURS)) {
            expiredTime += validTime * TimeConstant.HOURS_MILLISECOND;
        }
    
        // 创建日期
        String monthDate = DateUtil.format(new Date(), DateFormatConstant.MONTH_DAY_DATE_FORMAT);
        
        // 生成参与记录
        return MerchantJoinRecord.builder().merchantId(merchantId).channelEmployeeUid(channelEmployeeUid).placeId(placeId).inviterUid(inviterUid).inviterType(inviterType)
                .joinUid(joinUid).startTime(nowTime).expiredTime(expiredTime).status(MerchantJoinRecordConstant.STATUS_INIT).protectionTime(protectionExpireTime)
                .protectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_NORMAL).delFlag(NumberConstant.ZERO).createTime(nowTime).updateTime(nowTime).tenantId(tenantId)
                .modifyInviter(MerchantJoinRecordConstant.MODIFY_INVITER_NO).franchiseeId(franchiseeId).monthDate(monthDate).build();
    }
    
    @Slave
    @Override
    public MerchantJoinRecord queryByMerchantIdAndJoinUid(Long merchantId, Long joinUid) {
        return merchantJoinRecordMapper.selectByMerchantIdAndJoinUid(merchantId, joinUid);
    }
    
    @Slave
    @Override
    public MerchantJoinRecord queryByJoinUid(Long joinUid) {
        return merchantJoinRecordMapper.selectByJoinUid(joinUid);
    }
    
    @Override
    public Integer updateStatus(MerchantJoinRecordQueryModel queryModel) {
        return merchantJoinRecordMapper.updateStatus(queryModel);
    }
    
    @Slave
    @Override
    public Integer countByMerchantIdAndStatus(Long merchantId, Integer status) {
        return merchantJoinRecordMapper.countListByMerchantIdAndStatus(merchantId, status);
    }
    
    
    /**
     * 530需求废弃该接口，废弃字段：MerchantJoinRecord#protectionStatus，改为实时获取保护期过期时间
     */
    @Deprecated
    @Override
    public void handelProtectionStatus() {
        MerchantJoinRecord protectionJoinRecord = new MerchantJoinRecord();
        protectionJoinRecord.setProtectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_EXPIRED);
        protectionJoinRecord.setUpdateTime(System.currentTimeMillis());
        merchantJoinRecordMapper.updateProtectionExpired(protectionJoinRecord);
    }
    
    @Override
    public void handelExpiredStatus() {
        MerchantJoinRecord merchantJoinRecord = new MerchantJoinRecord();
        merchantJoinRecord.setStatus(MerchantJoinRecordConstant.STATUS_EXPIRED);
        merchantJoinRecord.setUpdateTime(System.currentTimeMillis());
        merchantJoinRecordMapper.updateExpired(merchantJoinRecord);
    }
    
    @Override
    public Integer updateById(MerchantJoinRecord record) {
        return merchantJoinRecordMapper.updateById(record);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecord> queryList(MerchantJoinRecordQueryMode joinRecordQueryMode) {
        return merchantJoinRecordMapper.selectList(joinRecordQueryMode);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantJoinRecordPageRequest merchantJoinRecordPageRequest) {
        MerchantJoinRecordQueryMode queryMode = new MerchantJoinRecordQueryMode();
        BeanUtils.copyProperties(merchantJoinRecordPageRequest, queryMode);
        
        return merchantJoinRecordMapper.countTotal(queryMode);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecordVO> listByPage(MerchantJoinRecordPageRequest merchantJoinRecordPageRequest) {
        MerchantJoinRecordQueryMode queryMode = new MerchantJoinRecordQueryMode();
        BeanUtils.copyProperties(merchantJoinRecordPageRequest, queryMode);
        
        List<MerchantJoinRecord> list = merchantJoinRecordMapper.selectListByPage(queryMode);
        if (ObjectUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        List<MerchantJoinRecordVO> voList = new ArrayList<>();
        for (MerchantJoinRecord merchantJoinRecord : list) {
            MerchantJoinRecordVO vo = new MerchantJoinRecordVO();
            BeanUtils.copyProperties(merchantJoinRecord, vo);
            
            // 查询用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(merchantJoinRecord.getJoinUid());
            if (Objects.nonNull(userInfo)) {
                vo.setUserName(userInfo.getName());
                vo.setPhone(userInfo.getPhone());
            }
            
            // 查询商户名称
            Merchant merchant = merchantService.queryByIdFromCache(merchantJoinRecord.getMerchantId());
            if (Objects.nonNull(merchant)) {
                vo.setMerchantName(merchant.getName());
            }
            
            // 加盟商名称
            Long franchiseeId = merchantJoinRecord.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                vo.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            
            if (Objects.isNull(merchantJoinRecord.getSuccessTime()) || Objects.equals(merchantJoinRecord.getSuccessTime(), NumberConstant.ZERO_L)) {
                vo.setSuccessTime(merchantJoinRecord.getCreateTime());
            }
            
            voList.add(vo);
        }
        
        return voList;
    }
    
    @Slave
    @Override
    public Integer countByCondition(MerchantPromotionScanCodeQueryModel queryModel) {
        return merchantJoinRecordMapper.countByCondition(queryModel);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecordVO> countByMerchantIdList(MerchantJoinRecordQueryMode joinRecordQueryMode) {
        return merchantJoinRecordMapper.countByMerchantIdList(joinRecordQueryMode);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecord> selectPromotionDataDetail(MerchantPromotionDataDetailQueryModel queryModel) {
        return merchantJoinRecordMapper.selectListPromotionDataDetail(queryModel);
    }
    
    @Slave
    @Override
    public List<MerchantJoinUserVO> selectJoinUserList(MerchantJoinUserQueryMode merchantJoinUserQueryMode) {
        //获取商户uid, 并检查当前商户是否存在且可用
        Merchant merchant = merchantService.queryByUid(merchantJoinUserQueryMode.getMerchantUid());
        if (Objects.isNull(merchant)) {
            return Collections.emptyList();
        }
        merchantJoinUserQueryMode.setMerchantId(merchant.getId());
        
        //计算当前日期后三天的时间毫秒数
        Long currentTime = System.currentTimeMillis();
        Long expiredTime = currentTime + MerchantConstant.MERCHANT_JOIN_USER_PACKAGE_EXPIRE_DAY * 24 * 60 * 60 * 1000L;
        /*if(MerchantConstant.MERCHANT_JOIN_USER_TYPE_NORMAL.equals(merchantJoinUserQueryMode.getType())){
            merchantJoinUserQueryMode.setExpireTime(expiredTime);
        } else if (MerchantConstant.MERCHANT_JOIN_USER_TYPE_OVERDUE_SOON.equals(merchantJoinUserQueryMode.getType())) {

        } else if (MerchantConstant.MERCHANT_JOIN_USER_TYPE_EXPIRED.equals(merchantJoinUserQueryMode.getType())) {

        }*/
        merchantJoinUserQueryMode.setCurrentTime(currentTime);
        merchantJoinUserQueryMode.setExpireTime(expiredTime);
        
        //获取当前商户下的用户列表信息
        List<MerchantJoinUserVO> merchantJoinUserVOS = merchantJoinRecordMapper.selectJoinUserList(merchantJoinUserQueryMode);
        
        if (CollectionUtils.isEmpty(merchantJoinUserVOS)) {
            return Collections.emptyList();
        }
        
        merchantJoinUserVOS.forEach(merchantJoinUserVO -> {
            //对电话号码中见四位做脱敏处理
            String phone = merchantJoinUserVO.getPhone();
            merchantJoinUserVO.setPhone(PhoneUtils.mobileEncrypt(phone));
            
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
    
    @Slave
    @Override
    public boolean existMerchantInviterData(Integer inviterType, Long inviterUid, Integer tenantId) {
        Integer existInviterData = merchantJoinRecordMapper.existMerchantInviterData(inviterType, inviterUid, tenantId);
        if (Objects.nonNull(existInviterData)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Slave
    @Override
    public boolean existMerchantAllInviterData(Long merchantId, Integer tenantId) {
        Integer existInviterData = merchantJoinRecordMapper.existMerchantAllInviterData(merchantId, tenantId);
        if (Objects.nonNull(existInviterData)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    @Slave
    public Integer countEmployeeScanCodeNum(List<Long> uidList, Long startTime, Long endTime, Integer status, Integer tenantId, Long channelEmployeeUid) {
        return merchantJoinRecordMapper.countEmployeeScanCodeNum(uidList, startTime, endTime, status, tenantId, channelEmployeeUid);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecord> selectListAllPromotionDataDetail(MerchantAllPromotionDataDetailQueryModel queryModel) {
        return merchantJoinRecordMapper.selectListAllPromotionDataDetail(queryModel);
    }
    
    @Slave
    @Override
    public List<MerchantJoinRecord> listByJoinUidAndStatus(Long joinUid, List<Integer> statusList) {
        return merchantJoinRecordMapper.selectListByJoinUidAndStatus(joinUid, statusList);
    }
    
    @Override
    public Integer updateStatusById(Long id, Integer status, long updateTime) {
        return merchantJoinRecordMapper.updateStatusById(id, status, updateTime);
    }
    
    @Slave
    @Override
    public MerchantJoinRecord querySuccessRecordByJoinUid(Long uid, Integer tenantId) {
        return merchantJoinRecordMapper.selectSuccessRecordByJoinUid(uid, tenantId);
    }
    
    public Integer removeByJoinUid(Long joinUid, Long updateTime, Integer tenantId) {
        return merchantJoinRecordMapper.removeByJoinUid(joinUid, updateTime, tenantId);
    }
    
    /**
     * 查询扫码人数成功的数量
     *
     * @param employeeIds
     * @param startTime
     * @param endTime
     * @param status
     * @param tenantId
     * @param uid
     * @return
     */
    @Slave
    @Override
    public Integer countEmployeeScanCodeSuccessNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid) {
        return merchantJoinRecordMapper.countEmployeeScanCodeSuccessNum(employeeIds, startTime, endTime, status, tenantId, uid);
    }
    
    /**
     * 查询扫码人数成功的数量
     *
     * @param scanCodeQueryModel
     * @return
     */
    @Slave
    @Override
    public Integer countSuccessByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel) {
        return merchantJoinRecordMapper.countSuccessByCondition(scanCodeQueryModel);
    }
    
    /**
     * 活动互斥判断: 1.是否成功参与过活动 2.平台用户，是否购买过套餐 3.平台用户，是否实名认证
     *
     * @param userInfo
     * @param shareActivityId   邀请返券或邀请返现活动的id（其它活动该参数为null）
     * @param shareActivityType 邀请返券-1,邀请返现-2（其它活动该参数为null）
     */
    @Override
    public R canJoinActivity(UserInfo userInfo, UserInfoExtra userInfoExtra, Integer shareActivityId, Integer shareActivityType) {
        Long uid = userInfo.getUid();
        
        // 是否成功参与过活动
        Integer activitySource = userInfoExtra.getActivitySource();
        if (Objects.nonNull(activitySource) && activitySource > NumberConstant.ZERO) {
            String activityName = StringUtils.EMPTY;
            switch (activitySource) {
                case 1:
                    activityName = UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getDesc();
                    break;
                case 2:
                    activityName = UserInfoActivitySourceEnum.SUCCESS_SHARE_MONEY_ACTIVITY.getDesc();
                    break;
                case 3:
                    activityName = UserInfoActivitySourceEnum.SUCCESS_INVITATION_ACTIVITY.getDesc();
                    break;
                case 4:
                    activityName = UserInfoActivitySourceEnum.SUCCESS_CHANNEL_ACTIVITY.getDesc();
                    break;
                case 5:
                    activityName = UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getDesc();
                    break;
                default:
                    break;
            }
            
            return R.fail("120121", "此活动仅限新用户参加，您已成功参与过" + activityName + "活动，无法参与，感谢您的支持");
        }
        
        // 平台用户，需判断是否购买过套餐
        if (userInfo.getPayCount() > NumberConstant.ZERO) {
            log.warn("JOIN ACTIVITY WARN! Is not new user, payCount > 0, joinUid={}", uid);
            return R.fail("120122", "此活动仅限新用户参加，您已是平台用户无法参与，感谢您的支持");
        }
        
        // 平台用户，如果邀请返券或邀请返现的成功标准为实名认证，那么需判断是否已实名认证
        if (Objects.nonNull(shareActivityId) && Objects.nonNull(shareActivityType)) {
            boolean authFlag = false;
            
            if (Objects.equals(shareActivityType, UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getCode())) {
                // 邀请返券
                ShareActivity shareActivity = shareActivityService.queryByIdFromCache(shareActivityId);
                if (Objects.nonNull(shareActivity) && Objects.equals(shareActivity.getInvitationCriteria(), ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode())) {
                    authFlag = true;
                }
            } else {
                // 邀请返现
                ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(shareActivityId);
                if (Objects.nonNull(shareMoneyActivity) && Objects.equals(shareMoneyActivity.getInvitationCriteria(), ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode())) {
                    authFlag = true;
                }
            }
            
            // 实名认证标准，并且userInfo已实名认证
            if (authFlag && Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("JOIN ACTIVITY WARN! Is not new user, already auth, joinUid={}", uid);
                return R.fail("120122", "此活动仅限新用户参加，您已是平台用户无法参与，感谢您的支持");
            }
        }
        
        return R.ok();
    }
    
    @Override
    @Slave
    public Integer countScanCodeRecord(MerchantScanCodeRecordPageRequest request) {
        if (StrUtil.isNotBlank(request.getPhone())) {
            if (!PhoneUtils.isChinaPhoneNum(request.getPhone())) {
                return NumberConstant.ZERO;
            }
            List<UserInfo> userList = userInfoService.queryListUserInfoByPhone(request.getPhone());
            List<Long> uids = userList.stream().map(UserInfo::getUid).collect(Collectors.toList());
            request.setUids(uids);
        }
        if (Objects.nonNull(request.getBuyTimeStart()) && Objects.nonNull(request.getBuyTimeEnd())) {
            List<ElectricityMemberCardOrder> orderList = electricityMemberCardOrderService.queryListByCreateTime(request.getBuyTimeStart(), request.getBuyTimeEnd());
            List<String> orderIdList = orderList.stream().map(ElectricityMemberCardOrder::getOrderId).collect(Collectors.toList());
            request.setOrderIdList(orderIdList);
        }
        
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return NumberConstant.ZERO;
        }
        request.setFranchiseeIds(pair.getRight());
        
        return merchantJoinRecordMapper.countScanCodeRecord(request);
    }
    
    @Slave
    @Override
    public MerchantJoinRecord queryRemoveSuccessRecord(Long joinUid, Long inviterUid, Integer tenantId) {
        return merchantJoinRecordMapper.selectRemoveSuccessRecord(joinUid, inviterUid, tenantId);
    }

    @Override
    @Slave
    public List<MerchantOverdueUserCountBO> listOverdueUserCount(Set<Long> merchantIdList, long currentTime) {
        return merchantJoinRecordMapper.selectListOverdueCount(merchantIdList, currentTime);
    }

    @Override
    public List<MerchantScanCodeRecordVO> listScanCodeRecordPage(MerchantScanCodeRecordPageRequest request) {
        if (StrUtil.isNotBlank(request.getPhone())) {
            if (!PhoneUtils.isChinaPhoneNum(request.getPhone())) {
                return new ArrayList<>();
            }
            List<UserInfo> userList = userInfoService.queryListUserInfoByPhone(request.getPhone());
            List<Long> uids = userList.stream().map(UserInfo::getUid).collect(Collectors.toList());
            request.setUids(uids);
        }
        
        if (Objects.nonNull(request.getBuyTimeStart()) && Objects.nonNull(request.getBuyTimeEnd())) {
            List<ElectricityMemberCardOrder> orderList = electricityMemberCardOrderService.queryListByCreateTime(request.getBuyTimeStart(), request.getBuyTimeEnd());
            List<String> orderIdList = orderList.stream().map(ElectricityMemberCardOrder::getOrderId).collect(Collectors.toList());
            request.setOrderIdList(orderIdList);
        }
        
        // 加盟商权限
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        request.setFranchiseeIds(pair.getRight());
        
        List<MerchantJoinRecord> list = merchantJoinRecordMapper.selectListScanCodeRecordPage(request);
        if (ObjectUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        // 提前查询订单信息和用户信息
        List<String> orderIdList = list.stream().map(MerchantJoinRecord::getOrderId).collect(Collectors.toList());
        Map<String, ElectricityMemberCardOrder> memberCardOrderMap = new HashMap<>();
        if (CollUtil.isNotEmpty(orderIdList)) {
            List<ElectricityMemberCardOrder> orderList = electricityMemberCardOrderService.queryListByOrderIds(orderIdList);
            memberCardOrderMap = orderList.stream().collect(Collectors.toMap(ElectricityMemberCardOrder::getOrderId, Function.identity(), (k1, k2) -> k1));
        }
        
        List<Long> uidList = list.parallelStream().map(MerchantJoinRecord::getJoinUid).collect(Collectors.toList());
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        if (CollUtil.isNotEmpty(uidList)) {
            List<UserInfo> userInfo = userInfoService.listByUidList(uidList);
            userInfoMap = userInfo.stream().collect(Collectors.toMap(UserInfo::getUid, Function.identity(), (k1, k2) -> k1));
        }
        
        Map<String, ElectricityMemberCardOrder> finalMemberCardOrderMap = memberCardOrderMap;
        Map<Long, UserInfo> finalUserInfoMap = userInfoMap;
        return list.parallelStream().map(e -> {
            MerchantScanCodeRecordVO vo = BeanUtil.copyProperties(e, MerchantScanCodeRecordVO.class);
            // 查询商户名称
            Merchant merchant = merchantService.queryByIdFromCache(e.getMerchantId());
            if (Objects.nonNull(merchant)) {
                vo.setMerchantName(merchant.getName());
            }
            
            ElectricityMemberCardOrder cardOrder = finalMemberCardOrderMap.get(e.getOrderId());
            if (Objects.nonNull(cardOrder)) {
                vo.setCardName(cardOrder.getCardName());
                vo.setOrderBuyTime(cardOrder.getCreateTime());
            }
            if (Objects.nonNull(e.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    vo.setFranchiseeId(e.getFranchiseeId());
                    vo.setFranchiseeName(franchisee.getName());
                }
            }
            
            // 查询用户信息
            UserInfo userInfo = finalUserInfoMap.get(e.getJoinUid());
            if (Objects.isNull(userInfo)) {
                log.warn("queryScanCodeRecordPage.userInfo is null,uid is {}", e.getJoinUid());
                return vo;
            }
            vo.setUserName(userInfo.getName());
            vo.setPhone(userInfo.getPhone());
            vo.setDelFlag(userInfo.getDelFlag());
            vo.setDelTime(Objects.equals(userInfo.getDelFlag(), UserInfo.DEL_DEL) ? userInfo.getUpdateTime() : null);
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    
    @Override
    @Slave
    public List<MerchantStatisticsUserVO> listSuccessJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel) {
        return merchantJoinRecordMapper.selectListSuccessJoinNumByCondition(scanCodeQueryModel);
    }
    
    @Override
    @Slave
    public List<MerchantStatisticsUserVO> listEmployeeSuccessJoinNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid) {
        return merchantJoinRecordMapper.selectListEmployeeSuccessJoinNum(employeeIds, startTime, endTime, status, tenantId, uid);
    }
    
    @Override
    @Slave
    public List<MerchantStatisticsUserVO> listJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel) {
        return merchantJoinRecordMapper.selectListJoinNumByCondition(scanCodeQueryModel);
    }
    
    @Override
    @Slave
    public List<MerchantStatisticsUserVO> listEmployeeJoinNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid) {
        return merchantJoinRecordMapper.selectListEmployeeJoinNum(employeeIds, startTime, endTime, status, tenantId, uid);
    }
}
