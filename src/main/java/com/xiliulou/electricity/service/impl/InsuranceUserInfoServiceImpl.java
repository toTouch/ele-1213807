package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.query.InsuranceUserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Transactional(rollbackFor = Exception.class)
    public R updateUserBatteryInsuranceStatus(Long uid, Integer insuranceStatus,Integer type) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
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

        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoMapper.selectOne(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid).eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
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
     * @return
     */
    @Override
    public void saveUserInsurance(InsuranceOrder insuranceOrder) {
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

        if (Objects.isNull(insuranceUserInfoCache) || Objects.equals(InsuranceUserInfo.IS_USE, insuranceUserInfoCache.getIsUse()) || insuranceUserInfoCache.getInsuranceExpireTime() < System.currentTimeMillis()) {
            insuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        } else {
            insuranceUserInfo.setInsuranceExpireTime(insuranceUserInfoCache.getInsuranceExpireTime() + insuranceOrder.getValidDays() * 24 * 60 * 60 * 1000L);
        }

        if(Objects.isNull(insuranceUserInfoCache)){
            insuranceUserInfo.setCreateTime(System.currentTimeMillis());
            insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            insuranceUserInfoService.insert(insuranceUserInfo);
        }else{
            insuranceUserInfo.setId(insuranceUserInfoCache.getId());
            insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            insuranceUserInfoService.updateInsuranceUserInfoById(insuranceUserInfo);
        }
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
            log.error("ELECTRICITY  ERROR! not found userInfo! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        InsuranceUserInfoVo insuranceUserInfoVo = queryByUidAndTenantId(uid, tenantId);
        if (Objects.isNull(insuranceUserInfoVo) || insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || Objects.equals(insuranceUserInfoVo.getIsUse(), InsuranceUserInfo.IS_USE)) {
            return R.ok();
        }
        return R.ok(insuranceUserInfoVo);
    }

    @Override
    public R queryUserInsurance(Long uid,Integer type) {

        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId={}", uid);
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
    public InsuranceUserInfoVo selectUserInsuranceInfo(Long uid,Integer type) {

        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId={}", uid);
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
            log.error("ELECTRICITY  ERROR! not found userInfo! userId={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        if (Objects.equals(status, InsuranceUserInfo.NOT_USE)) {

            InsuranceUserInfoVo insuranceUserInfoVo = queryByUidAndTenantId(uid, tenantId);
            if (Objects.isNull(insuranceUserInfoVo) || insuranceUserInfoVo.getInsuranceExpireTime() < System.currentTimeMillis() || !Objects.equals(insuranceUserInfoVo.getIsUse(), InsuranceUserInfo.NOT_USE)) {
                return R.ok();
            }
            List<InsuranceOrderVO> insuranceOrderList = new ArrayList<>();
            InsuranceOrderVO insuranceOrderVO = new InsuranceOrderVO();
            BeanUtil.copyProperties(insuranceUserInfoVo, insuranceOrderVO);
            insuranceOrderList.add(insuranceOrderVO);

            return R.ok(insuranceOrderList);
        } else if (Objects.equals(status, InsuranceUserInfo.IS_USE)) {
            InsuranceOrderQuery insuranceOrderQuery = InsuranceOrderQuery.builder()
                    .offset(offset)
                    .size(size)
                    .uid(uid)
                    .isUse(InsuranceUserInfo.IS_USE)
                    .tenantId(userInfo.getTenantId())
                    .build();
            List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderService.queryListByStatus(insuranceOrderQuery);
            if (Objects.nonNull(insuranceOrderVOList)) {
                insuranceOrderVOList.parallelStream().forEach(item -> {
                    item.setInsuranceExpireTime(item.getCreateTime() + (item.getValidDays() * (24 * 60 * 60 * 1000L)));
                });
            }

            return R.ok(insuranceOrderVOList);
        } else {
            InsuranceOrderQuery insuranceOrderQuery = InsuranceOrderQuery.builder()
                    .offset(offset)
                    .size(size)
                    .uid(uid)
                    .isUse(InsuranceUserInfo.NOT_USE)
                    .tenantId(userInfo.getTenantId())
                    .build();
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
            return R.ok(expireInsuranceOrderList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public R insertUserBatteryInsurance(InsuranceUserInfoQuery query) {

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

        if (Objects.equals(query.getType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY) && !Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
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
        InsuranceOrder insuranceUserOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType())
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_SUCCESS)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
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
        this.saveUserInsurance(insuranceUserOrder);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editUserInsuranceInfo(InsuranceUserInfoQuery query){

        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(userInfo.getUid(), query.getType());
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户未购买保险");
        }

        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
        updateInsuranceUserInfo.setId(insuranceUserInfo.getId());
        updateInsuranceUserInfo.setIsUse(query.getIsUse());
        updateInsuranceUserInfo.setType(query.getType());
        updateInsuranceUserInfo.setUid(userInfo.getUid());
        updateInsuranceUserInfo.setInsuranceExpireTime(query.getInsuranceExpireTime());
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        this.updateInsuranceUserInfoById(updateInsuranceUserInfo);

        InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
        insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
        insuranceOrderUpdate.setOrderId(insuranceUserInfo.getInsuranceOrderId());
        insuranceOrderUpdate.setIsUse(query.getIsUse());
        insuranceOrderUpdate.setTenantId(TenantContextHolder.getTenantId());
        insuranceOrderService.updateIsUseByOrderId(insuranceOrderUpdate);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R renewalUserBatteryInsurance(InsuranceUserInfoQuery query) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)|| !Objects.equals( userInfo.getTenantId(),TenantContextHolder.getTenantId() )) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(query.getType(),FranchiseeInsurance.INSURANCE_TYPE_BATTERY ) && !Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(),FranchiseeInsurance.INSURANCE_TYPE_CAR ) && !Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        if (Objects.equals(query.getType(),FranchiseeInsurance.INSURANCE_TYPE_BATTERY_CAR ) && !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        InsuranceUserInfo insuranceUserInfo = selectByUidAndTypeFromCache(query.getUid(), query.getType());
        if (Objects.isNull(insuranceUserInfo) ) {
            return R.fail("100309", "用户未购买保险");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(query.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            return R.fail("100306", "保险已禁用!");
        }

        String orderId=OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE,userInfo.getUid());
        InsuranceOrder insuranceUserOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType())
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_SUCCESS)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        insuranceOrderService.insert(insuranceUserOrder);

        this.saveUserInsurance(insuranceUserOrder);

        InsuranceOrder oldInsuranceUserOrder=insuranceOrderService.queryByOrderId(insuranceUserInfo.getInsuranceOrderId());
        if(Objects.nonNull(oldInsuranceUserOrder)){
            InsuranceOrder insuranceUserOrderUpdate = new InsuranceOrder();
            insuranceUserOrderUpdate.setId(oldInsuranceUserOrder.getId());
            insuranceUserOrderUpdate.setIsUse(Objects.equals(oldInsuranceUserOrder.getIsUse(), InsuranceOrder.IS_USE) ? InsuranceOrder.IS_USE : InsuranceOrder.INVALID);
            insuranceUserOrderUpdate.setUpdateTime(System.currentTimeMillis());
//            insuranceOrderService.update(insuranceUserOrderUpdate);
            insuranceOrderService.updateUseStatusForRefund(oldInsuranceUserOrder.getOrderId(),InsuranceOrder.INVALID);
        }
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
        if(Objects.isNull(franchiseeInsurance) || !Objects.equals( FranchiseeInsurance.CONSTRAINT_FORCE, franchiseeInsurance.getIsConstraint())){
            return Boolean.FALSE;
        }

        //获取用户当前绑定的保险
        InsuranceUserInfo insuranceUserInfo = this.selectByUidAndTypeFromDB(userInfo.getUid(), type);
        if(Objects.isNull(insuranceUserInfo) || insuranceUserInfo.getInsuranceExpireTime()<System.currentTimeMillis()){
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
            if(CollectionUtils.isEmpty(list)){
                return;
            }

            list.parallelStream().forEach(item -> {
                if (item.getInsuranceExpireTime() < System.currentTimeMillis()) {
                    //更新用户绑定保险状态
                    InsuranceUserInfo insuranceUserInfo = new InsuranceUserInfo();
                    insuranceUserInfo.setId(item.getId());
                    insuranceUserInfo.setUid(item.getUid());
                    insuranceUserInfo.setType(item.getType());
                    insuranceUserInfo.setIsUse(InsuranceOrder.EXPIRED);
                    insuranceUserInfo.setUpdateTime(System.currentTimeMillis());
                    this.updateInsuranceUserInfoById(insuranceUserInfo);

                    //更新订单状态
                    insuranceOrderService.updateUseStatusByOrderId(item.getInsuranceOrderId(), InsuranceOrder.EXPIRED);
                }
            });

            offset += size;
        }
    }

    @Override
    public List<InsuranceUserInfo> selectByUid(Long uid) {
        return baseMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid,uid));
    }

    private List<InsuranceUserInfo> selectUserInsuranceList(int offset, int size) {
        return baseMapper.selectUserInsuranceList(offset,size);
    }
}
