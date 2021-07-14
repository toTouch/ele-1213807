package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import java.util.List;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityRecord queryByIdFromDB(Long id);

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ShareActivityRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    ShareActivityRecord insert(ShareActivityRecord shareActivityRecord);

    /**
     * 修改数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(ShareActivityRecord shareActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

	R generateShareUrl(Integer activityId,Integer type);
}
