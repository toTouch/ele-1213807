package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.FaceAuthResultData;
import com.xiliulou.electricity.mapper.FaceAuthResultDataMapper;
import com.xiliulou.electricity.service.FaceAuthResultDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (FaceAuthResultData)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-03 11:03:24
 */
@Service("faceAuthResultDataService")
@Slf4j
public class FaceAuthResultDataServiceImpl implements FaceAuthResultDataService {
    @Autowired
    private FaceAuthResultDataMapper faceAuthResultDataMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceAuthResultData selectByIdFromDB(Long id) {
        return this.faceAuthResultDataMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceAuthResultData selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<FaceAuthResultData> selectByPage(int offset, int limit) {
        return this.faceAuthResultDataMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param faceAuthResultData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaceAuthResultData insert(FaceAuthResultData faceAuthResultData) {
        this.faceAuthResultDataMapper.insertOne(faceAuthResultData);
        return faceAuthResultData;
    }

    /**
     * 修改数据
     *
     * @param faceAuthResultData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FaceAuthResultData faceAuthResultData) {
        return this.faceAuthResultDataMapper.update(faceAuthResultData);

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
        return this.faceAuthResultDataMapper.deleteById(id) > 0;
    }
}
