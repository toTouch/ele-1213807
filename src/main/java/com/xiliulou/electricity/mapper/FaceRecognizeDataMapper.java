package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FaceRecognizeData;

import java.util.List;

import com.xiliulou.electricity.query.FaceRecognizeDataQuery;
import com.xiliulou.electricity.vo.FaceRecognizeDataVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FaceRecognizeData)表数据库访问层
 *
 * @author zzlong
 * @since 2023-01-31 15:38:29
 */
public interface FaceRecognizeDataMapper extends BaseMapper<FaceRecognizeData> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeData selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<FaceRecognizeDataVO> selectByPage(FaceRecognizeDataQuery query);

    Integer selectByPageCount(FaceRecognizeDataQuery query);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param faceRecognizeData 实例对象
     * @return 对象列表
     */
    List<FaceRecognizeData> selectByQuery(FaceRecognizeData faceRecognizeData);

    /**
     * 新增数据
     *
     * @param faceRecognizeData 实例对象
     * @return 影响行数
     */
    int insertOne(FaceRecognizeData faceRecognizeData);

    /**
     * 修改数据
     *
     * @param faceRecognizeData 实例对象
     * @return 影响行数
     */
    int update(FaceRecognizeData faceRecognizeData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);


}
