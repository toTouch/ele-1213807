package com.xiliulou.electricity.service;

import org.apache.commons.lang3.tuple.Triple;

public interface ActivityService {
    Triple<Boolean, String, Object> userActivityInfo();
}
