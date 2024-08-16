package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareActivityService {
    
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivity queryByIdFromCache(Integer id);
    
    /**
     * 新增数据
     *
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery);
    
    /**
     * 修改数据
     *
     * @param shareActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery);
    
    R queryList(ShareActivityQuery shareActivityQuery);
    
    R queryInfo(Integer id);
    
    R queryCount(ShareActivityQuery shareActivityQuery);
    
    R activityInfo();
    
    ShareActivity queryByStatus(Integer activityId);
    
    Triple<Boolean, String, Object> shareActivityDetail(Integer id);
    
    Triple<Boolean, String, Object> updateShareActivity(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery);
    
    R checkExistActivity();
    
    /**
     * <p>
     * Description: delete 9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
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
    R<?> removeById(Long id, List<Long> franchiseeIds);
    
    ShareActivity queryOnlineActivity(Integer tenantId, Long franchiseeId);
}
