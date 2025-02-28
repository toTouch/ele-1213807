package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.merchant.MerchantOverdueUserCountBO;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.query.merchant.MerchantAllPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionScanCodeQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantJoinRecordPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantJoinScanRequest;
import com.xiliulou.electricity.request.merchant.MerchantScanCodeRecordPageRequest;
import com.xiliulou.electricity.vo.merchant.MerchantJoinRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantJoinUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantStatisticsUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantScanCodeRecordVO;

import java.util.List;
import java.util.Set;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 17:53:17
 */
public interface MerchantJoinRecordService {
    
    /**
     * 扫码参与
     */
    R joinScanCode(MerchantJoinScanRequest request);
    
    Integer insertOne(MerchantJoinRecord record);
    
    /**
     * 根据参与人uid查询记录
     */
    MerchantJoinRecord queryByJoinUid(Long joinUid);
    
    /**
     * 修改参与状态
     */
    Integer updateStatus(MerchantJoinRecordQueryModel queryModel);
    
    /**
     * 根据商户id和参与人uid查询记录
     */
    MerchantJoinRecord queryByMerchantIdAndJoinUid(Long merchantId, Long joinUid);
    
    /**
     * 根据商户id和参与状态统计人数
     */
    Integer countByMerchantIdAndStatus(Long merchantId, Integer status);
    
    /**
     * 定时任务：保护期状态
     */
    void handelProtectionStatus();
    
    /**
     * 定时任务：有效期状态
     */
    void handelExpiredStatus();
    
    Integer updateById(MerchantJoinRecord record);
    
    List<MerchantJoinRecord> queryList(MerchantJoinRecordQueryMode joinRecordQueryMode);
    
    Integer countTotal(MerchantJoinRecordPageRequest merchantJoinRecordPageRequest);
    
    List<MerchantJoinRecordVO> listByPage(MerchantJoinRecordPageRequest merchantJoinRecordPageRequest);
    
    Integer countByCondition(MerchantPromotionScanCodeQueryModel queryModel);
    
    List<MerchantJoinRecordVO> countByMerchantIdList(MerchantJoinRecordQueryMode joinRecordQueryMode);
    
    List<MerchantJoinRecord> selectPromotionDataDetail(MerchantPromotionDataDetailQueryModel queryModel);
    
    /**
     * 商户端，用户管理，会员套餐详情信息查询
     *
     * @param merchantJoinUserQueryMode
     * @return
     */
    List<MerchantJoinUserVO> selectJoinUserList(MerchantJoinUserQueryMode merchantJoinUserQueryMode);
    
    /**
     * 是否存在邀请数据（仅商户）
     *
     * @param inviterType 邀请人类型
     * @param inviterUid  邀请人uid
     * @param tenantId    租户id
     * @return 是否存在邀请数据
     */
    boolean existMerchantInviterData(Integer inviterType, Long inviterUid, Integer tenantId);
    
    /**
     * 是否存在邀请数据
     *
     * @param merchantId  商户id
     * @param tenantId    租户id
     * @return 是否存在邀请数据
     */
    boolean existMerchantAllInviterData(Long merchantId, Integer tenantId);
    
    Integer countEmployeeScanCodeNum(List<Long> uidList, Long startTime, Long endTime, Integer status, Integer tenantId,Long channelEmployeeUid);
    
    List<MerchantJoinRecord> selectListAllPromotionDataDetail(MerchantAllPromotionDataDetailQueryModel queryModel);
    
    List<MerchantJoinRecord> listByJoinUidAndStatus(Long joinUid, List<Integer> statusList);
    
    Integer updateStatusById(Long id, Integer status, long updateTime);
    
    Integer countSuccessByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    MerchantJoinRecord querySuccessRecordByJoinUid(Long uid, Integer tenantId);
    
    Integer removeByJoinUid(Long joinUid, Long updateTime, Integer tenantId);
    
    Integer countEmployeeScanCodeSuccessNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid);
    
    R canJoinActivity(UserInfo userInfo, UserInfoExtra userInfoExtra, Integer shareActivityId, Integer shareActivityType);
    
    List<MerchantStatisticsUserVO> listSuccessJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    List<MerchantStatisticsUserVO> listEmployeeSuccessJoinNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid);
    
    List<MerchantStatisticsUserVO> listJoinNumByCondition(MerchantPromotionScanCodeQueryModel scanCodeQueryModel);
    
    List<MerchantStatisticsUserVO> listEmployeeJoinNum(List<Long> employeeIds, Long startTime, Long endTime, Integer status, Integer tenantId, Long uid);
    
    List<MerchantScanCodeRecordVO> listScanCodeRecordPage(MerchantScanCodeRecordPageRequest merchantScanCodeRecordPageRequest);
    
    Integer countScanCodeRecord(MerchantScanCodeRecordPageRequest merchantScanCodeRecordPageRequest);
    
    MerchantJoinRecord queryNotSuccessByJoinUid(Long uid);
    
    
    MerchantJoinRecord queryRemoveSuccessRecord(Long joinUid, Long inviterUid, Integer tenantId);

    List<MerchantOverdueUserCountBO> listOverdueUserCount(Set<Long> merchantIdList, long currentTime);
}
