package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.SystemDefinitionEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.reqparam.opt.carpackage.FreezeRentOrderOptReq;
import com.xiliulou.electricity.reqparam.opt.carpackage.MemberCurrPackageOptReq;
import com.xiliulou.electricity.reqparam.qry.userinfo.UserInfoQryReq;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageVo;
import com.xiliulou.electricity.vo.userinfo.UserInfoVO;
import com.xiliulou.electricity.vo.userinfo.UserMemberInfoVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户信息Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/userInfo/v2")
public class JsonAdminUserInfoV2Controller {

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Resource
    private CarRentalPackageBizService carRentalPackageBizService;

    @Resource
    private UserInfoService userInfoService;

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 编辑会员当前套餐信息
     * @param optReq 操作数据模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/updateCurrPackage")
    public R<Boolean> updateCurrPackage(@RequestBody MemberCurrPackageOptReq optReq) {
        if (!ObjectUtils.allNotNull(optReq, optReq.getUid(), optReq.getPackageOrderNo(), optReq.getType())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageMemberTermBizService.updateCurrPackage(tenantId, optReq, user.getUid(), user.getUsername()));

    }

    /**
     * 解绑车辆
     * @param uid 用户ID
     * @return true(成功)、false(失败)
     */
    @GetMapping("/unBindingCar")
    public R<Boolean> unBindingCar(Long uid) {
        if (ObjectUtils.isEmpty(uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalOrderBizService.unBindingCar(tenantId, uid, user.getUid()));
    }

    /**
     * 绑定车辆
     * @param carSn 车辆SN码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/bindingCar")
    public R<Boolean> bindingCar(String carSn, Long uid) {
        if (!ObjectUtils.allNotNull(carSn, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalOrderBizService.bindingCar(tenantId, uid, carSn, user.getUid()));
    }

    /**
     * 清空滞纳金
     * @param uid 用户UID
     * @return
     */
    @GetMapping("/clearSlippage")
    public R<Boolean> clearSlippage(Long uid) {
        if (ObjectUtils.isEmpty(uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageSlippageBizService.clearSlippage(tenantId, uid, user.getUid()));
    }

    /**
     * 启用冻结套餐订单
     * @param packageOrderNo 购买订单编号
     * @return true(成功)、false(失败)
     */
    @GetMapping("/enableFreezeRentOrder")
    public R<Boolean> enableFreezeRentOrder(String packageOrderNo, Long uid) {
        if (StringUtils.isBlank(packageOrderNo) || ObjectUtils.isEmpty(uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageOrderBizService.enableFreezeRentOrder(tenantId, uid, packageOrderNo, user.getUid(),user.getUsername()));
    }

    /**
     * 冻结套餐订单
     * @param freezeRentOrderoptReq 请求操作数据模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/freezeRentOrder")
    public R<Boolean> freezeRentOrder(@RequestBody FreezeRentOrderOptReq freezeRentOrderoptReq) {
        if (!ObjectUtils.allNotNull(freezeRentOrderoptReq, freezeRentOrderoptReq.getUid(), freezeRentOrderoptReq.getApplyTerm(), freezeRentOrderoptReq.getApplyTerm())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean freezeFlag = carRentalPackageOrderBizService.freezeRentOrder(tenantId, freezeRentOrderoptReq.getUid(), freezeRentOrderoptReq.getPackageOrderNo(), freezeRentOrderoptReq.getApplyTerm(),
                freezeRentOrderoptReq.getApplyReason(), SystemDefinitionEnum.BACKGROUND, user.getUid(), user.getUsername());
        return R.ok(freezeFlag);

    }

    /**
     * 获取会员的全量信息（套餐订单信息、车辆信息）
     * @param uid 用户ID
     * @return 用户会员全量信息（套餐订单信息、车辆信息）
     */
    @GetMapping("/queryUserMemberInfo")
    public R<UserMemberInfoVo> queryUserMemberInfo(Long uid) {
        if (ObjectUtils.isEmpty(uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        return R.ok(carRentalPackageMemberTermBizService.queryUserMemberInfo(tenantId, uid));

    }

    /**
     * 给用户绑定套餐
     * @param buyOptModel 绑定数据模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/bindingPackage")
    public R<Boolean> bindingPackage(@RequestBody CarRentalPackageOrderBuyOptModel buyOptModel ) {
        if (!ObjectUtils.allNotNull(buyOptModel, buyOptModel.getUid(), buyOptModel.getFranchiseeId(), buyOptModel.getStoreId(), buyOptModel.getRentalPackageId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        buyOptModel.setTenantId(tenantId);
        buyOptModel.setPayType(PayTypeEnum.OFF_LINE.getCode());

        return R.ok(carRentalPackageOrderBizService.bindingPackage(buyOptModel));
    }

    /**
     * 获取用户可以购买的套餐
     * @param qryReq 查询数据模型
     * @return 可购买的套餐数据集，包含赠送优惠券信息
     */
    @PostMapping("/queryCanPurchasePackage")
    public R<List<CarRentalPackageVo>> queryCanPurchasePackage(@RequestBody CarRentalPackageQryReq qryReq) {
        if (!ObjectUtils.allNotNull(qryReq, qryReq.getFranchiseeId(), qryReq.getStoreId(), qryReq.getCarModelId(), qryReq.getUid())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        List<CarRentalPackagePo> entityList = carRentalPackageBizService.queryCanPurchasePackage(qryReq, qryReq.getUid());

        // 转换 VO
        List<CarRentalPackageVo> voList = buildVOList(entityList);

        return R.ok(voList);
    }


    /**
     * entityList to voList
     * @param entityList
     * @return
     */
    private List<CarRentalPackageVo> buildVOList(List<CarRentalPackagePo> entityList) {
        return entityList.stream().map(entity -> {
            CarRentalPackageVo packageVo = new CarRentalPackageVo();
            packageVo.setId(entity.getId());
            packageVo.setName(entity.getName());
            packageVo.setType(entity.getType());
            packageVo.setTenancy(entity.getTenancy());
            packageVo.setTenancyUnit(entity.getTenancyUnit());
            packageVo.setRent(entity.getRent());
            packageVo.setRentRebate(entity.getRentRebate());
            packageVo.setRentRebateTerm(entity.getRentRebateTerm());
            packageVo.setDeposit(entity.getDeposit());
            packageVo.setFreeDeposit(entity.getFreeDeposit());
            packageVo.setConfine(entity.getConfine());
            packageVo.setConfineNum(entity.getConfineNum());
            packageVo.setGiveCoupon(entity.getGiveCoupon());
            packageVo.setRemark(entity.getRemark());
            packageVo.setBatteryVoltage(entity.getBatteryVoltage());
            return packageVo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据关键字查询用户集
     * @param userInfoQryReq 查询模型
     * @return 用户集
     */
    @PostMapping("/queryByKeywords")
    public R<List<UserInfoVO>> queryByKeywords(@RequestBody UserInfoQryReq userInfoQryReq) {
        if (ObjectUtils.isEmpty(userInfoQryReq)) {
            userInfoQryReq = new UserInfoQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .tenantId(tenantId)
                .keywords(userInfoQryReq.getKeywords())
                .offset(Long.valueOf(userInfoQryReq.getOffset()))
                .size(Long.valueOf(userInfoQryReq.getSize()))
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .build();

        List<UserInfo> userInfos = userInfoService.page(userInfoQuery);
        if (CollectionUtils.isEmpty(userInfos)) {
            return R.ok();
        }

        List<UserInfoVO> userInfoVoList = userInfos.stream().map(userInfo -> {
            // 拼装返回字段
            UserInfoVO userInfoVo = new UserInfoVO();
            userInfoVo.setId(userInfo.getId());
            userInfoVo.setUid(userInfo.getUid());
            userInfoVo.setName(userInfo.getName());
            userInfoVo.setPhone(userInfo.getPhone());

            // 赋值复合字段
            StringBuilder builderNameAndPhone = new StringBuilder();
            if (StringUtils.isNotBlank(userInfo.getName())) {
                builderNameAndPhone.append(userInfo.getName());
            }
            if (StringUtils.isNotBlank(builderNameAndPhone.toString())) {
                builderNameAndPhone.append("/");
            }
            if (StringUtils.isNotBlank(userInfo.getPhone())) {
                builderNameAndPhone.append(userInfo.getPhone());
            }
            userInfoVo.setNameAndPhone(builderNameAndPhone.toString());

            return userInfoVo;
        }).collect(Collectors.toList());

        return R.ok(userInfoVoList);
    }
}
