package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.VersionNotification;
import com.xiliulou.electricity.mapper.VersionNotificationMapper;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import com.xiliulou.electricity.service.VersionNotificationService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (VersionNotification)表服务实现类
 *
 * @author makejava
 * @since 2021-09-26 14:36:07
 */
@Service("versionNotificationService")
@Slf4j
public class VersionNotificationServiceImpl implements VersionNotificationService {

    private static final Integer limit = 20;

    @Resource
    private VersionNotificationMapper versionNotificationMapper;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public VersionNotification queryByIdFromDB(Integer id) {
        return this.versionNotificationMapper.selectById(id);
    }


    /**
     * 修改数据
     *
     * @param versionNotification 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(VersionNotification versionNotification) {
        return this.versionNotificationMapper.updateById(versionNotification);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateNotification(VersionNotificationQuery versionNotificationQuery) {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo) || !userInfo.getType().equals(User.TYPE_USER_SUPER)) {
            return Triple.of(false, "SYSTEM.0007", "系统错误");
        }

        VersionNotification versionNotification = new VersionNotification();
        versionNotification.setId(versionNotificationQuery.getId());
        versionNotification.setVersion(versionNotificationQuery.getVersion());
        versionNotification.setContent(versionNotificationQuery.getContent());
        versionNotification.setSendMailStatus(VersionNotification.STATUS_SEND_MAIL_NO);
        versionNotification.setUpdateTime(System.currentTimeMillis());
        update(versionNotification);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> insertNotification(VersionNotificationQuery versionNotificationQuery) {
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo) || !userInfo.getType().equals(User.TYPE_USER_SUPER)) {
            return Triple.of(false, "SYSTEM.0007", "系统错误");
        }
        VersionNotification versionNotification = new VersionNotification();
        versionNotification.setVersion(versionNotificationQuery.getVersion());
        versionNotification.setContent(versionNotificationQuery.getContent());
        versionNotification.setSendMailStatus(VersionNotification.STATUS_SEND_MAIL_NO);
        versionNotification.setCreateTime(System.currentTimeMillis());
        versionNotification.setUpdateTime(System.currentTimeMillis());
        insert(versionNotification);

        return Triple.of(true, null, null);
    }

    public Integer insert(VersionNotification versionNotification) {
        return this.versionNotificationMapper.insert(versionNotification);
    }

    @Override
    public List<VersionNotification> queryNotificationList(Long offset, Long size) {
        return this.versionNotificationMapper.queryVersionPage(offset, size);
    }

    @Override
    public R queryNotificationCount() {
        return R.ok(this.versionNotificationMapper.selectCount(null));
    }

    @Override
    public VersionNotification queryCreateTimeMaxTenantNotification() {
        return this.versionNotificationMapper.queryCreateTimeMaxTenantNotification();
    }

}
