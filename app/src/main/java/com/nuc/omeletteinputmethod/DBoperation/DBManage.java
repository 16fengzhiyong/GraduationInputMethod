package com.nuc.omeletteinputmethod.DBoperation;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.entityclass.SinograFromDB;

import java.util.ArrayList;

public class DBManage {
    public static String mydbName="myandroid.db";//数据库的名字
    private static String DATABASE_PATH="/data/data/com.nuc.omeletteinputmethod/databases/";//数据库在手机里的路径

    public DBManage(Context context){
        if(myDBHelper == null){
//            // 打开/sdcard/dictionary目录中的dictionary.db文件
//            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
//                    databaseFilename, null);
            myDBHelper = SQLiteDatabase.openOrCreateDatabase(
                    DATABASE_PATH+mydbName, null);
        }
//         DBCanWrite=myDBHelper.getWritableDatabase();
//         DBCanRead=myDBHelper.getReadableDatabase();
    }

    public SQLiteDatabase getMyDBHelper(){
        return myDBHelper;
    }
    private SQLiteDatabase myDBHelper = null;
//    //可写数据库
//    SQLiteDatabase DBCanWrite;
//    //可读数据库
//    SQLiteDatabase DBCanRead;
    public ArrayList<SinograFromDB> getSinogramByPinyin(String pinyin){
        ArrayList<SinograFromDB> arrayList = new ArrayList<>();
        pinyin = pinyin +"%";
        Cursor cursor=null;
        Log.e("数据库返回信息", "getSinogramByPinyin: 在此");
        //myDBHelper.execSQL("SELECT\t* FROM duozi WHERE pinyin LIKE \"ai'hao\"");
        Log.e("数据库返回信息", "getSinogramByPinyin: 作用成功");
        try {
            cursor = myDBHelper.query("hanzi", new String[]{"wenzi1,wenzi2,jisheng,id"}, "pinyin like ?", new String[]{pinyin}, null, null, null);
            Log.e("数据库返回信息", "getSinogramByPinyin: 获取返回成功");
        }catch (Exception e){
            Log.e("数据库返回信息", "getSinogramByPinyin: ",e );
            return null;
        }
     //   Cursor cursor = DBCanRead.query("hanzi", new String[]{"wenzi1,wenzi2,jisheng,id"}, "pinyin like ?", new String[]{pinyin}, null, null, null);
        Log.i("数据库返回信息", "getSinogramByPinyin: 成功返回");
        cursor.moveToFirst();

        while (!cursor.isLast()){
            arrayList.add(
                    new SinograFromDB(cursor.getString(cursor.getColumnIndex("wenzi1"))
                            ,cursor.getString(cursor.getColumnIndex("wenzi2"))
                            ,cursor.getInt(cursor.getColumnIndex("jisheng"))
                            ,cursor.getInt(cursor.getColumnIndex("id"))
                    ));
            cursor.moveToNext();
        }


        cursor.close();
        return arrayList;
    }


}
