package com.ihongqiqu.sectionlistview;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class ChooseAreaCodeActivity extends ListActivity {

	private final String TAG = ChooseAreaCodeActivity.class.getSimpleName();
	
	private ArrayList<String> nationCodes = new ArrayList<String>();
	private HashMap<String, String> allAreaNames = new HashMap<String, String>();
	private HashMap<String, String> phoneCodes = new HashMap<String, String>();
	
	private ArrayList<String> sections = new ArrayList<String>();
	private HashMap<String, Integer> sectionIndex = new HashMap<String, Integer>();
	
	private SlideBar mSlideBar = null; // 快速索引
	private RelativeLayout relativeLayout = null;
	private SimpleAdapter simpleAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.area_code_choose_layout);
		relativeLayout = (RelativeLayout) findViewById(R.id.area_code_layout);
		LocationLoadUtil.exportPhoneCode(this, new LocationLoadUtil.OnLocationLoadListener() {
			public void onFinised(Object v1, Object v2, Object v3, Object v4) {
				nationCodes = (ArrayList<String>) v1;
				phoneCodes = (HashMap<String, String>) v2;
				allAreaNames = (HashMap<String, String>) v3;
				initSections();
				Log.i(TAG, "sections.size() : " + sections.size());
			}
		});
		initializeAdapter();
		initSlideBar();
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

	private void initSlideBar() {
		mSlideBar = new SlideBar(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dip2px(ChooseAreaCodeActivity.this, 25), RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		relativeLayout.addView(mSlideBar, layoutParams);
		getListView().setVerticalScrollBarEnabled(false);
		mSlideBar.setVisibility(View.INVISIBLE);
		mSlideBar.setBackgroundResource(R.drawable.contact_quick_index_bg);
		mSlideBar.setOnTouchLetterChangeListenner(new SlideBar.OnTouchLetterChangeListenner() {

			@Override
			public void onTouchLetterChange(boolean isTouched, String s, int pos) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onTouchLetterChange()");

				if (TextUtils.isEmpty(s) || simpleAdapter == null
						|| (simpleAdapter.getSectionSize() < 1)
						|| !sections.contains(s.toLowerCase())) {
					return;
				}
				int index = sectionIndex.get(s.toLowerCase());// + pos;
				Log.i(TAG, "s : " + s);
				Log.i(TAG, "pos : " + pos);
				Log.i(TAG, "index : " + index);
				if (android.os.Build.VERSION.SDK_INT >= 8) {
					getListView().setSelection(index);
				} else {
					getListView().setSelection(index);
				}
				Log.i(TAG, "listView size : " + getListView().getAdapter().getCount());
				// showCustomToast(s);
			}

			@Override
			public void onTouchUp() {
				// TODO Auto-generated method stub
//				if (mToast != null) {
//					handler.postDelayed(new Runnable() {
//
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							if (mToast != null) {
//								mToast.cancel();
//							}
//						}
//					}, 1000);
//				}
			}
		});
		
		String[] secArr = new String[sections.size()];
		sections.toArray(secArr);
		for (int m = 0; m < secArr.length; m++) {
			secArr[m] = secArr[m].toUpperCase();
		}
		mSlideBar.setSection(secArr);
		mSlideBar.postInvalidate();
		mSlideBar.setVisibility(View.VISIBLE);
	}
	
	private void initSections() {
		Set<String> set = allAreaNames.keySet();
		for (String key : set) {
			String value = allAreaNames.get(key);
			String[] arr = PinyinHelper.toHanyuPinyinStringArray(value.charAt(0));
			if (arr != null) {
				String k = arr[0];
				if (!TextUtils.isEmpty(k)) {
					addSection(k.substring(0, 1));
				}
			} else {
				addSection(value.substring(0, 1));
			}
		}
		Collections.sort(sections);
	}

	private void addSection(String str) {
		if (!sections.contains(str)) {
			Log.d(TAG, "str : " + str);
			sections.add(str);
		}
	}
	
	private void initializeAdapter() {
		getListView().setFastScrollEnabled(false);
		simpleAdapter = new SimpleAdapter(this,
				R.layout.area_code_choose_item, R.id.textview1);
		setListAdapter(simpleAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = (Item) getListView().getAdapter().getItem(position);
	    if (item != null) {
	        Toast.makeText(this, "Item " + position + ": " + item.text, Toast.LENGTH_SHORT).show();
	    } else {
	        Toast.makeText(this, "Item " + position, Toast.LENGTH_SHORT).show();
	    }
	    String key = getKey(item.text);
	    Log.d(TAG, "key : " + key);
	    String code = phoneCodes.get(key);
	    Log.d(TAG, "code : " + code);
	}
	
	private String getKey(String str) {
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		Set<String> set = allAreaNames.keySet();
		for (String key : set) {
			String value = allAreaNames.get(key);
			if (str.equals(value)) {
				return key;
			}
		}
		return null;
	}
	
	class SimpleAdapter extends ArrayAdapter<Item> implements PinnedSectionListView.PinnedSectionListAdapter {

        private final int[] COLORS = new int[] {
            android.R.color.holo_blue_dark,  android.R.color.holo_green_light,
            android.R.color.holo_purple,  android.R.color.holo_red_light };

        public SimpleAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);

            final int sectionsNumber = sections.size();// 'Z' - 'A' + 1;
            prepareSections(sectionsNumber);

            int sectionPosition = 0, listPosition = 0;
            for (int i = 0; i < sections.size(); i++) {
            	Item section = new Item(Item.SECTION, sections.get(i).toUpperCase());
            	section.sectionPosition = i;
            	section.listPosition = listPosition++;
            	onSectionAdded(section, sectionPosition);
            	add(section);
            	
            	sectionIndex.put(sections.get(i), section.listPosition);
            	
            	Set<String> set = allAreaNames.keySet();
        		for (String key : set) {
        			String value = allAreaNames.get(key);
        			String[] arr = PinyinHelper.toHanyuPinyinStringArray(value.charAt(0));
        			String indexKey = null;
        			if (arr != null) {
        				String k = arr[0];
        				if (!TextUtils.isEmpty(k)) {
        					//addSection(k.substring(0, 1));
        					indexKey = k.substring(0, 1);
        				}
        			} else {
        				//addSection(value.substring(0, 1));
        				indexKey = value.substring(0, 1);
        			}
        			if (sections.get(i).equals(indexKey)) {
        				Item item = new Item(Item.ITEM, value);
        				item.sectionPosition = sectionPosition;
        				item.listPosition = listPosition++;
        				add(item);
        			}
        		}
        		sectionPosition++;
            	
            }
            
        }

        protected int getSectionSize() {
        	if (sections != null) {
        		return sections.size();
        	}
        	return 0;
        }
        
        protected void prepareSections(int sectionsNumber) { }
        protected void onSectionAdded(Item section, int sectionPosition) { }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTextColor(Color.DKGRAY);
            view.setTag("" + position);
            Item item = getItem(position);
            if (item.type == Item.SECTION) {
            	view.setCompoundDrawables(null, null, null, getResources().getDrawable(android.R.color.black));
                // view.setOnClickListener(PinnedSectionListActivity.this);
                // view.setBackgroundColor(Color.GRAY);
            	view.setBackgroundResource(R.drawable.textbar_1_bg);
            } else {
            	view.setBackgroundResource(R.drawable.textbar_2_bg_03);
            }
            return view;
        }

        @Override public int getViewTypeCount() {
            return 2;
        }

        @Override public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == Item.SECTION;
        }

    }
	
	static class Item {

		public static final int ITEM = 0;
		public static final int SECTION = 1;

		public final int type;
		public final String text;

		public int sectionPosition;
		public int listPosition;

		public Item(int type, String text) {
		    this.type = type;
		    this.text = text;
		}

		@Override public String toString() {
			return text;
		}

	}
	
}
