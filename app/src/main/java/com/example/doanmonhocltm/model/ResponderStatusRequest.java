package com.example.doanmonhocltm.model;

public class ResponderStatusRequest {

    private Integer accidentId;
    private String unitId;

    public ResponderStatusRequest(Integer accidentId, String unitId, String status) {
        this.accidentId = accidentId;
        this.unitId = unitId;
        this.status = status;
    }

    private String status;

    public ResponderStatusRequest() {

    }

    public Integer getAccidentId() {
        return accidentId;
    }

    public void setAccidentId(Integer accidentId) {
        this.accidentId = accidentId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
