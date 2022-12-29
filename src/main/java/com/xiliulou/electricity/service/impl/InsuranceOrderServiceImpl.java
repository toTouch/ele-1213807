package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
@Service("insuranceOrderService")
@Slf4j
public class InsuranceOrderServiceImpl extends ServiceImpl<InsuranceOrderMapper, InsuranceOrder> implements InsuranceOrderService {

    @Resource
    InsuranceOrderMapper insuranceOrderMapper;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    CityService cityService;

    @Autowired
    UserBatteryService userBatteryService;

    @Override
    public R queryList(InsuranceOrderQuery insuranceOrderQuery) {
        return R.ok(queryListByStatus(insuranceOrderQuery));
    }

    @Override
    public R queryCount(InsuranceOrderQuery insuranceOrderQuery) {
        return R.ok(insuranceOrderMapper.queryCount(insuranceOrderQuery));
    }

    @Override
    public R createOrder(InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }


        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrderAdd.getInsuranceId());

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }

        //生成保险订单
        String orderId = generateOrderId(user.getUid());

        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(tenantId)
                .uid(user.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();

        //支付零元
        if (franchiseeInsurance.getPremium().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            insuranceOrder.setStatus(InsuranceOrder.STATUS_SUCCESS);
            insuranceOrderMapper.insert(insuranceOrder);

            InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
            updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
            updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
            updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
            updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
            updateOrAddInsuranceUserInfo.setTenantId(tenantId);
            updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
            updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
            updateOrAddInsuranceUserInfo.setFranchiseeId(franchisee.getId());


            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(insuranceUserInfo)) {
                updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }
            return R.ok();
        }
        insuranceOrderMapper.insert(insuranceOrder);

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(franchiseeInsurance.getPremium())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_INSURANCE)
                    .attach(ElectricityTradeOrder.ATTACH_INSURANCE)
                    .description("保险收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE INSURANCE_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    public InsuranceOrder queryByOrderId(String orderNo) {
        return insuranceOrderMapper.selectOne(new LambdaQueryWrapper<InsuranceOrder>().eq(InsuranceOrder::getOrderId, orderNo));
    }

    @Override
    public Integer updateOrderStatusById(InsuranceOrder insuranceOrder) {
        return insuranceOrderMapper.updateById(insuranceOrder);
    }

    @Override
    public int updateIsUseByOrderId(InsuranceOrder insuranceOrder) {
        return insuranceOrderMapper.updateIsUseByOrderId(insuranceOrder);
    }

    @Override
    public R queryInsurance() {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryInsurance  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryInsurance  ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.DISABLE_INSURANCE)) {
            log.error("queryInsurance  ERROR! not found insurance！franchiseeId={}", userInfo.getFranchiseeId());
            return R.ok();
        }

        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("queryInsurance  ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        return R.ok(franchiseeInsuranceService.queryByFranchiseeId(userInfo.getFranchiseeId(), userBattery.getBatteryType(), tenantId));
    }

    @Override
    public R homeOneQueryInsurance(Integer model, Long franchiseeId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryInsurance  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryInsurance  ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.DISABLE_INSURANCE)) {
            log.error("queryInsurance  ERROR! not found insurance！franchiseeId={}", userInfo.getFranchiseeId());
            return R.ok();
        }

        String batteryType = null;
        if (Objects.nonNull(model)) {
            batteryType = BatteryConstant.acquireBatteryShort(model);
        }
        return R.ok(franchiseeInsuranceService.queryByFranchiseeId(franchiseeId, batteryType, tenantId));

    }

    @Override
    public void insert(InsuranceOrder insuranceOrder) {
        insuranceOrderMapper.insert(insuranceOrder);
    }


    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid +
                RandomUtil.randomNumbers(4);
    }

    @Override
    public Triple<Boolean, String, Object> handleRentBatteryInsurance(RentCarHybridOrderQuery query, UserInfo userInfo) {
        if (Objects.isNull(query.getInsuranceId())) {
            return Triple.of(true, "", null);
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(query.getInsuranceId());

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR!not found member_card by id={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR!member_card is un_usable id={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR!payAmount is null ！franchiseeId={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险");
        }


        //生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(userInfo.getTenantId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();

        return Triple.of(true, "", insuranceOrder);
    }

    @Override
    public List<InsuranceOrderVO> queryListByStatus(InsuranceOrderQuery insuranceOrderQuery) {
        List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderMapper.queryList(insuranceOrderQuery);
        if (ObjectUtil.isEmpty(insuranceOrderVOList)) {
            return new ArrayList<>();
        }

        insuranceOrderVOList.parallelStream().forEach(e -> {

            //获取城市名称
            City city = cityService.queryByIdFromDB(e.getCid());
            if (Objects.nonNull(city)) {
                e.setCityName(city.getName());
            }

            Integer validDays = e.getValidDays();
            Long insuranceExpireTime = validDays * (24 * 60 * 60 * 1000L);
            e.setInsuranceExpireTime(insuranceExpireTime);
        });
        return insuranceOrderVOList;
    }
}
