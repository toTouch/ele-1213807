/**
 *  Create date: 2024/7/23
 */

package com.xiliulou.electricity.service.template;

import com.alipay.api.AlipayApiException;
import com.xiliulou.electricity.dto.BatteryPowerNotifyDto;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.pay.base.exception.PayException;

import java.util.Map;
import java.util.function.Function;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 18:14
 */
public interface MiniTemplateMsgBizService {
    
    
    /**
     * 低电提醒
     *
     * @author caobotao.cbt
     * @date 2024/7/24 11:34
     */
    
    boolean sendLowBatteryReminder(Integer tenantId, Long uid, String soc, String sn);
    
    
    /**
     * 套餐过期
     *
     * @param tenantId
     * @param uid
     * @param cardName
     * @param memberCardExpireTimeStr
     * @author caobotao.cbt
     * @date 2024/7/24 14:37
     */
    boolean sendBatteryMemberCardExpiring(Integer tenantId, Long uid, String cardName, String memberCardExpireTimeStr);
}