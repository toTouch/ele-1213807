package com.xiliulou.electricity.controller.user.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositPayQryModel;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车押金的 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/car/deposit/pay")
public class JsonUserCarDepositPayController extends BasicController {

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    /**
     * 查询免押状态
     * @return true(成功)、false(失败)
     */
    @GetMapping("/queryFreeDepositStatus")
    public R<FreeDepositUserInfoVo> queryFreeDepositStatus() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(carRenalPackageDepositBizService.queryFreeDepositStatus(tenantId, user.getUid()));
    }

    /**
     * 创建免押订单
     * @param freeDepositOptReq 免押订单申请数据模型
     * @return 生成二维码的网址
     */
    @PostMapping("/createFreeDeposit")
    public R<String> createFreeDeposit(@RequestBody @Valid FreeDepositOptReq freeDepositOptReq) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(carRenalPackageDepositBizService.createFreeDeposit(tenantId, user.getUid(), freeDepositOptReq));
    }

    /**
     * 分页查询
     * @param offset 偏移量
     * @param size 取值数量
     * @return 押金缴纳订单集
     */
    @GetMapping("/page")
    public R<List<CarRentalPackageDepositPayVO>> page(Integer offset, Integer size) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());
        qryModel.setOffset(offset);
        qryModel.setSize(size);
        qryModel.setPayState(PayStateEnum.SUCCESS.getCode());


        // 调用服务
        List<CarRentalPackageDepositPayPO> depositPayEntityList = carRentalPackageDepositPayService.page(qryModel);
        if (CollectionUtils.isEmpty(depositPayEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 模型转换，封装返回
        List<CarRentalPackageDepositPayVO> depositPayVoList = depositPayEntityList.stream().map(depositPayEntity -> {
            CarRentalPackageDepositPayVO depositPayVo = new CarRentalPackageDepositPayVO();
            BeanUtils.copyProperties(depositPayEntity, depositPayVo);
            return depositPayVo;
        }).collect(Collectors.toList());

        return R.ok(depositPayVoList);
    }

    /**
     * 查询总数
     * @return 总数
     */
    @GetMapping("/count")
    public R<Integer> count() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CarRentalPackageDepositPayQryModel qryModel = new CarRentalPackageDepositPayQryModel();
        qryModel.setTenantId(tenantId);
        qryModel.setUid(user.getUid());
        qryModel.setPayState(PayStateEnum.SUCCESS.getCode());


        // 调用服务
        return R.ok(carRentalPackageDepositPayService.count(qryModel));
    }

    /**
     * 用户名下的押金信息(单车、车电一体)
     * @return
     */
    @GetMapping("/queryUnRefundCarDeposit")
    public R<CarRentalPackageDepositPayVO> queryUnRefundCarDeposit() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("not found user.");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRenalPackageDepositBizService.selectUnRefundCarDeposit(tenantId, user.getUid()));
    }

}
