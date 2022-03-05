package USForestRegistry;

import java.sql.*;
import java.util.HashMap;
import static USForestRegistry.AttributeNames.*;

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
		//con.setAutoCommit(false); //TODO: do we need this?
		addForestStmt_FOREST = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?, ?)", FOREST));
		addForestStmt_STATE = con.prepareStatement(String.format(
				"INSERT INTO %s(%s) VALUES(?)", STATE, ABBREVIATION));
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

	public void addForest(HashMap<String, String> attrToVal) throws SQLException
	{
		addForestStmt_FOREST.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_FOREST.setString(2, attrToVal.get(NAME));
		addForestStmt_FOREST.setDouble(3, Double.parseDouble(attrToVal.get(AREA)));
		addForestStmt_FOREST.setDouble(4, Double.parseDouble(attrToVal.get(ACID_LEVEL)));
		addForestStmt_FOREST.setDouble(5, Double.parseDouble(attrToVal.get(MBR_XMIN)));
		addForestStmt_FOREST.setDouble(6, Double.parseDouble(attrToVal.get(MBR_XMAX)));
		addForestStmt_FOREST.setDouble(7, Double.parseDouble(attrToVal.get(MBR_YMIN)));
		addForestStmt_FOREST.setDouble(8, Double.parseDouble(attrToVal.get(MBR_YMAX)));

		addForestStmt_STATE.setString(1, attrToVal.get(ABBREVIATION));

		addForestStmt_COVERAGE.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_COVERAGE.setString(2, attrToVal.get(ABBREVIATION));
		addForestStmt_COVERAGE.setDouble(3, Double.parseDouble(attrToVal.get(AREA))); //TODO: or is this parameter 4

		addForestStmt_FOREST.executeUpdate();
		addForestStmt_STATE.executeUpdate();
		addForestStmt_COVERAGE.executeUpdate();
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
		addForestStmt_FOREST.close();
		addForestStmt_STATE.close();
		addForestStmt_COVERAGE.close();
		//TODO: rest of statements
		con.close();
	}
}
