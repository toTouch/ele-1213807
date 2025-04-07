package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.task.BatteryMemberCardExpireReminderTask;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import org.apache.commons.lang3.tuple.Triple;

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

    List<ElectricityMemberCardOrder> selectUserMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery);
    
    Integer selectUserMemberCardOrderCount(ElectricityMemberCardOrderQuery orderQuery);

    BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList, Integer tenantId);

    List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList, Integer tenantId);

    R queryList(MemberCardOrderQuery memberCardOrderQuery);

    R queryCount(MemberCardOrderQuery memberCardOrderQuery);

    Integer queryCountForScreenStatistic(MemberCardOrderQuery memberCardOrderQuery);

    BigDecimal queryTurnOver(Integer tenantId, Long uid);

    R disableMemberCardForLimitTime(Integer disableCardDays, Long disableDeadline ,String applyReason);

    R enableMemberCardForLimitTime();

    R enableOrDisableMemberCardIsLimitTime();

    R adminDisableMemberCard(Long uid, Integer days);

    R adminEnableMemberCard(Long uid);

    R cleanBatteryServiceFee(Long uid);

    R getDisableMemberCardList(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

    ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId);
    
    void batteryMemberCardExpireReminder(BatteryMemberCardExpireReminderTask.TaskParam param);

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
    
    Integer queryMaxPayCount(UserBatteryMemberCard userBatteryMemberCard);

    ElectricityMemberCardOrder selectFirstMemberCardOrder(Long uid);
    
    ElectricityMemberCardOrder selectLatestByUid(Long uid);
    
    Triple<Boolean, String, Object> endOrder(String orderNo, Long uid);
    
    R disableMemberCardForRollback();
    
    Set<Integer> generateUserCouponIds(Integer userCouponId, List<Integer> userCouponIds);

    List<UserCoupon> buildUserCouponList(Set<Integer> userCouponIds, Integer status, String orderId);

    List<BatteryMemberCardOrderCoupon> buildMemberCardOrderCoupon(String orderId, Set<Integer> couponSet);

    Triple<Boolean, String, Object> calculatePayAmount(BigDecimal price, Set<Integer> userCouponIds, Long franchiseeId);
    
    Triple<Boolean, String, Object> calculatePayAmount(BatteryMemberCard batteryMemberCard, Set<Integer> userCouponIds);

    Integer checkOrderByMembercardId(Long membercardId);

    void handlerBatteryMembercardPaymentNotify(BatteryMemberCard batteryMemberCard,ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo);

    Integer updateStatusByOrderNo(ElectricityMemberCardOrder oldMemberCardOrder);

    Integer batchUpdateStatusByOrderNo(List<String> orderIds,Integer useStatus);

    Triple<Boolean, String, Object> addUserDepositAndMemberCard(UserBatteryDepositAndMembercardQuery query);

    Triple<Boolean, String, Object> editUserBatteryMemberCard(UserBatteryMembercardQuery query);

    Triple<Boolean, String, Object> renewalUserBatteryMemberCard(UserBatteryMembercardQuery query);

    Triple<Boolean, String, Object> userBatteryMembercardInfo(Long uid);

    Triple<Boolean, String, Object> userBatteryDepositAndMembercardInfo();

    List<ElectricityMemberCardOrderVO> selectElectricityMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery);

    void sendUserCoupon(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder);
    
    Integer batchUpdateChannelOrderStatusByOrderNo(List<String> selectUserBatteryMemberCardOrder, Integer useStatusExpire);
    
    
    /**
     * 退租订单数量
     * @param uid
     * @return
     */
    Integer countRefundOrderByUid(Long uid);
    
    /**
     * 购买成功订单数量
     */
    Integer countSuccessOrderByUid(Long uid);
    
    List<ElectricityMemberCardOrder> queryListByOrderIds(List<String> orderIdList);
    
    R listSuperAdminPage(MemberCardOrderQuery memberCardOrderQuery);
    
    List<ElectricityMemberCardOrder> queryListByCreateTime(Long buyStartTime, Long buyEndTime);
    
    boolean existNotFinishOrderByUid(Long uid);
    
    Integer deleteById(Long id);
    
    /**
     * 生成分期换电套餐订单
     */
    Triple<Boolean, String, ElectricityMemberCardOrder> generateInstallmentMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityCabinet electricityCabinet,
            InstallmentRecord installmentRecord, List<InstallmentDeductionPlan> deductionPlans);
    
    /**
     * 根据请求签约号及期次查询对应的套餐订单
     * @param externalAgreementNo 请求签约号
     * @param issue 期次
     * @return 返回套餐订单
     */
    ElectricityMemberCardOrder queryOrderByAgreementNoAndIssue(String externalAgreementNo, Integer issue);
    
    /**
     * 后台续费套餐、分期套餐续费
     */
    ElectricityMemberCardOrder saveRenewalUserBatteryMemberCardOrder(User user, UserInfo userInfo, BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard,
            BatteryMemberCard userBindbatteryMemberCard, InstallmentRecord installmentRecord, Integer source, List<InstallmentDeductionPlan> deductionPlans,  Integer type);
    
    /**
     * 查询分期套餐子套餐订单
     * @param externalAgreementNo 请求签约号
     * @return 套餐订单
     */
    List<ElectricityMemberCardOrder> listOrderByExternalAgreementNo(String externalAgreementNo);

    void updatePayChannelById(ElectricityMemberCardOrder memberCardOrder);

    ElectricityMemberCardOrder queryUserLastPaySuccessByUid(Long uid);

}
