package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.mapper.InsuranceUserInfoMapper;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    FranchiseeService franchiseeService;


    @Override
    public R queryList(InsuranceOrderQuery insuranceOrderQuery) {

        List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderMapper.queryList(insuranceOrderQuery);
        if (ObjectUtil.isEmpty(insuranceOrderVOList)) {
            return R.ok(new ArrayList<>());
        }

        insuranceOrderVOList.parallelStream().forEach(e -> {
            Integer validDays = e.getValidDays();
            Long insuranceExpireTime = validDays * (24 * 60 * 60 * 1000L);
            e.setInsuranceExpireTime(insuranceExpireTime);
        });

        return R.ok(insuranceOrderVOList);
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
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("CREATE INSURANCE_ORDER ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }


        Franchisee franchisee = franchiseeService.queryByIdFromDB(insuranceOrderAdd.getFranchiseeId().longValue());
        if (Objects.isNull(franchisee)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found Franchisee ！franchiseeId{}", insuranceOrderAdd.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrderAdd.getInsuranceId());

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID:{}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100305", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId{}", insuranceOrderAdd.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }

        //生成保险订单
        String orderId = generateOrderId(user.getUid());

        InsuranceOrder insuranceOrder=InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(orderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(tenantId)
                .uid(user.getUid())
                .userName(userInfo.getUserName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();




        return null;
    }

    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }
}
