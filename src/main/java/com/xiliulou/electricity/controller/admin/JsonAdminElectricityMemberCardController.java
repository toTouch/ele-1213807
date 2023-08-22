package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.DivisionAccountBatteryMembercard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:25
 **/
@RestController
@Slf4j
public class JsonAdminElectricityMemberCardController {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    @Autowired
    UserDataScopeService userDataScopeService;
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    @Autowired
    private CarRentalPackageService carRentalPackageService;

//    /**
//     * 新增
//     *
//     * @return
//     */
//    @PostMapping("admin/electricityMemberCard")
//    public R add(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
//        return electricityMemberCardService.add(electricityMemberCard);
//    }
//
//    /**
//     * 修改
//     *
//     * @return
//     */
//    @PutMapping("admin/electricityMemberCard")
//    @Log(title = "修改套餐")
//    public R update(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
//        if (Objects.isNull(electricityMemberCard)) {
//            return R.failMsg("请求参数不能为空!");
//        }
//        return electricityMemberCardService.update(electricityMemberCard);
//    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("admin/electricityMemberCard/{id}")
    @Log(title = "删除套餐")
    public R delete(@PathVariable(value = "id") Integer id) {
        return electricityMemberCardService.delete(id);
    }

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/list")
    public R getElectricityMemberCardPage(@RequestParam(value = "offset") Long offset,
                                          @RequestParam(value = "size") Long size,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "cardModel", required = false) Integer cardModel) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }
        
        return electricityMemberCardService.queryList(offset, size, status, type, TenantContextHolder.getTenantId(), cardModel, franchiseeIds);
    }


    /**
     * 分页数量
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/queryCount")
    public R getElectricityMemberCardPage(@RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "cardModel", required = false) Integer cardModel) {

        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        return electricityMemberCardService.queryCount(status, type, tenantId, cardModel, franchiseeIds);
    }


    /**
     * 加盟商分页
     *
     * @return
     */
    @GetMapping("/admin/electricityMemberCard/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "offset") Long offset,
                              @RequestParam(value = "size") Long size,
                              @RequestParam(value = "type", required = false) Integer type,
                              @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        if(CollectionUtils.isEmpty(franchiseeIds)){
            return R.ok(Collections.EMPTY_LIST);
        }
        
        return electricityMemberCardService.listByFranchisee(offset, size, status, type, tenantId, franchiseeIds);
    }

    /**
     * 加盟商分页数量
     *
     * @return
     */
    @GetMapping("/admin/electricityMemberCard/listCountByFranchisee")
    public R listCountByFranchisee(@RequestParam(value = "type", required = false) Integer type,
                                   @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        if(CollectionUtils.isEmpty(franchiseeIds)){
            return R.ok(Collections.EMPTY_LIST);
        }
        
        return electricityMemberCardService.listCountByFranchisee(status, type, tenantId, franchiseeIds);
    }

    /**
     * 根据名称模糊搜索套餐
     * @return
     */
//    @GetMapping(value = "/admin/electricityMemberCard/selectByQuery")
//    public R selectByQuery(@RequestParam(value = "name", required = false) String name,
//                           @RequestParam(value = "cardModel", required = false) Integer cardModel) {
//        ElectricityMemberCardQuery cardQuery = ElectricityMemberCardQuery.builder()
//                .name(name)
//                .cardModel(cardModel)
//                .tenantId(TenantContextHolder.getTenantId())
//                .build();
//        return R.ok(electricityMemberCardService.selectByQuery(cardQuery));
//    }


    //查询换电套餐根据加盟商
//    @GetMapping(value = "/admin/electricityMemberCard/queryByFranchisee/{id}")
//    public R getElectricityBatteryList(@PathVariable("id") Long id) {
//        Integer tenantId = TenantContextHolder.getTenantId();
//        return R.ok(electricityMemberCardService.selectByFranchiseeId(id, tenantId));
//    }

    //查询未删除并且启用换电套餐根据加盟商
    @GetMapping(value = "/admin/electricityMemberCard/queryUsableByFranchisee/{id}")
    public R getElectricityUsableBatteryList(@PathVariable("id") Long id) {
//        Integer tenantId = TenantContextHolder.getTenantId();
//        return R.ok(electricityMemberCardService.getElectricityUsableBatteryList(id,tenantId));
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .franchiseeId(id)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .status(BatteryMemberCard.STATUS_UP)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return R.ok(batteryMemberCardService.selectByQuery(query));
    }

    /**
     * 根据业务类型获取可用套餐列表
     * @param offset
     * @param size
     * @param franchiseeId
     * @param type
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/queryUsableByFranchisee")
    public R getElectricityUsablePackage(@RequestParam(value = "offset") Long offset,
                                             @RequestParam(value = "size") Long size,
                                             @RequestParam(value = "franchiseeId",  required = true) Long franchiseeId,
                                             @RequestParam(value = "type",  required = true) Integer type) {

            if(!DivisionAccountBatteryMembercard.PACKAGE_TYPES.contains(type)){
                return R.fail("000200", "业务类型参数不合法");
            }

            if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
                BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                        .offset(offset)
                        .size(size)
                        .franchiseeId(franchiseeId)
                        .delFlag(BatteryMemberCard.DEL_NORMAL)
                        .status(BatteryMemberCard.STATUS_UP)
                        .tenantId(TenantContextHolder.getTenantId()).build();
                return R.ok(batteryMemberCardService.selectByQuery(query));
            }else{
                CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
                qryModel.setOffset(offset.intValue());
                qryModel.setSize(size.intValue());
                qryModel.setFranchiseeId(franchiseeId.intValue());
                qryModel.setTenantId(TenantContextHolder.getTenantId());
                qryModel.setStatus(UpDownEnum.UP.getCode());

                if(DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY.equals(type)){
                    qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
                }else if(DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL.equals(type)){
                    qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
                }

                return R.ok(batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel));
            }
    }

    /**
     * 根据业务类型获取可用套餐列表总数
     * @param franchiseeId
     * @param type
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/queryUsableCount")
    public R getElectricityUsablePackageCount(@RequestParam(value = "franchiseeId",  required = true) Long franchiseeId,
                                              @RequestParam(value = "type",  required = true) Integer type) {

        if(!DivisionAccountBatteryMembercard.PACKAGE_TYPES.contains(type)){
            return R.fail("000200", "业务类型参数不合法");
        }

        if(DivisionAccountBatteryMembercard.TYPE_BATTERY.equals(type)){
            BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                    .franchiseeId(franchiseeId)
                    .delFlag(BatteryMemberCard.DEL_NORMAL)
                    .status(BatteryMemberCard.STATUS_UP)
                    .tenantId(TenantContextHolder.getTenantId()).build();
            return R.ok(batteryMemberCardService.selectByPageCount(query));
        }else{
            CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
            qryModel.setFranchiseeId(franchiseeId.intValue());
            qryModel.setTenantId(TenantContextHolder.getTenantId());
            qryModel.setStatus(UpDownEnum.UP.getCode());

            if(DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY.equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
            }else if(DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL.equals(type)){
                qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
            }
            return R.ok(carRentalPackageService.count(qryModel));
        }
    }

    /**
     * 根据加盟商id获取所有套餐
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/selectByFranchiseeId/{id}")
    public R selectByFranchiseeId(@PathVariable("id") Long id) {
//
//        ElectricityMemberCardQuery query = ElectricityMemberCardQuery.builder()
//                .tenantId(TenantContextHolder.getTenantId())
//                .franchiseeId(id).build();
//
//        return R.ok(electricityMemberCardService.selectByQuery(query));

        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().franchiseeId(id).tenantId(TenantContextHolder.getTenantId()).build();
        return R.ok(batteryMemberCardService.selectByQuery(query));
    }

    /**
     * 根据加盟商id获取所有套餐
     * @return
     */
//    @GetMapping(value = "/admin/electricityMemberCard/queryAll")
//    public R selectAll(@RequestParam(value = "name", required = false) String name) {
//
//        ElectricityMemberCardQuery query = ElectricityMemberCardQuery.builder()
//                .tenantId(TenantContextHolder.getTenantId())
//                .name(name).build();
//
//        return R.ok(electricityMemberCardService.selectByQuery(query));
//    }

    /**
     * 用户停卡记录
     *
     * @param offset
     * @param size
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/disableMemberCard")
    public R getElectricityDisableMemberCardList(@RequestParam(value = "offset") Long offset,
                                                 @RequestParam(value = "size") Long size,
                                                 @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
                                                 @RequestParam(value = "phone", required = false) String phone,
                                                 @RequestParam(value = "status", required = false) Integer status,
                                                 @RequestParam(value = "uid", required = false) Long uid) {
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

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder()
                .offset(offset)
                .size(size)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .disableMemberCardNo(disableMemberCardNo)
                .phone(phone)
                .status(status)
                .uid(uid)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return eleDisableMemberCardRecordService.list(electricityMemberCardRecordQuery);
    }

    /**
     * 停卡记录count
     *
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/disableMemberCardCount")
    public R getElectricityDisableMemberCardCount(@RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
                                                  @RequestParam(value = "phone", required = false) String phone,
                                                  @RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "uid", required = false) Long uid) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(NumberConstant.ZERO);
        }

        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder()
                .disableMemberCardNo(disableMemberCardNo)
                .phone(phone)
                .status(status)
                .uid(uid)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return eleDisableMemberCardRecordService.queryCount(electricityMemberCardRecordQuery);
    }

    /**
     * 审核用户停卡
     *
     */
    @PostMapping(value = "/admin/electricityMemberCard/reviewDisableMemberCard")
    @Log(title = "用户暂停套餐审核")
    public R reviewDisableMemberCard(@RequestParam("disableMemberCardNo") String disableMemberCardNo,
                                     @RequestParam("status") Integer status,
                                     @RequestParam(value = "errMsg", required = false) String errMsg) {
        return eleDisableMemberCardRecordService.reviewDisableMemberCard(disableMemberCardNo, errMsg, status);
    }


}
