package com.xiliulou.electricity.callback.impl.business;


import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Description: This class is CarBusinessHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
@Service
public class CarBusinessHandler implements BusinessHandler {
    
    @Override
    public boolean freeDeposit(FreeDepositOrder order) {
        return false;
    }
    
    @Override
    public boolean unfreeDeposit(FreeDepositOrder order) {
        return false;
    }
    
    @Override
    public boolean withholdDeposit(FreeDepositOrder order) {
        return false;
    }
}
