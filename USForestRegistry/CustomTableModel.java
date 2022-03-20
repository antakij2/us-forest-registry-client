package USForestRegistry;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class CustomTableModel implements TableModel
{
    ResultSet rs;
    ResultSetMetaData metadata;
    int cols;
    int rows;

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
        return String.class;
    }

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
        return false;
    }

    public void setValueAt(Object value, int row, int column) {}

    public void addTableModelListener(TableModelListener l) {}

    public void removeTableModelListener(TableModelListener l) {}
}
