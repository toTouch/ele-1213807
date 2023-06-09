package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityMemberCardQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
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

    /**
     * 新增
     *
     * @return
     */
    @PostMapping("admin/electricityMemberCard")
    public R add(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        return electricityMemberCardService.add(electricityMemberCard);
    }

    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/electricityMemberCard")
    @Log(title = "修改套餐")
    public R update(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        if (Objects.isNull(electricityMemberCard)) {
            return R.failMsg("请求参数不能为空!");
        }
        return electricityMemberCardService.update(electricityMemberCard);
    }

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
    @GetMapping(value = "/admin/electricityMemberCard/selectByQuery")
    public R selectByQuery(@RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "cardModel", required = false) Integer cardModel) {
        ElectricityMemberCardQuery cardQuery = ElectricityMemberCardQuery.builder()
                .name(name)
                .cardModel(cardModel)
                .tenantId(TenantContextHolder.getTenantId())
                .build();
        return R.ok(electricityMemberCardService.selectByQuery(cardQuery));
    }


    //查询换电套餐根据加盟商
    @GetMapping(value = "/admin/electricityMemberCard/queryByFranchisee/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityMemberCardService.selectByFranchiseeId(id, tenantId));
    }

    //查询未删除并且启用换电套餐根据加盟商
    @GetMapping(value = "/admin/electricityMemberCard/queryUsableByFranchisee/{id}")
    public R getElectricityUsableBatteryList(@PathVariable("id") Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(electricityMemberCardService.getElectricityUsableBatteryList(id,tenantId));
    }

    /**
     * 根据加盟商id获取所有套餐
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/electricityMemberCard/selectByFranchiseeId/{id}")
    public R selectByFranchiseeId(@PathVariable("id") Long id) {

        ElectricityMemberCardQuery query = ElectricityMemberCardQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(id).build();

        return R.ok(electricityMemberCardService.selectByQuery(query));
    }

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
                                                 @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder()
                .offset(offset)
                .size(size)
                .disableMemberCardNo(disableMemberCardNo)
                .phone(phone)
                .status(status)
                .tenantId(tenantId).build();

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
                                                  @RequestParam(value = "status", required = false) Integer status) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder()
                .disableMemberCardNo(disableMemberCardNo)
                .phone(phone)
                .status(status)
                .tenantId(tenantId).build();

        return eleDisableMemberCardRecordService.queryCount(electricityMemberCardRecordQuery);
    }

    /**
     * 审核用户停卡
     *
     * @param disableMemberCardNo
     * @param status
     * @param errMsg
     * @return
     */
    @PostMapping(value = "/admin/electricityMemberCard/reviewDisableMemberCard")
    @Log(title = "用户暂停套餐审核")
    public R reviewDisableMemberCard(@RequestParam("disableMemberCardNo") String disableMemberCardNo,
                                     @RequestParam("status") Integer status,
                                     @RequestParam(value = "errMsg", required = false) String errMsg) {
        return eleDisableMemberCardRecordService.reviewDisableMemberCard(disableMemberCardNo, errMsg, status);
    }


}
