package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.PhoneUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantJoinRecordConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantJoinRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantAllPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantJoinRecordPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantJoinScanRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Override
    public R joinScanCode(MerchantJoinScanRequest request) {
        Long joinUid = SecurityUtils.getUid();
        
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid, "1", 2000L, false)) {
            return R.fail(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.error("MERCHANT JOIN ERROR! not found tenant, tenantId={}", TenantContextHolder.getTenantId());
                return R.fail("ELECTRICITY.00101", "找不到租户");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(joinUid);
            if (Objects.isNull(userInfo)) {
                log.error("MERCHANT JOIN ERROR! not found userInfo, joinUid={}", joinUid);
                return R.fail(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            if (userInfo.getPayCount() > NumberConstant.ZERO) {
                log.info("MERCHANT JOIN ERROR! Exist package pay count for current user, joinUid = {}", joinUid);
                return R.fail("120106", "您已是会员用户,无法参加商户活动");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("MERCHANT JOIN ERROR! user usable, joinUid={}", joinUid);
                return R.fail(false, "120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 已过保护期+已参与状态 的记录，需要更新为已失效，才能再扫码
            MerchantJoinRecord needUpdatedToInvalidRecord = null;
            List<MerchantJoinRecord> joinRecordList = this.listByJoinUidAndStatus(joinUid, List.of(MerchantJoinRecordConstant.STATUS_INIT));
            if (CollectionUtils.isNotEmpty(joinRecordList)) {
                for (MerchantJoinRecord joinRecord : joinRecordList) {
                    // 有已参与记录
                    if (Objects.equals(joinRecord.getStatus(), MerchantJoinRecordConstant.STATUS_INIT)) {
                        
                        //未过有效期
                        if (Objects.equals(joinRecord.getProtectionStatus(), MerchantJoinRecordConstant.PROTECTION_STATUS_NORMAL)) {
                            log.error("MERCHANT JOIN ERROR! in protectionTime, merchantId={}, inviterUid={}, joinUid={}", joinRecord.getMerchantId(), joinRecord.getInviterUid(),
                                    joinUid);
                            
                            return R.fail(false, "120104", "商户保护期内，请稍后再试");
                        } else {
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
                log.error("MERCHANT JOIN ERROR! decode fail, joinUid={}, code={}", joinUid, code);
            }
            
            if (StringUtils.isBlank(decrypt)) {
                log.error("MERCHANT JOIN ERROR! merchant code decrypt error,code={}, joinUid={}", code, joinUid);
                return R.fail("100463", "二维码已失效");
            }
            
            log.info("MERCHANT JOIN INFO! joinScanCode decrypt={}", decrypt);
            
            String[] split = decrypt.split(String.valueOf(StrUtil.C_COLON));
            if (ArrayUtils.isEmpty(split) || split.length != NumberConstant.THREE) {
                log.error("MERCHANT JOIN ERROR! illegal code! code={}, joinUid={}", code, joinUid);
                return R.fail("100463", "二维码已失效");
            }
            
            String merchantIdStr = split[NumberConstant.ZERO];
            String inviterUidStr = split[NumberConstant.ONE];
            String inviterTypeStr = split[NumberConstant.TWO];
            if (StringUtils.isBlank(merchantIdStr) || StringUtils.isBlank(inviterUidStr) || StringUtils.isBlank(inviterTypeStr)) {
                log.error("MERCHANT JOIN ERROR! illegal code! code={}, joinUid={}", code, joinUid);
                return R.fail("100463", "二维码已失效");
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
                        inviterTypeStr);
                return R.fail("100463", "二维码已失效");
            }
            
            // 判断商户是否存在或被禁用
            Merchant merchant = merchantService.queryByIdFromCache(merchantId);
            if (Objects.isNull(merchant)) {
                log.error("MERCHANT JOIN ERROR! not found merchant, merchantId={}", merchantId);
                return R.fail("100463", "二维码已失效");
            }
            
            if (Objects.equals(merchant.getStatus(), MerchantConstant.DISABLE)) {
                log.error("MERCHANT JOIN ERROR! merchant disable, merchantId={}", merchantId);
                return R.fail("120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 判断邀请人是否存在或被禁用
            User inviterUser = userService.queryByUidFromCache(inviterUid);
            if (Objects.isNull(inviterUser)) {
                log.error("MERCHANT JOIN ERROR! not found inviterUser, inviterUid={}", inviterUid);
                return R.fail(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (inviterUser.isLock()) {
                log.error("MERCHANT JOIN ERROR! inviterUser locked, inviterUid={}", inviterUid);
                return R.fail(false, "120105", "该二维码暂时无法使用,请稍后再试");
            }
            
            // 扫自己码
            if (Objects.equals(userInfo.getUid(), inviterUid)) {
                log.info("MERCHANT JOIN ERROR! illegal operate! Can not scan own QR, inviterUid={}, joinUid={}", inviterUid, joinUid);
                return R.fail("100463", "二维码已失效");
            }
            
            // 邀请人类型
            if (!Objects.equals(inviterType, MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_SELF) && !Objects.equals(inviterType,
                    MerchantJoinRecordConstant.INVITER_TYPE_MERCHANT_PLACE_EMPLOYEE)) {
                log.info("MERCHANT JOIN ERROR! illegal operate! Inviter is illegal, inviterUid={}, inviterType={}, joinUid={}", inviterUid, inviterType, joinUid);
                return R.fail("100463", "二维码已失效");
            }
            
            // 获取商户保护期和有效期
            MerchantAttr merchantAttr = merchantAttrService.queryByTenantIdFromCache(merchant.getTenantId());
            if (Objects.isNull(merchantAttr)) {
                log.error("MERCHANT JOIN ERROR! not found merchantAttr, merchantId={}", merchantId);
                return R.fail("100463", "二维码已失效");
            }
            
            // 渠道员uid
            Long channelEmployeeUid = merchant.getChannelEmployeeUid();
            
            // 获取场地员工所绑定的场地
            Long placeId = Optional.ofNullable(merchantEmployeeService.queryMerchantEmployeeByUid(inviterUid)).orElse(new MerchantEmployeeVO()).getPlaceId();
            
            // 保存参与记录
            MerchantJoinRecord record = this.assembleRecord(merchantId, inviterUid, inviterType, joinUid, channelEmployeeUid, placeId, merchantAttr, tenant.getId());
            Integer result = merchantJoinRecordMapper.insertOne(record);
            
            // 将旧的已参与记录改为已失效
            if (Objects.nonNull(needUpdatedToInvalidRecord) && Objects.nonNull(result)) {
                this.updateStatusById(needUpdatedToInvalidRecord.getId(), MerchantJoinRecordConstant.STATUS_INVALID, System.currentTimeMillis());
            }
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid);
        }
    }
    
    private MerchantJoinRecord assembleRecord(Long merchantId, Long inviterUid, Integer inviterType, Long joinUid, Long channelEmployeeUid, Long placeId, MerchantAttr merchantAttr,
            Integer tenantId) {
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
        
        // 生成参与记录
        return MerchantJoinRecord.builder().merchantId(merchantId).channelEmployeeUid(channelEmployeeUid).placeId(placeId).inviterUid(inviterUid).inviterType(inviterType)
                .joinUid(joinUid).startTime(nowTime).expiredTime(expiredTime).status(MerchantJoinRecordConstant.STATUS_INIT).protectionTime(protectionExpireTime)
                .protectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_NORMAL).delFlag(NumberConstant.ZERO).createTime(nowTime).updateTime(nowTime).tenantId(tenantId)
                .build();
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
    
    @Override
    public void handelProtectionAndStartExpired() {
        MerchantJoinRecord protectionJoinRecord = new MerchantJoinRecord();
        protectionJoinRecord.setProtectionStatus(MerchantJoinRecordConstant.PROTECTION_STATUS_EXPIRED);
        protectionJoinRecord.setUpdateTime(System.currentTimeMillis());
        merchantJoinRecordMapper.updateProtectionExpired(protectionJoinRecord);
        
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
            
            voList.add(vo);
        }
        
        return voList;
    }
    
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
        
        log.info("query join user list for current merchant, request = {}", merchantJoinUserQueryMode);
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
    public Integer countEmployeeScanCodeNum(List<Long> uidList, Long startTime, Long endTime, Integer status, Integer tenantId) {
        return merchantJoinRecordMapper.countEmployeeScanCodeNum(uidList, startTime, endTime, status, tenantId);
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
    public String queryMerchantNameByJoinUid(Long joinUid, Integer status) {
        return merchantJoinRecordMapper.selectMerchantNameByJoinUid(joinUid, status);
    }
}
