package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.dto.UserDelStatusDTO;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderSlippageQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderSlippageQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderSlippageVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐订单逾期订单 Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackageOrderSlippage")
public class JsonAdminCarRentalPackageOrderSlippageController extends BasicController {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;
    
    @Resource
    private UserDelRecordService userDelRecordService;
    
    /**
     * 条件查询列表
     * @param qryReq 请求参数类
     * @return 逾期订单集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderSlippageVo>> page(@RequestBody CarRentalPackageOrderSlippageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderSlippageQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(Collections.emptyList());
        }

        // 转换请求体
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.page(qryModel);
        if (CollectionUtils.isEmpty(slippageEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（用户信息、加盟商信息）
        Set<Long> uids = new HashSet<>();
        Set<Long> franchiseeIds = new HashSet<>();
        slippageEntityList.forEach(slippagePO -> {
            uids.add(slippagePO.getUid());
            franchiseeIds.add(Long.valueOf(slippagePO.getFranchiseeId()));
        });

        // 用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoByUidsForMap(uids);

        // 加盟商信息
        Map<Long, String> franchiseeNameMap = getFranchiseeNameByIdsForMap(franchiseeIds);
    
        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(new ArrayList<>(uids),
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));
        
        // 模型转换，封装返回
        List<CarRentalPackageOrderSlippageVo> slippageVOList = slippageEntityList.stream().map(slippageEntity -> {

            CarRentalPackageOrderSlippageVo slippageVo = new CarRentalPackageOrderSlippageVo();
            BeanUtils.copyProperties(slippageEntity, slippageVo);
    
            Long uid = slippageEntity.getUid();
            if (!userInfoMap.isEmpty()) {
                UserInfo userInfo = userInfoMap.getOrDefault(uid, new UserInfo());
                slippageVo.setUserRelName(userInfo.getName());
                slippageVo.setUserPhone(userInfo.getPhone());
            }

            if (!franchiseeNameMap.isEmpty()) {
                slippageVo.setFranchiseeName(franchiseeNameMap.getOrDefault(Long.valueOf(slippageEntity.getFranchiseeId()), ""));
            }
    
            // 查询已删除/已注销
            slippageVo.setUserStatus(userDelRecordService.getUserStatus(uid, userStatusMap));
            
            return slippageVo;
        }).collect(Collectors.toList());

        return R.ok(slippageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderSlippageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderSlippageQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 数据权校验
        Triple<List<Integer>, List<Integer>, Boolean> permissionTriple = checkPermissionInteger();
        if (!permissionTriple.getRight()) {
            return R.ok(NumberConstant.ZERO);
        }

        // 转换请求体
        CarRentalPackageOrderSlippageQryModel qryModel = new CarRentalPackageOrderSlippageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(permissionTriple.getLeft());
        qryModel.setStoreIdList(permissionTriple.getMiddle());

        // 调用服务
        return R.ok(carRentalPackageOrderSlippageService.count(qryModel));
    }

}
