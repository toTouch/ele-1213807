package com.xiliulou.electricity.handler.exchange;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class HandlerController {
    
    private Map<Integer, Handler> handlers = new HashMap<>(10);
    
    public void putHandler(Integer reasonCode, Handler handler) {
        handlers.put(reasonCode, handler);
    }
    
    public Handler getRouteHandler(Integer reasonCode) {
        return handlers.get(reasonCode);
    }
}
