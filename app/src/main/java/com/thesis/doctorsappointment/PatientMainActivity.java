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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thesis.doctorsappointment.models.UserDetails;
import com.thesis.doctorsappointment.PatientFragments.MyAlarmsFragment;
import com.thesis.doctorsappointment.PatientFragments.MyAppointmentFragment;
import com.thesis.doctorsappointment.PatientFragments.PatientSearchDoctorsFragment;
import com.thesis.doctorsappointment.PatientFragments.PendingAppointmentFragment;

import java.util.Objects;

public class PatientMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "myLogTag";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressDialog progressDialog;
    private ImageView profileImage;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

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

                        initializeProfilePicture();
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

        Long nextAlarmDelay = AlarmReceiver.getNextAlarmDelay(getApplicationContext());
        AlarmService.setAlarm(getApplicationContext(), nextAlarmDelay);

        imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    // Set the profile picture using Glide
                    setProfileImage();

                    // Upload the image file to Firebase Storage
                    uploadImageToFirebaseStorage();
                }
            }
        });
//        AlarmService.scheduleAlarms(getApplicationContext());
    }

    private void initializeProfilePicture() {
        // Find the ImageView instance
        profileImage = drawerLayout.findViewById(R.id.profile_image);

        // Get Firebase storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set the profile picture using Glide
        setProfileImage();

        // Set OnClickListener for profileImage
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });


    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void setProfileImage() {
        Glide.with(this)
            .load(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl())
            .placeholder(R.drawable.ic_baseline_perm_identity_24)
            .error(R.drawable.ic_baseline_perm_identity_24)
            .into(profileImage);
    }

    private void uploadImageToFirebaseStorage() {
        if (selectedImageUri != null) {
            StorageReference imageRef = storageReference.child("profile_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image upload successful
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()) {
                                updateUserPhotoUri(task.getResult());
                                Toast.makeText(PatientMainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(PatientMainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Image upload failed
                    Toast.makeText(PatientMainActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void updateUserPhotoUri(Uri uri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build();

        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Photo URL update successful
                    // You can also retrieve the updated photo URL using currentUser.getPhotoUrl()
                    setProfileImage();
                    Toast.makeText(PatientMainActivity.this, "Photo URL updated", Toast.LENGTH_SHORT).show();
                } else {
                    // Photo URL update failed
                    Toast.makeText(PatientMainActivity.this, "Photo URL update failed", Toast.LENGTH_SHORT).show();
                }
            });
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