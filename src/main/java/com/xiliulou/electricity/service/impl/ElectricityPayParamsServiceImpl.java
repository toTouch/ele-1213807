package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:36
 **/
@Service
@Slf4j
public class ElectricityPayParamsServiceImpl extends ServiceImpl<ElectricityPayParamsMapper, ElectricityPayParams> implements ElectricityPayParamsService {

    @Autowired
    RedisService redisService;

    @Autowired
    WechatConfig config;

    /**
     * 新增或修改
     *
     * @param electricityPayParams
     * @return
     */
    @Override
    public R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams, MultipartFile file) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //加锁
        Boolean getLockerSuccess = redisService.setNx(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY,
                String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }


        ElectricityPayParams oldElectricityPayParams = queryFromCache(tenantId);

        String fileName = file.getOriginalFilename();
        String path = config.getMchCertificateDirectory() + fileName;

        try (InputStream inputStream = file.getInputStream(); FileOutputStream fileOutputStream = new FileOutputStream(path)) {
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, length);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            log.error("LOCKER ERROR! save config error! tenantId={}", tenantId, e);
            throw new CustomBusinessException("保存私钥文件失败！");
        }

        electricityPayParams.setUpdateTime(System.currentTimeMillis());
        if (Objects.isNull(oldElectricityPayParams)) {
            electricityPayParams.setCreateTime(System.currentTimeMillis());
            electricityPayParams.setTenantId(tenantId);
            baseMapper.insert(electricityPayParams);
            redisService.delete(ElectricityCabinetConstant.CACHE_PAY_PARAMS);
            redisService.delete(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY);

            return R.ok();
        } else {
            if (ObjectUtil.notEqual(oldElectricityPayParams.getId(), electricityPayParams.getId())) {
                return R.fail("请求参数id,不合法!");
            }
            redisService.delete(ElectricityCabinetConstant.CACHE_PAY_PARAMS);
            baseMapper.updateById(electricityPayParams);
            redisService.delete(ElectricityCabinetConstant.ADMIN_OPERATE_LOCK_KEY);
            return R.ok();
        }
    }

    /**
     * 获取支付参数
     * valid_days
     *
     * @return
     */
    @Override
    @DS("slave_1")
    public ElectricityPayParams queryFromCache(Integer tenantId) {
        ElectricityPayParams electricityPayParams = redisService.getWithHash(ElectricityCabinetConstant.CACHE_PAY_PARAMS, ElectricityPayParams.class);
        if (Objects.isNull(electricityPayParams)) {
            electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getTenantId,tenantId));
            if (Objects.nonNull(electricityPayParams)) {
                redisService.saveWithHash(ElectricityCabinetConstant.CACHE_PAY_PARAMS, electricityPayParams);
            }
        }
        return electricityPayParams;
    }
}
