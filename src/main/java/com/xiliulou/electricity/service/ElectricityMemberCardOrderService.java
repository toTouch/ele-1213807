package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface ElectricityMemberCardOrderService {

    /**
     * 根据用户ID查询对应状态的记录
     * @param tenantId
     * @param uid
     * @param status
     * @return
     */
    Integer selectCountByUid(Integer tenantId, Long uid, Integer status);

    R createOrder(ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request);

    Triple<Boolean, String, Object> buyBatteryMemberCard(ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request);

    List<ElectricityMemberCardOrder> selectUserMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery);
    
    Integer selectUserMemberCardOrderCount(ElectricityMemberCardOrderQuery orderQuery);

    BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList, Integer tenantId);

    List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList, Integer tenantId);

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

    Long calcRentCarMemberCardExpireTime(String rentType, Integer rentTime, UserCarMemberCard userCarMemberCard);

    ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId);

    ElectricityMemberCardOrder queryLastPayMemberCardTimeByUidAndSuccess(Long uid, Long franchiseeId, Integer tenantId);

    BigDecimal queryBatteryMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId);

    BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId);

    List<HomePageTurnOverGroupByWeekDayVo> queryBatteryMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);

    List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeIds, Long beginTime, Long endTime);

    BigDecimal querySumMemberCardTurnOver(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);

    void batteryMemberCardExpireReminder();

    void systemEnableMemberCardTask();

    BigDecimal checkDifferentModelBatteryServiceFee(Franchisee franchisee, UserInfo userInfo, UserBattery userBattery);

    @Deprecated
    BigDecimal checkUserDisableCardBatteryService(UserInfo userInfo, Long uid, Long cardDays, EleDisableMemberCardRecord eleDisableMemberCardRecord, ServiceFeeUserInfo serviceFeeUserInfo);

    @Deprecated
    BigDecimal checkUserMemberCardExpireBatteryService(UserInfo userInfo, Franchisee franchisee, Long cardDays);

    int insert(ElectricityMemberCardOrder electricityMemberCardOrder);

    int updateByID(ElectricityMemberCardOrder electricityMemberCardOrder);

    ElectricityMemberCardOrder selectByOrderNo(String orderNo);

    R queryUserExistMemberCard();

    Triple<Boolean, String, Object> handleRentBatteryMemberCard(String productKey, String deviceName, Set<Integer> userCouponIds, Integer memberCardId, Long franchiseeId, UserInfo userInfo);

    R cancelPayMemberCard();

    Pair<Boolean, Object> checkUserHaveBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard);
    
    Integer queryMaxPayCount(UserBatteryMemberCard userBatteryMemberCard);

    ElectricityMemberCardOrder selectFirstMemberCardOrder(Long uid);
    
    ElectricityMemberCardOrder selectLatestByUid(Long uid);
    
    Triple<Boolean, String, Object> endOrder(String orderNo, Long uid);
    
    R disableMemberCardForRollback();

//    Long handlerMembercardBindActivity(ElectricityMemberCard electricityMemberCard, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, Long remainingNumber);

    Set<Integer> generateUserCouponIds(Integer userCouponId, List<Integer> userCouponIds);

    List<UserCoupon> buildUserCouponList(Set<Integer> userCouponIds, Integer status, String orderId);

    List<BatteryMemberCardOrderCoupon> buildMemberCardOrderCoupon(String orderId, Set<Integer> couponSet);

    Triple<Boolean, String, Object> calculatePayAmount(BigDecimal price, Set<Integer> userCouponIds);

    Integer checkOrderByMembercardId(Long membercardId);

    void handlerBatteryMembercardPaymentNotify(BatteryMemberCard batteryMemberCard,ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo);

    Integer updateStatusByOrderNo(ElectricityMemberCardOrder oldMemberCardOrder);

    Triple<Boolean, String, Object> addUserDepositAndMemberCard(UserBatteryDepositAndMembercardQuery query);

    Triple<Boolean, String, Object> editUserBatteryMemberCard(UserBatteryMembercardQuery query);

    Triple<Boolean, String, Object> renewalUserBatteryMemberCard(UserBatteryMembercardQuery query);

    Triple<Boolean, String, Object> userBatteryMembercardInfo(Long uid);

    Triple<Boolean, String, Object> userBatteryDepositAndMembercardInfo();

    List<ElectricityMemberCardOrderVO> selectElectricityMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery);
}
