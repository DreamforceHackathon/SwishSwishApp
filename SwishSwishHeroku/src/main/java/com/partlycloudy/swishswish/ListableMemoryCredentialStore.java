package com.partlycloudy.swishswish;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ListableMemoryCredentialStore implements CredentialStore {

  /**
   * Lock on access to the store.
   */
  private final Lock lock = new ReentrantLock();

  /**
   * Store of memory persisted credentials, indexed by userId.
   */
  private final Map<String, MemoryPersistedCredential> store =
      new HashMap<String, MemoryPersistedCredential>();

  public void store(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item == null) {
        item = new MemoryPersistedCredential();
        store.put(userId, item);
      }
      item.store(credential);
    } finally {
      lock.unlock();
    }
  }

  public void delete(String userId, Credential credential) {
    lock.lock();
    try {
      store.remove(userId);
    } finally {
      lock.unlock();
    }
  }

  public boolean load(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item != null) {
        item.load(credential);
      }
      return item != null;
    } finally {
      lock.unlock();
    }
  }

  public List<String> listAllUsers() {
    List<String> allUsers = new ArrayList<String>();
    // Is that a 47 character long generic for one line of behavior? Yes, yes it is.
    for (Iterator<Map.Entry<String, MemoryPersistedCredential>> iterator = store.entrySet()
        .iterator();
         iterator.hasNext(); ) {
      allUsers.add(iterator.next().getKey());
    }
    return allUsers;
  }

  class MemoryPersistedCredential {

    /**
     * Access token or {@code null} for none.
     */
    private String accessToken;

    /**
     * Refresh token {@code null} for none.
     */
    private String refreshToken;

    /**
     * Expiration time in milliseconds {@code null} for none.
     */
    private Long expirationTimeMillis;

    /**
     * Store information from the credential.
     *
     * @param credential credential whose {@link Credential#getAccessToken access token},
     *                   {@link Credential#getRefreshToken refresh token}, and
     *                   {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
     */
    void store(Credential credential) {
      accessToken = credential.getAccessToken();
      refreshToken = credential.getRefreshToken();
      expirationTimeMillis = credential.getExpirationTimeMilliseconds();
    }

    /**
     * Load information into the credential.
     *
     * @param credential credential whose {@link Credential#setAccessToken access token},
     *                   {@link Credential#setRefreshToken refresh token}, and
     *                   {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
     *                   credential already exists in storage
     */
    void load(Credential credential) {
      credential.setAccessToken(accessToken);
      credential.setRefreshToken(refreshToken);
      credential.setExpirationTimeMilliseconds(expirationTimeMillis);
    }
  }
}