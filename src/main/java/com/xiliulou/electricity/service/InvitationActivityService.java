package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.vo.InvitationActivityVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (InvitationActivity)表服务接口
 *
 * @author zzlong
 * @since 2023-06-01 15:55:48
 */
public interface InvitationActivityService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    Integer insert(InvitationActivity invitationActivity);

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivity invitationActivity);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteById(Long id);

    List<InvitationActivityVO> selectByPage(InvitationActivityQuery query);

    Integer selectByPageCount(InvitationActivityQuery query);

    Triple<Boolean, String, Object> save(InvitationActivityQuery query);

    Triple<Boolean, String, Object> modify(InvitationActivityQuery query);

    Triple<Boolean, String, Object> updateStatus(InvitationActivityStatusQuery query);

    List<InvitationActivity> selectUsableActivity(Integer tenantId);

    List<InvitationActivity> selectBySearch(InvitationActivityQuery query);

    Integer checkUsableActivity(Integer tenantId, Long franchiseeId);

    Triple<Boolean, String, Object> activityInfo();
    
    Triple<Boolean, String, Object> activityInfoV2();
    
    Triple<Boolean, String, Object> findActivityById(Long id);
    
    Triple<Boolean, String, Object> selectActivityByUser(InvitationActivityQuery query, Long uid);
    
    /**
     * <p>
     *    Description: delete
     *    9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    R<?> removeById(Long id);
    
}
