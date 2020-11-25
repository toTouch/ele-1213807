package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.entity.Provincial;
import com.xiliulou.electricity.service.ProvincialService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (Provincial)表控制层
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
@RestController
@RequestMapping("provincial")
public class ProvincialController {
    /**
     * 服务对象
     */
    @Resource
    private ProvincialService provincialService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public Provincial selectOne(Integer id) {
        return this.provincialService.queryById(id);
    }

}