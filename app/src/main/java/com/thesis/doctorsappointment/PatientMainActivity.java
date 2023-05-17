package com.thesis.doctorsappointment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.thesis.doctorsappointment.DataRetrievalClass.UserDetails;
import com.thesis.doctorsappointment.PatientFragments.MyAlarmsFragment;
import com.thesis.doctorsappointment.PatientFragments.MyAppointmentFragment;
import com.thesis.doctorsappointment.PatientFragments.PatientSearchDoctorsFragment;
import com.thesis.doctorsappointment.PatientFragments.PendingAppointmentFragment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PatientMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "myLogTag";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);
        progressDialog=new ProgressDialog(PatientMainActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseDatabase.getInstance().getReference().child("UserDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if(snapshot.exists()){
                    final UserDetails userDetails=snapshot.getValue(UserDetails.class);
                    if(userDetails.getUserType().trim().equalsIgnoreCase("Patient")){
                        ReusableFunctionsAndObjects.setValues(userDetails.getFirstName()+" "+userDetails.getLastName(),userDetails.getEmail(),userDetails.getMobileNo());
                        TextView name=findViewById(R.id.name);
                        name.setText(userDetails.getFirstName()+" "+userDetails.getLastName());
                        name=findViewById(R.id.iniTv);
                        name.setText(userDetails.getFirstName().charAt(0)+""+userDetails.getLastName().charAt(0));
                        drawerLayout=findViewById(R.id.drawer_layout);
                        Toolbar toolbar=findViewById(R.id.toolBar);
                        setSupportActionBar(toolbar);
                        navigationView=findViewById(R.id.navigation_view);
                        navigationView.setNavigationItemSelectedListener(PatientMainActivity.this);
                        SwitchCompat switchCompat=(SwitchCompat)navigationView.getMenu().findItem(R.id.nav_switch).getActionView();
                        if(PatientMainActivity.this.getSharedPreferences("STORAGE",MODE_PRIVATE).getBoolean("IS_DARKMODE_ENABLED",false)){
                            switchCompat.setChecked(true);
                        }else{
                            switchCompat.setChecked(false);
                        }
                        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked){
                                    PatientMainActivity.this.getSharedPreferences("STORAGE",MODE_PRIVATE).edit().putBoolean("IS_DARKMODE_ENABLED",true).apply();
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                }else{
                                    PatientMainActivity.this.getSharedPreferences("STORAGE",MODE_PRIVATE).edit().putBoolean("IS_DARKMODE_ENABLED",false).apply();
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                }
                            }
                        });
                        ActionBarDrawerToggle toggle =new ActionBarDrawerToggle(PatientMainActivity.this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
                        drawerLayout.addDrawerListener(toggle);
                        toggle.syncState();
                        loadFragment(new PatientSearchDoctorsFragment(), "Search doctors",R.id.search_doctor);
                    }else{
                        logout();
                    }
                }else{
                    logout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        askNotificationPermission();

        Long nextAlarmDelay = AlarmReceiver.getNextAlarmDelay(getApplicationContext());
        AlarmService.setAlarm(getApplicationContext(), nextAlarmDelay);
//        AlarmService.scheduleAlarms(getApplicationContext());
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                // Log and toast
//                Log.d(TAG, token);
//                Toast.makeText(PatientMainActivity.this, token, Toast.LENGTH_SHORT).show();
//                sendRegistrationToServer(token);
            }
        });
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        FirebaseFirestore.getInstance().collection("FCM").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .set(data, SetOptions.merge()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "new token updated: " + token);
            }
            else{
                Log.e(TAG, task.getException().getMessage());
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String t = "";
        int ID=0;
        switch (item.getItemId()){
            case R.id.search_doctor:
                fragment = new PatientSearchDoctorsFragment();
                t = "Search doctors";
                ID=R.id.search_doctor;
                break;
            case R.id.pending_apt:
                fragment = new PendingAppointmentFragment();
                t = "Pending Appointments";
                ID=R.id.pending_apt;
                break;
            case R.id.apt:
                fragment = new MyAppointmentFragment();
                t = "My Appointments";
                ID=R.id.apt;
                break;
            case R.id.alarms:
                fragment = new MyAlarmsFragment();
                t = "My Alarms";
                ID=R.id.alarms;
                break;
            case R.id.logout:
                new AlertDialog.Builder(PatientMainActivity.this).setMessage("Are you sure you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }).setNegativeButton("No",null).show();
            break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return loadFragment(fragment,t,ID);
    }

    private boolean loadFragment(Fragment fragment, String title, int IDD) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_Container, fragment).commit();
            getSupportActionBar().setTitle(Html.fromHtml("<font>" + title + "</font>"));
            navigationView.setCheckedItem(IDD);
            return true;
        }
        return false;
    }

    private void logout(){
        PatientMainActivity.this.getSharedPreferences("STORAGE",MODE_PRIVATE).edit().putBoolean("IS_DARKMODE_ENABLED",false).apply();
        PatientMainActivity.this.getSharedPreferences("STORAGE",MODE_PRIVATE).edit().putString("USER_TYPE","NON").apply();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(PatientMainActivity.this,AskDoctorPatient.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}