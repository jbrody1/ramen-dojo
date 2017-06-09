package problem;
import java.util.*;

public interface Restaurant
{
    public void setTables(Set<Table> tables);
    public void lineUp(Customer customer);
    public void rageQuit(Customer customer);
}
