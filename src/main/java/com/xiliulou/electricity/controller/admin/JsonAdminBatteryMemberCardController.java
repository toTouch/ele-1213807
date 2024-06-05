package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ValidList;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-07-15:18
 */
@Slf4j
@RestController
public class JsonAdminBatteryMemberCardController extends BaseController {
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    /**
     * 搜索
     */
    @GetMapping("/admin/battery/memberCard/search")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rentType", required = false) Integer rentType, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId)
                //.franchiseeIds(franchiseeIds)
                .delFlag(BatteryMemberCard.DEL_NORMAL).status(status).rentType(rentType).name(name).build();
        
        return R.ok(batteryMemberCardService.search(query));
    }
    
    /**
     * 分页列表
     */
    @GetMapping("/admin/battery/memberCard/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                  @RequestParam(value = "mid", required = false) Long mid,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "rentType", required = false) Integer rentType,
                  @RequestParam(value = "rentUnit", required = false) Integer rentUnit,
                  @RequestParam(value = "businessType", required = false) Integer businessType,
                  @RequestParam(value = "name", required = false) String name, @RequestParam(value = "batteryModel", required = false) String batteryModel,
            @RequestParam(value = "userGroupId", required = false) Long userGroupId) {
        
        if (Objects.nonNull(rentType) && Objects.nonNull(userGroupId)) {
            return R.ok(Collections.emptyList());
        }
        
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.emptyList());
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .id(mid)
                .franchiseeId(franchiseeId)
                .status(status)
                .businessType(businessType == null ?  0 : businessType)
                .rentType(rentType)
                .rentUnit(rentUnit)
                .name(name)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .franchiseeIds(franchiseeIds).batteryModel(batteryModel)
                .userInfoGroupId(Objects.nonNull(userGroupId) ? userGroupId.toString() : null)
                .build();

        return R.ok(batteryMemberCardService.selectByPage(query));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/battery/memberCard/queryCount")
    public R pageCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "rentType", required = false) Integer rentType, @RequestParam(value = "rentUnit", required = false) Integer rentUnit,
            @RequestParam(value = "businessType", required = false) Integer businessType, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "batteryModel", required = false) String batteryModel, @RequestParam(value = "userGroupId", required = false) Long userGroupId) {
        
        if (Objects.nonNull(rentType) && Objects.nonNull(userGroupId)) {
            return R.ok(Collections.emptyList());
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(NumberConstant.ZERO);
            }
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().franchiseeId(franchiseeId).status(status).businessType(businessType == null ? 0 : businessType)
                .rentType(rentType).rentUnit(rentUnit).name(name).tenantId(TenantContextHolder.getTenantId()).delFlag(BatteryMemberCard.DEL_NORMAL).franchiseeIds(franchiseeIds)
                .batteryModel(batteryModel).userInfoGroupId(Objects.nonNull(userGroupId) ? userGroupId.toString() : null).build();
        
        return R.ok(batteryMemberCardService.selectByPageCount(query));
    }
    
    /**
     * 新增
     */
    @PostMapping("/admin/battery/memberCard")
    public R save(@RequestBody @Validated(CreateGroup.class) BatteryMemberCardQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        if (CollectionUtils.isNotEmpty(query.getCouponIdsTransfer()) && query.getCouponIdsTransfer().size() > BatteryMemberCardQuery.MAX_COUPON_NO) {
            return R.ok();
        }

        if (CollectionUtils.isNotEmpty(query.getUserInfoGroupIdsTransfer()) && query.getUserInfoGroupIdsTransfer().size() > BatteryMemberCardQuery.MAX_USER_INFO_GROUP_NO) {
            return R.ok();
        }
        
        return returnTripleResult(batteryMemberCardService.save(query));
    }
    
    /**
     * 修改
     */
    @PutMapping("/admin/battery/memberCard")
    @Log(title = "修改电池套餐")
    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryMemberCardQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        if (CollectionUtils.isNotEmpty(query.getCouponIdsTransfer()) && query.getCouponIdsTransfer().size() > BatteryMemberCardQuery.MAX_COUPON_NO) {
            return R.ok();
        }

        if (CollectionUtils.isNotEmpty(query.getUserInfoGroupIdsTransfer()) && query.getUserInfoGroupIdsTransfer().size() > BatteryMemberCardQuery.MAX_USER_INFO_GROUP_NO) {
            return R.ok();
        }
        
        return returnTripleResult(batteryMemberCardService.modify(query));
    }
    
    /**
     * 上下架
     */
    @PutMapping("/admin/battery/memberCard/shelf")
    @Log(title = "上/下架电池套餐")
    public R update(@RequestBody @Validated BatteryMemberCardStatusQuery batteryModelQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        return returnTripleResult(batteryMemberCardService.updateStatus(batteryModelQuery));
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/admin/battery/memberCard/{id}")
    @Log(title = "删除电池套餐")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        return returnTripleResult(batteryMemberCardService.delete(id));
    }
    
    /**
     * 后台绑定套餐 下拉列表
     *
     * @return
     */
    @GetMapping("/admin/battery/memberCard/selectListByQuery")
    @Deprecated
    public R selectListByQuery(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds)
                .delFlag(BatteryMemberCard.DEL_NORMAL).status(status).name(name).size(size).offset(offset).build();
        
        return R.ok(batteryMemberCardService.selectListByQuery(query));
    }
    
    /**
     * 获取可续费套餐列表 （押金、电池型号相同）
     */
    @GetMapping("/admin/battery/memberCardByUid")
    public R userBatteryMembercardList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("uid") long uid,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "name", required = false) String name) {
        
        if (offset != 0) {
            return R.ok(Collections.emptyList());
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().name(name).uid(uid).franchiseeId(franchiseeId).status(BatteryMemberCard.STATUS_UP)
                .delFlag(BatteryMemberCard.DEL_NORMAL).size(100L).offset(0L).tenantId(TenantContextHolder.getTenantId()).build();
        return R.ok(batteryMemberCardService.selectUserBatteryMembercardList(query));
    }
    
    /**
     * 返利配置-换电套餐下拉列表
     */
    @GetMapping("/admin/battery/memberCard/pageForMerchant")
    public R pageForMerchant(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "mid", required = false) Long mid,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "rentType", required = false) Integer rentType,
            @RequestParam(value = "rentUnit", required = false) Integer rentUnit, @RequestParam(value = "businessType", required = false) Integer businessType,
            @RequestParam(value = "name", required = false) String name) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.emptyList());
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .id(mid)
                .franchiseeId(franchiseeId)
                .status(status)
                .businessType(businessType == null ?  BatteryMemberCard.BUSINESS_TYPE_BATTERY : businessType)
                .rentType(rentType)
                .rentUnit(rentUnit)
                .name(name)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .build();
        
        return R.ok(batteryMemberCardService.selectByPageForMerchant(query));
    }
    
    
    /**
     * 批量修改套餐排序参数
     *
     * @param sortParamQueries
     * @return
     */
    @PutMapping("/admin/battery/memberCard/batchUpdateSortParam")
    public R batchUpdateSortParam(@RequestBody @Validated ValidList<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 仅超级管理员和运营商可修改排序参数
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(batteryMemberCardService.batchUpdateSortParam(sortParamQueries));
    }
    
    /**
     * 查询id、name、sortParam供排序使用
     *
     * @return
     */
    @GetMapping("/admin/battery/memberCard/listMemberCardForSort")
    public R listMemberCardForSort() {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 查询数据较多，限制仅超级管理员和运营商可使用
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(batteryMemberCardService.listMemberCardForSort());
    }
    
}
