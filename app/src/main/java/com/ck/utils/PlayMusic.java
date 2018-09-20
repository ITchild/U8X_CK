/* 
 * @Title:  PlayMusic.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-26 上午9:48:30 
 * @version:  V1.0 
 */
package com.ck.utils;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.ck.main.App_DataPara;
import com.hc.u8x_ck.R;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author
 * @data: 2016-8-26 上午9:48:30
 * @version: V1.0
 */
public class PlayMusic {
	public static MediaPlayer mPlay;
	private static boolean mBFirst = true;

	public static void playPromptMusic(Context context, boolean bCreat) {
		App_DataPara app = (App_DataPara) context.getApplicationContext();
		if (!app.m_bPlayMusic)
			return;
		if (mBFirst) {
			mBFirst = false;
			AudioManager m_AudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			// 将系统音量设置最大音量
			int max = m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
			m_AudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, max, 0);
			// 将音乐音量调到最大
			int max1 = m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			m_AudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max1, 0);
			mPlay = MediaPlayer.create(context, R.raw.tick_sound);
		}

		if (bCreat)
			mPlay = MediaPlayer.create(context, R.raw.tick_sound);
		mPlay.setLooping(false);
		mPlay.start();

	}
}
