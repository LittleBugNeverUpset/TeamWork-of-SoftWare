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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * AlarmInitReceiver 继承自 BroadcastReceiver，用于在设备启动或应用被重启时，
 * 重新初始化已经设置的笔记提醒闹钟（Alarm）。
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    // 查询笔记数据库所需要的字段（笔记ID和提醒时间）
    private static final String [] PROJECTION = new String [] {
            NoteColumns.ID,                // 笔记的唯一标识 ID
            NoteColumns.ALERTED_DATE       // 笔记的提醒时间
    };

    // 表示列索引，用于从查询结果中提取相应的数据
    private static final int COLUMN_ID                = 0; // ID 列的索引
    private static final int COLUMN_ALERTED_DATE      = 1; // 提醒时间列的索引

    /**
     * 当接收到广播时调用的方法，该方法会重新设置所有未过期的笔记提醒闹钟。
     * @param context 上下文，用于访问系统服务和内容提供者。
     * @param intent 广播意图，通常是系统发出的设备启动广播。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前的系统时间
        long currentDate = System.currentTimeMillis();

        // 查询数据库，获取所有提醒时间在未来的笔记（提醒时间大于当前时间）
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                PROJECTION,  // 要查询的列
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,
                new String[] { String.valueOf(currentDate) }, // 查询条件中的参数：当前时间
                null);

        // 如果查询结果不为空
        if (c != null) {
            // 检查查询结果是否至少有一条记录
            if (c.moveToFirst()) {
                do {
                    // 获取提醒时间
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);

                    // 创建用于接收闹钟提醒的 Intent，指定要启动的接收器为 AlarmReceiver
                    Intent sender = new Intent(context, AlarmReceiver.class);

                    // 设置数据，使用笔记的 ID 作为标识
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID)));

                    // 创建 PendingIntent，指定当闹钟到达时要触发的广播
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);

                    // 获取 AlarmManager 系统服务
                    AlarmManager alermManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    // 设置闹钟，指定 RTC_WAKEUP 模式（即在闹钟时间到达时唤醒设备）
                    alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);
                } while (c.moveToNext()); // 遍历查询结果中的每一条记录
            }
            // 关闭 Cursor 释放资源
            c.close();
        }
    }
}
