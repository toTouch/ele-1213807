package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;

/**
 * 活动表(Activity)表服务接口
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareMoneyActivityService {
    
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareMoneyActivity queryByIdFromCache(Integer id);
    
    /**
     * 新增数据
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R insert(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery);
    
    /**
     * 修改数据
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R update(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery);
    
    R queryList(ShareMoneyActivityQuery shareMoneyActivityQuery);
    
    R queryCount(ShareMoneyActivityQuery shareMoneyActivityQuery);
    
    R queryInfo(Integer id);
    
    ShareMoneyActivity queryByStatus(Integer activityId);
    
    R activityInfo();
    
    R checkActivity();
    
    R checkActivityStatusOn();
    
    Integer existShareMoneyActivity(Integer tenantId);
    
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
    R<?> removeById(Long id);
    
    ShareMoneyActivity queryOnlineActivity(Integer tenantId, Long franchiseeId);
}
