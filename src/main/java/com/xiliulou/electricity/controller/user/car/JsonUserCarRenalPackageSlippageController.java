package com.xiliulou.electricity.controller.user.car;

import cn.hutool.core.util.NumberUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderSlippageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 车辆套餐逾期订单 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackage/slippage")
public class JsonUserCarRenalPackageSlippageController extends BasicController {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    /**
     * 套餐逾期订单-条件查询列表
     * @param offset 偏移量
     * @param size 取值数量
     * @return 逾期订单集
     */
    @GetMapping("/page")
    public R<List<CarRentalPackageOrderSlippageVO>> page(Integer offset, Integer size) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());
        qryModel.setOffset(offset);
        qryModel.setSize(size);

        // 调用服务
        List<CarRentalPackageOrderSlippagePO> carRentalPackageSlippageEntityList = carRentalPackageOrderSlippageService.page(qryModel);
        if (CollectionUtils.isEmpty(carRentalPackageSlippageEntityList)) {
            return R.ok(Collections.emptyList());
        }

        List<CarRentalPackageOrderSlippageVO> carRentalPackageSlippageVoList = new ArrayList<>();
        long nowTime = System.currentTimeMillis();

        for (CarRentalPackageOrderSlippagePO slippageEntity : carRentalPackageSlippageEntityList) {
            CarRentalPackageOrderSlippageVO slippageVo = new CarRentalPackageOrderSlippageVO();
            BeanUtils.copyProperties(slippageEntity, slippageVo);

            Integer payState = slippageVo.getPayState();
            // 未支付、支付失败，计算滞纳金金额
            if (PayStateEnum.UNPAID.getCode().equals(payState) || PayStateEnum.FAILED.getCode().equals(payState) ) {
                // 结束时间，不为空
                if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                    nowTime = slippageEntity.getLateFeeEndTime().longValue();
                }

                // 时间比对
                long lateFeeStartTime = slippageEntity.getLateFeeStartTime().longValue();
                // 没有滞纳金产生
                if (lateFeeStartTime < nowTime) {
                    continue;
                }

                // 转换天
                long diffDay = DateUtils.diffDay(nowTime, lateFeeStartTime);
                // 计算滞纳金金额
                BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee()).setScale(2, RoundingMode.HALF_UP);

                slippageVo.setLateFeePayable(amount);
            }

            carRentalPackageSlippageVoList.add(slippageVo);
        }

        return R.ok(carRentalPackageSlippageVoList);
    }

    /**
     * 套餐逾期订单-条件查询总数
     * @return 总数
     */
    @GetMapping("/count")
    public R<Integer> count() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());;

        // 调用服务
        return R.ok(carRentalPackageOrderSlippageService.count(qryModel));
    }


}
