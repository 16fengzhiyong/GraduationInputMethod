package com.nuc.omeletteinputmethod.kernel

import android.inputmethodservice.InputMethodService
import android.os.Environment
import android.util.Log
import android.view.View

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nuc.omeletteinputmethod.DBoperation.DBManage
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB
import com.nuc.omeletteinputmethod.kernel.util.SinogramLibrary

import java.util.ArrayList

class OmeletteIME : InputMethodService() {
    /**
     * 屏幕宽度
     */
    private val mScreenWidth: Int = 0
    /**
     * 屏幕高度
     */
    private val mScreenHeight: Int = 0

    /**
     * 状态栏高度
     */
    private val mStatusBarHeight: Int = 0

    var keyboardSwisher: KeyboardSwisher? = null
        private set
    var dbManage: DBManage? = null
        private set

    override fun onCreate() {
        super.onCreate()
        keyboardSwisher = KeyboardSwisher(this)
        FloatShortInputAdapter.omeletteIME = this
        // /storage/emulated/0
        //File file = new File(basepath+"/zhengti2.txt");
        if (dbManage == null) {
            Log.i("OmeletteIME", "onCreate: new 数据库")
            dbManage = DBManage(this)
        }
    }

    override fun onCreateInputView(): View {
        return keyboardSwisher!!.choseKeyboard()
    }

    override fun onCreateCandidatesView(): View {
        return keyboardSwisher!!.choseCandidatesView()
    }


    fun commitText(data: String) {
        currentInputConnection.commitText(data.replace("'", ""), 0) // 往输入框输出内容

        setCandidatesViewShown(false) // 隐藏 CandidatesView
    }

    fun deleteText() {
        currentInputConnection.deleteSurroundingText(1, 0)
    }

    fun hideInputMethod() {
        hideWindow()
    }

    companion object {
        var canShowWindow = false
    }
}
