package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.request.merchant.MerchantPlacePageRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceSaveRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/15 21:06
 * @desc 场地
 */
@Slf4j
@RestController
public class JsonMerchantPlaceController extends BaseController {
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 保存
     *
     * @param merchantPlaceSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/place/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
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
                log.warn("merchant place save warn! franchisee is empty");
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            merchantPlaceSaveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceService.save(merchantPlaceSaveRequest);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        MerchantPlace place = (MerchantPlace) r.getRight();
        
        return R.ok(place.getId());
    }
    
    /**
     * 修改场地
     *
     * @param merchantPlaceSaveRequest
     * @return
     */
    @PostMapping("/admin/merchant/place/update")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
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
                log.warn("merchant place update warn! franchisee is empty");
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            merchantPlaceSaveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceService.update(merchantPlaceSaveRequest);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        MerchantPlace merchantPlace = (MerchantPlace) r.getRight();
        merchantPlaceService.deleteCache(merchantPlace);
        
        return R.ok();
    }
    
    /**
     * 删除场地
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/admin/merchant/place/delete")
    @Log(title = "删除场地")
    public R delete(@RequestParam(value = "id", required = true) Long id) {
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
                log.warn("merchant place delete warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Triple<Boolean, String, Object> r = merchantPlaceService.remove(id, franchiseeIds);
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        // 删除缓存
        MerchantPlace merchantPlace = (MerchantPlace) r.getRight();
        merchantPlaceService.deleteCache(merchantPlace);
        
        return R.ok();
    }
    
    /**
     * @param
     * @description 场地列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "merchantAreaId", required = false) Long merchantAreaId,
            @RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "idList", required = false) List<Long> idList,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
                log.warn("merchant place page count warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        MerchantPlacePageRequest merchantPlacePageRequest = MerchantPlacePageRequest.builder().merchantId(merchantId).name(name).franchiseeId(franchiseeId).tenantId(tenantId).idList(idList)
                .merchantAreaId(merchantAreaId).franchiseeIdList(franchiseeIds).build();
        
        return R.ok(merchantPlaceService.countTotal(merchantPlacePageRequest));
    }
    
    /**
     * @param
     * @description 场地列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "merchantAreaId", required = false) Long merchantAreaId, @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "idList", required = false) List<Long> idList, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant place page warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        MerchantPlacePageRequest merchantPlacePageRequest = MerchantPlacePageRequest.builder().merchantId(merchantId).franchiseeId(franchiseeId).name(name)
                .franchiseeIdList(franchiseeIds).idList(idList).size(size).offset(offset)
                .tenantId(tenantId).merchantAreaId(merchantAreaId).name(name).build();
        
        return R.ok(merchantPlaceService.listByPage(merchantPlacePageRequest));
    }
    
    /**
     * @param
     * @description 获取柜机下拉框数据
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/getCabinetList")
    public R getCabinetList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "cabinetName", required = false) String cabinetName,
            @RequestParam(value = "placeId") Long placeId) {
        if (size < 0 || size > 50) {
            size = 50L;
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
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant place page warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantPlacePageRequest merchantPlacePageRequest = MerchantPlacePageRequest.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).placeId(placeId).bindFranchiseeIdList(franchiseeIds)
                .cabinetName(cabinetName).build();
        
        return returnTripleResult(merchantPlaceService.queryListCabinet(merchantPlacePageRequest));
    }
    
    /**
     * @param
     * @description 获取场地下拉框
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/queryPlaceList")
    public R queryPlaceList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "merchantId", required = false) Long merchantId) {
        if (size < 0 || size > 50) {
            size = 50L;
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
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant place query place list warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantPlacePageRequest merchantPlacePageRequest = MerchantPlacePageRequest.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).name(name).franchiseeId(franchiseeId).franchiseeIdList(franchiseeIds)
                .merchantId(merchantId).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantPlaceService.queryListPlace(merchantPlacePageRequest));
    }
    
    /**
     * @param
     * @description 根据id查询商户信息
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/getById")
    public R getById(@RequestParam(value = "id") Long id) {
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
                log.warn("merchant place get by id warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        Triple<Boolean, String, Object> triple = merchantPlaceService.queryById(id, franchiseeIds);
        
        return returnTripleResult(triple);
    }
    
}
