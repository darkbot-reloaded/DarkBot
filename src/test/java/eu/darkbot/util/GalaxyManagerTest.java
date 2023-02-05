package eu.darkbot.util;

import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.backpage.GalaxyManager;
import com.github.manolo8.darkbot.backpage.entities.galaxy.GalaxyGate;
import com.github.manolo8.darkbot.utils.http.Http;
import com.github.manolo8.darkbot.utils.http.Method;
import com.github.manolo8.darkbot.utils.login.LoginData;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

public class GalaxyManagerTest {

    @Test
    public void testSpin() throws Exception {
        BackpageManager api = mock(BackpageManager.class, withSettings()
                .defaultAnswer(CALLS_REAL_METHODS));

        LoginData ld = new LoginData();
        ld.setSid("123456789", "localhost");
        ld.setPreloader("", "userID=1234");
        api.setLoginData(ld);

        Http http = mock(Http.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
        doReturn(new ByteArrayInputStream("materializer locked".getBytes())).when(http).getInputStream();

        doReturn(http).when(api).getConnection(anyString(), any());

        GalaxyManager spinner = new GalaxyManager(api);
        spinner.spinGate(GalaxyGate.ALPHA, true, 1, 0);

        verify(api).getConnection("flashinput/galaxyGates.php?" +
                "userID=1234&action=multiEnergy&sid=123456789&gateID=1&alpha=1&multiplier=1", Method.GET);
    }


}
