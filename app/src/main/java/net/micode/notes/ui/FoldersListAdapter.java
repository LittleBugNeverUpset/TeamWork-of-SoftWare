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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * FoldersListAdapter类用于适配文件夹列表数据。
 * 继承自CursorAdapter，以便于处理数据库游标数据。
 */
public class FoldersListAdapter extends CursorAdapter {
    // 查询的字段数组
    private static final String[] PROJECTION = {
            NoteColumns.ID,
            NoteColumns.SNIPPET
    };

    // 列索引
    public static final int ID_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static String[] getProjection() {
        return PROJECTION.clone(); // 返回副本以保护数组不被外部修改
    }
    /**
     * 构造函数，初始化适配器。
     *
     * @param context - 上下文
     * @param c - 数据库游标
     */
    public FoldersListAdapter(Context context, Cursor c) {
        super(context, c);

    }

    /**
     * 创建新的视图，用于展示每个文件夹项。
     *
     * @param context - 上下文
     * @param cursor - 数据游标
     * @param parent - 父视图组
     * @return 新创建的视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new FolderListItem(context);
    }

    /**
     * 绑定数据到视图。
     *
     * @param view - 要绑定的视图
     * @param context - 上下文
     * @param cursor - 数据游标
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof FolderListItem item) { // 使用模式匹配
            String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER)
                    ? context.getString(R.string.menu_move_parent_folder)
                    : cursor.getString(NAME_COLUMN);
            item.bind(folderName); // 绑定数据
        }
    }

    /**
     * 根据位置获取文件夹名称。
     *
     * @param context - 上下文
     * @param position - 位置
     * @return 文件夹名称
     */
    public String getFolderName(Context context, int position) {
        Cursor cursor = (Cursor) getItem(position);
        return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
    }

    /**
     * 内部类，用于表示每个文件夹的列表项视图。
     */
    private class FolderListItem extends LinearLayout {
        private TextView mName; // 文件夹名称文本视图

        /**
         * 构造函数，初始化文件夹列表项视图。
         *
         * @param context - 上下文
         */
        public FolderListItem(Context context) {
            super(context);
            // 加载布局文件
            inflate(context, R.layout.folder_list_item, this);
            // 获取文件夹名称文本视图
            mName = (TextView) findViewById(R.id.tv_folder_name);
        }

        /**
         * 绑定文件夹名称到视图。
         *
         * @param name - 文件夹名称
         */
        public void bind(String name) {
            mName.setText(name);
        }
    }
}
