package com.thesis.doctorsappointment.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.thesis.doctorsappointment.DataRetrievalClass.AppointmentRequest;
import com.thesis.doctorsappointment.DataRetrievalClass.PatientAppointmentRequest;
import com.thesis.doctorsappointment.DoctorFragments.AppointmentRequestFragment;
import com.thesis.doctorsappointment.DoctorMainActivity;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;

import java.util.List;

public class AppointmentRequestAdapter  extends RecyclerView.Adapter<AppointmentRequestAdapter.ViewHolder> {

    private final FirebaseFirestore firestore;
    private Context context;
    private List<AppointmentRequest> appointmentRequestList;
    private ProgressDialog progressDialog;

    public AppointmentRequestAdapter(Context context, List<AppointmentRequest> appointmentRequestList) {
        this.context = context;
        this.appointmentRequestList = appointmentRequestList;
        progressDialog= new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_apt_request,parent,false);
        return new AppointmentRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentRequest appointmentRequest=appointmentRequestList.get(position);
        final int index = holder.getAdapterPosition();
        holder.name.setText(appointmentRequest.getName());
        holder.email.setText(appointmentRequest.getPatientEmail());
        holder.phone.setText(appointmentRequest.getPatientPhone());
        holder.datetime.setText(appointmentRequest.getDateAndTime());
        holder.reject.setOnClickListener(v -> new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to reject the appointment request of "+appointmentRequest.getName()+" for "+appointmentRequest.getDateAndTime()+"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Rejecting...");
                        progressDialog.show();

                        WriteBatch batch = firestore.batch();

                        DocumentReference patientRef = firestore.collection("PatientAppointments").document(appointmentRequest.getPatientAppointKey());
                        batch.delete(patientRef);

                        DocumentReference doctorRef = firestore.collection("DoctorAppointments").document(appointmentRequest.getDoctorAppointKey());
                        batch.delete(doctorRef);
                        batch.commit().addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                                appointmentRequestList.remove(appointmentRequest);
                                notifyItemRemoved(index);
                            }
                            else{
                                ReusableFunctionsAndObjects.showMessageAlert(context, "Update Error", task.getException().getMessage(), "Close",(byte)0);
                            }
                        });
                    }
                }).setNegativeButton("No",null).show());

        holder.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to confirm the appointment request of "+appointmentRequest.getName()+" for "+appointmentRequest.getDateAndTime()+"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Confirming...");
                                progressDialog.show();

                                WriteBatch batch = firestore.batch();

// Set the value of 'status' to confirmed
                                DocumentReference patientRef = firestore.collection("PatientAppointments").document(appointmentRequest.getPatientAppointKey());
                                batch.update(patientRef, "status", "confirmed");

                                DocumentReference doctorRef = firestore.collection("DoctorAppointments").document(appointmentRequest.getDoctorAppointKey());
                                batch.update(doctorRef, "status", "confirmed");

// Commit the batch
                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if(task.isSuccessful()){
                                            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                                            appointmentRequestList.remove(appointmentRequest);
                                            notifyItemRemoved(index);
                                        }
                                        else{
                                            ReusableFunctionsAndObjects.showMessageAlert(context, "Update Error", task.getException().getMessage(), "Close",(byte)0);
                                        }
                                    }
                                });
                            }
                        }).setNegativeButton("No",null).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentRequestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,email,phone,datetime;
        AppCompatButton confirm,reject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.patient_name);
            email=itemView.findViewById(R.id.email);
            phone=itemView.findViewById(R.id.phone);
            datetime=itemView.findViewById(R.id.date_time);
            confirm=itemView.findViewById(R.id.confirm);
            reject=itemView.findViewById(R.id.reject);
        }
    }
}
