package com.xiliulou.electricity.query.faq;

import lombok.Data;

import java.util.List;

@Data
public class AdminFaqQuery {
    
    private Long typeId;
    private String title;
    private Integer tenantId;
    private Long size;
    private Long offset;
}
