package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageVO;
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
    public R<List<CarRentalPackageVO>> queryCanPurchasePackage(@RequestBody CarRentalPackageQryReq qryReq) {
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

        List<CarRentalPackagePO> entityList = carRentalPackageBizService.queryCanPurchasePackage(qryReq, user.getUid());

        // 获取优惠券ID集
        List<Long> couponIdList = entityList.stream()
                .filter(entity -> ObjectUtils.isNotEmpty(entity.getCouponId()) && entity.getCouponId().longValue() > 0)
                .map(CarRentalPackagePO::getCouponId).distinct().collect(Collectors.toList());

        // 查询赠送的优惠券信息
        Map<Long, Coupon> couponMap = queryCouponForMapByIds(couponIdList);

        // 转换 VO
        List<CarRentalPackageVO> voList = buildVOList(entityList, couponMap);

        return R.ok(voList);
    }

    /**
     * entityList to voList
     * @param entityList
     * @return
     */
    private List<CarRentalPackageVO> buildVOList(List<CarRentalPackagePO> entityList, Map<Long, Coupon> couponMap) {
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
            // 设置辅助业务信息
            packageVO.setGiveCouponAmount(couponMap.getOrDefault(entity.getCouponId(), new Coupon()).getAmount());
            return packageVO;
        }).collect(Collectors.toList());
    }

}
