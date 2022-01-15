package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public interface AuthAPI extends eu.darkbot.api.managers.AuthAPI {

    Path VERIFIER_PATH = Paths.get("lib", "verifier.jar");
    AuthAPI INSTANCE = ReflectionUtils.createInstance("eu.darkbot.verifier.AuthAPIImpl", VERIFIER_PATH);

    static AuthAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Sets up initial auth. Some environments (like servers) may have
     * less user friction by not requiring discord authentication.
     */
    void setupAuth();

    /**
     * Returns if the user has been validly authenticated with discord
     * @return true if auth was performed & valid, false otherwise.
     */
    boolean isAuthenticated();

    /**
     * If the user didn't authenticate beforehand, it will prompt the user to authenticate.
     * @return true if the user is a donor in the official darkbot discord server, false otherwise.
     */
    boolean isDonor();

    /**
     * If the user didn't authenticate beforehand, it will prompt the user to authenticate.
     * Will prompt the user to join the discord & donate if he hasn't done so yet.
     * @return true if the user is a donor in the official darkbot discord server, false otherwise.
     */
    boolean requireDonor();

    /**
     * Returns a unique id for the player, the only guarantee is the same player will keep the same id
     * as long as the auth method is the same, and that no 2 players will share it.
     * @return A unique string representing this user, null if no auth was done.
     */
    @Nullable
    String getAuthId();

    /**
     * Checks if a jar file has been signed by an authorized key
     * @param jarFile The jar file to check
     * @throws IOException If the file can't be found or read.
     * @return true if signed & known signature, null if not signed, false if signed by untrusted key.
     */
    Boolean checkPluginJarSignature(JarFile jarFile) throws IOException;

}
