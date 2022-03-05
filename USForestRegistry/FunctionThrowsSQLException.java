package USForestRegistry;

import java.sql.SQLException;
import java.util.HashMap;

public interface FunctionThrowsSQLException<R>
{
    R apply(HashMap<String, String> arg) throws SQLException;
}
