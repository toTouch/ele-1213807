package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.pay.weixin.entity.RefundQuery;
import com.xiliulou.pay.weixin.entity.WeiXinRefundNotify;
import com.xiliulou.pay.weixin.refund.RefundAdapterHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
@Service("eleRefundOrderService")
@Slf4j
public class EleRefundOrderServiceImpl implements EleRefundOrderService {
    @Resource
    EleRefundOrderMapper eleRefundOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RefundAdapterHandler refundAdapterHandler;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleRefundOrder queryByIdFromDB(Long id) {
        return this.eleRefundOrderMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  EleRefundOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleRefundOrder insert(EleRefundOrder eleRefundOrder) {
        this.eleRefundOrderMapper.insert(eleRefundOrder);
        return eleRefundOrder;
    }

    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleRefundOrder eleRefundOrder) {
       return this.eleRefundOrderMapper.update(eleRefundOrder);
         
    }


    @Override
    public Pair<Boolean, Object> commonCreateRefundOrder(RefundOrder refundOrder, ElectricityPayParams electricityPayParams, HttpServletRequest request) {
        //退款订单
        EleRefundOrder eleRefundOrder = new EleRefundOrder();
        eleRefundOrder.setRefundOrderNo(refundOrder.getRefundOrderNo());
        eleRefundOrder.setOrderId(refundOrder.getOrderId());
        eleRefundOrder.setPayAmount(refundOrder.getPayAmount());
        eleRefundOrder.setRefundAmount(refundOrder.getRefundAmount());
        eleRefundOrder.setCreateTime(System.currentTimeMillis());
        eleRefundOrder.setUpdateTime(System.currentTimeMillis());
        eleRefundOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        eleRefundOrderMapper.insert(eleRefundOrder);

        //退款
        RefundQuery refundQuery = new RefundQuery();
        refundQuery.setAppId(electricityPayParams.getAppId());
        refundQuery.setAppSecret(electricityPayParams.getAppSecret());
        refundQuery.setMchId(electricityPayParams.getMchId());
        refundQuery.setPaternerKey(electricityPayParams.getPaternerKey());
        refundQuery.setBody("换电押金退款:" + refundOrder.getRefundOrderNo());
        refundQuery.setOutTradeNo(refundOrder.getOrderId());
        refundQuery.setRefundOrderNo(refundOrder.getRefundOrderNo());
        refundQuery.setTotalFee(refundOrder.getPayAmount().multiply(new BigDecimal(100)).longValue());
        refundQuery.setRefundFee(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).longValue());
        refundQuery.setApiName(electricityPayParams.getApiName());
        //订单有效期为三分钟
        refundQuery.setAttach(refundOrder.getAttach());
        return refundAdapterHandler.refund(refundQuery);
    }

    @Override
    public Pair<Boolean, Object> notifyDepositRefundOrder(WeiXinRefundNotify weiXinRefundNotify) {
        //退款订单
        String tradeRefundNo = weiXinRefundNotify.getOutRefundNo();

        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo,tradeRefundNo));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", eleRefundOrder);
            return Pair.of(false, "未找到退款订单!");
        }
        if (ObjectUtil.notEqual(EleRefundOrder.STATUS_INIT, eleRefundOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeRefundNo);
            return Pair.of(false, "退款订单已处理");
        }

        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByTradeOrderNo(weiXinRefundNotify.getOutTradeNo());
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", weiXinRefundNotify.getOutTradeNo());
            return Pair.of(false, "未找到交易订单!");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        Integer refundOrderStatus = EleRefundOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(weiXinRefundNotify.getRefundStatus()) && ObjectUtil.equal("SUCCESS", weiXinRefundNotify.getRefundStatus())) {
            refundOrderStatus = EleRefundOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + weiXinRefundNotify.getOutRefundNo());
        }

        UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(),  weiXinRefundNotify.getOutRefundNo());
            return Pair.of(false, "未找到用户信息!");
        }

        if(Objects.equals(refundOrderStatus,EleDepositOrder.STATUS_SUCCESS)) {
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_AUTH);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setBatteryDeposit(null);
            userInfoUpdate.setOrderId(null);
            userInfoService.updateById(userInfoUpdate);
        }

        EleRefundOrder  eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setStatus(refundOrderStatus);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderMapper.updateById(eleRefundOrderUpdate);
        return Pair.of(result, null);
    }

}