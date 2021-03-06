package com.xiaosong.config;


import com.esotericsoftware.minlog.Log;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface MinniSDK extends StdCallLibrary {
	
	MinniSDK INSTANCE = (MinniSDK) Native.loadLibrary("/usr/mini/libs/libArmStrongSDK.so",
			MinniSDK.class);

	public static final int BoxSDK_INVALID_DEVICE_HANDLE=-1;
	public static final int BoxSDK_BOOL_FALSE=0;
	public static final int BoxSDK_BOOL_TRUE=1;
	public static final int BoxSDK_CONFIG_VALUE_KEEP=0;
	public static final int BoxSDK_CONFIG_VALUE_ENABLE=1;
	public static final int BoxSDK_CONFIG_VALUE_DISABLE=2;
	public static final int BoxSDK_MAX_FACE_LIB_SIZE=8;
	public String FACE_RESULT_STR="";

	public  static Logger log = Logger.getLogger(MinniSDK.class);
	// 错误码
	public enum BoxSDK_ErrorCode {
		BoxSDK_UNKNOWN_ERROR(-2), BoxSDK_MEMORY_ERROR(-1), BoxSDK_SUCCESS(0), BoxSDK_DEVICE_RESULT_FAILED(
				1), intBoxSDK_INVALID_PARAMETER(100), BoxSDK_UNINITED(101), BoxSDK_ALREADY_INITED(102), BoxSDK_TIMEOUT(
						103), BoxSDK_OPERATION_CANCELLED(104), BoxSDK_CONNECT_DEVICE_FAILED(
								200), BoxSDK_DEVICE_NOT_EXIST(201), BoxSDK_NO_DEVICE_ON_LINE(
										202), BoxSDK__LOGINDEVICE_PENDING(203), BoxSDK_DEVICE_STATUS_CONNECTING(
												204), BoxSDK_DEVICE_STATUS_RECONNECTING(
														205), BoxSDK_DEVICE_STATUS_DISCONNECTED(206),

		BoxSDK_NETWORK_UNINITED(300), BoxSDK_NETWORK_SYS_ERROR(301), BoxSDK_NETWORK_SEND_DATA_FAILED(
				302), BoxSDK_NETWORK_SEND_DATA_PENDING(303), BoxSDK_NETWORK_SOCKET_CONFUSED(
						304), BoxSDK_NETWORK_RECV_DATA_FAILED(305),

		BoxSDK_NO_PROTO_DATA(400), BoxSDK_PROTO_ERROR_DATA(401), BoxSDK_PROTO_SERIALIZE_FAILED(
				402), BoxSDK_PROTO_DESERIALIZE_FAILED(403), BoxSDK_PROTO_UNKNOWN_REPORT_TYPE(404),

		BoxSDK_OPEN_FILE_FAILED(500), BoxSDK_READ_FILE_FAILED(501), BoxSDK_FILE_SIZE_TOO_LARGE(502), BoxSDK_FILE_EMPTY(
				503), BoxSDK_QUEUE_FULL(600), BoxSDK_FUNCTION_NOT_IMPLEMENTED(99999);
		private final int statenum;

		BoxSDK_ErrorCode(int statenum) {
			this.statenum = statenum;
		}

		public int getStatenum() {

			return statenum;

		}

	}

	// 状态
	public enum BoxSDK_DeviceStatusType {
		BoxSDK_DEVICE_INVALID_STATUS(0), BoxSDK_DEVICE_LOGIN_SUCCESS(1), BoxSDK_DEVICE_LOGIN_FAILED(
				2), BoxSDK_DEVICE_DISCONNECT(3), BoxSDK_DEVICE_RECONNECTING(4), BoxSDK_DEVICE_RECONNECT_SUCCESS(
						5), BoxSDK_DEVICE_RECONNECT_FAILED(6), BoxSDK_DEVICE_RECONNECT_CANCEL(
								7), BoxSDK_DEVICE_RECONNECT_AGAIN(8), BoxSDK_DEVICE_IPC_CHANGE(128);
		private final int statenum;

		BoxSDK_DeviceStatusType(int statenum) {
			this.statenum = statenum;
		}

		public int getStatenum() {

			return statenum;

		}
	}

	// BoxSDK_IPCStatusType

	public enum BoxSDK_IPCStatusType {
		BoxSDK_IPC_INVALID_STATUS(0), BoxSDK_IPC_STREAM_ONLINE(1), BoxSDK_IPC_STREAM_OFFLINE(2);
		private final int statenum;

		BoxSDK_IPCStatusType(int statenum) {
			this.statenum = statenum;
		}

		public int getStatenum() {
			return statenum;
		}
	}
	
	
	public static class BoxSDK_ID extends Structure{
		public byte[] id = new byte[68];

		public static class ByReference extends BoxSDK_ID implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_ID implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}

	}
	
	public static class BoxSDK_TimeSlice extends Structure{
		public long begin_time_ms;
		public long end_time_ms;

		public static class ByReference extends BoxSDK_TimeSlice implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_TimeSlice implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_DeviceTime extends Structure{
		public long timestamp;    // 手动校时，unix时间戳，单位毫秒 
	    public byte[] ntp_server=new byte[128];   // 网络校时服务器地址 
	    public int interval;

		public static class ByReference extends BoxSDK_DeviceTime implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_DeviceTime implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_IPCStream extends Structure{
		public int status;             // 详见枚举BoxSDK_IPCStatusType 
		public byte[] rtsp_url=new byte[256];     // rtsp地址 
		public byte[] proxy_url=new byte[256];    // 盒子转发的rtsp url 
		public int width;              // 视频宽度 
		public int height;             // 视频高度 
		public int fps;                // 视频帧率 
		public int bitrate;            // 视频码率

		public static class ByReference extends BoxSDK_IPCStream implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_IPCStream implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_IPCInfo extends Structure{
		public int channel_id;                     // IPC的通道号 
		public byte[] channel_name=new byte[68];              // IPC的通道名称 
		public BoxSDK_IPCStream.ByValue main_stream ;       // 主码流
		public BoxSDK_IPCStream.ByValue sub_stream ;        // 子码流
		public int[] face_lib_ids=new int[BoxSDK_MAX_FACE_LIB_SIZE];
		public int enable_face_capture;
		public int min_face_size;
		public int enable_plate_capture;
		public int min_plate_width;
		public byte[] onvif_username=new byte[68];
		public byte[] onvif_password=new byte[68];
		public static class ByReference extends BoxSDK_IPCInfo implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_IPCInfo implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
		
	}
	
	public static class BoxSDK_DeviceStatus extends Structure{
		public Long device_handle;     // 设备句柄，没有正常login到设备时，此字段为0， 比如异步login设备失败时
		public byte[] device_ip=new byte[48];                     // 设备IP 
		public int device_port;                        // 设备端口号 
		public byte[] device_id=new byte[68];                     // 设备ID 
		public int status;                             // 设备状态，详见枚举BoxSDK_DeviceStatusType 
	    public byte[] message=new byte[256];

	    public static class ByReference extends BoxSDK_DeviceStatus implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_DeviceStatus implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_DeviceInfo extends Structure{
		public byte[] type=new byte[68];                          // 设备类型，比如"B2R" 
		public byte[] id=new byte[68];                            // 设备ID 
		public byte[] ip=new byte[48];                            // 设备IP 
		public int port;                               // 设备端口号 
		public int device_temperature;                 // 设备温度，单位℃ 
		public int fpag_active;                        // FPAG模块状态, 0未工作，1工作 
		public long up_time;                      // 设备运行时间，单位ms
		public long wall_clock;                   // 设备系统时间，unix时间，单位ms
		public int cpu_usage;                          // CPU使用率(百分比) 
		public int memory_usage;                       // 内存使用率(百分比) 
		public int disk_usage;                         // 硬盘使用率(百分比) 
		public byte[] hardware_version=new byte[68];              // 硬件版本号 
		public byte[] firmware_version=new byte[68];              // 固件版本号 
		public byte[] fpga_version=new byte[68];                  // FPGA版本号 
		public byte[] algorithm_version=new byte[68];             // 算法版本号 
		public byte[] detection_version=new byte[68];             // 检测(模型)版本号 
		public byte[] feature_version=new byte[68];               // 特征(模型)版本号 
		public int original_image_quality;             // 全景图质量分数 [1, 100] 
		public long max_face_library_capacity;    // 人脸底库最大容量 
		public long max_plate_library_capacity;   // 车牌底库最大容量 
		public byte[] reserve=new byte[32];                       // 保留字段

		 public static class ByReference extends BoxSDK_DeviceInfo implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_DeviceInfo implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	
	
	
	public static class BoxSDK_DeviceSearchInfo extends Structure{
		byte[] info=new byte[256];

		 public static class ByReference extends BoxSDK_DeviceSearchInfo implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_DeviceSearchInfo implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	
	public static class BoxSDK_DeviceSearch extends Structure{
		 public byte[] iface=new byte[68];
		 public   int port;
		 public   int timeout_ms;
		 public   int error_code;
		 public   byte[] message=new byte[256];
//		 public  Pointer info;//BoxSDK_DeviceSearchInfo(转换成point)
		 public  BoxSDK_DeviceSearchInfo.ByReference info;
		 public   int info_size;

		 public static class ByReference extends BoxSDK_DeviceSearch implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_DeviceSearch implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	public static class BoxSDK_Point extends Structure{
		int x;  //4
	    int y;  //4

	    public static class ByReference extends BoxSDK_Point implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_Point implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public enum BoxSDK_FaceGlasses
	{
	    BoxSDK_GLASSES_NO_RESULT(0),
	    BoxSDK_GLASSES_NONE (1),
	    BoxSDK_GLASSES_WEARING(2);
		private final int statement;
		BoxSDK_FaceGlasses(int statement){
			this.statement = statement;
		}
		public int getStatement() {
			return statement;
		}
	}
	
	public enum BoxSDK_FaceRespirator{
		BoxSDK_RESPIRATOR_NO_RESULT(0),
			    BoxSDK_RESPIRATOR_NONE(1),
			    BoxSDK_RESPIRATOR_WEARING(2);
		private final int statement;
		BoxSDK_FaceRespirator(int statement){
			this.statement = statement;
		}
		public int getStatement() {
			return statement;
		}
	}
	
	public enum BoxSDK_FaceFever{
		 BoxSDK_FEVER_NO_RESULT(0),
				    BoxSDK_FEVER_NO(1),
				    BoxSDK_FEVER_YES(2);
		
		private final int statement;
		BoxSDK_FaceFever(int statement){
			this.statement = statement;
		}
		public int getStatement() {
			return statement;
		}
	}
	
	public enum BoxSDK_FaceGender{
		BoxSDK_GENDER_NO_RESULT(0),
			    BoxSDK_GENDER_MALE(1),
			    BoxSDK_GENDER_FEMALE(2),
			    BoxSDK_GENDER_UNKNOWN(3);
		
		private final  int statement;
		BoxSDK_FaceGender(int statement){
			this.statement=statement;
		}
		public int getStatement() {
			return statement;
		}
		
	}
	
	public static class BoxSDK_FaceAttribute extends Structure{
		public int age;
		public    BoxSDK_FaceGender gender;
		public BoxSDK_FaceGlasses glasses;
		public  BoxSDK_FaceRespirator respirator;
		public   BoxSDK_FaceFever fever;

		 public static class ByReference extends BoxSDK_FaceAttribute implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_FaceAttribute implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	public static class BoxSDK_Buffer extends Structure{
		public Pointer[] data;
		public int size;

		 public static class ByReference extends BoxSDK_Buffer implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_Buffer implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	public static class BoxSDK_FaceImage extends Structure{
		 public BoxSDK_Buffer capture;
		 public BoxSDK_Buffer original;
		 public BoxSDK_Buffer exif;

		 public static class ByReference extends BoxSDK_FaceImage implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_FaceImage implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	public static class BoxSDK_FaceAlarm extends Structure{
		BoxSDK_FaceImage.ByValue image;
	    double temperature;

	    public static class ByReference extends BoxSDK_FaceAlarm implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_FaceAlarm implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_FaceRecogResult extends Structure{
		byte[] name=new byte[161];      //161    		//
		byte[] face_id=new byte[68];    //68    		// 比中人脸的ID
		byte[] comments=new byte[257];  //257    		//
		int lib_id;             		//4			// 比中的人脸所在底库的ID
		float score;           			//4			// 比分 [0.f, 100.f]
	    BoxSDK_Buffer.ByValue library_image; //8
	    BoxSDK_FaceAlarm.ByValue alarm;		 //24

	    public static class ByReference extends BoxSDK_FaceRecogResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_FaceRecogResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}
	}
	
	public static class BoxSDK_FaceCapture extends Structure{
		public BoxSDK_FaceAlarm.ByValue faceAlarm;
		 public static class ByReference extends BoxSDK_FaceAlarm implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_FaceAlarm implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}
	}
	
	public static class  BoxSDK_FaceResult extends Structure
	{
		public byte[] device_id=new byte[68];   //68    // 设备ID，不建议业务端使用此字段，正式版本中将取消此字段
		public long device_handle;    			//8	   // 设备句柄
	    public int channel_id;                  //4    // 通道ID
	    public int frame_id;                    //4    // 视频帧ID
	    public int track_id;                    //4    // 跟踪ID

	    // 人脸坐标 
	    public int face_x;						//4
	    public int face_y;						//4
	    public int face_width;					//4
	    public int face_height;					//4
	    public BoxSDK_Point.ByReference landmark;     //108 	// landmark数据
	    public int landmark_size;                     //4	    	// landmark点数
	    public Pointer feature_data;              	  //4	 		// 特征数据
	    public int feature_data_size;                 //4	    	// 特征大小
	    public BoxSDK_FaceAttribute.ByValue attribute;//20          // 人脸属性
	    public long[] timestamp;                      //4			// unix时间戳，单位ms

	    public BoxSDK_FaceCapture.ByValue capture;    //24         // 人脸抓拍(一定有)
	    public BoxSDK_FaceAlarm.ByReference fever;    //24            // 人脸发烧报警(无或1个）
	    public BoxSDK_FaceAlarm.ByReference respirator;//24           // 人脸无口罩报警(无或1个)
	    public BoxSDK_FaceRecogResult recog_results;   //526		// 人脸识别报警(无或1个或多个)
	    public int recog_result_size;                 			 // 识别报警的数量

	    byte[] reserve=new byte[32]; 			//778
	    public static class ByReference extends BoxSDK_FaceResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_FaceResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段

	};
	
	public static class BoxSDK_FaceResultConfig extends Structure{
		public int enable;                             // 是否接收人脸结果
	   	public int enable_feature;                     // 是否接收人脸的特征
	   	public int enable_capture_image;               // 是否接收人脸抓拍的抓拍图
	   	public int enable_original_image;              // 是否接收人脸抓拍的全景图
	   	public int enable_recog_capture_image;         // 是否接收人脸识别的底库图
	   	public int enable_recog_original_image;        // 是否接收人脸识别的底库图
	   	public int enable_recog_library_image;         // 是否接收人脸的底库图
	   	public int enable_fever_capture_image;         // 是否接收人脸的发烧抓拍图
	   	public int enable_fever_original_image;        // 是否接收人脸的发烧全景图
	   	public int enable_respirator_capture_image;    // 是否接收人脸的无口罩抓拍图
	   	public int enable_respirator_original_image;   // 是否接收人脸的无口罩全景图

	    public static class ByReference extends BoxSDK_FaceResultConfig implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_FaceResultConfig implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_PlateRecogResult extends Structure{
		int lib_id;             // 比中的车牌所在底库的ID，目前车牌默认一个全局底库，此字段为0 
	    byte[] number=new byte[68];        // 比中的车牌号码 
	    byte[] comments=new byte[257];

	    public static class ByReference extends BoxSDK_PlateRecogResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_PlateRecogResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_DeviceConfig extends Structure{
		public byte[] ip=new byte[48];        // 设备的ip
	    public int port;           // 设备的端口号
	    public byte[] username=new byte[68];  // 用户名
	    public byte[] password=new byte[68];  // 密码
	    public byte[] reserve=new byte[32];   // 保留字段

	    public static class ByReference extends BoxSDK_DeviceConfig implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_DeviceConfig implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_PlateResult extends Structure{
		public long device_handle;     // 设备句柄
		public byte[] device_id=new byte[68];                     // 设备ID，不建议业务端使用此字段，正式版本中将取消此字段 
		public int channel_id;                         // 通道ID 
		public int frame_id;                           // 视频帧ID 
		public int track_id;                           // 跟踪ID 

	    // 车牌坐标 
		public int plate_x;
		public int plate_y;
		public int plate_width;
		public int plate_height;

		public Pointer plate_image_data;           // 车牌抠图的数据 
		public int plate_image_size;                   // 车牌抠图的大小 
		public Pointer vvehicle_image_data;         // 车辆抠图的数据 
		public int vehicle_image_size;                 // 车辆抠图的大小 
		public Pointer original_image_data;        // 全景图的数据 
		public int original_image_size;                // 全景图的大小 
		public byte[] number=new byte[68];                        // 车牌号码 
//	    public Pointer recog_result;  					// 识别结果，识别成功时，指针非空，识别失败时指针为空 BoxSDK_PlateRecogResult（转指针）
	    public BoxSDK_PlateRecogResult recog_result;
	    public int recog_result_size;                  // 识别成功时，此字段表示是识别结果的数量，识别失败时此字段值为0
	    public long timestamp;                    // unix时间戳，单位ms
	    public byte[] reserve=new byte[32];

	    public static class ByReference extends BoxSDK_DeviceConfig implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_DeviceConfig implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public enum BoxSDK_FaceFileErrorCode {
	BoxSDK_FFEC_NOT_EXIST(-2),
	BoxSDK_FFEC_UNKNOWN(-1),
	BoxSDK_FFEC_SUCCESS(0),
	BoxSDK_FFEC_NO_FACE(1),
	BoxSDK_FFEC_MULTIPLE_FACES(2),
	BoxSDK_FFEC_LOW_QUALITY_FACE(3),
	BoxSDK_FFEC_TIMEOUT(4),
	BoxSDK_FFEC_NO_MODEL(5),
	BoxSDK_FFEC_LOADING_FEATURE(6),
	BoxSDK_FFEC_FACE_LIBRAY_MAX_LIMIT(7),
	BoxSDK_FFEC_EXIST_SAME_TOKEN(8),
	BoxSDK_FFEC_BAD_ASPECT_RATIO(9),
	BoxSDK_FFEC_IMAGE_DECODE_ERR(10),
	BoxSDK_FFEC_FQ_TOO_SMALL(11),
	BoxSDK_FFEC_FQ_TOO_LARGE_PITCH(12),
	BoxSDK_FFEC_FQ_TOO_LARGE_YAW(13),
	BoxSDK_FFEC_FQ_TOO_LARGE_ROLL(14),
	BoxSDK_FFEC_FQ_TOO_BLUR(15),
	BoxSDK_FFEC_FQ_NO_FACE(16),
	BoxSDK_FFEC_NEED_MODEL(1001),
	BoxSDK_FFEC_DUPLICATE(1002),
	BoxSDK_FFEC_IMAGE_TOO_LARGE(1003);
		private final int val;
		BoxSDK_FaceFileErrorCode(int val){
			this.val=val;
		}

		public int getVal() {
			return val;
		}
	}
	
	
	public enum BoxSDK_DBOperationType{
		BoxSDK_DBOP_INVALID(0),
		BoxSDK_DBOP_ADD(1),        // 增加 
		BoxSDK_DBOP_DELETE(2),     // 删除 
		BoxSDK_DBOP_UPDATE(3),     // 修改 
		BoxSDK_DBOP_QUERY(4);       // 查询 
		private final int val;
		BoxSDK_DBOperationType(int val){
			this.val = val;
		}
		public int getVal() {
			return val;
		}
	}
	
	public static class BoxSDK_FaceFile extends Structure{
		 byte[] id=new byte[68];        // 图片ID，入库时唯一，抽特征时可随意赋值 
		   Pointer data;   // 图片数据，支持格式: jpeg，jpg，bmp，png 
		    int data_size;      // 图片数据的大小 
		    byte[] name=new byte[161];     // UTF8字符串，最大长度160 
		    byte[] comments=new byte[257]; // UTF8字符串，最大长度256 
		    int libs;    // 所属分库的id 
		    int lib_size;       // 所属底库的数量 
		    BoxSDK_ErrorCode error_code; // 人脸文件管理操作的错误码 
		    BoxSDK_FaceFileErrorCode ff_error_code; // 人脸入库的错误码 
		    byte[] error_message=new byte[128]; // 错误信息

		    public static class ByReference extends BoxSDK_FaceFile implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_FaceFile implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}// 保留字段
	}
	
	public static class BoxSDK_FaceFileResult extends Structure{
			long device_handle;
		    BoxSDK_DBOperationType operation_type;
//		    Pointer face_files;//BoxSDK_FaceFile结构体转换
			BoxSDK_FaceFile.ByReference face_files;//BoxSDK_FaceFile结构体转换
		    int face_file_size;
		    BoxSDK_ErrorCode error_code;
		    byte[] error_message=new byte[128];
		    int total_size;

		    public static class ByReference extends BoxSDK_FaceFileResult implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_FaceFileResult implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}// 保留字段
	}
	
	public static class BoxSDK_PlateInfo extends Structure{
		byte[] number=new byte[68];
	    // char update_number[68]; // 目前尚不支持直接更新车牌号码 
	    byte[] comments=new byte[257]; // UTF8字符串，最大长度256 
	    BoxSDK_ErrorCode error_code;
	    BoxSDK_PlateInfoErrorCode pi_error_code;
	    byte[] error_message=new byte[128];

	    public static class ByReference extends BoxSDK_PlateInfo implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_PlateInfo implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public enum BoxSDK_PlateInfoErrorCode{
		BoxSDK_PIEC_NOT_EXIST(-2),
			    BoxSDK_PIEC_UNKNOWN(-1),
			    BoxSDK_PIEC_SUCCESS(0),
			    BoxSDK_PIEC_DUPLICATE(1),
			    BoxSDK_PIEC_INVALID_PLATE_NUMBER(2),
			    BoxSDK_PIEC_INVALID_LENGTH(3);
		
		private final int val;
		BoxSDK_PlateInfoErrorCode(int val){
			this.val = val;
		}
		public int getVal() {
			return val;
		}
	}
	
	public static class BoxSDK_PlateInfoResult extends Structure{
		public BoxSDK_ErrorCode error_code;
		public byte[] error_message=new byte[128];
		public long device_handle;
		public BoxSDK_DBOperationType operation_type;
//		public Pointer plate_info;//BoxSDK_PlateInfo结构体转换
		public BoxSDK_PlateInfo.ByReference plate_info;
		public int plate_info_size;
		public int total_size;
		public static class ByReference extends BoxSDK_PlateInfoResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_PlateInfoResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_UpdateLibraryProgress extends Structure{
		    long device_handle;            // 设备句柄
		    int total_count;                    // 操作的总数量 
		    int finished_count;                 // 已完成的数量，成功和失败均包括在内 
		    int failed_count;                   // 失败的数量 
		    public static class ByReference extends BoxSDK_UpdateLibraryProgress implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_UpdateLibraryProgress implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}// 保留字段
	}
	
	public static class BoxSDK_UpgradeDeviceProgress extends Structure{
		 	public long device_handle; // 设备句柄
		    int step;                           // 进度，见枚举BoxSDK_UpgradeDeviceStep 
		    int progress;                       // 进度(区分阶段)，完成度的百分比[0, 100] 
		    int error_code;                     // 当step为BoxSDK_UDS_ERROR时，此字段存放具体错误码 
		    byte[] error_message=new byte[128];            // 当step为BoxSDK_UDS_ERROR时，此字段存放设备端传回的具体的错误信息 

		    // 升级设备时可能需要重新入库(比如特征模型更新)，在step为BoxSDK_UDS_FEATURE_REBUILDING时下面的字段有效 
		    BoxSDK_UpdateLibraryProgress.ByValue library_progress;

		    public static class ByReference extends BoxSDK_UpgradeDeviceProgress implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_UpgradeDeviceProgress implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}// 保留字段
	}
	
	public static class BoxSDK_FaceHistoryResult extends Structure{
		long device_handle;
//	    Pointer face_results;//BoxSDK_FaceResult转换
		BoxSDK_FaceResult face_results;
	    int face_size;
	    int total_size;

	    public static class ByReference extends BoxSDK_FaceHistoryResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_FaceHistoryResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_PlateHistoryResult extends Structure{
		long device_handle;
//	    Pointer plate_results;//BoxSDK_PlateResult转换
		BoxSDK_PlateResult.ByReference plate_results;
	    int plate_size;
	    int total_size;


	    public static class ByReference extends BoxSDK_PlateHistoryResult implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_PlateHistoryResult implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}
	
	public static class BoxSDK_InternalInfo extends Structure{
		 	long device_handle; // 设备句柄
		    int code;                           // 状态码 
		    byte[] message=new byte[256];                  // 消息 
		    byte[] reserve=new byte[32];

		    public static class ByReference extends BoxSDK_InternalInfo implements Structure.ByReference {
	            //指针和引用的传递使用ByReference
		}

		public static class ByValue extends BoxSDK_InternalInfo implements Structure.ByValue {
	            //拷贝参数传递使用ByValue
		}// 保留字段
	}

	public static interface BoxSDK_DEVICE_STATUS_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_DeviceStatus.ByReference devicestatus, Pointer user_data);
	}
	
	public static interface BoxSDK_IPC_STATUS_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_IPCInfo.ByReference ipcinfo, int ipc_size, Pointer user_data);
	}
	
	public static interface BoxSDK_FACE_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_FaceResult.ByReference ipcinfo, int ipc_size, Pointer user_data);
	}
	
	public static class MessageHander implements BoxSDK_FACE_RESULT_CALLBACK{

		public static BoxSDK_FaceResult.ByReference result =new BoxSDK_FaceResult.ByReference();
		@Override
		public void invoke(BoxSDK_FaceResult.ByReference ipcinfo, int ipc_size, Pointer user_data) {
			// TODO Auto-generated method stub
			if(ipcinfo==null||ipc_size<=0){
				return;
			}else{
				for (int i = 0; i < ipc_size; i++) {
					byte[] device_id=new byte[68];
					long[] device_handle =new long[1];
					int[] channel_id= new int[1];
					int[] frame_id= new int[1];
					int[] track_id= new int[1];
					int[] face_x = new int[1];
					int[] face_y = new int[1];
					int[] face_width = new int[1];
					int[] face_height = new int[1];
					int[] x= new int[1];						//BoxSDK_Point.ByReference
					int[] y= new int[1];
					int[] landmark_size= new int[1];
					Pointer[] feature_data= new Pointer[1];
					int[] feature_data_size= new int[1];
					int[] age =new int[1];						//BoxSDK_FaceAttribute.ByValue
					int[] statement =new int[3];
					long[] timestamp = new long[1]; 			//时间
					Pointer[] data = new Pointer[9];
					int[] size = new int[9];
					double[] temperature = new double[3];
					byte[] name = new byte[161]; 				//姓名
					byte[] face_id=new byte[68]; 				//人脸id
					byte[] comments=new byte[257];
					int[] lib_id =new int[1];
					float[] score =new float[1];
					Pointer[] imageData =new Pointer[1];		//人脸信息//BoxSDK_Buffer
					int[] imageSize = new int[1];				//人脸信息
					Pointer[] alarmData = new Pointer[3];		//体温过高 人脸信息//BoxSDK_FaceAlarm
					int[] alarmSize = new int[3];				//体温过高 人脸信息
					double[] alarmSemperature =new double[1];

					user_data.read(0 + i * 788, device_id, 0, 68);
					user_data.read(72 + i * 788, device_handle, 0, 1);
					user_data.read(80 + i * 788, channel_id, 0, 1);
					user_data.read(84 + i * 788, frame_id, 0, 1);
					user_data.read(88 + i * 788, track_id, 0, 1);
					user_data.read(92 + i * 788, face_x, 0, 1);
					user_data.read(96 + i * 788, face_y, 0, 1);
					user_data.read(100 + i * 788, face_width, 0, 1);
					user_data.read(104 + i * 788, face_height, 0, 1);
					user_data.read(108 + i * 788, x, 0, 1);
					user_data.read(112 + i * 788, y, 0, 1);
					user_data.read(116 + i * 788, landmark_size, 0, 1);
					user_data.read(120 + i * 788, feature_data, 0, 1);
					user_data.read(124 + i * 788, feature_data_size, 0, 1);
					user_data.read(128 + i * 788, age, 0, 1);
					user_data.read(132 + i * 788, statement, 0, 3);
					user_data.read(144 + i * 788, timestamp, 0, 1);
					user_data.read(152 + i * 788, data, 0, 9);//---
					user_data.read(188 + i * 788, size, 0, 9);
					user_data.read(224 + i * 788, temperature, 0, 3);
					user_data.read(248 + i * 788, name, 0, 161);
					user_data.read(410 + i * 788, face_id, 0, 68);
					user_data.read(479 + i * 788, comments, 0, 257);
					user_data.read(740 + i * 788, lib_id, 0, 1);
					user_data.read(744 + i * 788, score, 0, 1);
					user_data.read(748 + i * 788, imageData, 0, 1);
					user_data.read(752 + i * 788, imageSize, 0, 1);
					user_data.read(756 + i * 788, alarmData, 0, 3);
					user_data.read(768 + i * 788, alarmSize, 0, 3);
					user_data.read(780 + i * 788, alarmSemperature, 0, 1);

					log.info(device_id[0]);
					log.info(device_handle[0]);
					log.info(channel_id[0]);
					log.info(frame_id[0]);
					log.info(track_id[0]);
					log.info(face_x[0]);
					log.info(face_y[0]);
					log.info(face_width[0]);
					log.info(face_height[0]);
					log.info(x[0]);
					log.info(y[0]);
					log.info(landmark_size[0]);
					log.info(feature_data[0]);
					log.info(feature_data_size[0]);
					log.info(age[0]);
					log.info(statement[0]);
					log.info(timestamp[0]);
					log.info(data[0]);
					log.info(size[0]);
					log.info(temperature[0]);
					log.info(name[0]);
					log.info(face_id[0]);
					log.info(comments[0]);
					log.info(lib_id[0]);
					log.info(score[0]);
					log.info(imageData[0]);
					log.info(imageSize[0]);
					log.info(alarmData[0]);
					log.info(alarmSize[0]);
					log.info(alarmSemperature[0]);

					ipcinfo.timestamp=timestamp;						//时间
					ipcinfo.recog_results.name=name;					//姓名
					ipcinfo.recog_results.face_id=face_id; 				//人脸id
					ipcinfo.recog_results.library_image.data=imageData; //人脸照片
					ipcinfo.recog_results.alarm.image.capture.data=alarmData; //发热人脸照片
					//把毫秒转换成 时间格式
					long milliSecond =0;
					for (long l : timestamp) {
						milliSecond = l;
					}
					Date date = new Date();
					date.setTime(milliSecond);
					String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
					try {
						log.info("当前时间:"+format+"姓名:"+new String(name, "GBK").trim()+"人脸id:"+new String(face_id).trim());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}
	
	public static interface BoxSDK_PLATE_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_PlateResult.ByReference palterresult, int ipc_size, Pointer user_data);
	}
	
	public static interface BoxSDK_FACE_FILE_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_FaceFileResult.ByReference palterresult, int ipc_size, Pointer user_data);
	}
	
	public static interface BoxSDK_PLATE_INFO_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_PlateInfoResult.ByReference palterresult, int ipc_size, Pointer user_data);
	}
	
	public static interface BoxSDK_UPGRADE_DEVICE_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_UpgradeDeviceProgress.ByReference upgradeDeviceProgress, Pointer user_data);
	}
	
	public static interface BoxSDK_FACE_HISTORY_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_FaceHistoryResult.ByReference facehisresult, int result_size, Pointer user_data);
	}
	
	public static interface BoxSDK_PLATE_HISTORY_RESULT_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_PlateHistoryResult.ByReference palterhisresult, int result_size, Pointer user_data);
	}
	
	public static interface BoxSDK_FACE_FEATURE_CALLBACK extends StdCallCallback{
		public void invoke(Pointer user_data);
	}
	
	public static interface BoxSDK_INTERNAL_INFO_CALLBACK extends StdCallCallback{
		public void invoke(BoxSDK_InternalInfo.ByReference internal_info, Pointer user_data);
	}
	
	public static class BoxSDK_Config extends Structure{
		public BoxSDK_DEVICE_STATUS_CALLBACK           device_status_callback;                 // 设备状态回调，此回调主要通知设备的上线和下线
	    public BoxSDK_IPC_STATUS_CALLBACK              ipc_status_callback;                    // ipc状态回调，此回调主要通知ipc的上线和下线
	    public BoxSDK_FACE_RESULT_CALLBACK             face_result_callback;                   // 人脸识别结果回调
	    public BoxSDK_PLATE_RESULT_CALLBACK            plate_result_callback;                  // 车牌识别结果回调
	    public BoxSDK_FACE_FILE_RESULT_CALLBACK        face_file_result_callback;              // 人脸文件管理结果回调
	    public BoxSDK_PLATE_INFO_RESULT_CALLBACK       plate_info_result_callback;             // 车牌信息管理结果回调
	    public BoxSDK_UPGRADE_DEVICE_CALLBACK          upgrade_device_callback;                // 升级设备的进度结果回调
	    public BoxSDK_FACE_HISTORY_RESULT_CALLBACK     face_history_result_callback;           // 人脸历史结果回调
	    public BoxSDK_PLATE_HISTORY_RESULT_CALLBACK    plate_history_result_callback;          // 车牌历史结果回调
	    public BoxSDK_FACE_FEATURE_CALLBACK            face_feature_callback;                  // 特征抽取结果回调
	    public BoxSDK_INTERNAL_INFO_CALLBACK           internal_info_callback;                 // SDK内部信息回调，主要用于监控分析SDK和device的一些异常状态
	    public Pointer                                    user_data;                             // 用户数据
	    public int enable_DAS;
	    public byte[] DAS_ip=new byte[48];
	    public int DAS_port;

	    public static class ByReference extends BoxSDK_Config implements Structure.ByReference {
            //指针和引用的传递使用ByReference
	}

	public static class ByValue extends BoxSDK_Config implements Structure.ByValue {
            //拷贝参数传递使用ByValue
	}// 保留字段
	}

	//初始化设备
//	 int BoxSDK_init(Pointer config);//BoxSDK_Config 转pointer后调用
	 int BoxSDK_init(BoxSDK_Config.ByReference config);

	// 注册B2R
//	int BoxSDK_login_device(Pointer config, int time_ms, int handle);//BoxSDK_DeviceConfig结构体转指针后调用
	int BoxSDK_login_device(BoxSDK_DeviceConfig.ByReference config, int time_ms, long handle);

	// 开启人脸比对结果
//	int BoxSDK_set_face_result_config(int device_handle, Pointer face_result_config, int timeout_ms);//BoxSDK_FaceResultConfig转指针后调用
	int BoxSDK_set_face_result_config(long device_handle, BoxSDK_FaceResultConfig.ByReference face_result_config, int timeout_ms);


	// 登出 B2R(可选)
	int BoxSDK_logout_device(long device_handle);

	// 销毁 SDK
	int BoxSDK_release();

}
