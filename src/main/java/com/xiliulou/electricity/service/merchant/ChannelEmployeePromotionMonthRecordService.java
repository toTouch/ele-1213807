package com.xiliulou.electricity.service.merchant;


import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionMonthRecord;
import com.xiliulou.electricity.request.merchant.ChannelEmployeePromotionRequest;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeePromotionVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/21 14:02
 * @desc
 */
public interface ChannelEmployeePromotionMonthRecordService {
    
    void batchInsert(List<ChannelEmployeePromotionMonthRecord> list);
    
    List<ChannelEmployeePromotionVO> listByPage(ChannelEmployeePromotionRequest channelEmployeeRequest);
    
    Integer countTotal(ChannelEmployeePromotionRequest channelEmployeeRequest);
    
    void export(String monthDate, HttpServletResponse response, Long franchiseeId);
}
