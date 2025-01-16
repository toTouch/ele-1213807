package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠券规则表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@RestController
@Slf4j
public class JsonAdminCouponController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    private CouponService couponService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    /**
     * 搜索
     */
    @GetMapping("/admin/coupon/search")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,@RequestParam(value = "discountType",required = false) Integer discountType) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        CouponQuery query = CouponQuery.builder().size(size).discountType(discountType).offset(offset).tenantId(TenantContextHolder.getTenantId()).name(name).franchiseeId(franchiseeId).build();
        
        return R.ok(couponService.search(query));
    }
    
    //新增
    @PostMapping(value = "/admin/coupon")
    public R save(@RequestBody @Validated(value = CreateGroup.class) CouponQuery couponQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        couponQuery.setUid(user.getUid());
        couponQuery.setUserName(user.getUsername());
        couponQuery.setFranchiseeIds(franchiseeIds);
        
        return couponService.insert(couponQuery);
    }
    
    //修改--暂时无此功能
    @PutMapping(value = "/admin/coupon")
    @Log(title = "修改优惠券")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) CouponQuery couponQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        couponQuery.setFranchiseeIds(franchiseeIds);
        
        return couponService.update(couponQuery);
    }
    
    /**
     * 根据优惠券ID查询优惠券信息
     *
     * @param id
     * @return
     */
    @GetMapping("/admin/coupon/update/{id}")
    public R edit(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        return returnTripleResult(couponService.findCouponById(id));
    }
    
    @DeleteMapping("/admin/coupon/delete/{id}")
    @Log(title = "删除优惠券")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        return returnTripleResult(couponService.deleteById(id, franchiseeIds));
    }

    /**
     * 列表查询
     */
    @GetMapping(value = "/admin/coupon/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "discountType", required = false) Integer discountType,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "applyType", required = false) Integer applyType, @RequestParam(value = "superposition", required = false) Integer superposition,
            @RequestParam(value = "useScope",required = false) Integer useScope) {
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
            return R.ok(Collections.emptyList());
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        CouponQuery couponQuery = CouponQuery.builder().offset(offset).size(size).name(name).discountType(discountType).franchiseeId(franchiseeId).applyType(applyType)
                .superposition(superposition).useScope(useScope).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).build();
        return couponService.queryCouponList(couponQuery);
    }
    
    //列表查询
    @GetMapping(value = "/admin/coupon/count")
    public R queryCount(@RequestParam(value = "discountType", required = false) Integer discountType, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "applyType", required = false) Integer applyType,
            @RequestParam(value = "superposition", required = false) Integer superposition,@RequestParam(value = "useScope",required = false) Integer useScope) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok(NumberConstant.ZERO);
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(NumberConstant.ZERO);
            }
        }
        
        CouponQuery couponQuery = CouponQuery.builder().name(name).discountType(discountType).franchiseeId(franchiseeId).applyType(applyType).superposition(superposition)
                .tenantId(tenantId).useScope(useScope).franchiseeIds(franchiseeIds).build();
        return couponService.queryCount(couponQuery);
    }
    
    @GetMapping(value = "/admin/coupon/queryPackages")
    public R queryPackagesByFranchisee(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "type", required = true) Integer type, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        
        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if (!packageTypes.contains(type)) {
            return R.fail("000200", "业务类型参数不合法");
        }
        
        offset = (Objects.isNull(offset) || offset < 0L) ? 0L : offset;
        size = (Objects.isNull(size) || size > 500L) ? 500L : size;
        
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().offset(offset).size(size).delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP)
                    .tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByQuery(query));
        } else {
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setOffset(offset.intValue());
            qryModel.setSize(size.intValue());
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setFranchiseeId(Objects.isNull(franchiseeId) ? null : franchiseeId.intValue());
            
            if (PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            } else if (PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            
            return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
        }
        
    }
    
    @GetMapping(value = "/admin/coupon/queryPackagesCount")
    public R getElectricityUsablePackageCount(@RequestParam(value = "type", required = true) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        
        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if (!packageTypes.contains(type)) {
            return R.fail("000200", "业务类型参数不合法");
        }
        
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP)
                    .tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByPageCount(query));
        } else {
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setFranchiseeId(franchiseeId.intValue());
            
            if (PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            } else if (PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            return R.ok(carRentalPackageService.count(qryModel));
        }
    }


    /**
     * 编辑优惠券禁用状态
     *
     * @param id    id
     * @param state state
     * @return R
     */
    @GetMapping("/admin/coupon/update/state")
    public R editEnablesState(@RequestParam("id") Long id, @RequestParam("state") Integer state) {
        return couponService.editEnablesState(id, state);
    }
}
