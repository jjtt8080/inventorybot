package com.gegejiejie.inventory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.Toast;

public class ProductsActivity extends AppCompatActivity {
    protected static final int PRODUCTS_REQUEST_CODE = 30;
    protected TableLayout tab = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        TableLayout tableLayout = (TableLayout) findViewById(R.id.history_tablelayout);
        if (tableLayout == null) {
            Toast.makeText(this, "Wrong design time problem", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String tableName = SqliteDatabaseObject.ProductColumns.TABLE_NAME;
        String[] columnNames = {SqliteDatabaseObject.ProductColumns.COLUMN_NAME_PRODUCT_ID,
                SqliteDatabaseObject.ProductColumns.COLUMN_NAME_QUANTITY};

        String[] columnDesc = {"Product ID",
                "Quantity"};

        boolean bSuccess = TableBrowsingUtil.browseDBTable(tableLayout,
                this,
                tableName,
                columnNames,
                columnDesc);

    }


}
