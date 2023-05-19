package com.xiliulou.electricity.service.impl;

import com.alibaba.nacos.common.utils.Objects;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.FaceRecognizeUserRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.FaceRecognizeUserRecordMapper;
import com.xiliulou.electricity.query.FaceRecognizeUserRecordQuery;
import com.xiliulou.electricity.service.FaceRecognizeUserRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.FaceRecognizeUserRecordVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (FaceRecognizeUserRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-02 14:27:09
 */
@Service("faceRecognizeUserRecordService")
@Slf4j
public class FaceRecognizeUserRecordServiceImpl implements FaceRecognizeUserRecordService {
    @Autowired
    private FaceRecognizeUserRecordMapper faceRecognizeUserRecordMapper;

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceRecognizeUserRecord selectByIdFromDB(Long id) {
        return this.faceRecognizeUserRecordMapper.selectById(id);
    }

    /**
     * 通过UID查询最新的数据
     * @return 实例对象
     */
    @Override
    public FaceRecognizeUserRecord selectLatestByUid(Long uid) {
        return this.faceRecognizeUserRecordMapper.selectLatestByUid(uid);
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Slave
    @Override
    public List<FaceRecognizeUserRecordVO> selectByPage(FaceRecognizeUserRecordQuery query) {
        List<FaceRecognizeUserRecordVO> faceRecognizeUserRecords = this.faceRecognizeUserRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(faceRecognizeUserRecords)) {
            return Collections.EMPTY_LIST;
        }

        return faceRecognizeUserRecords;
    }

    @Slave
    @Override
    public Integer selectByPageCount(FaceRecognizeUserRecordQuery query) {
        return this.faceRecognizeUserRecordMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaceRecognizeUserRecord insert(FaceRecognizeUserRecord faceRecognizeUserRecord) {
        this.faceRecognizeUserRecordMapper.insertOne(faceRecognizeUserRecord);
        return faceRecognizeUserRecord;
    }

    /**
     * 修改数据
     *
     * @param faceRecognizeUserRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FaceRecognizeUserRecord faceRecognizeUserRecord) {
        return this.faceRecognizeUserRecordMapper.update(faceRecognizeUserRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.faceRecognizeUserRecordMapper.deleteById(id) > 0;
    }
}
