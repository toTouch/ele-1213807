package com.xiliulou.electricity.service.process;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @ClassName: ExchangeEndOrderAssertProcess
 * @description: 柜机校验处理器
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("exchangeCabinetAssertProcess")
@Slf4j
public class ExchangeCabinetAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private StoreService storeService;
    
    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        Integer eid = context.getProcessModel().getEid();
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet)) {
            breakChain(context, "100003", "柜机不存在");
            return;
        }
        
        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            breakChain(context, "100203", "换电柜已打烊");
            return;
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            breakChain(context, "100004", "柜机不在线");
            return;
        }
        
        UserInfo userInfo = context.getProcessModel().getUserInfo();
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.warn("ORDER WARN!  not found store ！uid={},eid={},storeId={}", userInfo.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
            breakChain(context, "100204", "未找到门店");
            return;
        }
        
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.warn("ORDER WARN! storeId  is not equal franchiseeId uid={} , store's fid={} ,fid={}", userInfo.getUid(), store.getFranchiseeId(), userInfo.getFranchiseeId());
            breakChain(context, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            return;
        }
        context.getProcessModel().getChainObject().setElectricityCabinet(electricityCabinet);
    }
    
    
    public boolean isBusiness(ElectricityCabinet electricityCabinet) {
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public Long getTime(Long time) {
        Date date1 = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date1);
        Date date2 = null;
        try {
            date2 = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long ts = date2.getTime();
        return time - ts;
    }
}
