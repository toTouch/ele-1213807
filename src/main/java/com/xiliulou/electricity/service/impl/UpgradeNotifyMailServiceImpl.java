package com.xiliulou.electricity.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UpgradeNotifyMail;
import com.xiliulou.electricity.mapper.UpgradeNotifyMailMapper;
import com.xiliulou.electricity.query.UpgradeNotifyMailQuery;
import com.xiliulou.electricity.service.UpgradeNotifyMailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.UpgradeNotifyMailVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UpgradeNotifyMail)表服务实现类
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */
@Service("upgradeNotifyMailService")
@Slf4j
public class UpgradeNotifyMailServiceImpl implements UpgradeNotifyMailService {
    @Autowired
    private UpgradeNotifyMailMapper upgradeNotifyMailMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UpgradeNotifyMail selectByIdFromDB(Long id) {
        return this.upgradeNotifyMailMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UpgradeNotifyMail selectByIdFromCache(Long id) {
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
    public List<UpgradeNotifyMail> selectByPage(int offset, int limit) {
        return this.upgradeNotifyMailMapper.selectByPage(offset, limit);
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

        List<UpgradeNotifyMail> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(upgradeNotifyMailQuery.getMail())) {
            for (String mail : upgradeNotifyMailQuery.getMail()) {
                if (Validator.isEmail(mail)) {
                    return R.fail("SYSTEM.0002", "邮箱格式不合法！");
                }
                UpgradeNotifyMail upgradeNotifyMail = new UpgradeNotifyMail();
                upgradeNotifyMail.setMail(mail);
                upgradeNotifyMail.setTenantId(TenantContextHolder.getTenantId().longValue());
                upgradeNotifyMail.setCreateTime(System.currentTimeMillis());
                upgradeNotifyMail.setUpdateTime(System.currentTimeMillis());
                list.add(upgradeNotifyMail);
            }
        }

        this.upgradeNotifyMailMapper.deleteByTenantId(TenantContextHolder.getTenantId());

        if(CollectionUtils.isNotEmpty(list)){
            this.upgradeNotifyMailMapper.batchInsert(list);
            return R.ok();
        }
        return R.fail("系统错误!");
    }

    /**
     * 修改数据
     *
     * @param upgradeNotifyMail 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UpgradeNotifyMail upgradeNotifyMail) {
        return this.upgradeNotifyMailMapper.update(upgradeNotifyMail);

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
        return this.upgradeNotifyMailMapper.deleteById(id) > 0;
    }


    @Override
    public List<UpgradeNotifyMail> selectByTenantId() {
        List<UpgradeNotifyMail> upgradeNotifyMails = this.upgradeNotifyMailMapper.selectList(new LambdaQueryWrapper<UpgradeNotifyMail>().eq(UpgradeNotifyMail::getTenantId, TenantContextHolder.getTenantId()));
        return upgradeNotifyMails;
    }

    @Override
    public Boolean checkByTenantId() {
        Boolean result = Boolean.TRUE;
        List<UpgradeNotifyMail> upgradeNotifyMails = this.upgradeNotifyMailMapper.selectList(new LambdaQueryWrapper<UpgradeNotifyMail>().eq(UpgradeNotifyMail::getTenantId, TenantContextHolder.getTenantId()));

        if (CollectionUtils.isEmpty(upgradeNotifyMails)) {
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
