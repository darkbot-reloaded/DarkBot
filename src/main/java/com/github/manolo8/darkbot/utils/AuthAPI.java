package com.github.manolo8.darkbot.utils;

public interface AuthAPI {

    static AuthAPI getInstance() {
        // To run in dev mode, comment discord utils and use an AuthAPIImpl.
        return DiscordUtils.get();
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

    class AuthAPIImpl implements AuthAPI {
        public void setupAuth() {}
        public boolean isAuthenticated() {
            return false;
        }
        public boolean isDonor() {
            return false;
        }
        public boolean requireDonor() {
            return false;
        }
    }

}
