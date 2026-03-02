package com.servercontroller.app.config;

import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;

public class KeychainService {
    private static final String DOMAIN = "ServerController";

    public void saveApiKey(String serverId, String apiKey) {
        try (Keyring keyring = Keyring.create()) {
            keyring.setPassword(DOMAIN, serverId, apiKey);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to store API key", e);
        }
    }

    public String loadApiKey(String serverId) {
        try (Keyring keyring = Keyring.create()) {
            return keyring.getPassword(DOMAIN, serverId);
        } catch (PasswordAccessException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteApiKey(String serverId) {
        try (Keyring keyring = Keyring.create()) {
            keyring.deletePassword(DOMAIN, serverId);
        } catch (Exception ignored) {
        }
    }
}
