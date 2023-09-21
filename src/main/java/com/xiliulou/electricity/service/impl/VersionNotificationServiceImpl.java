package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.TenantNotifyMailDTO;
import com.xiliulou.electricity.entity.EmailRecipient;
import com.xiliulou.electricity.entity.MQMailMessageNotify;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.VersionNotification;
import com.xiliulou.electricity.mapper.VersionNotificationMapper;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import com.xiliulou.electricity.service.MailService;
import com.xiliulou.electricity.service.TenantNotifyMailService;
import com.xiliulou.electricity.service.VersionNotificationService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (VersionNotification)表服务实现类
 *
 * @author makejava
 * @since 2021-09-26 14:36:07
 */
@Service("versionNotificationService")
@Slf4j
public class VersionNotificationServiceImpl implements VersionNotificationService {

    private static final Integer limit = 5;

    private static final String SUBJECT_PREFIX = "版本升级通知：";

    @Resource
    private VersionNotificationMapper versionNotificationMapper;
    @Autowired
    private TenantNotifyMailService tenantNotifyMailService;
    @Autowired
    private MailService mailService;


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

    /**
     * 获取最新未发送邮件通知的版本升级记录
     *
     * @return
     */
    @Override
    public VersionNotification selectNotSendMailOne() {
        return this.versionNotificationMapper.selectNotSendMailOne();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleVersionNotificationSendEmail() {
        //1.获取最新未发送邮件通知的版本升级记录
        VersionNotification versionNotification = this.selectNotSendMailOne();
        if (Objects.isNull(versionNotification)) {
            log.warn("ELE ERROR!versionNotification is null");
            return;
        }


        //2.获取所有通知邮箱   根据租户ID分组 发送通知
        List<TenantNotifyMailDTO> tenantNotifyMailDTOList = tenantNotifyMailService.selectAllTenantNotifyMail();
        if (CollectionUtils.isEmpty(tenantNotifyMailDTOList)) {
            log.error("ELE ERROR!tenantNotifyMailDTOList is empty");
            return;
        }

        Map<Long, List<TenantNotifyMailDTO>> tenantNotifyMailMap = tenantNotifyMailDTOList.stream().collect(Collectors.groupingBy(TenantNotifyMailDTO::getTenantId));
        if(ObjectUtil.isEmpty(tenantNotifyMailMap)){
            log.error("ELE ERROR!tenantNotifyMailMap is empty");
            return;
        }

        for (List<TenantNotifyMailDTO> tenantNotifyMailDTOs : tenantNotifyMailMap.values()) {
            if (CollectionUtils.isEmpty(tenantNotifyMailDTOs)) {
                log.info("ELE INFO!tenantNotifyMailDTOs is empty");
                break;
            }

            List<EmailRecipient> mailList = tenantNotifyMailDTOs.stream().map(item -> {
                EmailRecipient emailRecipient = new EmailRecipient();
                emailRecipient.setEmail(item.getMail());
                emailRecipient.setName(item.getTenantName());
                return emailRecipient;
            }).collect(Collectors.toList());

            MQMailMessageNotify mailMessageNotify = MQMailMessageNotify.builder()
                    .to(mailList)
                    .subject(SUBJECT_PREFIX + versionNotification.getVersion())
                    .text(versionNotification.getContent()).build();

            mailService.sendVersionNotificationEmailToMQ(mailMessageNotify);
        }

        //3.发送完毕 更新邮件发送状态
        VersionNotification updateVersionNotification = new VersionNotification();
        updateVersionNotification.setId(versionNotification.getId());
        updateVersionNotification.setSendMailStatus(VersionNotification.STATUS_SEND_MAIL_YES);
        updateVersionNotification.setUpdateTime(System.currentTimeMillis());
        this.update(updateVersionNotification);
    }
}
