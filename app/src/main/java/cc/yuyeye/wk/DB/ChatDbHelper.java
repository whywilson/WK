package cc.yuyeye.wk.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cc.yuyeye.wk.MainActivity;

class ChatDbHelper extends SQLiteOpenHelper
{
	
	private String DB_TABLE = "create table if not exists " + MainActivity.phoneAlias + " (_id integer primary key autoincrement, cSend text, cReceive text, cTime text, cMsg text)";
	
	ChatDbHelper(Context context, String db_name)
	{
		super(context, db_name, null, 1);
	}


	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		p1.execSQL(DB_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
		//String sql1 = "drop table if exists card1";
		p1.execSQL(DB_TABLE);
		onCreate(p1);
	}

}

