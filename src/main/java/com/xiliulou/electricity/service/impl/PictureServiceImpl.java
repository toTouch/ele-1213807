package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.mapper.PictureMapper;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.query.StorePictureQuery;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 图片表(Picture)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-14 13:54:08
 */
@Service("pictureService")
@Slf4j
public class PictureServiceImpl implements PictureService {
    @Autowired
    private PictureMapper pictureMapper;
    @Autowired
    StorageConfig storageConfig;
    @Qualifier("aliyunOssService")
    @Autowired
    StorageService storageService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Picture selectByIdFromDB(Long id) {
        return this.pictureMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Picture selectByIdFromCache(Long id) {
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
    public List<Picture> selectByPage(int offset, int limit) {
        return this.pictureMapper.selectByPage(offset, limit);
    }


    @Override
    public List<Picture> selectByQuery(PictureQuery pictureQuery) {
        List<Picture> pictures = this.pictureMapper.selectByQuery(pictureQuery);
        if (CollectionUtils.isEmpty(pictures)) {
            return Collections.EMPTY_LIST;
        }

        return pictures.parallelStream().peek(item -> {
            item.setPictureUrl(storageService.getOssFileUrl(storageConfig.getBucketName(), item.getPictureUrl(), System.currentTimeMillis() + 10 * 60 * 1000L));
        }).collect(Collectors.toList());

    }

    /**
     * 新增数据
     *
     * @param picture 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Picture insert(Picture picture) {
        this.pictureMapper.insertOne(picture);
        return picture;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<Picture> pictures) {
        if (CollectionUtils.isEmpty(pictures)) {
            return NumberConstant.ZERO;
        }
        return this.pictureMapper.batchInsert(pictures);
    }

    /**
     * 修改数据
     *
     * @param picture 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Picture picture) {
        return this.pictureMapper.update(picture);

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
        return this.pictureMapper.deleteById(id) > 0;
    }

    @Override
    public int deleteByBusinessId(Long id) {
        return this.pictureMapper.deleteByBusinessId(id);
    }

    @Override
    public List<Picture> selectByByBusinessId(Long id) {
        return this.pictureMapper.selectByByBusinessId(id);
    }

    @Override
    public Integer savePictureCallBack(CallBackQuery callBackQuery) {
        if (CollectionUtils.isEmpty(callBackQuery.getFileNameList()) || Objects.isNull(callBackQuery.getOtherId())) {
            return NumberConstant.ZERO;
        }


        //删除车辆型号图片
        this.deleteByBusinessId(callBackQuery.getOtherId());

        List<Picture> list = Lists.newArrayList();

        List<String> pictureNameList = callBackQuery.getFileNameList();
        for (int i = 0; i < pictureNameList.size(); i++) {
            Picture picture = new Picture();
            picture.setBusinessId(callBackQuery.getOtherId());
            picture.setPictureUrl(pictureNameList.get(i));
            picture.setSeq(i);
            picture.setStatus(Picture.STATUS_ENABLE);
            picture.setImgType(callBackQuery.getFileType());
            picture.setDelFlag(Picture.DEL_NORMAL);
            picture.setTenantId(TenantContextHolder.getTenantId());
            picture.setCreateTime(System.currentTimeMillis());
            picture.setUpdateTime(System.currentTimeMillis());
            list.add(picture);
        }

        //保存车辆型号图片
        return this.batchInsert(list);
    }

}
