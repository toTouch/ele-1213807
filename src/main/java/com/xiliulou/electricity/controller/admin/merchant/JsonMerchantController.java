package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantUnbindReq;
import com.xiliulou.electricity.request.merchant.*;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:10
 */
@Slf4j
@RestController
public class JsonMerchantController extends BaseController {
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    /**
     * 查询商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeConditionInfo")
    public R upgradeConditionInfo(@RequestParam(value = "franchiseeId") Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(merchantAttrService.queryUpgradeCondition(franchiseeId));
    }
    
    /**
     * 修改商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeCondition")
    @Log(title = "修改商户升级条件")
    public R updateUpgradeCondition(@RequestParam("condition") Integer condition,@RequestParam(value = "franchiseeId") Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(merchantAttrService.updateUpgradeCondition(franchiseeId, condition));
    }
    
    /**
     * 修改邀请条件
     */
    @PutMapping("/admin/merchantAttr/invitationCondition")
    @Log(title = "修改邀请条件")
    public R updateInvitationCondition(@RequestBody @Validated MerchantAttrRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(merchantAttrService.updateInvitationCondition(request));
    }
    
    /**
     * 修改渠道员变更返利开关
     */
    @GetMapping("/admin/merchantAttr/switchState")
    @Log(title = "修改渠道员变更返利开关")
    public R updateChannelSwitchState(@RequestParam("status") Integer status,@RequestParam("franchiseeId") Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(merchantAttrService.updateChannelSwitchState(franchiseeId, status));
    }
    
    /**
     * 保存
     *
     * @param merchantSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) MerchantSaveRequest merchantSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("merchant save warn! not find user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 商户保存权限 admin,租户，加盟商
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.warn("merchant save warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant save warn! franchisee is empty");
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            merchantSaveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        Triple<Boolean, String, Object> r = merchantService.save(merchantSaveRequest);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        return R.ok();
    }
    
    /**
     * 修改
     *
     * @param merchantSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/update")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) MerchantSaveRequest merchantSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant update warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            merchantSaveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        Triple<Boolean, String, Object> r = merchantService.update(merchantSaveRequest);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = (MerchantDeleteCacheDTO) r.getRight();
        merchantService.deleteCache(merchantDeleteCacheDTO);
        
        return R.ok();
    }
    
    /**
     * 删除商户
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/admin/merchant/delete")
    @Log(title = "删除商户")
    public R delete(@RequestParam(value = "id", required = true) Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant delete warn! franchisee is empty");
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Triple<Boolean, String, Object> r = merchantService.remove(id, franchiseeIds);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        // 删除缓存
        MerchantDeleteCacheDTO merchantDeleteCacheDTO = (MerchantDeleteCacheDTO) r.getRight();
        merchantService.deleteCache(merchantDeleteCacheDTO);
        
        return R.ok();
    }
    
    /**
     * @param
     * @description 修补历史企业数据
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/repairEnterprise")
    public R repairEnterprise(@RequestParam(value = "tenantId", required = false) Integer tenantId) {
        List<Long> enterpriseIds = new ArrayList<>();
        List<Long> merchantIds = new ArrayList<>();
        
        merchantService.repairEnterprise(enterpriseIds, merchantIds, tenantId);
        
        merchantService.deleteCacheForRepairEnterprise(enterpriseIds, merchantIds);
        return R.ok();
    }
    
    /**
     * @param
     * @description 商户列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId ,@RequestParam(value = "merchantGradeId") Long merchantGradeId,
            @RequestParam(value = "channelEmployeeUid", required = false) Long channelEmployeeUid, @RequestParam(value = "phone", required = false) String phone) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant page count warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).tenantId(TenantContextHolder.getTenantId()).franchiseeIdList(franchiseeIds)
                .merchantGradeId(merchantGradeId).channelEmployeeUid(channelEmployeeUid).franchiseeId(franchiseeId).phone(phone).build();
        
        return R.ok(merchantService.countTotal(merchantPageRequest));
    }
    
    /**
     * @param
     * @description 商户列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,@RequestParam(value = "merchantGradeId", required = false) Long merchantGradeId,
            @RequestParam(value = "channelEmployeeUid", required = false) Long channelEmployeeUid, @RequestParam(value = "phone", required = false) String phone) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant page warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).size(size).offset(offset).tenantId(TenantContextHolder.getTenantId())
                .merchantGradeId(merchantGradeId).channelEmployeeUid(channelEmployeeUid).franchiseeId(franchiseeId).phone(phone).franchiseeIdList(franchiseeIds).build();
        
        return R.ok(merchantService.listByPage(merchantPageRequest));
    }

    /**
     * @param
     * @description 列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/overdueUser/pageCount")
    public R pageOverdueUserCount(@RequestParam(value = "merchantId", required = true) Long merchantId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        MerchantJoinUserQueryMode merchantJoinUserQueryMode = MerchantJoinUserQueryMode.builder().type(MerchantConstant.MERCHANT_OVERDUE_USER_QUERY_TYPE).merchantId(merchantId)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(merchantService.countOverdueUserTotal(merchantJoinUserQueryMode));
    }

    /**
     * @param
     * @description 列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/overdueUser/page")
    public R pageOverdueUser(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "merchantId", required = true) Long merchantId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        MerchantJoinUserQueryMode merchantJoinUserQueryMode = MerchantJoinUserQueryMode.builder().type(MerchantConstant.MERCHANT_OVERDUE_USER_QUERY_TYPE).merchantId(merchantId).offset(offset)
                .size(size).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(merchantService.listOverdueUserByPage(merchantJoinUserQueryMode));
    }
    
    /**
     * @param
     * @description 根据id查询商户信息
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/getById")
    public R getById(@RequestParam(value = "id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant get warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
    
        Triple<Boolean, String, Object> triple = merchantService.queryById(id, franchiseeIds);
        
        return returnTripleResult(triple);
    }
    
    
    @PostMapping("/admin/merchant/unbindOpenId")
    public R unbindOpenId(@RequestBody @Validated MerchantUnbindReq   params){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.info("merchant unbind open id warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            params.setBindFranchiseeIdList(franchiseeIds);
        }
        
        Pair<Boolean,Object> triple = merchantService.unbindOpenId(params);
        
        return returnPairResult(triple);
    }
    /**
     * @param
     * @description 商户下拉框查询
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/queryList")
    public R queryList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant query warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).size(size).offset(offset).franchiseeIdList(franchiseeIds).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(merchantService.queryList(merchantPageRequest));
    }
}
