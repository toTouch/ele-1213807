package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.FranchiseeAmount;
import com.xiliulou.electricity.mapper.EleAuthEntryMapper;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 实名认证资料项(TEleAuthEntry)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
@Service("eleAuthEntryService")
@Slf4j
public class EleAuthEntryServiceImpl implements EleAuthEntryService {
    @Resource
    EleAuthEntryMapper eleAuthEntryMapper;

    @Autowired
    RedisService redisService;


        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleAuthEntry queryByIdFromCache(Integer id) {
        EleAuthEntry eleAuthEntry =redisService.getWithHash(CacheConstant.ELE_CACHE_AUTH_ENTRY  + id, EleAuthEntry.class);
        if (Objects.isNull(eleAuthEntry)) {
            eleAuthEntry = this.eleAuthEntryMapper.selectById(id);
            if (Objects.nonNull(eleAuthEntry)) {
                redisService.saveWithHash(CacheConstant.ELE_CACHE_AUTH_ENTRY  + id, eleAuthEntry);
            }
        }
        return eleAuthEntry;
    }


    @Override
    public R updateEleAuthEntries(List<EleAuthEntry> eleAuthEntryList) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        for (EleAuthEntry eleAuthEntry : eleAuthEntryList) {
            if (ObjectUtil.isEmpty(eleAuthEntry.getId())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            if (!Objects.equals(eleAuthEntry.getTenantId(),tenantId)) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            if (ObjectUtil.isNotEmpty(eleAuthEntry.getType()) && !this.checkAuthEntryTypeAllowable(eleAuthEntry.getType())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            eleAuthEntry.setUpdateTime(System.currentTimeMillis());
            eleAuthEntryMapper.update(eleAuthEntry);
            redisService.delete(CacheConstant.ELE_CACHE_AUTH_ENTRY + eleAuthEntry.getId());
        }
        return R.ok();
    }

    @Slave
    @Override
    public  List<EleAuthEntry> getEleAuthEntriesList(Integer tenantId) {
        return eleAuthEntryMapper.selectList(new LambdaQueryWrapper<EleAuthEntry>().eq(EleAuthEntry::getDelFlag,EleAuthEntry.DEL_NORMAL).eq(EleAuthEntry::getTenantId,tenantId));
    }

    @Override
    public List<EleAuthEntry> getUseEleAuthEntriesList(Integer tenantId) {
        return eleAuthEntryMapper.selectList(new LambdaQueryWrapper<EleAuthEntry>().eq(EleAuthEntry::getIsUse,EleAuthEntry.IS_USE).eq(EleAuthEntry::getDelFlag,EleAuthEntry.DEL_NORMAL).eq(EleAuthEntry::getTenantId,tenantId));
    }

    @Override
    public void insertByTenantId(Integer tenantId) {
        EleAuthEntry eleAuthEntry=new EleAuthEntry();
        eleAuthEntry.setIsUse(EleAuthEntry.IS_USE);
        eleAuthEntry.setCreateTime(System.currentTimeMillis());
        eleAuthEntry.setUpdateTime(System.currentTimeMillis());
        eleAuthEntry.setDelFlag(EleAuthEntry.DEL_NORMAL);
        eleAuthEntry.setTenantId(tenantId);

        //真实姓名
        eleAuthEntry.setName("真实姓名");
        eleAuthEntry.setType("input");
        eleAuthEntry.setIdentify(EleAuthEntry.ID_NAME_ID);
        eleAuthEntryMapper.insert(eleAuthEntry);

        //身份证号
        eleAuthEntry.setName("身份证号");
        eleAuthEntry.setIdentify(EleAuthEntry.ID_ID_CARD);
        eleAuthEntryMapper.insert(eleAuthEntry);

        //身份证正面照片
        eleAuthEntry.setName("身份证正面照片");
        eleAuthEntry.setType("file");
        eleAuthEntry.setIdentify(EleAuthEntry.ID_CARD_FRONT_PHOTO);
        eleAuthEntryMapper.insert(eleAuthEntry);

        //身份证反面照片
        eleAuthEntry.setName("身份证反面照片");
        eleAuthEntry.setType("file");
        eleAuthEntry.setIdentify(EleAuthEntry.ID_CARD_BACK_PHOTO);
        eleAuthEntryMapper.insert(eleAuthEntry);

        //自拍照片
        eleAuthEntry.setName("自拍照片");
        eleAuthEntry.setType("file");
        eleAuthEntry.setIdentify(EleAuthEntry.ID_SELF_PHOTO);
        eleAuthEntryMapper.insert(eleAuthEntry);

    }

    /**
     * 检查资料类型是否合法
     *
     */
    public Boolean checkAuthEntryTypeAllowable(String authType) {
        Boolean allowAble = Boolean.FALSE;
        switch (authType) {
            case "input":
                allowAble = Boolean.TRUE;
                break;
            case "select":
                allowAble = Boolean.TRUE;
                break;
            case "radio":
                allowAble = Boolean.TRUE;
                break;
            case "file":
                allowAble = Boolean.TRUE;
                break;

        }
        return allowAble;
    }
}
