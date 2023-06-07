package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.InvitationActivityRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.vo.InvitationActivityRecordVO;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * (InvitationActivityRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-06-05 20:17:53
 */
public interface InvitationActivityRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityRecord queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 实例对象
     */
    InvitationActivityRecord insert(InvitationActivityRecord invitationActivityRecord);

    /**
     * 修改数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivityRecord invitationActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    void handleInvitationActivity(UserInfo userInfo, ElectricityMemberCardOrder electricityMemberCardOrder, Integer payCount);

    Triple<Boolean, String, Object> joinActivity(InvitationActivityQuery query);

    Triple<Boolean, String, Object> generateCode();

    List<InvitationActivityRecordVO> selectByPage(InvitationActivityRecordQuery query);

    Integer selectByPageCount(InvitationActivityRecordQuery query);

    Integer addCountAndMoneyByUid( BigDecimal rewardAmount, Long recordId);
}
