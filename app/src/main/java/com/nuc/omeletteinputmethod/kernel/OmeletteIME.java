package com.nuc.omeletteinputmethod.kernel;

import android.content.Intent;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nuc.omeletteinputmethod.DBoperation.DBManage;
import com.nuc.omeletteinputmethod.R;
import com.nuc.omeletteinputmethod.SettingsActivity;
import com.nuc.omeletteinputmethod.entityclass.OneSinograEntity;
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB;
import com.nuc.omeletteinputmethod.kernel.util.KeyboardUtil;
import com.nuc.omeletteinputmethod.kernel.util.SinogramLibrary;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class OmeletteIME extends InputMethodService {
	public static boolean canShowWindow = false;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 屏幕高度
     */
    private int mScreenHeight;

    /**
     * 状态栏高度
     */
    private int mStatusBarHeight;

    private KeyboardSwisher keyboardSwisher;
    private static DBManage dbManage = null;
    @Override
    public void onCreate() {
        super.onCreate();
        keyboardSwisher = new KeyboardSwisher(this);
        if (oneSinograEntityArrayList == null){
			setOneSinograEntityArrayList(SinogramLibrary.oneSinogramString);
		}
		// /storage/emulated/0
		//File file = new File(basepath+"/zhengti2.txt");
		if (dbManage == null){
			Log.i("OmeletteIME", "onCreate: new 数据库");
			dbManage = new DBManage(this);
		}
    }

//	public static String ReadTxtFile(String strFilePath)
//	{
//		String path = strFilePath;
//		String content = ""; //文件内容字符串
//		//打开文件
//		File file = new File(path);
//		//如果path是传递过来的参数，可以做一个非目录的判断
//		Log.d("TestFile", "path : "+path);
//		if (file.isDirectory())
//		{
//			Log.d("TestFile", "The File doesn't not exist.");
//		}
//		else
//		{
//			try {
//				InputStream instream = new FileInputStream(file);
//				if (instream != null)
//				{
//					InputStreamReader inputreader = new InputStreamReader(instream);
//					BufferedReader buffreader = new BufferedReader(inputreader);
//					String line;
//					//分行读取
//					while (( line = buffreader.readLine()) != null) {
//						content += line + "\n";
//						Log.i("这一行内容是", "readFileOnLine: "+line);
//					}
//
//					instream.close();
//				}
//			}
//			catch (java.io.FileNotFoundException e)
//			{
//				Log.d("TestFile", "The File doesn't not exist.");
//			}
//			catch (IOException e)
//			{
//				Log.d("TestFile", e.getMessage());
//			}
//		}
//		return content;
//	}
//	public void readFileOnLine(String filePath){//输入文件路径
//		File file = new File(filePath);
//
//		if (file.isDirectory())
//		{
//			Log.d("TestFile", "The File doesn't not exist.");
//		}
//		FileInputStream fis = null;//打开文件输入流
//		try {
//			fis = openFileInput(filePath);
//			StringBuffer sBuffer = new StringBuffer();
//			DataInputStream dataIO = new DataInputStream(fis);//读取文件数据流
//			String strLine = null;
//			while((strLine =  dataIO.readLine()) != null) {//通过readline按行读取
//				sBuffer.append(strLine + "\n");//strLine就是一行的内容
//			}
//			Log.i("这一行内容是", "readFileOnLine: "+strLine);
//			dataIO.close();
//			fis.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
    @Override
	public View onCreateInputView() {
		return keyboardSwisher.choseKeyboard();
	}
	
	@Override
	public View onCreateCandidatesView() {
		return keyboardSwisher.choseCandidatesView();
	}
	
	public void commitText(String data) {
		String basepath = Environment.getExternalStorageDirectory().toString();
//		ReadTxtFile(basepath+"/zhengti2.txt");
		if (dbManage == null) Log.i("数据库是否为空", "dbManage == null");
		Log.i("数据库返回信息", "commitText: 开始");
		ArrayList<SinograFromDB> sinograFromDBS = new ArrayList<>();
		sinograFromDBS = dbManage.getSinogramByPinyin(data);
		for (SinograFromDB sinograFromDB :sinograFromDBS){
			Log.i("数据库返回信息", "commitText: "+sinograFromDB.getWenzi1());
		}
		getCurrentInputConnection().commitText(data, 0); // 往输入框输出内容
		setCandidatesViewShown(false); // 隐藏 CandidatesView
	}
	
	public void deleteText(){
		getCurrentInputConnection().deleteSurroundingText(1, 0);
	}
	
	public void hideInputMethod() {
		hideWindow();
	}

	public KeyboardSwisher getKeyboardSwisher() {
		return keyboardSwisher;
	}
	public ArrayList<OneSinograEntity> getOneSinograEntityArrayList() {
		return oneSinograEntityArrayList;
	}
	private static ArrayList<OneSinograEntity> oneSinograEntityArrayList = null;
	private void setOneSinograEntityArrayList(String result){
		oneSinograEntityArrayList = new ArrayList<>();
		JsonParser parser = new JsonParser();  //创建JSON解析器
		JsonArray array = (JsonArray)parser.parse(result) ;
		for (int i = 0; i < array.size(); i++) {
			JsonArray array2 = array.get(i).getAsJsonArray();
			Log.i("OmeletteIME", "setOneSinograEntityArrayList: array.size() "+array.size() );

			for (int j = 0; j < array2.size(); j++) {
				Log.i("OmeletteIME", "setOneSinograEntityArrayList: array2.size() "+array2.size() );

				JsonObject subObject = array2.get(j).getAsJsonObject();
//                Log.i("SettingsActivity", "setOneSinograEntityArrayList: " + subObject.getAsString());
				Log.i("SettingsActivity", "setOneSinograEntityArrayList: " + subObject.entrySet());
				for (Map.Entry entry : subObject.entrySet()) {
					Log.i("SettingsActivity", "entry.getKey():" + entry.getKey());
					Log.i("SettingsActivity", "entry.getKey():" + entry.getValue().toString());
//                    entry.getKey();
//                    entry.getValue().toString();
					oneSinograEntityArrayList.add(new OneSinograEntity(entry.getKey().toString(), entry.getValue().toString()));
				}

			}
		}
	}
}
