package com.teamhns.hnsmobile.Model;

public class ReportPost {

    private String reportKey;
    private String reporterID;
    private String whyReport;

    public ReportPost(String reportKey, String reporterID, String whyReport) {
        this.reportKey = reportKey;
        this.reporterID = reporterID;
        this.whyReport = whyReport;
    }

    public String getWhyReport() {
        return whyReport;
    }

    public void setWhyReport(String whyReport) {
        this.whyReport = whyReport;
    }

    public String getReportKey() {
        return reportKey;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey;
    }

    public String getReporterID() {
        return reporterID;
    }

    public void setReporterID(String reporterID) {
        this.reporterID = reporterID;
    }
}
