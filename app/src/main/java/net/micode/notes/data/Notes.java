/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.data;

import android.net.Uri;

public class Notes {
    // ContentProvider的授权字符串，定义了访问该应用数据的唯一标识符
    public static final String AUTHORITY = "micode_notes";

    // 日志标签，便于日志输出时识别
    public static final String TAG = "Notes";

    // 表示数据类型：笔记、文件夹和系统类型
    public static final int TYPE_NOTE = 0;    // 普通笔记
    public static final int TYPE_FOLDER = 1;  // 文件夹
    public static final int TYPE_SYSTEM = 2;  // 系统项

    /**
     * 以下常量是系统文件夹的标识符
     * {@link Notes#ID_ROOT_FOLDER} 是默认文件夹
     * {@link Notes#ID_TEMPARAY_FOLDER} 用于存放不属于任何文件夹的笔记
     * {@link Notes#ID_CALL_RECORD_FOLDER} 用于存储通话记录
     */
    public static final int ID_ROOT_FOLDER = 0;           // 根文件夹ID
    public static final int ID_TEMPARAY_FOLDER = -1;      // 临时文件夹ID
    public static final int ID_CALL_RECORD_FOLDER = -2;   // 通话记录文件夹ID
    public static final int ID_TRASH_FOLER = -3;          // 回收站文件夹ID

    // 各种意图的附加信息键，用于在不同组件之间传递数据
    public static final String INTENT_EXTRA_ALERT_DATE = "net.micode.notes.alert_date";
    public static final String INTENT_EXTRA_BACKGROUND_ID = "net.micode.notes.background_color_id";
    public static final String INTENT_EXTRA_WIDGET_ID = "net.micode.notes.widget_id";
    public static final String INTENT_EXTRA_WIDGET_TYPE = "net.micode.notes.widget_type";
    public static final String INTENT_EXTRA_FOLDER_ID = "net.micode.notes.folder_id";
    public static final String INTENT_EXTRA_CALL_DATE = "net.micode.notes.call_date";

    // 小部件的类型定义
    public static final int TYPE_WIDGET_INVALIDE = -1;  // 无效小部件类型
    public static final int TYPE_WIDGET_2X = 0;         // 2x小部件类型
    public static final int TYPE_WIDGET_4X = 1;         // 4x小部件类型

    //定义常量替换 "content://"
    private  static final String uriHead = "content://";

    // 内部类，用于定义笔记数据类型常量
    public static class DataConstants {
        private DataConstants(){}
        public static final String NOTE = TextNote.CONTENT_ITEM_TYPE;     // 文本笔记内容类型
        public static final String CALL_NOTE = CallNote.CONTENT_ITEM_TYPE; // 通话笔记内容类型
    }

    /**
     * 查询所有笔记和文件夹的URI
     */
    public static final Uri CONTENT_NOTE_URI = Uri.parse(uriHead  + AUTHORITY + "/note");

    /**
     * 查询数据的URI
     */
    public static final Uri CONTENT_DATA_URI = Uri.parse(uriHead  + AUTHORITY + "/data");

    // NoteColumns接口定义了与笔记相关的数据库表字段
    public interface NoteColumns {


        public static final String ID = "_id";

        public static final String PARENT_ID = "parent_id";

        public static final String CREATED_DATE = "created_date";

        public static final String MODIFIED_DATE = "modified_date";

        public static final String ALERTED_DATE = "alert_date";

        public static final String SNIPPET = "snippet";

        public static final String WIDGET_ID = "widget_id";

        public static final String WIDGET_TYPE = "widget_type";

        public static final String BG_COLOR_ID = "bg_color_id";

        public static final String HAS_ATTACHMENT = "has_attachment";

        public static final String NOTES_COUNT = "notes_count";

        public static final String TYPE = "type";

        public static final String SYNC_ID = "sync_id";

        public static final String LOCAL_MODIFIED = "local_modified";

        public static final String ORIGIN_PARENT_ID = "origin_parent_id";

        public static final String GTASK_ID = "gtask_id";

        public static final String VERSION = "version";
    }

    // DataColumns接口定义了与数据相关的数据库表字段
    public interface DataColumns {
        /**
         * 行的唯一ID
         * <P> 类型: INTEGER (long) </P>
         */
        public static final String ID = "_id";

        /**
         * 行所代表项目的MIME类型
         * <P> 类型: TEXT </P>
         */
        public static final String MIME_TYPE = "mime_type";

        /**
         * 该数据所属的笔记的引用ID
         * <P> 类型: INTEGER (long) </P>
         */
        public static final String NOTE_ID = "note_id";

        /**
         * 数据创建日期
         * <P> 类型: INTEGER (long) </P>
         */
        public static final String CREATED_DATE = "created_date";

        /**
         * 最近修改日期
         * <P> 类型: INTEGER (long) </P>
         */
        public static final String MODIFIED_DATE = "modified_date";

        /**
         * 数据内容
         * <P> 类型: TEXT </P>
         */
        public static final String CONTENT = "content";

        /**
         * 泛型数据列，MIME类型为整数数据
         * <P> 类型: INTEGER </P>
         */
        public static final String DATA1 = "data1";

        /**
         * 泛型数据列，MIME类型为整数数据
         * <P> 类型: INTEGER </P>
         */
        public static final String DATA2 = "data2";

        /**
         * 泛型数据列，MIME类型为文本数据
         * <P> 类型: TEXT </P>
         */
        public static final String DATA3 = "data3";

        /**
         * 泛型数据列，MIME类型为文本数据
         * <P> 类型: TEXT </P>
         */
        public static final String DATA4 = "data4";

        /**
         * 泛型数据列，MIME类型为文本数据
         * <P> 类型: TEXT </P>
         */
        public static final String DATA5 = "data5";
    }

    // TextNote类实现了DataColumns接口，表示文本笔记的数据结构
    public static final class TextNote implements DataColumns {
        private TextNote(){}
        /**
         * 模式标识，标记文本笔记是否为检查列表模式
         * <P> 类型: Integer 1:检查列表模式 0:普通模式 </P>
         */
        public static final String MODE = DATA1;

        // 检查列表模式的常量值
        public static final int MODE_CHECK_LIST = 1;

        // 文本笔记的内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/text_note";

        // 单个文本笔记的内容类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/text_note";

        // 文本笔记的查询URI
        public static final Uri CONTENT_URI = Uri.parse(uriHead  + AUTHORITY + "/text_note");
    }

    // CallNote类实现了DataColumns接口，表示通话笔记的数据结构
    public static final class CallNote implements DataColumns {
        private CallNote(){}
        /**
         * 通话记录的日期
         * <P> 类型: INTEGER (long) </P>
         */
        public static final String CALL_DATE = DATA1;

        /**
         * 通话记录的电话号码
         * <P> 类型: TEXT </P>
         */
        public static final String PHONE_NUMBER = DATA3;

        // 通话笔记的内容类型
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/call_note";

        // 单个通话笔记的内容类型
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/call_note";

        // 通话笔记的查询URI
        public static final Uri CONTENT_URI = Uri.parse(uriHead  + AUTHORITY + "/call_note");
    }
}
