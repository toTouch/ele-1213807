package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FranchiseeInsuranceMapper;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    InsuranceOrderMapper insuranceOrderMapper;

    @Override
    public List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId) {
        return insuranceUserInfoMapper.selectList(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getInsuranceId, id).eq(InsuranceUserInfo::getTenantId, tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }

    @Override
    public InsuranceUserInfo queryByUid(Long uid, Integer tenantId) {
        return insuranceUserInfoMapper.selectOne(new LambdaQueryWrapper<InsuranceUserInfo>().eq(InsuranceUserInfo::getUid, uid).eq(InsuranceUserInfo::getTenantId, tenantId)
                .eq(InsuranceUserInfo::getDelFlag, InsuranceUserInfo.DEL_NORMAL));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateInsuranceStatus(Long uid, Integer insuranceStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId, userInfo.getTenantId())) {
            return R.ok();
        }

        InsuranceUserInfo insuranceUserInfo = queryByUidFromCache(uid);
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户不存在保险");
        }

        if (Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.IS_USE)) {
            return R.fail("100311", "用户保险状态为已出险，无法修改");
        }

        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
        updateInsuranceUserInfo.setIsUse(insuranceStatus);
        updateInsuranceUserInfo.setUid(uid);
        updateInsuranceUserInfo.setTenantId(tenantId);
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());

        InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
        insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
        insuranceOrderUpdate.setOrderId(insuranceUserInfo.getInsuranceOrderId());
        insuranceOrderUpdate.setIsUse(insuranceStatus);
        insuranceOrderUpdate.setTenantId(tenantId);
        insuranceOrderService.updateIsUseByOrderId(insuranceOrderUpdate);

        return R.ok(update(updateInsuranceUserInfo));
    }

    @Override
    public InsuranceUserInfo queryByUidFromCache(Long uid) {

        InsuranceUserInfo cache = redisService.getWithHash(CacheConstant.CACHE_INSURANCE_USER_INFO + uid, InsuranceUserInfo.class);
//        if (Objects.nonNull(cache)) {
//            if (Objects.nonNull(cache.getInsuranceExpireTime()) && cache.getInsuranceExpireTime() < System.currentTimeMillis()) {
//                redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + uid);
//            } else {
//                return cache;
//            }
//        }

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
    public Integer insert(InsuranceUserInfo insuranceUserInfo) {
        return insuranceUserInfoMapper.insert(insuranceUserInfo);
    }

    @Override
    public Integer update(InsuranceUserInfo insuranceUserInfo) {
        int result = this.insuranceUserInfoMapper.update(insuranceUserInfo);
        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + insuranceUserInfo.getUid());
            return null;
        });
        return result;
    }

    @Override
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
    public R insertUserInsurance(InsuranceUserInfo insuranceOrder) {

        if (Objects.isNull(insuranceOrder)) {
            log.error("INSERT USER INSURANCEORDER ERROR! PARAM IS NULL");
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Long uid = insuranceOrder.getUid();
        Integer insuranceId = insuranceOrder.getInsuranceId();
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        BigDecimal premium = insuranceOrder.getPremium();
        Long insuranceExpireTime = insuranceOrder.getInsuranceExpireTime();
        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", uid);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={} ", uid);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not pay deposit,uid={}", uid);
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
        if (Objects.nonNull(insuranceUserInfo) && Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.NOT_USE) && insuranceUserInfo.getInsuranceExpireTime() > System.currentTimeMillis()) {
            log.error("CREATE INSURANCE_ORDER ERROR! user have insurance ！uid={}", userInfo.getUid());
            return R.fail("100310", "已购买保险");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceId);

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={}",insuranceId);
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", insuranceId);
            return R.fail("100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }

        String orderId=String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid + RandomUtil.randomNumbers(4);
        InsuranceOrder insuranceUserOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_SUCCESS)
                .tenantId(tenantId)
                .uid(uid)
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        insuranceOrderMapper.insert(insuranceUserOrder);

        insuranceExpireTime = (insuranceExpireTime == null || insuranceExpireTime < System.currentTimeMillis()) ? (System.currentTimeMillis() + franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L))) : insuranceExpireTime;
        premium = (premium == null || premium.compareTo(BigDecimal.ZERO) < 0) ? franchiseeInsurance.getPremium() : premium;

        InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
        updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
        updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
        updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
        updateOrAddInsuranceUserInfo.setInsuranceExpireTime(insuranceExpireTime);
        updateOrAddInsuranceUserInfo.setTenantId(tenantId);
        updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
        updateOrAddInsuranceUserInfo.setPremium(premium);
        updateOrAddInsuranceUserInfo.setFranchiseeId(franchisee.getId());


        if (Objects.isNull(insuranceUserInfo)) {
            updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());
            insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
        } else {
            insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
        }
        return R.ok();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editUserInsuranceInfo(InsuranceUserInfo order){
        if (Objects.isNull(order)) {
            log.error("INSERT USER INSURANCEORDER ERROR! PARAM IS NULL");
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        Integer insuranceStatus = order.getIsUse();
        if( !Objects.equals(InsuranceUserInfo.IS_USE, insuranceStatus) && !Objects.equals(InsuranceUserInfo.NOT_USE, insuranceStatus)){
            log.error("INSERT USER INSURANCEORDER ERROR! INSURANCESTATUS IS ILLEGAL");
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        Long uid = order.getUid();
        Long insuranceExpireTime = order.getInsuranceExpireTime();
        BigDecimal premium = order.getPremium();


        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId, userInfo.getTenantId())) {
            return R.ok();
        }

        InsuranceUserInfo insuranceUserInfo = queryByUidFromCache(uid);
        if (Objects.isNull(insuranceUserInfo)) {
            return R.fail("100309", "用户不存在保险");
        }


        InsuranceUserInfo updateInsuranceUserInfo = new InsuranceUserInfo();
        updateInsuranceUserInfo.setIsUse(insuranceStatus);
        updateInsuranceUserInfo.setUid(uid);
        updateInsuranceUserInfo.setTenantId(tenantId);
        updateInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateInsuranceUserInfo.setInsuranceExpireTime(insuranceExpireTime);

        InsuranceOrder insuranceOrderUpdate = new InsuranceOrder();
        insuranceOrderUpdate.setUpdateTime(System.currentTimeMillis());
        insuranceOrderUpdate.setOrderId(insuranceUserInfo.getInsuranceOrderId());
        insuranceOrderUpdate.setIsUse(insuranceStatus);
        insuranceOrderUpdate.setTenantId(tenantId);
        insuranceOrderService.updateIsUseByOrderId(insuranceOrderUpdate);

        return R.ok(update(updateInsuranceUserInfo));


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R renewalUserInsurance(InsuranceUserInfo insuranceOrder) {

        if (Objects.isNull(insuranceOrder)) {
            log.error("INSERT USER INSURANCEORDER ERROR! PARAM IS NULL");
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Long uid = insuranceOrder.getUid();
        Integer insuranceId = insuranceOrder.getInsuranceId();
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", uid);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={} ", uid);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not pay deposit,uid={}", uid);
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(insuranceUserInfo) ) {
            log.error("CREATE MEMBER_ORDER ERROR! not pay insurance! uid={} ", uid);
            return R.fail("100309", "用户不存在保险");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceId);

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={}",insuranceId);
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", insuranceId);
            return R.fail("100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }

        String orderId=String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid + RandomUtil.randomNumbers(4);
        InsuranceOrder insuranceUserOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.OFFLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_SUCCESS)
                .tenantId(tenantId)
                .uid(uid)
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        insuranceOrderMapper.insert(insuranceUserOrder);


        InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
        updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
        updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
        updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
        updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
        updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
        if(Objects.equals(InsuranceUserInfo.IS_USE,insuranceUserInfo.getIsUse())){
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis()+franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
        }else{
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime()+franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
        }
        updateOrAddInsuranceUserInfo.setTenantId(tenantId);
        updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
        updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
        updateOrAddInsuranceUserInfo.setFranchiseeId(franchisee.getId());


        if (Objects.isNull(insuranceUserInfo)) {
            updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());
            insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
        } else {
            insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
        }
        return R.ok();

    }
}
