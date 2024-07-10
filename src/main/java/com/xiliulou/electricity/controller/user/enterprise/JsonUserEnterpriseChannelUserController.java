package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;
import com.xiliulou.electricity.request.enterprise.EnterpriseUserExitCheckRequest;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
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
    
    @Resource
    private EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    /**
     * 根据UID查询企业渠道骑手信息
     *
     * @param uid
     * @return
     */
    @GetMapping({"/user/enterprise/queryEnterpriseChannelUser", "/merchant/enterprise/queryEnterpriseChannelUser"})
    public R queryEnterpriseChannelUser(@RequestParam(value = "uid", required = true) Long uid) {
        
        return R.ok(enterpriseChannelUserService.queryEnterpriseChannelUser(uid));
    }
    
    /**
     * 修改单个骑手自主续费状态
     *
     * @param enterpriseChannelUserQuery
     * @return
     */
    @PutMapping({"/user/enterprise/updateRenewalStatus", "/merchant/enterprise/updateRenewalStatus"})
    public R updateRenewalStatus(@RequestBody EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        if (!ObjectUtils.allNotNull(enterpriseChannelUserQuery, enterpriseChannelUserQuery.getUid(), enterpriseChannelUserQuery.getRenewalStatus())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        return R.ok(enterpriseChannelUserService.updateRenewStatus(enterpriseChannelUserQuery));
    }
    
    @PostMapping({"/user/enterprise/addUser", "/merchant/enterprise/addUser"})
    public R addUser(@RequestBody @Validated(CreateGroup.class) EnterpriseChannelUserQuery query) {
        
        return returnTripleResult(enterpriseChannelUserService.save(query));
        
    }
    
    @PostMapping({"/user/enterprise/addUserNew", "/merchant/enterprise/addUserNew"})
    public R addUserNew(@RequestBody @Validated(CreateGroup.class) EnterpriseChannelUserQuery query) {
        return returnTripleResult(enterpriseChannelUserService.addUserNew(query));
    }
    
    /**
     * 骑手自主续费检测
     *
     * @param request
     * @return
     */
    @PostMapping({"/user/enterprise/channelUserExitCheck", "/merchant/enterprise/channelUserExitCheck"})
    public R channelUserExitCheck(@RequestBody @Validated(UpdateGroup.class) EnterpriseUserExitCheckRequest request) {
        
        return returnTripleResult(enterpriseChannelUserService.channelUserExitCheck(request));
    }
    
    /**
     * 骑手自主续费检测
     *
     * @param request
     * @return
     */
    @PostMapping({"/user/enterprise/channelUserExitCheckAll", "/merchant/enterprise/channelUserExitCheckAll"})
    public R channelUserExitCheckAll(@RequestBody @Validated(CreateGroup.class) EnterpriseUserExitCheckRequest request) {
        
        return returnTripleResult(enterpriseChannelUserService.channelUserExitCheckAll(request));
    }
    
    /**
     * 骑手自主续费
     *
     * @param request
     * @return
     */
    @PostMapping({"/user/enterprise/channelUserExit", "/merchant/enterprise/channelUserExit"})
    public R channelUserExit(@RequestBody @Validated(UpdateGroup.class) EnterpriseUserExitCheckRequest request) {
        
        return returnTripleResult(enterpriseChannelUserService.channelUserExit(request));
    }
    
    /**
     * 骑手自主续费
     *
     * @param request
     * @return
     */
    @PostMapping({"/user/enterprise/channelUserExitAll", "/merchant/enterprise/channelUserExitAll"})
    public R channelUserExitAll(@RequestBody @Validated(CreateGroup.class) EnterpriseUserExitCheckRequest request) {
        
        return returnTripleResult(enterpriseChannelUserService.channelUserExitAll(request));
    }
    
    /**
     * 骑手自主续费关闭
     *
     * @param request
     * @return
     */
    @PostMapping({"/user/enterprise/channelUserClose", "/merchant/enterprise/channelUserClose"})
    public R channelUserClose(@RequestBody @Validated(CreateGroup.class) EnterpriseUserExitCheckRequest request) {
        
        return returnTripleResult(enterpriseChannelUserService.channelUserClose(request));
    }
    
    
    /**
     * 骑手概览
     *
     * @return
     */
    @GetMapping({"/user/enterprise/user/queryEnterpriseChannelUserList", "/merchant/enterprise/user/queryEnterpriseChannelUserList"})
    public R queryEnterpriseChannelUserList() {
        
        return returnTripleResult(enterpriseChannelUserService.queryEnterpriseChannelUserList());
    }
    
    
    /**
     * 根据手机号查询当前加盟商下的企业渠道用户信息
     *
     * @param phone
     * @param
     * @return
     */
    @GetMapping({"/user/enterprise/queryUser", "/merchant/enterprise/queryUser"})
    public R queryUser(@RequestParam(value = "phone", required = true) String phone) {
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().phone(phone).tenantId(tenantId.longValue()).build();
        
        return returnTripleResult(enterpriseChannelUserService.queryUser(enterpriseChannelUserQuery));
    }
    
    /**
     * 企业生成二维码前，创建用户邀请记录
     *
     * @param enterpriseId
     * @param
     * @return
     */
    @GetMapping({"/user/enterprise/generateEnterpriseUser", "/merchant/enterprise/generateEnterpriseUser"})
    public R generateUserRecord(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().enterpriseId(enterpriseId)
                .tenantId(TenantContextHolder.getTenantId().longValue()).build();
        
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
    
    /**
     * 被邀请用户扫码后，将该用户添加至关联的企业中
     *
     * @param id
     * @param uid
     * @return
     */
    @GetMapping("/user/enterprise/addUserByScanNew")
    public R addUserByScanNew(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "uid", required = true) Long uid) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().id(id).uid(uid).build();
        
        return returnTripleResult(enterpriseChannelUserService.updateUserAfterQRScanNew(enterpriseChannelUserQuery));
    }
    
    /**
     * 被邀请用户扫码后，将该用户添加至关联的企业中
     *
     * @param id
     * @param uid
     * @return
     */
    @GetMapping("/user/enterprise/addUserByScanNewCheck")
    public R addUserByScanNewCheck(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "uid", required = true) Long uid) {
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = EnterpriseChannelUserQuery.builder().id(id).uid(uid).build();
        
        return returnTripleResult(enterpriseChannelUserService.addUserByScanNewCheck(enterpriseChannelUserQuery));
    }
    
    @GetMapping({"/user/enterprise/checkChannelUser", "/merchant/enterprise/checkChannelUser"})
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
    @GetMapping({"/user/enterprise/queryRiderDetails", "/merchant/enterprise/queryRiderDetails"})
    public R queryRiderDetails(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        
        EnterpriseMemberCardQuery query = EnterpriseMemberCardQuery.builder().enterpriseId(enterpriseId).uid(uid).orderNo(orderNo).build();
        
        // return returnTripleResult(enterpriseBatteryPackageService.queryRiderDetails(query));
        return returnTripleResult(enterpriseUserCostRecordService.queryRiderDetails(query));
    }
    
    /**
     * 查询骑手消费详情
     *
     * @param enterpriseId
     * @param uid
     * @return
     */
    @GetMapping({"/user/enterprise/queryRiderCostDetails", "/merchant/enterprise/queryRiderCostDetails"})
    public R queryRiderCostDetails(@RequestParam(value = "enterpriseId", required = true) Long enterpriseId, @RequestParam(value = "uid", required = true) Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        /*EnterprisePackageOrderQuery query = EnterprisePackageOrderQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();*/
        //return returnTripleResult(enterpriseBatteryPackageService.queryCostDetails(query));
        
        EnterpriseUserCostRecordQuery enterpriseUserCostRecordQuery = EnterpriseUserCostRecordQuery.builder().enterpriseId(enterpriseId).uid(uid).beginTime(beginTime)
                .endTime(endTime).build();
        
        return R.ok(enterpriseUserCostRecordService.queryUserCostRecordList(enterpriseUserCostRecordQuery));
    }
    
    /**
     * 查询企业侧已支付，待支付，未支付骑手对应的套餐信息
     *
     * @param enterpriseId  企业ID
     * @param paymentStatus 1- 代付到期， 2-已代付， 3-未代付
     * @param userName
     * @param phone
     * @return
     */
    @GetMapping({"/user/enterprise/queryPurchaseOrder", "/merchant/enterprise/queryPurchaseOrder"})
    public R queryPurchaseOrder(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "enterpriseId", required = true) Long enterpriseId,
            @RequestParam(value = "paymentStatus", required = true) Integer paymentStatus, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterprisePurchaseOrderQuery enterprisePurchaseOrderQuery = EnterprisePurchaseOrderQuery.builder().enterpriseId(enterpriseId).paymentStatus(paymentStatus)
                .userName(userName).phone(phone).tenantId(tenantId.longValue()).currentTime(System.currentTimeMillis()).offset(offset).size(size).build();
        
        return returnTripleResult(enterpriseBatteryPackageService.queryPurchasedPackageOrders(enterprisePurchaseOrderQuery));
        
    }
    
    /**
     * 查询当前骑手的电池信息
     *
     * @param uid
     * @return
     */
    @GetMapping({"/user/enterprise/queryBattery", "/merchant/enterprise/queryBattery"})
    public R queryBattery(@RequestParam(value = "uid", required = true) Long uid) {
        return R.ok(enterpriseChannelUserService.queryBatteryByUid(uid));
    }
    
    
}
