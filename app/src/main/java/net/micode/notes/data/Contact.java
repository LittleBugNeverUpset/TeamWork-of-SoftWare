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

package net.micode.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.HashMap;

public class Contact {
    // 静态缓存，用于存储电话号码和联系人名称的映射
    private static HashMap<String, String> sContactCache;
    private static final String TAG = "Contact";

    // 用于从数据库中查询电话号码的 SQL 查询语句
    // PHONE_NUMBERS_EQUAL 是用于比较电话号码的函数
    // 过滤条件基于电话号码的匹配和 MIME 类型为 Phone.CONTENT_ITEM_TYPE
    // 此查询还会从 "phone_lookup" 表中获取最小匹配的原始联系人 ID
    private static final String CALLER_ID_SELECTION = "PHONE_NUMBERS_EQUAL(" + Phone.NUMBER
            + ",?) AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
            + " AND " + Data.RAW_CONTACT_ID + " IN "
            + "(SELECT raw_contact_id "
            + " FROM phone_lookup"
            + " WHERE min_match = '+')";

    /**
     * 获取与电话号码对应的联系人姓名
     *
     * @param context 当前应用上下文，用于访问内容提供者
     * @param phoneNumber 查询的电话号码
     * @return 联系人的姓名，如果未找到则返回 null
     */
    public static String getContact(Context context, String phoneNumber) {
        // 初始化联系人缓存
        if(sContactCache == null) {
            sContactCache = new HashMap<String, String>();
        }

        // 检查缓存中是否已有该电话号码的联系人信息
        if(sContactCache.containsKey(phoneNumber)) {
            // 如果有缓存，直接返回缓存中的联系人名称
            return sContactCache.get(phoneNumber);
        }

        // 替换查询语句中的 "+" 以便进行电话号码匹配
        String selection = CALLER_ID_SELECTION.replace("+",
                PhoneNumberUtils.toCallerIDMinMatch(phoneNumber));

        // 执行数据库查询，查询 Data 表中符合条件的联系人数据
        Cursor cursor = context.getContentResolver().query(
                Data.CONTENT_URI,
                new String [] { Phone.DISPLAY_NAME },  // 需要查询的字段，联系人显示名称
                selection,  // 查询条件
                new String[] { phoneNumber },  // 查询条件中的参数，传入电话号码
                null);

        // 如果查询结果不为空且有数据
        if (cursor != null && cursor.moveToFirst()) {
            try {
                // 获取查询到的联系人名称
                String name = cursor.getString(0);
                // 将联系人名称和电话号码存入缓存
                sContactCache.put(phoneNumber, name);
                return name;
            } catch (IndexOutOfBoundsException e) {
                // 捕获查询出错的异常并记录日志
                Log.e(TAG, " Cursor get string error " + e.toString());
                return null;
            } finally {
                // 关闭游标，释放资源
                cursor.close();
            }
        } else {
            // 如果没有查询到结果，记录日志并返回 null
            Log.d(TAG, "No contact matched with number:" + phoneNumber);
            return null;
        }
    }
}
