package pt.ulisboa.tecnico.cross.api.exceptions;

import java.io.IOException;

public class APIException extends IOException {

  public APIException(String errorMessage) {
    super(errorMessage);
  }
}
