package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    /**
     * 创建月卡订单
     *
     * @param uid
     * @param memberId
     * @return
     */
    @Override
    public R createOrder(Long uid, Integer memberId, HttpServletRequest request) {
        ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS:{}");
            return R.failMsg("未配置支付参数!");
        }

        // TODO: 2020/12/3 0003  获取用户信息
        //获取openId
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.getElectricityMemberCard(memberId);
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", memberId);
            return R.failMsg("未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", memberId);
            return R.failMsg("月卡已禁用!");
        }
        // TODO: 2020/12/3 0003  用户是否是月卡用户 是否可以继续购买月卡  次数用完可继续购买

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderNo(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(memberId);
        electricityMemberCardOrder.setUid(1L);
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getPrice());
        electricityMemberCardOrder.setUserName("YG");
        electricityMemberCardOrder.setValidDays(31);
        baseMapper.insert(electricityMemberCardOrder);
        Pair<Boolean, Object> getPayParamsPair =
                electricityTradeOrderService.createTradeOrderAndGetPayParams(electricityMemberCardOrder, electricityPayParams, "openId", request);
        if (!getPayParamsPair.getLeft()) {
            return R.failMsg(getPayParamsPair.getRight().toString());
        }
        return R.ok(getPayParamsPair.getRight());
    }


}