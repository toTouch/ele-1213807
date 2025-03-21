package com.xiliulou.electricity.service.car.v2;

import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.SystemDefinitionEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.FreeDepositVO;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;

import java.math.BigDecimal;

/**
 * 租车套餐押金业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRenalPackageDepositV2BizService {

    /**
     * 运营商端创建退押，特殊退押(2.0过度数据)
     *
     * @param optModel 操作数据模型
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    boolean refundDepositCreateSpecial(CarRentalPackageDepositRefundOptModel optModel, Long optUid);

    /**
     * 运营商端创建退押
     *
     * @param optModel 操作数据模型
     * @param optUid   操作人UID
     * @return
     */
    boolean refundDepositCreate(CarRentalPackageDepositRefundOptModel optModel, Long optUid);


    /**
     * 审批退还押金申请单
     *
     * @param refundDepositOrderNo 退押申请单
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @param compelOffLine        强制线下退款
     * @return
     */
    boolean approveRefundDepositOrder(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, BigDecimal refundAmount, Integer compelOffLine);


    /**
     * 查询免押状态
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return true(成功)、false(失败)
     */
    FreeDepositUserInfoVo queryFreeDepositStatus(Integer tenantId, Long uid);

    /**
     * 创建免押订单，生成二维码<br /> 创建押金缴纳订单、生成免押记录
     *
     * @param tenantId          租户ID
     * @param uid               C端用户ID
     * @param freeDepositOptReq 免押申请数据
     * @return
     */
    FreeDepositVO createFreeDeposit(Integer tenantId, Long uid, FreeDepositOptReq freeDepositOptReq);


    /**
     * C端退押申请
     *
     * @param tenantId          租户ID
     * @param uid               用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @return
     */
    boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo);

    /**
     * 用户名下的押金信息(单车、车电一体)
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金缴纳信息
     */
    CarRentalPackageDepositPayVo selectUnRefundCarDeposit(Integer tenantId, Long uid);


    CarRentalPackageMemberTermPo buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePo packageEntity, String depositPayOrderNo,
                                                                 CarRentalPackageMemberTermPo memberTermEntity);

    Boolean isCarZeroDepositOrder(UserInfo userInfo);
}