package USForestRegistry;

import sun.rmi.server.InactiveGroupException;

import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import static USForestRegistry.StringConstants.*;

public class Model
{
	public final NumberFormat[] NUMBER_INSTANCES;
	public final NumberFormat[] INTEGER_INSTANCES;
	public final SimpleDateFormat[] DATE_INSTANCES;

	private final Connection con;
	private final PreparedStatement addForestStmt_FOREST;
	private final PreparedStatement addForestStmt_STATE;
	private final PreparedStatement addForestStmt_COVERAGE;
	private final PreparedStatement addWorkerStmt;
	private final PreparedStatement addSensorStmt;
	private final PreparedStatement switchWorkersDutiesCheckStateStmt;
	private final PreparedStatement switchWorkersDutiesQuerySensorIDStmt;
	private final PreparedStatement switchWorkersDutiesSetMaintainerOnSSNStmt;
	//private final PreparedStatement updateSensorStatusStmt;
	private final PreparedStatement updateForestCoveredAreaStmt;
	//private final PreparedStatement findTopKBusyWorkersStmt;
	//private final PreparedStatement displaySensorsRankingStmt;

	public Model(HashMap<String, String> attrToVal) throws SQLException
	{
		NUMBER_INSTANCES = new NumberFormat[6];
		for(int i=0; i<NUMBER_INSTANCES.length; ++i)
		{
			NUMBER_INSTANCES[i] = NumberFormat.getNumberInstance();
		}

		INTEGER_INSTANCES = new NumberFormat[1];
		for(int i=0; i<INTEGER_INSTANCES.length; ++i)
		{
			INTEGER_INSTANCES[i] = NumberFormat.getIntegerInstance();
		}

		DATE_INSTANCES = new SimpleDateFormat[2];
		for(int i=0; i<DATE_INSTANCES.length; ++i)
		{
			DATE_INSTANCES[i] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}

		String url = String.format(
				"jdbc:postgresql://%s:%s/%s",
				attrToVal.get(HOSTNAME), attrToVal.get(PORT), attrToVal.get(DATABASE_NAME));

		con = DriverManager.getConnection(url, attrToVal.get(USERNAME), attrToVal.get(PASSWORD));
		//TODO: alter constraints on schema to implement restrictions from instructions
		addForestStmt_FOREST = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?, ?)", FOREST));
		addForestStmt_STATE = con.prepareStatement(String.format(
				"INSERT INTO %s(%s) VALUES(?) ON CONFLICT DO NOTHING", STATE, ABBREVIATION));
		addForestStmt_COVERAGE = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, 1, ?)", COVERAGE));

		addWorkerStmt = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, ?, ?)", WORKER));

		addSensorStmt = con.prepareStatement(String.format(
				"INSERT INTO %s VALUES(?, ?, ?, ?, ?, ?, ?)", SENSOR));

		switchWorkersDutiesCheckStateStmt = con.prepareStatement(String.format(
				"SELECT %s, %s FROM worker WHERE %s in (?, ?)", SSN, EMPLOYING_STATE, NAME));
		switchWorkersDutiesQuerySensorIDStmt = con.prepareStatement(String.format(
				"SELECT %s FROM sensor WHERE %s=?", SENSOR_ID, MAINTAINER));
		switchWorkersDutiesSetMaintainerOnSSNStmt = con.prepareStatement(String.format(
				"UPDATE sensor SET %s=? WHERE %s=?", MAINTAINER, MAINTAINER));

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

		executeStatements(addForestStmt_FOREST, addForestStmt_STATE, addForestStmt_COVERAGE);
		return "Forest \"" + attrToVal.get(NAME) + "\" added successfully.";
	}

	public String addWorker(HashMap<String, String> attrToVal) throws SQLException
	{
		addWorkerStmt.setString(1, attrToVal.get(SSN));
		addWorkerStmt.setString(2, attrToVal.get(NAME));
		addWorkerStmt.setInt(3, Integer.parseInt(attrToVal.get(RANK)));
		addWorkerStmt.setString(4, attrToVal.get(EMPLOYING_STATE));

		executeStatements(addWorkerStmt);
		return "Worker \"" + attrToVal.get(NAME) + "\" added successfully.";
	}

	public String addSensor(HashMap<String, String> attrToVal) throws SQLException, ParseException
	{
		addSensorStmt.setInt(1, Integer.parseInt(attrToVal.get(SENSOR_ID)));
		addSensorStmt.setInt(2, Integer.parseInt(attrToVal.get(X)));
		addSensorStmt.setInt(3, Integer.parseInt(attrToVal.get(Y)));

		java.util.Date date = DATE_INSTANCES[0].parse(attrToVal.get(LAST_CHARGED));
		Timestamp timestamp = new Timestamp(date.getTime());
		addSensorStmt.setTimestamp(4, timestamp);

		addSensorStmt.setString(5, attrToVal.get(MAINTAINER));

		date = DATE_INSTANCES[0].parse(attrToVal.get(LAST_READ));
		timestamp = new Timestamp(date.getTime());
		addSensorStmt.setTimestamp(6, timestamp);

		addSensorStmt.setDouble(7, Double.parseDouble(attrToVal.get(ENERGY)));


		executeStatements(addSensorStmt);
		return "Sensor \"" + attrToVal.get(SENSOR_ID) + "\" at coordinates X = " + attrToVal.get(X) +
				", Y = " + attrToVal.get(Y) + " added successfully.";
	}

	public String switchWorkersDuties(HashMap<String, String> attrToVal) throws Exception
	{
		// Check if both workers' names exist in the database
		switchWorkersDutiesCheckStateStmt.setString(1, attrToVal.get(WORKER_A_NAME));
		switchWorkersDutiesCheckStateStmt.setString(2, attrToVal.get(WORKER_B_NAME));
		executeStatements(switchWorkersDutiesCheckStateStmt);

		ResultSet rs = switchWorkersDutiesCheckStateStmt.getResultSet();
		String[] ssns = new String[2];
		String[] states = new String[2];

		int i = 0;
		while(rs.next())
		{
			ssns[i] = rs.getString(1);
			states[i] = rs.getString(2);

			++i;
		}
		if(ssns[1] == null)
		{
			// if the result set had any less than 2 rows, then one or both of the worker names did not exist
			throw new Exception("One or more of the supplied workers do not exist.");
		}

		// Check if both workers have the same employing state
		if(!states[0].equals(states[1]))
		{
			// if the employing states aren't the same, the workers can't switch duties
			throw new Exception("The employing states of the supplied workers are different. " +
					"The workers cannot switch duties.");
		}


		// Store the sensor_id's of the sensors currently assigned to the first ssn
		switchWorkersDutiesQuerySensorIDStmt.setString(1, ssns[0]);
		executeStatements(switchWorkersDutiesQuerySensorIDStmt);
		rs = switchWorkersDutiesQuerySensorIDStmt.getResultSet();

		// For all sensors with the second ssn as the maintainer, change them to have the first ssn
		switchWorkersDutiesSetMaintainerOnSSNStmt.setString(1, ssns[0]);
		switchWorkersDutiesSetMaintainerOnSSNStmt.setString(2, ssns[1]);

		// For all sensors that originally had the first ssn as the maintainer, if there were any,
		// change them to have the second ssn
		if(rs.next())
		{
			StringBuilder ids = new StringBuilder("(");
			ids.append(rs.getInt(1));
			while (rs.next())
			{
				ids.append(",");
				ids.append(rs.getInt(1));
			}
			ids.append(")");
			PreparedStatement setMaintainerOnIDStmt = con.prepareStatement(String.format(
					"UPDATE sensor SET %s=%s WHERE %s in %s", MAINTAINER, ssns[1], SENSOR_ID, ids.toString()));
			executeStatements(switchWorkersDutiesSetMaintainerOnSSNStmt, setMaintainerOnIDStmt);
		}
		else
		{
			executeStatements(switchWorkersDutiesSetMaintainerOnSSNStmt);
		}

		return "Workers " + attrToVal.get(WORKER_A_NAME) + " and " + attrToVal.get(WORKER_B_NAME) +
				" have successfully switched duties.";
	}

	private void executeStatements(PreparedStatement... statements) throws SQLException
	{
		con.setAutoCommit(false);
		try
		{
			for (PreparedStatement s : statements)
			{
				s.execute();
			}
		}
		catch(SQLException e)
		{
			con.rollback(); // undo the staged changes made so far
			throw e;		// report the original error
		}
		con.setAutoCommit(true);
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
