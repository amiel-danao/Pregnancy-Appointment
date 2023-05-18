package com.thesis.doctorsappointment.DoctorFragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.doctorsappointment.Adapters.AppointmentAdapter;
import com.thesis.doctorsappointment.DoctorMainActivity;
import com.thesis.doctorsappointment.models.AppointmentRequest;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;

import java.util.ArrayList;
import java.util.List;

public class AppointmentFragment extends Fragment {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private AppointmentAdapter appointmentAdapter;
    private List<AppointmentRequest> appointmentRequestList;
    private FirebaseFirestore firestore;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_common,container,false);
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentRequestList=new ArrayList<>();
        progressDialog= new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...\nPlease wait...");
        progressDialog.show();

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("DoctorAppointments")
                .whereEqualTo("status", "confirmed")
                .whereEqualTo("doctorId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            appointmentRequestList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                appointmentRequestList.add(document.toObject(AppointmentRequest.class));
                            }
                            appointmentAdapter=new AppointmentAdapter(getContext(),appointmentRequestList, ((DoctorMainActivity) getActivity()).getSupportFragmentManager());
                            recyclerView.setAdapter(appointmentAdapter);
                            progressDialog.dismiss();
                        } else {
                            ReusableFunctionsAndObjects.showMessageAlert(getContext(),"Error", task.getException().getMessage(),"Ok",(byte)0);
                        }
                        progressDialog.dismiss();
                    }
                });

        return  view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.my_search_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.search_bar);
        searchView=(SearchView)menuItem.getActionView();
        searchView.setQueryHint("Search Patients");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query!=null){
                    filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText!=null){
                    filter(newText);
                }
                return true;
            }
        });
    }
    private void filter(String s){
        List<AppointmentRequest> filteredlist=new ArrayList<>();
        for(AppointmentRequest symptoms:appointmentRequestList){
            if(symptoms.getName().toLowerCase().contains(s.toLowerCase())){
                filteredlist.add(symptoms);
            }
        }
        appointmentAdapter=new AppointmentAdapter(getContext(), filteredlist, ((DoctorMainActivity) getActivity()).getSupportFragmentManager());
        recyclerView.setAdapter(appointmentAdapter);
    }
}
