package com.gegejiejie.inventory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TableLayout;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {
    protected static final int HISTORY_REQUEST_CODE = 20;
    protected TableLayout tab = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        TableLayout tableLayout = (TableLayout)findViewById(R.id.history_tablelayout);
        if (tableLayout == null) {
            Toast.makeText(this, "Wrong design time problem", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String tableName = SqliteDatabaseObject.HistoryColumns.TABLE_NAME;
        String[] columnNames = {SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_DATE,
                SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_STORE_ID,
                SqliteDatabaseObject.HistoryColumns.COLUMN_NAME_PRODUCT_QUANTITY};

        String[] columnDesc = { "Date",
                "Store_ID",
                "Details"
        };

        boolean bSuccess = TableBrowsingUtil.browseDBTable(tableLayout,
               this,
                tableName,
                columnNames,
                columnDesc);



    }
}
