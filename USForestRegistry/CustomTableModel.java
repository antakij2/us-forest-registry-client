package USForestRegistry;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Implementation of the TableModel interface, which holds the information read from a database table.
 * For this application, all this implementation needs to do is display an immutable table of Java Strings.
 */
public class CustomTableModel implements TableModel
{
    ResultSet rs;
    ResultSetMetaData metadata;
    int cols;
    int rows;

    /**
     * @param rs the results of a query executed on the underlying database
     */
    public CustomTableModel(ResultSet rs) throws SQLException
    {
        this.rs = rs;
        metadata = rs.getMetaData();
        cols = metadata.getColumnCount();

        while (rs.next())
        {
            ++rows;
        }
        rs.beforeFirst();
    }

    public int getColumnCount()
    {
        return cols;
    }

    public int getRowCount()
    {
        return rows;
    }

    public String getColumnName(int column)
    {
        try
        {
            return metadata.getColumnLabel(column + 1);
        }
        catch (SQLException e)
        {
            return e.toString();
        }
    }

    public Class getColumnClass(int column)
    {
        return String.class; // all columns display Strings
    }

    /**
     * Get the value held in a specific table cell
     * @param rowIndex the row index
     * @param columnIndex the column index
     * @return the value in the specified table cell
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        try
        {
            rs.absolute(rowIndex + 1);
            Object o = rs.getObject(columnIndex + 1);
            if (o == null)
            {
                return null;
            }
            else
            {
                return o.toString();
            }
        }
        catch (SQLException e)
        {
            return e.toString();
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false; // the displayed table is immutable
    }

    public void setValueAt(Object value, int row, int column) {}

    public void addTableModelListener(TableModelListener l) {}

    public void removeTableModelListener(TableModelListener l) {}
}
