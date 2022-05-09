package com.xiliulou.electricity.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2022-04-20 10:21:24
 */
@Service("eleBatteryServiceFeeOrderService")
@Slf4j
public class EleBatteryServiceFeeOrderServiceImpl implements EleBatteryServiceFeeOrderService {

    @Resource
    EleBatteryServiceFeeOrderMapper eleBatteryServiceFeeOrderMapper;
    @Autowired
    ElectricityBatteryService electricityBatteryService;


    @Override
    public EleBatteryServiceFeeOrder queryEleBatteryServiceFeeOrderByOrderId(String orderNo) {
        return eleBatteryServiceFeeOrderMapper.selectOne(new LambdaQueryWrapper<EleBatteryServiceFeeOrder>().eq(EleBatteryServiceFeeOrder::getOrderId, orderNo));
    }

    @Override
    public void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        eleBatteryServiceFeeOrderMapper.updateById(eleBatteryServiceFeeOrder);
    }

    @Override
    public R queryList(Long offset, Long size, Long startTime, Long endTime) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return this.queryListForAdmin(offset, size, startTime, endTime, user.getUid(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS);
    }

    @Override
    public R queryListForAdmin(Long offset, Long size, Long startTime, Long endTime, Long uid, Integer status) {

        List<EleBatteryServiceFeeOrderVo> eleBatteryServiceFeeOrders = eleBatteryServiceFeeOrderMapper.queryListForAdmin(uid, offset, size, startTime, endTime, status);

        for (EleBatteryServiceFeeOrderVo eleBatteryServiceFeeOrderVo : eleBatteryServiceFeeOrders) {
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
                Integer model = BatteryConstant.acquireBattery(eleBatteryServiceFeeOrderVo.getBatteryType());
                eleBatteryServiceFeeOrderVo.setModel(model);
            }
        }
        return R.ok(eleBatteryServiceFeeOrders);
    }
}
