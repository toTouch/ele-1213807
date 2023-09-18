package com.xiliulou.electricity.factory.paycallback;

import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信退款工厂
 *
 * @author xiaohui.song
 **/
@Component
public class WxRefundPayServiceFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext appContext;

    private static final Map<String, WxRefundPayService> WX_REFUND_PAY_SERVICE_MAP = new HashMap<>();

    public static WxRefundPayService getService(String optType) {
        return WX_REFUND_PAY_SERVICE_MAP.get(optType);
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        appContext.getBeansOfType(WxRefundPayService.class).values().forEach(s -> {
            WX_REFUND_PAY_SERVICE_MAP.put(s.getOptType(), s);
        });
    }

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     * <p>Invoked after population of normal bean properties but before an init callback such
     * as {@link InitializingBean#afterPropertiesSet()}
     * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
     * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
     * {@link MessageSourceAware}, if applicable.
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws ApplicationContextException in case of context initialization errors
     * @throws BeansException              if thrown by application context methods
     * @see BeanInitializationException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }
}
