package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderService {


    EleDepositOrder queryByOrderId(String orderNo);

    R payDeposit(String productKey, String deviceName, Long franchiseeId, Integer model, HttpServletRequest request);

    R returnDeposit(HttpServletRequest request);

    R queryList(EleDepositOrderQuery eleDepositOrderQuery);

    R queryListToUser(EleDepositOrderQuery eleDepositOrderQuery);

    R payDepositOrderList(EleDepositOrderQuery eleDepositOrderQuery);

    void update(EleDepositOrder eleDepositOrderUpdate);

    R queryUserDeposit();

    void exportExcel(EleDepositOrderQuery eleDepositOrderQuery, HttpServletResponse response);

    R queryFranchiseeDeposit(String productKey, String deviceName, Long franchiseeId);

    R queryDeposit(String productKey, String deviceName, Long franchiseeId);

    R queryCount(EleDepositOrderQuery eleDepositOrderQuery);

    void insert(EleDepositOrder eleDepositOrder);

    R queryModelType(String productKey, String deviceName);

    BigDecimal queryTurnOver(Integer tenantId);

    R payBatteryServiceFee(HttpServletRequest request);

    R adminPayRentCarDeposit(RentCarDepositAdd rentCarDepositAdd);

    @Deprecated
    R payRentCarDeposit(Long storeId, Integer carModelId, HttpServletRequest request);

    R refundRentCarDeposit(HttpServletRequest request);

    R queryRentCarDeposit();

    EleDepositOrder queryLastPayDepositTimeByUid(Long uid, Long franchiseeId, Integer tenantId,Integer depositType);

    EleDepositOrder selectLatestByUid(Long uid);

    R adminPayBatteryDeposit(BatteryDepositAdd batteryDepositAdd);

    BigDecimal queryDepositTurnOverByDepositType(Integer tenantId, Long todayStartTime, Integer depositType, List<Long> franchiseeIds);

    List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(Integer tenantId, Integer depositType, List<Long> franchiseeId, Long beginTime, Long enTime);

    BigDecimal querySumDepositTurnOverAnalysis(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long enTime);

    Triple<Boolean, String, Object> handleRentBatteryDeposit(Long franchiseeId, Integer memberCardId,Integer model, UserInfo userInfo);
    
    R adminPayCarDeposit(RentCarDepositQuery rentCarDepositQuery);
    
    //R adminPayCarDeposit(RentCarDepositAdd rentCarDepositAdd);
    
    R refundCarDeposit();
}
