package com.imagic.lamp.kevin.ble;import java.util.ArrayList;import android.app.Application;public class RFImagicApp extends Application {	public static final String KTag = "rfstar";	public static final String RFSTAR_DEVICE = "rfstar_device";	public static final String COLOR = "_color";	public static final String CONTROLLER_LAMP_MAC = "RFSTAR_LAMP_MAC";	public ArrayList<RFLampDevice> lampConnecteArray = null;	public RFImagicManage manager = null;	@Override	public void onCreate() {		// TODO Auto-generated method stub		super.onCreate();		manager = new RFImagicManage(getApplicationContext());		lampConnecteArray = new ArrayList<RFLampDevice>();	}}