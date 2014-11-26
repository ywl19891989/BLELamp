package com.imagic.lamp.kevin.ble;import java.util.ArrayList;import com.imagic.lamp.kevin.R;import android.app.Activity;import android.bluetooth.BluetoothAdapter;import android.bluetooth.BluetoothDevice;import android.bluetooth.BluetoothManager;import android.content.Context;import android.content.Intent;import android.content.pm.PackageManager;import android.os.Handler;import android.util.Log;import android.widget.Toast;/* 管理所有的 蓝牙设备  * 			功能: * 			   1)扫描所有的蓝牙设备  * 			   2)判断蓝牙权限是否打开 * @author Kevin.wu * */public class RFImagicManage {	private static int SCAN_TIME = 10000; // 扫描的时间为10秒	private static final int REQUEST_CODE = 0x01;// 返回的唯一标识	private Context context = null;	public static BluetoothAdapter bleAdapter = null;	private Handler handler = null;	private boolean isScanning = false; // 是否正在扫描	private RFImagicManageListener listener = null;	public static final String RFSTAR = "_rfstar";	public ArrayList<RFLampDevice> scanLampDeviceArray = new ArrayList<RFLampDevice>();	private ArrayList<BluetoothDevice> scanBlueDeviceArray = new ArrayList<BluetoothDevice>(); // 扫描到的数据	public RFImagicManage(Context context) {		handler = new Handler();		if (!context.getPackageManager().hasSystemFeature( // 检察系统是否包含蓝牙低功耗的jar包				PackageManager.FEATURE_BLUETOOTH_LE)) {			Toast.makeText(context, R.string.ble_not_supported,					Toast.LENGTH_SHORT).show();			((Activity) context).finish();		}		this.context = context;		BluetoothManager manager = (BluetoothManager) this.context				.getSystemService(Context.BLUETOOTH_SERVICE);		bleAdapter = manager.getAdapter();		if (bleAdapter == null) { // 检察手机硬件是滞支持蓝牙低功耗			Toast.makeText(context, R.string.error_bluetooth_not_supported,					Toast.LENGTH_SHORT).show();			((Activity) context).finish();			return;		}	}	/**	 * 设置扫描的时长，默认为10秒	 * 	 * @param scanTime	 */	public void setScanTime(int scanTime) {		SCAN_TIME = scanTime;	}	/**	 * 获取当前的搜索状态	 * 	 * @return	 */	public boolean getScanningState() {		return isScanning;	}	/**	 * 每扫描到一个蓝牙设备调用一次	 * 	 * @param listener	 */	public void setRFstarBLEManagerListener(RFImagicManageListener listener) {		this.listener = listener;	}	/**	 * 扫描蓝牙设备	 */	public void startScanBluetoothDevice() {		if (scanBlueDeviceArray != null) {			scanBlueDeviceArray = null;		}		 if (!scanLampDeviceArray.isEmpty()) {			 scanLampDeviceArray.clear();		 }		scanBlueDeviceArray = new ArrayList<BluetoothDevice>();		handler.postDelayed(new Runnable() {			@Override			public void run() {				// TODO Auto-generated method stub				stopScanBluetoothDevice();			}		}, SCAN_TIME); // 10秒后停止扫描		isScanning = true;		bleAdapter.startLeScan(bleScanCallback);		listener.RFstarBLEManageStartScan();	}	/**	 * 停止扫描蓝牙设备	 */	public void stopScanBluetoothDevice() {		if (isScanning) {			isScanning = false;			bleAdapter.stopLeScan(bleScanCallback);			listener.RFstarBLEManageStopScan();		}	}	public void addLampDevice(RFLampDevice device) {		this.scanLampDeviceArray.add(device);	}	public void removeLampDevice(RFLampDevice device) {		this.scanBlueDeviceArray.remove(device);	}	public ArrayList<RFLampDevice> getLampDevices() {		return this.scanLampDeviceArray;	}	// Device scan callback.	private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {		@Override		public void onLeScan(final BluetoothDevice device, final int rssi,				final byte[] scanRecord) {			// TODO 添加扫描到的device，并刷新数据			handler.post(new Runnable() {				@Override				public void run() {					// TODO Auto-generated method stub					if (!scanBlueDeviceArray.contains(device)) {						scanBlueDeviceArray.add(device);												byte[] encodeByte = new byte[8];						int y = 0;						for (int count = encodeByte.length + 1; count <= 16; count++) {							if (y < encodeByte.length) {								encodeByte[y] = scanRecord[count];								y++;							}						}						byte[] decodeByte = new byte[7];						boolean checksum = arrayCrcDecode(7, encodeByte,								decodeByte);						if (checksum) {							byte macByte[] = new byte[6], lampByte;							for (int idx = 0; idx < 6; idx++) {								macByte[idx] = decodeByte[idx];							}							lampByte = decodeByte[6];							// 信号为负数，转化为绝对值							listener.RFstarBLEManageListener(device,									Math.abs(rssi), encodeByte,									(int) (lampByte & 0xff));						}					}				}			});		}	};	/**	 * 判断是否开启蓝牙权限	 * 	 * @return	 */	public boolean isEdnabled(Activity activity) {		if (!bleAdapter.isEnabled()) {			if (!bleAdapter.isEnabled()) {				Intent enableBtIntent = new Intent(						BluetoothAdapter.ACTION_REQUEST_ENABLE);				activity.startActivityForResult(enableBtIntent, REQUEST_CODE);			}			return true;		}		return false;	}	/**	 * 设置权限后，返回时调用	 * 	 * @param requestCode	 * @param resultCode	 * @param data	 */	public void onRequestResult(int requestCode, int resultCode, Intent data) {		// User chose not to enable Bluetooth.		if (requestCode == REQUEST_CODE				&& resultCode == Activity.RESULT_CANCELED) {			((Activity) this.context).finish();			return;		}	}	/**	 * 用于处理，刷新到设备时更新界面	 * 	 * @author Kevin.wu	 * 	 */	public interface RFImagicManageListener {		public void RFstarBLEManageListener(BluetoothDevice device, int rssi,				byte[] scanRecord, int lampType);		public void RFstarBLEManageStartScan();		public void RFstarBLEManageStopScan();	}	/**	 * 字节转十六进制 为相应的字符串显示	 * 	 * @param data	 * @return	 */	public static String byte2Hex(byte data[]) {		if (data != null && data.length > 0) {			StringBuilder sb = new StringBuilder(data.length);			for (byte tmp : data) {				sb.append(String.format("%02X ", tmp));			}			return sb.toString();		}		return "no data";	}	// *******************************	/**	 * 数据加密和解密	 * 	 * @param arrayLengh	 * @param arrayEncode	 * @param arrayDecode	 * @return	 */	private boolean arrayCrcDecode(int arrayLengh, byte[] arrayEncode,			byte[] arrayDecode) {		boolean checkout = false;		// 生成新的数组		for (int idx = 0; idx < arrayLengh; idx++) {			arrayDecode[idx] = (byte) (arrayEncode[0] ^ arrayEncode[idx + 1]);		}		// 计算crcChecksum		byte crcChecksum = this.CRC_Checksum(arrayLengh, arrayDecode);		if (crcChecksum == arrayEncode[0]) {			checkout = true;		}		return checkout;	}	private byte CRC_Checksum(int arrayLengh, byte[] array) {		int i, j;		byte crcPassword[] = { 'C', 'h', 'e', 'c', 'k', 'A', 'e', 's' };		byte CRC_Checkout = 0x0;		for (i = 0; i < arrayLengh; i++) {			byte CRC_Temp = array[i];			for (j = 0; j < 8; j++) {				if (((int) CRC_Temp & 0x01) == 1) {					CRC_Checkout = (byte) (CRC_Checkout ^ crcPassword[j]);				}				CRC_Temp = (byte) (CRC_Temp >> 1);			}		}		return CRC_Checkout;	}	// *********************************	/*	 * 组合特征值	 * 	 * @param uuid	 * 	 * @return	 */	private String getSubUUID(String uuid) {		return "0000" + uuid + "-0000-1000-8000-00805f9b34fb";	}}