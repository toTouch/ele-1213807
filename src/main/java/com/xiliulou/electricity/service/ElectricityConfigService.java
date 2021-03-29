package com.xiliulou.electricity.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityConfig;


/**
 * 用户列表(ElectricityConfig)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface ElectricityConfigService extends IService<ElectricityConfig> {

    R edit(String name,Integer orderTime);

    ElectricityConfig queryOne();
}