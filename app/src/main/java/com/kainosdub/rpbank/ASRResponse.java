package com.kainosdub.rpbank;

public class ASRResponse {
    private String transcript;
    private String time_taken;
    private String vtt;
    private String status;

    public String getTime_taken() {
        return time_taken;
    }

    public void setTime_taken(String time_taken) {
        this.time_taken = time_taken;
    }

    public String getVtt() {
        return vtt;
    }

    public void setVtt(String vtt) {
        this.vtt = vtt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
}
