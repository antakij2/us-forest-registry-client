package USForestRegistry;

import java.sql.*;
import java.util.HashMap;
import static USForestRegistry.StringConstants.*;

public class Model
{
	private final Connection con;
	private final PreparedStatement addForestStmt_FOREST;
	private final PreparedStatement addForestStmt_STATE;
	private final PreparedStatement addForestStmt_COVERAGE;
	private final PreparedStatement addWorkerStmt;
	private final PreparedStatement addSensorStmt;
	//private final PreparedStatement switchWorkersDutiesStmt;
	//private final PreparedStatement updateSensorStatusStmt;
	private final PreparedStatement updateForestCoveredAreaStmt;
	//private final PreparedStatement findTopKBusyWorkersStmt;
	//private final PreparedStatement displaySensorsRankingStmt;

	public Model(HashMap<String, String> attrToVal) throws SQLException
	{
		String url = String.format(
				"jdbc:postgresql://%s:%s/%s",
				attrToVal.get(HOSTNAME), attrToVal.get(PORT), attrToVal.get(DATABASE_NAME));

		con = DriverManager.getConnection(url, attrToVal.get(USERNAME), attrToVal.get(PASSWORD));
		//TODO: we need rollback logic in each function if one of the queries fails
		//TODO: alter constraints on schema to implement restrictions from instructions
		addForestStmt_FOREST = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?, ?)", FOREST));
		addForestStmt_STATE = con.prepareStatement(String.format(
				"INSERT INTO %s(%s) VALUES(?) ON CONFLICT DO NOTHING", STATE, ABBREVIATION));
		addForestStmt_COVERAGE = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, 1, ?)", COVERAGE));

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

	public String addForest(HashMap<String, String> attrToVal) throws SQLException
	{
		//TODO: check to see if entered state already exists. You don't need a STATE update if it exists.
		// Same goes for entered fields that are primary keys in other tables, for the remaining functions.

		addForestStmt_FOREST.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_FOREST.setString(2, attrToVal.get(NAME));
		addForestStmt_FOREST.setDouble(3, Double.parseDouble(attrToVal.get(AREA)));
		addForestStmt_FOREST.setDouble(4, Double.parseDouble(attrToVal.get(ACID_LEVEL)));
		addForestStmt_FOREST.setDouble(5, Double.parseDouble(attrToVal.get(MBR_XMIN)));
		addForestStmt_FOREST.setDouble(6, Double.parseDouble(attrToVal.get(MBR_XMAX)));
		addForestStmt_FOREST.setDouble(7, Double.parseDouble(attrToVal.get(MBR_YMIN)));
		addForestStmt_FOREST.setDouble(8, Double.parseDouble(attrToVal.get(MBR_YMAX)));

		addForestStmt_STATE.setString(1, attrToVal.get(STATE_ABBREVIATION));

		addForestStmt_COVERAGE.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_COVERAGE.setString(2, attrToVal.get(STATE_ABBREVIATION));
		addForestStmt_COVERAGE.setDouble(3, Double.parseDouble(attrToVal.get(AREA)));

		con.setAutoCommit(false);
		try
		{
			addForestStmt_FOREST.executeUpdate();
			addForestStmt_STATE.executeUpdate();
			addForestStmt_COVERAGE.executeUpdate();
			con.commit();
		}
		catch(SQLException e)
		{
			con.rollback(); // undo the staged changes made so far
			throw e;		// report the original error
		}
		con.setAutoCommit(true);

		return attrToVal.get(NAME) + " added successfully.";
	}

	public String addWorker(HashMap<String, String> attrToVal) throws SQLException
	{
		addWorkerStmt.setString(1, attrToVal.get(SSN));
		addWorkerStmt.setString(2, attrToVal.get(NAME));
		addWorkerStmt.setInt(3, Integer.parseInt(attrToVal.get(RANK)));
		addWorkerStmt.setString(4, attrToVal.get(STATE));

		addWorkerStmt.executeUpdate();
		return attrToVal.get(NAME) + " added successfully.";
	}

	public void closeConnection() throws SQLException
	{
		addForestStmt_FOREST.close();
		addForestStmt_STATE.close();
		addForestStmt_COVERAGE.close();
		//TODO: rest of statements
		con.close();
	}
}
