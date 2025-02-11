package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.query.InsuranceUserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceUserInfoService")
@Slf4j
public class InsuranceUserInfoServiceImpl extends ServiceImpl<InsuranceUserInfoMapper, InsuranceUserInfo> implements InsuranceUserInfoService {
    
    @Resource
    InsuranceUserInfoMapper insuranceUserInfoMapper;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    CityService cityService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    
    @Resource
    private UserDelRecordService userDelRecordService;
    
    @Slave
    @Override
    public List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId) {
        return insuranceUserInfoMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getInsuranceId, id).eq(InsuranceUserInfo::getTenantId, tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }
    
    @Override
    @Deprecated
    public InsuranceUserInfo queryByUid(Long uid, Integer tenantId) {
        return insuranceUserInfoMapper.selectOne(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid).eq(InsuranceUserInfo::getTenantId, tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }
    
    @Override
    public R updateUserBatteryInsuranceStatus(Long uid, Integer insuranceStatus, Integer type) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(uid, type);
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户不存在保险");
        }
        
        if (Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.IS_USE)) {
            return R.fail("100311", "用户保险状态为已出险，无法修改");
        }
        
        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
        updateInsuranceUserInfo.setId(insuranceUserInfo.getId());
        updateInsuranceUserInfo.setIsUse(insuranceStatus);
        updateInsuranceUserInfo.setUid(uid);
        updateInsuranceUserInfo.setType(type);
        updateInsuranceUserInfo.setTenantId(TenantContextHolder.getTenantId());
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        this.updateInsuranceUserInfoById(updateInsuranceUserInfo);
        
        InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
        insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
        insuranceOrderUpdate.setOrderId(insuranceUserInfo.getInsuranceOrderId());
        insuranceOrderUpdate.setIsUse(insuranceStatus);
        insuranceOrderUpdate.setTenantId(TenantContextHolder.getTenantId());
        insuranceOrderService.updateIsUseByOrderId(insuranceOrderUpdate);
        
        return R.ok();
    }
    
    @Override
    @Deprecated
    public InsuranceUserInfo queryByUidFromCache(Long uid) {
        
        InsuranceUserInfo cache = redisService.getWithHash(CacheConstant.CACHE_INSURANCE_USER_INFO + uid, InsuranceUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoMapper.selectOne(
                new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid).eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
        if (Objects.isNull(insuranceUserInfo)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_INSURANCE_USER_INFO + uid, insuranceUserInfo);
        return insuranceUserInfo;
    }
    
    @Override
    public InsuranceUserInfo selectByUidAndTypeFromDB(Long uid, Integer type) {
        return insuranceUserInfoMapper.selectByUidAndTypeFromDB(uid, type);
    }
    
    @Override
    public InsuranceUserInfo selectByUidAndTypeFromCache(Long uid, Integer type) {
        InsuranceUserInfo cache = redisService.getWithHash(CacheConstant.CACHE_INSURANCE_USER_INFO + uid + ":" + type, InsuranceUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        InsuranceUserInfo insuranceUserInfo = this.selectByUidAndTypeFromDB(uid, type);
        if (Objects.isNull(insuranceUserInfo)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_INSURANCE_USER_INFO + uid + ":" + type, insuranceUserInfo);
        return insuranceUserInfo;
    }
    
    @Override
    public Integer insert(InsuranceUserInfo insuranceUserInfo) {
        return insuranceUserInfoMapper.insert(insuranceUserInfo);
    }
    
    @Override
    public Integer update(InsuranceUserInfo insuranceUserInfo) {
        int result = this.insuranceUserInfoMapper.update(insuranceUserInfo);
        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid());
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid() + ":" + insuranceUserInfo.getType());
            return null;
        });
        return result;
    }
    
    @Override
    public int updateInsuranceUserInfoById(InsuranceUserInfo insuranceUserInfo) {
        int result = this.insuranceUserInfoMapper.updateById(insuranceUserInfo);
        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid());
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid() + ":" + insuranceUserInfo.getType());
            return null;
        });
        return result;
    }
    
    @Override
    @Deprecated
    public InsuranceUserInfoVo queryByUidAndTenantId(Long uid, Integer tenantId) {
        InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoMapper.queryByUidAndTenantId(uid, tenantId);
        
        if (Objects.nonNull(insuranceUserInfoVo)) {
            //获取城市名称
            City city = cityService.queryByIdFromDB(insuranceUserInfoVo.getCid());
            if (Objects.nonNull(city)) {
                insuranceUserInfoVo.setCityName(city.getName());
            }
        }
        return insuranceUserInfoVo;
    }
    
    @Slave
    @Override
    public InsuranceUserInfoVo selectUserInsurance(Long uid, Integer type) {
        InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return insuranceUserInfoVo;
        }
        
        insuranceUserInfoVo = this.selectUserInsuranceDetailByUidAndType(uid, type);
        if (Objects.nonNull(insuranceUserInfoVo)) {
            InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(insuranceUserInfoVo.getInsuranceOrderId());
            insuranceUserInfoVo.setPayInsuranceTime(Objects.isNull(insuranceOrder) ? null : insuranceOrder.getCreateTime());
        }
        
        return insuranceUserInfoVo;
    }
    
    /**
     * 保存或更新用户保险
     *
     * @return
     */
    @Override
    public InsuranceUserInfo saveUserInsurance(InsuranceOrder insuranceOrder) {
        InsuranceUserInfo insuranceUserInfoCache = this.selectByUidAndTypeFromCache(insuranceOrder.getUid(), insuranceOrder.getInsuranceType());
        
        InsuranceUserInfo insuranceUserInfo = new InsuranceUserInfo();
        insuranceUserInfo.setUid(insuranceOrder.getUid());
        insuranceUserInfo.setFranchiseeId(insuranceOrder.getFranchiseeId());
        insuranceUserInfo.setPremium(insuranceOrder.getPayAmount());
        insuranceUserInfo.setForehead(insuranceOrder.getForehead());
        insuranceUserInfo.setInsuranceId(insuranceOrder.getInsuranceId());
        insuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
        insuranceUserInfo.setInsuranceExpireTime(0L);
        insuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        insuranceUserInfo.setDelFlag(InsuranceUserInfo.DEL_NORMAL);
        insuranceUserInfo.setTenantId(insuranceOrder.getTenantId());
        insuranceUserInfo.setType(insuranceOrder.getInsuranceType());
        
        if (Objects.isNull(insuranceUserInfoCache) || Objects.equals(InsuranceUserInfo.IS_USE, insuranceUserInfoCache.getIsUse())
                || insuranceUserInfoCache.getInsuranceExpireTime() < System.currentTimeMillis()) {
            insuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        } else {
            insuranceUserInfo.setInsuranceExpireTime(insuranceUserInfoCache.getInsuranceExpireTime() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        }
        
        if (Objects.isNull(insuranceUserInfoCache)) {
            insuranceUserInfo.setCreateTime(System.currentTimeMillis());
            insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            insuranceUserInfoService.insert(insuranceUserInfo);
        } else {
            insuranceUserInfo.setId(insuranceUserInfoCache.getId());
            insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            insuranceUserInfoService.updateInsuranceUserInfoById(insuranceUserInfo);
        }
        return insuranceUserInfo;
    }
    
    @Override
    public R queryUserInsurance() {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  WARN! not found userInfo! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InsuranceUserInfoVo insuranceUserInfoVo = queryByUidAndTenantId(uid, tenantId);
        if (Objects.isNull(insuranceUserInfoVo) || insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || Objects.equals(insuranceUserInfoVo.getIsUse(),
                InsuranceUserInfo.IS_USE)) {
            return R.ok();
        }
        return R.ok(insuranceUserInfoVo);
    }
    
    @Override
    public R queryUserInsurance(Long uid, Integer type) {
        
        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  WARN! not found userInfo! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(uid, type);
        if (Objects.isNull(insuranceUserInfo)) {
            log.warn("ELE WARN!not found insuranceUserInfo,uid={},type={}", uid, type);
            return R.ok();
        }
        
        InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
        BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
        
        if (insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || Objects.equals(insuranceUserInfoVo.getIsUse(), InsuranceUserInfo.IS_USE)) {
            return R.ok();
        }
        
        return R.ok(insuranceUserInfoVo);
    }
    
    /**
     * 兼容租车和车电一体
     */
    @Override
    public InsuranceUserInfoVo selectUserInsuranceInfo(Long uid, Integer type) {
        
        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  WARN! not found userInfo! userId={}", uid);
            return null;
        }
        
        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(uid, type);
        if (Objects.isNull(insuranceUserInfo)) {
            log.warn("ELE WARN!not found insuranceUserInfo,uid={},type={}", uid, type);
            return null;
        }
        
        InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
        BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
        
        if (insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || Objects.equals(insuranceUserInfoVo.getIsUse(), InsuranceUserInfo.IS_USE)) {
            return null;
        }
        
        return insuranceUserInfoVo;
    }
    
    @Override
    public R queryInsuranceByStatus(Integer status, Long offset, Long size) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  WARN! not found userInfo! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(status, InsuranceUserInfo.NOT_USE)) {
            InsuranceUserInfoVo insuranceUserInfoVo = queryByUidAndTenantId(uid, tenantId);
            if (Objects.isNull(insuranceUserInfoVo) || insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || !Objects.equals(insuranceUserInfoVo.getIsUse(),
                    InsuranceUserInfo.NOT_USE)) {
                return R.ok();
            }
            List<InsuranceOrderVO> insuranceOrderList = new ArrayList<>();
            InsuranceOrderVO insuranceOrderVO = new InsuranceOrderVO();
            BeanUtil.copyProperties(insuranceUserInfoVo, insuranceOrderVO);
            insuranceOrderList.add(insuranceOrderVO);
            return R.ok(insuranceOrderList);
        } else if (Objects.equals(status, InsuranceUserInfo.IS_USE)) {
            InsuranceOrderQuery insuranceOrderQuery = InsuranceOrderQuery.builder().offset(offset).size(size).uid(uid).isUse(InsuranceUserInfo.IS_USE)
                    .tenantId(userInfo.getTenantId()).build();
            List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderService.queryListByStatus(insuranceOrderQuery);
            if (Objects.nonNull(insuranceOrderVOList)) {
                insuranceOrderVOList.parallelStream().forEach(item -> {
                    item.setInsuranceExpireTime(item.getCreateTime() + (item.getValidDays() * (24 * 60 * 60 * 1000L)));
                });
            }
            return R.ok(insuranceOrderVOList);
        } else {
            InsuranceOrderQuery insuranceOrderQuery = InsuranceOrderQuery.builder().offset(offset).size(size).uid(uid).isUse(InsuranceUserInfo.NOT_USE)
                    .tenantId(userInfo.getTenantId()).build();
            List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderService.queryListByStatus(insuranceOrderQuery);
            List<InsuranceOrderVO> expireInsuranceOrderList = new ArrayList<>();
            if (Objects.nonNull(insuranceOrderVOList)) {
                insuranceOrderVOList.parallelStream().forEach(item -> {
                    if ((item.getCreateTime() + (item.getValidDays() * (24 * 60 * 60 * 1000L))) < System.currentTimeMillis()) {
                        item.setInsuranceExpireTime(item.getCreateTime() + (item.getValidDays() * (24 * 60 * 60 * 1000L)));
                        expireInsuranceOrderList.add(item);
                    }
                    
                });
            }
            log.error("============333{}", JsonUtil.toJson(expireInsuranceOrderList));
            return R.ok(expireInsuranceOrderList);
        }
    }
    
    @Override
    public int deleteById(InsuranceUserInfo insuranceUserInfo) {
        int delete = baseMapper.deleteById(insuranceUserInfo.getId());
        
        expireInsuranceOrder(insuranceUserInfo);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid());
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid() + ":" + insuranceUserInfo.getType());
            return null;
        });
        return delete;
    }
    
    @Override
    public int deleteByUidAndType(Long uid, Integer type) {
        int delete = this.baseMapper.deleteByUidAndType(uid, type);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + uid);
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + uid + ":" + type);
            return null;
        });
        return delete;
    }
    
    private void expireInsuranceOrder(InsuranceUserInfo insuranceUserInfo) {
        String insuranceOrderId = insuranceUserInfo.getInsuranceOrderId();
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(insuranceOrderId);
        if (Objects.nonNull(insuranceOrder)) {
            Long validDays = Math.abs((System.currentTimeMillis() - insuranceOrder.getCreateTime()) / (24 * 60 * 60 * 1000L));
            InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
            insuranceOrderUpdate.setValidDays(validDays.intValue());
            insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
            insuranceOrderUpdate.setId(insuranceOrder.getId());
            insuranceOrderService.updateOrderStatusById(insuranceOrderUpdate);
        }
    }
    
    @Override
    public R insertUserBatteryInsurance(InsuranceUserInfoQuery query) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return R.fail("120139", "账号处于注销缓冲期内，无法操作");
        }
        
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY) && !Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_CAR) && !Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR) && !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(query.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            return R.fail("100305", "未找到保险!");
        }
        
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            return R.fail("100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            return R.fail("100305", "未找到保险");
        }
        
        if (!Objects.equals(franchiseeInsurance.getFranchiseeId(), userInfo.getFranchiseeId())) {
            return R.fail("ELECTRICITY.0038", "用户加盟商与保险加盟商不一致");
        }
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(query.getUid(), query.getType());
        if (Objects.nonNull(insuranceUserInfo)) {
            return R.fail("100310", "用户已购买保险");
        }
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceUserOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(orderId).cid(franchiseeInsurance.getCid()).franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium()).forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_SUCCESS).tenantId(TenantContextHolder.getTenantId()).uid(userInfo.getUid()).userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).build();
        insuranceOrderService.insert(insuranceUserOrder);

/*
        InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
        updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
        updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
        updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
        updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + franchiseeInsurance.getValidDays() * 24 * 60 * 60 * 1000L);
        updateOrAddInsuranceUserInfo.setTenantId(TenantContextHolder.getTenantId());
        updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
        updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
        updateOrAddInsuranceUserInfo.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
        updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());
        insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
        */
        InsuranceUserInfo saveUserInsurance = this.saveUserInsurance(insuranceUserOrder);
        
        //新增操作记录
        EleUserOperateRecord record = EleUserOperateRecord.builder().operateUid(user.getUid()).uid(userInfo.getUid()).name(user.getUsername())
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
        //判断是单电的电池操作还是车电一体的电池操作
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            record.setOperateModel(UserOperateRecordConstant.BATTERY_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.EDIT_BATTERY_INSURANCE_CONTENT);
            record.setNewBatteryInsuranceStatus(InsuranceOrder.NOT_USE);
            record.setNewBatteryInsuranceExpireTime(saveUserInsurance.getInsuranceExpireTime());
        } else {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            record.setOperateModel(UserOperateRecordConstant.CAR_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.EDIT_CAR_INSURANCE_CONTENT);
            record.setNewCarInsuranceStatus(InsuranceOrder.NOT_USE);
            record.setNewCarInsuranceExpireTime(saveUserInsurance.getInsuranceExpireTime());
        }
        
        eleUserOperateRecordService.asyncHandleUserOperateRecord(record);
        return R.ok();
    }
    
    @Override
    public R editUserInsuranceInfo(InsuranceUserInfoQuery query) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return R.fail("120139", "账号处于注销缓冲期内，无法操作");
        }
        
        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(userInfo.getUid(), query.getType());
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户未购买保险");
        }
        InsuranceUserInfoVo oldInsuranceUserInfo = selectUserInsurance(query.getUid(), query.getType());


        // 是否存在未生效的保险
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(userInfo.getUid(), query.getType(), InsuranceOrder.NOT_EFFECTIVE);

        // 未出险--已出险 && 已经过期
        if (issueInsuranceCheck(insuranceUserInfo, query.getIsUse())) {
            if (Objects.isNull(insuranceOrder)) {
                //无承接保险订单 订单状态更新为已过期
                updateInsuranceOrder(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.EXPIRED);
                return R.fail("402016", "当前保单已过期，无法进行出险操作");
            } else {
                //  有承接保险订单
                //  前一个保单更新为【已过期】
                updateInsuranceOrder(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.EXPIRED);
                //  承接的新保单状态更新为【已出险】
                updateInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.IS_USE);

                // 用户保险为出险
                InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
                updateInsuranceUserInfo.setId(insuranceUserInfo.getId());
                updateInsuranceUserInfo.setIsUse(query.getIsUse());
                updateInsuranceUserInfo.setType(query.getType());
                updateInsuranceUserInfo.setUid(userInfo.getUid());
                // 修改到期时间为承接保险的到期时间
                updateInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
                updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
                updateInsuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
                this.updateInsuranceUserInfoById(updateInsuranceUserInfo);

                // 操作记录
                operate(query, user, userInfo, oldInsuranceUserInfo, updateInsuranceUserInfo);
                return R.ok();
            }
        }


        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
        updateInsuranceUserInfo.setId(insuranceUserInfo.getId());
        updateInsuranceUserInfo.setIsUse(query.getIsUse());
        updateInsuranceUserInfo.setType(query.getType());
        updateInsuranceUserInfo.setUid(userInfo.getUid());
        updateInsuranceUserInfo.setInsuranceExpireTime(query.getInsuranceExpireTime());
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(insuranceOrder)){
            // 存在承接保险订单
            if (Objects.nonNull(query.getInsuranceExpireTime()) && query.getInsuranceExpireTime() <= System.currentTimeMillis() || Objects.equals(query.getIsUse(), InsuranceUserInfo.IS_USE)){
                existNotUseInsuranceOrder(updateInsuranceUserInfo, insuranceOrder);
            }
        }else {
            if (Objects.nonNull(query.getInsuranceExpireTime()) && query.getInsuranceExpireTime() <= System.currentTimeMillis()) {
                updateInsuranceUserInfo.setIsUse(InsuranceOrder.EXPIRED);
            }
            if (Objects.equals(query.getIsUse(), InsuranceUserInfo.IS_USE)) {
                updateInsuranceUserInfo.setIsUse(query.getIsUse());
            }
        }
        this.updateInsuranceUserInfoById(updateInsuranceUserInfo);


        // 当前保险状态更新为已出险
        InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
        insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
        insuranceOrderUpdate.setOrderId(insuranceUserInfo.getInsuranceOrderId());
        // 当前保险到期时间小于等于当前时间,保险状态更新为：已过期
        if (Objects.nonNull(query.getInsuranceExpireTime()) && query.getInsuranceExpireTime() <= System.currentTimeMillis()){
            insuranceOrderUpdate.setIsUse(InsuranceOrder.EXPIRED);
        }else {
            insuranceOrderUpdate.setIsUse(query.getIsUse());
        }
        insuranceOrderUpdate.setTenantId(TenantContextHolder.getTenantId());
        insuranceOrderService.updateIsUseByOrderId(insuranceOrderUpdate);


        //新增操作记录
        operate(query, user, userInfo, oldInsuranceUserInfo, updateInsuranceUserInfo);
        return R.ok();
    }

    private void existNotUseInsuranceOrder(InsuranceUserInfo updateInsuranceUserInfo, InsuranceOrder insuranceOrder) {
        updateInsuranceUserInfo.setInsuranceId(insuranceOrder.getInsuranceId());
        updateInsuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
        // 设置用户未出险
        updateInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        // 时间为承接保险的到期时间+当前时间
        updateInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        // 有未生效的保险设置为 未出险
        this.updateInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.NOT_USE);
    }

    private void operate(InsuranceUserInfoQuery query, TokenUser user, UserInfo userInfo, InsuranceUserInfoVo oldInsuranceUserInfo, InsuranceUserInfo updateInsuranceUserInfo) {
        EleUserOperateRecord record = EleUserOperateRecord.builder().operateUid(user.getUid()).uid(userInfo.getUid()).name(user.getUsername())
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();

        //判断是单电的保险操作还是车的保险操作
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            record.setOperateModel(UserOperateRecordConstant.BATTERY_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.EDIT_BATTERY_INSURANCE_CONTENT);
            record.setOldBatteryInsuranceStatus(oldInsuranceUserInfo.getIsUse());
            record.setNewBatteryInsuranceStatus(updateInsuranceUserInfo.getIsUse());
            record.setOldBatteryInsuranceExpireTime(oldInsuranceUserInfo.getInsuranceExpireTime());
            record.setNewBatteryInsuranceExpireTime(query.getInsuranceExpireTime());
        } else {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            record.setOperateModel(UserOperateRecordConstant.CAR_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.EDIT_CAR_INSURANCE_CONTENT);
            record.setOldCarInsuranceStatus(oldInsuranceUserInfo.getIsUse());
            record.setNewCarInsuranceStatus(updateInsuranceUserInfo.getIsUse());
            record.setOldCarInsuranceExpireTime(oldInsuranceUserInfo.getInsuranceExpireTime());
            record.setNewCarInsuranceExpireTime(query.getInsuranceExpireTime());
        }

        eleUserOperateRecordService.asyncHandleUserOperateRecord(record);
    }

    private static boolean issueInsuranceCheck(InsuranceUserInfo insuranceUserInfo, Integer isUser) {
        return Objects.equals(isUser, InsuranceUserInfo.IS_USE) && Objects.nonNull(insuranceUserInfo.getInsuranceExpireTime()) && insuranceUserInfo.getInsuranceExpireTime() < System.currentTimeMillis();
    }

    private void updateInsuranceOrder(String orderId, Integer status) {
        InsuranceOrder newInsuranceOrder = new InsuranceOrder();
        newInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        newInsuranceOrder.setOrderId(orderId);
        newInsuranceOrder.setIsUse(status);
        newInsuranceOrder.setTenantId(TenantContextHolder.getTenantId());
        insuranceOrderService.updateIsUseByOrderId(newInsuranceOrder);
    }


    
    @Override
    public R renewalUserBatteryInsurance(InsuranceUserInfoQuery query) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return R.fail("120139", "账号处于注销缓冲期内，无法操作");
        }
        
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY) && !Objects.equals(userInfo.getBatteryDepositStatus(),
                UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_CAR) && !Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR) && !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(query.getUid(), query.getType());
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户未购买保险");
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(query.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            return R.fail("100306", "当前保险已禁用，如需续费请前往【保险配置】处理");
        }

        List<InsuranceOrder> insuranceOrders = insuranceOrderService.queryByUid(query.getUid(), query.getType());
        // 用户是否有未生效保险
        if (CollUtil.isNotEmpty(insuranceOrders.stream().filter(item -> Objects.equals(item.getIsUse(), InsuranceOrder.NOT_EFFECTIVE)).collect(Collectors.toList()))) {
            return R.fail("402015", "当前用户已有未生效的保险订单，请前往【保险购买记录】查看详情");
        }

        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();

        // 生成保险订单
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(orderId).cid(franchiseeInsurance.getCid()).franchiseeId(franchiseeInsurance.getFranchiseeId())
                .payAmount(franchiseeInsurance.getPremium()).forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_SUCCESS).tenantId(TenantContextHolder.getTenantId()).uid(userInfo.getUid()).userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).build();
        // 如意存在未出险的保险订单&已经支付成功的，续费新的保险订单为未生效
        if (CollUtil.isNotEmpty(insuranceOrders.stream().filter(item -> Objects.equals(item.getIsUse(), InsuranceOrder.NOT_USE)
                && Objects.equals(item.getStatus(), InsuranceOrder.STATUS_SUCCESS)).collect(Collectors.toList()))) {
            insuranceOrder.setIsUse(InsuranceOrder.NOT_EFFECTIVE);
        } else {
            // 未出险
            insuranceOrder.setIsUse(InsuranceOrder.NOT_USE);
            // 更新保险绑定关系
            updateInsuranceUserInfo.setInsuranceId(insuranceOrder.getInsuranceId());
            updateInsuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
        }
        insuranceOrderService.insert(insuranceOrder);

        // 如果本来就存在未出险的保险，则不更新绑定，如果没有未出险订单，则更新绑定
        InsuranceUserInfo insuranceUserInfoCache = this.selectByUidAndTypeFromCache(insuranceOrder.getUid(), insuranceOrder.getInsuranceType());
        updateInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        if (Objects.isNull(insuranceUserInfoCache) || Objects.equals(insuranceUserInfoCache.getIsUse(), InsuranceUserInfo.IS_USE)
                || insuranceUserInfoCache.getInsuranceExpireTime() < System.currentTimeMillis()) {
            updateInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        }
        updateInsuranceUserInfo.setId(insuranceUserInfoCache.getId());
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateInsuranceUserInfo.setUid(query.getUid());
        updateInsuranceUserInfo.setType( query.getType());
        insuranceUserInfoService.updateInsuranceUserInfoById(updateInsuranceUserInfo);

        
        InsuranceOrder oldInsuranceUserOrder = insuranceOrderService.queryByOrderId(insuranceUserInfo.getInsuranceOrderId());
//        if (Objects.nonNull(oldInsuranceUserOrder)) {
//            InsuranceOrder insuranceUserOrderUpdate = new InsuranceOrder();
//            insuranceUserOrderUpdate.setId(oldInsuranceUserOrder.getId());
//            insuranceUserOrderUpdate.setIsUse(Objects.equals(oldInsuranceUserOrder.getIsUse(), InsuranceOrder.IS_USE) ? InsuranceOrder.IS_USE : InsuranceOrder.INVALID);
//            insuranceUserOrderUpdate.setUpdateTime(System.currentTimeMillis());
//            insuranceOrderService.updateUseStatusForRefund(oldInsuranceUserOrder.getOrderId(), InsuranceOrder.INVALID);
//        }
/*
        InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
        updateOrAddInsuranceUserInfo.setId(insuranceUserInfo.getId());
        updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
        updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        updateOrAddInsuranceUserInfo.setType(query.getType());
        updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
        updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
        if (Objects.equals(InsuranceUserInfo.IS_USE, insuranceUserInfo.getIsUse()) || (Objects.equals(InsuranceUserInfo.NOT_USE, insuranceUserInfo.getIsUse()) && insuranceUserInfo.getInsuranceExpireTime() < System.currentTimeMillis())) {
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis()+franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
        }else{
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime()+franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
        }
        updateOrAddInsuranceUserInfo.setTenantId(TenantContextHolder.getTenantId());
        updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
        updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
        updateOrAddInsuranceUserInfo.setFranchiseeId(userInfo.getFranchiseeId());

        insuranceUserInfoService.updateInsuranceUserInfoById(updateOrAddInsuranceUserInfo);

        */
        
        //新增操作记录
        EleUserOperateRecord record = EleUserOperateRecord.builder().operateUid(user.getUid()).uid(userInfo.getUid()).name(user.getUsername())
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
        //判断是单电的保险操作还是车的保险操作
        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            record.setOperateModel(UserOperateRecordConstant.BATTERY_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.RENEWAL_BATTERY_INSURANCE_CONTENT);
            record.setOldBatteryInsuranceStatus(oldInsuranceUserOrder.getIsUse());
            record.setNewBatteryInsuranceStatus(updateInsuranceUserInfo.getIsUse());
            record.setOldBatteryInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime());
            record.setNewBatteryInsuranceExpireTime(updateInsuranceUserInfo.getInsuranceExpireTime());
        } else {
            record.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            record.setOperateModel(UserOperateRecordConstant.CAR_INSURANCE);
            record.setOperateContent(UserOperateRecordConstant.RENEWAL_CAR_INSURANCE_CONTENT);
            record.setOldCarInsuranceStatus(oldInsuranceUserOrder.getIsUse());
            record.setNewCarInsuranceStatus(updateInsuranceUserInfo.getIsUse());
            record.setOldCarInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime());
            record.setNewCarInsuranceExpireTime(updateInsuranceUserInfo.getInsuranceExpireTime());
        }
        eleUserOperateRecordService.asyncHandleUserOperateRecord(record);
        
        return R.ok();
    }
    
    @Override
    public Boolean verifyUserIsNeedBuyInsurance(UserInfo userInfo, Integer type, String simpleBatteryType, Long carModelId) {
        
        FranchiseeInsuranceQuery query = new FranchiseeInsuranceQuery();
        query.setTenantId(userInfo.getTenantId());
        query.setFranchiseeId(userInfo.getFranchiseeId());
        query.setStatus(FranchiseeInsurance.STATUS_USABLE);
        query.setInsuranceType(type);
        query.setSimpleBatteryType(simpleBatteryType);
        query.setCarModelId(carModelId);
        
        if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            query.setSimpleBatteryType(simpleBatteryType);
        } else if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_CAR)) {
            query.setCarModelId(carModelId);
        } else if (Objects.equals(type, FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR)) {
            query.setSimpleBatteryType(simpleBatteryType);
            query.setCarModelId(carModelId);
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.selectInsuranceByType(query);
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(FranchiseeInsurance.CONSTRAINT_FORCE, franchiseeInsurance.getIsConstraint())) {
            return Boolean.FALSE;
        }
        
        //获取用户当前绑定的保险
        InsuranceUserInfo insuranceUserInfo = this.selectByUidAndTypeFromDB(userInfo.getUid(), type);
        if (Objects.isNull(insuranceUserInfo) || insuranceUserInfo.getInsuranceExpireTime() < System.currentTimeMillis()) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    @Override
    public InsuranceUserInfoVo selectUserInsuranceDetailByUidAndType(Long uid, Integer type) {
        InsuranceUserInfo insuranceUserInfo = this.selectByUidAndTypeFromDB(uid, type);
        if (Objects.isNull(insuranceUserInfo)) {
            return null;
        }
        
        InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
        BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceUserInfo.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            return insuranceUserInfoVo;
        }
        
        insuranceUserInfoVo.setInsuranceName(franchiseeInsurance.getName());
        
        City city = cityService.queryByIdFromDB(franchiseeInsurance.getCid());
        insuranceUserInfoVo.setCityName(Objects.isNull(city) ? "" : city.getName());
        
        return insuranceUserInfoVo;
    }
    
    @Override
    public void updateUserInsuranceOrderStatusTask() {
        
        int offset = 0;
        int size = 200;
        
        while (true) {
            List<InsuranceUserInfo> list = this.selectUserInsuranceList(offset, size);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            list.parallelStream().forEach(this::userInsuranceExpireAutoConvert);
            offset += size;
        }
    }

    @Override
    public void userInsuranceExpireAutoConvert(InsuranceUserInfo item) {
        if (Objects.isNull(item)) {
            log.warn("UserInsuranceExpireAutoConvert Warn! item is null");
            return;
        }
        if (Objects.isNull(item.getInsuranceExpireTime())) {
            log.warn("UserInsuranceExpireAutoConvert Warn! insuranceExpireTime is null, id is {}", item.getId());
            return;
        }
        if (item.getInsuranceExpireTime() < System.currentTimeMillis()) {
            // 更新保险订单状态=已过期
            insuranceOrderService.updateUseStatusByOrderId(item.getInsuranceOrderId(), InsuranceOrder.EXPIRED);

            InsuranceUserInfo insuranceUserInfo = new InsuranceUserInfo();
            insuranceUserInfo.setId(item.getId());
            insuranceUserInfo.setUid(item.getUid());
            insuranceUserInfo.setType(item.getType());
            insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            // 当前保险过期，未生效保险立即生效（允许定时任务的时间误差），保险状态更新为未出险
            InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(item.getUid(), item.getType(), InsuranceOrder.NOT_EFFECTIVE);
            if (Objects.isNull(insuranceOrder)) {
                // 没有承接保险
                insuranceUserInfo.setIsUse(InsuranceOrder.EXPIRED);
            } else {
                // 未出险，用户新绑定的保险订单
                insuranceUserInfo.setIsUse(InsuranceOrder.NOT_USE);
                insuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
                insuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
                //更新承接保险订单状态=未出险
                insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.NOT_USE);
            }
            this.updateInsuranceUserInfoById(insuranceUserInfo);
        }
    }

    @Override
    public List<InsuranceUserInfo> selectByUid(Long uid) {
        return baseMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid));
    }
    
    private List<InsuranceUserInfo> selectUserInsuranceList(int offset, int size) {
        return baseMapper.selectUserInsuranceList(offset, size);
    }


    @Override
    public R renewalUserInsuranceInfoCheck(InsuranceUserInfoQuery query) {
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(query.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            return R.fail("100305", "未找到保险!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            //  续费同类型未禁用的保险
            franchiseeInsurance = franchiseeInsuranceService.querySameInsuranceType(userInfo.getTenantId(), userInfo.getFranchiseeId(), query.getType(), franchiseeInsurance.getSimpleBatteryType(), franchiseeInsurance.getCarModelId());
            if (Objects.isNull(franchiseeInsurance)) {
                return R.fail("100306", "当前保险已禁用，如需续费请前往【保险配置】处理");
            }
        }

        return R.ok(franchiseeInsurance);
    }

    @Override
    @Slave
    public List<InsuranceUserInfo> listByUid(Long uid) {
        return baseMapper.selectListByUid(uid);
    }
}
