# revised version

|version|date|describe|reviser|
|:---|:---:|:---:|:---:|
|v1.0.0|    2020-09-18|    Basic version of the smart cabinet API document on the sixth floor of the West|    YG|
|v1.0.1|    2020-11-9    |Add control log options, increase log compression, and optimize some code. |DY|
|v1.0.2|    2020-12-7    |Add color lights, weighing sensors and ping	|DY|
|v1.0.8|    2021-6-30    |Some bug fixes, turn on the lights, turn on disinfection, turn on heating of the old N and old S protocols	|DY|
|v1.0.9|    2021-7-6    |Delete the old N and old S protocols, add synchronization operations and pause polling to check the block status. |DY|
---

# 1. Introduction

## 1.1 Design Objectives
This document defines the API of the smart cabinet APP's call to the underlying hardware and supports all communication
board protocols that currently exist on the market.

# 2 API
## 1.2 Scope of application
The scope of application of the document is all smart cabinet Android developers.

## 2.1 Initialization
### 2.1.1 Instructions

Initialize the SDK at the program entrance and set the callback interface. The code can refer to the demo (directory:)

The initialization process is divided into the following three steps:
- Get an instance of CupboardHandler 
- Initialize CupboardHandler
- Set notification callback monitoring

>Note:
>>1. Initializing sdk is the prerequisite for calling other functions (opening the door, turning on the light, etc.). If
   the initialization is unsuccessful, the subsequent interfaces cannot be called.
>>2. All API methods are not applicable to all protocol boards. For specific APIs supported by the protocol board, you can
   ask the protocol board provider.
>>3. Principle of callback monitoring: There is a timing task at the bottom of the sdk. Only when the block door is open,
   the block state will be constantly checked in rotation. When the block state changes, it will be called back. In
   version 1.0.9 and above, a switch is provided to control the turn on this polling. It is turned on by default. If it
   is turned off, You can actively query the block status without relying on the underlying sdk timing task. When the
   block status changes, it will be callback.

### 2.1.2 The initialization sample code is as follows:
```java
//1. Get the example of CupboardHandler;
cupboardHandler = CupboardHandler.getInstance();
//2. Initialize cupboardHandler (protocol, serial port, etc., overloaded method init method, unset parameters will have default values) getDynamicCellConfig() is the code to get the block.
cupboardHandler.init(getApplicationContext(), CupboardControlProtocol.PROTOCOL_S, "ttyS4", getDynami cCellConfig());
//3. set a callback.
cupboardHandler.setCabinetListener(new CabinetListener() {
    @Override
    public void onCabinetStatusCallback(int cell, int msg, int errorCode) {
    // Callbacks occur when the state of the grid changes.
    }
    @Override
    public void onCommCallback(String s) {
    // The invocation and debugging information related to the underlying layer will be called back through this method.
    }
   @Override
   public void onErrorCallBack(String s) {
   // The underlying error message
    }
});
```
### 2.1.3init function parameter description
CupboardHandler's init() function provides an overload method, and different initialization parameters can be selected
according to the cabinet protocol board.

|PARAMETERS |interpretation |notes |
|:---|:---:|:---:|
|context| context|| 
|CupboardControlProtocol| communication agreement| Enumeration class (five communication protocols are provided, and the specific use protocol is used to ask the smart cabinet manufacturer)defaults to the new S protocol.|
|serialPort| communication serial port |Default is ttyS4|
|DynamicCellConfig| Configure grid objects| The default configuration is two cabinets, each with 16 blocks, regardless of size grids.|


>Note:
>>At present, there are 4 protocols, old N, old S, new N, new S. When the sdk is initialized, only the new S and new N protocols can
immediately obtain all the statuses of the control board (door, disinfection, lights, temperature, item detection). For
old N and old S, only the status of the door can be obtained for historical reasons, and other The state cannot be
obtained immediately, and the state can only be obtained by triggering the response status command. For example, the
initial state of the control panel is on, the door state is on, and the disinfection is on. Then the new S and new N
protocols can be read to all states immediately when initialized, but the old N and old S can only read that the state
of the door is on, and other lights and disinfection read on, only when sending the light on or off the light. Command
to get the correct state.

### 2.1.4 Device Status Monitoring

SDK provides the monitoring implementation of cabinet status (including all lattice). Note that the callback interface
will only call back when the state changes. For example, door 1 opens, the status changes, callback, and then sdk will
keep querying the status of the door until the door closes. At this time, the status of door 1 will call back. Light No.
1 turns on and changes the status. Call back. Turn on Light No. 1 again. No callback, but turning off the light will
call back. setCabinetListener (CabinetListener listener); Among them, Listener callback method:

void onCabinetStatusCallback(int cellID, int changeSource, int errCode)

|PARAMETERS| interpretation| notes|
|:---|:---:|:---:|
|cellId| Grids with state changes| Any status change (temperature, lighting, lock,disinfection) in the underlying grid will be recalled. |
|changeSource| Sensor state change source| SOURCE = 0x00000000 From high to low, it is: lock, disinfection, heating, temperature, lamp, content, weight, color. If caused by a certain state, the position is 1 case: lock is closed or locked open. Callback: 0x10000000 Lock and disinfection change at the same time. Cause callback: 0x11000000|
|errCode| Hardware failure code|
|DynamicCellConfig| Configure grid objects |The default configuration is two cabinets, each with 16 blocks, regardless of size grids.|

void onCommCallback(String info);

|PARAMETERS| interpretation |notes|
|:---|:---:|:---:|
|info| Bottom communication information| This callback is mainly used for upper debugging and observing the underlying communication information. The underlying related calls and debugging information can be called back through this method. |

## 2.2 Call API

### 2.2.1 Obtain grid port information

The underlying API of the cabinet provides grid status data Bean, which is convenient for the cabinet app to query grid
status.

Call function:
SmartCabinetCellBean getCellStatus (int cellID);

cellID refers to the id of the grid (the grid id increments in order from 1)
SmartCabinetCellBean, through this example, you can get the temperature, lock status, lighting status, disinfection
status, heating status, the presence of goods, etc. SmartCabinetCellBean needs to provide instructions on how to call:

|call the method.| Return value type| notes|
|:---|:---:|:---:|
|getCellTmp() |   int |Current temperature of grid |
|getCellLightColor()    |int|Current light color of the grid: -1: light off, other: corresponding color value|
| isCellLockOpen()   | boolean | Lock Status: True: On, False: Off |
|isCellLightOpen()   | boolean | Light Status: True: On, false: Off |
|isCellCleanseOpen() |boolean| Disinfection status: true: open, false: off|
| isCellHeatingOpen()   | boolean |Heating status: true: on, false: off|
|isCellGoodsExist()  |  boolean| Status of the goods: true: exist, false: picked up |
|getCabinetId()  |  int |The cabinet ID where the grid is located| 
|getId()  |  int| grid number|
| getCellGoodsWeight()  |  int| Item weight (g)|
|getCellLightColor() |   int| Light color (RGB), such as red: 0xFF0000 |

### 2.2.2 Unified control

Description: Most of all block control APIs are asynchronous calls. The result of the call is obtained by monitoring
callbacks, and the synchronization method will be indicated.

#### 2.2.2.1 Open all blocks

int openEntireCell(int cabinetID)

|PARAMETERS| instructions |return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId |cabinet number| Operation results| The cabinet number is from 1|

#### 2.2.2.2 Turn on all lattice lights

int openEntireLight(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId |cabinet number| Operation results| The cabinet number is from 1|
#### 2.2.2.3 Open all grids for disinfection
int openEntireCleanse(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
#### 2.2.2.4 Turn on all grids for heating
int openEntireHeat(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|

#### 2.2.2.5 Close all grid disinfection
int closeEntireCleanse(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
#### 2.2.2.6 Turn off all lattice lights
int closeEntireLight(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
#### 2.2.2.7 Turn off all lattice heating
int closeEntireHeat(int cabinetID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|

### 2.2.3 Single lattice control
#### 2.2.3.1 Open the specified block
int openCell(int cabinetID, int cellID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
|cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.2 Open the specified grid for disinfection
int openCellCleanse(int cabinetID, int cellID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.3 Turn on the specified grid light
int openCellLight(int cabinetID, int cellID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId |cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.4 Turn on the specified grid and specify the color light
int openCellLight(int cabinetID, int cellID,int color);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
| color| light color| Operation results| int value, 0xFFFFFF, RGB, standard color value Example: red: 0xFF0000, green: 0x00FF00, blue: 0x0000FF|
#### 2.2.3.5 Open the specified grid for heating
int openCellHeat(int cabinetID, int cellID);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.6 Turn off the specified grid for disinfection
int closeCellCleanse(int cabinetID, int cellID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.7 Turn off the specified grid heating
int closeCellHeat(int cabinetID, int cellID)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
#### 2.2.3.8 Turn off the specified grid light
int closeCellLight(int cabinetID, int cellID);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results The cabinet number is from 1|
| cellID| block number| Operation results The cabinet number is from 1|
#### 2.2.3.9 Set the grid address (new and old S protocols)
boolean setAddress(Integer boardNo, Integer cellNo, Integer retryTimes)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
| retryTimes| Number of retries| Operation|results at least once |
####2.2.3.10 ping block (detection control board communication status) (synchronous method)
int pingCell(int cell);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
Explanation: In order to detect whether the communication of the control board is normal, the synchronization
method

#### 2.2.3.11 Open the door synchronously, turn on the light, turn off the light, turn on the heating, turn off the heating, turn on disinfection, turn off disinfection,

int openCellSync(Integer boardNo, Integer cellNo) \
int openCellLightSync(Integer boardNo,Integer cellNo) \
int closeCellLightSync(Integer boardNo,Integer cellNo) \
int openCellHeatSync(Integer boardNo, Integer cellNo) \
int closeCellHeatSync(Integer boardNo,Integer cellNo) \
int openCellCleanseSync(Integer boardNo,Integer cellNo) \
int closeCellCleanseSync(Integer boardNo,Integer cellNo) 

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
Explanation: They are all synchronization methods. The return result indicates whether the operation is successful, but the specific status change must call the synchronization query block status method (2.2.3.11) to query. It is recommended to call the operation first, and then call the query.

#### 2.2.3.12 Synchronous query block status
int syncReadCellStatus(int cabinetId, int cellId)

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| The cabinet number is from 1|
| cellID| block number| Operation results| The cabinet number is from 1|
Explanation: Synchronize the method, and the return result indicates whether the reading is successful. If the block status changes, it will be notified by callback, or the current block state can be actively obtained by calling getCellStatus (int cellID).

### 2.2.4 Modify initialization parameters

#### 2.2.4.1 Modify (set) block information
boolean setCells(DynamicCellConfig dynamicCellConfig);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| DynamicCellConfig |Grid configuration object| boolean| The cabinetCount property indicates that the number of lists in the total number of cabinets and cells is the same as that of cabietCount. sdk will generate continuous block loading memory from 1| 

#### 2.2.4.2 Modify the communication board agreement
boolean changeCupboardControlProtocol(String protocol);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| String| agreement |boolean| N: New N Agreement, ON: Old N Agreement, S: New S Agreement, OS: Old S Agreement, A:A Agreement|
#### 2.2.4.3 Modify serial port protocol
boolean changeSerialPort(String serialPort);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| String| serial port| boolean| Default is ttyS4|
#### 2.2.4.4 Whether to print sdk logs
int enableLogOutput(boolean enableLogOutput);


|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|enableLogOutput| Whether to turn on sdk log output |boolean |The default is true| 
Explanation: sdk opens the log output by default, and the output directory is a logger file (/mnt/sdcard/logger) under the root directory of sdcard. sdk will automatically control the log size and retention time. This time and size cannot be manually modified. Default log Save for 5 days, delete automatically with a total size of more than 400m, and compress it the next day.

#### 2.2.4.5 Control the read delay speed of the old N protocol
int setNProtocolReadCellDoorStatusLatency(Long timeMilles);


|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|timeMilles| Millisecond value of read delay| Operation results||
Explanation:According to the control board you purchased, such as the old N control protocol motherboard you purchased, after the door opening instruction is issued, the instruction will be sent to read the door status immediately. However, because this control board reacts slower than other control boards, a delay must be added between the sending and reading instructions to receive it correctly. Callback. This parameter is recommended to be set to more than 500ms, or you can pass the test or get a rough value. If you have any questions, please ask the manufacturer on the sixth floor of the West for help.

#### 2.2.4.6 Dynamic Blocking
int setCabinet(int cabinetID, int count);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| From 1|
| cellID| block number| Operation results| From 1|
Explanation: Dynamic coverage and addition of the number of blocks in the sdk cabinet

### 2.2.5 Optional functions (depending on the purchased cabinet functions)

#### 2.2.5.1 Turn on colored lights
int openCellLight(int cabinetID, int cellID, int color);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cabinetId| cabinet number| Operation results| From 1|
| cellID| block number| Operation results| From 1|
| color| RGB value of color| Operation results| Red: 0xFF0000...|
Explanation: Dynamic coverage and addition of the number of blocks in the sdk cabinet

#### 2.2.5.2 Item detector zeroing (synchronous method)
int resetItemDetector(int cellId);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cellID| block number| Operation results| From 1|
Explanation: Item detector, let it clear, synchronization method

#### 2.2.5.3 Set the threshold of the item detector (synchronous method)
int setItemDetectorInitWeight(int cell,int weight);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| cellID| block number| Operation results| From 1|
| weight| weight of the item| Operation results| Unit: g |
Explanation: Set the critical value of the item detector. Only when the critical value is exceeded can there be an item.

#### 2.2.5.4 Query error status code information
String getErrorMessage(int code);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|code| Error code int value| error message| Hardware communication anomalies|
Explanation: It is convenient for callers to view error messages directly through the program, not documents.

#### 2.2.5.5 Set the underlying item detector diff report value
int setItemDetectorNotificationDiff(int weight);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
|weight| diff difference| Operation results| Hardware communication anomalies|
Explanation: Because the item detection is affected by the environment, there are constant weight changes. Set weight and control the last and this change of excess weight before reporting.

#### 2.2.5.6 Set whether to turn on polling query block status
int enable LoopDetectDoorStatus (boolean isOpen);

|PARAMETERS| instructions| return value| notes|
|:---|:---:|:---:|:---:|
| isOpen| Whether to turn on polling query block status|Operation results|success|
Explanation: Set to true, sdk will keep polling query block status when the door is open until the door closes, false, close polling query

# appendix

## 3.1 Method return value

|return value| instructions| notes|
|:---|:---:|:---:|
|0| the call was successful||
|1| call failed||
|2| Incorrect parameter||
|3| This method is not supported||
|4| Illegal operation||
|6| Failed to query the specified block||
|8| ping failed||
|9| Sensor zeroing failure||
|10| Sensor clearance setting threshold failed||