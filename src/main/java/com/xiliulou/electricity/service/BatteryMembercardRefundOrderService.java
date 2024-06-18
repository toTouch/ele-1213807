package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * (BatteryMembercardRefundOrder)表服务接口
 *
 * @author zzlong
 * @since 2023-07-12 15:56:43
 */
public interface BatteryMembercardRefundOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMembercardRefundOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMembercardRefundOrder queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param batteryMembercardRefundOrder 实例对象
     * @return 实例对象
     */
    Integer update(BatteryMembercardRefundOrder batteryMembercardRefundOrder);
    
    /**
     * 根据更换手机号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid,String newPhone);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<BatteryMembercardRefundOrderVO> selectByPage(BatteryMembercardRefundOrderQuery query);

    Integer selectPageCount(BatteryMembercardRefundOrderQuery query);

    BatteryMembercardRefundOrder selectByRefundOrderNo(String orderNo);

    BatteryMembercardRefundOrder selectLatestByMembercardOrderNo(String orderNo);

    Triple<Boolean,String,Object> batteryMembercardRefund(String orderNo);

    Triple<Boolean, String, Object> batteryMembercardRefundOrderDetail(String orderNo,Integer confirm);

    void updateUserCouponStatus(String orderId);

    Triple<Boolean, String, Object> batteryMembercardRefundAudit(String refundOrderNo, String errMsg, BigDecimal refundAmount, Integer status, HttpServletRequest request);

    Integer insert(BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert);

    Triple<Boolean, String, Object> batteryMembercardRefundForAdmin(String orderNo, BigDecimal refundAmount, HttpServletRequest request);

    WechatJsapiRefundResultDTO handleRefundOrder(BatteryMembercardRefundOrder batteryMembercardRefundOrder, WechatPayParamsDetails wechatPayParamsDetails, HttpServletRequest request) throws WechatPayException;

    List<BatteryMembercardRefundOrder> selectRefundingOrderByUid(Long uid);

    BigDecimal selectUserTotalRefund(Integer tenantId, Long uid);

    void sendAuditNotify(UserInfo userInfo);
    
    void sendMerchantRebateRefundMQ(Long uid, String orderId);
    
    R checkPayParamsDetails(String orderNo);
}
