package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.FaceRecognizeRechargeRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.FaceRecognizeRechargeRecordMapper;
import com.xiliulou.electricity.query.FaceRecognizeRechargeRecordQuery;
import com.xiliulou.electricity.service.FaceRecognizeRechargeRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.FaceRecognizeRechargeRecordVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (FaceRecognizeRechargeRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-01-31 17:18:00
 */
@Service("faceRecognizeRechargeRecordService")
@Slf4j
public class FaceRecognizeRechargeRecordServiceImpl implements FaceRecognizeRechargeRecordService {
    @Autowired
    private FaceRecognizeRechargeRecordMapper faceRecognizeRechargeRecordMapper;

    @Autowired
    private UserService userService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceRecognizeRechargeRecord selectByIdFromDB(Long id) {
        return this.faceRecognizeRechargeRecordMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceRecognizeRechargeRecord selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<FaceRecognizeRechargeRecordVO> selectByPage(FaceRecognizeRechargeRecordQuery query) {
        List<FaceRecognizeRechargeRecordVO> faceRecognizeRechargeRecords = this.faceRecognizeRechargeRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(faceRecognizeRechargeRecords)) {
            return Collections.EMPTY_LIST;
        }

        return faceRecognizeRechargeRecords.stream().peek(item -> {
            if (Objects.nonNull(item.getOperator())) {
                User user = userService.queryByUidFromCache(item.getOperator());
                item.setOperatorName(Objects.isNull(user) ? "" : user.getName());
            }
        }).collect(Collectors.toList());

    }

    @Override
    public Integer selectByPageCount(FaceRecognizeRechargeRecordQuery query) {
        return this.faceRecognizeRechargeRecordMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaceRecognizeRechargeRecord insert(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord) {
        this.faceRecognizeRechargeRecordMapper.insertOne(faceRecognizeRechargeRecord);
        return faceRecognizeRechargeRecord;
    }

    /**
     * 修改数据
     *
     * @param faceRecognizeRechargeRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FaceRecognizeRechargeRecord faceRecognizeRechargeRecord) {
        return this.faceRecognizeRechargeRecordMapper.update(faceRecognizeRechargeRecord);

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
        return this.faceRecognizeRechargeRecordMapper.deleteById(id) > 0;
    }
}
