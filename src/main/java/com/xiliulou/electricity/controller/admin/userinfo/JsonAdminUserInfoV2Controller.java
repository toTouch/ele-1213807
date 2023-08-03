package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.reqparam.qry.userinfo.UserInfoQryReq;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
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
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Resource
    private CarRentalPackageBizService carRentalPackageBizService;

    @Resource
    private UserInfoService userInfoService;


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
        buyOptModel.setDepositType(YesNoEnum.NO.getCode());

        return R.ok(carRentalPackageOrderBizService.bindingPackage(buyOptModel));
    }

    /**
     * 获取用户可以购买的套餐
     * @param qryReq 查询数据模型
     * @return 可购买的套餐数据集，包含赠送优惠券信息
     */
    @PostMapping("/queryCanPurchasePackage")
    public R<List<CarRentalPackageVO>> queryCanPurchasePackage(@RequestBody CarRentalPackageQryReq qryReq) {
        if (!ObjectUtils.allNotNull(qryReq, qryReq.getFranchiseeId(), qryReq.getStoreId(), qryReq.getCarModelId(), qryReq.getUid())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        List<CarRentalPackagePO> entityList = carRentalPackageBizService.queryCanPurchasePackage(qryReq, qryReq.getUid());

        // 转换 VO
        List<CarRentalPackageVO> voList = buildVOList(entityList);

        return R.ok(voList);
    }


    /**
     * entityList to voList
     * @param entityList
     * @return
     */
    private List<CarRentalPackageVO> buildVOList(List<CarRentalPackagePO> entityList) {
        return entityList.stream().map(entity -> {
            CarRentalPackageVO packageVO = new CarRentalPackageVO();
            packageVO.setId(entity.getId());
            packageVO.setName(entity.getName());
            packageVO.setType(entity.getType());
            packageVO.setTenancy(entity.getTenancy());
            packageVO.setTenancyUnit(entity.getTenancyUnit());
            packageVO.setRent(entity.getRent());
            packageVO.setRentRebate(entity.getRentRebate());
            packageVO.setRentRebateTerm(entity.getRentRebateTerm());
            packageVO.setDeposit(entity.getDeposit());
            packageVO.setFreeDeposit(entity.getFreeDeposit());
            packageVO.setConfine(entity.getConfine());
            packageVO.setConfineNum(entity.getConfineNum());
            packageVO.setGiveCoupon(entity.getGiveCoupon());
            packageVO.setRemark(entity.getRemark());
            packageVO.setBatteryVoltage(entity.getBatteryVoltage());
            return packageVO;
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

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .tenantId(tenantId)
                .keywords(userInfoQryReq.getKeywords())
                .offset(Long.valueOf(userInfoQryReq.getOffset()))
                .size(Long.valueOf(userInfoQryReq.getSize()))
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
