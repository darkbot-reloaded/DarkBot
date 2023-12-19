package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.gui.utils.Strings;
import eu.darkbot.api.API;
import eu.darkbot.util.function.ThrowingFunction;
import lombok.Getter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class StartupParams implements API.Singleton {
    private static final String COMMAND_PREFIX = "-";

    public enum LaunchArg {
        /**
         * Auto-login without a login pop-up, requires a path to a properties file
         * with a username and either a password or a master-password.
         * Example usage: {@code -login C:\Users\Owner\login.properties}
         */
        LOGIN(AutoLoginProps::new),
        START, /** Auto-start the bot */
        NO_OP, /** Run the bot in no-op mode (no-op api) */
        CONFIG(s -> s), /** Start the bot with a specific config */
        HIDE, /** If the bot should hide api window on start */
        NO_WARN; /** Disable warnings about unsupported java version */

        private final ThrowingFunction<String, ?, Exception> parser;

        LaunchArg() {
            this(null);
        }

        LaunchArg(ThrowingFunction<String, ?, Exception> parser) {
            this.parser = parser;
        }

        public static LaunchArg of(String str) {
            while (str.startsWith(COMMAND_PREFIX)) str = str.substring(1);
            try {
                return LaunchArg.valueOf(str.toUpperCase(Locale.ROOT).replace("-", "_"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public Object parse(String param) {
            if (parser == null) return true;
            try {
                return parser.apply(param);
            } catch (Exception e) {
                System.err.println("Failed to parse parameter for argument: " + this);
                e.printStackTrace();
                return null;
            }
        }
    }

    public enum PropertyKey {
        USERNAME, PASSWORD, MASTER_PASSWORD, SERVER, SID, ALLOW_STORE_SID;
        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private final Map<LaunchArg, Object> startupParams = new HashMap<>();

    public StartupParams(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String strArgument = args[i];
            LaunchArg arg = LaunchArg.of(strArgument);
            if (arg == null) {
                System.err.println("Unknown startup argument: " + strArgument + " , ignoring argument.");
                continue;
            }
            if (arg.parser != null) i++;
            if (i >= args.length) {
                System.err.println("Missing required argument for " + strArgument);
                break;
            }
            startupParams.put(arg, arg.parse(args[i]));
        }
    }

    public AutoLoginProps getAutoLoginProps() {
        return (AutoLoginProps) startupParams.getOrDefault(LaunchArg.LOGIN, null);
    }

    /* Other params */
    public boolean has(LaunchArg arg) {
        return startupParams.containsKey(arg);
    }

    public boolean getAutoLogin() {
        return has(LaunchArg.LOGIN);
    }

    public boolean getAutoStart() {
        return has(LaunchArg.START);
    }

    public boolean useNoOp() {
        return has(LaunchArg.NO_OP);
    }

    public String getStartConfig() {
        return (String) startupParams.getOrDefault(LaunchArg.CONFIG, null);
    }

    public boolean getAutoHide() {
        return has(LaunchArg.HIDE);
    }

    @Override
    public String toString() {
        return "StartupParams{" + startupParams.entrySet().stream()
                .map(e -> e.getKey().toString() + "= " + e.getValue().toString())
                .collect(Collectors.joining(","));
    }

    public static class AutoLoginProps {
        private final Properties prop;
        private final String path;

        private AutoLoginProps(String path) throws IOException {
            this.prop = new Properties();
            this.path = path;
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                prop.load(reader);
            }
            System.out.println("Loaded startup properties file");
        }

        public String getUsername() {
            return getAccount().username.orElse(null);
        }

        public String getPassword() {
            return getAccount().password.orElse(null);
        }

        public char[] getMasterPassword() {
            return getAccount().masterPassword.orElse(null);
        }

        public String getServer() {
            return getAccount().server.orElse(null);
        }

        public String getSID() {
            return getAccount().sid.orElse(null);
        }

        public boolean isAllowStoreSID() {
            return getAccount().allowStore;
        }

        public void setServer(String server) {
            getAccount().setServer(server);
        }

        public void setSID(String sid) {
            getAccount().setSid(sid);
        }

        public void updateLoginFile() {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
                prop.store(writer, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public AccountCredential getAccount() {
            return new AccountCredential(null);
        }

        public List<AccountCredential> getAccounts() {
            List<AccountCredential> accounts = new ArrayList<>();
            accounts.add(getAccount());

            for(int i = 1;; i++) {
                AccountCredential account = new AccountCredential(i);
                if(!account.isValid()) break;
                accounts.add(account);
            }

            return accounts;
        }

        public class AccountCredential {
            @Getter
            private Optional<String> username, password, server, sid;
            @Getter
            private Optional<char[]> masterPassword;
            @Getter
            private final boolean allowStore;

            private final String index;

            private AccountCredential(Integer i) {
                this.index = i == null ? "" : String.valueOf(i);
                this.username = Optional.ofNullable(toNullEmpty(prop.getProperty(PropertyKey.USERNAME + index)));
                this.password = Optional.ofNullable(toNullEmpty(prop.getProperty(PropertyKey.PASSWORD + index)));
                this.server = Optional.ofNullable(toNullEmpty(prop.getProperty(PropertyKey.SERVER + index)));
                this.sid = Optional.ofNullable(toNullEmpty(prop.getProperty(PropertyKey.SID + index)));
                this.masterPassword = Optional.ofNullable(toNullEmpty(prop.getProperty(PropertyKey.MASTER_PASSWORD.toString()))).map(String::toCharArray);
                this.allowStore = Boolean.parseBoolean(toNullEmpty(prop.getProperty(PropertyKey.ALLOW_STORE_SID.toString())));
            }

            public void setUsername(String username) {
                this.username = Optional.ofNullable(toNullEmpty(username));
                prop.setProperty(PropertyKey.USERNAME + index, username);
            }

            public void setPassword(String password) {
                this.password = Optional.ofNullable(toNullEmpty(password));
                prop.setProperty(PropertyKey.PASSWORD + index, password);
            }

            public void setServer(String server) {
                this.server = Optional.ofNullable(toNullEmpty(server));
                prop.setProperty(PropertyKey.SERVER + index, server);
            }

            public void setSid(String sid) {
                this.sid = Optional.ofNullable(toNullEmpty(sid));
                prop.setProperty(PropertyKey.SID + index, sid);
            }

            public boolean flush() {
                if (!allowStore) return  false;
                updateLoginFile();
                return true;
            }

            public boolean hasServerAndSid() {
                return server.isPresent() && sid.isPresent();
            }

            private boolean isValid() {
                return username.isPresent() && password.isPresent() || hasServerAndSid();
            }

            private String toNullEmpty(String val) {
                return Strings.isEmpty(val) ? null : val;
            }
        }
    }
}
