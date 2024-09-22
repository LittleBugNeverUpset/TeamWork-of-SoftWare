/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * AlarmReceiver 是一个广播接收器类，用于接收闹钟提醒的广播，并启动相应的提醒活动。
 */
public class AlarmReceiver extends BroadcastReceiver {
    /**
     * 当广播接收器接收到闹钟广播时调用的方法。
     * 它将会启动提醒界面（AlarmAlertActivity），让用户看到提醒内容。
     *
     * @param context 上下文，用于启动新活动。
     * @param intent 广播意图，包含相关的闹钟提醒信息。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将收到的 Intent 的目标类设置为 AlarmAlertActivity，即提醒界面
        intent.setClass(context, AlarmAlertActivity.class);

        // 添加 FLAG_ACTIVITY_NEW_TASK 标志，表示新启动的活动将作为一个新的任务来执行
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 启动 AlarmAlertActivity 来显示提醒
        context.startActivity(intent);
    }
}
