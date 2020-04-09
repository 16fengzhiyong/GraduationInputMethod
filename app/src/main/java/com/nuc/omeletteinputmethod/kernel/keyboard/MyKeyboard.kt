package com.nuc.omeletteinputmethod.kernel.keyboard


import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.inputmethodservice.Keyboard
import android.util.DisplayMetrics
import android.util.Log
import android.util.Xml

import androidx.annotation.XmlRes

import com.nuc.omeletteinputmethod.kernel.OmeletteIME
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil

import java.util.ArrayList

class MyKeyboard {
    var keyboardWidth: Int = 0
        private set
    var keyboardHeight: Int = 0
        private set
    var rows = ArrayList<KeyboardRow>()
    private val mKeys = ArrayList<Key>()

    constructor() {

    }

    @JvmOverloads
    constructor(context: Context, @XmlRes xmlLayoutResId: Int, modeId: Int = 0) {
        //DisplayMetrics dm = context.getResources().getDisplayMetrics();
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    private fun loadKeyboard(context: Context, parser: XmlResourceParser) {
        var inKey = false
        var inRow = false
        val leftMostKey = false
        var row = 0
        var x = 0
        var y = 0
        var Bfx = 0f
        var key: Key? = null
        var currentRow: KeyboardRow?
        val res = context.resources
        val skipRow = false

        try {
            var event: Int = parser.next()
            while ( event != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    val tag = parser.name
                    currentRow = createRowFromXml(res, parser)
                    if (TAG_ROW == tag) {
                        inRow = true
                        x = 0

                        rows.add(currentRow)
                        y = currentRow.rowHeight + y
                        Log.i(TAG, "loadKeyboard: TAG_ROW y :$y")
                    } else if (TAG_KEY == tag) {
                        inKey = true
                        key = createKeyFromXml(res, currentRow, row, parser)
                        mKeys.add(key)
                        Bfx = key.length + Bfx
                        Log.i(TAG, "loadKeyboard: TAG_KEY Bfx :$Bfx")
                        if (Bfx < 100) {
                            keyboardWidth = (Bfx * KeyboardUtil.getViewWidth(context) / 100).toInt()
                        } else
                            keyboardWidth = KeyboardUtil.getViewWidth(context)
                        Log.i(TAG, "loadKeyboard: TAG_KEY keyboardWidth :$keyboardWidth")
                        Log.i(TAG, "loadKeyboard: TAG_KEY keyboardHeight :" + KeyboardUtil.getViewHeight(context))
                    } else if (TAG_KEYBOARD == tag) {
                        parseKeyboardAttributes(res, parser)
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        Log.i("loadKeyboard", "loadKeyboard: inKey")
                        inKey = false
                    } else if (inRow) {
                        inRow = false
                        row++
                        Log.i("loadKeyboard", "loadKeyboard: inRow$row")
                    } else {
                        // TODO: error or extend?
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error:$e")
            e.printStackTrace()
        }

        keyboardHeight = y + 50
    }

    /**
     * 解析到<Row>时传递到KeyboardRow
     * [KeyboardRow]
    </Row> */
    protected fun createRowFromXml(res: Resources, parser: XmlResourceParser): KeyboardRow {
        return KeyboardRow(res, this, parser)
    }

    /**
     * 解析到<Key>时传递到 Key
     * [Key]
    </Key> */
    protected fun createKeyFromXml(res: Resources, parent: KeyboardRow, x: Int, y: Int, parser: XmlResourceParser): Key {
        return Key(res, parent, x, y, parser)
    }

    /**
     * 解析到<Key>时传递到 Key
     * [Key]
     * @param row 行数
    </Key> */
    protected fun createKeyFromXml(res: Resources, parent: KeyboardRow, row: Int, parser: XmlResourceParser): Key {
        return Key(res, parent, row, parser)
    }

    /**
     * 解析<Keyboard></Keyboard>属性
     */
    private fun parseKeyboardAttributes(res: Resources, parser: XmlResourceParser) {}

    fun getmKeys(): ArrayList<Key> {
        return mKeys
    }

    companion object {
        // Keyboard XML Tags
        private val TAG_KEYBOARD = "MyKeyboard"
        private val TAG_ROW = "Row"
        private val TAG_KEY = "Key"
        private val TAG = "MyKeyboard"
    }
}

