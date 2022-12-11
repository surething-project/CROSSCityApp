package pt.ulisboa.tecnico.cross.model;

import androidx.room.TypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class Converters {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @TypeConverter
  public static String fromMap(Map<String, String> value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting map '%s' to string.", value);
      return null;
    }
  }

  @TypeConverter
  public static Map<String, String> toMap(String value) {
    try {
      return objectMapper.readValue(value, new TypeReference<Map<String, String>>() {});
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting string '%s' to map.", value);
      return null;
    }
  }

  @TypeConverter
  public static String fromList(List<String> value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting list '%s' to string.", value);
      return null;
    }
  }

  @TypeConverter
  public static List<String> toList(String value) {
    try {
      return objectMapper.readValue(value, new TypeReference<List<String>>() {});
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting string '%s' to list.", value);
      return null;
    }
  }

  @TypeConverter
  public static String fromDoubleArray(double[] value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting double[] '%s' to string.", Arrays.toString(value));
      return null;
    }
  }

  @TypeConverter
  public static double[] toDoubleArray(String value) {
    try {
      return objectMapper.readValue(value, new TypeReference<double[]>() {});
    } catch (JsonProcessingException e) {
      Timber.e(e, "Error converting string '%s' to double[].", value);
      return null;
    }
  }
}
