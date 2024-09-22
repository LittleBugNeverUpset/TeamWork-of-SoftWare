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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import net.micode.notes.R;

/**
 * DropdownMenu类用于创建一个下拉菜单。
 * 该菜单由一个按钮触发，点击按钮时显示菜单项。
 */
public class DropdownMenu {
    // 触发下拉菜单的按钮
    private Button mButton;

    // 弹出菜单对象
    private PopupMenu mPopupMenu;

    // 菜单对象
    private Menu mMenu;

    /**
     * 构造函数，初始化下拉菜单。
     *
     * @param context - 上下文，用于创建PopupMenu
     * @param button - 触发菜单的按钮
     * @param menuId - 菜单资源ID，指定菜单的内容
     */
    public DropdownMenu(Context context, Button button, int menuId) {
        mButton = button;
        // 设置按钮背景为下拉图标
        mButton.setBackgroundResource(R.drawable.dropdown_icon);

        // 初始化弹出菜单
        mPopupMenu = new PopupMenu(context, mButton);

        // 获取菜单对象并加载指定的菜单资源
        mMenu = mPopupMenu.getMenu();
        mPopupMenu.getMenuInflater().inflate(menuId, mMenu);

        // 设置按钮点击事件，点击时显示下拉菜单
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
    }

    /**
     * 设置下拉菜单项点击监听器。
     *
     * @param listener - 菜单项点击事件的监听器
     */
    public void setOnDropdownMenuItemClickListener(OnMenuItemClickListener listener) {
        if (mPopupMenu != null) {
            mPopupMenu.setOnMenuItemClickListener(listener);
        }
    }

    /**
     * 根据菜单项ID查找菜单项。
     *
     * @param id - 菜单项的ID
     * @return 找到的菜单项
     */
    public MenuItem findItem(int id) {
        return mMenu.findItem(id);
    }

    /**
     * 设置按钮的标题文本。
     *
     * @param title - 要设置的标题文本
     */
    public void setTitle(CharSequence title) {
        mButton.setText(title);
    }
}
