package USForestRegistry;

import java.sql.*;
import java.util.LinkedHashMap;
import static USForestRegistry.AttributeNames.*;

public class Model
{
	private final Connection con;
	private final PreparedStatement addForestStmt_FOREST;
	private final PreparedStatement addWorkerStmt;
	private final PreparedStatement addSensorStmt;
	private final PreparedStatement switchWorkersDutiesStmt;
	private final PreparedStatement updateSensorStatusStmt;
	private final PreparedStatement updateForestCoveredAreaStmt;
	private final PreparedStatement findTopKBusyWorkersStmt;
	private final PreparedStatement displaySensorsRankingStmt;

	public Model(String[] values) throws SQLException
	{
		// "values" consists of hostname, port, database name, username, and password
		String url = "jdbc:postgresql://" + values[0] + ":" + values[1] + "/" + values[2];

		con = DriverManager.getConnection(url, values[3], values[4]);
		//con.setAutoCommit(false); //TODO: do we need this?
		addForestStmt_FOREST = con.prepareStatement("INSERT INTO forest VALUES(?, ?, ?, ?, ?, ?, ?)");
		//addForestStmt_STATE = con.prepareStatement()
		addWorkerStmt = con.prepareStatement("INSERT INTO worker VALUES(?, ?, ?, ?)");
		addSensorStmt = con.prepareStatement("INSERT INTO sensor VALUES(?, ?, ?, ?, ?, ?, ?)");
		//switchWorkersDutiesStmt
		//updateSensorStatusStmt = con.prepareStatement("UPDATE TABLE sensor SET(energy=?, " +
		//		"last_charged=?, )");
		updateForestCoveredAreaStmt = con.prepareStatement("UPDATE TABLE forest SET(area=?, " +
				"state_abbreviation=?) WHERE name=?");
		//more for updateForestCoveredAreaStmt, to change Coverage table
		//findTopKBusyWorkersStmt
		//displaySensorsRankingStmt
	}

	public void addForest(LinkedHashMap<String, String> attrToVal) throws SQLException
	{
		addForestStmt_FOREST.setString(1, attrToVal.get(FOREST_NO));
		for(int i=2; i<8; ++i)
		{
			addForestStmt.setDouble(i, Double.parseDouble(values[i-1]));
		}
		addForestStmt.setString(8, values[7]);

		addForestStmt.executeUpdate();
	}

	public void addWorker(String ssn, String name, int rank, String state) throws SQLException
	{
		addWorkerStmt.setString(1, ssn);
		addWorkerStmt.setString(2, name);
		addWorkerStmt.setInt(3, rank);
		addWorkerStmt.setString(4, state);

		addWorkerStmt.executeUpdate();
	}

	public void closeConnection() throws SQLException
	{
		con.close();
	}

	public static void main(String[] args) throws SQLException
	{
		Model m = new Model(new String[] {"localhost", "5432", "pset1", "postgres", "pass"});
		m.closeConnection();
	}
}
