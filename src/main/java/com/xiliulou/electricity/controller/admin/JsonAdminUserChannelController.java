package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserChannelQuery;
import com.xiliulou.electricity.service.UserChannelService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (UserChannel)表控制层
 *
 * @author Hardy
 * @since 2023-03-22 15:34:58
 */
@RestController
public class JsonAdminUserChannelController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserChannelService userChannelService;

    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("admin/userChannel/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "phone", required = false) String phone) {

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

        UserChannelQuery userChannelQuery = UserChannelQuery.builder()
                .offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .uid(uid)
                .name(name)
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return this.returnTripleResult(userChannelService.queryUserChannelActivityList(userChannelQuery));
    }
    
    @GetMapping("admin/userChannel/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "phone", required = false) String phone) {

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

        UserChannelQuery userChannelQuery = UserChannelQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .uid(uid)
                .name(name)
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return this.returnTripleResult(userChannelService.queryUserChannelActivityCount(userChannelQuery));
    }
    
    @PostMapping("admin/userChannel/save")
    public R saveOne(@RequestParam("uid") Long uid) {
        return this.returnTripleResult(userChannelService.saveOne(uid));
    }
}
