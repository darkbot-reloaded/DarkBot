package com.github.manolo8.darkbot.utils.login;

import com.github.manolo8.darkbot.config.utils.ByteArrayToBase64TypeAdapter;
import com.github.manolo8.darkbot.utils.Encryption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores user credentials encrypted on disk, using a password+salt
 */
public class Credentials {
    private static transient final Type USER_LIST = new TypeToken<List<Credentials.User>>(){}.getType();
    public static transient final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
            .create();

    private byte[] salt;
    private String data; // Encrypted data
    @Getter
    private transient List<Credentials.User> users = new ArrayList<>(); // Non-encrypted data, not stored

    public static Credentials create() {
        Credentials c = new Credentials();
        c.salt = new byte[32];
        new Random().nextBytes(c.salt);
        return c;
    }

    public boolean isEmpty() {
        return data == null;
    }

    public void decrypt(char[] password) throws GeneralSecurityException {
        users = data == null ? new ArrayList<>() :
                GSON.fromJson(Encryption.decrypt(data, Encryption.createSecretKey(password, salt)), USER_LIST);
    }

    public void encrypt(char[] password) throws GeneralSecurityException {
        data = users.isEmpty() ? null :
                Encryption.encrypt(GSON.toJson(users), Encryption.createSecretKey(password, salt));
    }

    public static class User {
        public String u, p, s, sv;

        public User() {}

        public User(String username, String password) {
            this.u = username;
            this.p = password;
        }

        public void setSid(String sid, String server) {
            this.s = sid;
            this.sv = server;
        }

        @Override
        public String toString() {
            return u;
        }
    }
}