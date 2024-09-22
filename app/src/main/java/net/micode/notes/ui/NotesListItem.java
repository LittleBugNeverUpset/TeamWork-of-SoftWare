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

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser.NoteItemBgResources;

/**
 * NotesListItem 类是一个自定义的 LinearLayout，表示单个笔记条目。
 * 它包含了笔记的标题、时间、提醒图标、以及在通话记录中的姓名等信息，并根据不同的条件来显示不同的视图样式。
 */
public class NotesListItem extends LinearLayout {
    // 定义布局中的视图
    private ImageView mAlert; // 提醒图标（比如闹钟图标）
    private TextView mTitle;  // 笔记标题或内容的简要概述
    private TextView mTime;   // 笔记最后修改的时间
    private TextView mCallName; // 如果是通话记录，显示通话联系人姓名
    private NoteItemData mItemData; // 存储与该视图绑定的笔记条目的数据
    private CheckBox mCheckBox; // 用于多选模式下显示的复选框

    // 构造方法：从上下文 context 中获取布局并初始化各个视图
    public NotesListItem(Context context) {
        super(context);
        inflate(context, R.layout.note_item, this); // 将 note_item.xml 中的布局文件进行加载
        // 通过 ID 查找各个视图
        mAlert = (ImageView) findViewById(R.id.iv_alert_icon);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTime = (TextView) findViewById(R.id.tv_time);
        mCallName = (TextView) findViewById(R.id.tv_name);
        mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
    }

    /**
     * 绑定数据到视图中，并根据笔记类型和其他属性来更新视图的显示内容。
     * @param context 上下文对象
     * @param data 包含笔记内容的 NoteItemData 对象
     * @param choiceMode 是否处于多选模式
     * @param checked 当前条目是否被选中
     */
    public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
        // 如果处于多选模式并且当前条目是笔记类型，显示复选框
        if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(checked); // 设置复选框是否选中
        } else {
            mCheckBox.setVisibility(View.GONE); // 否则隐藏复选框
        }

        // 将传入的笔记数据绑定到当前视图
        mItemData = data;

        // 如果当前条目是通话记录文件夹
        if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
            mCallName.setVisibility(View.GONE); // 隐藏联系人姓名
            mAlert.setVisibility(View.VISIBLE); // 显示提醒图标
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem); // 设置标题样式
            // 设置标题为 "通话记录" 以及文件数量
            mTitle.setText(context.getString(R.string.call_record_folder_name)
                    + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
            // 设置图标为通话记录图标
            mAlert.setImageResource(R.drawable.call_record);
        }
        // 如果是通话记录的子条目
        else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) {
            mCallName.setVisibility(View.VISIBLE); // 显示联系人姓名
            mCallName.setText(data.getCallName()); // 设置联系人姓名
            mTitle.setTextAppearance(context,R.style.TextAppearanceSecondaryItem); // 设置次要条目的样式
            mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 设置简要内容
            // 根据是否有提醒，决定是否显示提醒图标
            if (data.hasAlert()) {
                mAlert.setImageResource(R.drawable.clock); // 设置提醒为闹钟图标
                mAlert.setVisibility(View.VISIBLE);
            } else {
                mAlert.setVisibility(View.GONE);
            }
        }
        // 处理普通的笔记条目
        else {
            mCallName.setVisibility(View.GONE); // 隐藏通话联系人姓名
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem); // 设置标题样式
            // 如果当前条目是文件夹，显示文件夹名称以及包含的笔记数量
            if (data.getType() == Notes.TYPE_FOLDER) {
                mTitle.setText(data.getSnippet()
                        + context.getString(R.string.format_folder_files_count,
                        data.getNotesCount()));
                mAlert.setVisibility(View.GONE); // 隐藏提醒图标
            }
            // 普通笔记条目，显示简要内容
            else {
                mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
                // 根据是否有提醒，决定是否显示提醒图标
                if (data.hasAlert()) {
                    mAlert.setImageResource(R.drawable.clock); // 设置提醒图标
                    mAlert.setVisibility(View.VISIBLE);
                } else {
                    mAlert.setVisibility(View.GONE);
                }
            }
        }
        // 设置显示笔记修改时间（相对时间格式）
        mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate()));

        // 设置条目背景，根据不同的笔记类型和状态选择不同的背景资源
        setBackground(data);
    }

    /**
     * 根据笔记的类型和状态设置条目的背景。
     * @param data 包含笔记信息的 NoteItemData 对象
     */
    private void setBackground(NoteItemData data) {
        int id = data.getBgColorId(); // 获取背景色 ID
        // 如果是普通笔记，根据其在列表中的位置设置不同的背景资源
        if (data.getType() == Notes.TYPE_NOTE) {
            if (data.isSingle() || data.isOneFollowingFolder()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id)); // 单个笔记或后跟文件夹
            } else if (data.isLast()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id)); // 列表中的最后一个笔记
            } else if (data.isFirst() || data.isMultiFollowingFolder()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id)); // 列表中的第一个笔记或后跟多个文件夹
            } else {
                setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id)); // 普通的笔记
            }
        } else {
            // 如果是文件夹，设置文件夹的背景资源
            setBackgroundResource(NoteItemBgResources.getFolderBgRes());
        }
    }

    /**
     * 获取当前视图绑定的笔记数据对象。
     * @return 当前绑定的 NoteItemData 对象
     */
    public NoteItemData getItemData() {
        return mItemData;
    }
}
