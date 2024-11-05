package com.xiliulou.electricity.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.query.merchant.MerchantAllPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantScanCodeRecordPageRequest;
import com.xiliulou.electricity.vo.merchant.MerchantJoinRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantJoinUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 18:25:02
 */
public interface MerchantJoinRecordMapper extends BaseMapper<MerchantJoinRecord> {
    
    Integer insertOne(MerchantJoinRecord record);
    
    MerchantJoinRecord selectByMerchantIdAndJoinUid(@Param("merchantId") Long merchantId, @Param("joinUid") Long joinUid);
    
    Integer updateStatus(MerchantJoinRecordQueryModel queryModel);
    
    Integer updateProtectionExpired(MerchantJoinRecord protectionJoinRecord);
    
    Integer updateExpired(MerchantJoinRecord merchantJoinRecord);
    
    List<MerchantJoinRecord> selectList(MerchantJoinRecordQueryMode joinRecordQueryMode);
    
    Integer countListByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    Integer countTotal(MerchantJoinRecordQueryMode queryMode);
    
    List<MerchantJoinRecord> selectListByPage(MerchantJoinRecordQueryMode queryMode);
    
    MerchantJoinRecord selectByJoinUid(Long joinUid);
    
    Integer countByCondition(MerchantPromotionScanCodeQueryModel queryModel);
    
    List<MerchantJoinRecordVO> countByMerchantIdList(MerchantJoinRecordQueryMode joinRecordQueryMode);
    
    List<MerchantJoinRecord> selectListPromotionDataDetail(MerchantPromotionDataDetailQueryModel queryModel);
    
    List<MerchantJoinUserVO> selectJoinUserList(MerchantJoinUserQueryMode merchantJoinUserQueryMode);
    
    Integer existMerchantInviterData(@Param("inviterType") Integer inviterType, @Param("inviterUid") Long inviterUid, @Param("tenantId") Integer tenantId);
    
    Integer countEmployeeScanCodeNum(@Param("uidList") List<Long> employeeIdList, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("status") Integer status, @Param("tenantId") Integer tenantId, @Param("channelEmployeeUid") Long channelEmployeeUid);
    
    List<MerchantJoinRecord> selectListAllPromotionDataDetail(MerchantAllPromotionDataDetailQueryModel query);
    
    List<MerchantJoinRecord> selectListByJoinUidAndStatus(@Param("joinUid") Long joinUid, @Param("statusList") List<Integer> statusList);
    
    Integer updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") long updateTime);
    
    Integer existMerchantAllInviterData(@Param("merchantId")Long merchantId, @Param("tenantId")Integer tenantId);
    
    Integer countSuccessByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    MerchantJoinRecord selectSuccessRecordByJoinUid(@Param("joinUid") Long uid, @Param("tenantId") Integer tenantId);
    
    Integer removeByJoinUid(@Param("joinUid") Long joinUid, @Param("updateTime") Long updateTime, @Param("tenantId") Integer tenantId);
    
    Integer countEmployeeScanCodeSuccessNum(@Param("uidList") List<Long> employeeIdList, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("status") Integer status, @Param("tenantId") Integer tenantId, @Param("channelEmployeeUid") Long channelEmployeeUid);
    
    List<MerchantJoinRecord> selectListScanCodeRecordPage(MerchantScanCodeRecordPageRequest request);
    
    Integer countScanCodeRecord(MerchantScanCodeRecordPageRequest request);
    
    MerchantJoinRecord selectRemoveSuccessRecord(@Param("joinUid") Long joinUid, @Param("inviterUid") Long inviterUid, @Param("tenantId") Integer tenantId);
    
    List<MerchantStatisticsUserVO> selectListSuccessJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    List<MerchantStatisticsUserVO> selectListEmployeeSuccessJoinNum(@Param("uidList") List<Long> employeeIdList, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("status") Integer status, @Param("tenantId") Integer tenantId, @Param("channelEmployeeUid") Long channelEmployeeUid);
    
    List<MerchantStatisticsUserVO> selectListJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    List<MerchantStatisticsUserVO> selectListEmployeeJoinNum(@Param("uidList") List<Long> employeeIdList, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("status") Integer status, @Param("tenantId") Integer tenantId, @Param("channelEmployeeUid") Long channelEmployeeUid);
}
