package com.appinforium.newthinktanktutorials;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appinforium.newthinktanktutorials.adapter.NavDrawerListAdapter;
import com.appinforium.newthinktanktutorials.adapter.NewThinkTankSyncAdapter;
import com.appinforium.newthinktanktutorials.data.AppDataContentProvider;
import com.appinforium.newthinktanktutorials.data.AppDatabase;
import com.appinforium.newthinktanktutorials.extras.ButteryProgressBar;
import com.appinforium.newthinktanktutorials.model.NavDrawerItem;
import com.appinforium.newthinktanktutorials.service.ArticlesUpdaterIntentService;
import com.appinforium.newthinktanktutorials.service.PlaylistUpdaterIntentService;
import com.appinforium.newthinktanktutorials.service.PlaylistsUpdaterIntentService;

import java.util.ArrayList;


public class MainActivity extends Activity implements
        PlaylistsGridFragment.OnPlaylistSelectedListener,
        VideosGridFragment.OnVideoSelectedListener,
        VideoDetailFragment.OnWatchVideoClickedListener,
        VideoDetailFragment.OnBookmarkVideoClickedListener,
        VideoPlayerFragment.OnVideoDetailsListener,
        ArticlesListFragment.OnArticleClickedListener {


    private static final String DEBUG_TAG = "MainActivity";
    private static final String TITLE = "TITLE";
    private static final String LOADING_QUEUE = "LOADING_QUEUE";
    private static final String ACCOUNT = "dummyaccount";
    private static final String ACCOUNT_TYPE = "com.appinforium.newthinktanktutorials";

    // Sync interval constants
    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 120L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE *
                    MILLISECONDS_PER_SECOND;

    private int loadingJobs = 0;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;

    private ButteryProgressBar progressBar;
    private ActionBarDrawerToggle drawerToggle;
    // nav drawer title
    private CharSequence drawerTitle;

    // used to store app title
    private CharSequence curActionBarTitle;

    // Menu Identifiers
    private final static int TOPICS = 0;
    private final static int MOST_RECENT = 1;
    private final static int BOOKMARKED = 2;
    private final static int ARTICLES = 3;
    private final static int ABOUT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // create new ProgressBar and style it
        progressBar = new ButteryProgressBar(this);

        progressBar.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 24));
        // retrieve the top view of our application
        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(progressBar);

        // Here we try to position the ProgressBar to the correct position by looking
        // at the position where content area starts. But during creating time, sizes
        // of the components are not set yet, so we have to wait until the components
        // has been laid out
        // Also note that doing progressBar.setY(136) will not work, because of different
        // screen densities and different sizes of actionBar
        ViewTreeObserver observer = progressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                progressBar.setY(contentView.getY() - 0);

                ViewTreeObserver observer = progressBar.getViewTreeObserver();
                observer.removeGlobalOnLayoutListener(this);
            }
        });


        String[] navMenuTitles;

        ArrayList<NavDrawerItem> navDrawerItems;
        NavDrawerListAdapter navDrawerListAdapter;

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
                getActionBar().setTitle(curActionBarTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(getResources().getString(R.string.app_name));
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
//            Intent intent = new Intent(this, PlaylistsUpdaterIntentService.class);
//            intent.putExtra(PlaylistsUpdaterIntentService.CHANNEL_ID,
//                getResources().getString(R.string.channel_id));
//            startService(intent);
            final Account account = CreateSyncAccount(this);
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */

            // hide the progressBar
            progressBar.setVisibility(View.GONE);

            ContentResolver.requestSync(account, AppDataContentProvider.AUTHORITY, settingsBundle);

            ContentResolver.setSyncAutomatically(account, AppDataContentProvider.AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, AppDataContentProvider.AUTHORITY, new Bundle(), SYNC_INTERVAL);

        } else {

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.executePendingTransactions();
            if (fragmentManager.getBackStackEntryCount() < 1) {
                drawerToggle.setDrawerIndicatorEnabled(true);
            } else {
                drawerToggle.setDrawerIndicatorEnabled(false);
            }

            loadingJobs = savedInstanceState.getInt(LOADING_QUEUE);
            if (loadingJobs > 0) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            setTitle(savedInstanceState.getString(TITLE));
        }



    }



    private void displayMenuFragment(int menuPosition) {

        Fragment fragment = null;
        Bundle args = null;

        switch (menuPosition) {
            case TOPICS:

                fragment = new PlaylistsGridFragment();
//                setTitle(navMenuTitles[position]);
                break;
            case MOST_RECENT:
                fragment = new VideosGridFragment();
                args = new Bundle();
                args.putString(VideosGridFragment.SORT_ORDER, AppDatabase.COL_PUBLISHED_AT + " DESC LIMIT 20");
                fragment.setArguments(args);
                setTitle("Most Recent");
                break;
            case BOOKMARKED:
                fragment = new VideosGridFragment();
                args = new Bundle();
                args.putString(VideosGridFragment.SELECTION, AppDatabase.COL_BOOKMARKED + "=?");
                String[] selectionArgs = {"1"};
                args.putStringArray(VideosGridFragment.SELECTION_ARGS, selectionArgs);
                args.putString(VideosGridFragment.EMPTY_MSG, "You have no bookmarks.");
                fragment.setArguments(args);
                setTitle("Bookmarks");
                break;
            case ARTICLES:
                fragment = new ArticlesListFragment();
                setTitle("Articles");
                Intent intent = new Intent(this, ArticlesUpdaterIntentService.class);
                startService(intent);
                break;
            case ABOUT:
                fragment = new AboutFragment();
                setTitle("About");
                break;
        }

        if (fragment != null) {

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    android.R.animator.fade_in, android.R.animator.fade_out);
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

//            String selection = "playlist_id = ?";
//            String[] selectionArgs = { playlistId};
//
//            args.putString(VideosGridFragment.SELECTION, selection);
//            args.putStringArray(VideosGridFragment.SELECTION_ARGS, selectionArgs);
            args.putString(VideosGridFragment.ID_PLAYLIST, cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ID)));
            args.putString(VideosGridFragment.SORT_ORDER, "published_at ASC");
            fragment.setArguments(args);

            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                android.R.animator.fade_in, android.R.animator.fade_out);

            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            setTitle(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));

            drawerToggle.setDrawerIndicatorEnabled(false);

//            Intent intent = new Intent(this, PlaylistUpdaterIntentService.class);
//            intent.putExtra(PlaylistUpdaterIntentService.PLAYLIST_ID, playlistId);
//            startService(intent);
//            setProgressBarIndeterminateVisibility(true);
        }

        cursor.close();

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

        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
            android.R.animator.fade_in, android.R.animator.fade_out);
        fragment.setArguments(args);
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onWatchVideoClicked(String videoId, long videoIndex) {

        String[] projection = {AppDatabase.COL_DURATION, AppDatabase.COL_PLAY_TIME};

        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, String.valueOf(videoIndex));
        Cursor cursor = getContentResolver().query(content_uri, projection, null, null, null);

        if (cursor.moveToFirst()) {
            int startTime = cursor.getInt(cursor.getColumnIndex(AppDatabase.COL_PLAY_TIME));

            VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();

            Bundle args = new Bundle();
            args.putString(VideoPlayerFragment.VIDEO_ID, videoId);
            args.putLong(VideoPlayerFragment.VIDEO_INDEX, videoIndex);
            args.putInt(VideoPlayerFragment.START_TIME, startTime);

            videoPlayerFragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content_frame, videoPlayerFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBookmarkVideoClicked(long videoIndex, boolean bookmarked) {

        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, String.valueOf(videoIndex));

        ContentValues contentValues = new ContentValues();

        if (bookmarked) {
            contentValues.put(AppDatabase.COL_BOOKMARKED, "1");
            Toast.makeText(this, "Bookmark created", Toast.LENGTH_LONG).show();
        } else {
            contentValues.put(AppDatabase.COL_BOOKMARKED, "0");
            Toast.makeText(this, "Bookmark removed", Toast.LENGTH_LONG).show();
        }
        getContentResolver().update(content_uri, contentValues, null, null);
    }

    @Override
    public void onVideoDetails(long videoIndex, int playTime, int duration) {
        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_VIDEOS, String.valueOf(videoIndex));

        ContentValues contentValues = new ContentValues();

        contentValues.put(AppDatabase.COL_DURATION, duration);
        contentValues.put(AppDatabase.COL_PLAY_TIME, playTime);

        getContentResolver().update(content_uri, contentValues, null, null);
    }

    @Override
    public void onArticleClicked(long id) {

        String[] projection = {AppDatabase.COL_ID, AppDatabase.COL_ARTICLE_URL, AppDatabase.COL_TITLE};

        Uri content_uri = Uri.withAppendedPath(AppDataContentProvider.CONTENT_URI_ARTICLES, String.valueOf(id));
        Cursor cursor = getContentResolver().query(content_uri, projection, null, null, null);

        if (cursor.moveToFirst()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            WebViewFragment webViewFragment = new WebViewFragment();

            Bundle args = new Bundle();
            args.putString(WebViewFragment.WEB_URL, cursor.getString(cursor.getColumnIndex(AppDatabase.COL_ARTICLE_URL)));

            webViewFragment.setArguments(args);
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out,
                    android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentTransaction.replace(R.id.content_frame, webViewFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            setTitle(cursor.getString(cursor.getColumnIndex(AppDatabase.COL_TITLE)));
            drawerToggle.setDrawerIndicatorEnabled(false);

        }

        cursor.close();
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
        registerReceiver(broadcastReceiver, new IntentFilter(NewThinkTankSyncAdapter.BROADCAST_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter(WebViewFragment.BROADCAST_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter(ArticlesUpdaterIntentService.BROADCAST_ACTION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE, String.valueOf(getActionBar().getTitle()));
        outState.putInt(LOADING_QUEUE, loadingJobs);
        super.onSaveInstanceState(outState);

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

    // Called whenever invalidateOptionsMenu() is called
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
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
        if (id == R.id.action_exit) {
            finish();
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        curActionBarTitle = title;
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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager.executePendingTransactions();
        if (fragmentManager.getBackStackEntryCount() < 1){
            drawerToggle.setDrawerIndicatorEnabled(true);
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (intent.getAction().equals(NewThinkTankSyncAdapter.BROADCAST_ACTION)) {
                    int status = bundle.getInt(NewThinkTankSyncAdapter.STATUS);
                    if (status == NewThinkTankSyncAdapter.RUNNING) {
                        Log.d(DEBUG_TAG, "Youtube Sync running");
//                        progressBar.setVisibility(View.VISIBLE);
                        loadingJobs += 1;
                    } else if (status == NewThinkTankSyncAdapter.FINISHED) {
                        Log.d(DEBUG_TAG, "Youtube Sync finished");
//                        progressBar.setVisibility(View.GONE);
                        loadingJobs -= 1;
                    }
                }


                if (intent.getAction().equals(WebViewFragment.BROADCAST_ACTION)) {
                    int status = bundle.getInt(WebViewFragment.STATUS);
                    if (status == WebViewFragment.LOADING) {
//                        progressBar.setVisibility(View.VISIBLE);
                        loadingJobs += 1;
                    } else if (status == WebViewFragment.FINISHED) {
//                        progressBar.setVisibility(View.GONE);
                        loadingJobs -= 1;
                    }
                }

                if (intent.getAction().equals(ArticlesUpdaterIntentService.BROADCAST_ACTION)) {
                    int status = bundle.getInt(ArticlesUpdaterIntentService.STATUS);
                    if (status == ArticlesUpdaterIntentService.LOADING) {
//                        progressBar.setVisibility(View.VISIBLE);
                        loadingJobs += 1;
                    } else if (status == ArticlesUpdaterIntentService.FINISHED) {
//                        progressBar.setVisibility(View.GONE);
                        loadingJobs -= 1;
                    }
                }

                if (loadingJobs > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
//                int resultCode = bundle.getInt(PlaylistsUpdaterIntentService.RESULT);
//                if (resultCode == RESULT_OK) {
//                    Log.d(DEBUG_TAG, "Playlists updated");
//
//                } else {
//                    Toast.makeText(getApplicationContext(), "Playlists update failed", Toast.LENGTH_LONG).show();
//                }

//                setProgressBarIndeterminateVisibility(false);
            }
        }
    };

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {

        Log.d(DEBUG_TAG, "CreateSyncAccount called");
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d(DEBUG_TAG, "something went wrong during CreateSyncAccount");
            return newAccount;
        }

    }

}
