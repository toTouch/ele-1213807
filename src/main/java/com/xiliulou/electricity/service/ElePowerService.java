package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.query.ElePowerListQuery;
import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (ElePower)表服务接口
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
public interface ElePowerService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElePower queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElePower queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElePower> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    ElePower insert(ElePower elePower);

    /**
     * 修改数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    Integer update(ElePower elePower);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    int insertOrUpdate(ElePower power);


    Pair<Boolean, Object> queryList(ElePowerListQuery query);

    Pair<Boolean, Object> queryDayList(Long eid, Long startTime, Long endTime, Integer tenantId);

    Pair<Boolean, Object> queryMonthList(Long eid, Long startTime, Long endTime, Integer tenantId);

    Pair<Boolean, Object> queryDayDetail(Long eid, Long startTime, Long endTime, Integer tenantId);

    Pair<Boolean, Object> queryMonthDetail(Long eid, Long startTime, Long endTime, Integer tenantId);

    void exportList(ElePowerListQuery query, HttpServletResponse response);
}
