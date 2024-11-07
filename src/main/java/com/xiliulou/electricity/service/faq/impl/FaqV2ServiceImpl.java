package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.mapper.faq.FaqV2Mapper;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqVo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 常见问题Service接口实现类
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Service
@AllArgsConstructor
public class FaqV2ServiceImpl implements FaqV2Service {
    
    private final FaqV2Mapper faqV2Mapper;
    
    private final FaqCategoryV2Mapper faqCategoryV2Mapper;
    
    private final ElectricityConfigMapper electricityConfigMapper;
    
    @Override
    public R saveFaqQuery(AdminFaqReq faqReq) {
        FaqV2 faq = BeanUtil.toBean(faqReq, FaqV2.class);
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqReq.getTypeId());
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        faq.setSort(BigDecimal.valueOf(countFaqReqByTypeId + 1));
        faq.setTenantId(TenantContextHolder.getTenantId());
        faq.setOpUser(SecurityUtils.getUid());
        faq.setOnShelf(faqReq.getOnShelf() == null ? UpDownEnum.DOWN.getCode() : faqReq.getOnShelf());
        faq.setCreateTime(System.currentTimeMillis());
        faq.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.insert(faq);
        return R.ok();
    }
    
    @Override
    public R updateFaqReq(AdminFaqReq faqReq) {
        FaqV2 faq = this.queryEntity(faqReq.getId());
        if (Objects.isNull(faq)) {
            return R.ok();
        }
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqReq.getTypeId());
        
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        BeanUtil.copyProperties(faqReq, faq);
        faq.setOpUser(SecurityUtils.getUid());
        faq.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.updateByPrimaryKeySelective(faq);
        return R.ok();
    }
    
    
    @Override
    public void removeByCategoryId(Long id) {
        FaqV2 faqV2 = new FaqV2();
        faqV2.setTypeId(id);
        List<FaqV2> faqs = faqV2Mapper.selectListByParams(faqV2);
        if (CollectionUtil.isNotEmpty(faqs)) {
            faqV2Mapper.removeByIds(faqs.stream().map(FaqV2::getId).collect(Collectors.toList()));
        }
        faqCategoryV2Mapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public void upDownBatch(AdminFaqUpDownReq faqUpDownReq) {
        if (CollectionUtil.isEmpty(faqUpDownReq.getIds())) {
            return;
        }
        
        FaqV2 faqV2 = new FaqV2();
        faqV2.setOnShelf(faqUpDownReq.getOnShelf());
        faqV2.setUpdateTime(System.currentTimeMillis());
        
        faqV2Mapper.batchUpdateByIds(faqV2, faqUpDownReq.getIds());
        
    }
    
    @Override
    public R changeTypeBatch(AdminFaqChangeTypeReq faqChangeTypeReq) {
        List<Long> ids = faqChangeTypeReq.getIds();
        if (CollectionUtil.isEmpty(ids)) {
            return R.ok();
        }
        
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqChangeTypeReq.getTypeId());
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId + ids.size()) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        
        FaqV2 faqV2 = new FaqV2();
        faqV2.setTypeId(faqChangeTypeReq.getTypeId());
        faqV2.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.batchUpdateByIds(faqV2, faqChangeTypeReq.getIds());
        return R.ok();
    }
    
    @Override
    public void removeByIds(List<Long> ids) {
        faqV2Mapper.removeByIds(ids);
    }
    
    /**
     * 查看常见问题
     */
    @Override
    public List<FaqVo> listFaqQueryResult(AdminFaqQuery faqQuery) {
        faqQuery.setTenantId(TenantContextHolder.getTenantId());
        FaqV2 faqV2 = new FaqV2();
        BeanUtils.copyProperties(faqQuery, faqV2);
        List<FaqV2BO> faqVos = faqV2Mapper.selectLeftJoinByParams(faqV2);
        
        if (CollectionUtil.isEmpty(faqVos)) {
            return null;
        }
        
        // 模糊返回全部
        if (!StringUtils.isEmpty(faqQuery.getTitle())) {
            return faqVos.stream().map(e -> {
                FaqVo faqVo = new FaqVo();
                BeanUtils.copyProperties(e, faqVo);
                return faqVo;
            }).sorted(Comparator.comparing(FaqVo::getTypeSort).thenComparing(FaqVo::getSort)).collect(Collectors.toList());
        }
        
        // 默认获取第一个分类
        Integer minSort = faqV2Mapper.selectMinimumSort(TenantContextHolder.getTenantId());
        long firstCategory = faqQuery.getTypeId() == null ? minSort : faqQuery.getTypeId();
        
        return faqVos.stream().filter(e -> Objects.equals(e.getTypeId(), firstCategory)).map(e -> {
            FaqVo faqVo = new FaqVo();
            BeanUtils.copyProperties(e, faqVo);
            return faqVo;
        }).sorted(Comparator.comparing(FaqVo::getTypeSort).thenComparing(FaqVo::getSort)).collect(Collectors.toList());
    }
    
    @Override
    public List<FaqVo> listFaqQueryToUser(AdminFaqQuery faqQuery) {
        List<FaqVo> faqVos = listFaqQueryResult(faqQuery);
        if (CollectionUtil.isEmpty(faqVos)) {
            return null;
        }
        return faqVos.stream().filter(e -> Objects.equals(e.getOnShelf(), FaqV2.SHELF_TYPE)).collect(Collectors.toList());
    }
    
    @Override
    public R updateFaqReqSort(AdminFaqReq faqReq) {
        FaqV2 faq = this.queryEntity(faqReq.getId());
        if (Objects.isNull(faq)) {
            return R.ok();
        }
        Integer countFaqReqByTypeId = faqV2Mapper.countFaqReqByTypeId(faqReq.getTypeId());
        
        if (FaqV2.SIMILAR_FAQ_LIMIT < countFaqReqByTypeId) {
            return R.fail("该类型下的常见问题不能超过" + FaqV2.SIMILAR_FAQ_LIMIT + "个");
        }
        faq.setSort(faqReq.getSort());
        faq.setId(faqReq.getId());
        faq.setOpUser(SecurityUtils.getUid());
        faq.setUpdateTime(System.currentTimeMillis());
        faqV2Mapper.updateByPrimaryKeySelective(faq);
        return R.ok();
    }
    
    @Override
    public Integer batchInsert(List<FaqV2> list) {
        return faqV2Mapper.batchInsert(list);
    }
    
    
    @Override
    public R queryDetail(Long id) {
        FaqVo faqVo = new FaqVo();
        FaqV2 faq = queryEntity(id);
        if (Objects.isNull(faq)) {
            return R.fail("此问题不存在");
        }
        BeanUtil.copyProperties(faq, faqVo);
        
        return R.ok(faqVo);
    }
    
    public FaqV2 queryEntity(Long id) {
        return faqV2Mapper.selectByPrimaryKey(id);
    }
}
