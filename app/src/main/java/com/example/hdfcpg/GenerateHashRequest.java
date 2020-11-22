package com.example.hdfcpg;

import com.google.gson.annotations.SerializedName;

public class GenerateHashRequest {
    @SerializedName("hash_string")
    public String hashString;

    public GenerateHashRequest(String hashString) {
        this.hashString = hashString;
    }

    public GenerateHashRequest() {
    }

    public String getHashString() {
        return hashString;
    }

    public void setHashString(String hashString) {
        this.hashString = hashString;
    }
}

