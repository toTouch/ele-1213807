package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.TurnoverStatisticQueryModel;
import com.xiliulou.electricity.service.TurnoverStatisticService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName : JsonAdminTurnoverStatisticController
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-01-23
 */

@RestController
@Slf4j
public class JsonAdminTurnoverStatisticController {
    @Autowired
    private TurnoverStatisticService turnoverStatisticService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @GetMapping(value = "/admin/earning/turnover/list")
    public R queryTurnoverStatisticList(@RequestParam("size") Long size,
            @RequestParam("offset") Long offset,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
    
        if (size < 0L || size > 50L) {
            size = 10L;
        }
    
        if (offset < 0L) {
            offset = 0L;
        }
    
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        TurnoverStatisticQueryModel queryModel = new TurnoverStatisticQueryModel();
    
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }else{
                queryModel.setStoreIds(storeIds);
            }
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }else{
                queryModel.setFranchiseeIds(franchiseeIds);
            }
        }
        queryModel.setSize(size);
        queryModel.setOffset(offset);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        queryModel.setBeginTime(beginTime);
        queryModel.setEndTime(endTime);
        return R.ok(turnoverStatisticService.listTurnoverStatistic(queryModel));
    }
    
    @GetMapping(value = "/admin/earning/turnover/count")
    public R queryTurnoverStatisticListCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        TurnoverStatisticQueryModel queryModel = new TurnoverStatisticQueryModel();
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }else{
                queryModel.setStoreIds(storeIds);
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }else{
                queryModel.setFranchiseeIds(franchiseeIds);
            }
        }

        queryModel.setTenantId(TenantContextHolder.getTenantId());
        queryModel.setBeginTime(beginTime);
        queryModel.setEndTime(endTime);
        return R.ok(turnoverStatisticService.countTurnoverStatistic(queryModel));
    }
}
