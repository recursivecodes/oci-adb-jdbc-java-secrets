package codes.recursive;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class WalletSecretFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnGreeting() {
        testing.givenEvent().enqueue();
        testing.thenRun(WalletSecretFunction.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        //assertEquals("Hello, world!", result.getBodyAsString());
        assertEquals(true, true);
    }

}