package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ChannelActivityHistoryQuery;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (ChannelActivityHistory)表控制层
 *
 * @author Hardy
 * @since 2023-03-23 09:24:25
 */
@RestController
public class JsonAdminChannelActivityHistoryController extends BaseController {
    
    /**
     * 服务对象
     */
    @Resource
    private ChannelActivityHistoryService channelActivityHistoryService;

    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/channelActivityHistory/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            offset = 50L;
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

        ChannelActivityHistoryQuery channelActivityHistoryQuery = ChannelActivityHistoryQuery.builder()
                .offset(offset)
                .size(size)
                .uid(uid)
                .phone(phone)
                .tenantId(TenantContextHolder.getTenantId())
                .beginTime(beginTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return this
                .returnTripleResult(channelActivityHistoryService.queryActivityHistoryList(channelActivityHistoryQuery));
    }
    
    @GetMapping("/admin/channelActivityHistory/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

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

        ChannelActivityHistoryQuery channelActivityHistoryQuery = ChannelActivityHistoryQuery.builder()
                .uid(uid)
                .phone(phone)
                .tenantId(TenantContextHolder.getTenantId())
                .beginTime(beginTime)
                .endTime(endTime)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .build();

        return this.returnTripleResult(channelActivityHistoryService.queryActivityHistoryCount(channelActivityHistoryQuery));
    }
    
    @GetMapping("/admin/channelActivityHistory/exportExcel")
    public void queryExportExcel(@RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam(value = "uid", required = false) Long uid,
                                 @RequestParam(value = "beginTime", required = false) Long beginTime,
                                 @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {
        channelActivityHistoryService.queryExportExcel(phone, uid, beginTime, endTime, response);
    }
}
