package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.service.car.CarDataService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.PageDataResult;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Objects;


/**
 * 车辆运营数据
 */
@Slf4j
@RestController
public class JsonAdminCarDataController extends BaseController {

    @Autowired
    private CarDataService carDataService;
    private Triple<Boolean, String, Object> verifyUserPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return Triple.of(false, null, Collections.emptyList());
        }

        return Triple.of(true, null, null);
    }
    /**
     * 获取全部车辆的分页数据
     */
    @GetMapping("/admin/carData/allCar/page")
    public R queryAllCarDataPage(@RequestParam("offset") Long offset,
                                 @RequestParam("size") Long size,
                                 @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                 @RequestParam(value = "storeId", required = false) Long storeId,
                                 @RequestParam(value = "modelId", required = false) Integer modelId,
                                 @RequestParam(value = "sn", required = false) String sn,
                                 @RequestParam(value = "userName", required = false) String userName,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return carDataService.queryAllCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取全部车辆的数据总数
     */
    @GetMapping("/admin/carData/allCar/count")
    public R queryAllCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }
        return carDataService.queryAllCarDataCount(franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取待租车辆的分页数据
     */
    @GetMapping("/admin/carData/pendingRentalCar/page")
    public R queryPendingRentalCarDataPage(@RequestParam("offset") Long offset,
                                           @RequestParam("size") Long size,
                                           @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                           @RequestParam(value = "storeId", required = false) Long storeId,
                                           @RequestParam(value = "modelId", required = false) Integer modelId,
                                           @RequestParam(value = "sn", required = false) String sn,
                                           @RequestParam(value = "userName", required = false) String userName,
                                           @RequestParam(value = "phone", required = false) String phone,
                                           @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return carDataService.queryPendingRentalCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取待租车辆的数据总数
     */
    @GetMapping("/admin/carData/pendingRentalCar/count")
    public R queryPendingRentalCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }
        return carDataService.queryPendingRentalCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取已租车辆的分页数据
     */
    @GetMapping("/admin/carData/leasedRentalCar/page")
    public R queryLeasedCarDataPage(@RequestParam("offset") Long offset,
                                    @RequestParam("size") Long size,
                                    @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                    @RequestParam(value = "storeId", required = false) Long storeId,
                                    @RequestParam(value = "modelId", required = false) Integer modelId,
                                    @RequestParam(value = "sn", required = false) String sn,
                                    @RequestParam(value = "userName", required = false) String userName,
                                    @RequestParam(value = "phone", required = false) String phone,
                                    @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return carDataService.queryLeasedCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取已租车辆的数据总数
     */
    @GetMapping("/admin/carData/leasedRentalCar/count")
    public R queryLeasedCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }
        return carDataService.queryLeasedCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取逾期车辆的分页数据
     */
    @GetMapping("/admin/carData/overdueCar/page")
    public R queryOverdueCarDataPage(@RequestParam("offset") Long offset,
                                     @RequestParam("size") Long size,
                                     @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                     @RequestParam(value = "storeId", required = false) Long storeId,
                                     @RequestParam(value = "modelId", required = false) Integer modelId,
                                     @RequestParam(value = "sn", required = false) String sn,
                                     @RequestParam(value = "userName", required = false) String userName,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return carDataService.queryOverdueCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }

    /**
     * 获取逾期车辆的数据总数
     */
    @GetMapping("/admin/carData/overdueCar/count")
    public R queryOverdueCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }
        return carDataService.queryOverdueCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone,uid);
    }
}
