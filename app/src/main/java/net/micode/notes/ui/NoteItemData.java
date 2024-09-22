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
import android.database.Cursor;
import android.text.TextUtils;

import net.micode.notes.data.Contact;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.DataUtils;

/**
 * NoteItemData类，用于表示笔记项的数据模型。
 */
public class NoteItemData {
    // 定义要查询的字段
    static final String [] PROJECTION = new String [] {
            NoteColumns.ID,              // 笔记ID
            NoteColumns.ALERTED_DATE,    // 提醒日期
            NoteColumns.BG_COLOR_ID,     // 背景颜色ID
            NoteColumns.CREATED_DATE,     // 创建日期
            NoteColumns.HAS_ATTACHMENT,   // 是否有附件
            NoteColumns.MODIFIED_DATE,    // 修改日期
            NoteColumns.NOTES_COUNT,      // 笔记数量
            NoteColumns.PARENT_ID,        // 父文件夹ID
            NoteColumns.SNIPPET,         // 笔记片段
            NoteColumns.TYPE,             // 笔记类型
            NoteColumns.WIDGET_ID,        // 小部件ID
            NoteColumns.WIDGET_TYPE,      // 小部件类型
    };

    // 各列索引常量
    private static final int ID_COLUMN                    = 0;
    private static final int ALERTED_DATE_COLUMN          = 1;
    private static final int BG_COLOR_ID_COLUMN           = 2;
    private static final int CREATED_DATE_COLUMN          = 3;
    private static final int HAS_ATTACHMENT_COLUMN        = 4;
    private static final int MODIFIED_DATE_COLUMN         = 5;
    private static final int NOTES_COUNT_COLUMN           = 6;
    private static final int PARENT_ID_COLUMN             = 7;
    private static final int SNIPPET_COLUMN               = 8;
    private static final int TYPE_COLUMN                  = 9;
    private static final int WIDGET_ID_COLUMN             = 10;
    private static final int WIDGET_TYPE_COLUMN           = 11;

    // 笔记项的各属性
    private long mId;                     // 笔记ID
    private long mAlertDate;              // 提醒日期
    private int mBgColorId;               // 背景颜色ID
    private long mCreatedDate;            // 创建日期
    private boolean mHasAttachment;        // 是否有附件
    private long mModifiedDate;           // 修改日期
    private int mNotesCount;              // 笔记数量
    private long mParentId;               // 父文件夹ID
    private String mSnippet;               // 笔记片段
    private int mType;                    // 笔记类型
    private int mWidgetId;                // 小部件ID
    private int mWidgetType;              // 小部件类型
    private String mName;                 // 联系人名称
    private String mPhoneNumber;          // 联系电话

    // 状态标识
    private boolean mIsLastItem;          // 是否为最后一项
    private boolean mIsFirstItem;         // 是否为第一项
    private boolean mIsOnlyOneItem;      // 是否为唯一一项
    private boolean mIsOneNoteFollowingFolder; // 是否有一个笔记跟随文件夹
    private boolean mIsMultiNotesFollowingFolder; // 是否有多个笔记跟随文件夹

    /**
     * 构造函数，根据Cursor初始化NoteItemData实例
     */
    public NoteItemData(Context context, Cursor cursor) {
        mId = cursor.getLong(ID_COLUMN); // 获取ID
        mAlertDate = cursor.getLong(ALERTED_DATE_COLUMN); // 获取提醒日期
        mBgColorId = cursor.getInt(BG_COLOR_ID_COLUMN); // 获取背景颜色ID
        mCreatedDate = cursor.getLong(CREATED_DATE_COLUMN); // 获取创建日期
        mHasAttachment = (cursor.getInt(HAS_ATTACHMENT_COLUMN) > 0); // 是否有附件
        mModifiedDate = cursor.getLong(MODIFIED_DATE_COLUMN); // 获取修改日期
        mNotesCount = cursor.getInt(NOTES_COUNT_COLUMN); // 获取笔记数量
        mParentId = cursor.getLong(PARENT_ID_COLUMN); // 获取父文件夹ID
        mSnippet = cursor.getString(SNIPPET_COLUMN); // 获取笔记片段
        mSnippet = mSnippet.replace(NoteEditActivity.TAG_CHECKED, "").replace(
                NoteEditActivity.TAG_UNCHECKED, ""); // 移除标记

        mType = cursor.getInt(TYPE_COLUMN); // 获取笔记类型
        mWidgetId = cursor.getInt(WIDGET_ID_COLUMN); // 获取小部件ID
        mWidgetType = cursor.getInt(WIDGET_TYPE_COLUMN); // 获取小部件类型

        mPhoneNumber = ""; // 初始化电话号
        if (mParentId == Notes.ID_CALL_RECORD_FOLDER) { // 如果属于通话记录文件夹
            mPhoneNumber = DataUtils.getCallNumberByNoteId(context.getContentResolver(), mId); // 获取电话号
            if (!TextUtils.isEmpty(mPhoneNumber)) {
                mName = Contact.getContact(context, mPhoneNumber); // 获取联系人名称
                if (mName == null) {
                    mName = mPhoneNumber; // 如果联系人为空，使用电话号
                }
            }
        }

        if (mName == null) {
            mName = ""; // 确保名称不为空
        }
        checkPostion(cursor); // 检查位置状态
    }

    /**
     * 检查当前位置的状态
     */
    private void checkPostion(Cursor cursor) {
        mIsLastItem = cursor.isLast(); // 是否为最后一项
        mIsFirstItem = cursor.isFirst(); // 是否为第一项
        mIsOnlyOneItem = (cursor.getCount() == 1); // 是否为唯一一项
        mIsMultiNotesFollowingFolder = false; // 初始化
        mIsOneNoteFollowingFolder = false; // 初始化

        // 如果是笔记类型且不是第一项
        if (mType == Notes.TYPE_NOTE && !mIsFirstItem) {
            int position = cursor.getPosition(); // 获取当前光标位置
            if (cursor.moveToPrevious()) { // 移动到前一项
                // 检查前一项的类型
                if (cursor.getInt(TYPE_COLUMN) == Notes.TYPE_FOLDER
                        || cursor.getInt(TYPE_COLUMN) == Notes.TYPE_SYSTEM) {
                    if (cursor.getCount() > (position + 1)) {
                        mIsMultiNotesFollowingFolder = true; // 多个笔记跟随文件夹
                    } else {
                        mIsOneNoteFollowingFolder = true; // 仅有一个笔记跟随文件夹
                    }
                }
                if (!cursor.moveToNext()) {
                    throw new IllegalStateException("cursor move to previous but can't move back");
                }
            }
        }
    }

    // 各种状态查询方法
    public boolean isOneFollowingFolder() {
        return mIsOneNoteFollowingFolder;
    }

    public boolean isMultiFollowingFolder() {
        return mIsMultiNotesFollowingFolder;
    }

    public boolean isLast() {
        return mIsLastItem;
    }

    public String getCallName() {
        return mName;
    }

    public boolean isFirst() {
        return mIsFirstItem;
    }

    public boolean isSingle() {
        return mIsOnlyOneItem;
    }

    // 各种数据获取方法
    public long getId() {
        return mId;
    }

    public long getAlertDate() {
        return mAlertDate;
    }

    public long getCreatedDate() {
        return mCreatedDate;
    }

    public boolean hasAttachment() {
        return mHasAttachment;
    }

    public long getModifiedDate() {
        return mModifiedDate;
    }

    public int getBgColorId() {
        return mBgColorId;
    }

    public long getParentId() {
        return mParentId;
    }

    public int getNotesCount() {
        return mNotesCount;
    }

    public long getFolderId() {
        return mParentId;
    }

    public int getType() {
        return mType;
    }

    public int getWidgetType() {
        return mWidgetType;
    }

    public int getWidgetId() {
        return mWidgetId;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public boolean hasAlert() {
        return (mAlertDate > 0); // 是否有提醒
    }

    public boolean isCallRecord() {
        return (mParentId == Notes.ID_CALL_RECORD_FOLDER && !TextUtils.isEmpty(mPhoneNumber)); // 是否为通话记录
    }

    public static int getNoteType(Cursor cursor) {
        return cursor.getInt(TYPE_COLUMN); // 获取笔记类型
    }
}
