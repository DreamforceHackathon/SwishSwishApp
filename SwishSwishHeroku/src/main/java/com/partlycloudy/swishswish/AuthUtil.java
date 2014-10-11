package com.partlycloudy.swishswish;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthUtil {
  public static ListableMemoryCredentialStore store = new ListableMemoryCredentialStore();
  public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline "
      + "https://www.googleapis.com/auth/glass.location "
      + "https://www.googleapis.com/auth/glass.timeline "
      + "https://www.googleapis.com/auth/userinfo.profile "
      + "https://www.googleapis.com/auth/blogger";
  private static final Logger LOG = Logger.getLogger(AuthUtil.class.getSimpleName());

  /**
   * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
   */
  public static AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
    InputStream authPropertiesStream = AuthorizationCodeFlow.class.getResourceAsStream("/oauth.properties");
    Properties authProperties = new Properties();
    authProperties.load(authPropertiesStream);

    String clientId = authProperties.getProperty("client_id");
    String clientSecret = authProperties.getProperty("client_secret");

    return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),
        clientId, clientSecret, Collections.singleton(GLASS_SCOPE))
        .setCredentialStore(store).build();
  }

  /**
   * Get the current user's ID from the session
   *
   * @return string user id or null if no one is logged in
   */
  public static String getUserId(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return (String) session.getAttribute("userId");
  }

  public static void setUserId(HttpServletRequest request, String userId) {
    HttpSession session = request.getSession();
    session.setAttribute("userId", userId);
  }

  public static void clearUserId(HttpServletRequest request) throws IOException {
    // Delete the credential in the credential store
    String userId = getUserId(request);
    store.delete(userId, getCredential(userId));

    // Remove their ID from the local session
    request.getSession().removeAttribute("userId");
  }

  public static Credential getCredential(String userId) throws IOException {
    if (userId == null) {
      return null;
    } else {
      return AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    }
  }

  public static Credential getCredential(HttpServletRequest req) throws IOException {
    return AuthUtil.newAuthorizationCodeFlow().loadCredential(getUserId(req));
  }

  public static List<String> getAllUserIds() {
    return store.listAllUsers();
  }
}
