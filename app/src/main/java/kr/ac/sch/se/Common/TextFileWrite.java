package kr.ac.sch.se.Common;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextFileWrite<T> implements Serializable{
	private final String TAG = "TEXT_FILE_WRITE";
	private String path, final_path, date, time;
	private String[] dt;
	private File file;

	public TextFileWrite(){}

	// folder name ,
	public TextFileWrite(String folderName) {
		String path = Environment.getExternalStorageDirectory().toString() + "/" + folderName;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.KOREA);
		Date currentTime = new Date();
		String str = formatter.format(currentTime);
		dt = str.split("-");

		date = time = "";

		date = dt[0] + dt[1] + dt[2];
		time = dt[3] + dt[4];

		file = new File(path);
		if (!file.exists()) {
			file.mkdir();
			Log.e(TAG, folderName + "��������");
		} else {
			Log.e(TAG, folderName + "��������");
		}

		file = new File(path +"/"+ str);

		if (!file.exists()) {
			file.mkdir();
			Log.e(TAG, str + "��������");
		} else {
			Log.e(TAG, str + "��������");
		}

		this.path = path +"/"+ str;
	}

	public void TextFileInit(String path, String fileName) {
		file = new File(path, fileName + ".txt");
		final_path = path + "/" + fileName;
	}

	public void add(long time, T data) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.append(String.valueOf(time)+","+String.valueOf(data)+"\r\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(T data) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.append(String.valueOf(data)+"\r\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(byte[] str){
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(str);

//			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
//			for(int n = 1; n < str.length; n++){
//				out.append(str[n]+"");
//				if(n % 3 == 0)
//					out.append(str[n]+" ");
//			}
//			out.append(str[str.length+1]+"\r\n");

//			out.append(String.valueOf(data)+"\r\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(String str){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.append(str+"\r\n");
//			out.append(String.valueOf(data)+"\r\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void add(T data, double time) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.append(time + ", "+String.valueOf(data)+"\r\n");
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Double[] wrapperArray(double[] array){
		Double[] result = new Double[array.length];

		for(int n =0; n < array.length; n++){
			result[n] = new Double(array[n]);
		}
		return result;
	}

	public void add(T[] data) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			for (int n = 0; n < data.length; n++) {
				out.append(String.valueOf(data[n]) + " ");
				out.flush();
			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPath() {
		return path;
	}

	public String finalPath() { return (final_path + ".txt"); }

	public String returnDate() { return date; }

	public String returnTime() { return time; }
}