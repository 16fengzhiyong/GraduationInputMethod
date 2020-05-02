package com.nuc.omeletteinputmethod.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.R;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * 自定义toolbar
 */
public class TitleBar extends Toolbar {
    //中心标题
    private TextView mCenterTitle;
    //中心icon
    private ImageView mCenterIcon;

    //左侧按钮
    private ImageButton mNavigationIcon;
    //左侧文字
    private TextView mNavigationText;
    //右侧文字
    private TextView mSettingText;
    //右侧按钮
    private ImageButton mSettingIcon;

    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public ImageButton getRightBtn() {
        return mSettingIcon;
    }

    public TextView getRightText() {
        return mSettingText;
    }

    public TextView getCenterTextView() {
        return mCenterTitle;
    }

    /**
     * 左侧文字
     *
     * @param Rid
     */
    public TitleBar setMyNavigationText(@StringRes int Rid) {
        setMyNavigationText(this.getContext().getText(Rid));
        return this;
    }

    /**
     * ToolBar左侧有contentInsetStart 16Dp的空白，若需要可自己定义style修改
     * 详情请见 http://my.oschina.net/yaly/blog/502471
     *
     * @param text
     */
    public TitleBar setMyNavigationText(CharSequence text) {
        Context context = this.getContext();
        if (this.mNavigationText == null) {
            this.mNavigationText = new TextView(context);

            mNavigationText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

            this.mNavigationText.setGravity(Gravity.CENTER_VERTICAL);
            this.mNavigationText.setSingleLine();
            this.mNavigationText.setEllipsize(TextUtils.TruncateAt.END);
//            setMyNavigationTextAppearance(getContext(), R.style.TextAppearance_TitleBar_subTitle);
            //textView in left
            this.addMyView(this.mNavigationText, Gravity.START, DensityUtil.dip2px(15), 0, 0, 0);
        }
        mNavigationText.setText(text);
        return this;
    }

    public TitleBar setMyNavigationText(CharSequence text, int paddingleft) {
        Context context = this.getContext();
        if (this.mNavigationText == null) {
            this.mNavigationText = new TextView(context);

            mNavigationText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

            this.mNavigationText.setGravity(Gravity.CENTER_VERTICAL);
            this.mNavigationText.setSingleLine();
            this.mNavigationText.setEllipsize(TextUtils.TruncateAt.END);
//            setMyNavigationTextAppearance(getContext(), R.style.TextAppearance_TitleBar_subTitle);
            //textView in left
            this.addMyView(this.mNavigationText, Gravity.START, DensityUtil.dip2px(paddingleft), 0, 0, 0);
        }
        mNavigationText.setText(text);
        return this;
    }


    public TitleBar setMyNavigationTextColor(@ColorInt int color) {
        if (this.mNavigationText != null) {
            this.mNavigationText.setTextColor(color);
        }
        return this;
    }

    public TitleBar setNavigationTextOnClickListener(View.OnClickListener listener) {
        if (mNavigationText != null) {
            mNavigationText.setOnClickListener(listener);
        }
        return this;
    }

    public TitleBar setNavigationIconOnClickListener(View.OnClickListener listener) {
        if (mNavigationIcon != null) {
            mNavigationIcon.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * 居中标题
     *
     * @param Rid
     */
    public TitleBar setMyCenterTitle(@StringRes int Rid) {
        setMyCenterTitle(this.getContext().getText(Rid));
        return this;
    }

    public TitleBar setMyCenterTitle(CharSequence text) {
        Context context = this.getContext();
        if (this.mCenterTitle == null) {
            this.mCenterTitle = new TextView(context);
            this.mCenterTitle.setGravity(Gravity.CENTER);
            this.mCenterTitle.setSingleLine();
            this.mCenterTitle.setEllipsize(TextUtils.TruncateAt.END);
            setMyCenterTextAppearance(getContext(), R.style.TextAppearance_TitleBar_Title);
            //textView in center
            this.addMyView(this.mCenterTitle, Gravity.CENTER);
        } else {
            if (this.mCenterTitle.getVisibility() != VISIBLE) {
                mCenterTitle.setVisibility(VISIBLE);
            }
        }
        if (mCenterIcon != null && mCenterIcon.getVisibility() != GONE) {
            mCenterIcon.setVisibility(GONE);
        }
        //隐藏toolbar自带的标题
        setTitle("");
        mCenterTitle.setText(text);
        return this;
    }

    public TitleBar setMyCenterTextAppearance(Context context, @StyleRes int resId) {
        if (this.mCenterTitle != null) {
            this.mCenterTitle.setTextAppearance(context, resId);
        }
        return this;
    }

    public TitleBar setMyCenterTextColor(@ColorInt int color) {
        if (this.mCenterTitle != null) {
            this.mCenterTitle.setTextColor(color);
        }
        return this;
    }

    /**
     * 居中图标
     *
     * @param resId
     */
    public TitleBar setMyCenterIcon(@DrawableRes int resId) {
        setMyCenterIcon(ContextCompat.getDrawable(this.getContext(), resId));
        return this;
    }

    public TitleBar setMyCenterIcon(Drawable drawable) {
        Context context = this.getContext();
        if (this.mCenterIcon == null) {
            this.mCenterIcon = new ImageView(context);
            this.mCenterIcon.setScaleType(ImageView.ScaleType.CENTER);
            //textView in center
            this.addMyView(this.mCenterIcon, Gravity.CENTER);
        } else {
            if (mCenterIcon.getVisibility() != VISIBLE) {
                mCenterIcon.setVisibility(VISIBLE);
            }
        }
        if (mCenterTitle != null && mCenterTitle.getVisibility() != GONE) {
            mCenterTitle.setVisibility(GONE);
        }
        //隐藏toolbar自带的标题
        setTitle("");
        mCenterIcon.setImageDrawable(drawable);
        return this;
    }

    /**
     * 右侧文字
     *
     * @param Rid
     */
    public TitleBar setMySettingText(@StringRes int Rid) {
        setMySettingText(this.getContext().getText(Rid));
        return this;
    }

    public TitleBar setMySettingText(CharSequence text) {
        Context context = this.getContext();
        if (this.mSettingText == null) {
            this.mSettingText = new TextView(context);
            this.mSettingText.setGravity(Gravity.CENTER);
            this.mSettingText.setSingleLine();
            this.mSettingText.setEllipsize(TextUtils.TruncateAt.END);

            //textView in center
//            int padding = 16;
//            this.mSettingText.setPadding(padding, 0, DisplayUtil.px2dip(30), 0);
            setMySettingTextAppearance(getContext(), R.style.TextAppearance_TitleBar_Title);

            this.addMyView(this.mSettingText, Gravity.END, 0, 0, DensityUtil.dip2px(15), 0);
        } else {
            if (mSettingText.getVisibility() != VISIBLE) {
                mSettingText.setVisibility(VISIBLE);
            }
        }
        if (mSettingIcon != null && mSettingIcon.getVisibility() != GONE) {
            mSettingIcon.setVisibility(GONE);
        }

        mSettingText.setText(text);

        return this;
    }

    private TitleBar setMySettingTextAppearance(Context context, @StyleRes int resId) {
        if (this.mSettingText != null) {
            this.mSettingText.setTextAppearance(context, resId);
        }
        return this;
    }




    public TitleBar setMySettingTextColor(@ColorInt int color) {
        if (mSettingText != null) {
            mSettingText.setTextColor(color);
        }
        return this;
    }

    public TitleBar setSettingTextOnClickListener(View.OnClickListener listener) {
        if (mSettingText != null) {
            mSettingText.setOnClickListener(listener);
        }
        return this;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TitleBar setMnavigationIcon(@DrawableRes int resId) {
        setMnavigationIcon(ContextCompat.getDrawable(this.getContext(), resId));
        //获取系统判定的最低华东距离
//        ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
        return this;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TitleBar setMnavigationIcon(Drawable drawable) {
        Context context = this.getContext();
        if (this.mNavigationIcon == null) {
            this.mNavigationIcon = new ImageButton(context);
            this.mNavigationIcon.setBackground(null);
            //保持点击区域
            int padding = 16;
            this.mNavigationIcon.setPadding(padding, 0, padding, 0);

            this.mNavigationIcon.setScaleType(ImageView.ScaleType.CENTER);
            //textView in center
            this.addMyView(this.mNavigationIcon, Gravity.START);
        } else {
            if (mNavigationIcon.getVisibility() != VISIBLE) {
                mNavigationIcon.setVisibility(VISIBLE);
            }
        }
        if (mNavigationText != null && mNavigationText.getVisibility() != GONE) {
            mNavigationText.setVisibility(GONE);
        }
        mNavigationIcon.setImageDrawable(drawable);
        return this;
    }


    /**
     * 右侧图标
     *
     * @param resId
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TitleBar setMySettingIcon(@DrawableRes int resId) {
        setMySettingIcon(ContextCompat.getDrawable(this.getContext(), resId));
        //获取系统判定的最低华东距离
//        ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TitleBar setMySettingIcon(Drawable drawable) {
        Context context = this.getContext();
        if (this.mSettingIcon == null) {
            this.mSettingIcon = new ImageButton(context);
            this.mSettingIcon.setBackground(null);
            //保持点击区域
            int padding = 16;
            this.mSettingIcon.setPadding(padding, 0, padding, 0);

            this.mSettingIcon.setScaleType(ImageView.ScaleType.CENTER);
            //textView in center
            addMyView(this.mSettingIcon, Gravity.END, 0, 0, DensityUtil.dip2px(10), 0);
        } else {
            if (mSettingIcon.getVisibility() != VISIBLE) {
                mSettingIcon.setVisibility(VISIBLE);
            }
        }
        if (mSettingText != null && mSettingText.getVisibility() != GONE) {
            mSettingText.setVisibility(GONE);
        }
        mSettingIcon.setImageDrawable(drawable);
        return this;
    }

    public TitleBar setSettingIconOnClickListener(View.OnClickListener listener) {
        if (mSettingIcon != null) {
            mSettingIcon.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * @param v
     * @param gravity
     */
    private TitleBar addMyView(View v, int gravity) {
        addMyView(v, gravity, 0, 0, 0, 0);
        return this;
    }

    private TitleBar addMyView(View v, int gravity, int left, int top, int right, int bottom) {
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, gravity);
        lp.setMargins(left, top, right, bottom);
        this.addView(v, lp);
        return this;
    }


}