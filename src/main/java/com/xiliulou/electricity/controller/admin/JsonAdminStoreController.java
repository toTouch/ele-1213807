package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.StoreAccountQuery;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class JsonAdminStoreController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    StoreService storeService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    StoreAmountService storeAmountService;

    @Autowired
    StoreSplitAccountHistoryService storeSplitAccountHistoryService;
    @Autowired
    UserDataScopeService userDataScopeService;

    //新增门店
    @PostMapping(value = "/admin/store")
    public R save(@RequestBody @Validated(value = CreateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
        return storeService.save(storeAddAndUpdate);
    }

    //修改门店
    @PutMapping(value = "/admin/store")
    @Log(title = "修改门店")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
        return storeService.edit(storeAddAndUpdate);
    }

    //删除门店
    @DeleteMapping(value = "/admin/store/{id}")
    @Log(title = "删除门店")
    public R delete(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return storeService.delete(id);
    }

    /**
     * 根据角色获取租户下门店列表
     * @return
     */
    @GetMapping(value = "/admin/store/selectListQuery")
    public R selectList(){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        StoreQuery storeQuery=new StoreQuery();
        
        List<Long> franchiseeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds=userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> storeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds=userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        storeQuery.setTenantId(TenantContextHolder.getTenantId());
        storeQuery.setFranchiseeIds(franchiseeIds);
        storeQuery.setStoreIdList(storeIds);

        return returnTripleResult(storeService.selectListByQuery(storeQuery));
    }


    //列表查询
    @GetMapping(value = "/admin/store/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "payType", required = false) Integer payType,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> ids = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            ids = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(ids)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> franchiseeIds = null;
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)){
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        StoreQuery storeQuery = StoreQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .tenantId(TenantContextHolder.getTenantId())
                .payType(payType)
                .storeIdList(ids)
                .franchiseeIds(franchiseeIds)
                .franchiseeId(franchiseeId).build();

        return storeService.queryList(storeQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/store/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "address", required = false) String address,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                        @RequestParam(value = "payType", required = false) Integer payType,
                        @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> ids = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            ids = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(ids)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> franchiseeIds = null;
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)){
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        StoreQuery storeQuery = StoreQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .payType(payType)
                .tenantId(TenantContextHolder.getTenantId())
                .storeIdList(ids)
                .franchiseeIds(franchiseeIds)
                .franchiseeId(franchiseeId).build();

        return storeService.queryCount(storeQuery);
    }

    //加盟商列表查询
    @GetMapping(value = "/admin/store/listByFranchisee")
    public R listByFranchisee(@RequestParam("size") Long size,
                              @RequestParam("offset") Long offset,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "beginTime", required = false) Long beginTime,
                              @RequestParam(value = "endTime", required = false) Long endTime,
                              @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        StoreQuery storeQuery = StoreQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .storeIdList(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return storeService.queryList(storeQuery);
    }

    //加盟商列表查询
    @GetMapping(value = "/admin/store/queryCountByFranchisee")
    public R queryCountByFranchisee(@RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "address", required = false) String address,
                                    @RequestParam(value = "beginTime", required = false) Long beginTime,
                                    @RequestParam(value = "endTime", required = false) Long endTime,
                                    @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        StoreQuery storeQuery = StoreQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .storeIdList(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return storeService.queryCountByFranchisee(storeQuery);
    }

    //禁启用门店
    @PutMapping(value = "/admin/store/updateStatus")
    public R updateStatus(@RequestParam("id") Long id, @RequestParam("usableStatus") Integer usableStatus) {
        return storeService.updateStatus(id, usableStatus);
    }

    /**
     * 门店用户金额列表
     */
    @GetMapping("/admin/store/getAccountList")
    public R getAccountList(@RequestParam("size") Long size,
                            @RequestParam("offset") Long offset,
                            @RequestParam(value ="userName", required = false) String storeName,
                            @RequestParam(value = "storeId", required = false) Long storeId,
                            @RequestParam(value = "startTime", required = false) Long startTime,
                            @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 50L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIdList = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIdList)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        
            List<Store> storeList = storeService.selectByFranchiseeIds(franchiseeIds);
            if (CollectionUtils.isEmpty(storeList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
    
            storeIdList = storeList.stream().map(Store::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(storeIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        

        StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
                .offset(offset)
                .size(size)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .storeId(storeId)
                .storeName(storeName)
                .storeIdList(storeIdList).build();

        return storeAmountService.queryList(storeAccountQuery);
    }

    /**
     * 门店用户金额列表数量
     */
    @GetMapping("/admin/store/getAccountCount")
    public R getAccountCount(@RequestParam(value = "storeId", required = false) Long storeId,
                             @RequestParam(value ="userName", required = false) String storeName,
                             @RequestParam(value = "startTime", required = false) Long startTime,
                             @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> storeIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIdList = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIdList)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        
            List<Store> storeList = storeService.selectByFranchiseeIds(franchiseeIds);
            if (CollectionUtils.isEmpty(storeList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        
            storeIdList = storeList.stream().map(Store::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(storeIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .storeId(storeId)
                .storeName(storeName)
                .storeIdList(storeIdList).build();

        return storeAmountService.queryCount(storeAccountQuery);
    }

    @GetMapping("/admin/store/getAccountHistoryList")
    public R getAccountHistoryList(@RequestParam("size") Long size,
                                   @RequestParam("offset") Long offset,
                                   @RequestParam(value ="orderId", required = false) String orderId,
                                   @RequestParam(value = "storeId", required = false) Long storeId,
                                   @RequestParam(value = "startTime", required = false) Long startTime,
                                   @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 50L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .storeId(storeId).build();

        return storeSplitAccountHistoryService.queryList(storeAccountQuery);
    }

    @GetMapping("/admin/store/getAccountHistoryCount")
    public R getAccountHistoryCount(
            @RequestParam(value ="orderId", required = false) String orderId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "oid", required = false) Long oid) {

        StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
                .orderId(orderId)
                .startTime(startTime)
                .endTime(endTime)
                .storeId(storeId).build();

        return storeSplitAccountHistoryService.queryCount(storeAccountQuery);
    }

    //修改余额
    @PostMapping("/admin/store/modifyAccount")
    @Log(title = "修改门店余额")
    public R modifyShopAccountAmount(@RequestParam("storeId") Long storeId,
                                     @RequestParam("balance") BigDecimal modifyBalance) {
        if (storeId <= 0 || modifyBalance.compareTo(BigDecimal.valueOf(0.0)) >= 0) {
            return R.fail("LOCKER.10005", "不合法的参数");
        }

        return storeAmountService.modifyBalance(storeId, modifyBalance);
    }

}
