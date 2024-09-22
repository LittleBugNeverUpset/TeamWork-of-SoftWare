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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import net.micode.notes.data.Notes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * NotesListAdapter类，用于管理笔记列表的适配器。
 */
public class NotesListAdapter extends CursorAdapter {
    private static final String TAG = "NotesListAdapter"; // 日志标记
    private Context mContext; // 上下文
    private HashMap<Integer, Boolean> mSelectedIndex; // 记录选中项的索引
    private int mNotesCount; // 笔记数量
    private boolean mChoiceMode; // 是否选择模式

    // 小部件属性类
    public static class AppWidgetAttribute {
        public int widgetId; // 小部件ID
        public int widgetType; // 小部件类型
    }

    // 构造函数
    public NotesListAdapter(Context context) {
        super(context, null);
        mSelectedIndex = new HashMap<Integer, Boolean>(); // 初始化选中项索引
        mContext = context;
        mNotesCount = 0; // 初始化笔记数量
    }

    // 创建新视图
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new NotesListItem(context); // 返回新的笔记项视图
    }

    // 绑定视图
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof NotesListItem) {
            NoteItemData itemData = new NoteItemData(context, cursor); // 创建笔记项数据
            ((NotesListItem) view).bind(context, itemData, mChoiceMode, // 绑定数据到视图
                    isSelectedItem(cursor.getPosition())); // 传递选中状态
        }
    }

    // 设置选中项
    public void setCheckedItem(final int position, final boolean checked) {
        mSelectedIndex.put(position, checked); // 更新选中状态
        notifyDataSetChanged(); // 通知数据集变化
    }

    // 检查是否处于选择模式
    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    // 设置选择模式
    public void setChoiceMode(boolean mode) {
        mSelectedIndex.clear(); // 清空选中项
        mChoiceMode = mode; // 设置选择模式
    }

    // 全选或取消全选
    public void selectAll(boolean checked) {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) { // 如果是笔记类型
                    setCheckedItem(i, checked); // 设置选中状态
                }
            }
        }
    }

    // 获取选中项的ID集合
    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<Long>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) { // 如果该项被选中
                Long id = getItemId(position); // 获取项ID
                if (id == Notes.ID_ROOT_FOLDER) {
                    Log.d(TAG, "Wrong item id, should not happen"); // 日志记录错误ID
                } else {
                    itemSet.add(id); // 添加到集合中
                }
            }
        }
        return itemSet; // 返回选中项ID集合
    }

    // 获取选中小部件属性集合
    public HashSet<AppWidgetAttribute> getSelectedWidget() {
        HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) { // 如果该项被选中
                Cursor c = (Cursor) getItem(position); // 获取光标
                if (c != null) {
                    AppWidgetAttribute widget = new AppWidgetAttribute(); // 创建小部件属性
                    NoteItemData item = new NoteItemData(mContext, c); // 获取笔记项数据
                    widget.widgetId = item.getWidgetId(); // 设置小部件ID
                    widget.widgetType = item.getWidgetType(); // 设置小部件类型
                    itemSet.add(widget); // 添加到集合中
                    /**
                     * 不在这里关闭光标，只有适配器可以关闭它
                     */
                } else {
                    Log.e(TAG, "Invalid cursor"); // 日志记录无效光标
                    return null; // 返回null
                }
            }
        }
        return itemSet; // 返回选中小部件属性集合
    }

    // 获取选中项数量
    public int getSelectedCount() {
        Collection<Boolean> values = mSelectedIndex.values();
        if (null == values) {
            return 0; // 如果没有选中项，返回0
        }
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (true == iter.next()) {
                count++; // 统计选中项数量
            }
        }
        return count; // 返回选中项数量
    }

    // 检查是否所有项都已选中
    public boolean isAllSelected() {
        int checkedCount = getSelectedCount(); // 获取选中项数量
        return (checkedCount != 0 && checkedCount == mNotesCount); // 返回是否所有项均被选中
    }

    // 检查指定位置的项是否被选中
    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false; // 如果该项不存在，返回false
        }
        return mSelectedIndex.get(position); // 返回选中状态
    }

    // 内容变化时的回调
    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        calcNotesCount(); // 重新计算笔记数量
    }

    // 改变光标时的回调
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calcNotesCount(); // 重新计算笔记数量
    }

    // 计算笔记数量
    private void calcNotesCount() {
        mNotesCount = 0; // 初始化笔记数量
        for (int i = 0; i < getCount(); i++) {
            Cursor c = (Cursor) getItem(i); // 获取光标
            if (c != null) {
                if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) {
                    mNotesCount++; // 统计笔记数量
                }
            } else {
                Log.e(TAG, "Invalid cursor"); // 日志记录无效光标
                return; // 返回
            }
        }
    }
}
