package com.ihongqiqu.sectionlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义的View，实现ListView A~Z快速索引效果
 * 
 * @author jingle1267@163.com
 * 
 */
public class SlideBar extends View {
	private Paint paint = new Paint();
	private OnTouchLetterChangeListenner listenner;
	// 是否画出背景
	private boolean showBg = false;
	// 选中的项
	private int choose = -1;
	// 准备好的A~Z的字母数组
	public static String[] letters = { "#", "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };
	
	// 构造方法
	public SlideBar(Context context) {
		super(context);
	}

	public SlideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setSection(String[] random) {
		String[] arr = random;
		for (int i = 0; i < arr.length -1; i++){    //最多做n-1趟排序
            for(int j = 0 ;j < arr.length - i - 1; j++){    //对当前无序区间score[0......length-i-1]进行排序(j的范围很关键，这个范围是在逐步缩小的)
                if(arr[j].toCharArray()[0] > arr[j + 1].toCharArray()[0]){    //把小的值交换到后面
                    // int temp = arr[j].toCharArray()[0];
                    String tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }            
            /*System.out.print("第" + (i + 1) + "次排序结果：");
            for(int a = 0; a < arr.length; a++){
                System.out.print(arr[a] + "\t");
            }
            System.out.println("");*/
        }
		letters = arr;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 *
	 * @param context 上下文
	 * @param dpValue 尺寸dip
	 * @return 像素值
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float fontSize = dip2px(getContext(), 13);
		// 获取宽和高
		int width = getWidth();
		int height = getHeight() - (int)fontSize * 2;
		// 每个字母的高度
		int singleHeight = height / letters.length;
		if (showBg) {
			// 画出背景
			canvas.drawColor(Color.parseColor("#03000000"));
		}
		// 画字母
		for (int i = 0; i < letters.length; i++) {
			paint.setColor(Color.parseColor("#666666"));
			// 设置字体格式
			// paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			if (fontSize < singleHeight) {
				paint.setTextSize(fontSize);
			} else {
				paint.setTextSize(singleHeight * 0.8f);
			}
			// 如果这一项被选中，则换一种颜色画
			if (i == choose) {
				paint.setColor(Color.parseColor("#3498db"));
				paint.setFakeBoldText(true);
			}
			// 要画的字母的x,y坐标
			float posX = width / 2 - paint.measureText(letters[i]) / 2;
			float posY = fontSize + i * singleHeight + singleHeight / 2;
			// 画出字母
			canvas.drawText(letters[i], posX, posY, paint);
			// 重新设置画笔
			paint.reset();
		}
	}

	/**
	 * 处理SlideBar的状态
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final float y = event.getY();
		// 算出点击的字母的索引
		final int index = (int) (y / getHeight() * letters.length);
		// 保存上次点击的字母的索引到oldChoose
		final int oldChoose = choose;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			showBg = true;
			if (oldChoose != index && listenner != null && index >= 0
					&& index < letters.length) {
				if (!TextUtils.isEmpty(letters[index])) {
					choose = index;
					listenner.onTouchLetterChange(showBg, letters[index], index + 1);
					invalidate();
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (oldChoose != index && listenner != null && index >= 0
					&& index < letters.length) {
				choose = index;
				if (!TextUtils.isEmpty(letters[index])) {
					listenner.onTouchLetterChange(showBg, letters[index], index + 1);
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			showBg = false;
			choose = -1;
			if (listenner != null) {
				if (index <= 0) {
					listenner.onTouchLetterChange(showBg, "#", 1);
				} else if (index > 0 && index < letters.length) {
					if (!TextUtils.isEmpty(letters[index])) {
						listenner.onTouchLetterChange(showBg, letters[index], index + 1);
					}
				} else if (index >= letters.length) {
					listenner.onTouchLetterChange(showBg, "Z", letters.length);
				}
				listenner.onTouchUp();
			}
			invalidate();
			break;
		}
		return true;
	}

	/**
	 * 回调方法，注册监听器
	 * 
	 * @param listenner
	 */
	public void setOnTouchLetterChangeListenner(
			OnTouchLetterChangeListenner listenner) {
		this.listenner = listenner;
	}

	/**
	 * SlideBar 的监听器接口
	 * 
	 * @author Folyd
	 * 
	 */
	public interface OnTouchLetterChangeListenner {

		void onTouchLetterChange(boolean isTouched, String s, int pos);
		
		void onTouchUp();
	}

}

