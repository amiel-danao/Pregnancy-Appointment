package com.thesis.doctorsappointment.PatientFragments;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.doctorsappointment.Adapters.MyAppointmentAdapter;
import com.thesis.doctorsappointment.Adapters.PendingAppointmentAdapter;
import com.thesis.doctorsappointment.DataRetrievalClass.PatientAppointmentRequest;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.ReusableFunctionsAndObjects;
import java.util.ArrayList;
import java.util.List;

public class MyAppointmentFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private List<PatientAppointmentRequest> appointmentRequestList;
    private MyAppointmentAdapter adapter;
    private SearchView searchView;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
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
        firestore.collection("PatientAppointments")
                .whereEqualTo("status", "confirmed")
                .whereEqualTo("patientUserKey", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            appointmentRequestList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                appointmentRequestList.add(document.toObject(PatientAppointmentRequest.class));
                            }
                            adapter=new MyAppointmentAdapter(getContext(),appointmentRequestList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            ReusableFunctionsAndObjects.showMessageAlert(getContext(),"Error",task.getException().getMessage(),"Ok",(byte)0);
                        }
                        progressDialog.dismiss();
                    }
                });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.my_search_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.search_bar);
        searchView=(SearchView)menuItem.getActionView();
        searchView.setQueryHint("Search Doctors");
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
        List<PatientAppointmentRequest> filteredlist=new ArrayList<>();
        for(PatientAppointmentRequest req: appointmentRequestList){
            if(req.getName().toLowerCase().contains(s.toLowerCase())){
                filteredlist.add(req);
            }
        }
        adapter=new MyAppointmentAdapter(getContext(),filteredlist);
        recyclerView.setAdapter(adapter);
    }

}
