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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.thesis.doctorsappointment.DoctorFragments.AppointmentPreviewFragment;
import com.thesis.doctorsappointment.models.AppointmentRequest;
import com.thesis.doctorsappointment.DoctorFragments.AppointmentFragment;
import com.thesis.doctorsappointment.DoctorMainActivity;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private static final String TAG = "myLogTag";
    private final FragmentManager fragmentManager;
    private final FirebaseFirestore fireStore;
    private Context context;
    private List<AppointmentRequest> appointmentRequestList;
    private ProgressDialog progressDialog;
    private Map<AppointmentRequest, Uri> patient_pictures;

    public AppointmentAdapter(Context context, List<AppointmentRequest> appointmentRequestList, FragmentManager fragmentManager) {
        this.context = context;
        this.appointmentRequestList = appointmentRequestList;
        progressDialog= new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        patient_pictures = new HashMap<>();
        this.fragmentManager = fragmentManager;
        this.fireStore = FirebaseFirestore.getInstance();
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
        final int index = holder.getAdapterPosition();
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

        holder.appointment_parent.setTag(appointmentRequest);
        holder.appointment_parent.setOnClickListener(clickPreviewAppointment);

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to cancel the appointment with "+appointmentRequest.getName()+" for "+appointmentRequest.getDateAndTime()+"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Cancelling...");
                                progressDialog.show();

                                FirebaseFirestore.getInstance().collection("DoctorAppointments").document(appointmentRequest.getDoctorAppointKey())
                                                .delete();

                                WriteBatch batch = fireStore.batch();

                                DocumentReference patientRef = fireStore.collection("PatientAppointments").document(appointmentRequest.getPatientAppointKey());
                                batch.delete(patientRef);

                                DocumentReference doctorRef = fireStore.collection("DoctorAppointments").document(appointmentRequest.getDoctorAppointKey());
                                batch.delete(doctorRef);
                                batch.commit().addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                                        appointmentRequestList.remove(appointmentRequest);
                                        notifyItemRemoved(index);
                                    }
                                    else{
                                        ReusableFunctionsAndObjects.showMessageAlert(context, "Update Error", task.getException().getMessage(), "Close",(byte)0);
                                    }
                                });
                            }
                        }).setNegativeButton("No",null).show();
            }
        });
        holder.confirm.setVisibility(View.GONE);
    }

    private final View.OnClickListener clickPreviewAppointment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppointmentRequest appointmentRequest = (AppointmentRequest)v.getTag();
            Uri imageUrl = null;
            if(patient_pictures.containsKey(appointmentRequest)) {
                imageUrl = patient_pictures.get(appointmentRequest);
            }
            AppointmentPreviewFragment fragment = AppointmentPreviewFragment.newInstance(appointmentRequest, imageUrl == null? "" : imageUrl.toString());
// Perform the Fragment transaction
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_Container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    };

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
        View appointment_parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.patient_name);
            email=itemView.findViewById(R.id.email);
            phone=itemView.findViewById(R.id.phone);
            datetime=itemView.findViewById(R.id.date_time);
            confirm=itemView.findViewById(R.id.confirm);
            reject=itemView.findViewById(R.id.reject);
            patient_picture=itemView.findViewById(R.id.patient_picture);
            appointment_parent=itemView.findViewById(R.id.appointment_parent);
        }
    }
}
