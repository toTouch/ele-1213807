package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.RentCarTypeDTO;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜型号表(TElectricityCarModel)表控制层
 *
 * @author makejava
 * @since 2022-06-06 16:31:04
 */
@RestController
@Slf4j
public class JsonAdminElectricityCarModelController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserDataScopeService userDataScopeService;

    //新增换电柜车辆型号
    @PostMapping(value = "/admin/electricityCarModel")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarModelQuery electricityCarModelQuery) {
        if (verifyParams(electricityCarModelQuery)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return electricityCarModelService.save(electricityCarModelQuery);
    }

    //修改换电柜车辆型号
    @PutMapping(value = "/admin/electricityCarModel")
    @Log(title = "修改换电柜车辆型号")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCarModelQuery electricityCarModelQuery) {
        if (verifyParams(electricityCarModelQuery)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return electricityCarModelService.edit(electricityCarModelQuery);
    }

    //删除换电柜型号
    @DeleteMapping(value = "/admin/electricityCarModel/{id}")
    @Log(title = "删除换电柜车辆型号")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCarModelService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCarModel/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "storeId", required = false) Long storeId,
                       @RequestParam(value = "uid", required = false) Long uid) {
        if (size < 0 || size > 50 && size < 1000) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            List<Long> storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }

            List<Store> stores=storeService.selectByStoreIds(storeIds);
            if(CollectionUtils.isEmpty(stores)){
                return R.ok(Collections.EMPTY_LIST);
            }

            franchiseeIds = stores.stream().map(Store::getFranchiseeId).collect(Collectors.toList());
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .uid(uid)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCarModelService.queryList(electricityCarModelQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCarModel/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name) {

        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            List<Long> storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }

            List<Store> stores=storeService.selectByStoreIds(storeIds);
            if(CollectionUtils.isEmpty(stores)){
                return R.ok(Collections.EMPTY_LIST);
            }

            franchiseeIds = stores.stream().map(Store::getFranchiseeId).collect(Collectors.toList());
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .franchiseeIds(franchiseeIds)
                .name(name)
                .tenantId(tenantId).build();

        return electricityCarModelService.queryCount(electricityCarModelQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCarModel/selectByStoreId")
    public R queryCount(@RequestParam(value = "storeId", required = false) Long storeId) {

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .delFlag(ElectricityCarModel.DEL_NORMAL)
                .storeId(storeId)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCarModelService.selectByStoreId(electricityCarModelQuery);
    }


    /**
     * 校验金额不能为0
     *
     * @param electricityCarModelQuery
     * @return
     */
    private boolean verifyParams(ElectricityCarModelQuery electricityCarModelQuery) {
        //校验押金
        if (NumberConstant.ZERO_BD.compareTo(electricityCarModelQuery.getCarDeposit()) == NumberConstant.ONE) {
            return Boolean.TRUE;
        }
    
        //校验套餐
        if (StringUtils.isNotBlank(electricityCarModelQuery.getRentType())) {
            List<RentCarTypeDTO> rentCarTypes = JsonUtil.fromJsonArray(electricityCarModelQuery.getRentType(), RentCarTypeDTO.class);
            if (!CollectionUtils.isEmpty(rentCarTypes)) {
                for (RentCarTypeDTO rentCarType : rentCarTypes) {
                    if (BigDecimal.valueOf(0.01).compareTo(BigDecimal.valueOf(rentCarType.getPrice())) == NumberConstant.ONE) {
                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }
    
    @GetMapping(value = "/admin/electricityCarModel/pull")
    public R queryPull(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "franschiseeId") Long franchiseeId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        return electricityCarModelService.queryPull(size, offset, franchiseeId, name);
    }
}
