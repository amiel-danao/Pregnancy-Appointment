package com.thesis.doctorsappointment.DoctorFragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.thesis.doctorsappointment.R;
import com.thesis.doctorsappointment.databinding.FragmentAppointmentPreviewBinding;
import com.thesis.doctorsappointment.models.Appointment;
import com.thesis.doctorsappointment.models.AppointmentRequest;
import com.thesis.doctorsappointment.models.PatientAppointmentRequest;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppointmentPreviewFragment extends Fragment {

    private FragmentAppointmentPreviewBinding binding;
    private AppointmentRequest mViewModel;
    private View parent;
    private CircleImageView patient_image;
    private String imageUrl;

    public static AppointmentPreviewFragment newInstance(Appointment mViewModel, String imageUrl) {
        AppointmentPreviewFragment fragment = new AppointmentPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", mViewModel);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public AppointmentPreviewFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mViewModel = (AppointmentRequest) arguments.getSerializable("data");
            imageUrl = arguments.getString("imageUrl");
        }


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_appointment_preview, container, false);
        //here data must be an instance of the class MarsDataProvider

        binding.setModel(mViewModel);

        parent = binding.getRoot();
        patient_image = parent.findViewById(R.id.patient_image);

        Glide.with(requireContext()).load(imageUrl).into(patient_image);

        return parent;
    }


}