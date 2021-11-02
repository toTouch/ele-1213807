## 电池信息上报(回调) 

    +  batteryName
    +  soc
    +  chargeStatus
    +  reportTime
    +  batteryOtherProperties

两个地方回调，接受到电池信息上报的时候，和gps回掉的时候

## 柜子格挡信息上报(回调) 

+ cellNo
+ isLock
+ isFanOpen
+ isHeat
+ lightStatus
+ temp

## 柜机信息上报

+ version

+ status
+ productKey
+ deviceName
+ lastOnlineTime

## 租电

+ requestId

+ orderId

## 租电回调

+ requestId
+ orderId
+ cellNo
+ status
+ msg
+ isException
+ rentBatteryName
+ reportTime

## 退电

+ requestId
+ orderId

## 退电回调

+ requestId
+ orderId
+ cellNo
+ status
+ msg
+ isException
+ returnBatteryName
+ reportTime

## 换电

+ requestId
+ orderId

## 换电回调

orderId
placCellNo
placeBatteryName
placeIsModelType
placeMultiBatteryModelName
takeCellNo



+ requestId
+ orderId
+ placeCellNo
+ placeBatteryName
+ takeCellNo
+ takeBatteryName
+ reportTime
+ msg
+ isException

## 错误信息上报

+ errorMsg
+ cellNo
+ type
+ createTime

## 查询柜机信息

+ productKey
+ deviceName

返回：

+ name
+ address
+ p d
+ status
+ batteryInfo
+ cellInfo

## 查询电池信息

batterName

soc

chargeStatus

reportTime

batteryOtherProperties

## 查询指定格挡信息

+ cellNo
+ isLock
+ isFanOpen
+ isHeat
+ lightStatus
+ temp









附录：

所有操作记录： 
    上报： api_exchange_order_operate_record


换电：
  输入：api_exchange_battery
	orderId
	placCellNo
	placeBatteryName
	placeIsModelType
	placeMultiBatteryModelName
	takeCellNo
  输出： api_exchange_battery_rsp
    public String sessionId;
    public String productKey;
    //订单id
    public String orderId;
    //正确或者错误信息，当isProcessFail使用该msg
    public String msg;
    //订单状态
    public String orderStatus;
    //订单状态序号
    private Double orderSeq;
    //是否需要结束订单
    private Boolean isException;
    //创建时间
    private Long reportTime;

    private Integer placeCellNo;
    
    private String placeBatteryName;
    
    private String takeBatteryName;
    
    private Integer takeCellNo;

   订单状态：
       EXCEPTION_ORDER(100.0),
    /**
     * 放入电池开门失败
     */
    PLACE_OPEN_FAIL(100.0),

    /**
     * 放入电池超时
     */
    PLACE_TIME_OUT(100.0),
    /**
     * 放入电池不匹配
     */
    PLACE_BATTERY_NOT_MATCH(100.0),
    /**
     * 放入电池不属于系统
     */
    PLACE_BATTERY_NOT_BELONG(100.0),
    /**
     * 换电柜放入没电电池开门成功
     */
    PLACE_OPEN_SUCCESS(1.0),
    /**
     * 换电柜检测没电电池成功
     */
    PLACE_BATTERY_CHECK_SUCCESS(2.0),
    /**
     * 换电柜开满电电池仓门成功
     */
    TAKE_OPEN_SUCCESS(3.0),
    
    /**
     * 取电池超时
     */
    TAKE_TIME_OUT(100.0),
    /**
     * 取电池开门失败
     */
    TAKE_OPEN_FAIL(100.0),
    /**
     * 换电柜满电电池成功取走，流程结束
     */
    TAKE_BATTERY_SUCCESS(4.0);

还电：
  输入： api_return_battery
  orderId
  cellNo
  isModelType 
  multiBatteryModelName
  returnBatteryName

  输出：
     api_return_battery_rsp
    public String sessionId;
    public String productKey;
    //订单id
    public String orderId;
    //正确或者错误信息，当isProcessFail使用该msg
    public String msg;
    //订单状态
    public String orderStatus;
    //订单状态序号
    private Double orderSeq;
    //是否需要结束订单
    private Boolean isException;
    //创建时间
    private Long reportTime;

    private String rentBatteryName;

   订单状态：
       /**
     * 异常订单
     */
    EXCEPTION_ORDER(100.0),
    /**
     * 开门失败
     */
    RETURN_OPEN_FAIL(100.0),

    /**
     * 还电超时
     */
    
    RETURN_TIME_OUT(100.0),
    
    /**
     * 放入电池不属于系统
     */
    
    RETURN_BATTERY_NOT_BELONG(100.0),
    /**
     * 还电池开门成功
     */
    RETURN_OPEN_SUCCESS(1.0),
    
    /**
     * 换电池检测电池不匹配
     */
    RETURN_BATTERY_NOT_MATCH(100.0),
    /**
     * 还电池成功
     */
    RETURN_BATTERY_CHECK_SUCCESS(2.0);

租电：
  输入： api_rent_battery
    orderId
    cellNo
  输出：api_rent_battery_rsp
      public String sessionId;
    public String productKey;
    //订单id
    public String orderId;
    //正确或者错误信息，当isProcessFail使用该msg
    public String msg;
    //订单状态
    public String orderStatus;
    //订单状态序号
    private Double orderSeq;
    //是否需要结束订单
    private Boolean isException;
    //创建时间
    private Long reportTime;

    private String rentBatteryName;

   订单状态：
    
  /**
     * 异常订单
     */
    EXCEPTION_ORDER(100.0),
    /**
     * 开门失败
     */
    RENT_OPEN_FAIL(100.0),
    /**
     * 租电池超时
     */
    RENT_TIME_OUT(100.0),

    /**
     * 租电池开门成功
     */
    RENT_OPEN_SUCCESS(1.0),
    /**
     * 租电池成功取走
     */
    RENT_BATTERY_TAKE_SUCCESS(2.0);