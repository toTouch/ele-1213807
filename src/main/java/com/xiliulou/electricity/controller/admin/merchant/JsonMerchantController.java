package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
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
    
    /**
     * 查询商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeConditionInfo")
    public R upgradeConditionInfo() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
    
        return R.ok(merchantAttrService.queryUpgradeCondition(TenantContextHolder.getTenantId()));
    }
    
    /**
     * 修改商户升级条件
     */
    @PutMapping("/admin/merchantAttr/upgradeCondition")
    @Log(title = "修改商户升级条件")
    public R updateUpgradeCondition(@RequestParam("condition") Integer condition) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return returnTripleResult(merchantAttrService.updateUpgradeCondition(TenantContextHolder.getTenantId(), condition));
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return returnTripleResult(merchantAttrService.updateInvitationCondition(request));
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
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
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
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = merchantService.remove(id);
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
     * @description 商户列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId") Long franchiseeId,@RequestParam(value = "merchantGradeId") Long merchantGradeId,
            @RequestParam(value = "channelEmployeeUid", required = false) Long channelEmployeeUid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).tenantId(TenantContextHolder.getTenantId())
                .merchantGradeId(merchantGradeId).channelEmployeeUid(channelEmployeeUid).franchiseeId(franchiseeId).build();
        
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
            @RequestParam(value = "channelEmployeeUid", required = false) Long channelEmployeeUid) {
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
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).size(size).offset(offset).tenantId(TenantContextHolder.getTenantId())
                .merchantGradeId(merchantGradeId).channelEmployeeUid(channelEmployeeUid).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantService.listByPage(merchantPageRequest));
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Triple<Boolean, String, Object> triple = merchantService.queryById(id);
        
        return returnTripleResult(triple);
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantPageRequest merchantPageRequest = MerchantPageRequest.builder().name(name).size(size).offset(offset).build();
        
        return R.ok(merchantService.queryList(merchantPageRequest));
    }
}
