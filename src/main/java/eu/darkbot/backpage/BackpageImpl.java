package eu.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import eu.darkbot.api.managers.BackpageAPI;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class BackpageImpl extends BackpageManager implements BackpageAPI {

    public BackpageImpl(Main main) {
        super(main);
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public URI getInstanceURI() {
        return URI.create(instance);
    }

    @Override
    public Instant getLastRequestTime() {
        return Instant.ofEpochMilli(lastRequest);
    }

    @Override
    public Optional<String> findReloadToken(String body) {
        return Optional.ofNullable(getReloadToken(body));
    }
}
