package com.xiliulou.electricity.controller.user.car.v2;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.v2.CarRenalPackageDepositV2BizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车押金的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/deposit/v2")
public class JsonUserCarDepositPayV2Controller extends BasicController {
    
    @Resource
    private CarRenalPackageDepositV2BizService carRenalPackageDepositV2BizService;

    
    /**
     * 查询免押状态
     *
     * @return true(成功)、false(失败)
     */
    @GetMapping("/pay/queryFreeDepositStatus")
    public R<FreeDepositUserInfoVo> queryFreeDepositStatus() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(carRenalPackageDepositV2BizService.queryFreeDepositStatus(tenantId, user.getUid()));
    }
    
    /**
     * 创建免押订单
     *
     * @param freeDepositOptReq 免押订单申请数据模型
     * @return 生成二维码的网址
     */
    @PostMapping("/pay/createFreeDeposit")
    public R<String> createFreeDeposit(@RequestBody @Valid FreeDepositOptReq freeDepositOptReq) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(carRenalPackageDepositV2BizService.createFreeDeposit(tenantId, user.getUid(), freeDepositOptReq));
    }
    
    /**
     * 退押申请
     *
     * @param depositPayOrderNo 押金缴纳订单编码
     * @return true(成功)、false(失败)
     */
    @GetMapping("/refund/refundDeposit")
    public R<Boolean> refundDeposit(String depositPayOrderNo) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(carRenalPackageDepositV2BizService.refundDeposit(tenantId, user.getUid(), depositPayOrderNo));
    }
    
    /**
     * 用户名下的押金信息(单车、车电一体)
     *
     * @return
     */
    @GetMapping("/queryUnRefundCarDeposit")
    public R<CarRentalPackageDepositPayVo> queryUnRefundCarDeposit() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(carRenalPackageDepositV2BizService.selectUnRefundCarDeposit(tenantId, user.getUid()));
    }
}
