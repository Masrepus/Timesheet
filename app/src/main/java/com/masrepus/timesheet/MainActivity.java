package com.masrepus.timesheet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.masrepus.timesheet.database.References;
import com.masrepus.timesheet.database.Timerecord;
import com.masrepus.timesheet.database.Timesheet;
import com.masrepus.timesheet.database.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private FirebaseAuth auth;
    private NavigationView navigationView;
    private DatabaseReference timesheets;
    private DataSnapshot timesheetSnapshot;
    private DatabaseReference users;
    private DataSnapshot userSnapshot;
    private DatabaseReference timerecords;
    private DataSnapshot timerecordSnapshot;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> addTimeRecord());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            initUserData();
            setUpDatabase();
        }
    }

    private void setUpDatabase() {
        timesheets = FirebaseDatabase.getInstance().getReference(References.TIMESHEETS);
        users = FirebaseDatabase.getInstance().getReference(References.USERS);
        timerecords = FirebaseDatabase.getInstance().getReference(References.TIMERECORDS);

        timesheets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timesheetSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //check if this user already has a timesheet, else create a new one
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userSnapshot = dataSnapshot;
                if (!dataSnapshot.hasChild(uid)) {
                    createEmptyTimesheet();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        timerecords.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timerecordSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String createEmptyTimesheet() {
        String timesheetId = timesheets.push().getKey();
        Timesheet timesheet = new Timesheet(auth.getCurrentUser().getDisplayName(), Arrays.asList(uid));
        timesheets.child(timesheetId).setValue(timesheet);
        User user = new User(Arrays.asList(timesheetId));
        users.child(uid).setValue(user);
        return timesheetId;
    }

    private void addTimeRecord() {
        Timerecord timerecord = new Timerecord(System.currentTimeMillis(), System.currentTimeMillis() + 18000000, 15);
        String sheetId;
        if (!userSnapshot.hasChild(uid)) {
            sheetId = createEmptyTimesheet();
        }
        else {
            User user = userSnapshot.child(uid).getValue(User.class);
            sheetId = user.getTimesheets().get(0);
        }
        timerecords.child(sheetId).push().setValue(timerecord);
    }

    private void initUserData() {
        FirebaseUser user = auth.getCurrentUser();
        uid = user.getUid();

        new ImageLoader().execute(user.getPhotoUrl().toString());

        View headerLayout = navigationView.getHeaderView(0);
        TextView username = (TextView) headerLayout.findViewById(R.id.textview_username);
        username.setText(user.getDisplayName());
        TextView email = (TextView) headerLayout.findViewById(R.id.textview_user_email);
        email.setText(user.getEmail());
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            BitmapFactory.Options bmOptions;
            bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;
            return loadBitmap(params[0], bmOptions);
        }

        public Bitmap loadBitmap(String URL, BitmapFactory.Options options) {
            Bitmap bitmap = null;
            InputStream in = null;
            try {
                in = openHttpConnection(URL);
                bitmap = BitmapFactory.decodeStream(in, null, options);
                in.close();
            } catch (IOException e1) {
            }
            return bitmap;
        }

        private InputStream openHttpConnection(String strURL)
            throws IOException {
            InputStream inputStream = null;
            URL url = new URL(strURL);
            URLConnection conn = url.openConnection();

            try {
                HttpsURLConnection httpConn = (HttpsURLConnection) conn;
                httpConn.setRequestMethod("GET");
                httpConn.connect();

                if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = httpConn.getInputStream();
                }
            } catch (Exception ex) {}
            return inputStream;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            View headerLayout = navigationView.getHeaderView(0);
            ImageView userImage = (ImageView) headerLayout.findViewById(R.id.imageView_user);
            userImage.setImageBitmap(bitmap);
        }
    }
}
