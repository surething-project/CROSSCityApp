package pt.ulisboa.tecnico.cross.wifi;

import android.net.wifi.ScanResult;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

public class WiFiScanResult {

  @NonNull public final String bssid;
  @NonNull public final String ssid; // Could be an OTP issued by custom trusted infrastructure
  public final long sightingMillis;

  public WiFiScanResult(ScanResult scanResult) {
    this.bssid = scanResult.BSSID;
    this.ssid = scanResult.SSID;
    this.sightingMillis = System.currentTimeMillis();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WiFiScanResult that = (WiFiScanResult) o;
    return bssid.equals(that.bssid) && ssid.equals(that.ssid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bssid, ssid);
  }

  @NonNull
  @Override
  public String toString() {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("BSSID", bssid);
      jsonObject.put("SSID", ssid);
      jsonObject.put("SightingMillis", new Date(sightingMillis));
      return jsonObject.toString(2);
    } catch (JSONException e) {
      return e.getMessage();
    }
  }
}
