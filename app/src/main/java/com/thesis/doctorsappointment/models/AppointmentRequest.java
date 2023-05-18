package com.thesis.doctorsappointment.models;

public class AppointmentRequest extends Appointment{
    private String DateAndTime,DoctorAppointKey,Name,PatientAppointKey,PatientEmail,PatientID,PatientPhone, status, doctorId;

    public AppointmentRequest() {
    }

    public AppointmentRequest(String dateAndTime, String doctorAppointKey, String name, String patientAppointKey, String patientEmail, String patientID, String patientPhone, String status, String doctorId) {
        DateAndTime = dateAndTime;
        DoctorAppointKey = doctorAppointKey;
        Name = name;
        PatientAppointKey = patientAppointKey;
        PatientEmail = patientEmail;
        PatientID = patientID;
        PatientPhone = patientPhone;
        this.status = status;
        this.doctorId = doctorId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateAndTime() {
        return DateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        DateAndTime = dateAndTime;
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

    public String getPatientEmail() {
        return PatientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        PatientEmail = patientEmail;
    }

    public String getPatientID() {
        return PatientID;
    }

    public void setPatientID(String patientID) {
        PatientID = patientID;
    }

    public String getPatientPhone() {
        return PatientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        PatientPhone = patientPhone;
    }
}
