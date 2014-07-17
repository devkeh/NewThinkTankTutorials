package com.appinforium.newthinktanktutorials;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.appinforium.newthinktanktutorials.adapter.NavDrawerListAdapter;
import com.appinforium.newthinktanktutorials.adapter.VideosCursorAdapter;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;
import com.appinforium.newthinktanktutorials.model.NavDrawerItem;
import com.appinforium.newthinktanktutorials.service.PlaylistUpdaterIntentService;
import com.appinforium.newthinktanktutorials.service.PlaylistsUpdaterIntentService;

import java.util.ArrayList;


public class MainActivity extends Activity implements
        PlaylistsGridFragment.OnPlaylistSelectedListener,
        VideosGridFragment.OnVideoSelectedListener  {

    private static final String DEBUG_TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private String[] navMenuTitles;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter navDrawerListAdapter;
    private ActionBarDrawerToggle drawerToggle;
    // nav drawer title
    private CharSequence drawerTitle;

    // used to store app title
    private CharSequence navTitle;


    // Menu Identifiers
    private final static int TOPICS = 0;
    private final static int MOST_RECENT = 1;
    private final static int ABOUT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navTitle = drawerTitle = getTitle();

        Intent intent = new Intent(this, PlaylistsUpdaterIntentService.class);
        intent.putExtra(PlaylistsUpdaterIntentService.CHANNEL_ID,
                getResources().getString(R.string.channel_id));
        startService(intent);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.nav_list_view);

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        navDrawerListAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        drawerListView.setAdapter(navDrawerListAdapter);
        drawerListView.setOnItemClickListener(new NavMenuClickListener());


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_navigation_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(navTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            displayMenuFragment(TOPICS);
        } else {

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.executePendingTransactions();
            if (fragmentManager.getBackStackEntryCount() < 1) {
                drawerToggle.setDrawerIndicatorEnabled(true);
            } else {
                drawerToggle.setDrawerIndicatorEnabled(false);
            }

            setTitle(navTitle);
        }
    }

    private void displayMenuFragment(int menuPosition) {

        Fragment fragment = null;

        switch (menuPosition) {
            case TOPICS:

                fragment = new PlaylistsGridFragment();
//                setTitle(navMenuTitles[position]);
                break;
            case MOST_RECENT:
                fragment = new VideosGridFragment();
                Bundle args = new Bundle();
                args.putString(VideosGridFragment.SORT_ORDER, AppDatabase.COL_PUBLISHED_AT + " DESC LIMIT 20");
                fragment.setArguments(args);
                setTitle("Most Recent");
                break;
            case ABOUT:
                fragment = new AboutFragment();
                setTitle("About");
                break;
        }

        if (fragment != null) {

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 //            fragmentTransaction.setCustomAnimations();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.commit();

            drawerListView.setItemChecked(menuPosition, true);
            drawerListView.setSelection(menuPosition);
            drawerLayout.closeDrawer(drawerListView);
        }
    }


    @Override
    public void onPlaylistSelected(long id) {


        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_PLAYLIST_ID, AppDatabase.COL_TITLE};
        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_PLAYLISTS, String.valueOf(id));

        Cursor cursor = getContentResolver().query(content_uri, projection, null, null, null);

        if (cursor.moveToFirst()) {
            String playlistId = cursor.getString(cursor.getColumnIndex(AppDatabase.COL_PLAYLIST_ID));
            Log.d(DEBUG_TAG, "Playlist selected: " + playlistId);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = new VideosGridFragment();
            Bundle args = new Bundle();

            String selection = "playlist_id = ?";
            String[] selectionArgs = { playlistId};

            args.putString(VideosGridFragment.SELECTION, selection);
            args.putStringArray(VideosGridFragment.SELECTION_ARGS, selectionArgs);

            fragment.setArguments(args);

            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            setTitle(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));

            drawerToggle.setDrawerIndicatorEnabled(false);

            Intent intent = new Intent(this, PlaylistUpdaterIntentService.class);
            intent.putExtra(PlaylistUpdaterIntentService.PLAYLIST_ID, playlistId);
            startService(intent);
        }

//        displayFragment(PLAYLIST_GRID_FRAGMENT);
    }

    @Override
    public void onVideoSelected(long id) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new VideoDetailFragment();

        Bundle args = new Bundle();
        args.putLong(VideoDetailFragment.VIDEO_INDEX, id);

        drawerToggle.setDrawerIndicatorEnabled(false);

        fragment.setArguments(args);
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }


    private class NavMenuClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // display fragment for selected nav drawer item
            displayMenuFragment(position);
        }
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(PlaylistsUpdaterIntentService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
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

        // toggle nav drawer on selecting action bar app icon/title
        if (drawerToggle.isDrawerIndicatorEnabled() && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.executePendingTransactions();
            if (fragmentManager.getBackStackEntryCount() < 1){
                drawerToggle.setDrawerIndicatorEnabled(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        navTitle = title;
        getActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);


    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(PlaylistsUpdaterIntentService.RESULT);
                if (resultCode == RESULT_OK) {
                    Log.d(DEBUG_TAG, "Playlists updated");
                } else {
                    Toast.makeText(getApplicationContext(), "Playlists update failed", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
