package com.xiliulou.electricity.service.supper;

import cn.hutool.core.lang.Pair;
import com.xiliulou.electricity.query.supper.UserGrantSourceReq;

import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
public interface AdminSupperService {
    
    /**
     * 根据电池SN删除电池
     * @param tenantId 租户ID
     * @param batterySnList 电池SN集
     * @param violentDel 是否暴力删除
     * @return Pair<已删除的电池编码、未删除的电池编码>
     */
    Pair<List<String>, List<String>> delBatteryBySnList(Integer tenantId, List<String> batterySnList, Integer violentDel);
    
    /**
     * <p>
     *     内部接口,授予用户权限,默认所有租户
     *    Description: grantPermission
     * </p>
     * @param userGrantSourceReq userGrantSourceReq
     * @return void
     * <p>Project: AdminSupperService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/5/21
    */
    void grantPermission(UserGrantSourceReq userGrantSourceReq);
}
