package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Faq;
import com.xiliulou.electricity.mapper.FaqMapper;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.service.FaqService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (Faq)表服务实现类
 *
 * @author makejava
 * @since 2021-09-26 14:06:24
 */
@Deprecated
@Service("faqService")
@Slf4j
public class FaqServiceImpl implements FaqService {
    @Resource
    private FaqMapper faqMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Faq queryByIdFromDB(Integer id, Integer tenantId) {
        return this.faqMapper.selectOne(new LambdaQueryWrapper<Faq>().eq(Faq::getId, id).eq(Faq::getTenantId, tenantId));
    }


    /**
     * 新增数据
     *
     * @param faq 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Faq insert(Faq faq) {
        this.faqMapper.insert(faq);
        return faq;
    }

    /**
     * 修改数据
     *
     * @param faq 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Faq faq) {
        return this.faqMapper.updateById(faq);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id, Integer tenantId) {
        return this.faqMapper.delete(id, tenantId) > 0;
    }

    @Slave
    @Override
    public Triple<Boolean, String, Object> queryList(Integer size, Integer offset) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        return Triple.of(true, null, faqMapper.queryList(size, offset, tenantId));
    }

    @Override
    public Triple<Boolean, String, Object> addFaq(FaqQuery faqQuery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Faq build = Faq.builder()
                .content(faqQuery.getContent())
                .createTime(System.currentTimeMillis())
                .title(faqQuery.getTitle())
                .pic(faqQuery.getPic())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId).build();
        insert(build);
        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> updateFaq(FaqQuery faqQuery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Faq faq = queryByIdFromDB(faqQuery.getId(), tenantId);
        if (Objects.isNull(faq)) {
            return Triple.of(false, "ELECTRICITY.0096", "没有该记录");
        }


        faq.setUpdateTime(System.currentTimeMillis());
        faq.setContent(faqQuery.getContent());
        faq.setTitle(faqQuery.getTitle());
        faq.setPic(faqQuery.getPic());

        update(faq);
        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> delete(Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        deleteById(id, tenantId);
        return Triple.of(true, null, null);
    }

    @Slave
    @Override
    public Triple<Boolean, String, Object> queryCount() {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        return Triple.of(true, null, faqMapper.queryCount(tenantId));
    }
}
