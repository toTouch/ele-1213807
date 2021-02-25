package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.mapper.EleAuthEntryMapper;
import com.xiliulou.electricity.service.EleAuthEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleAuthEntry queryByIdFromDB(Long id) {
        return this.eleAuthEntryMapper.selectById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleAuthEntry queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleAuthEntry insert(EleAuthEntry eleAuthEntry) {
        this.eleAuthEntryMapper.insert(eleAuthEntry);
        return eleAuthEntry;
    }

    /**
     * 修改数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleAuthEntry eleAuthEntry) {
       return this.eleAuthEntryMapper.updateById(eleAuthEntry);
         
    }

    @Override
    public R batchInsertAuthEntry(List<EleAuthEntry> eleAuthEntryList) {
        List<EleAuthEntry> eleAuthEntryListWillInsertList = new ArrayList<>(eleAuthEntryList.size());
        for (EleAuthEntry eleAuthEntry : eleAuthEntryList) {
            if (ObjectUtil.isEmpty(eleAuthEntry.getType()) || !this.checkAuthEntryTypeAllowable(eleAuthEntry.getType())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            eleAuthEntryListWillInsertList.add(eleAuthEntry);
        }

        Long effectRows = eleAuthEntryListWillInsertList.parallelStream().map(e -> {
            e.setCreateTime(System.currentTimeMillis());
            e.setUpdateTime(System.currentTimeMillis());
            e.setDelFlag(EleAuthEntry.DEL_NORMAL);

            return eleAuthEntryMapper.insert(e);
        }).count();

        if (effectRows < eleAuthEntryListWillInsertList.size()) {
            log.error("insert size is  more than insert effectRows ");
            return R.fail("SYSTEM.0001","系统错误!");
        }
        return R.ok();
    }

    @Override
    public R updateEleAuthEntries(List<EleAuthEntry> eleAuthEntryList) {
        for (EleAuthEntry eleAuthEntry : eleAuthEntryList) {
            if (ObjectUtil.isEmpty(eleAuthEntry.getId())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            if (ObjectUtil.isNotEmpty(eleAuthEntry.getType()) && !this.checkAuthEntryTypeAllowable(eleAuthEntry.getType())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            eleAuthEntry.setUpdateTime(System.currentTimeMillis());
            eleAuthEntryMapper.updateById(eleAuthEntry);
            redisService.deleteKeys(ElectricityCabinetConstant.ELE_CACHE_AUTH_ENTRY + eleAuthEntry.getId());
        }
        return R.ok();
    }

    @Override
    public  List<EleAuthEntry> getEleAuthEntriesList() {
        return eleAuthEntryMapper.selectList(new LambdaQueryWrapper<EleAuthEntry>().eq(EleAuthEntry::getDelFlag,EleAuthEntry.DEL_NORMAL));
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