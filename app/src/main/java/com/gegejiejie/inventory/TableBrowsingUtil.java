package com.gegejiejie.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;


public class TableBrowsingUtil {
    private static String columnToString(int columnType, int columnIndex, Cursor c) {
        switch(columnType)
        {
            case FIELD_TYPE_FLOAT:
            case FIELD_TYPE_INTEGER:
                return String.valueOf(c.getInt(columnIndex));

            case FIELD_TYPE_STRING:
                return c.getString(columnIndex);
            case FIELD_TYPE_NULL:
                return "null";

        }
        return "";
    }
    static public boolean browseDBTable(TableLayout  tableLayout,
                                        Context context,
                                        String tableName,
                                        String[] columnNames, String[] columnTitle) {
        SqliteDatabaseObject db = SqliteDatabaseObject.getInstance(context);
        if (db == null ) {
            Toast.makeText(context, "Does not have any history yet, please click on Scan to scan products.", Toast.LENGTH_LONG).show();
            return false;
        }

        Cursor cursorObj = db.query(tableName,
                columnNames, null);
        cursorObj.moveToFirst();

        //First make the label
        TableRow titleRow = new TableRow(context);
        titleRow.setGravity(Gravity.CENTER_HORIZONTAL);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT, 7);
        params.setMargins(5, 5,5,5);
        titleRow.setLayoutParams(params);

        for (int j =0; j < columnTitle.length; ++j) {
            TextView textView = new TextView(context);
            String curText = columnTitle[j];
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(curText);
            textView.setPadding(0, 0, 0, 0);
            textView.setBackgroundColor(Color.parseColor("#ffaaaaaa"));
            textView.setTextSize(15);
            titleRow.addView(textView);
        }
        tableLayout.addView(titleRow);
        for (int i = 0; i  < cursorObj.getCount(); ++i) {
            TableRow tableRow = new TableRow(context);

            for (int j =0; j < cursorObj.getColumnCount(); ++j) {
                TextView textView = new TextView(context);
                int fieldType = cursorObj.getType(j);
                String curText = columnToString(fieldType, j, cursorObj);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setText(curText);
                textView.setPadding(0, 0, 0, 0);
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
            cursorObj.moveToNext();
        }
        return true;
    }

    static protected Uri exportAsFile(final String tableName, Context context, final String sep) throws IOException {
        String dirpath = context.getFilesDir() + File.separator;
        File file = new File(dirpath + "products.txt");


        SqliteDatabaseObject db = SqliteDatabaseObject.getInstance(context);
        if (db == null ) {
            Toast.makeText(context, "Does not have any history yet, please click on Scan to scan products.", Toast.LENGTH_LONG).show();
            return null;
        }

        Cursor cursorObj = db.query(tableName,
                null, null);
        cursorObj.moveToFirst();

        FileWriter writer = new FileWriter(file.getPath());

        for (int j =0; j < cursorObj.getColumnCount(); ++j) {

            String cName = cursorObj.getColumnName(j);
            writer.write(cName);
            if (j !=  cursorObj.getColumnCount()-1) {
                writer.write(sep);
            }

        }
        writer.write(System.lineSeparator());
        for (int i = 0; i  < cursorObj.getCount(); ++i) {

            for (int j =0; j < cursorObj.getColumnCount(); ++j) {

                int fieldType = cursorObj.getType(j);
                String curText = columnToString(fieldType, j, cursorObj);
                writer.write(curText);
                if (j !=  cursorObj.getColumnCount()-1) {
                    writer.write(sep);
                }
            }
            writer.write(System.lineSeparator());
            cursorObj.moveToNext();
        }
        writer.close();
        Uri uri = FileProvider.getUriForFile(context,
                "com.gegejiejie.inventory.fileprovider", file);
        return uri;
    }
}
