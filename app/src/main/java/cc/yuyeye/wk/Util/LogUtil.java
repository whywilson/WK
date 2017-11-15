package cc.yuyeye.wk.Util;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cc.yuyeye.wk.Common;

public class LogUtil
{  
	public static SharedPreferences logSharePre = PreferenceManager.getDefaultSharedPreferences(Common.getContext());
    public static Boolean LOG_SWITCH = true; // 日志文件总开关  
    public static Boolean LOG_WRITE_TO_FILE = logSharePre.getBoolean(SettingUtil.LOG_KEY,false);// 日志写入文件开关  
    public static char LOG_TYPE='v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息  
    public static String LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().toString();// 日志文件在sdcard中的路径  
    public static int SDCARD_LOG_FILE_SAVE_DAYS = 3;// sd卡中日志文件的最多保存天数  
    public static String LOGFILEName = "Log.txt";// 本类输出的日志文件名称  
    public static SimpleDateFormat LOGSdf = new SimpleDateFormat(  
		"yyyy-MM-dd HH:mm:ss");// 日志的输出格式  
    public static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式  

    public static void w(String tag, Object msg)
	{ // 警告信息  
        log(tag, msg.toString(), 'w');  
    }  

    public static void e(String tag, Object msg)
	{ // 错误信息  
        log(tag, msg.toString(), 'e');  
    }  

    public static void d(String tag, Object msg)
	{// 调试信息  
        log(tag, msg.toString(), 'd');  
    }  

    public static void i(String tag, Object msg)
	{//  
        log(tag, msg.toString(), 'i');  
    }  

    public static void v(String tag, Object msg)
	{  
        log(tag, msg.toString(), 'v');  
    }  

    public static void w(String tag, String text)
	{  
        log(tag, text, 'w');  
    }  

    public static void e(String tag, String text)
	{  
        log(tag, text, 'e');  
    }  

    public static void d(String tag, String text)
	{  
        log(tag, text, 'd');  
    }  

    public static void i(String tag, String text)
	{  
        log(tag, text, 'i');  
    }  

    public static void v(String tag, String text)
	{  
        log(tag, text, 'v');  
    }  
  
    public static void log(String tag, String msg, char level)
	{  
        if (LOG_SWITCH)
		{  
            if ('e' == level && ('e' == LOG_TYPE || 'v' == LOG_TYPE))
			{ // 输出错误信息  
                Log.e(tag, msg);  
            }
			else if ('w' == level && ('w' == LOG_TYPE || 'v' == LOG_TYPE))
			{  
                Log.w(tag, msg);  
            }
			else if ('d' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE))
			{  
                Log.d(tag, msg);  
            }
			else if ('i' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE))
			{  
                Log.i(tag, msg);  
            }
			else
			{  
                Log.v(tag, msg);  
            } 
            if (logSharePre.getBoolean(SettingUtil.LOG_KEY,false))  
                writeLogtoFile(String.valueOf(level), tag, msg);  
        }  
    }  

    /** 
     * 打开日志文件并写入日志 
     *  
     * @return 
     * **/  
    public static void writeLogtoFile(String LOGtype, String tag, String text)
	{
        Date nowtime = new Date();  
        String needWriteFiel = logfile.format(nowtime);  
        String needWriteMessage = LOGSdf.format(nowtime) + "    " + LOGtype  
			+ "    " + tag + "    " + text;  
        File file = new File(LOG_PATH_SDCARD_DIR, needWriteFiel  
							 + LOGFILEName);  
        try
		{  
            FileWriter filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖  
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);  
            bufWriter.write(needWriteMessage);  
            bufWriter.newLine();  
            bufWriter.close();  
            filerWriter.close();  
        }
		catch (IOException e)
		{  
            e.printStackTrace();
        }  
    }  

    /** 
     * 删除制定的日志文件 
     * */  
    public static void delFile()
	{
		for(int i = SDCARD_LOG_FILE_SAVE_DAYS; i<100 ;i ++){
			String needDelFiel = logfile.format(getDateBefore(i));  
			File file = new File(LOG_PATH_SDCARD_DIR, needDelFiel + LOGFILEName);  
			if (file.exists())
			{  
				file.delete();  
			}  
		}
        
    }  

    
    public static Date getDateBefore(int daysBefore)
	{  
        Date nowtime = new Date();  
        Calendar now = Calendar.getInstance();  
        now.setTime(nowtime);  
        now.set(Calendar.DATE, now.get(Calendar.DATE)  
                - daysBefore);  
        return now.getTime();  
    }  

}  
