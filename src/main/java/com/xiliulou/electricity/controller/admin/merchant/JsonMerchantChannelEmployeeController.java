package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:26
 */

@Slf4j
@RestController
public class JsonMerchantChannelEmployeeController extends BaseController {
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/merchant/channelEmployeeList")
    public R channelEmployeeList(@RequestParam("size") Integer size,
                                 @RequestParam("offset") Integer offset,
                                 @RequestParam(value = "uid", required = false) Long uid,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                 @RequestParam(value = "areaId", required = false) Long areaId) {
        if (size < 0 || size > 50) {
            size = 10;
        }
    
        if (offset < 0) {
            offset = 0;
        }
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("channel employee List error! not find user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee List error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ChannelEmployeeRequest channelEmployeeRequest = ChannelEmployeeRequest.builder()
                .size(size)
                .offset(offset)
                .uid(uid)
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIdList(franchiseeIds)
                .areaId(areaId)
                .tenantId(tenantId)
                .build();
        
        return R.ok(channelEmployeeService.listChannelEmployee(channelEmployeeRequest));
        
    }
    
    
    @GetMapping("/admin/merchant/channelEmployeeCount")
    public R channelEmployeeCount(
                                @RequestParam(value = "uid", required = false) Long uid,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                @RequestParam(value = "areaId", required = false) Long areaId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("channel employee count error! not find user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee count error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ChannelEmployeeRequest channelEmployeeRequest = ChannelEmployeeRequest.builder()
                .uid(uid)
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIdList(franchiseeIds)
                .areaId(areaId)
                .tenantId(tenantId)
                .build();
        
        return R.ok(channelEmployeeService.countChannelEmployee(channelEmployeeRequest));
        
    }
    
    @PostMapping("/admin/merchant/addChannelEmployee")
    public R addChannelEmployee(@RequestBody @Validated(value = CreateGroup.class) ChannelEmployeeRequest channelEmployeeRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee add error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            channelEmployeeRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        return returnTripleResult(channelEmployeeService.saveChannelEmployee(channelEmployeeRequest));
    }
    
    
    @PostMapping("/admin/merchant/updateChannelEmployee")
    public R updateChannelEmployee(@RequestBody @Validated(value = UpdateGroup.class) ChannelEmployeeRequest channelEmployeeRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee update error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            channelEmployeeRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        return returnTripleResult(channelEmployeeService.updateChannelEmployee(channelEmployeeRequest));
    }
    
    @GetMapping("/admin/merchant/queryChannelEmployeeById")
    public R queryChannelEmployeeById(@RequestParam("id") Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee query by id error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        return R.ok(channelEmployeeService.queryById(id, franchiseeIds));
    }
    
    @DeleteMapping("/admin/merchant/removeChannelEmployee")
    public R removeChannelEmployee(@RequestParam("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee query by id error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        return R.ok(channelEmployeeService.removeById(id, franchiseeIds));
    }
    
    /**
     * 渠道员下拉框选择
     * @param size
     * @param offset
     * @param franchiseeId
     * @param name
     * @return
     */
    @GetMapping("/admin/merchant/queryChannelEmployees")
    public R queryChannelEmployees(@RequestParam("size") Integer size,
            @RequestParam("offset") Integer offset,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "name", required = false) String name) {
        
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
        }
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee query employees error! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ChannelEmployeeRequest channelEmployeeRequest = ChannelEmployeeRequest.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIdList(franchiseeIds)
                .tenantId(tenantId)
                .build();
        
        return R.ok(channelEmployeeService.queryChannelEmployees(channelEmployeeRequest));
        
    }


}
