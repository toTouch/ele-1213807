package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.vo.FinalJoinInvitationActivityHistoryVO;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityAnalysisAdminVO;

import java.util.List;

/**
 * (InvitationActivityJoinHistory)表服务接口
 *
 * @author zzlong
 * @since 2023-06-06 09:51:43
 */
public interface InvitationActivityJoinHistoryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityJoinHistory queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityJoinHistory queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityJoinHistory> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    InvitationActivityJoinHistory insert(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 修改数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivityJoinHistory invitationActivityJoinHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    InvitationActivityJoinHistory selectByActivityAndInvitationUid(Long id, Long invitationUid, Long uid);

    InvitationActivityJoinHistory selectByActivityAndUid(Long id, Long uid);

    List<InvitationActivityJoinHistoryVO> selectByPage(InvitationActivityJoinHistoryQuery query);

    Integer selectByPageCount(InvitationActivityJoinHistoryQuery query);

    Integer updateStatusByActivityId(Long activityId, Integer status);

    InvitationActivityJoinHistory selectByJoinIdAndStatus(Long uid, Integer statusInit);

    InvitationActivityJoinHistory selectByJoinUid(Long uid);

    List<InvitationActivityJoinHistoryVO> selectUserByPage(InvitationActivityJoinHistoryQuery query);

    void handelActivityJoinHistoryExpired();
    
    Integer existsByJoinUidAndActivityId(Long joinUid, Long activityId);
    
    List<InvitationActivityJoinHistory> listByJoinUid(Long uid);
    
    List<InvitationActivityJoinHistoryVO> listByInviterUidOfAdmin(InvitationActivityJoinHistoryQuery query);
    
    List<InvitationActivityJoinHistoryVO> listByInviterUid(InvitationActivityJoinHistoryQuery query);
    
    List<InvitationActivityJoinHistoryVO> listByInviterUidDistinctJoin(InvitationActivityJoinHistoryQuery query);
    
    InvitationActivityAnalysisAdminVO queryInvitationAdminAnalysis(InvitationActivityAnalysisRequest request);
    
    /**
     * 根据活动id和参与人uid查询对应的参与记录
     */
    InvitationActivityJoinHistory queryByJoinUidAndActivityId(Long joinUid, Long activityId);
    
    FinalJoinInvitationActivityHistoryVO queryFinalHistoryByJoinUid(Long uid, Integer tenantId);
    
    InvitationActivityJoinHistory querySuccessHistoryByJoinUid(Long uid, Integer tenantId);
    
    Integer removeByJoinUid(Long uid, Long updateTime, Integer tenantId);
    
    InvitationActivityJoinHistory queryModifiedInviterHistory(Long joinUid, Integer tenantId);
    
    /**
     * 如果"退押后再次购买是否返现"为关闭状态，则需判断退押后再次购买是否给邀请人返现，判断逻辑：查询最后一笔退押成功的订单（电/车/车电），判断该笔订单退押时间是否在扫码之后，如果是，则不进行返现。
     * 返回：true-可以返现，false-不可以返现
     */
    Boolean isRebateAfterDepositRefund(Long uid, InvitationActivityJoinHistory invitationActivityJoinHistory);
}
