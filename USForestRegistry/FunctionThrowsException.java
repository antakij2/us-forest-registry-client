package USForestRegistry;

import java.util.HashMap;

public interface FunctionThrowsException<R>
{
    R apply(HashMap<String, String> arg) throws Exception;
}
