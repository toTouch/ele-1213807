package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FaceRecognizeRechargeRecord;

import java.util.List;

import com.xiliulou.electricity.query.FaceRecognizeRechargeRecordQuery;
import com.xiliulou.electricity.vo.FaceRecognizeRechargeRecordVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FaceRecognizeRechargeRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-01-31 17:18:00
 */
public interface FaceRecognizeRechargeRecordMapper extends BaseMapper<FaceRecognizeRechargeRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeRechargeRecord selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<FaceRecognizeRechargeRecordVO> selectByPage(FaceRecognizeRechargeRecordQuery query);


    Integer selectByPageCount(FaceRecognizeRechargeRecordQuery query);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 对象列表
     */
    List<FaceRecognizeRechargeRecord> selectByQuery(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord);

    /**
     * 新增数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 影响行数
     */
    int insertOne(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord);

    /**
     * 修改数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 影响行数
     */
    int update(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
