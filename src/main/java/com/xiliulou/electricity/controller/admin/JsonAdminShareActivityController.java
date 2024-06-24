package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
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
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        
        return shareActivityService.insert(shareActivityAddAndUpdateQuery, user, franchiseeId);
    }

    @GetMapping(value = "/admin/shareActivity/detail/{id}")
    public R detail(@PathVariable("id") Integer id){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        
        return returnTripleResult(shareActivityService.shareActivityDetail(id, franchiseeId));
    }

    /**
     * 编辑功能
     * @param shareActivityAddAndUpdateQuery
     * @return
     */
    @PutMapping(value = "/admin/shareActivity/update")
    public R updateActivity(@RequestBody @Validated(value = UpdateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        
        return returnTripleResult(shareActivityService.updateShareActivity(shareActivityAddAndUpdateQuery, franchiseeId));
    }

    /**
     * 上下架活动
     * @param shareActivityAddAndUpdateQuery
     * @return
     */
    @PutMapping(value = "/admin/shareActivity")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        
        return shareActivityService.update(shareActivityAddAndUpdateQuery, franchiseeId);
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivity/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeIds", required = false) List<Long> franchiseeIds,
                       @RequestParam(value = "type", required = false) String type,
                       @RequestParam(value = "status", required = false) Integer status,
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
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .franchiseeId(franchiseeId).build();

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
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .franchiseeId(franchiseeId).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[]) JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            shareActivityQuery.setTypeList(typeList);
        }
        return shareActivityService.queryCount(shareActivityQuery);
    }

    //根据id查询活动详情（跟时孟杨确认该接口暂无调用）
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
                                       @RequestParam(value = "type",  required = true) Integer type,
                                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if(!packageTypes.contains(type)){
            return R.fail("000200", "业务类型参数不合法");
        }

        //需要获取租金不可退的套餐
        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)){
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                    .offset(offset)
                    .size(size)
                    .delFlag(BatteryMemberCard.DEL_NORMAL)
                    .status(BatteryMemberCard.STATUS_UP)
                    .isRefund(BatteryMemberCard.NO)
                    .tenantId(TenantContextHolder.getTenantId())
                    .franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByQuery(query));
        }else{
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setOffset(offset.intValue());
            qryModel.setSize(size.intValue());
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());
            qryModel.setFranchiseeId(franchiseeId.intValue());

            if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }

            return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
        }

    }

    @GetMapping(value = "/admin/shareActivity/queryPackagesCount")
    public R getElectricityUsablePackageCount(@RequestParam(value = "type", required = true) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        List<Integer> packageTypes = Arrays.stream(PackageTypeEnum.values()).map(PackageTypeEnum::getCode).collect(Collectors.toList());
        if(!packageTypes.contains(type)){
            return R.fail("000200", "业务类型参数不合法");
        }

        //需要获取租金不可退的套餐
        if(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(type)){
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                    .delFlag(BatteryMemberCard.DEL_NORMAL)
                    .status(BatteryMemberCard.STATUS_UP)
                    .isRefund(BatteryMemberCard.NO)
                    .tenantId(TenantContextHolder.getTenantId())
                    .franchiseeId(franchiseeId).build();
            return R.ok(batteryMemberCardService.selectByPageCount(query));
        }else{
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());
            qryModel.setRentRebate(YesNoEnum.NO.getCode());
            qryModel.setFranchiseeId(franchiseeId.intValue());

            if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            return R.ok(carRentalPackageService.count(qryModel));
        }
    }
    
    
    @GetMapping(value = "/admin/shareActivity/checkExistActivity")
    public R checkExistActivity(){
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return shareActivityService.checkExistActivity();
    }
    
    /**
     * <p>
     *    Description: delete
     *    9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @GetMapping("/admin/shareActivity/delete")
    public R<?> delete(@RequestParam("id") Long id){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("未找到用户!");
        }
        
        if (Objects.isNull(id)){
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
    
            franchiseeId = franchiseeIds.get(0);
        }
        
        return shareActivityService.removeById(id, franchiseeId);
    }
}
