package cc.yuyeye.wk.DB;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import cc.yuyeye.wk.R;

import static cc.yuyeye.wk.Fragment.ChatFragment.qq;
import static cc.yuyeye.wk.Fragment.ChatFragment.tencentUtil;

public class ChatDb extends Activity {
    public ChatDbHelper DbHelper;

    public ChatDb(Context context) {
        DbHelper = new ChatDbHelper(context, "chatRecord");
    }

    public ChatDb(Context context, boolean i) {
        DbHelper = new ChatDbHelper(context, "chatRecord");
        if (i) {
            SQLiteDatabase db = DbHelper.getWritableDatabase();
            DbHelper.onUpgrade(db, 1, 2);
        }
    }

    public ArrayList<String> getContacts(Context context, String alias) {
        Cursor cursor;
        ArrayList<String> contacts = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getReadableDatabase();
        String Turing = context.getResources().getString(R.string.Turing);
        contacts.add(context.getResources().getString(R.string.allContacts));
        contacts.add(Turing);
        try {
            cursor = db.query(true, alias, new String[]{"cSend "}, null, null, null, null, "cTime desc", null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (!cursor.getString(0).equals(Turing)) {
                        contacts.add(cursor.getString(0));
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.i("sql", "query Error " + e);
        }
        return contacts;
    }

    public ArrayList<chatListBean> getChatRecord(String alias, String sendPerson, String phoneAlias) {
        ArrayList<chatListBean> chatListBeen = new ArrayList<>();
        SQLiteDatabase db = DbHelper.getReadableDatabase();

        try {
            Cursor cursor;
            if (sendPerson.equals("所有人")) {
                cursor = db.query(alias, new String[]{"_id, cSend, cReceive, cMsg, cTime"}, null, null, null, null, "_id", null);
            } else {
                cursor = db.query(alias, new String[]{"_id, cSend, cReceive, cMsg, cTime"}, "(cSend = ? and cReceive = ?) or (cReceive = ? and cSend = ?)", new String[]{sendPerson, phoneAlias, sendPerson, phoneAlias}, null, null, "_id", null);
            }

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    chatListBean clb = new chatListBean();
                    clb.setId(cursor.getInt(0));
                    clb.setcSend(cursor.getString(1));
                    clb.setcReceive(cursor.getString(2));
                    clb.setMsgContent(cursor.getString(3));
                    clb.setTime(cursor.getString(4));

                    if(cursor.getString(1).equals(phoneAlias)){
                        clb.setMeSend(true);
                        clb.setIconUrl(tencentUtil.getQqIconUrl(qq));
                    }else {
                        clb.setMeSend(false);
                    }
                    chatListBeen.add(clb);
                }
                cursor.close();
                return chatListBeen;
            }
            return chatListBeen;
        } catch (Exception e) {
            Log.i("sql", "getChatRecord Error " + e);

        }
        db.close();
        return null;
    }

    public void insert(String alias, String send, String receive, String time, String msg) {

        SQLiteDatabase db = DbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("cSend", send);
        cv.put("cReceive", receive);
        cv.put("cTime", time);
        cv.put("cMsg", msg);
        db.insert(alias, null, cv);
        db.close();
    }

    public void delete(String alias) {
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.delete(alias, null, null);
        db.close();
    }

    public void deleteById(String alias, int id) {
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + alias + " WHERE _id=" + id);
        db.close();
    }

    public void deleteByContact(String alias, String contact) {
        SQLiteDatabase db = DbHelper.getWritableDatabase();
        db.delete(alias, "(cSend = ? and cReceive = ?) or (cReceive = ? and cSend = ?)", new String[]{contact, alias, contact, alias});
        db.close();
    }

}
