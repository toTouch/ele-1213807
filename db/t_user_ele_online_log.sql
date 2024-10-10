create table t_user_ele_online_log(
    id bigint primary_key,
    eid int not null comment '换电柜ID',
    `client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端IP',
    `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'offline下线，online上线',
    `msg` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息内容',
    `appear_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上报时间',
    `create_time` bigint NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_cid` (`eid`)
);