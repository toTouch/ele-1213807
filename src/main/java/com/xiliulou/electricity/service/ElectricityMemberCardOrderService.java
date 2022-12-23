package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderService {


    R createOrder(ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request);

    R queryUserList(Long offset, Long size, Long startTime, Long endTime);

    BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList, Integer tenantId);

    List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList, Integer tenantId);

    R getMemberCardOrderCount(Long uid, Long startTime, Long endTime);


    R queryList(MemberCardOrderQuery memberCardOrderQuery);

    void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response);

    R queryCount(MemberCardOrderQuery memberCardOrderQuery);

    Integer queryCountForScreenStatistic(MemberCardOrderQuery memberCardOrderQuery);

    BigDecimal queryTurnOver(Integer tenantId, Long uid);

    R openOrDisableMemberCard(Integer usableStatus);

    R disableMemberCardForLimitTime(Integer disableCardDays, Long disableDeadline);

    R enableMemberCardForLimitTime();

    R enableOrDisableMemberCardIsLimitTime();

    R adminOpenOrDisableMemberCard(Integer usableStatus, Long uid);

    R cleanBatteryServiceFee(Long uid);

    R getDisableMemberCardList(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

    R addUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate);

    R editUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate);

    R renewalUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate);

    R payRentCarMemberCard(CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request);

    Long calcRentCarMemberCardExpireTime(String rentType, Integer rentTime, UserCarMemberCard userCarMemberCard);

    ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId);

    BigDecimal queryBatteryMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId);

    BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId);

    List<HomePageTurnOverGroupByWeekDayVo> queryBatteryMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);

    List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeIds, Long beginTime, Long endTime);

    BigDecimal querySumMemberCardTurnOver(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);

    void batteryMemberCardExpireReminder();

    void carMemberCardExpireReminder();

    void systemEnableMemberCardTask();

    void expireReminderHandler();

    BigDecimal checkDifferentModelBatteryServiceFee(Franchisee franchisee, UserInfo userInfo, UserBattery userBattery);

    BigDecimal checkUserDisableCardBatteryService(UserInfo userInfo, Long uid, Long cardDays, EleDisableMemberCardRecord eleDisableMemberCardRecord, ServiceFeeUserInfo serviceFeeUserInfo);

    BigDecimal checkUserMemberCardExpireBatteryService(UserInfo userInfo, Franchisee franchisee, Long cardDays);

    int insert(ElectricityMemberCardOrder electricityMemberCardOrder);

    int updateByID(ElectricityMemberCardOrder electricityMemberCardOrder);

    ElectricityMemberCardOrder selectByOrderNo(String orderNo);

    R queryUserExistMemberCard();

    Triple<Boolean, String, Object> handleRentBatteryMemberCard(RentCarHybridOrderQuery query, UserInfo userInfo);

}
