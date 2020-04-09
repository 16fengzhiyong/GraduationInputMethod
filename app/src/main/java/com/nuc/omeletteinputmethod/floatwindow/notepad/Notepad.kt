package com.nuc.omeletteinputmethod.floatwindow.notepad

import android.content.ClipboardManager
import android.content.Context
import android.util.Log

/**
 * 此文件中需要实现 文本复制之后
 * 将其转化为记事本 放在 数据库 中
 * 如果没有复制信息 则显示 记事本
 */
class Notepad {
    internal var mClipboardManager: ClipboardManager? = null
    internal var mOnPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    internal var mContext: Context? = null
    /**
     * 注册剪切板复制、剪切事件监听
     */
    private fun registerClipEvents() {
        mClipboardManager = mContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        mOnPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            if (mClipboardManager!!.hasPrimaryClip() && mClipboardManager!!.primaryClip!!.itemCount > 0) {
                // 获取复制、剪切的文本内容
                val content = mClipboardManager!!.primaryClip!!.getItemAt(0).text
                Log.d("TAG", "复制、剪切的内容为：$content")
            }
        }
        mClipboardManager!!.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener)
    }

    /**
     * 注销监听，避免内存泄漏。
     */
    protected fun onDestroy() {
        if (mClipboardManager != null && mOnPrimaryClipChangedListener != null) {
            mClipboardManager!!.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener)
        }
    }
}
