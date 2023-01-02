package com.xiliulou.electricity.config;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.xiliulou.electricity.config.sentinel.CustomSentinelRetrofitDegrade;
import com.xiliulou.electricity.exception.NotFoundServiceInstanceException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2022/12/29 09:32
 */
@Configuration
public class RetrofitMicroServiceConfig {
    @Bean
    public ServiceInstanceChooser serviceInstanceChooser(LoadBalancerClient loadBalancerClient) {
        return new SpringCloudServiceInstanceChooser(loadBalancerClient);
    }
    
    @Bean
    public RetrofitDegrade retrofitSentinelRetrofitDegrade(RetrofitProperties properties) {
        return new CustomSentinelRetrofitDegrade(properties.getDegrade().getGlobalSentinelDegrade());
    }
    
}

class SpringCloudServiceInstanceChooser implements ServiceInstanceChooser {
    private LoadBalancerClient loadBalancerClient;
    
    public SpringCloudServiceInstanceChooser(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }
    @Override
    public URI choose(String serviceId) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        if(Objects.isNull(serviceInstance))  {
            throw new NotFoundServiceInstanceException("not found service:" + serviceId);
        }
        return serviceInstance.getUri();
    }
}