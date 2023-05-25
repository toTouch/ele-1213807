package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FaceRecognizeData;
import com.xiliulou.electricity.query.FaceRecognizeDataQuery;
import com.xiliulou.electricity.vo.FaceRecognizeDataVO;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (FaceRecognizeData)表服务接口
 *
 * @author zzlong
 * @since 2023-01-31 15:38:29
 */
public interface FaceRecognizeDataService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceRecognizeData selectByIdFromDB(Long id);

    FaceRecognizeData selectByTenantId(Integer tenantId);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<FaceRecognizeDataVO> selectByPage(FaceRecognizeDataQuery query);

    Integer selectByPageCount(FaceRecognizeDataQuery query);

    /**
     * 新增数据
     *
     * @return 实例对象
     */
    Triple<Boolean,String,Object> insert(FaceRecognizeDataQuery faceRecognizeDataQuery);

    /**
     * 修改数据
     *
     * @return 实例对象
     */
    Pair<Boolean, Object> update(FaceRecognizeDataQuery faceRecognizeDataQuery);

    Integer updateById(FaceRecognizeData faceRecognizeDataUpdate);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);


    Triple<Boolean,String,Object> recharge(FaceRecognizeDataQuery faceRecognizeDataQuery);

    Integer deductionCapacityByTenantId(FaceRecognizeData faceRecognizeDataUpdate);
}
