package com.imagic.lamp.kevin.ble;import java.io.InputStream;import android.content.Context;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.Canvas;import android.graphics.Color;import android.graphics.LinearGradient;import android.graphics.Paint;import android.graphics.Point;import android.graphics.PorterDuff;import android.graphics.PorterDuffXfermode;import android.graphics.Shader;import android.util.AttributeSet;import android.view.MotionEvent;import android.view.ViewGroup.LayoutParams;import android.widget.ImageView;/** * 亮度 *  * @author Kevin.wu *  */public class LightView extends ImageView {	Paint p;	private Bitmap mBitmap;	private Shader mShader;	private int width, height; // 图片的宽高	private LightRGBDelegate delegate = null;	private static final int INSTANCE = 10;	/** 亮度百分比 */	private String currentLevelLight = "0%";	/**	 * 开始触屏和结束触屏	 */	Point startPoint = null, endPoint = null;	private int currentColor = Color.WHITE;	public LightView(Context context, AttributeSet attrs) {		super(context, attrs);		// TODO Auto-generated constructor stub	}	public LightView(Context context, int pic) {		super(context);		setFocusable(true);		this.setClickable(true);		InputStream is = context.getResources().openRawResource(pic);		Bitmap tmpBit = BitmapFactory.decodeStream(is);		this.width = tmpBit.getWidth();		this.height = tmpBit.getHeight();		this.setLayoutParams(new LayoutParams(width, height));		mBitmap = Bitmap.createBitmap(this.width, this.height,				Bitmap.Config.ALPHA_8);		tmpBit.recycle();		drawIntoBitmap(mBitmap);		startPoint = new Point();		endPoint = new Point();	}	public void setColor(int color) {		p = new Paint();		if (color == Color.TRANSPARENT) {			p.setColor(Color.WHITE);		} else {			p.setColor(color);		}		this.currentColor = color;		mShader = new LinearGradient(0, 0, 0, this.width, new int[] {				Color.BLACK, color, Color.WHITE }, null, Shader.TileMode.MIRROR);		invalidate();	}	@Override	protected void onDraw(Canvas canvas) {		canvas.drawColor(Color.TRANSPARENT);		p.setShader(mShader);		int width = (this.getWidth() - this.width) / 2;		int height = (this.getHeight() - this.height) / 2;		canvas.drawBitmap(mBitmap, width, height, p);	}	/**	 * 设置亮度比例	 * 	 * @param level	 */	public void setLevelLight(String level) {		this.currentLevelLight = level;		drawIntoBitmap(mBitmap);		invalidate();	}	private void drawIntoBitmap(Bitmap bm) {		float x = bm.getWidth();		float y = bm.getHeight();		Canvas c = new Canvas(bm);		Paint p = new Paint();		p.setAntiAlias(true);		c.drawCircle(x / 2, y / 2, x / 2, p);		p.setAlpha(0x30);		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));		p.setTextSize(60);		p.setTextAlign(Paint.Align.CENTER);		Paint.FontMetrics fm = p.getFontMetrics();		c.drawText(this.currentLevelLight, width / 2, (height - fm.ascent) / 2,				p);		// p.setAlpha(0x70);		this.setImageBitmap(mBitmap);	}	@Override	public boolean onTouchEvent(MotionEvent event) {		// TODO Auto-generated method stub		switch (event.getAction()) {		case MotionEvent.ACTION_MOVE:			endPoint.x = (int) event.getX();			endPoint.y = (int) event.getY();			if ((endPoint.y - startPoint.y) > INSTANCE) {				this.setLevelLight(false);			} else if ((endPoint.y - startPoint.y) < INSTANCE) {				this.setLevelLight(true);			}			break;		case MotionEvent.ACTION_DOWN:			startPoint.x = (int) event.getX();			startPoint.y = (int) event.getY();			break;		case MotionEvent.ACTION_UP:			endPoint.x = (int) event.getX();			endPoint.y = (int) event.getY();			break;		default:			break;		}		return super.onTouchEvent(event);	}	public void setOnLevelLightDelegate(LightRGBDelegate delegate) {		this.delegate = delegate;	}	/**	 * 控制上下，	 * 	 * @param boo	 *            真为增加，假为减少	 */	private void setLevelLight(boolean boo) {		delegate.lightColor(this, boo, this.currentColor);	}	/**	 * 获取颜色的比例	 * 	 * @author kevin	 * 	 */	public interface LightRGBDelegate {		void lightColor(LightView view, boolean boo, int color);	}}