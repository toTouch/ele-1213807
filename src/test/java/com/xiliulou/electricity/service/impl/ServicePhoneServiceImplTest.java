package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.ElectricityCabinetApplication;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.request.ServicePhonesRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HeYafeng
 * @description TODO
 * @date 2024/10/30 20:30:35
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElectricityCabinetApplication.class)
@ActiveProfiles("dev")
public class ServicePhoneServiceImplTest {
    
    @Resource
    private ServicePhoneServiceImpl servicePhoneService;
    
    @Test
    public void insertOrUpdate() {
    
        
        List<ServicePhoneRequest> phoneList = new ArrayList<>();
        phoneList.add(ServicePhoneRequest.builder().id(3L).phone("1381234563").remark("测试").build());
    
        servicePhoneService.insertOrUpdate(ServicePhonesRequest.builder().phoneList(phoneList).build());
    }
}