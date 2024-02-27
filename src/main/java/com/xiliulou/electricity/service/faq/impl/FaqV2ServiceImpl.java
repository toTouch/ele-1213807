package com.xiliulou.electricity.service.faq.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.faq.FaqCategoryV2Mapper;
import com.xiliulou.electricity.mapper.faq.FaqV2Mapper;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.faq.FaqListVos;
import com.xiliulou.electricity.vo.faq.FaqVo;
import lombok.AllArgsConstructor;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private final FaqV2Mapper faqMapper;
    
    private final FaqCategoryV2Mapper faqCategoryMapper;
    
    @Override
    public void add(AdminFaqReq faqReq) {
        FaqV2 faq = BeanUtil.toBean(faqReq, FaqV2.class);
        faq.setTenantId(TenantContextHolder.getTenantId())
                .setOpUser(SecurityUtils.getUid())
                .setOnShelf(UpDownEnum.DOWN.getCode())
                .setCreateTime(System.currentTimeMillis())
                .setUpdateTime(System.currentTimeMillis());
        faqMapper.insert(faq);
    }
    
    @Override
    public void edit(AdminFaqReq faqReq) {
        FaqV2 faq = this.queryEntity(faqReq.getId());
        BeanUtil.copyProperties(faqReq, faq);
        faq.setOpUser(SecurityUtils.getUid()).setUpdateTime(System.currentTimeMillis());
        faqMapper.updateByPrimaryKeySelective(faq);
    }
    
    @Override
    public void removeByCategoryId(Long id) {
        
        Map<String, Object> map = new HashMap<>();
        map.put("typeId", id);
        List<FaqV2> faqs = faqMapper.selectListByParams(map);
        if (CollectionUtil.isNotEmpty(faqs)) {
            faqMapper.removeByIds(faqs.stream().map(FaqV2::getId).collect(Collectors.toList()));
        }
        faqCategoryMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public void upDownBatch(AdminFaqUpDownReq faqUpDownReq) {
        Map<String, Object> map = new HashMap<>();
        map.put("idList", faqUpDownReq.getIds());
        List<FaqV2> faqs = faqMapper.selectListByParams(map);
        if (CollectionUtil.isEmpty(faqs)) {
            return;
        }
        faqs.stream().forEach(e -> {
            e.setOnShelf(faqUpDownReq.getOnShelf().getCode());
            e.setUpdateTime(System.currentTimeMillis());
            faqMapper.updateByPrimaryKeySelective(e);
        });
        
    }
    
    @Override
    public void changeTypeBatch(AdminFaqChangeTypeReq faqChangeTypeReq) {
        Map<String, Object> map = new HashMap<>();
        map.put("idList", faqChangeTypeReq.getIds());
        List<FaqV2> faqs = faqMapper.selectListByParams(map);
        if (CollectionUtil.isEmpty(faqs)) {
            return;
        }
        faqs.stream().forEach(e -> {
            e.setTypeId(faqChangeTypeReq.getTypeId());
            e.setUpdateTime(System.currentTimeMillis());
            faqMapper.updateByPrimaryKeySelective(e);
        });
    }
    
    @Override
    public void removeByIds(List<Long> ids) {
        faqMapper.removeByIds(ids);
    }
    
    @Override
    public List<FaqListVos> page(AdminFaqQuery faqQuery) {
        if (Objects.isNull(faqQuery.getTenantId())){
            faqQuery.setTenantId(TenantContextHolder.getTenantId());
        }
        
        Map<String, Object> map = beanToMap(faqQuery);
        List<FaqVo> faqVos = faqMapper.selectLeftJoinByParams(map);
        
        if (CollectionUtil.isNotEmpty(faqVos)) {
            Map<Long, List<FaqVo>> listMap = faqVos.stream().collect(Collectors.groupingBy(FaqVo::getId));
            //三种情况，1.默认页面，展示第一个 2.选择了分类,展示选择的分类 3.搜索了关键字
            List<FaqListVos> faqListVosList = listMap.entrySet().stream().map(e -> {
                FaqListVos faqListVos = new FaqListVos();
                faqListVos.setId(e.getKey());
                faqListVos.setType(e.getValue().stream().findAny().get().getType());
                faqListVos.setSort(e.getValue().stream().findAny().get().getTypeSort());
                long count = e.getValue().stream().filter(x -> Objects.nonNull(x.getTypeId())).count();
                faqListVos.setCount((int) count);
                return faqListVos;
            }).collect(Collectors.toList());
            CollectionUtil.sort(faqListVosList, Comparator.comparing(FaqListVos::getSort));
            
            if (null == faqQuery.getTypeId()) {
                //选择排序第一个来进行，分页查询
                map.put("typeId", faqListVosList.stream().findFirst().get().getId());
            }
            
            //通过typeId和其他参数来进行分页查询，然后赋值给对应typeId的集合
            List<FaqV2> faqs = faqMapper.selectListByParamsPage(map, faqQuery.getOffset(), faqQuery.getSize());
            if (CollectionUtil.isNotEmpty(faqs)) {
                List<FaqVo> faqVoLists = faqs.stream().map(e -> {
                    FaqVo faqVo = new FaqVo();
                    BeanUtil.copyProperties(e,faqVo,"onShelf");
                    faqVo.setOnShelf(UpDownEnum.getUpDownEnum(e.getOnShelf()));
                    return faqVo;
                }).collect(Collectors.toList());
                
                faqListVosList.stream().filter(e -> e.getId().equals(map.get("typeId"))).findAny().ifPresent(e -> e.setFaqVoList(faqVoLists));
            }
            return faqListVosList;
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public FaqVo detail(Long id) {
        FaqVo faqVo = new FaqVo();
        FaqV2 faq = queryEntity(id);
        BeanUtil.copyProperties(faq, faqVo, "onShelf");
        faqVo.setOnShelf(UpDownEnum.getUpDownEnum(faq.getOnShelf()));
        return faqVo;
    }
    
    public FaqV2 queryEntity(Long id) {
        FaqV2 faq = faqMapper.selectByPrimaryKey(id);
        if (null == faq) {
            throw new BizException("300000", "数据有误");
        }
        return faq;
    }
    
    
    public Map<String, Object> beanToMap(AdminFaqQuery bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }
}
