package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.vo.InvitationActivitySearchVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (InvitationActivity)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-01 15:55:47
 */
public interface InvitationActivityMapper extends BaseMapper<InvitationActivity> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivity queryById(Long id);
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivity 实例对象
     * @return 对象列表
     */
    List<InvitationActivity> queryAll(InvitationActivity invitationActivity);
    
    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivity invitationActivity);
    
    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 影响行数
     */
    int update(InvitationActivity invitationActivity);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<InvitationActivity> selectByPage(InvitationActivityQuery query);
    
    Integer selectByPageCount(InvitationActivityQuery query);
    
    Integer checkUsableActivity(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<InvitationActivity> selectUsableActivity(Integer tenantId);
    
    List<InvitationActivity> selectBySearch(InvitationActivityQuery query);
    
    List<InvitationActivitySearchVO> selectByPageSearchNoUid(InvitationActivityQuery query);
    
    
    List<InvitationActivitySearchVO> selectByPageSearch(InvitationActivityQuery query);
    /**
     * <p>
     * Description: removeById 9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     *
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    int removeById(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
