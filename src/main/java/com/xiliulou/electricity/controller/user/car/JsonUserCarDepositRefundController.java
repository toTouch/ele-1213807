package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositRefundVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车押金返还 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/deposit/refund")
public class JsonUserCarDepositRefundController extends BasicController {

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    /**
     * 退押申请
     * @param depositPayOrderNo 押金缴纳订单编码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/refundDeposit")
    public R<Boolean> refundDeposit(String depositPayOrderNo) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageDepositBizService.refundDeposit(tenantId, user.getUid(), depositPayOrderNo));
    }

    /**
     * 分页查询
     * @param offset 偏移量
     * @param size 取值数量
     * @return 押金退还订单集
     */
    @GetMapping("/page")
    public R<List<CarRentalPackageDepositRefundVo>> page(Integer offset, Integer size) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());
        qryModel.setRefundState(RefundStateEnum.SUCCESS.getCode());
        qryModel.setOffset(offset);
        qryModel.setSize(size);

        // 调用服务
        List<CarRentalPackageDepositRefundPo> depositRefundEntityList = carRentalPackageDepositRefundService.page(qryModel);
        if (CollectionUtils.isEmpty(depositRefundEntityList)) {
            return R.ok(Collections.emptyList());
        }


        // 模型转换，封装返回
        List<CarRentalPackageDepositRefundVo> depositRefundVoList = depositRefundEntityList.stream().map(depositRefundEntity -> {
            CarRentalPackageDepositRefundVo depositRefundVo = new CarRentalPackageDepositRefundVo();
            BeanUtils.copyProperties(depositRefundEntity, depositRefundVo);
            return depositRefundVo;
        }).collect(Collectors.toList());

        return R.ok(depositRefundVoList);
    }

    /**
     * 查询总数
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

        CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());
        qryModel.setRefundState(RefundStateEnum.SUCCESS.getCode());

        // 调用服务
        return R.ok(carRentalPackageDepositRefundService.count(qryModel));
    }
}
