package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FaceRecognizeRechargeRecord;
import com.xiliulou.electricity.query.FaceRecognizeRechargeRecordQuery;
import com.xiliulou.electricity.vo.FaceRecognizeRechargeRecordVO;

import java.util.List;

/**
 * (FaceRecognizeRechargeRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-01-31 17:18:00
 */
public interface FaceRecognizeRechargeRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeRechargeRecord selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeRechargeRecord selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<FaceRecognizeRechargeRecordVO> selectByPage(FaceRecognizeRechargeRecordQuery query);

    Integer selectByPageCount(FaceRecognizeRechargeRecordQuery query);

    /**
     * 新增数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 实例对象
     */
    FaceRecognizeRechargeRecord insert(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord);

    /**
     * 修改数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 实例对象
     */
    Integer update(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);


}
