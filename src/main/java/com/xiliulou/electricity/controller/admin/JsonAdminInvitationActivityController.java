package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-01-15:57
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityController extends BaseController {
    
    @Autowired
    private InvitationActivityService invitationActivityService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/invitationActivity/search")
    public R search(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).name(name).build();
        
        return R.ok(invitationActivityService.selectBySearch(query));
    }
    
    /**
     * @param uid          邀请人uid
     * @param activityName 活动名称
     * @description 根据邀请人uid获取可参加的活动列表
     * @date 2023/11/13 15:43:36
     * @author HeYafeng
     */
    @GetMapping("/admin/invitationActivity/searchByUser")
    public R searchByUser(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid") Long uid,
            @RequestParam(value = "activityName", required = false) String activityName) {
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
        
        UserInfo invitationUser = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(invitationUser)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).status(NumberConstant.ONE)
                .name(activityName).build();
        
        return returnTripleResult(invitationActivityService.selectActivityByUser(query, uid));
    }
    
    /**
     * 新增
     */
    @PostMapping("/admin/invitation/activity/save")
    public R save(@RequestBody @Validated(CreateGroup.class) InvitationActivityQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
            
            Long franchiseeId = query.getFranchiseeId();
            if (Objects.nonNull(franchiseeId) && !Objects.equals(franchiseeIds.get(0), franchiseeId)) {
                log.warn("InvitationActivity WARN! Franchisees are inconsistent, franchiseeId={}", franchiseeId);
                return R.fail("120128", "所属加盟商不一致");
            }
        }
        
        return returnTripleResult(invitationActivityService.save(query));
    }
    
    @GetMapping(value = "/admin/invitation/update/{id}")
    public R update(@PathVariable("id") Long id) {
        return returnTripleResult(invitationActivityService.findActivityById(id));
    }
    
    /**
     * 修改
     */
    @PutMapping("/admin/invitation/activity/update")
    public R update(@RequestBody @Validated(UpdateGroup.class) InvitationActivityQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }
        
        return returnTripleResult(invitationActivityService.modify(query));
    }
    
    /**
     * 上架/下架
     */
    @PutMapping("/admin/invitation/activity/shelf")
    public R updateStatus(@RequestBody @Validated InvitationActivityStatusQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(invitationActivityService.updateStatus(query));
    }
    
    @GetMapping("/admin/invitationActivity/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "name", required = false) String name) {
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
        
        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).name(name).tenantId(TenantContextHolder.getTenantId()).status(status).build();
        
        return R.ok(invitationActivityService.selectByPage(query));
    }
    
    @GetMapping("/admin/invitationActivity/queryCount")
    public R count(@RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "name", required = false) String name) {
        
        InvitationActivityQuery query = InvitationActivityQuery.builder().tenantId(TenantContextHolder.getTenantId()).name(name).status(status).build();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }
        
        return R.ok(invitationActivityService.selectByPageCount(query));
    }
    
    @GetMapping(value = "/admin/invitationActivity/queryPackages")
    public R queryPackagesByFranchisee(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "type", required = true) Integer type, @RequestParam(value = "franchiseeId", required = true) Long franchiseeId) {
        
        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if (!packageTypes.contains(type)) {
            return R.fail("000200", "业务类型参数不合法");
        }
        
        //需要获取租金不可退的套餐
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().offset(offset).size(size).delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP)
                    .isRefund(BatteryMemberCard.NO).tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByQuery(query));
        } else {
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setOffset(offset.intValue());
            qryModel.setSize(size.intValue());
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());
            qryModel.setFranchiseeId(Objects.isNull(franchiseeId) ? null : franchiseeId.intValue());
            
            if (PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            } else if (PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            
            return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
        }
        
    }
    
    @GetMapping(value = "/admin/invitationActivity/queryPackagesCount")
    public R queryPackagesCount(@RequestParam(value = "type", required = true) Integer type, @RequestParam(value = "franchiseeId", required = true) Long franchiseeId) {
        
        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if (!packageTypes.contains(type)) {
            return R.fail("000200", "业务类型参数不合法");
        }
        
        //需要获取租金不可退的套餐
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)) {
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP).isRefund(BatteryMemberCard.NO)
                    .tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByPageCount(query));
        } else {
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());
            qryModel.setFranchiseeId(Objects.isNull(franchiseeId) ? null : franchiseeId.intValue());
            
            if (PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            } else if (PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)) {
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            return R.ok(carRentalPackageService.count(qryModel));
        }
    }
    
    
    /**
     * <p>
     * Description: delete 9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     *
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @GetMapping("/admin/invitationActivity/delete")
    public R<?> removeById(@RequestParam("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("未找到用户!");
        }
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return invitationActivityService.removeById(id);
    }
    
}
