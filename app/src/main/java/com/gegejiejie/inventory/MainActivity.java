package com.gegejiejie.inventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //txtView = (TextView) findViewById(R.id.txtResult);
        if (id == R.id.nav_import) {
            Intent intent = new Intent(this, ChatbotActivity.class);
            startActivityForResult(intent, ChatbotActivity.CHATBOT_REQUEST_CODE);
        } else if (id == R.id.nav_gallery) {
            startProductsActivity();
        } else if (id == R.id.nav_slideshow) {
            startHistoryActivity();
        } else if (id == R.id.nav_manage) {


        } else if (id == R.id.nav_share) {
            startShareAction();

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    protected void startHistoryActivity() {
        Intent history_intent = new Intent(this, HistoryActivity.class);
        startActivity(history_intent);
    }
    protected void startProductsActivity() {
        Intent product_intent = new Intent(this, ProductsActivity.class);
        startActivity(product_intent);
    }
    protected void startShareAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
        try {
            Uri uri = TableBrowsingUtil.exportAsFile("Products", this, ",");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        } catch(Exception ex) {
            Log.e("Main", "Error storing file");
            ex.printStackTrace();

            Toast.makeText(this, "Error storing file", Toast.LENGTH_SHORT).show();
            return;
        }

        sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Sharing File...");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }
    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatbotActivity.CHATBOT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
               int numRecords = data.getIntExtra(ChatbotActivity.NUM_RECORDS, 0);
               if (numRecords > 0) {
                   //Open the HistoryActivity product scanning history
                   startProductsActivity();
               }

            }
        }

    }
}
