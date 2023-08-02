package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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

    @GetMapping("/admin/invitationActivity/search")
    public R search(@RequestParam("size") long size, @RequestParam("offset") long offset,
                    @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset)
                .tenantId(TenantContextHolder.getTenantId()).name(name).build();

        return R.ok(invitationActivityService.selectBySearch(query));
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

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(invitationActivityService.save(query));
    }

    @GetMapping(value = "/admin/invitation/update/{id}")
    public R update(@PathVariable("id") Long id){
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
            return R.fail("ELECTRICITY.0066", "用户权限不足");
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
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).name(name)
                .tenantId(TenantContextHolder.getTenantId()).status(status).build();

        return R.ok(invitationActivityService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivity/queryCount")
    public R count(@RequestParam(value = "status", required = false) Integer status,
                   @RequestParam(value = "name", required = false) String name) {

        InvitationActivityQuery query = InvitationActivityQuery.builder()
                .tenantId(TenantContextHolder.getTenantId()).name(name).status(status).build();

        return R.ok(invitationActivityService.selectByPageCount(query));
    }

    @GetMapping(value = "/admin/invitationActivity/queryPackagesByFranchisee")
    public R queryPackagesByFranchisee(@RequestParam(value = "offset") Long offset,
                                       @RequestParam(value = "size") Long size,
                                       @RequestParam(value = "type",  required = true) Integer type) {

        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if(!packageTypes.contains(type)){
            return R.fail("000200", "业务类型参数不合法");
        }

        //需要获取租金不可退的套餐
        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.equals(type)){
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                    .offset(offset)
                    .size(size)
                    .delFlag(BatteryMemberCard.DEL_NORMAL)
                    .status(BatteryMemberCard.STATUS_UP)
                    .isRefund(BatteryMemberCard.NO)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            return R.ok(batteryMemberCardService.selectByQuery(query));
        }else{
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setOffset(offset.intValue());
            qryModel.setSize(size.intValue());
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());

            if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.equals(type)){
                qryModel.setType(CarRentalPackageTypeEnum.CAR_BATTERY.getCode());
            }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.equals(type)){
                qryModel.setType(CarRentalPackageTypeEnum.CAR.getCode());
            }

            return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
        }

    }

    @GetMapping(value = "/admin/invitationActivity/queryPackagesCount")
    public R queryPackagesCount(@RequestParam(value = "type",  required = true) Integer type) {

        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if(!packageTypes.contains(type)){
            return R.fail("000200", "业务类型参数不合法");
        }

        //需要获取租金不可退的套餐
        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.equals(type)){
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                    .delFlag(BatteryMemberCard.DEL_NORMAL)
                    .status(BatteryMemberCard.STATUS_UP)
                    .isRefund(BatteryMemberCard.NO)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            return R.ok(batteryMemberCardService.selectByPageCount(query));
        }else{
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());

            if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.equals(type)){
                qryModel.setType(CarRentalPackageTypeEnum.CAR_BATTERY.getCode());
            }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.equals(type)){
                qryModel.setType(CarRentalPackageTypeEnum.CAR.getCode());
            }
            return R.ok(carRentalPackageService.count(qryModel));
        }
    }


}
