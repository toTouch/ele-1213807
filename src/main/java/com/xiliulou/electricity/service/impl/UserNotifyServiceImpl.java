package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.UserNotifyConstant;
import com.xiliulou.electricity.converter.storage.StorageConverter;
import com.xiliulou.electricity.entity.UserNotify;
import com.xiliulou.electricity.mapper.UserNotifyMapper;
import com.xiliulou.electricity.query.NotifyPictureInfo;
import com.xiliulou.electricity.query.UserNotifyQuery;
import com.xiliulou.electricity.service.UserNotifyService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.NotifyPictureInfoVO;
import com.xiliulou.electricity.vo.UserNotifyVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * (UserNotify)表服务实现类
 *
 * @author zgw
 * @since 2023-02-21 09:10:41
 */
@Service("userNotifyService")
@Slf4j
public class UserNotifyServiceImpl implements UserNotifyService {
    
    @Resource
    private UserNotifyMapper userNotifyMapper;
    
    @Resource
    RedisService redisService;
    
    
    @Autowired
    StorageConverter storageConverter;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserNotify queryByIdFromDB(Long id) {
        return this.userNotifyMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserNotify queryByIdFromCache(Long id) {
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
    public List<UserNotify> queryAllByLimit(int offset, int limit) {
        return this.userNotifyMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserNotify insert(UserNotify userNotify) {
        this.userNotifyMapper.insertOne(userNotify);
        return userNotify;
    }
    
    /**
     * 修改数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserNotify userNotify) {
        return this.userNotifyMapper.update(userNotify);
        
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
        return this.userNotifyMapper.deleteById(id) > 0;
    }
    
    @Override
    public UserNotify queryByTenantId() {
        return this.userNotifyMapper.queryByTenantId(TenantContextHolder.getTenantId());
    }
    
    @Override
    public R deleteOne(Long id) {
        return R.ok(userNotifyMapper.deleteById(id));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editOne(UserNotifyQuery userNotifyQuery) {
        // 限制操作频次
        if (!redisService.setNx(CacheConstant.USER_NOTIFY_SAVE_CACHE_UID + SecurityUtils.getUid(), "1", 3 * 1000L, false)) {
            return R.fail(false, "ELECTRICITY.0001", "操作频繁！");
        }
        
        if (Objects.isNull(userNotifyQuery.getStatus())) {
            userNotifyQuery.setStatus(UserNotify.STATUS_OFF);
        }
        
        if (Objects.equals(userNotifyQuery.getStatus(), UserNotify.STATUS_ON)) {
            if (Objects.equals(userNotifyQuery.getType(), UserNotifyConstant.TYPE_CONTENT) && (StringUtils.isBlank(userNotifyQuery.getTitle()) || StringUtils.isBlank(
                    userNotifyQuery.getContent()))) {
                return R.fail("100368", "用户通知标题和内容不能为空");
            }
            
            if (Objects.equals(userNotifyQuery.getType(), UserNotifyConstant.TYPE_PICTURE) && CollectionUtils.isEmpty(userNotifyQuery.getPictureInfoList())) {
                return R.fail("100376", "请上传通知图片");
            }
            
            if (Objects.isNull(userNotifyQuery.getBeginTime()) || Objects.isNull(userNotifyQuery.getEndTime())) {
                return R.fail("100369", "用户通知时间间隔不能为空");
            }
            
            if (Objects.equals(userNotifyQuery.getType(), UserNotifyConstant.TYPE_CONTENT) && StringUtils.isNotBlank(userNotifyQuery.getContent())) {
                if (userNotifyQuery.getContent().length() > 200) {
                    return R.fail("100377", "参数校验错误");
                }
            }
        }
        
        UserNotify userNotify = queryByTenantId();
        
        // 如果租户的通知配置不存在且状态为关闭则直接返回成功
        if (Objects.isNull(userNotify) && Objects.equals(userNotifyQuery.getStatus(), UserNotify.STATUS_OFF)) {
            return R.ok();
        }
        
        UserNotify updateAndInsert = new UserNotify();
        
        if (Objects.equals(userNotifyQuery.getStatus(), UserNotify.STATUS_ON)) {
            updateAndInsert.setTitle(userNotifyQuery.getTitle());
            updateAndInsert.setBeginTime(userNotifyQuery.getBeginTime());
            updateAndInsert.setEndTime(userNotifyQuery.getEndTime());
            updateAndInsert.setStatus(userNotifyQuery.getStatus());
            updateAndInsert.setTenantId(TenantContextHolder.getTenantId());
            updateAndInsert.setUpdateTime(System.currentTimeMillis());
            
            if (Objects.equals(userNotifyQuery.getType(), UserNotifyConstant.TYPE_PICTURE)) {
                updateAndInsert.setType(UserNotifyConstant.TYPE_PICTURE);
                List<NotifyPictureInfo> pictureInfoList = userNotifyQuery.getPictureInfoList();
                if (CollectionUtils.isNotEmpty(pictureInfoList)) {
                    updateAndInsert.setPictureInfo(JsonUtil.toJson(pictureInfoList));
                }
                updateAndInsert.setContent(StringUtils.EMPTY);
                updateAndInsert.setTitle(StringUtils.EMPTY);
            } else {
                updateAndInsert.setType(UserNotifyConstant.TYPE_CONTENT);
                updateAndInsert.setContent(userNotifyQuery.getContent());
                updateAndInsert.setPictureInfo(StringUtils.EMPTY);
            }
        }
        
        // 如果状态为关闭则只修改状态
        if (Objects.equals(userNotifyQuery.getStatus(), UserNotify.STATUS_OFF)) {
            updateAndInsert.setStatus(userNotifyQuery.getStatus());
            updateAndInsert.setUpdateTime(System.currentTimeMillis());
        }
        
        if (Objects.isNull(userNotify)) {
            updateAndInsert.setCreateTime(System.currentTimeMillis());
            insert(updateAndInsert);
        } else {
            updateAndInsert.setId(userNotify.getId());
            update(updateAndInsert);
        }
        operateRecordUtil.record(userNotify, updateAndInsert);
        return R.ok();
    }
    
    @Override
    public R queryOne(Integer newVersion) {
        UserNotify userNotify = this.queryByTenantId();
        if (Objects.isNull(userNotify)) {
            return R.ok();
        }
        
        UserNotifyVo vo = new UserNotifyVo();
        BeanUtils.copyProperties(userNotify, vo);
        String pictureInfo = userNotify.getPictureInfo();
        if (StringUtils.isNotBlank(pictureInfo)) {
            List<NotifyPictureInfo> pictureInfoList = JsonUtil.fromJsonArray(pictureInfo, NotifyPictureInfo.class);
            List<NotifyPictureInfoVO> pictureInfoVOList = new ArrayList<>();
            for (NotifyPictureInfo info : pictureInfoList) {
                NotifyPictureInfoVO infoVo = new NotifyPictureInfoVO();
                infoVo.setActivityType(info.getActivityType());
                infoVo.setPictureUrl(info.getPictureUrl());
//                infoVo.setPictureOSSUrl(StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + info.getPictureUrl());
                infoVo.setPictureOSSUrl(storageConverter.filePrefixFoldPathHuaweiOrAliWithCdn(info.getPictureUrl()));
                pictureInfoVOList.add(infoVo);
            }
            vo.setPictureInfoList(pictureInfoVOList);
        }
        
        if (!Objects.equals(newVersion, UserNotifyConstant.NEW_VERSION) && Objects.equals(userNotify.getType(), UserNotifyConstant.TYPE_PICTURE) && Objects.equals(
                userNotify.getStatus(), UserNotifyConstant.STATUS_ON)) {
            // 通知状态 0--关闭 1--开启
            vo.setStatus(UserNotifyConstant.STATUS_OFF);
        }
        
        return R.ok(vo);
    }
    
    @Override
    public R queryOneForAdmin() {
        UserNotify userNotify = this.queryByTenantId();
        if (Objects.isNull(userNotify)) {
            return R.ok();
        }
        
        UserNotifyVo vo = new UserNotifyVo();
        BeanUtils.copyProperties(userNotify, vo);
        String pictureInfo = userNotify.getPictureInfo();
        if (StringUtils.isNotBlank(pictureInfo)) {
            List<NotifyPictureInfo> pictureInfoList = JsonUtil.fromJsonArray(pictureInfo, NotifyPictureInfo.class);
            List<NotifyPictureInfoVO> pictureInfoVOList = new ArrayList<>();
            for (NotifyPictureInfo info : pictureInfoList) {
                NotifyPictureInfoVO infoVo = new NotifyPictureInfoVO();
                infoVo.setActivityType(info.getActivityType());
                infoVo.setPictureUrl(info.getPictureUrl());
//                infoVo.setPictureOSSUrl(StorageConfig.HTTPS + storageConfig.getBucketName() + "." + storageConfig.getOssEndpoint() + "/" + info.getPictureUrl());
                infoVo.setPictureOSSUrl(storageConverter.filePrefixFoldPathHuaweiOrAliWithCdn(info.getPictureUrl()));
                pictureInfoVOList.add(infoVo);
            }
            vo.setPictureInfoList(pictureInfoVOList);
        } else {
            vo.setPictureInfoList(Lists.newArrayList());
        }
        
        return R.ok(vo);
    }
}
