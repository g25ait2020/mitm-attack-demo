package com.mitm.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HealthRecord class representing patient health information
 * This sensitive data will be intercepted in MITM attacks
 */
public class HealthRecord {
    private String patientId;
    private String patientName;
    private int age;
    private String gender;
    private String adhar;
    private String bloodType;
    private String medicalCondition;
    private String medications;
    private String allergies;
    private String symptoms;
    private String doctorNotes;
    private String timestamp;

    public HealthRecord() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public HealthRecord(String patientName, int age, String gender, String adhar,
                       String bloodType, String medicalCondition, String medications,
                       String allergies, String symptoms) {
        this();
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.adhar = adhar;
        this.bloodType = bloodType;
        this.medicalCondition = medicalCondition;
        this.medications = medications;
        this.allergies = allergies;
        this.symptoms = symptoms;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAdhar() {
        return adhar;
    }

    public void setAdhar(String adhar) {
        this.adhar = adhar;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public void setMedicalCondition(String medicalCondition) {
        this.medicalCondition = medicalCondition;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Convert to JSON-like string for transmission
     */
    public String toDataString() {
        return String.format(
            "PATIENT_NAME:%s|AGE:%d|GENDER:%s|ADHAR:%s|BLOOD_TYPE:%s|CONDITION:%s|MEDICATIONS:%s|ALLERGIES:%s|SYMPTOMS:%s",
            patientName, age, gender, adhar, bloodType, medicalCondition, medications, allergies, symptoms
        );
    }

    /**
     * Parse from data string
     */
    public static HealthRecord fromDataString(String data) {
        HealthRecord record = new HealthRecord();
        String[] parts = data.split("\\|");
        
        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                
                switch (key) {
                    case "PATIENT_NAME":
                        record.setPatientName(value);
                        break;
                    case "AGE":
                        record.setAge(Integer.parseInt(value));
                        break;
                    case "GENDER":
                        record.setGender(value);
                        break;
                    case "ADHAR":
                        record.setAdhar(value);
                        break;
                    case "BLOOD_TYPE":
                        record.setBloodType(value);
                        break;
                    case "CONDITION":
                        record.setMedicalCondition(value);
                        break;
                    case "MEDICATIONS":
                        record.setMedications(value);
                        break;
                    case "ALLERGIES":
                        record.setAllergies(value);
                        break;
                    case "SYMPTOMS":
                        record.setSymptoms(value);
                        break;
                }
            }
        }
        
        return record;
    }

    @Override
    public String toString() {
        return "HealthRecord{" +
                "patientId='" + patientId + '\'' +
                ", patientName='" + patientName + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", adhar='" + adhar + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", medicalCondition='" + medicalCondition + '\'' +
                ", medications='" + medications + '\'' +
                ", allergies='" + allergies + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    /**
     * Get formatted display string for GUI
     */
    public String toDisplayString() {
        return String.format(
            "Patient: %s (Age: %d)\n" +
            "Gender: %s\n" +
            "Adhar Number: %s\n" +
            "Blood Type: %s\n" +
            "Medical Condition: %s\n" +
            "Current Medications: %s\n" +
            "Allergies: %s\n" +
            "Symptoms: %s\n" +
            "Recorded: %s",
            patientName, age, gender, adhar, bloodType, medicalCondition,
            medications, allergies, symptoms, timestamp
        );
    }
}