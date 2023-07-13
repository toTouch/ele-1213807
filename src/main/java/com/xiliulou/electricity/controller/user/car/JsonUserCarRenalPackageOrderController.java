package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageOrderQryReq;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐订单相关的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/renalPackageOrder")
public class JsonUserCarRenalPackageOrderController extends BasicController {

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;


    /**
     * 启用冻结套餐订单申请
     * @param packageOrderNo 购买订单编号
     * @return
     */
    @GetMapping("/enableFreezeRentOrder")
    public R<Boolean> enableFreezeRentOrder(String packageOrderNo) {

        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean enableFreezeFlag = carRentalPackageOrderBizService.enableFreezeRentOrder(tenantId, user.getUid(), packageOrderNo, false, user.getUid());

        return R.ok(enableFreezeFlag);
    }

    /**
     * 撤销冻结套餐订单申请
     * @param packageOrderNo 购买订单编号
     * @return
     */
    @GetMapping("/revokeFreezeRentOrder")
    public R<Boolean> revokeFreezeRentOrder(String packageOrderNo) {

        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean cancelFreezeFlag = carRentalPackageOrderBizService.revokeFreezeRentOrder(tenantId, user.getUid(), packageOrderNo);

        return R.ok(cancelFreezeFlag);
    }

    /**
     * 冻结套餐订单申请
     * @param packageOrderNo 购买订单编号
     * @param applyTerm 申请期限(天)
     * @return
     */
    @GetMapping("/freezeRentOrder")
    public R<Boolean> freezeRentOrder(String packageOrderNo, Integer applyTerm) {

        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean freezeFlag = carRentalPackageOrderBizService.freezeRentOrder(tenantId, user.getUid(), packageOrderNo, applyTerm);

        return R.ok(freezeFlag);

    }

    /**
     * 用户根据套餐购买订单编码进行订单退租申请
     * @param packageOrderNo 购买订单编码
     * @return
     */
    @GetMapping("/refundRentOrder")
    public R<Boolean> refundRentOrder(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean refundFlag = carRentalPackageOrderBizService.refundRentOrder(tenantId, user.getUid(), packageOrderNo);

        return R.ok(refundFlag);
    }


    /**
     * 条件查询列表
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageOrderVO>> page(@RequestBody CarRentalPackageOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderQryReq();
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        qryReq.setTenantId(tenantId);
        qryReq.setUid(user.getUid());

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        List<CarRentalPackageOrderPO> carRentalPackageOrderPOList = carRentalPackageOrderService.page(qryModel);

        // 获取辅助业务信息（套餐信息，车辆型号信息）
        Set<Long> rentalPackageIds = new HashSet<>();
        Set<Integer> carModelIds = new HashSet<>();

        carRentalPackageOrderPOList.forEach(carRentalPackageOrder -> {
            rentalPackageIds.add(carRentalPackageOrder.getRentalPackageId());
            carModelIds.add(carRentalPackageOrder.getCarModelId());
        });

        // 套餐名称信息
        Map<Long, String> carRentalPackageNameMap = getCarRentalPackageNameByIdsForMap(rentalPackageIds);

        // 车辆型号名称信息
        Map<Integer, String> carModelNameMap = getCarModelNameByIdsForMap(carModelIds);

        long nowTime = System.currentTimeMillis();

        // 模型转换，封装返回
        List<CarRentalPackageOrderVO> carRentalPackageVOList = carRentalPackageOrderPOList.stream().map(carRentalPackageOrder -> {
            CarRentalPackageOrderVO carRentalPackageOrderVO = new CarRentalPackageOrderVO();
            carRentalPackageOrderVO.setOrderNo(carRentalPackageOrder.getOrderNo());
            carRentalPackageOrderVO.setRentalPackageType(carRentalPackageOrder.getRentalPackageType());
            carRentalPackageOrderVO.setConfine(carRentalPackageOrder.getConfine());
            carRentalPackageOrderVO.setConfineNum(carRentalPackageOrder.getConfineNum());
            carRentalPackageOrderVO.setTenancy(carRentalPackageOrder.getTenancy());
            carRentalPackageOrderVO.setTenancyUnit(carRentalPackageOrder.getTenancyUnit());
            carRentalPackageOrderVO.setRent(carRentalPackageOrder.getRent());
            carRentalPackageOrderVO.setBatteryV(carRentalPackageOrder.getBatteryV());
            carRentalPackageOrderVO.setPayState(carRentalPackageOrder.getPayState());
            carRentalPackageOrderVO.setUseState(carRentalPackageOrder.getUseState());
            carRentalPackageOrderVO.setCreateTime(carRentalPackageOrder.getCreateTime());

            // 判定是否可退
            if (PayTypeEnum.GIVE.getCode().equals(carRentalPackageOrder.getPayType())) {
                // 1. 判定类型
                carRentalPackageOrderVO.setRentRebate(YesNoEnum.NO.getCode());
            } else {
                // 2. 判定可退截止时间
                carRentalPackageOrderVO.setRentRebate(carRentalPackageOrder.getRentRebateEndTime().longValue() >= nowTime ? YesNoEnum.NO.getCode() : YesNoEnum.YES.getCode());
            }

            // 赋值业务属性信息
            carRentalPackageOrderVO.setCarRentalPackageName(carRentalPackageNameMap.getOrDefault(carRentalPackageOrder.getRentalPackageId(), ""));
            carRentalPackageOrderVO.setCarModelName(carModelNameMap.getOrDefault(carRentalPackageOrder.getCarModelId(), ""));

            return carRentalPackageOrderVO;
        }).collect(Collectors.toList());

        return R.ok(carRentalPackageVOList);
    }

    /**
     * 条件查询总数
     * @param qryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageOrderQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageOrderQryReq();
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 赋值租户、用户
        qryReq.setTenantId(tenantId);
        qryReq.setUid(user.getUid());

        // 转换请求体
        CarRentalPackageOrderQryModel qryModel = new CarRentalPackageOrderQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageOrderService.count(qryModel));
    }

    /**
     * 查询用户正在使用的租车套餐信息（单车、车电一体）<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    @GetMapping("/queryUseRentalPackageOrder")
    public R<RentalPackageVO> queryUseRentalPackageOrder() {

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return carRentalPackageOrderBizService.queryUseRentalPackageOrderByUid(tenantId, user.getUid());
    }

    /**
     * <code>C</code>端用户取消套餐订单<br />
     * 用于如下情况
     * <pre>
     *     1. 用户未支付（主动终止流程，未曾真正调用微信支付系统）
     * </pre>
     * @param packageOrderNo 购买套餐订单编号
     * @return com.xiliulou.core.web.R<java.lang.Boolean>
     * @author xiaohui.song
     **/
    @GetMapping("/cancelRentalPackageOrder")
    public R<Boolean> cancelRentalPackageOrder(String packageOrderNo) {
        if (StringUtils.isBlank(packageOrderNo)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean cancelFlag = carRentalPackageOrderBizService.cancelRentalPackageOrder(packageOrderNo, tenantId, user.getUid());

        return R.ok(cancelFlag);
    }

    /**
     * <code>C</code>端用户购买租车套餐订单
     * @param buyOptModel 购买参数模型
     * @param request HTTP请求
     * @return com.xiliulou.core.web.R
     * @author xiaohui.song
     */
    @PostMapping("/buyRentalPackageOrder")
    public R<?> buyRentalPackageOrder(@RequestBody CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
        // 参数基本校验
        if (ObjectUtils.allNotNull(buyOptModel, buyOptModel.getRentalPackageId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // 从上下文中重新赋值参数
        buyOptModel.setTenantId(tenantId);
        buyOptModel.setUid(user.getUid());

        return carRentalPackageOrderBizService.buyRentalPackageOrder(buyOptModel, request);
    }

}
