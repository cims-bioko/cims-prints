package data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CCODKContentProvider {
    protected Context mContext;
    private final Uri CASE_LISTING = Uri.parse("content://org.commcare.dalvik.case/casedb/case");
    protected final String TAG = "CCODKContentProvider";

    public CCODKContentProvider(Context context) {
        mContext = context;

        Cursor c = mContext.getContentResolver().query(CASE_LISTING, null, null, null, null);
        String[] columns = c.getColumnNames();
        String column_names = "Column Names:";
        for (String column_name : columns) {
            column_names = column_names + " | " + column_name;
        }
        Log.d(TAG, column_names);
        if (c.moveToFirst()) {
            do {
                String base = "";
                for (String column_name : columns) {
                    base = base + " | " + column_name + " -> " + c.getString(c.getColumnIndex(column_name));
                }

                Log.d(TAG, base);
            } while (c.moveToNext());
        }
        c.close();

    }

    public List<String> get_case_ids(String field, String field_value) {
        //SELECT FROM DB STILL NON-OP; ergo filtering inline
        Log.d(TAG, "field: " + field + " | field_value: " + field_value);
        String[] projection = new String[]{"case_id", field};
        String selection = field + " = ?";
        Log.d(TAG, selection);
        String[] selectionArgs = new String[]{field_value};
        Log.d(TAG, "Selection Args | " + selectionArgs[0]);
        String sortOrder = null;
        Cursor c = mContext.getContentResolver().query(CASE_LISTING, projection, selection, selectionArgs, sortOrder);

        if (c.moveToFirst()) {
            List<String> output = new ArrayList<String>();
            int column_index = c.getColumnIndex("case_id");
            do {
                Log.d(TAG, "case_id: " + c.getString(column_index) + " | " + field + ": " + c.getString(c.getColumnIndex(field)) + " | " + Boolean.toString(field_value.equals(c.getString(c.getColumnIndex(field)))));
                //filtering inline
                if (field_value.equals(c.getString(c.getColumnIndex(field))) == true) {
                    output.add(c.getString(column_index));
                }
            } while (c.moveToNext());
            c.close();
            return output;
        } else {
            c.close();
            return new ArrayList<String>();
        }

    }

}
