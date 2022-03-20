package USForestRegistry;

import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import static USForestRegistry.StringConstants.*;

public class Model
{
	public final NumberFormat[] NUMBER_INSTANCES;
	public final NumberFormat[] INTEGER_INSTANCES;
	public final SimpleDateFormat[] DATE_INSTANCES;

	private final Connection con;
	private final PreparedStatement addForestStmt_FOREST;
	private final PreparedStatement maybeAddNewState;
	private final PreparedStatement addForestStmt_COVERAGE;
	private final PreparedStatement addWorkerStmt;
	private final PreparedStatement addSensorStmt;
	private final PreparedStatement switchWorkersDutiesCheckStateStmt;
	private final PreparedStatement switchWorkersDutiesQuerySensorIDStmt;
	private final PreparedStatement switchWorkersDutiesSetMaintainerOnSSNStmt;
	private final PreparedStatement updateSensorStatusQueryStmt;
	private final PreparedStatement updateSensorStatusStmt_SENSOR;
	private final PreparedStatement updateSensorStatusStmt_REPORT;
	private final PreparedStatement updateForestCoveredAreaQueryStmt;
	private final PreparedStatement updateForestCoveredAreaStmt_FOREST;
	private final PreparedStatement updateForestCoveredArea_COVERAGE;
	private final PreparedStatement findTopKBusyWorkersStmt;
	private final PreparedStatement displaySensorsRankingStmt;
	private final PreparedStatement fetchForestStmt;
	private final PreparedStatement fetchCoverageStmt;
	private final PreparedStatement fetchIntersectionStmt;
	private final PreparedStatement fetchReportStmt;
	private final PreparedStatement fetchRoadStmt;
	private final PreparedStatement fetchSensorStmt;
	private final PreparedStatement fetchStateStmt;
	private final PreparedStatement fetchWorkerStmt;

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
		addForestStmt_FOREST = con.prepareStatement("INSERT INTO forest VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
		maybeAddNewState = con.prepareStatement(String.format(
				"INSERT INTO state(%s) VALUES(?) ON CONFLICT DO NOTHING", ABBREVIATION));
		addForestStmt_COVERAGE = con.prepareStatement("INSERT INTO COVERAGE VALUES(?, ?, 1, ?)");

		addWorkerStmt = con.prepareStatement("INSERT INTO worker VALUES(?, ?, ?, ?)");

		addSensorStmt = con.prepareStatement("INSERT INTO sensor VALUES(?, ?, ?, ?, ?, ?, ?)");

		switchWorkersDutiesCheckStateStmt = con.prepareStatement(String.format(
				"SELECT %s, %s FROM worker WHERE %s in (?, ?)", SSN, EMPLOYING_STATE, NAME));
		switchWorkersDutiesQuerySensorIDStmt = con.prepareStatement(String.format(
				"SELECT %s FROM sensor WHERE %s=?", SENSOR_ID, MAINTAINER));
		switchWorkersDutiesSetMaintainerOnSSNStmt = con.prepareStatement(String.format(
				"UPDATE sensor SET %s=? WHERE %s=?", MAINTAINER, MAINTAINER));

		updateSensorStatusQueryStmt = con.prepareStatement(String.format(
				"SELECT %s FROM sensor WHERE %s=? AND %s=?", SENSOR_ID, X, Y));
		updateSensorStatusStmt_SENSOR = con.prepareStatement(String.format(
				"UPDATE sensor SET %s=?, %s=localtimestamp(0), %s=? WHERE %s=?", LAST_CHARGED, LAST_READ, ENERGY, SENSOR_ID));
		updateSensorStatusStmt_REPORT = con.prepareStatement("INSERT INTO report VALUES(?, localtimestamp(0), ?)");

		updateForestCoveredAreaQueryStmt = con.prepareStatement(String.format(
				"SELECT %s FROM forest WHERE %s=?", FOREST_NO, NAME));
		updateForestCoveredAreaStmt_FOREST = con.prepareStatement(String.format(
				"UPDATE forest SET %s=? WHERE %s=?", AREA, FOREST_NO));
		updateForestCoveredArea_COVERAGE = con.prepareStatement(String.format(
				"UPDATE coverage SET %s=?, %s=? WHERE %s=?", STATE, AREA, FOREST_NO));

		findTopKBusyWorkersStmt = con.prepareStatement(String.format(
				"SELECT %s, %s, COUNT(*) sensors_to_charge FROM worker JOIN sensor ON %s=%s AND" +
						" %s <= 2 GROUP BY %s, %s ORDER BY sensors_to_charge DESC LIMIT ?",
				SSN, NAME, SSN, MAINTAINER, ENERGY, SSN, NAME),
				ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);

		displaySensorsRankingStmt = con.prepareStatement(String.format(
				"SELECT sensor.%s, %s, %s, COUNT(*) reports_generated FROM sensor JOIN report ON " +
						"sensor.%s=report.%s GROUP BY sensor.%s, %s, %s ORDER BY reports_generated DESC",
				SENSOR_ID, X, Y, SENSOR_ID, SENSOR_ID, SENSOR_ID, X, Y),
				ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);

		fetchForestStmt = con.prepareStatement("SELECT * FROM forest", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchCoverageStmt = con.prepareStatement("SELECT * FROM coverage", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchIntersectionStmt = con.prepareStatement("SELECT * FROM intersection", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchReportStmt = con.prepareStatement("SELECT * FROM report", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchRoadStmt = con.prepareStatement("SELECT * FROM road", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchSensorStmt = con.prepareStatement("SELECT * FROM sensor", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchStateStmt = con.prepareStatement("SELECT * FROM state", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
		fetchWorkerStmt = con.prepareStatement("SELECT * FROM worker", ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
	}

	public String addForest(HashMap<String, String> attrToVal) throws SQLException
	{
		addForestStmt_FOREST.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_FOREST.setString(2, attrToVal.get(NAME));
		addForestStmt_FOREST.setDouble(3, Double.parseDouble(attrToVal.get(AREA)));
		addForestStmt_FOREST.setDouble(4, Double.parseDouble(attrToVal.get(ACID_LEVEL)));
		addForestStmt_FOREST.setDouble(5, Double.parseDouble(attrToVal.get(MBR_XMIN)));
		addForestStmt_FOREST.setDouble(6, Double.parseDouble(attrToVal.get(MBR_XMAX)));
		addForestStmt_FOREST.setDouble(7, Double.parseDouble(attrToVal.get(MBR_YMIN)));
		addForestStmt_FOREST.setDouble(8, Double.parseDouble(attrToVal.get(MBR_YMAX)));

		maybeAddNewState.setString(1, attrToVal.get(STATE_ABBREVIATION));

		addForestStmt_COVERAGE.setString(1, attrToVal.get(FOREST_NO));
		addForestStmt_COVERAGE.setString(2, attrToVal.get(STATE_ABBREVIATION));
		addForestStmt_COVERAGE.setDouble(3, Double.parseDouble(attrToVal.get(AREA)));

		executeStatements(addForestStmt_FOREST, maybeAddNewState, addForestStmt_COVERAGE);
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
			throw new Exception("The employing states of the supplied workers are different.\n" +
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

	public String updateSensorStatus(HashMap<String, String> attrToVal) throws Exception
	{
		// Look up the sensor_id associated with the user-supplied X and Y coordinates
		updateSensorStatusQueryStmt.setDouble(1, Double.parseDouble(attrToVal.get(X)));
		updateSensorStatusQueryStmt.setDouble(2, Double.parseDouble(attrToVal.get(Y)));
		executeStatements(updateSensorStatusQueryStmt);
		ResultSet rs = updateSensorStatusQueryStmt.getResultSet();
		int sensorId;
		if(rs.next())
		{
			sensorId = rs.getInt(1);
		}
		else
		{
			throw new Exception("No sensor with the supplied coordinates exists.");
		}

		// Update the sensor with the user-supplied time of last charge and energy level,
		// at the current time
		java.util.Date date = DATE_INSTANCES[0].parse(attrToVal.get(LAST_CHARGED));
		Timestamp timestamp = new Timestamp(date.getTime());
		updateSensorStatusStmt_SENSOR.setTimestamp(1, timestamp);
		updateSensorStatusStmt_SENSOR.setDouble(2, Double.parseDouble(attrToVal.get(ENERGY)));
		updateSensorStatusStmt_SENSOR.setDouble(3, sensorId);

		// Insert a new report with the user-supplied temperature, at the current time
		double temp = Double.parseDouble(attrToVal.get(TEMPERATURE));
		updateSensorStatusStmt_REPORT.setInt(1, sensorId);
		updateSensorStatusStmt_REPORT.setDouble(2, temp);

		executeStatements(updateSensorStatusStmt_SENSOR, updateSensorStatusStmt_REPORT);

		String returnMessage = "Sensor updated successfully.\n";
		if(temp > 100.0)
		{
			returnMessage += "EMERGENCY! This sensor just reported a temperature of " + temp + "!";
		}
		else
		{
			returnMessage += "No emergency reported.";
		}

		return returnMessage;
	}

	public String updateForestCoveredArea(HashMap<String, String> attrToVal) throws Exception
	{
		// Look up the forest_no associated with the user-supplied forest name
		updateForestCoveredAreaQueryStmt.setString(1, attrToVal.get(FOREST_NAME));
		executeStatements(updateForestCoveredAreaQueryStmt);
		ResultSet rs = updateForestCoveredAreaQueryStmt.getResultSet();
		String forestNo;
		if(rs.next())
		{
			forestNo = rs.getString(1);
		}
		else
		{
			throw new Exception("No forest with the supplied name exists.");
		}

		// Update the forest, coverage, and state tables with the user-supplied input
		updateForestCoveredAreaStmt_FOREST.setDouble(1, Double.parseDouble(attrToVal.get(AREA)));
		updateForestCoveredAreaStmt_FOREST.setString(2, forestNo);

		updateForestCoveredArea_COVERAGE.setString(1, attrToVal.get(STATE_ABBREVIATION));
		updateForestCoveredArea_COVERAGE.setDouble(2, Double.parseDouble(attrToVal.get(AREA)));
		updateForestCoveredArea_COVERAGE.setString(3, forestNo);

		maybeAddNewState.setString(1, attrToVal.get(STATE_ABBREVIATION));

		executeStatements(maybeAddNewState, updateForestCoveredAreaStmt_FOREST, updateForestCoveredArea_COVERAGE);
		return "Forest \"" + attrToVal.get(FOREST_NAME) + "\" successfully updated.";
	}

	public ResultSet findTopKBusyWorkers(HashMap<String, String> attrToVal) throws SQLException
	{
		findTopKBusyWorkersStmt.setInt(1, Integer.parseInt(attrToVal.get(K)));

		executeStatements(findTopKBusyWorkersStmt);
		return findTopKBusyWorkersStmt.getResultSet();
	}

	public ResultSet displaySensorsRanking(HashMap<String, String> attrToVal) throws SQLException
	{
		executeStatements(displaySensorsRankingStmt);
		return displaySensorsRankingStmt.getResultSet();
	}

	public ResultSet fetchForest(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchForestStmt);
		return fetchForestStmt.getResultSet();
	}

	public ResultSet fetchCoverage(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchCoverageStmt);
		return fetchCoverageStmt.getResultSet();
	}

	public ResultSet fetchIntersection(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchIntersectionStmt);
		return fetchIntersectionStmt.getResultSet();
	}

	public ResultSet fetchReport(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchReportStmt);
		return fetchReportStmt.getResultSet();
	}

	public ResultSet fetchRoad(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchRoadStmt);
		return fetchRoadStmt.getResultSet();
	}

	public ResultSet fetchSensor(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchSensorStmt);
		return fetchSensorStmt.getResultSet();
	}

	public ResultSet fetchState(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchStateStmt);
		return fetchStateStmt.getResultSet();
	}

	public ResultSet fetchWorker(HashMap<String, String> arg) throws SQLException
	{
		executeStatements(fetchWorkerStmt);
		return fetchWorkerStmt.getResultSet();
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
		addForestStmt_COVERAGE.close();
		addWorkerStmt.close();
		addSensorStmt.close();
		switchWorkersDutiesCheckStateStmt.close();
		switchWorkersDutiesQuerySensorIDStmt.close();
		switchWorkersDutiesSetMaintainerOnSSNStmt.close();
		updateSensorStatusQueryStmt.close();
		updateSensorStatusStmt_SENSOR.close();
		updateSensorStatusStmt_REPORT.close();
		updateForestCoveredAreaQueryStmt.close();
		updateForestCoveredAreaStmt_FOREST.close();
		findTopKBusyWorkersStmt.close();
		displaySensorsRankingStmt.close();
		fetchForestStmt.close();
		fetchCoverageStmt.close();
		fetchIntersectionStmt.close();
		fetchReportStmt.close();
		fetchRoadStmt.close();
		fetchSensorStmt.close();
		fetchStateStmt.close();
		fetchWorkerStmt.close();

		con.close();
	}
}
