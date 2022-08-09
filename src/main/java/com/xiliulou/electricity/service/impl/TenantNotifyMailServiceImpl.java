package com.xiliulou.electricity.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TenantNotifyMail;
import com.xiliulou.electricity.mapper.TenantNotifyMailMapper;
import com.xiliulou.electricity.query.UpgradeNotifyMailQuery;
import com.xiliulou.electricity.service.TenantNotifyMailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (UpgradeNotifyMail)表服务实现类
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */
@Service("upgradeNotifyMailService")
@Slf4j
public class TenantNotifyMailServiceImpl implements TenantNotifyMailService {
    @Autowired
    private TenantNotifyMailMapper tenantNotifyMailMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TenantNotifyMail selectByIdFromDB(Long id) {
        return this.tenantNotifyMailMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TenantNotifyMail selectByIdFromCache(Long id) {
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
    public List<TenantNotifyMail> selectByPage(int offset, int limit) {
        return this.tenantNotifyMailMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param upgradeNotifyMailQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(UpgradeNotifyMailQuery upgradeNotifyMailQuery) {

        List<TenantNotifyMail> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(upgradeNotifyMailQuery.getMail())) {
            for (String mail : upgradeNotifyMailQuery.getMail()) {
                if (Validator.isEmail(mail)) {
                    return R.fail("SYSTEM.0002", "邮箱格式不合法！");
                }
                TenantNotifyMail tenantNotifyMail = new TenantNotifyMail();
                tenantNotifyMail.setMail(mail);
                tenantNotifyMail.setTenantId(TenantContextHolder.getTenantId().longValue());
                tenantNotifyMail.setCreateTime(System.currentTimeMillis());
                tenantNotifyMail.setUpdateTime(System.currentTimeMillis());
                list.add(tenantNotifyMail);
            }
        }

        this.tenantNotifyMailMapper.deleteByTenantId(TenantContextHolder.getTenantId());

        if (CollectionUtils.isNotEmpty(list)) {
            this.tenantNotifyMailMapper.batchInsert(list);
            return R.ok();
        }
        return R.fail("系统错误!");
    }

    /**
     * 修改数据
     *
     * @param tenantNotifyMail 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(TenantNotifyMail tenantNotifyMail) {
        return this.tenantNotifyMailMapper.update(tenantNotifyMail);

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
        return this.tenantNotifyMailMapper.deleteById(id) > 0;
    }


    @Override
    public List<TenantNotifyMail> selectByTenantId() {
        List<TenantNotifyMail> tenantNotifyMails = this.tenantNotifyMailMapper.selectList(new LambdaQueryWrapper<TenantNotifyMail>().eq(TenantNotifyMail::getTenantId, TenantContextHolder.getTenantId()));
        return tenantNotifyMails;
    }

    @Override
    public List<TenantNotifyMail> selectByTenantId(Long tenantId) {
        return this.tenantNotifyMailMapper.selectList(new LambdaQueryWrapper<TenantNotifyMail>().eq(TenantNotifyMail::getTenantId, tenantId));
    }

    @Override
    public Boolean checkByTenantId() {
        Boolean result = Boolean.TRUE;
        List<TenantNotifyMail> tenantNotifyMails = this.tenantNotifyMailMapper.selectList(new LambdaQueryWrapper<TenantNotifyMail>().eq(TenantNotifyMail::getTenantId, TenantContextHolder.getTenantId()));

        if (CollectionUtils.isEmpty(tenantNotifyMails)) {
            result = Boolean.FALSE;
        }
        return result;
    }

    @Override
    public R insertOrUpdate(UpgradeNotifyMailQuery upgradeNotifyMailQuery) {
//        if (StringUtils.isNotBlank(upgradeNotifyMailQuery.getMail())) {
//            List<String> mails = JsonUtil.fromJsonArray(upgradeNotifyMailQuery.getMail(), String.class);
//            for (String mail : mails) {
//                if (Validator.isEmail(mail)) {
//                    return R.fail("", "邮箱格式不合法！");
//                }
//            }
//        }
//
//        upgradeNotifyMailQuery.setTenantId(TenantContextHolder.getTenantId().longValue());
//        upgradeNotifyMailQuery.setCreateTime(System.currentTimeMillis());
//        upgradeNotifyMailQuery.setUpdateTime(System.currentTimeMillis());
//
//        return R.ok(this.upgradeNotifyMailMapper.insertOrUpdate(upgradeNotifyMailQuery));
        return R.ok();
    }
}
