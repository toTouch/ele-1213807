package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.service.StoreService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class StoreAdminController {
    /**
     * 服务对象
     */
    @Resource
    private StoreService tStoreService;

}