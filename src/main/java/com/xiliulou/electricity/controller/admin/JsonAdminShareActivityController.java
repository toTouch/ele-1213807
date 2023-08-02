package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.UserDataScopeService;
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
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonAdminShareActivityController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    private ShareActivityService shareActivityService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserDataScopeService userDataScopeService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    //新增
    @PostMapping(value = "/admin/shareActivity")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        return shareActivityService.insert(shareActivityAddAndUpdateQuery);
    }

    @GetMapping(value = "/admin/shareActivity/detail/{id}")
    public R detail(@PathVariable("id") Integer id){
        return returnTripleResult(shareActivityService.shareActivityDetail(id));
    }

    @PutMapping(value = "/admin/shareActivity/update")
    public R updateActivity(@RequestBody @Validated(value = UpdateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        return returnTripleResult(shareActivityService.updateShareActivity(shareActivityAddAndUpdateQuery));
    }


    //修改--暂时无此功能
    @PutMapping(value = "/admin/shareActivity")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        return shareActivityService.update(shareActivityAddAndUpdateQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivity/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeIds", required = false) List<Long> franchiseeIds,
                       @RequestParam(value = "type", required = false) String type,
                       @RequestParam(value = "status", required = false) Integer status) {
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

//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
//            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
//            if (CollectionUtils.isEmpty(franchiseeIds)) {
//                return R.ok(Collections.EMPTY_LIST);
//            }
//        }
//
//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
//            return R.ok(Collections.EMPTY_LIST);
//        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[])
                    JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            shareActivityQuery.setTypeList(typeList);
        }
        return shareActivityService.queryList(shareActivityQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivity/count")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "franchiseeIds", required = false) List<Long> franchiseeIds,
                        @RequestParam(value = "type", required = false) String type,
                        @RequestParam(value = "status", required = false) Integer status) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
//            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
//            if (CollectionUtils.isEmpty(franchiseeIds)) {
//                return R.ok(Collections.EMPTY_LIST);
//            }
//        }
//
//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
//            return R.ok(Collections.EMPTY_LIST);
//        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[]) JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            shareActivityQuery.setTypeList(typeList);
        }
        return shareActivityService.queryCount(shareActivityQuery);
    }

    //根据id查询活动详情
    @GetMapping(value = "/admin/shareActivity/queryInfo/{id}")
    public R queryInfo(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return shareActivityService.queryInfo(id);
    }

    @GetMapping(value = "/admin/shareActivity/queryPackages")
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

    @GetMapping(value = "/admin/shareActivity/queryPackagesCount")
    public R getElectricityUsablePackageCount(@RequestParam(value = "type",  required = true) Integer type) {

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
