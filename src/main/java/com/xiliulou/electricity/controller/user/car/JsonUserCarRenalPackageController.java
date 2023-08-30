package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车套餐相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackage")
public class JsonUserCarRenalPackageController extends BasicController {

    @Resource
    private CarRentalPackageBizService carRentalPackageBizService;

    /**
     * 获取用户可以购买的套餐
     * @param qryReq 查询数据模型
     * @return 可购买的套餐数据集，包含赠送优惠券信息
     */
    @PostMapping("/queryCanPurchasePackage")
    public R<List<CarRentalPackageVo>> queryCanPurchasePackage(@RequestBody CarRentalPackageQryReq qryReq) {
        if (!ObjectUtils.allNotNull(qryReq, qryReq.getFranchiseeId(), qryReq.getStoreId(), qryReq.getCarModelId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        qryReq.setTenantId(tenantId);

        List<CarRentalPackagePo> entityList = carRentalPackageBizService.queryCanPurchasePackage(qryReq, user.getUid());

        // 获取优惠券ID集
        List<Long> couponIdList = entityList.stream()
                .filter(entity -> ObjectUtils.isNotEmpty(entity.getCouponId()) && YesNoEnum.YES.getCode().equals(entity.getGiveCoupon()))
                .map(CarRentalPackagePo::getCouponId).distinct().collect(Collectors.toList());

        // 查询赠送的优惠券信息
        Map<Long, Coupon> couponMap = getCouponForMapByIds(couponIdList);

        // 转换 VO
        List<CarRentalPackageVo> voList = buildVOList(entityList, couponMap);

        return R.ok(voList);
    }

    /**
     * entityList to voList
     * @param entityList
     * @return
     */
    private List<CarRentalPackageVo> buildVOList(List<CarRentalPackagePo> entityList, Map<Long, Coupon> couponMap) {
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
            packageVo.setRemark(entity.getRemark());
            packageVo.setBatteryVoltage(entity.getBatteryVoltage());
            packageVo.setGiveCoupon(entity.getGiveCoupon());
            // 设置辅助业务信息
            if (YesNoEnum.YES.getCode().equals(entity.getGiveCoupon())) {
                packageVo.setGiveCouponAmount(couponMap.getOrDefault(entity.getCouponId(), new Coupon()).getAmount());
            }
            return packageVo;
        }).collect(Collectors.toList());
    }

}
