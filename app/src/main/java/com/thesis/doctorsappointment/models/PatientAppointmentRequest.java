package com.thesis.doctorsappointment.models;

public class PatientAppointmentRequest extends Appointment{
    private String Address,City,DateAndTime,DocID,DoctorAppointKey,Name,PatientAppointKey,Specialization, status, patientUserKey;

    public PatientAppointmentRequest() {
    }

    public PatientAppointmentRequest(String address, String city, String dateAndTime, String docID, String doctorAppointKey, String name, String patientAppointKey, String specialization, String status, String patientUserKey) {
        Address = address;
        City = city;
        DateAndTime = dateAndTime;
        DocID = docID;
        DoctorAppointKey = doctorAppointKey;
        Name = name;
        PatientAppointKey = patientAppointKey;
        Specialization = specialization;
        this.status = status;
        this.patientUserKey = patientUserKey;
    }



    public String getPatientUserKey() {
        return patientUserKey;
    }

    public void setPatientUserKey(String patientUserKey) {
        this.patientUserKey = patientUserKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getDateAndTime() {
        return DateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        DateAndTime = dateAndTime;
    }

    public String getDocID() {
        return DocID;
    }

    public void setDocID(String docID) {
        DocID = docID;
    }

    public String getDoctorAppointKey() {
        return DoctorAppointKey;
    }

    public void setDoctorAppointKey(String doctorAppointKey) {
        DoctorAppointKey = doctorAppointKey;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPatientAppointKey() {
        return PatientAppointKey;
    }

    public void setPatientAppointKey(String patientAppointKey) {
        PatientAppointKey = patientAppointKey;
    }

    public String getSpecialization() {
        return Specialization;
    }

    public void setSpecialization(String specialization) {
        Specialization = specialization;
    }
}
