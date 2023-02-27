package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FaceRecognizeUserRecord;
import com.xiliulou.electricity.query.FaceRecognizeUserRecordQuery;
import com.xiliulou.electricity.vo.FaceRecognizeUserRecordVO;

import java.util.List;

/**
 * (FaceRecognizeUserRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-02-02 14:27:09
 */
public interface FaceRecognizeUserRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeUserRecord selectByIdFromDB(Long id);

    /**
     * 通过UID查询最新的数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    FaceRecognizeUserRecord selectLatestByUid(Long uid);
    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<FaceRecognizeUserRecordVO> selectByPage(FaceRecognizeUserRecordQuery query);

    Integer selectByPageCount(FaceRecognizeUserRecordQuery query);

    /**
     * 新增数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 实例对象
     */
    FaceRecognizeUserRecord insert(FaceRecognizeUserRecord faceRecognizeUserRecord);

    /**
     * 修改数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 实例对象
     */
    Integer update(FaceRecognizeUserRecord faceRecognizeUserRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}
