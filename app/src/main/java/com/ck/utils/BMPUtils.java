/* 
 * @Title:  BMPUtils.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-11-24 上午11:26:53 
 * @version:  V1.0 
 */
package com.ck.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author
 * @data: 2016-11-24 上午11:26:53
 * @version: V1.0
 */
public class BMPUtils {
	
	public static byte[] getBMPByteArr(int[] pixels, int w, int h) {
		byte[] rgb = addBMP_RGB_888(pixels, w, h);
		byte[] header = addBMPImageHeader(rgb.length);
		byte[] infos = addBMPImageInfosHeader(w, h);

		byte[] buffer = new byte[54 + rgb.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(infos, 0, buffer, 14, infos.length);
		System.arraycopy(rgb, 0, buffer, 54, rgb.length);
		return buffer;
	}
	
	private static byte[] mBMPImageHeader = new byte[14];
	private static byte[] addBMPImageHeader(int size) {
		
		mBMPImageHeader[0] = 0x42;
		mBMPImageHeader[1] = 0x4D;
		mBMPImageHeader[2] = (byte) (size >> 0);
		mBMPImageHeader[3] = (byte) (size >> 8);
		mBMPImageHeader[4] = (byte) (size >> 16);
		mBMPImageHeader[5] = (byte) (size >> 24);
		mBMPImageHeader[6] = 0x00;
		mBMPImageHeader[7] = 0x00;
		mBMPImageHeader[8] = 0x00;
		mBMPImageHeader[9] = 0x00;
		mBMPImageHeader[10] = 0x36;
		mBMPImageHeader[11] = 0x00;
		mBMPImageHeader[12] = 0x00;
		mBMPImageHeader[13] = 0x00;
		return mBMPImageHeader;
	}
	private static byte[] mBMPImageInfosHeader = new byte[40];
	private static byte[] addBMPImageInfosHeader(int w, int h) {
		
		mBMPImageInfosHeader[0] = 0x28;
		mBMPImageInfosHeader[1] = 0x00;
		mBMPImageInfosHeader[2] = 0x00;
		mBMPImageInfosHeader[3] = 0x00;
		mBMPImageInfosHeader[4] = (byte) (w >> 0);
		mBMPImageInfosHeader[5] = (byte) (w >> 8);
		mBMPImageInfosHeader[6] = (byte) (w >> 16);
		mBMPImageInfosHeader[7] = (byte) (w >> 24);
		mBMPImageInfosHeader[8] = (byte) (h >> 0);
		mBMPImageInfosHeader[9] = (byte) (h >> 8);
		mBMPImageInfosHeader[10] = (byte) (h >> 16);
		mBMPImageInfosHeader[11] = (byte) (h >> 24);
		mBMPImageInfosHeader[12] = 0x01;
		mBMPImageInfosHeader[13] = 0x00;
		mBMPImageInfosHeader[14] = 0x18;
		mBMPImageInfosHeader[15] = 0x00;
		mBMPImageInfosHeader[16] = 0x00;
		mBMPImageInfosHeader[17] = 0x00;
		mBMPImageInfosHeader[18] = 0x00;
		mBMPImageInfosHeader[19] = 0x00;
		mBMPImageInfosHeader[20] = 0x00;
		mBMPImageInfosHeader[21] = 0x00;
		mBMPImageInfosHeader[22] = 0x00;
		mBMPImageInfosHeader[23] = 0x00;
		mBMPImageInfosHeader[24] = (byte) 0xE0;
		mBMPImageInfosHeader[25] = 0x01;
		mBMPImageInfosHeader[26] = 0x00;
		mBMPImageInfosHeader[27] = 0x00;
		mBMPImageInfosHeader[28] = 0x02;
		mBMPImageInfosHeader[29] = 0x03;
		mBMPImageInfosHeader[30] = 0x00;
		mBMPImageInfosHeader[31] = 0x00;
		mBMPImageInfosHeader[32] = 0x00;
		mBMPImageInfosHeader[33] = 0x00;
		mBMPImageInfosHeader[34] = 0x00;
		mBMPImageInfosHeader[35] = 0x00;
		mBMPImageInfosHeader[36] = 0x00;
		mBMPImageInfosHeader[37] = 0x00;
		mBMPImageInfosHeader[38] = 0x00;
		mBMPImageInfosHeader[39] = 0x00;
		return mBMPImageInfosHeader;
	}

	private static byte[] addBMP_RGB_888(int[] b, int w, int h) {
		int len = b.length;
		System.out.println(b.length);
		byte[] buffer = new byte[w * h * 3];
		int offset = 0;
		for (int i = len - 1; i >= w; i -= w) {

			int end = i, start = i - w + 1;
			for (int j = start; j <= end; j++) {
				buffer[offset] = (byte) (b[j] >> 0);
				buffer[offset + 1] = (byte) (b[j] >> 8);
				buffer[offset + 2] = (byte) (b[j] >> 16);
				offset += 3;
			}
		}
		return buffer;
	}

	/*
	 * bitmap转base64   将图片转为String类型进行存储
	 * */
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		byte[] bitmapBytes = bitmapToByteArr(bitmap);
		if(null != bitmapBytes) {
			result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
		}
		return result;
	}

	/**
	 * bitmap转为二进制数组
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmapToByteArr(Bitmap bitmap){
		ByteArrayOutputStream baos = null;
		byte[] bitmapBytes = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				bitmapBytes = baos.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmapBytes;
	}

	/**
	 * base64转为bitmap
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data,BitmapFactory.Options options) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return bytesToBitmap(bytes,options);
	}

	public static Bitmap bytesToBitmap(byte[] bytes ,BitmapFactory.Options options){
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);
	}

}
