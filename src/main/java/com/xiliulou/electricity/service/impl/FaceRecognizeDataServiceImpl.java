package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FaceRecognizeData;
import com.xiliulou.electricity.entity.FaceRecognizeRechargeRecord;
import com.xiliulou.electricity.mapper.FaceRecognizeDataMapper;
import com.xiliulou.electricity.query.FaceRecognizeDataQuery;
import com.xiliulou.electricity.service.FaceRecognizeDataService;
import com.xiliulou.electricity.service.FaceRecognizeRechargeRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FaceRecognizeDataVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (FaceRecognizeData)表服务实现类
 *
 * @author zzlong
 * @since 2023-01-31 15:38:29
 */
@Service("faceRecognizeDataService")
@Slf4j
public class FaceRecognizeDataServiceImpl implements FaceRecognizeDataService {

    @Autowired
    private FaceRecognizeDataMapper faceRecognizeDataMapper;

    @Autowired
    private FaceRecognizeRechargeRecordService rechargeRecordService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FaceRecognizeData selectByIdFromDB(Long id) {
        return this.faceRecognizeDataMapper.selectById(id);
    }

    /**
     * 通过租户ID查询单条数据
     */
    @Override
    public FaceRecognizeData selectByTenantId(Integer tenantId) {
        return this.faceRecognizeDataMapper.selectOne(new LambdaQueryWrapper<FaceRecognizeData>().eq(FaceRecognizeData::getDelFlag, FaceRecognizeData.DEL_NORMAL)
                .eq(FaceRecognizeData::getTenantId, tenantId));
    }

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<FaceRecognizeDataVO> selectByPage(FaceRecognizeDataQuery query) {
        List<FaceRecognizeDataVO> faceRecognizeDatas = this.faceRecognizeDataMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(faceRecognizeDatas)) {
            return Collections.EMPTY_LIST;
        }

        return faceRecognizeDatas;
    }

    @Override
    public Integer selectByPageCount(FaceRecognizeDataQuery query) {
        return this.faceRecognizeDataMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> insert(FaceRecognizeDataQuery faceRecognizeDataQuery) {

        FaceRecognizeData recognizeData = this.selectByTenantId(faceRecognizeDataQuery.getTenantId());
        if (Objects.nonNull(recognizeData)) {
            return Triple.of(false, "100333", "该租户已存在人脸核身配置，请勿重复添加");
        }

        FaceRecognizeData faceRecognizeData = new FaceRecognizeData();
        BeanUtils.copyProperties(faceRecognizeDataQuery, faceRecognizeData);

        faceRecognizeData.setRechargeTime(System.currentTimeMillis());
        faceRecognizeData.setDelFlag(FaceRecognizeData.DEL_NORMAL);
        faceRecognizeData.setCreateTime(System.currentTimeMillis());
        faceRecognizeData.setUpdateTime(System.currentTimeMillis());

        this.faceRecognizeDataMapper.insertOne(faceRecognizeData);

        //保存充值记录
        rechargeRecordService.insert(buildFaceRecognizeRechargeRecord(faceRecognizeData, faceRecognizeDataQuery.getFaceRecognizeCapacity()));

        return Triple.of(true, "", null);
    }

    /**
     * 修改数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> update(FaceRecognizeDataQuery faceRecognizeDataQuery) {
        FaceRecognizeData faceRecognizeData = this.selectByIdFromDB(faceRecognizeDataQuery.getId());

        if (faceRecognizeData.getFaceRecognizeCapacity() > 0 && System.currentTimeMillis() - faceRecognizeData.getRechargeTime() < 365 * 24 * 60 * 60 * 1000L) {
            return Pair.of(false, "计费周期内不允许重复充值");
        }

        Integer faceRecognizeCapacity = faceRecognizeDataQuery.getFaceRecognizeCapacity();
        if (faceRecognizeDataQuery.getFaceRecognizeCapacity() <= 0) {
            faceRecognizeCapacity = faceRecognizeData.getFaceRecognizeCapacity() + faceRecognizeDataQuery.getFaceRecognizeCapacity();
        }

        FaceRecognizeData faceRecognizeDataUpdate = new FaceRecognizeData();
        faceRecognizeDataUpdate.setId(faceRecognizeData.getId());
        faceRecognizeDataUpdate.setFaceRecognizeCapacity(faceRecognizeCapacity);
        faceRecognizeDataUpdate.setRechargeTime(System.currentTimeMillis());
        faceRecognizeDataUpdate.setUpdateTime(System.currentTimeMillis());

        this.faceRecognizeDataMapper.update(faceRecognizeDataUpdate);

        //保存充值记录
        rechargeRecordService.insert(buildFaceRecognizeRechargeRecord(faceRecognizeData, faceRecognizeDataQuery.getFaceRecognizeCapacity()));

        return Pair.of(true, null);
    }

    /**
     * 人脸核身充值
     * @param faceRecognizeDataQuery
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> recharge(FaceRecognizeDataQuery faceRecognizeDataQuery) {
        FaceRecognizeData recognizeData = this.selectByTenantId(faceRecognizeDataQuery.getTenantId());
        if (Objects.isNull(recognizeData)) {
            FaceRecognizeData faceRecognizeData = new FaceRecognizeData();
            BeanUtils.copyProperties(faceRecognizeDataQuery, faceRecognizeData);

            faceRecognizeData.setRechargeTime(System.currentTimeMillis());
            faceRecognizeData.setDelFlag(FaceRecognizeData.DEL_NORMAL);
            faceRecognizeData.setCreateTime(System.currentTimeMillis());
            faceRecognizeData.setUpdateTime(System.currentTimeMillis());

            this.faceRecognizeDataMapper.insertOne(faceRecognizeData);

            //保存充值记录
            rechargeRecordService.insert(buildFaceRecognizeRechargeRecord(faceRecognizeData, faceRecognizeDataQuery.getFaceRecognizeCapacity()));

            return Triple.of(true, "", null);
        }

        if (recognizeData.getFaceRecognizeCapacity() > 0 && System.currentTimeMillis() - recognizeData.getRechargeTime() < 365 * 24 * 60 * 60 * 1000L) {
            return Triple.of(false, "100333", "计费周期内不允许重复充值");
        }

        Integer faceRecognizeCapacity = faceRecognizeDataQuery.getFaceRecognizeCapacity();
        if (faceRecognizeDataQuery.getFaceRecognizeCapacity() <= 0) {
            faceRecognizeCapacity = recognizeData.getFaceRecognizeCapacity() + faceRecognizeDataQuery.getFaceRecognizeCapacity();
        }

        FaceRecognizeData faceRecognizeDataUpdate = new FaceRecognizeData();
        faceRecognizeDataUpdate.setId(recognizeData.getId());
        faceRecognizeDataUpdate.setFaceRecognizeCapacity(faceRecognizeCapacity);
        faceRecognizeDataUpdate.setRechargeTime(System.currentTimeMillis());
        faceRecognizeDataUpdate.setUpdateTime(System.currentTimeMillis());

        this.faceRecognizeDataMapper.update(faceRecognizeDataUpdate);

        //保存充值记录
        rechargeRecordService.insert(buildFaceRecognizeRechargeRecord(recognizeData, faceRecognizeDataQuery.getFaceRecognizeCapacity()));

        return Triple.of(true, "", null);
    }

    @Override
    public Integer updateById(FaceRecognizeData faceRecognizeDataUpdate) {
        return this.faceRecognizeDataMapper.update(faceRecognizeDataUpdate);
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
        return this.faceRecognizeDataMapper.deleteById(id) > 0;
    }


    private FaceRecognizeRechargeRecord buildFaceRecognizeRechargeRecord(FaceRecognizeData faceRecognizeData, Integer capacity) {
        FaceRecognizeRechargeRecord rechargeRecord = new FaceRecognizeRechargeRecord();
        rechargeRecord.setOperator(SecurityUtils.getUid());
        rechargeRecord.setTenantId(faceRecognizeData.getTenantId());
        rechargeRecord.setFaceRecognizeCapacity(capacity);
        rechargeRecord.setDelFlag(FaceRecognizeRechargeRecord.DEL_NORMAL);
        rechargeRecord.setCreateTime(System.currentTimeMillis());
        rechargeRecord.setUpdateTime(System.currentTimeMillis());

        return rechargeRecord;
    }
}
