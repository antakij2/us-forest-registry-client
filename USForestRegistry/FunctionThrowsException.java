package USForestRegistry;

import java.util.HashMap;

/**
 * A type for variables holding a method reference, where the referenced method takes one argument
 * (a HashMap of String to String), returns a value of some type, and may throw an Exception.
 * @param <R> the return type of the referenced method
 */
public interface FunctionThrowsException<R>
{
    R apply(HashMap<String, String> arg) throws Exception;
}
