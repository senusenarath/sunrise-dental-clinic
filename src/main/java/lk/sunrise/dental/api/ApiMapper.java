package lk.sunrise.dental.api;

import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * ApiMapper.java
 * Converts domain model objects to JSON for the REST API layer
 *
 * Keeps JSON field construction out of the servlets, and handles
 * nulls safely (org.json throws on a raw null value).
 * Package : lk.sunrise.dental.api
 * ================================================================
 */
public final class ApiMapper {

    private ApiMapper() {}

    private static Object orNull(Object value) {
        return value == null ? JSONObject.NULL : value;
    }

    public static JSONObject toJson(Patient p) {
        JSONObject json = new JSONObject();
        json.put("id", p.getId());
        json.put("patientCode", orNull(p.getPatientCode()));
        json.put("fullName", orNull(p.getFullName()));
        json.put("dateOfBirth", orNull(p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null));
        json.put("age", p.getAge());
        json.put("gender", orNull(p.getGender()));
        json.put("address", orNull(p.getAddress()));
        json.put("contact", orNull(p.getContact()));
        json.put("email", orNull(p.getEmail()));
        json.put("bloodType", orNull(p.getBloodType()));
        json.put("allergies", orNull(p.getAllergies()));
        json.put("medicalNotes", orNull(p.getMedicalNotes()));
        json.put("isActive", p.isActive());
        return json;
    }

    public static JSONArray patientsToJsonArray(List<Patient> patients) {
        JSONArray array = new JSONArray();
        for (Patient p : patients) {
            array.put(toJson(p));
        }
        return array;
    }

    public static JSONObject toJson(Appointment a) {
        JSONObject json = new JSONObject();
        json.put("id", a.getId());
        json.put("aptCode", orNull(a.getAptCode()));
        json.put("patientId", a.getPatientId());
        json.put("patientName", orNull(a.getPatientName()));
        json.put("patientContact", orNull(a.getPatientContact()));
        json.put("dentistId", a.getDentistId());
        json.put("dentistName", orNull(a.getDentistName()));
        json.put("treatmentId", a.getTreatmentId());
        json.put("treatmentName", orNull(a.getTreatmentName()));
        json.put("aptDate", orNull(a.getAptDate() != null ? a.getAptDate().toString() : null));
        json.put("aptTime", orNull(a.getAptTime() != null ? a.getAptTime().toString() : null));
        json.put("status", orNull(a.getStatus()));
        json.put("notes", orNull(a.getNotes()));
        json.put("treatmentCost", a.getTreatmentCost());
        json.put("consultFee", a.getConsultFee());
        json.put("totalCost", a.getTotalCost());
        json.put("hasBill", a.isHasBill());
        return json;
    }

    public static JSONArray appointmentsToJsonArray(List<Appointment> appointments) {
        JSONArray array = new JSONArray();
        for (Appointment a : appointments) {
            array.put(toJson(a));
        }
        return array;
    }

    public static JSONObject toJson(Bill b) {
        JSONObject json = new JSONObject();
        json.put("id", b.getId());
        json.put("billCode", orNull(b.getBillCode()));
        json.put("appointmentId", b.getAppointmentId());
        json.put("aptCode", orNull(b.getAptCode()));
        json.put("patientName", orNull(b.getPatientName()));
        json.put("dentistName", orNull(b.getDentistName()));
        json.put("treatmentName", orNull(b.getTreatmentName()));
        json.put("treatmentFee", b.getTreatmentFee());
        json.put("consultFee", b.getConsultFee());
        json.put("discount", b.getDiscount());
        json.put("totalAmount", b.getTotalAmount());
        json.put("paymentMethod", orNull(b.getPaymentMethod()));
        json.put("status", orNull(b.getStatus()));
        return json;
    }

    public static JSONArray toJsonArray(List<Bill> bills) {
        JSONArray array = new JSONArray();
        for (Bill b : bills) array.put(toJson(b));
        return array;
    }

    public static JSONArray toJsonArrayFromMaps(List<Map<String, Object>> maps) {
        JSONArray array = new JSONArray();
        for (Map<String, Object> m : maps) array.put(new JSONObject(m));
        return array;
    }
}
