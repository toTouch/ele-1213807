public enum ExchangeOrderStatus {
    /**
     * 换电过程放入没电电池检测失败
     */
    INIT_CHECK_FAIL(1.1),
    /**
     * 换电柜放入没电电池开门发现有电池存在
     */
    INIT_CHECK_BATTERY_EXISTS(2.1),
    /**
     * 换电柜放入没电电池开门成功
     */
    INIT_OPEN_SUCCESS(3.0),
    /**
     * 换电柜放入没电电池开门失败
     */
    INIT_OPEN_FAIL(3.1),
    /**
     * 换电柜检测没电电池成功
     */
    INIT_BATTERY_CHECK_SUCCESS(4.0),
    /**
     * 换电柜检测没电电池失败
     */
    INIT_BATTERY_CHECK_FAIL(4.1),
    /**
     * 换电柜检测没电电池超时
     */
    INIT_BATTERY_CHECK_TIMEOUT(4.2),

    /**
     * 换电柜开满电电池前置检测失败
     */
    COMPLETE_CHECK_FAIL(5.1),
    /**
     * 换电柜开满电电池发现电池不存在
     */
    COMPLETE_CHECK_BATTERY_NOT_EXISTS(5.2),
    /**
     * 换电柜开满电电池仓门成功
     */
    COMPLETE_OPEN_SUCCESS(6.0),
    /**
     * 换电柜开满电电池仓门失败
     */
    COMPLETE_OPEN_FAIL(6.1),
    /**
     * 换电柜满电电池成功取走，流程结束
     */
    COMPLETE_BATTERY_TAKE_SUCCESS(7.0),
    /**
     * 换电柜取走满电电池超时
     */
    COMPLETE_BATTERY_TAKE_TIMEOUT(7.1);

    private Double statusSeq;

    ExchangeOrderStatus(Double statusSeq) {
        this.statusSeq = statusSeq;
    }

    public Double getStatusSeq() {
        return statusSeq;
    }


}
