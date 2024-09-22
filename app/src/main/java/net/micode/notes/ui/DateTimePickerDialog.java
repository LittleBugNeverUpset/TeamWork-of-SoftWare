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

import java.util.Calendar;

import net.micode.notes.R;
import net.micode.notes.ui.DateTimePicker;
import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * 这个类创建了一个自定义的日期和时间选择对话框。
 * 它扩展了AlertDialog，允许用户选择日期和时间，
 * 使用DateTimePicker控件。
 */
public class DateTimePickerDialog extends AlertDialog implements OnClickListener {

    // 存储选择的日期和时间的Calendar实例
    private Calendar mDate = Calendar.getInstance();

    // 标记对话框是否为24小时制
    private boolean mIs24HourView;

    // 监听器，用于处理用户设置日期和时间的事件
    private OnDateTimeSetListener mOnDateTimeSetListener;

    // 自定义的DateTimePicker控件，用于选择日期和时间
    private DateTimePicker mDateTimePicker;

    /**
     * 日期和时间设置监听器的接口定义。
     */
    public interface OnDateTimeSetListener {
        void OnDateTimeSet(AlertDialog dialog, long date);
    }

    /**
     * DateTimePickerDialog的构造函数。
     *
     * @param context - 创建对话框的上下文
     * @param date - 初始显示在对话框中的日期和时间
     */
    public DateTimePickerDialog(Context context, long date) {
        super(context);

        // 初始化DateTimePicker并将其设置为对话框的视图
        mDateTimePicker = new DateTimePicker(context);
        setView(mDateTimePicker);

        // 为DateTimePicker设置日期和时间变化的监听器
        mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
            public void onDateTimeChanged(DateTimePicker view, int year, int month,
                                          int dayOfMonth, int hourOfDay, int minute) {
                // 更新内部Calendar实例以反映所选的日期和时间
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDate.set(Calendar.MINUTE, minute);

                // 更新对话框标题以显示当前日期和时间
                updateTitle(mDate.getTimeInMillis());
            }
        });

        // 设置初始日期和时间，并将秒数设置为0
        mDate.setTimeInMillis(date);
        mDate.set(Calendar.SECOND, 0);
        mDateTimePicker.setCurrentDate(mDate.getTimeInMillis());

        // 设置对话框的“确认”和“取消”按钮
        setButton(context.getString(R.string.datetime_dialog_ok), this);
        setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener)null);

        // 根据系统设置设置视图模式（12小时或24小时格式）
        set24HourView(DateFormat.is24HourFormat(this.getContext()));

        // 更新对话框的标题以显示当前日期和时间
        updateTitle(mDate.getTimeInMillis());
    }

    /**
     * 设置对话框是否为24小时制。
     *
     * @param is24HourView - true表示24小时制，false表示12小时制
     */
    public void set24HourView(boolean is24HourView) {
        mIs24HourView = is24HourView;
    }

    /**
     * 设置当用户设置新日期和时间时调用的监听器。
     *
     * @param callBack - 当日期和时间被设置时通知的监听器
     */
    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }

    /**
     * 更新对话框的标题以显示所选的日期和时间。
     *
     * @param date - 以毫秒为单位表示所选日期和时间的时间戳
     */
    private void updateTitle(long date) {
        // 创建一个标志来格式化标题中的日期和时间
        int flag =
                DateUtils.FORMAT_SHOW_YEAR |
                        DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_TIME;

        // 根据需要应用24小时格式
        flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_12HOUR;

        // 设置格式化后的日期和时间作为对话框标题
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    /**
     * 处理“确认”按钮点击事件。
     * 如果设置了监听器，则使用所选的日期和时间调用它。
     *
     * @param arg0 - 接收点击事件的对话框
     * @param arg1 - 被点击的按钮（确认按钮）
     */
    public void onClick(DialogInterface arg0, int arg1) {
        // 如果已设置监听器，则用所选日期和时间调用它
        if (mOnDateTimeSetListener != null) {
            mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis());
        }
    }

}
