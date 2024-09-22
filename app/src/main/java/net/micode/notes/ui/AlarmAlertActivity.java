/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;

import java.io.IOException;

/**
 * AlarmAlertActivity 是用于处理笔记提醒的 Activity。
 * 当闹钟提醒到达时，Activity 会弹出一个对话框并播放闹钟声。
 */
public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    private long mNoteId; // 记录当前笔记的 ID
    private String mSnippet; // 当前笔记的片段（简要内容）
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 笔记片段的最大显示长度
    MediaPlayer mPlayer; // 用于播放闹钟声音的媒体播放器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏

        final Window win = getWindow();
        // 设置窗口在锁屏时显示
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // 如果屏幕未亮，则保持屏幕常亮，并解锁屏幕
        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        }

        // 获取启动该 Activity 的 Intent
        Intent intent = getIntent();

        try {
            // 从 Intent 中提取笔记 ID 和片段内容
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1)); // 获取笔记 ID
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId); // 获取笔记片段
            // 如果片段长度超过最大长度，进行截取并加上省略号
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? mSnippet.substring(0,
                    SNIPPET_PREW_MAX_LEN) + getResources().getString(R.string.notelist_string_info)
                    : mSnippet;
        } catch (IllegalArgumentException e) {
            // 如果获取笔记 ID 失败，打印堆栈信息并返回
            e.printStackTrace();
            return;
        }
        mPlayer = new MediaPlayer(); // 初始化 MediaPlayer
        // 检查当前笔记是否在数据库中可见
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog(); // 显示操作对话框
            playAlarmSound(); // 播放闹钟声音
        } else {
            finish(); // 如果笔记不可见，则关闭当前 Activity
        }
    }

    /**
     * 判断屏幕是否开启
     * @return 如果屏幕已亮，则返回 true；否则返回 false
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn(); // 调用 PowerManager 判断屏幕状态
    }

    /**
     * 播放闹钟声音
     */
    private void playAlarmSound() {
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM); // 获取默认闹钟铃声

        // 获取系统设置中静音模式会影响的音频流
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 如果闹钟流受静音模式影响，则设置播放静音流，否则播放闹钟流
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams); // 设置为静音模式流
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // 设置为闹钟音频流
        }
        try {
            // 设置音频来源为默认闹钟铃声
            mPlayer.setDataSource(this, url);
            mPlayer.prepare(); // 准备音频资源
            mPlayer.setLooping(true); // 设置音频循环播放
            mPlayer.start(); // 开始播放
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示提醒对话框，供用户选择操作
     */
    private void showActionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this); // 创建对话框
        dialog.setTitle(R.string.app_name); // 设置对话框标题为应用名称
        dialog.setMessage(mSnippet); // 设置对话框显示的内容为笔记片段
        dialog.setPositiveButton(R.string.notealert_ok, this); // 添加 "OK" 按钮
        // 如果屏幕已亮，则显示 "进入" 按钮
        if (isScreenOn()) {
            dialog.setNegativeButton(R.string.notealert_enter, this); // 添加 "进入" 按钮
        }
        // 显示对话框，并设置对话框关闭事件监听
        dialog.show().setOnDismissListener(this);
    }

    /**
     * 处理用户点击对话框按钮的事件
     */
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE:
                // 用户点击 "进入" 按钮，跳转到 NoteEditActivity 进行笔记编辑
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_UID, mNoteId); // 传递笔记 ID
                startActivity(intent); // 启动 NoteEditActivity
                break;
            default:
                break;
        }
    }

    /**
     * 当对话框关闭时调用，停止闹钟并关闭当前 Activity
     */
    public void onDismiss(DialogInterface dialog) {
        stopAlarmSound(); // 停止闹钟声音
        finish(); // 关闭当前 Activity
    }

    /**
     * 停止闹钟声音
     */
    private void stopAlarmSound() {
        if (mPlayer != null) {
            mPlayer.stop(); // 停止播放
            mPlayer.release(); // 释放 MediaPlayer 资源
            mPlayer = null; // 将引用置为空
        }
    }
}
