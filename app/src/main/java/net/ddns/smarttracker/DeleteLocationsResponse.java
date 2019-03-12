package net.ddns.smarttracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteLocationsResponse {

  @SerializedName("status")
  @Expose
  private String status;

  @SerializedName("message")
  @Expose
  private String message;

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
