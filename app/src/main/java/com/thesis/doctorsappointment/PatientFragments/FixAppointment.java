package com.thesis.doctorsappointment.PatientFragments;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.thesis.doctorsappointment.models.AppointmentRequest;
import com.thesis.doctorsappointment.models.PatientAppointmentRequest;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;
import java.util.Calendar;
import java.util.HashMap;

public class FixAppointment extends AppCompatActivity {

    private TextView date,time;
    private String docid,name,addr,city,spl;
    private ProgressDialog progressDialog;
    private int d = 0,m=0,y=0,min=0,h=0;
    private HashMap<String,String> hashMap;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_appointment);
        setTitle("Get Appointment");
        date=findViewById(R.id.date);
        time=findViewById(R.id.time);
        TextView t=findViewById(R.id.name);
        progressDialog=new ProgressDialog(FixAppointment.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        firestore = FirebaseFirestore.getInstance();
        name=getIntent().getStringExtra("NAME");
        t.setText(name);
        t=findViewById(R.id.spl);
        spl=getIntent().getStringExtra("SPL");
        t.setText("Specialization: "+spl);
        t=findViewById(R.id.city);
        city=getIntent().getStringExtra("CITY");
        t.setText("City: "+city);
        t=findViewById(R.id.addr);
        addr=getIntent().getStringExtra("ADDR");
        t.setText("Address: "+addr);
        docid=getIntent().getStringExtra("DOCID");
        Calendar c= Calendar.getInstance();
        y=c.get(Calendar.YEAR);
        m=c.get(Calendar.MONTH);
        d=c.get(Calendar.DAY_OF_MONTH);
        h=c.get(Calendar.HOUR_OF_DAY);
        min=c.get(Calendar.MINUTE);
        date.setText(d+"/"+(m+1)+"/"+y);
        if (h == 0) {
            time.setText(h+":"+min);
            time.setText("12"+":"+min+" AM");
        } else if (h < 12) {
            time.setText(h+":"+min+" AM");
        } else if (h == 12) {
            time.setText(h+":"+min+" PM");
        } else {
            time.setText((h-12)+":"+min+" PM");
        }
        AppCompatButton datebtn=findViewById(R.id.setdate);
        AppCompatButton timebtn=findViewById(R.id.settime);
        AppCompatButton setapp=findViewById(R.id.setappoinment);
        datebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(FixAppointment.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                }, y, m, d);
                datePickerDialog.show();
            }
        });

        timebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog=new TimePickerDialog(FixAppointment.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay == 0) {
                            time.setText("12"+":"+minute+" AM");
                        } else if (hourOfDay < 12) {
                            time.setText(hourOfDay+":"+minute+" AM");
                        } else if (hourOfDay == 12) {
                            time.setText(hourOfDay+":"+minute+" PM");
                        } else {
                            time.setText((hourOfDay-12)+":"+minute+" PM");
                        }
                    }
                },h,min,false);
                timePickerDialog.show();
            }
        });

        setapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(FixAppointment.this);
                builder.setTitle("Confirmation").setMessage("Are you sure you want set an appointment with Dr. "+name+" on "+date.getText()+" at "+time.getText()+"?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();

                        WriteBatch batch = firestore.batch();

                        PatientAppointmentRequest newPatientAppointment = new PatientAppointmentRequest();

                        newPatientAppointment.setStatus("pending");
                        newPatientAppointment.setAddress(addr);
                        newPatientAppointment.setCity(city);
                        newPatientAppointment.setDocID(docid);
                        newPatientAppointment.setName(name);
                        newPatientAppointment.setSpecialization(spl);
                        newPatientAppointment.setDateAndTime(date.getText()+" "+time.getText());
                        newPatientAppointment.setPatientUserKey(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        // Set the value of 'NYC'
                        DocumentReference patientRef = firestore.collection("PatientAppointments").document();
                        DocumentReference doctorRef = firestore.collection("DoctorAppointments").document();

                        newPatientAppointment.setDoctorAppointKey(doctorRef.getId());
                        newPatientAppointment.setPatientAppointKey(patientRef.getId());

                        AppointmentRequest newDoctorAppointment = new AppointmentRequest();

                        newDoctorAppointment.setStatus("pending");
                        newDoctorAppointment.setName(ReusableFunctionsAndObjects.Name);
                        newDoctorAppointment.setPatientID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        newDoctorAppointment.setPatientEmail(ReusableFunctionsAndObjects.Email);
                        newDoctorAppointment.setPatientPhone(ReusableFunctionsAndObjects.MobileNo);
                        newDoctorAppointment.setPatientAppointKey(patientRef.getId());
                        newDoctorAppointment.setDoctorAppointKey(doctorRef.getId());
                        newDoctorAppointment.setDateAndTime(date.getText()+" "+time.getText());
                        newDoctorAppointment.setDoctorId(docid);

                        batch.set(patientRef, newPatientAppointment);
                        batch.set(doctorRef, newDoctorAppointment);

// Commit the batch
                        batch.commit().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                AlertDialog alertDialog = new AlertDialog.Builder(FixAppointment.this).create();
                                alertDialog.setTitle("Completed");
                                alertDialog.setMessage("Appointment has been requested to Dr. "+name+" on "+date.getText()+" at "+time.getText()+". Check status in pending appointments.");
                                alertDialog.setIcon(android.R.drawable.ic_dialog_info);
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                                alertDialog.show();
                            }
                            else{
                                ReusableFunctionsAndObjects.showMessageAlert(FixAppointment.this, "Error", task.getException().getMessage(), "OK",(byte)0);
                            }
                            progressDialog.dismiss();
                        });
                    }
                });
                builder.setNegativeButton("No",null);
                builder.setCancelable(false).show();
            }
        });

    }
}
