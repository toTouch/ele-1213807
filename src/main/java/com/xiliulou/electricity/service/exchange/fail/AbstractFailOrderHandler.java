package com.xiliulou.electricity.service.exchange.fail;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.ExchangeRemarkConstant;
import com.xiliulou.electricity.constant.LessScanConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.exchange.AbstractOrderHandler;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @Description: AbstractFailOrderHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Slf4j
public abstract class AbstractFailOrderHandler extends AbstractOrderHandler implements OrderStatusStrategy {


    @Override
    public Pair<Boolean, ExchangeUserSelectVO> process(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, Integer code, Integer secondFlexibleRenewal) {
        return lastExchangeFailHandler(lastOrder, electricityBattery, cabinet, userInfo, code, secondFlexibleRenewal);
    }


    /**
     * @param lastOrder             lastOrder
     * @param electricityBattery    electricityBattery
     * @param cabinet               cabinet
     * @param userInfo              userInfo
     * @param code                  code
     * @param secondFlexibleRenewal secondFlexibleRenewal
     * @return: Pair
     */

    abstract Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ElectricityCabinet cabinet,
                                                                         UserInfo userInfo, Integer code, Integer secondFlexibleRenewal);


}
