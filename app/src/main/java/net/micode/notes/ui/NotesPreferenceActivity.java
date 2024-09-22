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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.gtask.remote.GTaskSyncService;

/**
 * 笔记偏好设置活动
 */
public class NotesPreferenceActivity extends PreferenceActivity {
    public static final String PREFERENCE_NAME = "notes_preferences"; // 偏好设置名称

    public static final String PREFERENCE_SYNC_ACCOUNT_NAME = "pref_key_account_name"; // 同步账户名称

    public static final String PREFERENCE_LAST_SYNC_TIME = "pref_last_sync_time"; // 上次同步时间

    public static final String PREFERENCE_SET_BG_COLOR_KEY = "pref_key_bg_random_appear"; // 背景色设置

    private static final String PREFERENCE_SYNC_ACCOUNT_KEY = "pref_sync_account_key"; // 同步账户键

    private static final String AUTHORITIES_FILTER_KEY = "authorities"; // 权限过滤键

    private PreferenceCategory mAccountCategory; // 账户类别

    private GTaskReceiver mReceiver; // 接收器

    private Account[] mOriAccounts; // 原始账户列表

    private boolean mHasAddedAccount; // 是否已添加账户标志

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        /* 使用应用图标进行导航 */
        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.preferences); // 从资源中添加偏好设置
        mAccountCategory = (PreferenceCategory) findPreference(PREFERENCE_SYNC_ACCOUNT_KEY); // 获取账户类别
        mReceiver = new GTaskReceiver(); // 初始化接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(GTaskSyncService.GTASK_SERVICE_BROADCAST_NAME); // 注册广播接收器
        registerReceiver(mReceiver, filter);

        mOriAccounts = null; // 初始化原始账户
        View header = LayoutInflater.from(this).inflate(R.layout.settings_header, null); // 加载头部视图
        getListView().addHeaderView(header, null, true); // 添加头部视图
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 如果用户添加了新账户，则需要自动设置同步账户
        if (mHasAddedAccount) {
            Account[] accounts = getGoogleAccounts(); // 获取Google账户
            if (mOriAccounts != null && accounts.length > mOriAccounts.length) {
                for (Account accountNew : accounts) {
                    boolean found = false;
                    for (Account accountOld : mOriAccounts) {
                        if (TextUtils.equals(accountOld.name, accountNew.name)) {
                            found = true; // 找到匹配的账户
                            break;
                        }
                    }
                    if (!found) {
                        setSyncAccount(accountNew.name); // 设置新账户为同步账户
                        break;
                    }
                }
            }
        }

        refreshUI(); // 刷新UI
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver); // 注销广播接收器
        }
        super.onDestroy();
    }

    // 加载账户偏好设置
    private void loadAccountPreference() {
        mAccountCategory.removeAll(); // 清空账户类别

        Preference accountPref = new Preference(this); // 创建账户偏好设置项
        final String defaultAccount = getSyncAccountName(this); // 获取当前同步账户名称
        accountPref.setTitle(getString(R.string.preferences_account_title)); // 设置标题
        accountPref.setSummary(getString(R.string.preferences_account_summary)); // 设置摘要
        accountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!GTaskSyncService.isSyncing()) { // 如果当前没有正在同步
                    if (TextUtils.isEmpty(defaultAccount)) {
                        // 第一次设置账户
                        showSelectAccountAlertDialog(); // 显示选择账户对话框
                    } else {
                        // 如果账户已设置，提示用户风险
                        showChangeAccountConfirmAlertDialog(); // 显示更改账户确认对话框
                    }
                } else {
                    Toast.makeText(NotesPreferenceActivity.this,
                                    R.string.preferences_toast_cannot_change_account, Toast.LENGTH_SHORT)
                            .show(); // 提示正在同步，无法更改账户
                }
                return true;
            }
        });

        mAccountCategory.addPreference(accountPref); // 添加账户偏好设置项
    }

    // 加载同步按钮
    private void loadSyncButton() {
        Button syncButton = (Button) findViewById(R.id.preference_sync_button); // 获取同步按钮
        TextView lastSyncTimeView = (TextView) findViewById(R.id.prefenerece_sync_status_textview); // 获取上次同步时间视图

        // 设置按钮状态
        if (GTaskSyncService.isSyncing()) {
            syncButton.setText(getString(R.string.preferences_button_sync_cancel)); // 如果正在同步，设置为取消同步
            syncButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    GTaskSyncService.cancelSync(NotesPreferenceActivity.this); // 取消同步
                }
            });
        } else {
            syncButton.setText(getString(R.string.preferences_button_sync_immediately)); // 设置为立即同步
            syncButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    GTaskSyncService.startSync(NotesPreferenceActivity.this); // 开始同步
                }
            });
        }
        syncButton.setEnabled(!TextUtils.isEmpty(getSyncAccountName(this))); // 设置按钮是否可用

        // 设置上次同步时间
        if (GTaskSyncService.isSyncing()) {
            lastSyncTimeView.setText(GTaskSyncService.getProgressString()); // 更新同步状态文本
            lastSyncTimeView.setVisibility(View.VISIBLE);
        } else {
            long lastSyncTime = getLastSyncTime(this); // 获取上次同步时间
            if (lastSyncTime != 0) {
                lastSyncTimeView.setText(getString(R.string.preferences_last_sync_time,
                        DateFormat.format(getString(R.string.preferences_last_sync_time_format),
                                lastSyncTime))); // 显示上次同步时间
                lastSyncTimeView.setVisibility(View.VISIBLE);
            } else {
                lastSyncTimeView.setVisibility(View.GONE); // 如果没有同步时间，隐藏视图
            }
        }
    }

    // 刷新UI
    private void refreshUI() {
        loadAccountPreference(); // 加载账户偏好设置
        loadSyncButton(); // 加载同步按钮
    }

    // 显示选择账户对话框
    private void showSelectAccountAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View titleView = LayoutInflater.from(this).inflate(R.layout.account_dialog_title, null); // 加载对话框标题视图
        TextView titleTextView = (TextView) titleView.findViewById(R.id.account_dialog_title);
        titleTextView.setText(getString(R.string.preferences_dialog_select_account_title)); // 设置标题文本
        TextView subtitleTextView = (TextView) titleView.findViewById(R.id.account_dialog_subtitle);
        subtitleTextView.setText(getString(R.string.preferences_dialog_select_account_tips)); // 设置副标题文本

        dialogBuilder.setCustomTitle(titleView); // 设置自定义标题
        dialogBuilder.setPositiveButton(null, null); // 设置对话框按钮

        Account[] accounts = getGoogleAccounts(); // 获取Google账户
        String defAccount = getSyncAccountName(this); // 获取当前同步账户

        mOriAccounts = accounts; // 保存原始账户
        mHasAddedAccount = false; // 标记尚未添加账户

        if (accounts.length > 0) {
            CharSequence[] items = new CharSequence[accounts.length]; // 创建账户项数组
            final CharSequence[] itemMapping = items;
            int checkedItem = -1; // 记录选中项索引
            int index = 0;
            for (Account account : accounts) {
                if (TextUtils.equals(account.name, defAccount)) {
                    checkedItem = index; // 记录当前同步账户的索引
                }
                items[index++] = account.name; // 添加账户名到数组
            }
            dialogBuilder.setSingleChoiceItems(items, checkedItem,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setSyncAccount(itemMapping[which].toString()); // 设置选中的账户为同步账户
                            dialog.dismiss(); // 关闭对话框
                            refreshUI(); // 刷新UI
                        }
                    });
        }

        View addAccountView = LayoutInflater.from(this).inflate(R.layout.add_account_text, null); // 加载添加账户视图
        dialogBuilder.setView(addAccountView); // 设置对话框视图

        final AlertDialog dialog = dialogBuilder.show(); // 显示对话框
        addAccountView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mHasAddedAccount = true; // 标记已添加账户
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS"); // 创建添加账户设置的Intent
                intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                        "gmail-ls" // 过滤权限
                });
                startActivityForResult(intent, -1); // 启动添加账户设置
                dialog.dismiss(); // 关闭对话框
            }
        });
    }

    // 显示更改账户确认对话框
    private void showChangeAccountConfirmAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View titleView = LayoutInflater.from(this).inflate(R.layout.account_dialog_title, null); // 加载对话框标题视图
        TextView titleTextView = (TextView) titleView.findViewById(R.id.account_dialog_title);
        titleTextView.setText(getString(R.string.preferences_dialog_change_account_title,
                getSyncAccountName(this))); // 设置标题文本
        TextView subtitleTextView = (TextView) titleView.findViewById(R.id.account_dialog_subtitle);
        subtitleTextView.setText(getString(R.string.preferences_dialog_change_account_warn_msg)); // 设置警告消息
        dialogBuilder.setCustomTitle(titleView); // 设置自定义标题

        CharSequence[] menuItemArray = new CharSequence[] {
                getString(R.string.preferences_menu_change_account), // 更改账户选项
                getString(R.string.preferences_menu_remove_account), // 移除账户选项
                getString(R.string.preferences_menu_cancel) // 取消选项
        };
        dialogBuilder.setItems(menuItemArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showSelectAccountAlertDialog(); // 显示选择账户对话框
                } else if (which == 1) {
                    removeSyncAccount(); // 移除同步账户
                    refreshUI(); // 刷新UI
                }
            }
        });
        dialogBuilder.show(); // 显示对话框
    }

    // 获取Google账户
    private Account[] getGoogleAccounts() {
        AccountManager accountManager = AccountManager.get(this);
        return accountManager.getAccountsByType("com.google"); // 返回Google账户
    }

    // 设置同步账户
    private void setSyncAccount(String account) {
        if (!getSyncAccountName(this).equals(account)) { // 如果当前账户不等于新账户
            SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            if (account != null) {
                editor.putString(PREFERENCE_SYNC_ACCOUNT_NAME, account); // 保存同步账户
            } else {
                editor.putString(PREFERENCE_SYNC_ACCOUNT_NAME, "");
            }
            editor.commit(); // 提交更改

            // 清除上次同步时间
            setLastSyncTime(this, 0);

            // 清除本地GTask相关信息
            new Thread(new Runnable() {
                public void run() {
                    ContentValues values = new ContentValues();
                    values.put(NoteColumns.GTASK_ID, ""); // 清除GTASK_ID
                    values.put(NoteColumns.SYNC_ID, 0); // 清除SYNC_ID
                    getContentResolver().update(Notes.CONTENT_NOTE_URI, values, null, null); // 更新内容提供者
                }
            }).start();

            Toast.makeText(NotesPreferenceActivity.this,
                    getString(R.string.preferences_toast_success_set_accout, account), // 显示设置成功消息
                    Toast.LENGTH_SHORT).show();
        }
    }

    // 移除同步账户
    private void removeSyncAccount() {
        SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (settings.contains(PREFERENCE_SYNC_ACCOUNT_NAME)) {
            editor.remove(PREFERENCE_SYNC_ACCOUNT_NAME); // 移除同步账户
        }
        if (settings.contains(PREFERENCE_LAST_SYNC_TIME)) {
            editor.remove(PREFERENCE_LAST_SYNC_TIME); // 移除上次同步时间
        }
        editor.commit(); // 提交更改

        // 清除本地GTask相关信息
        new Thread(new Runnable() {
            public void run() {
                ContentValues values = new ContentValues();
                values.put(NoteColumns.GTASK_ID, ""); // 清除GTASK_ID
                values.put(NoteColumns.SYNC_ID, 0); // 清除SYNC_ID
                getContentResolver().update(Notes.CONTENT_NOTE_URI, values, null, null); // 更新内容提供者
            }
        }).start();
    }

    // 获取同步账户名称
    public static String getSyncAccountName(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return settings.getString(PREFERENCE_SYNC_ACCOUNT_NAME, ""); // 返回同步账户名称
    }

    // 设置上次同步时间
    public static void setLastSyncTime(Context context, long time) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(PREFERENCE_LAST_SYNC_TIME, time); // 保存上次同步时间
        editor.commit(); // 提交更改
    }

    // 获取上次同步时间
    public static long getLastSyncTime(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        return settings.getLong(PREFERENCE_LAST_SYNC_TIME, 0); // 返回上次同步时间
    }

    // 广播接收器
    private class GTaskReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUI(); // 刷新UI
            if (intent.getBooleanExtra(GTaskSyncService.GTASK_SERVICE_BROADCAST_IS_SYNCING, false)) {
                TextView syncStatus = (TextView) findViewById(R.id.prefenerece_sync_status_textview);
                syncStatus.setText(intent
                        .getStringExtra(GTaskSyncService.GTASK_SERVICE_BROADCAST_PROGRESS_MSG)); // 更新同步状态信息
            }
        }
    }

    // 处理选项菜单点击事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, NotesListActivity.class); // 返回笔记列表活动
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
