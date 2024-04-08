package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordPageRequest;
import com.xiliulou.electricity.request.user.UserGroupSaveRequest;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 19:39:33
 */
@Slf4j
@RestController
public class JsonAdminUserInfoGroupController extends BasicController {
    
    @Resource
    private UserInfoGroupService userInfoGroupService;
    
    /**
     * @description 新增用户分组
     * @date 2024/4/8 19:43:44
     * @author HeYafeng
     */
    @PostMapping("/admin/userInfo/userInfoGroup/add")
    public R save(@RequestBody @Validated(value = CreateGroup.class) UserGroupSaveRequest userGroupSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupService.save(userGroupSaveRequest, user.getUid());
    }
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/userInfo/userInfoGroup/page")
    public R pageCount(@RequestParam(value = "orderNo", required = false) String orderNo, @RequestParam(value = "sourceFranchiseeId", required = false) Long sourceFranchiseeId,
            @RequestParam(value = "targetFranchiseeId", required = false) Long targetFranchiseeId, @RequestParam(value = "type", required = false) Integer type) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //    return R.fail("ELECTRICITY.0066", "用户权限不足");
        //}
        
        AssetAllocateRecordPageRequest allocateRecordPageRequest = AssetAllocateRecordPageRequest.builder().orderNo(orderNo).type(type).sourceFranchiseeId(sourceFranchiseeId)
                .targetFranchiseeId(targetFranchiseeId).build();
        return R.ok(assetAllocateRecordService.countTotal(allocateRecordPageRequest));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/userInfo/userInfoGroup/pageCount")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "sourceFranchiseeId", required = false) Long sourceFranchiseeId,
            @RequestParam(value = "targetFranchiseeId", required = false) Long targetFranchiseeId, @RequestParam(value = "type", required = false) Integer type) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //    return R.fail("ELECTRICITY.0066", "用户权限不足");
        //}
        
        AssetAllocateRecordPageRequest allocateRecordPageRequest = AssetAllocateRecordPageRequest.builder().orderNo(orderNo).type(type).sourceFranchiseeId(sourceFranchiseeId)
                .targetFranchiseeId(targetFranchiseeId).size(size).offset(offset).build();
        return R.ok(assetAllocateRecordService.listByPage(allocateRecordPageRequest));
    }
}
