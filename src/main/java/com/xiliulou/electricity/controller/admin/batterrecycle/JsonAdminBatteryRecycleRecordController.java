package com.xiliulou.electricity.controller.admin.batterrecycle;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleSaveOrUpdateRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecyclePageRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
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
     * 分页查询
     */
    @GetMapping("/admin/battery/recycle/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "batchNo", required = false) String batchNo, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
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
        
        List<Long> franchiseeIds = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIds.add(franchiseeId);
        }
        BatteryRecyclePageRequest request = BatteryRecyclePageRequest.builder().size(size).offset(offset).batchNo(batchNo).status(status).electricityCabinetId(electricityCabinetId)
                .franchiseeIdList(franchiseeIds).startTime(startTime).endTime(endTime).sn(sn).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(batteryRecycleRecordService.listByPage(request));
    }
    
    @GetMapping("/admin/battery/recycle/pageCount")
    public R pageCount( @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "batchNo", required = false) String batchNo, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
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
    
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIds.add(franchiseeId);
        }
        BatteryRecyclePageRequest request = BatteryRecyclePageRequest.builder().batchNo(batchNo).status(status).electricityCabinetId(electricityCabinetId)
                .franchiseeIdList(franchiseeIds).startTime(startTime).endTime(endTime).sn(sn).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(batteryRecycleRecordService.countTotal(request));
    }
}

