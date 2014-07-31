package com.worthed.sectionlistview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * 读取 assets/location.json文件，读取地理位置经纬度和名称
 * 
 * @author tangyuchun
 * 
 */
public class LocationLoadUtil {
	public interface OnLocationLoadListener {
		public void onFinised(Object v1, Object v2, Object v3, Object v4);
	}

	private static String getRightLang(Context context) {
		String localLang = "auto"; //UserKeeper.readStringLocale(context);
		if (localLang.equals("auto")) {
			Locale l = context.getResources().getConfiguration().locale;
			localLang = l.getLanguage();
			if (TextUtils.isEmpty(localLang)) {
				localLang = "en";
			}
		}
		return localLang;
	}

	/**
	 * 解析电话区号:返回国家代号，国家名称，地区代号
	 * 
	 * @param context
	 * @param lang
	 * @param listener
	 */
	public static void exportPhoneCode(Context context, OnLocationLoadListener listener) {
		HashMap<String, String> allAreaNames = new HashMap<String, String>();
		ArrayList<String> nationCodes = new ArrayList<String>();// 所有国家的代号
		HashMap<String, String> phoneCodes = new HashMap<String, String>();// 电话区号
		String lang = LocationLoadUtil.getRightLang(context);

		/* 先要解析区号,再解析国家,防止部分国家没有区号 */
		try {
			InputStream stream = context.getAssets().open("mobile_code.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String lineString = null;
			String phoneLineCodes[] = null;// 一行数据:国,区号
			while ((lineString = reader.readLine()) != null) {
				phoneLineCodes = lineString.split(" ");
				if (phoneLineCodes == null || phoneLineCodes.length != 2 || TextUtils.isEmpty(phoneLineCodes[0])
						|| TextUtils.isEmpty(phoneLineCodes[1])) {
					Log.e("TEST", "跳过  " + lineString);
					continue;
				}
				phoneCodes.put(phoneLineCodes[0], phoneLineCodes[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			/* 解析国家代号和名称 */
			InputStream inStream = context.getAssets().open("area_" + lang + ".txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

			String line = null;
			String lineCodes[] = null;// 一行数据:国，市，区，翻译
			while ((line = reader.readLine()) != null) {
				lineCodes = exportLineCodes(line);
				if (lineCodes == null) {
					continue;
				}
				// 只有国
				if (!TextUtils.isEmpty(lineCodes[0]) && !TextUtils.isEmpty(lineCodes[3])
						&& TextUtils.isEmpty(lineCodes[1]) && TextUtils.isEmpty(lineCodes[2])) {
					if (!nationCodes.contains(lineCodes[0]) && phoneCodes.containsKey(lineCodes[0])) {// 保证有该国家的区号
						nationCodes.add(lineCodes[0]);
					}
					allAreaNames.put(lineCodes[0], lineCodes[3]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (listener != null) {
			listener.onFinised(nationCodes, phoneCodes, allAreaNames, null);
		}
	}

	/**
	 * 扫描一次文件
	 * 
	 * @param context
	 * @param lang
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static void exportArea(Context context, OnLocationLoadListener listener) throws IOException {
		HashMap<String, String> allAreaNames = new HashMap<String, String>();
		ArrayList<String> nationCodes = new ArrayList<String>();// 所有国家的代号
		HashMap<String, List<String>> cityCodes = new HashMap<String, List<String>>(); // 国家-->市映射
		HashMap<String, List<String>> countyCodes = new HashMap<String, List<String>>();// 市-->县映射

		String lang = LocationLoadUtil.getRightLang(context);
		InputStream inStream = context.getAssets().open("area_" + lang + ".txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

		String line = null;
		String lineCodes[] = null;// 一行数据:国，市，区，翻译
		while ((line = reader.readLine()) != null) {
			lineCodes = exportLineCodes(line);
			if (lineCodes == null) {
				continue;
			}
			if (!TextUtils.isEmpty(lineCodes[0])) { // 国
				if (!nationCodes.contains(lineCodes[0])) {
					nationCodes.add(lineCodes[0]);
				}
				if (cityCodes.get(lineCodes[0]) == null) {
					cityCodes.put(lineCodes[0], new ArrayList<String>());
				}
				if (!TextUtils.isEmpty(lineCodes[1])) {// 市
					if (!cityCodes.get(lineCodes[0]).contains(lineCodes[1])) {
						cityCodes.get(lineCodes[0]).add(lineCodes[1]);
					}
					if (countyCodes.get(lineCodes[1]) == null) {
						countyCodes.put(lineCodes[1], new ArrayList<String>());
					}
					if (!TextUtils.isEmpty(lineCodes[2])) {// 区
						if (!countyCodes.get(lineCodes[1]).contains(lineCodes[2])) {
							countyCodes.get(lineCodes[1]).add(lineCodes[2]);
						}
					}
				}
			}
			for (int i = 2; i >= 0; i--) {
				if (lineCodes[i] != null) {
					allAreaNames.put(lineCodes[i], lineCodes[3]);
					break;
				}
			}
		}
		if (listener != null) {
			listener.onFinised(nationCodes, cityCodes, countyCodes, allAreaNames);
		}
	}

	/**
	 * 将一行文本拆分成4个字段，文本格式必须为：　国＿市＿区｜翻译　eg: KR_Gangwon-do_Donghae|东海市
	 * 
	 * @param line
	 *            一行文本
	 * @param codes
	 */
	private static String[] exportLineCodes(String line) {
		String[] lineCodes = new String[4];
		String splits[] = line.split("\\|");// |是转义字符，需要转义
		if (splits.length != 2 || splits[0] == null || splits[1] == null) {
			return null;
		}
		lineCodes[3] = splits[1].trim();// 翻译
		String codesSplit = splits[0];// 地区字段,以下划线 _为分割点
		String[] codeArray = codesSplit.split("_");
		for (int i = 0; i < codeArray.length; i++) {
			lineCodes[i] = codeArray[i];
		}
		return lineCodes;
	}

}
