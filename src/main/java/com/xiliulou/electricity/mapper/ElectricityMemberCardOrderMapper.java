package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderMapper extends BaseMapper<ElectricityMemberCardOrder> {

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE order_id = #{orderId}")
    ElectricityMemberCardOrder selectByOrderNo(@Param("orderId") String orderNo);

    @Deprecated
    List<ElectricityMemberCardOrder> queryUserList(@Param("uid") Long uid,
                                                   @Param("offset") Long offset, @Param("size") Long size, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
    
    List<ElectricityMemberCardOrder> selectUserMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery);
    
    Integer selectUserMemberCardOrderCount(ElectricityMemberCardOrderQuery orderQuery);
    
    BigDecimal homeOne(@Param("first") Long first, @Param("now") Long now, @Param("cardIdList") List<Integer> cardIdList, @Param("tenantId") Integer tenantId);


    List<HashMap<String, String>> homeTwo(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay, @Param("cardIdList") List<Integer> cardIdList, @Param("tenantId") Integer tenantId);

    List<ElectricityMemberCardOrderVO> queryList(@Param("query") MemberCardOrderQuery memberCardOrderQuery);
    
    @Deprecated
    Long getMemberCardOrderCount(@Param("uid") Long uid, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    Integer queryCount(@Param("query") MemberCardOrderQuery memberCardOrderQuery);

    BigDecimal queryTurnOver(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(@Param("uid") Long uid, @Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    ElectricityMemberCardOrder selectLatestByUid(@Param("uid") Long uid);
    
    ElectricityMemberCardOrder queryCreateTimeMaxMemberCardOrder(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    Integer queryMaxPayCountByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    ElectricityMemberCardOrder selectFirstMemberCardOrder(@Param("uid") Long uid );

    Integer checkOrderByMembercardId(@Param("membercardId") Long membercardId);

    Integer updateStatusByOrderNo(ElectricityMemberCardOrder memberCardOrder);

    Integer batchUpdateStatusByOrderNo(@Param("orderIds") List<String> orderIds, @Param("useStatus") Integer useStatus);
    
    Integer batchUpdateChannelOrderStatusByOrderNo(@Param("orderIds") List<String> orderIds,@Param("useStatus") Integer useStatus);
    
    Integer countRefundOrderByUid(Long uid);
    
    Integer countSuccessOrderByUid(Long uid);
    
    List<ElectricityMemberCardOrder> selectListByOrderIds(@Param("orderIdList") List<String> orderIdList);
    
    List<ElectricityMemberCardOrderVO> selectListSuperAdminPage(@Param("query") MemberCardOrderQuery memberCardOrderQuery);
    
    List<ElectricityMemberCardOrder> selectListByCreateTime(@Param("beginTime") Long buyStartTime,@Param("endTime")  Long buyEndTime);
    
    ElectricityMemberCardOrder selectOrderByAgreementNoAndIssue(@Param("externalAgreementNo") String externalAgreementNo, @Param("issue") Integer issue);
    
    List<ElectricityMemberCardOrder> selectListOrderByExternalAgreementNo(@Param("externalAgreementNo") String externalAgreementNo);
    
    List<ElectricityMemberCardOrder> selectListByUidAndUseStatus(@Param("uidList") List<Long> uidList, @Param("tenantId") Integer tenantId, @Param("useStatus") Integer useStatus,
            @Param("payTypeList") List<Integer> payTypeList);
}
