package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FaqMapper;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.query.UnionTradeOrderAdd;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 混合支付(UnionTradeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-07 14:06:24
 */
@Service("tradeOrderService")
@Slf4j
public class TradeOrderServiceImpl implements TradeOrderService {


    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    EleDepositOrderService eleDepositOrderService;

    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createOrder(UnionTradeOrderAdd unionTradeOrderAdd, HttpServletRequest request) {

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
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL UID={}", user.getUid());
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
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user! userId={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            log.error("payDeposit  ERROR! user is rent deposit! ,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(unionTradeOrderAdd.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={}", unionTradeOrderAdd.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        BigDecimal depositPayAmount = null;

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(unionTradeOrderAdd.getModel())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            //型号押金
            List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={}", unionTradeOrderAdd.getFranchiseeId());
                return R.fail("ELECTRICITY.00110", "未找到押金");
            }


            for (Map map : modelBatteryDepositList) {
                if ((double) (map.get("model")) - unionTradeOrderAdd.getModel() < 1 && (double) (map.get("model")) - unionTradeOrderAdd.getModel() >= 0) {
                    depositPayAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
                    break;
                }
            }

        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{}", unionTradeOrderAdd.getFranchiseeId());
            return R.fail("ELECTRICITY.00110", "未找到押金");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(unionTradeOrderAdd.getInsuranceId());

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={}", unionTradeOrderAdd.getInsuranceId());
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", unionTradeOrderAdd.getInsuranceId());
            return R.fail("100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={}", unionTradeOrderAdd.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }

        //生成押金独立订单
        String orderId = generateDepositOrderId(user.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId)
                .franchiseeId(franchisee.getId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(null)
                .modelType(franchisee.getModelType()).build();

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            eleDepositOrder.setBatteryType(BatteryConstant.acquireBatteryShort(unionTradeOrderAdd.getModel()));
        }
        eleDepositOrderService.insert(eleDepositOrder);

        //生成保险独立订单
        String insuranceOrderId = generateInsuranceOrderId(user.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(tenantId)
                .uid(user.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        insuranceOrderService.insert(insuranceOrder);

        List<String> orderList = new ArrayList<>();
        orderList.add(orderId);
        orderList.add(insuranceOrderId);

        List<Integer> orderTypeList = new ArrayList<>();
        orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
        orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);

        List<BigDecimal> allPayAmount = new ArrayList<>();
        allPayAmount.add(depositPayAmount);
        allPayAmount.add(franchiseeInsurance.getPremium());

        //调起支付
        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount))
                    .payAmount(depositPayAmount.add(franchiseeInsurance.getPremium()))
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_UNION_INSURANCE_AND_DEPOSIT)
                    .description("保险押金联合收费")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO =
                    unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }


    private String generateDepositOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }

    private String generateInsuranceOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid +
                RandomUtil.randomNumbers(4);
    }
}
