package com.xiliulou.electricity.controller.admin.car.v2;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.query.car.audit.AuditOptReq;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.v2.CarRenalPackageDepositV2BizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 租车套餐押金退押 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageDepositRefund/v2")
public class JsonAdminCarRentalPackageDepositRefundV2Controller extends BasicController {
    
    @Resource
    private CarRenalPackageDepositV2BizService carRenalPackageDepositV2BizService;
    
    /**
     * 运营商端创建退押，特殊退押(2.0过度数据)
     *
     * @param optModel 操作实体类
     * @return true(成功)、false(失败)
     */
    @PostMapping("/createSpecial")
    public R<Boolean> createSpecial(@RequestBody CarRentalPackageDepositRefundOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        optModel.setTenantId(tenantId);
        
        return R.ok(carRenalPackageDepositV2BizService.refundDepositCreateSpecial(optModel, user.getUid()));
    }
    
    /**
     * 创建退押
     *
     * @param optModel 操作实体类
     * @return true(成功)、false(失败)
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody CarRentalPackageDepositRefundOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        optModel.setTenantId(tenantId);
        
        return R.ok(carRenalPackageDepositV2BizService.refundDepositCreate(optModel, user.getUid()));
    }
    
    /**
     * 审核拒绝
     *
     * @param optReq 审核操作数据
     * @return true(成功)、false(失败)
     */
    @PostMapping("/auditReject")
    public R<Boolean> auditReject(@RequestBody AuditOptReq optReq) {
        if (!ObjectUtils.allNotNull(optReq, optReq.getOrderNo(), optReq.getReason())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(carRenalPackageDepositV2BizService.approveRefundDepositOrder(optReq.getOrderNo(), false, optReq.getReason(), user.getUid(), null, optReq.getCompelOffLine()));
    }
    
    /**
     * 审核通过
     *
     * @param optReq 审核操作数据
     * @return true(成功)、false(失败)
     */
    @PostMapping("/approved")
    public R<Boolean> approved(@RequestBody AuditOptReq optReq) {
        if (!ObjectUtils.allNotNull(optReq, optReq.getOrderNo())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(carRenalPackageDepositV2BizService.approveRefundDepositOrder(optReq.getOrderNo(), true, optReq.getReason(), user.getUid(), optReq.getAmount(),
                optReq.getCompelOffLine()));
    }
    
}
