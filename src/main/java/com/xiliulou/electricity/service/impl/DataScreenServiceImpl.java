package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.DataScreenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据大屏service
 *
 * @author hrp
 * @since 2022-03-22 10:56:56
 */

@Service("dataScreenService")
@Slf4j
public class DataScreenServiceImpl implements DataScreenService {
    @Override
    public R queryOrderStatistics(Integer tenantId) {
        return null;
    }
}
