package com.nuc.omeletteinputmethod.DBoperation

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.TextView

import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB
import com.nuc.omeletteinputmethod.kernel.OmeletteIME

import java.util.ArrayList

class DBManage(private val omeletteIME: OmeletteIME) {
    var myDBHelper: SQLiteDatabase? = null
        private set

    init {
        if (myDBHelper == null) {
            //            // 打开/sdcard/dictionary目录中的dictionary.db文件
            //            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
            //                    databaseFilename, null);
            myDBHelper = SQLiteDatabase.openOrCreateDatabase(
                    DATABASE_PATH + mydbName, null)
        }
        //         DBCanWrite=myDBHelper.getWritableDatabase();
        //         DBCanRead=myDBHelper.getReadableDatabase();
    }

    fun getSinogramByPinyin(pinyin: String): ArrayList<SinograFromDB>? {
        val arrayList = ArrayList<SinograFromDB>()
        val sendStr = "$pinyin%"
        var cursor: Cursor? = null
        Log.e("数据库返回信息", "getSinogramByPinyin: 在此")
        Log.e("数据库返回信息", "getSinogramByPinyin: 作用成功")
        try {
            // ORDER BY allcishu DESC
            cursor = myDBHelper!!.query("test_hanzi", arrayOf("wenzi1,pinyin,jisheng,id,allcishu,usercishu"), "pinyin like ? ", arrayOf(sendStr), null, null, "COALESCE(allcishu,usercishu)  DESC")
            Log.e("数据库返回信息", "getSinogramByPinyin: 获取返回成功")
        } catch (e: Exception) {
            Log.e("数据库返回信息", "getSinogramByPinyin: ", e)
            return null
        }

        //   Cursor cursor = DBCanRead.query("hanzi", new String[]{"wenzi1,wenzi2,jisheng,id"}, "pinyin like ?", new String[]{pinyin}, null, null, null);
        Log.i("数据库返回信息", "getSinogramByPinyin: 成功返回")
        if (cursor!!.count == 0) {
            Log.i("数据库返回信息", "getSinogramByPinyin: 单字拼音为空 flag为1")
            return getSomeSinogramByPinyin(pinyin, 1)
        }
        try {
            cursor.moveToFirst()

            while (!cursor.isLast) {
                arrayList.add(
                        SinograFromDB(cursor.getString(cursor.getColumnIndex("wenzi1")), cursor.getString(cursor.getColumnIndex("pinyin")), cursor.getInt(cursor.getColumnIndex("allcishu")), cursor.getInt(cursor.getColumnIndex("id")), cursor.getInt(cursor.getColumnIndex("usercishu"))
                        ))
                cursor.moveToNext()
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("数据库返回信息错误", "getSinogramByPinyin: ", e)
        }

        return arrayList
    }

    //SELECT * FROM duozi WHERE pinyin LIKE "cha%'ji%"
    //SELECT * FROM duozi WHERE pinyin LIKE "s%'j%'s%" ORDER BY pinlv DESC
    fun savePinlvOfOneSinogra(data: CandidatesEntity) {
        //        myDBHelper.update()
        val values = ContentValues()
        values.put("allcishu", data.allcishu + 1)
        values.put("usercishu", data.allcishu + 1)

        val shumu = myDBHelper!!.update("test_hanzi", values, "id=?", arrayOf(data.id.toString()))
        Log.i("数据库返回信息", "更新的数目 $shumu")

    }

    fun savePinlvOfMoreSinogra(data: CandidatesEntity) {
        val values = ContentValues()
        values.put("allcishu", data.allcishu + 1)
        values.put("usercishu", data.allcishu + 1)
        val shumu = myDBHelper!!.update("duozi", values, "id=?", arrayOf(data.id.toString()))
        Log.i("数据库返回信息", "更新的数目 $shumu")
    }

    fun getSomeSinogramByPinyin(oldpinyin: String, flag: Int): ArrayList<SinograFromDB>? {
        val arrayList = ArrayList<SinograFromDB>()
        //此时为单字符传递过来的
        var newpinyin = ""
        var nowpinyin = ""
        var cursor: Cursor? = null
        //flag = 1为单字搜索后 执行的操作
        //flag = 0此时为带有 ' 的拼音
        if (flag == 1 && oldpinyin.length >= 2) {
            if (oldpinyin.substring(oldpinyin.length - 2, oldpinyin.length - 1) != "'") {
                nowpinyin = (oldpinyin.substring(0, oldpinyin.length - 1)
                        + "'" + oldpinyin.substring(oldpinyin.length - 1))
            } else {
                nowpinyin = oldpinyin
            }
            val linshi = nowpinyin.replace("'", "%'")
            newpinyin = linshi + "%"
        } else if (flag == 0 || flag == 2) {
            nowpinyin = oldpinyin
            val linshi = nowpinyin.replace("'", "%'")
            newpinyin = linshi + "%"
        }

        try {
            Log.e("数据库返回信息", "getSinogramByPinyin: nowpinyin = $nowpinyin flag = $flag")
            Log.e("数据库返回信息", "getSinogramByPinyin: newpinyin = $newpinyin flag = $flag")
            // ORDER BY allcishu DESC
            cursor = myDBHelper!!.query("duozi", arrayOf("wenzi,pinyin,allcishu,id,usercishu"), "pinyin like ? ", arrayOf(newpinyin), null, null, "COALESCE(allcishu,usercishu)  DESC")
            Log.e("数据库返回信息", "getSinogramByPinyin: 获取返回成功")
        } catch (e: Exception) {
            Log.e("数据库返回信息", "getSinogramByPinyin: ", e)
            return null
        }

        if (cursor!!.count != 0) {
            try {
                cursor.moveToFirst()

                while (!cursor.isLast) {
                    arrayList.add(
                            SinograFromDB(cursor.getString(cursor.getColumnIndex("wenzi")), cursor.getString(cursor.getColumnIndex("pinyin")), cursor.getInt(cursor.getColumnIndex("allcishu")), cursor.getInt(cursor.getColumnIndex("id")), cursor.getInt(cursor.getColumnIndex("usercishu"))
                            ))
                    cursor.moveToNext()
                }

                cursor.close()
            } catch (e: Exception) {
                Log.e("数据库返回信息错误", "getSomeSinogramByPinyin: ", e)
            }

            omeletteIME.keyboardSwisher!!.myKeyboardView.nowPinYin = nowpinyin
            return arrayList
        } else {

            if (flag == 2) {
                omeletteIME.keyboardSwisher!!.myKeyboardView.nowPinYin = nowpinyin
                return arrayList
            }
            nowpinyin = (oldpinyin.substring(0, oldpinyin.length - 1)
                    + "'" + oldpinyin.substring(oldpinyin.length - 1))
            return getSomeSinogramByPinyin(nowpinyin, 2)
        }
    }

    companion object {
        var mydbName = "myandroid.db"//数据库的名字
        private val DATABASE_PATH = "/data/data/com.nuc.omeletteinputmethod/databases/"//数据库在手机里的路径
    }

    //    public ArrayList<SinograFromDB> getSomeSinogramByPinyin1(String oldpinyin,int flag){
    //        ArrayList<SinograFromDB> arrayList = new ArrayList<>();
    //        //此时为单字符传递过来的
    //        String newpinyin = "";
    //        String nowpinyin = "";
    //        Cursor cursor=null;
    //        boolean isdelete = false;
    //        if (!oldpinyin.substring(oldpinyin.length()-2,oldpinyin.length()-1).equals("'")){
    //            nowpinyin = oldpinyin.substring(0,oldpinyin.length()-1)
    //                    +"'"+oldpinyin.substring(oldpinyin.length()-1);
    //        }else {
    //            isdelete = true;
    //            nowpinyin = oldpinyin;
    //        }
    //
    //        if (flag == 0){
    //            oldpinyin = oldpinyin.replace("'","%'");
    //            newpinyin = oldpinyin.substring(0,oldpinyin.length()-1)
    //                    +"%'"+oldpinyin.substring(oldpinyin.length()-1)+"%";
    //        }else if (flag == 1){
    //            Log.i("数据库返回信息", "getSomeSinogramByPinyin: " +nowpinyin.substring(nowpinyin.length()-2,nowpinyin.length()-1));
    //            if (nowpinyin.substring(nowpinyin.length()-2,nowpinyin.length()-1).equals("'")&&!isdelete){
    //                omeletteIME.getKeyboardSwisher().getMyKeyboardView().setNowPinYin(nowpinyin);
    //                return arrayList;
    //            }
    //            oldpinyin.replace("'","'%");
    //            newpinyin = oldpinyin + "%";
    //        }else return null;
    //
    //        try {
    //            Log.e("数据库返回信息", "getSinogramByPinyin: newpinyin = "+newpinyin +"flag = "+flag);
    //            // ORDER BY allcishu DESC
    //            cursor = myDBHelper.query("duozi", new String[]{"wenzi,pinyin,pinlv,id"}, "pinyin like ? ", new String[]{newpinyin}, null, null, "pinlv DESC");
    //            Log.e("数据库返回信息", "getSinogramByPinyin: 获取返回成功");
    //        }catch (Exception e){
    //            Log.e("数据库返回信息", "getSinogramByPinyin: ",e );
    //            return null;
    //        }
    //
    //        if (cursor.getCount() == 0){
    //            if (nowpinyin.substring(nowpinyin.length()-2,nowpinyin.length()-1).equals("'")){
    //                omeletteIME.getKeyboardSwisher().getMyKeyboardView().setNowPinYin(nowpinyin);
    //                return arrayList;
    //            }
    //            Log.i("数据库准备", "getSomeSinogramByPinyin: "+nowpinyin);
    //            return getSomeSinogramByPinyin(nowpinyin,1);
    //        }
    //        try {
    //            cursor.moveToFirst();
    //
    //            while (!cursor.isLast()){
    //                arrayList.add(
    //                        new SinograFromDB(cursor.getString(cursor.getColumnIndex("wenzi"))
    //                                ,cursor.getString(cursor.getColumnIndex("wenzi"))
    //                                ,cursor.getInt(cursor.getColumnIndex("id"))
    //                                ,cursor.getInt(cursor.getColumnIndex("id"))
    //                        ));
    //                cursor.moveToNext();
    //            }
    //
    //            cursor.close();
    //        }catch (Exception e){
    //            Log.e("数据库返回信息错误", "getSomeSinogramByPinyin: ", e );
    //        }
    //        omeletteIME.getKeyboardSwisher().getMyKeyboardView().setNowPinYin(nowpinyin);
    //        return arrayList;
    //    }
}
