package com.imagic.lamp.kevin.ble;import java.util.ArrayList;import java.util.List;import android.app.Service;import android.bluetooth.BluetoothDevice;import android.bluetooth.BluetoothGatt;import android.bluetooth.BluetoothGattCallback;import android.bluetooth.BluetoothGattCharacteristic;import android.bluetooth.BluetoothGattService;import android.bluetooth.BluetoothProfile;import android.content.Intent;import android.os.Binder;import android.os.IBinder;import android.util.Log;/* * 管理蓝牙的服务  * 			功能：  *			    1) 连接蓝牙设备 *				2) 管理连接状态 *				3) 获取蓝牙设备的相关服务 * * @author Kevin.wu *  */public final class RFImagicBLEService extends Service {	public final static String ACTION_GATT_CONNECTED = "com.rfstar.kevin.service.ACTION_GATT_CONNECTED";	public final static String ACTION_GATT_CONNECTING = "com.rfstar.kevin.service.ACTION_GATT_CONNECTING";	public final static String ACTION_GATT_DISCONNECTED = "com.rfstar.kevin.service.ACTION_GATT_DISCONNECTED";	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.rfstar.kevin.service.ACTION_GATT_SERVICES_DISCOVERED";	public final static String ACTION_DATA_AVAILABLE = "com.rfstar.kevin.service.ACTION_DATA_AVAILABLE";	public final static String EXTRA_DATA = "com.rfstar.kevin.service.EXTRA_DATA";	public final static String ACTION_GAT_RSSI = "com.rfstar.kevin.service.RSSI";	public final static String RFSTAR_CHARACTERISTIC_ID = "com.rfstar.kevin.service.characteristic"; // 唯一标识	private final IBinder kBinder = new LocalBinder();	// private BluetoothGatt gatt = null;	private static ArrayList<BluetoothGatt> arrayGatts = new ArrayList<BluetoothGatt>(); // 存放BluetoothGatt的集合	@Override	public IBinder onBind(Intent intent) {		// TODO Auto-generated method stub		return kBinder;	}	@Override	public boolean onUnbind(Intent intent) {		// TODO Auto-generated method stub		// if (gatt == null) {		// return false;		// }		// gatt.close();		// gatt = null;		return super.onUnbind(intent);	}	/**	 * 初始化BLE 如果已经连接就不用再次连接	 * 	 * @param bleDevice	 * @return	 */	public boolean initBluetoothDevice(BluetoothDevice device) {		BluetoothGatt gatt = this.getBluetoothGatt(device);		if (gatt != null) {			if (gatt.connect()) {				// 已经连接上				Log.d(RFImagicManage.RFSTAR, "55555 当前连接的设备同address mac : "						+ gatt.getDevice().getAddress() + "  连接上  数量: "						+ arrayGatts.size());			} else {				return false;			}			return true;		}		Log.d(RFImagicManage.RFSTAR, "5555" + device.getName() + ": 蓝牙设备正准备连接");		gatt = device.connectGatt(this, false, bleGattCallback);		arrayGatts.add(gatt);		return true;	}	/**	 * 断开连接	 */	public void disconnect() {	}	/**	 * 连接防丢器	 * 	 * @return	 */	public boolean connect(BluetoothDevice device) {		return initBluetoothDevice(device); // 写到这，无法打 cancelOpen	}	public class LocalBinder extends Binder {		public RFImagicBLEService getService() {			return RFImagicBLEService.this;		}	}	private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {		/*		 * 连接的状发生变化 (non-Javadoc)		 * 		 * @see		 * android.bluetooth.BluetoothGattCallback#onConnectionStateChange(android		 * .bluetooth.BluetoothGatt, int, int)		 */		@Override		public void onConnectionStateChange(BluetoothGatt gatt, int status,				int newState) {			String action = null;			if (newState == BluetoothProfile.STATE_CONNECTED) {				action = ACTION_GATT_CONNECTED;				gatt.discoverServices();			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {				action = ACTION_GATT_DISCONNECTED;			}			if (action != null && !action.equals("")) {				broadcastUpdate(action);			}		}		/*		 * 搜索device中的services (non-Javadoc)		 * 		 * @see		 * android.bluetooth.BluetoothGattCallback#onServicesDiscovered(android		 * .bluetooth.BluetoothGatt, int)		 */		@Override		public void onServicesDiscovered(BluetoothGatt gatt, int status) {			Log.w(RFImagicManage.RFSTAR,					"eeeeeeee  onServicesDiscovered received: " + status);			if (status == BluetoothGatt.GATT_SUCCESS) {				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);			} else {				Log.w(RFImagicManage.RFSTAR, "onServicesDiscovered received: "						+ status);			}		}		/*		 * 读取特征值 (non-Javadoc)		 * 		 * @see		 * android.bluetooth.BluetoothGattCallback#onCharacteristicRead(android		 * .bluetooth.BluetoothGatt,		 * android.bluetooth.BluetoothGattCharacteristic, int)		 */		public void onCharacteristicRead(BluetoothGatt gatt,				android.bluetooth.BluetoothGattCharacteristic characteristic,				int status) {			if (status == BluetoothGatt.GATT_SUCCESS) {				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);			} else {				Log.d(RFImagicManage.RFSTAR, "onCharacteristicRead received: "						+ status);			}		}		/*		 * 特征值的变化 (non-Javadoc)		 * 		 * @see		 * android.bluetooth.BluetoothGattCallback#onCharacteristicChanged(android		 * .bluetooth.BluetoothGatt,		 * android.bluetooth.BluetoothGattCharacteristic)		 */		public void onCharacteristicChanged(BluetoothGatt gatt,				android.bluetooth.BluetoothGattCharacteristic characteristic) {			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);		}		/*		 * 读取信号 (non-Javadoc)		 * 		 * @see		 * android.bluetooth.BluetoothGattCallback#onReadRemoteRssi(android.		 * bluetooth.BluetoothGatt, int, int)		 */		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {			if (gatt.connect()) {				broadcastUpdate(ACTION_GAT_RSSI);				// Log.d(RFstarManage.RFSTAR,				// "11111111111111111 onReadRemoteRssi  : " + rssi);			}		}	};	/**	 * 发送数据到广播	 * 	 * @param action	 */	private void broadcastUpdate(String action) {		Intent intent = new Intent(action);		sendBroadcast(intent);	}	/**	 * 发送带蓝牙信息的到广播	 * 	 * @param action	 * @param characteristic	 */	private void broadcastUpdate(String action,			BluetoothGattCharacteristic characteristic) {		Intent intent = new Intent(action);		// For all other profiles, writes the data formatted in HEX.		final byte[] data = characteristic.getValue();		if (data != null && data.length > 0) {			intent.putExtra(EXTRA_DATA, characteristic.getValue());			intent.putExtra(RFSTAR_CHARACTERISTIC_ID, characteristic.getUuid()					.toString());		}		sendBroadcast(intent);	}	public void readValue(BluetoothDevice device,			BluetoothGattCharacteristic characteristic) {		// TODO Auto-generated method stub		BluetoothGatt gatt = this.getBluetoothGatt(device);		if (gatt == null) {			Log.w(RFImagicManage.RFSTAR, "kBluetoothGatt 为没有初始化，所以不能读取数据");			return;		}		gatt.readCharacteristic(characteristic);	}	public void writeValue(BluetoothDevice device,			BluetoothGattCharacteristic characteristic) {		// TODO Auto-generated method stub		BluetoothGatt gatt = this.getBluetoothGatt(device);		if (gatt == null) {			Log.w(RFImagicManage.RFSTAR, "kBluetoothGatt 为没有初始化，所以不能写入数据");			return;		}		gatt.writeCharacteristic(characteristic);		Log.d(RFImagicManage.RFSTAR,				"55 connect :  连接上  数量： " + arrayGatts.size());	}	public void setCharacteristicNotification(BluetoothDevice device,			BluetoothGattCharacteristic characteristic, boolean enable) {		// TODO Auto-generated method stub		BluetoothGatt gatt = this.getBluetoothGatt(device);		if (gatt == null) {			Log.w(RFImagicManage.RFSTAR, "kBluetoothGatt 为没有初始化，所以不能发送使能数据");			return;		}		// Log.w(RFstarManage.RFSTAR, "111111111111 Notification :  "		// + characteristic.getUuid().toString());		gatt.setCharacteristicNotification(characteristic, enable);	}	/**	 * 获取services	 * 	 * @return	 */	public List<BluetoothGattService> getSupportedGattServices(			BluetoothDevice device) {		BluetoothGatt gatt = this.getBluetoothGatt(device);		if (gatt == null) {			// Log.w(RFstarManage.RFSTAR, "111111111111  services is null ");			return null;		}		return gatt.getServices();	}	// 从arrayGatts匹配出与device中address想同的BluetoothGatt	private BluetoothGatt getBluetoothGatt(BluetoothDevice device) {		BluetoothGatt gatt = null;		for (BluetoothGatt tmpGatt : arrayGatts) {			if (tmpGatt.getDevice().getAddress().equals(device.getAddress())) {				gatt = tmpGatt;			}		}		return gatt;	}}