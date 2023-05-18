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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.thesis.doctorsappointment.models.PatientAppointmentRequest;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;
import java.util.List;

public class MyAppointmentAdapter extends RecyclerView.Adapter<MyAppointmentAdapter.ViewHolder> {

    private final FirebaseFirestore firestore;
    private Context context;
    private List<PatientAppointmentRequest> appointmentRequestList;
    private ProgressDialog progressDialog;
    public MyAppointmentAdapter(Context context, List<PatientAppointmentRequest> appointmentRequestList) {
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
        View view= LayoutInflater.from(context).inflate(R.layout.item_patient_apt,parent,false);
        return new MyAppointmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatientAppointmentRequest request=appointmentRequestList.get(position);
        final int index = holder.getAdapterPosition();
        holder.doc_name.setText(request.getName());
        holder.spl.setText("Specialization: "+request.getSpecialization());
        holder.appointmentDate.setText(request.getDateAndTime());

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setCancelable(false).setMessage("Are you sure you want to cancel the appointment of Dr. "+request.getName()+" for "+request.getDateAndTime()+"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Cancelling...");
                        progressDialog.show();

                        WriteBatch batch = firestore.batch();

                        DocumentReference patientRef = firestore.collection("PatientAppointments").document(request.getPatientAppointKey());
                        batch.delete(patientRef);

                        DocumentReference doctorRef = firestore.collection("DoctorAppointments").document(request.getDoctorAppointKey());
                        batch.delete(doctorRef);
                        batch.commit().addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                                appointmentRequestList.remove(request);
                                notifyItemRemoved(index);
                            }
                            else{
                                ReusableFunctionsAndObjects.showMessageAlert(context, "Cancel Error", task.getException().getMessage(), "Close",(byte)0);
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
        private TextView doc_name,spl, appointmentDate;
        AppCompatButton cancel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doc_name=itemView.findViewById(R.id.doc_name);
            cancel=itemView.findViewById(R.id.cancel);
            spl=itemView.findViewById(R.id.spl);
            appointmentDate=itemView.findViewById(R.id.appointmentDate);
        }
    }
}
