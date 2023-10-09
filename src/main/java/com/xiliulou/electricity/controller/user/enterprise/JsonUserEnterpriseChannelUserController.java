package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/21 11:08
 */
@Slf4j
@RestController
public class JsonUserEnterpriseChannelUserController extends BaseController {
    
    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    private EnterpriseBatteryPackageService enterpriseBatteryPackageService;
    
    /**
     * 根据UID查询企业渠道骑手信息
     * @param uid
     * @return
     */
    @GetMapping("/user/enterprise/queryEnterpriseChannelUser")
    public R queryEnterpriseChannelUser(@RequestParam(value = "uid", required = true) Long uid) {
        
        return R.ok(enterpriseChannelUserService.queryEnterpriseChannelUser(uid));
    }
    
    /**
     * 修改骑手自主续费状态
     * @param enterpriseChannelUserQuery
     * @return
     */
    @PutMapping("/user/enterprise/updateRenewalStatus")
    public R updateRenewalStatus(@RequestBody @Validated(UpdateGroup.class) EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        
        return R.ok(enterpriseChannelUserService.updateChannelUser(enterpriseChannelUserQuery));
    }
    
    @PostMapping("/user/enterprise/addUser")
    public R addUser(@RequestBody @Validated(CreateGroup.class) EnterpriseChannelUserQuery query) {
        
        return R.ok(enterpriseChannelUserService.save(query));
        
    }
    
    /**
     * 根据手机号查询当前加盟商下的企业渠道用户信息
     *
     * @param phone
     * @param
     * @return
     */
    @GetMapping("/user/enterprise/queryUser")
    public R queryUser(@RequestParam(value = "phone", required = true) String phone) {
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .phone(phone)
                .tenantId(tenantId.longValue())
                .build();
        
        return returnTripleResult(enterpriseChannelUserService.queryUser(enterpriseChannelUserQuery));
    }
    
    /**
     * 企业生成二维码前，创建用户邀请记录
     *
     * @param enterpriseId
     * @param
     * @return
     */
    @GetMapping("/user/enterprise/generateEnterpriseUser")
    public R generateUserRecord(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder()
                .enterpriseId(enterpriseId)
                .tenantId(TenantContextHolder.getTenantId().longValue())
                .build();
        
        return returnTripleResult(enterpriseChannelUserService.generateChannelUser(enterpriseChannelUserQuery));
    }
    
    /**
     * 被邀请用户扫码后，将该用户添加至关联的企业中
     *
     * @param id
     * @param uid
     * @param renewalStatus
     * @return
     */
    @GetMapping("/user/enterprise/addUserByScan")
    public R addUserByScan(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "renewalStatus", required = true) Integer renewalStatus) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().id(id).uid(uid).renewalStatus(renewalStatus).build();
        
        return returnTripleResult(enterpriseChannelUserService.updateUserAfterQRScan(enterpriseChannelUserQuery));
    }
    
    @GetMapping("/user/enterprise/checkChannelUser")
    public R checkChannelUser(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "uid", required = false) Long uid) {
        
        return returnTripleResult(enterpriseChannelUserService.checkUserExist(id, uid));
    }
    
    /**
     * 查询骑手详情
     *
     * @param enterpriseId
     * @param uid
     * @return
     */
    @GetMapping("/user/enterprise/queryRiderDetails")
    public R queryRiderDetails(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid) {
        
        EnterpriseMemberCardQuery query = EnterpriseMemberCardQuery.builder().enterpriseId(enterpriseId).uid(uid).build();
        
        return returnTripleResult(enterpriseBatteryPackageService.queryRiderDetails(query));
    }
    
    /**
     * 查询骑手消费详情
     *
     * @param enterpriseId
     * @param uid
     * @return
     */
    @GetMapping("/user/enterprise/queryRiderCostDetails")
    public R queryRiderCostDetails(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid) {
        
        EnterprisePackageOrderQuery query = EnterprisePackageOrderQuery.builder().enterpriseId(enterpriseId).uid(uid).build();
        
        return returnTripleResult(enterpriseBatteryPackageService.queryCostDetails(query));
    }
    
    /**
     * 查询企业侧已支付，待支付，未支付骑手对应的套餐信息
     * @param enterpriseId 企业ID
     * @param paymentStatus 1- 代付到期， 2-已代付， 3-未代付
     * @param userName
     * @param phone
     * @return
     */
    @GetMapping("/user/enterprise/queryPurchaseOrder")
    public R queryPurchaseOrder(@RequestParam("offset") long offset,
                                @RequestParam("size") long size,
                                @RequestParam(value = "enterpriseId", required = true) Long enterpriseId,
                                @RequestParam(value = "paymentStatus", required = true) Integer paymentStatus,
                                @RequestParam(value = "userName", required = false) String userName,
                                @RequestParam(value = "phone", required = false) String phone) {
    
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterprisePurchaseOrderQuery enterprisePurchaseOrderQuery = EnterprisePurchaseOrderQuery.builder()
                .enterpriseId(enterpriseId)
                .paymentStatus(paymentStatus)
                .userName(userName)
                .phone(phone)
                .tenantId(tenantId)
                .currentTime(System.currentTimeMillis())
                .offset(offset)
                .size(size)
                .build();
    
        return returnTripleResult(enterpriseBatteryPackageService.queryPurchasedPackageOrders(enterprisePurchaseOrderQuery));
        
    }
    
}
