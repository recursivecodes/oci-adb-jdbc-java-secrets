package codes.recursive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fnproject.fn.testing.*;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

public class WalletSecretFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void shouldReturnList() throws JsonProcessingException {
        testing.givenEvent().enqueue();
        testing.thenRun(WalletSecretFunction.class, "handleRequest");
        FnResult result = testing.getOnlyResult();
        List list = new ObjectMapper().readValue(result.getBodyAsString(),List.class);
        assertNotNull(list);
    }

}