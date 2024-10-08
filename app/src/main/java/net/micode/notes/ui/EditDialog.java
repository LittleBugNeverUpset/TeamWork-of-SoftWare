package net.micode.notes.ui;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import net.micode.notes.R;
/**
 * Created by Administrator on 2018/1/31.
 */
public class EditDialog extends Dialog {
    private Button yes;//确定按钮
    private Button no;
    private TextView titleTv;//消息标题文本
    private EditText etphone;//输入电话
    private String titleStr;//从外界设置的title文本

    //确定文本和取消文本的显示内容
    private String yesStr;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private OnYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    /**
     * 设置取消按钮的显示内容和监听
     */
    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (nostr != null) {
            no.setText(noStr);
        }
        this.noOnclickListener = onNoOnclickListener;
    }
    /**
     * 设置确定按钮的显示内容和监听
     */
    public void setYesOnclickListener(String str, OnYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = OnYesOnclickListener;
    }
    public EditDialog(Context context) {
        super(context, R.style.Dialog_Msg);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }
    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick(etphone.getText().toString());
                }
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick();
                }
            }
        });
    }
    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTv.setText(titleStr);
        }

        //如果设置按钮的文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
    }
    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        titleTv = (TextView) findViewById(R.id.title);
        etphone = (EditText) findViewById(R.id.etphone);
    }
    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }
    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public void setMessage(String message) {
        messageStr = message;
    }
    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface OnYesOnclickListener {
        public void onYesClick(String phone);
    }
    public interface OnNoOnclickListener {
        public void onNoClick();
    }
    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height= ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }
}