package com.nuc.omeletteinputmethod.myframent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.io.Serializable
import androidx.fragment.app.Fragment


/**
 * Created by zlk on 2017/7/24.
 */

abstract class BaseFrament : Fragment() {
    protected var mContext: Context? = null
    protected var rootView: View? = null

    protected abstract val layoutId: Int

    //============================== 以下为广播===============================
    private var internalReceiver: InternalReceiver? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()

        if (internalReceiver != null) {
            mContext!!.unregisterReceiver(internalReceiver)
        }
        mContext = null

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (rootView == null) {
            rootView = inflater.inflate(layoutId, container, false)
            initView(savedInstanceState)
            initWidgetActions()
        } else {
            // 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，
            // 要不然会发生这个rootview已经有parent的错误。
            val parent = rootView!!.parent as ViewGroup
            parent?.removeView(rootView)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("aa", javaClass.getSimpleName())
    }

    protected abstract fun initView(savedInstanceState: Bundle?)

    protected abstract fun initWidgetActions()

    /**
     * 注册广播
     * @param actionArray
     */
    protected fun registerReceiver(actionArray: Array<String>?) {
        if (actionArray == null) {
            return
        }
        val intentfilter = IntentFilter()
        for (action in actionArray) {
            intentfilter.addAction(action)
        }
        if (internalReceiver == null) {
            internalReceiver = InternalReceiver()
        }
        activity!!.registerReceiver(internalReceiver, intentfilter)
    }


    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
    }

    /**
     * 自定义应用全局广播处理器，方便全局拦截广播并进行分发
     * @author 容联•云通讯
     * @date 2014-12-4
     * @version 4.0
     */
    private inner class InternalReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            handleReceiver(context, intent)
        }
    }

    /**
     * 如果子界面需要拦截处理注册的广播
     * 需要实现该方法
     * @param context
     * @param intent
     */
    protected fun handleReceiver(context: Context, intent: Intent) {}

    @JvmOverloads
    fun startActivity(cls: Class<*>, bundle: Bundle? = null) {
        val intent = Intent()
        intent.setClass(mContext!!, cls)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    fun startNextActivity(cls: Class<*>, key: String, serializable: Serializable) {
        val intent = Intent(mContext, cls)
        intent.putExtra(key, serializable)
        startActivity(intent)
    }
}//======================= 以下封装activity跳转=======================================
