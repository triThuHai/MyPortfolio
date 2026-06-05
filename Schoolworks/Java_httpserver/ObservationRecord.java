package com.o3.server;

import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class ObservationRecord {

    private static ArrayList<ObservationRecord> arrayList = new ArrayList<>();
    private String targetBodyName;
    private String centerBodyName;
    private Long epoch;
    private double semiMajorAxisAu;
    private double eccentricity;
    private double inclinationDeg;
    private double longitudeAscendingNodeDeg;
    private double argumentOfPeriapsisDeg;
    private double meanAnomalyDeg;
    private double [] positionAu;
    private double [] velocityAuPerDay;
    private Long recordTimeReceived;
    private String recordOwner;
    private String id;
    private String recordPayload;
    private boolean hasOrbitalElements;
    private boolean hasStateVector;
    private boolean hasMetaData;
    private String orbitalElementsJson;
    private String stateVectorJson;
    private JSONArray observatory;
    private static final DateTimeFormatter utcFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneId.of("UTC"));

    public String getTargetBodyName() { 
        return this.targetBodyName; 
    }

    public void setTargetBodyName(String targetBodyName) { 
        this.targetBodyName = targetBodyName; 
    }

    public String getCenterBodyName() { 
        return this.centerBodyName; 
    }

    public void setCenterBodyName(String centerBodyName) { 
        this.centerBodyName = centerBodyName; 
    }

    public Long getEpoch() { 
        return this.epoch;  
    }

    public void setEpoch(Long epoch) { 
        this.epoch = epoch; 
    }

    public String getEpochAsIsoUtcString() {
        return utcFormat.format(Instant.ofEpochMilli(this.epoch));
    }

    public void setEpochFromIsoUtcString(String isoUtcString) throws DateTimeParseException {
        this.epoch = Instant.parse(isoUtcString).toEpochMilli();
    }

    public double getSemiMajorAxisAu () { 
        return this.semiMajorAxisAu;
    }

    public void setSemiMajorAxisAu(double semiMajorAxisAu) { 
        this.semiMajorAxisAu = semiMajorAxisAu; }

    public double getEccentricity() { 
        return this.eccentricity; 
    }

    public void setEccentricity(double eccentricity) { 
        this.eccentricity = eccentricity; 
    }

    public double getInclinationDeg() { 
        return this.inclinationDeg; 
    }
    
    public void setInclinationDeg(double inclinationDeg) { 
        this.inclinationDeg = inclinationDeg; 
    }

    public double getLongitudeAscendingNodeDeg() { 
        return this.longitudeAscendingNodeDeg; 
    }

    public void setLongitudeAscendingNodeDeg(double longitudeAscendingNodeDeg) { 
        this.longitudeAscendingNodeDeg = longitudeAscendingNodeDeg; 
    }

    public double getArgumentOfPeriapsisDeg() { 
        return this.argumentOfPeriapsisDeg; 
    }

    public void setArgumentOfPeriapsisDeg(double argumentOfPeriapsisDeg) { 
        this.argumentOfPeriapsisDeg = argumentOfPeriapsisDeg; 
    }

    public double getMeanAnomalyDeg() { 
        return this.meanAnomalyDeg; 
    }

    public void setMeanAnomalyDeg(double meanAnomalyDeg) { 
        this.meanAnomalyDeg = meanAnomalyDeg; 
    }

    public double [] getPositionAu() { 
        return this.positionAu; 
    }

    public void setPositionAu(double[] positionAu) { 
        this.positionAu = positionAu; 
    }

    public double [] getVelocityAuPerDay() { 
        return this.velocityAuPerDay; 
    }

    public void setVelocityAuPerDay(double[] velocityAuPerDay) { 
        this.velocityAuPerDay = velocityAuPerDay; 
    }

    public Long getRecordTime() { 
        return this.recordTimeReceived; 
    }

    public void setRecordTime(Long recordTimeReceived) { 
        this.recordTimeReceived = recordTimeReceived; 
        if (recordTimeReceived != null) {
            this.hasMetaData = true;
        }
    }

    public String getRecordTimeAsIsoUtcString() {
        return utcFormat.format(Instant.ofEpochMilli(this.recordTimeReceived));
    }

    public void setRecordTimeReceivedNowUtc() throws DateTimeParseException {
        this.recordTimeReceived = Instant.now().toEpochMilli();
    }

    public void setRecordOwner(String recordOwner) { 
        this.recordOwner = recordOwner; 
        if (recordOwner != null) {
            this.hasMetaData = true;
        }
    }

    public String getRecordOwner() { 
        return this.recordOwner; 
    }

    public void setId(String id) { 
        this.id = id; 
        if (id != null) {
            this.hasMetaData = true;
        }
    }

    public String getId() { 
        return this.id; 
    } 

    public void setRecordPayload(String recordPayload) { 
        this.recordPayload = recordPayload; 
        if (recordPayload != null) {
            this.hasMetaData = true;
        }
    }

    public String getRecordPayload() { 
        return this.recordPayload;
    }

    public String getOrbitalElementsJson() { 
        return this.orbitalElementsJson; 
    }

    public void setOrbitalElements(JSONObject orbitalElementsJson) {
        if (orbitalElementsJson == null || orbitalElementsJson.isEmpty()) {
            this.orbitalElementsJson = null;
            this.hasOrbitalElements = false;
            return;
        }
        this.hasOrbitalElements = true;
        this.orbitalElementsJson = orbitalElementsJson.toString();

        // Populate numeric fields for toJson() when rebuilt from DB
        this.semiMajorAxisAu = orbitalElementsJson.optDouble("semi_major_axis_au", this.semiMajorAxisAu);
        this.eccentricity = orbitalElementsJson.optDouble("eccentricity", this.eccentricity);
        this.inclinationDeg = orbitalElementsJson.optDouble("inclination_deg", this.inclinationDeg);
        this.longitudeAscendingNodeDeg = orbitalElementsJson.optDouble("longitude_ascending_node_deg", this.longitudeAscendingNodeDeg);
        this.argumentOfPeriapsisDeg = orbitalElementsJson.optDouble("argument_of_periapsis_deg", this.argumentOfPeriapsisDeg);
        this.meanAnomalyDeg = orbitalElementsJson.optDouble("mean_anomaly_deg", this.meanAnomalyDeg);
    }

    public String getStateVectorJson() { 
        return this.stateVectorJson; 
    }

    public void setStateVector(JSONObject stateVectorJson) {
        if (stateVectorJson == null || stateVectorJson.isEmpty()) {
            this.stateVectorJson = null;
            this.hasStateVector = false;
            return;
        }
        this.hasStateVector = true;
        this.stateVectorJson = stateVectorJson.toString();

        // Populate arrays for toJson() when loaded from DB
        JSONArray pos = stateVectorJson.optJSONArray("position_au");
        if (pos != null) {
            double[] p = new double[pos.length()];
            for (int i = 0; i < pos.length(); i++) p[i] = pos.optDouble(i);
            this.positionAu = p;
        }

        JSONArray vel = stateVectorJson.optJSONArray("velocity_au_per_day");
        if (vel != null) {
            double[] v = new double[vel.length()];
            for (int i = 0; i < vel.length(); i++) v[i] = vel.optDouble(i);
            this.velocityAuPerDay = v;
        }
    }

    public JSONArray getObservatory() { 
        return this.observatory; 
    }

    public void setObservatory(JSONArray observatory) { 
        this.observatory = observatory; 
    }

    public ObservationRecord recordedFromJson(JSONObject object) throws JSONException {
        this.setTargetBodyName(object.getString("target_body_name"));
        this.setCenterBodyName(object.getString("center_body_name"));
        this.setEpochFromIsoUtcString(object.getString("epoch"));

        JSONObject orbitalElements = object.optJSONObject("orbital_elements");
        if (orbitalElements != null && !orbitalElements.isEmpty()) {
            this.hasOrbitalElements = true;
            this.setSemiMajorAxisAu(orbitalElements.getDouble("semi_major_axis_au"));
            this.setEccentricity(orbitalElements.getDouble("eccentricity"));
            this.setInclinationDeg(orbitalElements.getDouble("inclination_deg"));
            this.setLongitudeAscendingNodeDeg(orbitalElements.getDouble("longitude_ascending_node_deg"));
            this.setArgumentOfPeriapsisDeg(orbitalElements.getDouble("argument_of_periapsis_deg"));
            this.setMeanAnomalyDeg(orbitalElements.getDouble("mean_anomaly_deg"));
            this.setOrbitalElements(orbitalElements);
        }

        JSONObject stateVector = object.optJSONObject("state_vector");
        if (stateVector != null && !stateVector.isEmpty()) {
            this.hasStateVector = true;
            JSONArray stateVectorArray = stateVector.getJSONArray("position_au");
            double [] positionAu = new double[stateVectorArray.length()];
            for (int i = 0; i < stateVectorArray.length(); i ++) {
                positionAu[i] = stateVectorArray.getDouble(i);
            }
            this.setPositionAu(positionAu);

            JSONArray velocityArray = stateVector.getJSONArray("velocity_au_per_day");
            double [] velocityAuPerDay = new double[velocityArray.length()];
            for (int i = 0; i < velocityArray.length(); i ++) {
                velocityAuPerDay[i] = velocityArray.getDouble(i);
            }
            this.setVelocityAuPerDay(velocityAuPerDay);
            
            this.setStateVector(stateVector);
        }

        JSONObject metaData = object.optJSONObject("metadata");
        if (metaData != null && !metaData.isEmpty()) {
            this.hasMetaData = true;
            this.recordOwner = metaData.optString("record_owner");
            this.id = metaData.optString("id");
            this.setRecordPayload(metaData.optString("record_payload"));
            if (metaData.has("observatory")) {
                this.observatory = metaData.getJSONArray("observatory");
            }  
        }          
        return this;
    }


    public void addToList(ObservationRecord record) {
        arrayList.add(record);
    }

    public ArrayList<ObservationRecord> getList() {
        return arrayList;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("target_body_name", getTargetBodyName());
        object.put("center_body_name", getCenterBodyName());
        object.put("epoch", this.getEpochAsIsoUtcString());

        if (this.hasOrbitalElements) {
            JSONObject orbitalElements = new JSONObject();
            orbitalElements.put("semi_major_axis_au", getSemiMajorAxisAu());
            orbitalElements.put("eccentricity", getEccentricity());
            orbitalElements.put("inclination_deg", getInclinationDeg());
            orbitalElements.put("longitude_ascending_node_deg", getLongitudeAscendingNodeDeg());
            orbitalElements.put("argument_of_periapsis_deg", getArgumentOfPeriapsisDeg());
            orbitalElements.put("mean_anomaly_deg", getMeanAnomalyDeg());
            object.put("orbital_elements", orbitalElements);
        }

        if (this.hasStateVector) {
            double[] posArray = getPositionAu();
            double[] velArray = getVelocityAuPerDay();

            if (posArray != null && velArray != null) {
                JSONObject stateVector = new JSONObject();

                JSONArray positionArray = new JSONArray();
                for (double pos : posArray) {
                    positionArray.put(pos);
                }
                stateVector.put("position_au", positionArray);

                JSONArray velocityArray = new JSONArray();
                for (double vel : velArray) {
                    velocityArray.put(vel);
                }
                stateVector.put("velocity_au_per_day", velocityArray);

                object.put("state_vector", stateVector);
            }
        }
        
        if (this.hasMetaData) {
            JSONObject metaData = new JSONObject();
            metaData.put("record_time_received", getRecordTimeAsIsoUtcString());
            metaData.put("record_owner", getRecordOwner());
            metaData.put("id", getId());
            metaData.put("record_payload", getRecordPayload());

            JSONArray obsArray = getObservatory();
            if (obsArray != null) {
            metaData.put("observatory", getObservatory());
            }

            object.put("metadata", metaData);
        }

        return object;  
    }

    public JSONArray toJSONArray(JSONObject object) throws JSONException {
        JSONArray responseMessages = new JSONArray();
        responseMessages.put(object);
        return responseMessages;
    }
}
