package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetServerOperRecord;
import com.xiliulou.electricity.vo.ElectricityCabinetServerOperRecordVo;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ElectricityCabinetServerOperRecord)表数据库访问层
 *
 * @author Hardy
 * @since 2022-09-26 17:54:53
 */
public interface ElectricityCabinetServerOperRecordMapper extends BaseMapper<ElectricityCabinetServerOperRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServerOperRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetServerOperRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetServerOperRecord> queryAll(
        ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord);

    /**
     * 新增数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord);

    /**
     * 修改数据
     *
     * @param electricityCabinetServerOperRecord 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetServerOperRecord electricityCabinetServerOperRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ElectricityCabinetServerOperRecordVo> queryList(@Param("createUserName") String createUserName,
        @Param("eleServerId") Long eleServerId, @Param("offset") Long offset, @Param("size") Long size);

    Long queryCount(@Param("createUserName") String createUserName, @Param("eleServerId") Long eleServerId);
}
