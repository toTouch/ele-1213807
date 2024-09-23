/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/9/10
 */

package com.xiliulou.electricity.controller.admin.base;

import com.google.common.base.Objects;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/10 10:19
 */
public abstract class AbstractFranchiseeDataPermissionController {
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    
    /**
     * 校验加盟商配置
     *
     * @author caobotao.cbt
     * @date 2024/9/10 10:23
     */
    protected List<Long> checkFranchiseeDataPermission() {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (!Objects.equal(User.DATA_TYPE_FRANCHISEE, userInfo.getDataType())) {
            return null;
        }
        
        List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            throw new BizException("ELECTRICITY.0038", "加盟商不存在");
        }
        
        return franchiseeIds;
    }
    
    /**
     * 校验加盟商配置
     *
     * @author caobotao.cbt
     * @date 2024/9/10 10:23
     */
    protected void checkFranchiseeDataPermissionByFranchiseeId(Long franchiseeId) {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (!Objects.equal(User.DATA_TYPE_FRANCHISEE, userInfo.getDataType())) {
            return;
        }
        
        List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(franchiseeIds) || !franchiseeIds.contains(franchiseeId)) {
            throw new BizException("ELECTRICITY.0038", "加盟商不存在");
        }
        
    }
    
    
}
