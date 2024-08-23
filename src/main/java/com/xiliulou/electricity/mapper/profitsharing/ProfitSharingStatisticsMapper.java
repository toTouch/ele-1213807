package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 分账统计(TProfitSharingStatistics)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-22 17:31:15
 */
public interface ProfitSharingStatisticsMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ProfitSharingStatistics queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param profitSharingStatistics 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<ProfitSharingStatistics> queryAllByLimit(ProfitSharingStatistics profitSharingStatistics, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param profitSharingStatistics 查询条件
     * @return 总行数
     */
    long count(ProfitSharingStatistics profitSharingStatistics);

    /**
     * 新增数据
     *
     * @param profitSharingStatistics 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingStatistics profitSharingStatistics);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TProfitSharingStatistics> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ProfitSharingStatistics> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TProfitSharingStatistics> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<ProfitSharingStatistics> entities);

    /**
     * 修改数据
     *
     * @param profitSharingStatistics 实例对象
     * @return 影响行数
     */
    int update(ProfitSharingStatistics profitSharingStatistics);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}

