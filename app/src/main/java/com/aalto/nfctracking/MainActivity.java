package com.aalto.nfctracking;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aalto.nfctracking.utils.RestApiCall;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements FragmentInteractionListener{

    private NfcAdapter mNfcAdapter;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    static Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("DNES", "MainActivity onCreate");
        checkNFC();
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = getPreferences(0);
        String loggedUserName = preferences.getString("loggedUserName", "");
        if(loggedUserName.isEmpty()) {
            Log.i("DNES", "Not logged in! directing to login screen");
            changeFragment(new LoginFragment());
        } else {
            loginToBimServer();

            setupDrawerContent();

            String nfcReaderId = preferences.getString("nfcReaderId", "");
            if(nfcReaderId.isEmpty()) {
                //reader not registerd to server yet, so load settings fragment.
                changeFragment(new SettingsFragment());
            } else {
                changeFragment(new HomeFragment());
            }
        }


    }

    @Override
    protected void onPause() {
        // Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
        Log.i("DNES", "MainActivity onPause");
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("DNES", "MainActivity onResume");
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("DNES","MainActivity onNewIntent");
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        setIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed(){
        if(currentFragment instanceof HomeFragment) {
            this.finish();
        } else {
            changeFragment(new HomeFragment());
        }
    }

    private void checkNFC(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled!", Toast.LENGTH_LONG).show();
        }
    }

    private void setupDrawerContent() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });

    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.read_tag_fragment:
                fragmentClass = ReadTagFragment.class;
                break;
            case R.id.write_tag_fragment:
                fragmentClass = WriteTagFragment.class;
                break;
            case R.id.settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = ReadTagFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        changeFragment(fragment);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    public void changeFragment(Fragment destinationFragment) {
        currentFragment = destinationFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, destinationFragment).commit();
        getIntent().setAction("");
    }

    private boolean isUserLoggedIn() {
        Log.i("DNES", "Main Activity | checkUserLoggedIn");
        SharedPreferences preferences = getPreferences(0);
        String userName = preferences.getString("loggedUserName", "");
        if(userName.equals("")){
            changeFragment(new LoginFragment());
            return false;
        } else {
            return true;
        }
    }

    private void loginToBimServer(){
        Log.i("DNES", "Main Activity | loginToBimServer");
        try {
            JSONObject req = new JSONObject();
            req.put("interface", "Bimsie1AuthInterface");
            req.put("method", "login");

            JSONObject param = new JSONObject();
            param.put("username", "admin@bimserver.org");
            param.put("password", "admin");
            req.put("parameters", param);

            Response.Listener successListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("DNES", "Response: " + response.toString());
                    try {
                        String result = response.getJSONObject("response").getString("result");
                        RestApiCall.BIM_AUTH_TOKEN = result;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            RestApiCall.postJsonObjectReqest(this, req, successListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
