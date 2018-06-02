package com.gegejiejie.inventory;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Date;

public class DatabaseObject {
    private InventoryDbHelper mDbHelper;
    public static class EmployeeColumns implements BaseColumns {
        public static final String TABLE_NAME = "Employees";
        public static final String COLUMN_NAME_EMPLOYEE_ID = "EMP_ID";
        public static final String COLUMN_NAME_STORE_ID = "STORE_ID";
    }

    public static class ProductColumns implements BaseColumns {
        public static final String TABLE_NAME = "Products";
        public static final String COLUMN_NAME_PRODUCT_ID = "PRODUCT_ID";
        public static final String COLUMN_NAME_QUANTITY = "QUANTITY";
    }
    public static class HistoryColumns implements BaseColumns {
        public static final String TABLE_NAME = "InventoryHistory";
        public static final String COLUMN_NAME_DATE = "INPUT_DATE";
        public static final String COLUMN_NAME_EMPLOYEE_ID = "EMP_ID";
        public static final String COLUMN_NAME_STORE_ID = "STORE_ID";
        public static final String COLUMN_NAME_PRODUCT_QUANTITY = "PRODUCT_QUANTITY";
    }

    private static final String SQL_CREATE_ENTRIES[] =
            {"CREATE TABLE " + EmployeeColumns.TABLE_NAME + " (" +
                    EmployeeColumns._ID + " INTEGER PRIMARY KEY," +
                    EmployeeColumns.COLUMN_NAME_EMPLOYEE_ID + " INTEGER," +
                    EmployeeColumns.COLUMN_NAME_STORE_ID + " INTEGER) ",
             "CREATE TABLE " + ProductColumns.TABLE_NAME + " (" +
                     ProductColumns._ID + " INTEGER PRIMARY KEY," +
                     ProductColumns.COLUMN_NAME_PRODUCT_ID + " 'INTEGER'," +
                     ProductColumns.COLUMN_NAME_QUANTITY + " INTEGER) ",
             "CREATE TABLE " + HistoryColumns.TABLE_NAME + " (" +
                     HistoryColumns._ID + " INTEGER PRIMARY KEY," +
                     HistoryColumns.COLUMN_NAME_DATE + " DATETIME," +
                     HistoryColumns.COLUMN_NAME_EMPLOYEE_ID + " INTEGER, " +
                     HistoryColumns.COLUMN_NAME_STORE_ID + " INTEGER, " +
                     HistoryColumns.COLUMN_NAME_PRODUCT_QUANTITY + " TEXT) "};

    private static final String SQL_DELETE_ENTRIES[] =
            {"DROP TABLE IF EXISTS " + EmployeeColumns.TABLE_NAME,
             "DROP TABLE IF EXISTS " + ProductColumns.TABLE_NAME,
             "DROP TABLE IF EXISTS " + HistoryColumns.TABLE_NAME};

    public class InventoryDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "inventory.db";

        public InventoryDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            for (String s : SQL_CREATE_ENTRIES) {
                db.execSQL(s);
            }
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            for (String s : SQL_DELETE_ENTRIES) {
                db.execSQL(s);
            }
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public void Update(InventoryObject obj) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //First we update the employee ID if there is no such ID exist yet
        ContentValues values = new ContentValues();
        values.put(EmployeeColumns.COLUMN_NAME_EMPLOYEE_ID, obj.emp_id);
        values.put(EmployeeColumns.COLUMN_NAME_STORE_ID, obj.store_id);
        String employeeid_filter = EmployeeColumns.COLUMN_NAME_EMPLOYEE_ID + " = " + obj.emp_id;
        String[] emp_columns = {EmployeeColumns.COLUMN_NAME_EMPLOYEE_ID, EmployeeColumns.COLUMN_NAME_STORE_ID};
        Cursor cs = db.query(true, EmployeeColumns.TABLE_NAME, emp_columns, employeeid_filter, null, null, null, null, null, null);
        if (cs.getCount() == 0) {
            db.beginTransaction();
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(EmployeeColumns.TABLE_NAME, null, values);

            db.endTransaction();
        }
        cs.close();
        values.clear();

        //Then we update the Products and history columns
        String[] product_columns = {ProductColumns.COLUMN_NAME_PRODUCT_ID, ProductColumns.COLUMN_NAME_QUANTITY};
        db.beginTransaction();
        for (int i = 0; i < obj.products_list.size(); ++i)
        {
            int p = obj.products_list.get(i);
            int q = obj.quantity_list.get(i);
            values.put(ProductColumns.COLUMN_NAME_PRODUCT_ID, p);
            values.put(ProductColumns.COLUMN_NAME_QUANTITY, q);
            String productid_filter = ProductColumns.COLUMN_NAME_PRODUCT_ID + " = " + p;
            cs = db.query(true, ProductColumns.TABLE_NAME, product_columns, productid_filter, null, null, null, null, null, null);

            if (cs.getCount() == 0) {
                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(EmployeeColumns.TABLE_NAME, null, values);

            }else {
                db.update(EmployeeColumns.TABLE_NAME, values, productid_filter, null);
            }
            cs.close();
        }
        db.endTransaction();
        String[] history_columns = {HistoryColumns.COLUMN_NAME_DATE, HistoryColumns.COLUMN_NAME_EMPLOYEE_ID, HistoryColumns.COLUMN_NAME_STORE_ID, HistoryColumns.COLUMN_NAME_PRODUCT_QUANTITY};
        String contentString = obj.ProdQuantToString();
        db.beginTransaction();
        values.put(HistoryColumns.COLUMN_NAME_DATE, new Date().getTime());
        values.put(HistoryColumns.COLUMN_NAME_EMPLOYEE_ID, obj.emp_id);
        values.put(HistoryColumns.COLUMN_NAME_STORE_ID, obj.store_id);
        values.put(HistoryColumns.COLUMN_NAME_PRODUCT_QUANTITY, contentString);
        db.insert(HistoryColumns.TABLE_NAME, null, values);
        db.endTransaction();
        //Then we put a history on entries
    }
}