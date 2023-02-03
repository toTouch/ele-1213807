package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FaceRecognizeUserRecord;

import java.util.List;

import com.xiliulou.electricity.query.FaceRecognizeUserRecordQuery;
import com.xiliulou.electricity.vo.FaceRecognizeUserRecordVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FaceRecognizeUserRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-02 14:27:09
 */
public interface FaceRecognizeUserRecordMapper extends BaseMapper<FaceRecognizeUserRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeUserRecord selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<FaceRecognizeUserRecord> selectByPage(FaceRecognizeUserRecordQuery query);

    Integer selectByPageCount(FaceRecognizeUserRecordQuery query);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 对象列表
     */
    List<FaceRecognizeUserRecord> selectByQuery(FaceRecognizeUserRecord faceRecognizeUserRecord);

    /**
     * 新增数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 影响行数
     */
    int insertOne(FaceRecognizeUserRecord faceRecognizeUserRecord);

    /**
     * 修改数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 影响行数
     */
    int update(FaceRecognizeUserRecord faceRecognizeUserRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    FaceRecognizeUserRecord selectLatestByUid(@Param("uid") Long uid);
}
