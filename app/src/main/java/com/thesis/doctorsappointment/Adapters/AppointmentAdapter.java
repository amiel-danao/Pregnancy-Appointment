package com.thesis.doctorsappointment.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.thesis.doctorsappointment.DataRetrievalClass.AppointmentRequest;
import com.thesis.doctorsappointment.DoctorFragments.AppointmentFragment;
import com.thesis.doctorsappointment.DoctorMainActivity;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private static final String TAG = "myLogTag";
    private Context context;
    private List<AppointmentRequest> appointmentRequestList;
    private ProgressDialog progressDialog;
    private Map<AppointmentRequest, Uri> patient_pictures;

    public AppointmentAdapter(Context context, List<AppointmentRequest> appointmentRequestList) {
        this.context = context;
        this.appointmentRequestList = appointmentRequestList;
        progressDialog= new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        patient_pictures = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_apt_request,parent,false);
        return new AppointmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentRequest appointmentRequest=appointmentRequestList.get(position);
        holder.name.setText(appointmentRequest.getName());
        holder.email.setText(appointmentRequest.getPatientEmail());
        holder.phone.setText(appointmentRequest.getPatientPhone());
        holder.datetime.setText(appointmentRequest.getDateAndTime());
        holder.reject.setText("Cancel");

        if(!patient_pictures.containsKey(appointmentRequest)){
            fetchPatientPictureUri(appointmentRequest, holder.patient_picture);
        }
        else{
            setPatientPhoto(holder.patient_picture, patient_pictures.get(appointmentRequest));
        }

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to cancel the appointment with "+appointmentRequest.getName()+" for "+appointmentRequest.getDateAndTime()+"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Cancelling...");
                                progressDialog.show();
                                FirebaseDatabase.getInstance().getReference().child("ConfirmedPatientAppointments").child(appointmentRequest.getPatientID()).child(appointmentRequest.getPatientAppointKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance().getReference().child("ConfirmedDocAppointments").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(appointmentRequest.getDoctorAppointKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                                                        ((DoctorMainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_Container, new AppointmentFragment(),"Appointments").addToBackStack(null).commit();
                                                    }else {
                                                        progressDialog.dismiss();
                                                        ReusableFunctionsAndObjects.showMessageAlert(context, "Network Error", "Make sure you are connected to internet.", "OK",(byte)0);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    ReusableFunctionsAndObjects.showMessageAlert(context, "Network Error", "Make sure you are connected to internet.", "OK",(byte)0);
                                                }
                                            });
                                        }else{
                                            progressDialog.dismiss();
                                            ReusableFunctionsAndObjects.showMessageAlert(context, "Network Error", "Make sure you are connected to internet.", "OK",(byte)0);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        ReusableFunctionsAndObjects.showMessageAlert(context, "Network Error", "Make sure you are connected to internet.", "OK",(byte)0);
                                    }
                                });
                            }
                        }).setNegativeButton("No",null).show();
            }
        });
        holder.confirm.setVisibility(View.GONE);
    }

    private void fetchPatientPictureUri(AppointmentRequest appointmentRequest, ImageView imageView) {

        FirebaseStorage.getInstance().getReference().child("profile_images/" + appointmentRequest.getPatientID())
                .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            patient_pictures.put(appointmentRequest, task.getResult());
                            setPatientPhoto(imageView, task.getResult());
                        }
                        else{
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });
    }

    void setPatientPhoto(ImageView imageView, Uri uri){
        Glide.with(context)
                .load(uri)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return appointmentRequestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,email,phone,datetime;
        ImageView patient_picture;
        AppCompatButton confirm,reject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.patient_name);
            email=itemView.findViewById(R.id.email);
            phone=itemView.findViewById(R.id.phone);
            datetime=itemView.findViewById(R.id.date_time);
            confirm=itemView.findViewById(R.id.confirm);
            reject=itemView.findViewById(R.id.reject);
            patient_picture=itemView.findViewById(R.id.patient_picture);
        }
    }
}
