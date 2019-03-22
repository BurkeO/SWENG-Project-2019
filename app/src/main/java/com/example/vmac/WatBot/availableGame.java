package com.example.vmac.WatBot;

public class availableGame {

    private String id, status;


    public availableGame() {
    }

    public availableGame(String id, String status) {
        this.id = id;
        this.status = status;


    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
