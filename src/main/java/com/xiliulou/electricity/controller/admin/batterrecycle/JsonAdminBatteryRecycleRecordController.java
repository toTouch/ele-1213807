package com.xiliulou.electricity.controller.admin.batterrecycle;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleCancelRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleSaveOrUpdateRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecyclePageRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 电池回收记录表(TBatteryRecycleRecord)表控制层
 *
 * @author maxiaodong
 * @since 2024-10-30 10:47:42
 */
@RestController
@Slf4j
public class JsonAdminBatteryRecycleRecordController extends BaseController {
    
    @Resource
    private BatteryRecycleRecordService batteryRecycleRecordService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 新增
     */
    @PostMapping("/admin/battery/recycle/save")
    public R save(@RequestBody @Validated(CreateGroup.class) BatteryRecycleSaveOrUpdateRequest saveRequest) {
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
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
            
            saveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        return returnTripleResult(batteryRecycleRecordService.save(saveRequest, user.getUid()));
    }

    /**
     * 取消
     */
    @PostMapping("/admin/battery/recycle/cancel")
    public R cancel(@RequestBody @Validated(CreateGroup.class) BatteryRecycleCancelRequest request) {
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
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }

            request.setFranchiseeIdList(franchiseeIds);
        }

        request.setTenantId(TenantContextHolder.getTenantId());

        return returnTripleResult(batteryRecycleRecordService.cancel(request, batteryRecycleRecordService.listBySnList(request)));
    }
    
    
    /**
     * 分页查询
     */
    @PostMapping("/admin/battery/recycle/page")
    public R page(@RequestBody @Validated(BatteryRecyclePageRequest.class) BatteryRecyclePageRequest request) {
        if (request.getSize() < 0 || request.getSize() > 50) {
            request.setSize(10L);
        }
        
        if (request.getOffset() < 0) {
            request.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Long> franchiseeIds = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        if (Objects.nonNull(request.getFranchiseeId())) {
            franchiseeIds.add(request.getFranchiseeId());
        }

        request.setFranchiseeIdList(franchiseeIds);
        request.setTenantId(TenantContextHolder.getTenantId());

        if (CollectionUtils.isNotEmpty(request.getSnList()) && request.getSnList().size() == 1) {
            request.setSn(request.getSnList().get(0));
            request.setSnList(Collections.EMPTY_LIST);
        }

        return R.ok(batteryRecycleRecordService.listByPage(request));
    }
    
    @PostMapping("/admin/battery/recycle/pageCount")
    public R pageCount(@RequestBody BatteryRecyclePageRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }

        if (Objects.nonNull(request.getFranchiseeId())) {
            franchiseeIds.add(request.getFranchiseeId());
        }

        request.setFranchiseeIdList(franchiseeIds);
        request.setTenantId(TenantContextHolder.getTenantId());

        if (CollectionUtils.isNotEmpty(request.getSnList()) && request.getSnList().size() == 1) {
            request.setSn(request.getSnList().get(0));
            request.setSnList(Collections.EMPTY_LIST);
        }
        
        return R.ok(batteryRecycleRecordService.countTotal(request));
    }
}

