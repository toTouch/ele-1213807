package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.enums.SystemDefinitionEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.query.car.CarRentalPackageRefundReq;
import com.xiliulou.electricity.vo.car.CarRentRefundVo;
import com.xiliulou.electricity.vo.rental.RefundRentOrderHintVo;
import com.xiliulou.electricity.vo.rental.RentalPackageRefundVO;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐购买业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderBizService {

    /**
     * 根据用户UID查询总金额<br />
     * 订单支付成功总金额 - 退租订单成功总金额
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @return 总金额
     */
    BigDecimal queryAmountTotalByUid(Integer tenantId, Long uid);

    /**
     * 退租提示
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 套餐购买订单编码
     * @return 提示模型
     */
    RefundRentOrderHintVo refundRentOrderHint(Integer tenantId, Long uid, String packageOrderNo);

    /**
     * 查询针对当前套餐发起退款时的信息
     * @param orderNo
     * @return
     */
    RentalPackageRefundVO queryRentalPackageRefundData(String orderNo);

    /**
     * 后台租车退款确认业务
     * @param carRentalPackageRefundReq
     * @return
     */
    Boolean refundConfirmation(CarRentalPackageRefundReq carRentalPackageRefundReq);

    /**
     * 后端给用户绑定套餐
     * @param buyOptModel 购买套餐数据模型
     * @return true(成功)、false(失败)
     */
    boolean bindingPackage (CarRentalPackageOrderBuyOptModel buyOptModel);

    /**
     * 审批冻结申请单
     * @param freezeRentOrderNo 冻结申请单编码
     * @param approveFlag 审批标识，true(同意)；false(驳回)
     * @param apploveDesc 审批意见
     * @param apploveUid 审批人
     * @return
     */
    Boolean approveFreezeRentOrder(String freezeRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid);

    /**
     * 审批退租申请单
     * @param refundRentOrderNo 退租申请单编码
     * @param approveFlag 审批标识，true(同意)；false(驳回)
     * @param apploveDesc 审批意见
     * @param apploveUid 审批人
     * @return
     */
    Boolean approveRefundRentOrder(String refundRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid);

    /**
     * 审批退租申请单, 支持页面提交可退款金额
     * @param carRentRefundVo
     * @return
     */
    Boolean approveRefundRentOrder(CarRentRefundVo carRentRefundVo);

    /**
     * 启用用户冻结订单<br />
     * 自动启用，用于定时任务
     * @param offset 偏移量
     * @param size 取值数量
     */
    void enableFreezeRentOrderAuto(Integer offset, Integer size);

    /**
     * 启用用户冻结订单<br />
     * 手动启用
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID
     * @return true(成功)、false(失败)
     */
    Boolean enableFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid);

    /**
     * 撤销用户冻结订单申请
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @return
     */
    Boolean revokeFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo);

    /**
     * 根据用户ID及订单编码进行冻结订单申请
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 套餐购买订单编号
     * @param applyTerm 申请期限(天)
     * @param applyReason 申请理由
     * @param systemDefinitionEnum 操作系统
     * @param optUid 操作人ID
     * @return
     */
    Boolean freezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Integer applyTerm, String applyReason, SystemDefinitionEnum systemDefinitionEnum, Long optUid);

    /**
     * 根据用户ID及订单编码，退租购买的订单申请
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 套餐购买订单编号
     * @param optUid 操作人ID
     * @return
     */
    Boolean refundRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid);

    /**
     * 根据用户ID查询正在使用的套餐信息<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    R<RentalPackageVO> queryUseRentalPackageOrderByUid(Integer tenantId, Long uid);

    /**
     * 取消租车套餐订单
     * @param packageOrderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    Boolean cancelRentalPackageOrder(String packageOrderNo, Integer tenantId, Long uid);

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    R buyRentalPackageOrder (CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request);

    /**
     * 支付成功之后的逻辑<br />
     * 此处逻辑不包含回调处理，是回调逻辑中的一处子逻辑<br />
     * 调用此方法需要慎重
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param userCouponIds 用户优惠券ID集，可为空
     * @return
     */
    Pair<Boolean, Object> handBuyRentalPackageOrderSuccess(String orderNo, Integer tenantId, Long uid, List<Long> userCouponIds);

    /**
     * 支付失败之后的逻辑
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    Pair<Boolean, Object> handBuyRentalPackageOrderFailed(String orderNo, Integer tenantId, Long uid);

    /**
     *  根据资产类型,租户，uid，查询用户是否存在对应的资产
     * @param uid
     * @param tenantId
     * @param assetType
     * @return
     */
    boolean checkUserHasAssets(Long uid,Integer tenantId,Integer assetType);

}
