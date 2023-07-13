package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
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
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<BatteryMembercardRefundOrderVO> selectByPage(BatteryMembercardRefundOrderQuery query);

    Integer selectPageCount(BatteryMembercardRefundOrderQuery query);

    BatteryMembercardRefundOrder selectByOrderNo(String orderNo);

    BatteryMembercardRefundOrder selectByMembercardOrderNo(String orderNo);

    Triple<Boolean,String,Object> batteryMembercardRefund(String orderNo);

    Triple<Boolean, String, Object> batteryMembercardRefundOrderDetail(String orderNo);

    Triple<Boolean, String, Object> batteryMembercardRefundAudit(String refundOrderNo, String errMsg, Integer status, HttpServletRequest request);

    Integer insert(BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert);

    Triple<Boolean, String, Object> batteryMembercardRefundForAdmin(String refundOrderNo, String errMsg, Integer status, HttpServletRequest request);
}
