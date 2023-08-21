package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserActiveInfoQuery;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (UserActiveInfo)表控制层
 *
 * @author Hardy
 * @since 2023-03-01 10:15:12
 */
@RestController
public class JsonAdminUserActiveInfoController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserActiveInfoService userActiveInfoService;

    @Autowired
    UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/userActiveInfo/list")
    public R queryList(@RequestParam(value = "userName", required = false) String userName,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "day", required = false) Integer day,
                       @RequestParam(value = "batterySn", required = false) String batterySn,
                       @RequestParam(value = "payCount", required = false) Integer payCount,
                       @RequestParam("offset") Long offset,
                       @RequestParam("size") Long size) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 50L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        UserActiveInfoQuery query = UserActiveInfoQuery.builder().userName(userName).phone(phone).uid(uid).day(day)
                .batterySn(batterySn).payCount(payCount).offset(offset).size(size).storeIds(storeIds).franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return userActiveInfoService.queryList(query);
    }

    @GetMapping("/admin/userActiveInfo/queryCount")
    public R queryCount(@RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "day", required = false) Integer day,
                        @RequestParam(value = "batterySn", required = false) String batterySn,
                        @RequestParam(value = "payCount", required = false) Integer payCount) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        UserActiveInfoQuery query = UserActiveInfoQuery.builder().userName(userName).phone(phone).uid(uid).day(day).storeIds(storeIds).franchiseeIds(franchiseeIds)
                .batterySn(batterySn).payCount(payCount).tenantId(TenantContextHolder.getTenantId()).build();
        return userActiveInfoService.queryCount(query);
    }
}
