package com.xiliulou.electricity.service.exchange.fail;


import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ExchangeRemarkConstant;
import com.xiliulou.electricity.constant.LessScanConstant;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.CellTypeEnum;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
import com.xiliulou.electricity.enums.OrderCheckEnum;
import com.xiliulou.electricity.query.OrderQueryV3;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @Description: OldCellOpenFailOrderHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Service("oldCellOpenFailOrderHandler")
@Slf4j
public class OldCellOpenFailOrderHandler extends AbstractFailOrderHandler {

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    @Resource
    private UserBatteryTypeService userBatteryTypeService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private ElectricityCabinetOrderService orderService;


    @Override
    Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ElectricityCabinet cabinet, UserInfo userInfo, Integer code, Integer secondFlexibleRenewal, ExchangeUserSelectVO vo) {

        vo.setOrderId(lastOrder.getOrderId());
        vo.setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN!oldCellCheckFail.userBindingBatterySn  is null, uid is {}", lastOrder.getUid());
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }

        String userBindingBatterySn = electricityBattery.getSn();
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());

        log.info("OrderV3 INFO! oldCellCheckFail.cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));

        // 判断灵活续费场景下，二次扫码是走去电流程还是自主开仓，true为取电，false为自主开仓
        List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        Integer flexibleRenewalEnumCode = checkExchangeOrSelfOpen(userBatteryTypes, electricityBattery, electricityConfig);

        // 租借在仓（上一个订单旧仓门内），仓门锁状态：关闭
        if (Objects.nonNull(cabinetBox) && Objects.equals(cabinetBox.getIsLock(), ElectricityCabinetBox.CLOSE_DOOR) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(
                Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getOldCellNo())) {

            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL);
            vo.setIsEnterTakeBattery(LessScanConstant.ENTER_TAKE_BATTERY);
            vo.setCellType(CellTypeEnum.OLD_CELL.getCode());

            // 传递secondFlexibleRenewal时为第二次调用V3多次扫码，直接换电不作处理；校验灵活续费flexibleRenewalEnumCode 的值为 NORMAL 直接换电
            if (!Objects.equals(secondFlexibleRenewal, OrderQueryV3.SECOND_FLEXIBLE_RENEWAL) && !Objects.equals(flexibleRenewalEnumCode, FlexibleRenewalEnum.NORMAL.getCode())) {
                // 灵活续费为先退后租时自主开仓
                if (Objects.equals(flexibleRenewalEnumCode, FlexibleRenewalEnum.RETURN_BEFORE_RENT.getCode())) {
                    BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(lastOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                            .cell(lastOrder.getOldCellNo()).orderId(lastOrder.getOrderId()).cabinet(cabinet).msg(ExchangeRemarkConstant.FLEXIBLE_RENEWAL_SYSTEM_SELF_CELL)
                            .tenantId(lastOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode()).build();
                    backSelfOpen(openCellDTO);
                    vo.setBeginSelfOpen(ExchangeUserSelectVO.BEGIN_SELF_OPEN);
                    vo.setCell(lastOrder.getOldCellNo());
                } else {
                    orderService.checkFlexibleRenewal(vo, electricityBattery, userInfo);
                }

                // 灵活续费为换电时，不开仓不分配电池，返回给前端等待第二次调用V3多次扫码
                return Pair.of(true, vo);
            }

            // 这里为了兼容选仓换电：只有换电，才去获取满电仓，而选仓取电不走这里
            if (Objects.equals(code, OrderCheckEnum.ORDER.getCode())) {
                // 获取满电仓
                Integer cellNo = this.getFullCellHandler(cabinet, userInfo);
                vo.setCell(cellNo);
                String sessionId = this.openFullBatteryCellHandler(lastOrder, cabinet, cellNo, userBindingBatterySn, cabinetBox.getCellNo());
                vo.setSessionId(sessionId);
            }

            return Pair.of(true, vo);
        } else {
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }
    }
}
