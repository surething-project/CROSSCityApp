package pt.ulisboa.tecnico.cross.account.model;

public class LoggedInUser {

  private final String username;
  private final String sessionId;

  public LoggedInUser(String username, String sessionId) {
    this.username = username;
    this.sessionId = sessionId;
  }

  public String getUsername() {
    return username;
  }

  public String getSessionId() {
    return sessionId;
  }
}
