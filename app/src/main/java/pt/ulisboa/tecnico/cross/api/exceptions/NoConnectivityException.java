package pt.ulisboa.tecnico.cross.api.exceptions;

import java.io.IOException;

public class NoConnectivityException extends IOException {

  public NoConnectivityException() {
    super("No Internet access.");
  }
}
