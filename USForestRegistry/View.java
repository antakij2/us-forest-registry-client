package USForestRegistry;

import java.awt.*;
import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.HashMap;

import static USForestRegistry.StringConstants.*;
//TODO: create the max amount of each formatter type that is shown in a dialog box at once, and use those as pools
public class View 
{
	/*
	* Create the GUI and show it. 
	*/
	private static void createAndShowGUI()
	{
		//Create and set up the window.
		JFrame frame = new JFrame("U.S. Forest Registry");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //TODO: do custom function where it closes model, which needs to be an instance variable of a View object

		//Before showing the main frame, show the initial database connection/login dialog, and connect to the database
		/*LabelAndFormat[] connectLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(HOSTNAME, null),
						new LabelAndFormat(PORT, NumberFormat.getIntegerInstance()),
						new LabelAndFormat(DATABASE_NAME, null),
						new LabelAndFormat(USERNAME, null),
						new LabelAndFormat(PASSWORD, null)
				};
		Model model = callMethodViaCustomDialog(Model::new, frame, "Connect to Database",
				connectLabelsAndFormats, "Connect", false);*/
		//TODO: remove this autologin after debugging
		LinkedHashMap<String, String> loginCredentials = new LinkedHashMap<>();
		loginCredentials.put(HOSTNAME, "localhost");
		loginCredentials.put(PORT, "5432");
		loginCredentials.put(DATABASE_NAME, "pset1");
		loginCredentials.put(USERNAME, "postgres");
		loginCredentials.put(PASSWORD, "pass");
		Model model = null;
		try
		{
			model = new Model(loginCredentials);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if(model == null)
		{
			//User X'd out without logging in
			frame.dispose();
			return;
		}

		//Create the menu bar and contained menu items.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);

		JMenu insert = new JMenu(INSERT);

		// These are the text fields, and the format of inputs permitted in those text fields,
		// that the dialog for this action will present to the user to fill in
		LabelAndFormat[] addForestLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(FOREST_NO, null),
						new LabelAndFormat(NAME, null),
						new LabelAndFormat(AREA, NumberFormat.getNumberInstance()),
						new LabelAndFormat(ACID_LEVEL, NumberFormat.getNumberInstance()),
						new LabelAndFormat(MBR_XMIN, NumberFormat.getNumberInstance()),
						new LabelAndFormat(MBR_XMAX, NumberFormat.getNumberInstance()),
						new LabelAndFormat(MBR_YMIN, NumberFormat.getNumberInstance()),
						new LabelAndFormat(MBR_YMAX, NumberFormat.getNumberInstance()),
						new LabelAndFormat(STATE_ABBREVIATION, null)
				};
		MenuItemAction<String> addForestAction = new MenuItemAction<>(ADD_FOREST+DOTS, model::addForest, frame,
				ADD_FOREST, addForestLabelsAndFormats, INSERT, true);
		JMenuItem addForest = new JMenuItem(addForestAction);

		LabelAndFormat[] addWorkerLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(SSN, null),
						new LabelAndFormat(NAME, null),
						new LabelAndFormat(RANK, NumberFormat.getIntegerInstance()),
						new LabelAndFormat(EMPLOYING_STATE, null)
				};
		MenuItemAction<String> addWorkerAction = new MenuItemAction<>(ADD_WORKER+DOTS, model::addWorker, frame,
				ADD_WORKER, addWorkerLabelsAndFormats, INSERT, true);
		JMenuItem addWorker = new JMenuItem(addWorkerAction);

		LabelAndFormat[] addSensorLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(SENSOR_ID, NumberFormat.getIntegerInstance()),
						new LabelAndFormat(X, NumberFormat.getNumberInstance()),
						new LabelAndFormat(Y, NumberFormat.getNumberInstance()),
						new LabelAndFormat(LAST_CHARGED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
						new LabelAndFormat(MAINTAINER, null),
						new LabelAndFormat(LAST_READ, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
						new LabelAndFormat(ENERGY, NumberFormat.getNumberInstance())
				};
		MenuItemAction<String> addSensorAction = new MenuItemAction<>(ADD_SENSOR+DOTS, model::addSensor, frame,
				ADD_SENSOR, addSensorLabelsAndFormats, INSERT, true);
		JMenuItem addSensor = new JMenuItem(addSensorAction);

		insert.add(addForest);
		insert.add(addWorker);
		insert.add(addSensor);


		JMenu update = new JMenu(UPDATE);

		LabelAndFormat[] switchWorkersDutiesLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(WORKER_A_NAME, null),
						new LabelAndFormat(WORKER_B_NAME, null)
				};
		MenuItemAction<String> switchWorkersDutiesAction = new MenuItemAction<>(SWITCH_WORKERS_DUTIES + DOTS,
				model::switchWorkersDuties, frame, SWITCH_WORKERS_DUTIES, switchWorkersDutiesLabelsAndFormats, UPDATE,
				true);
		JMenuItem switchWorkersDuties = new JMenuItem(switchWorkersDutiesAction);

		LabelAndFormat[] updateSensorStatusLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(X, NumberFormat.getNumberInstance()),
						new LabelAndFormat(Y, NumberFormat.getNumberInstance()),
						new LabelAndFormat(LAST_CHARGED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
						new LabelAndFormat(ENERGY, NumberFormat.getNumberInstance()),
						new LabelAndFormat(TEMPERATURE, NumberFormat.getNumberInstance())
				};
		MenuItemAction<String> updateSensorStatusAction = new MenuItemAction<>(UPDATE_SENSOR_STATUS + DOTS,
				model::updateSensorStatus, frame, UPDATE_SENSOR_STATUS, updateSensorStatusLabelsAndFormats, UPDATE,
				true);
		JMenuItem updateSensorStatus = new JMenuItem(updateSensorStatusAction);

		LabelAndFormat[] updateForestCoveredAreaLabelsAndformats = new LabelAndFormat[]
				{
						new LabelAndFormat(FOREST_NAME, null),
						new LabelAndFormat(AREA, NumberFormat.getNumberInstance()),
						new LabelAndFormat(STATE_ABBREVIATION, null)
				};
		MenuItemAction<String> updateForestCoveredAreaAction = new MenuItemAction<>(UPDATE_FOREST_COVERED_AREA + DOTS,
				model::updateForestCoveredArea, frame, UPDATE_FOREST_COVERED_AREA, updateForestCoveredAreaLabelsAndformats,
				UPDATE, true);
		JMenuItem updateForestCoveredArea = new JMenuItem(updateForestCoveredAreaAction);

		update.add(switchWorkersDuties);
		update.add(updateSensorStatus);
		update.add(updateForestCoveredArea);


		JMenu select = new JMenu(QUERY);

		LabelAndFormat[] findTopKBusyWorkersLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(K, NumberFormat.getIntegerInstance())
				};
		MenuItemAction<ResultSet> findTopKBusyWorkersAction = new MenuItemAction<>(FIND_TOP_K_BUSY_WORKERS + DOTS,
				model::findTopKBusyWorkers, frame, FIND_TOP_K_BUSY_WORKERS, findTopKBusyWorkersLabelsAndFormats, QUERY,
				true);
		JMenuItem findTopKBusyWorkers = new JMenuItem(findTopKBusyWorkersAction);

		MenuItemAction<ResultSet> displaySensorsRankingAction = new MenuItemAction<>(DISPLAY_SENSORS_RANKING + DOTS,
				model::displaySensorsRanking, frame, DISPLAY_SENSORS_RANKING, null, QUERY, true);
		JMenuItem displaySensorsRanking = new JMenuItem(displaySensorsRankingAction);

		select.add(findTopKBusyWorkers);
		select.add(displaySensorsRanking);

		menuBar.add(insert);
		menuBar.add(update);
		menuBar.add(select);
		frame.setJMenuBar(menuBar);

		//Create table viewing area.
		HashMap<String, FunctionThrowsException<ResultSet>> tableToFetchMethod = new HashMap<>();
		tableToFetchMethod.put(FOREST, model::fetchForest);
		tableToFetchMethod.put(COVERAGE, model::fetchCoverage);
		tableToFetchMethod.put(INTERSECTION, model::fetchIntersection);
		tableToFetchMethod.put(REPORT, model::fetchReport);
		tableToFetchMethod.put(ROAD, model::fetchRoad);
		tableToFetchMethod.put(SENSOR, model::fetchSensor);
		tableToFetchMethod.put(STATE, model::fetchState);
		tableToFetchMethod.put(WORKER, model::fetchWorker);

		try
		{
			CustomTableModel tableModel = new CustomTableModel(model.fetchForest(null));
			JTable databaseViewer = new JTable();
			databaseViewer.setModel(tableModel);
			JScrollPane scrollPane = new JScrollPane(databaseViewer);
			databaseViewer.setFillsViewportHeight(true);
			frame.getContentPane().add(scrollPane); //TODO: can only call this once, so must make it contain multiple panels

			String[] tableNames = {FOREST, COVERAGE, INTERSECTION, REPORT, ROAD, SENSOR, STATE, WORKER};
			JComboBox<String> comboBox = new JComboBox<>(tableNames);
			frame.getContentPane().add(comboBox);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}


		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	//TODO: alter so you can have menu item which has no dialog, just executes method
	private static <R> R callMethodViaCustomDialog(FunctionThrowsException<R> methodReference, Frame owner,
												   String title, LabelAndFormat[] labelsAndFormats,
												   String affirmativeOptionText, boolean hasCancelButton)
	{
		// Generate the dialog box according to the arguments to this function
		R toReturn = null;
		LabeledTextFieldDialog dialog = new LabeledTextFieldDialog(
				owner,
				title,
				labelsAndFormats,
				affirmativeOptionText,
				hasCancelButton
		);

		// Attempt to call the associated method with the user-supplied info from the dialog box.
		// Show an error message and let the user re-enter info if the intended method call throws an exception,
		// otherwise, show a method-specific confirmation message if the method call was successful.
		while(true)
		{
			if(dialog.getOptionPaneValue() == affirmativeOptionText) //user hit the affirmative button
			{
				try
				{
					toReturn = methodReference.apply(dialog.getLabelToTypedText());
					break;
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(dialog, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					dialog.revive();
				}
			}
			else //user hit Cancel, or X'd out the window
			{
				break;
			}
		}

		dialog.dispose();
		return toReturn;
	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(
				new Runnable() 
				{
					public void run() 
					{
						createAndShowGUI();
					}
				}
				);
	}

	private static class MenuItemAction<R> extends AbstractAction
	{
		private final FunctionThrowsException<R> methodToCall;
		private final Frame dialogOwner;
		private final String dialogTitle;
		private final LabelAndFormat[] dialogLabelsAndFormats;
		private final String dialogAffirmativeOptionText;
		private final boolean dialogHasCancelButton;

		public MenuItemAction(String text, FunctionThrowsException<R> methodToCall, Frame dialogOwner, String dialogTitle,
							  LabelAndFormat[] dialogLabelsAndFormats, String dialogAffirmativeOptionText,
							  boolean dialogHasCancelButton)
		{
			super(text);
			this.methodToCall = methodToCall;
			this.dialogOwner = dialogOwner;
			this.dialogTitle = dialogTitle;
			this.dialogLabelsAndFormats = dialogLabelsAndFormats;
			this.dialogAffirmativeOptionText = dialogAffirmativeOptionText;
			this.dialogHasCancelButton = dialogHasCancelButton;
		}

		public void actionPerformed(ActionEvent a)
		{
			R confirmation = callMethodViaCustomDialog(methodToCall, dialogOwner, dialogTitle, dialogLabelsAndFormats,
					dialogAffirmativeOptionText, dialogHasCancelButton);

			if(confirmation != null)
			{
				/*if(confirmation instanceof rowset)
				{
					//set toDisplay = table view
					// change "Confirmation" text to be "Results" here
				}
				else
				{
					//set toDisplay = confirmation
					// and change "confirmation" argument to toDisplay
				}*/
				JOptionPane.showMessageDialog(dialogOwner, confirmation, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}
