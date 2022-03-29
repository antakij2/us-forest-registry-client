package USForestRegistry;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static USForestRegistry.StringConstants.*;
//TODO: in model, create pools of each formatter type that is shown in a dialog box
//TODO: wherever there's a printStackTrace, replace with a popup error dialog showing message
//TODO: add comments for all classes/functions
//FIXME: how to add sensor with null maintainer? jtextfield interprets it as empty string rn.
// Also means forgetting to add value for what should violate a null constraint will be allowed since its
// the empty string. hardcode it to interpret empty string as null?
public class View implements ActionListener
{
	Model model;
	JComboBox<String> cb;
	JTable databaseViewer;

	/*
	* Create the GUI and show it. 
	*/
	private void createAndShowGUI()
	{
		//Create and set up the window.
		JFrame frame = new JFrame("U.S. Forest Registry");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

		frame.addWindowListener(new ModelWindowAdapter(model));

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
						new LabelAndFormat(AREA, model.NUMBER_INSTANCES[0]),
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
						new LabelAndFormat(ENERGY, model.NUMBER_INSTANCES[0])
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

		MenuItemAction<ResultSet> displaySensorsRankingAction = new MenuItemAction<>(DISPLAY_SENSORS_RANKING,
				model::displaySensorsRanking, frame, "", null, "", false);
		JMenuItem displaySensorsRanking = new JMenuItem(displaySensorsRankingAction);

		select.add(findTopKBusyWorkers);
		select.add(displaySensorsRanking);


		/*JMenu testing = new JMenu("Testing");

		MenuItemAction<String> testUpdateForestCoveredAreaAction = new MenuItemAction<>("Test Concurrency: Update Forest Covered Area",
				model::testConcurrencyError, frame, "Test Concurrency: Update Forest Covered Area",
				null, "", false);
		JMenuItem testUpdateForestCoveredArea = new JMenuItem(testUpdateForestCoveredAreaAction);

		testing.add(testUpdateForestCoveredArea);*/

		menuBar.add(insert);
		menuBar.add(update);
		menuBar.add(select);
		//menuBar.add(testing);
		frame.setJMenuBar(menuBar);

		CustomTableModel tableModel = null;
		try
		{
			tableModel = new CustomTableModel(model.fetchForest());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		databaseViewer = new JTable();
		databaseViewer.setModel(tableModel);
		JScrollPane scrollPane = new JScrollPane(databaseViewer);
		scrollPane.setPreferredSize(new Dimension(700, 200));
		databaseViewer.setFillsViewportHeight(true);

		JLabel tableSelectionLabel = new JLabel("Select a table to view:       ");
		cb = new JComboBox<>(new String[] {FOREST, COVERAGE, INTERSECTION, REPORT, ROAD, SENSOR, STATE, WORKER});
		cb.addActionListener(this);
		JButton refresh = new JButton(new RefreshButtonAction(this));

		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.LINE_AXIS));
		lowerPanel.add(tableSelectionLabel);
		lowerPanel.add(cb);
		lowerPanel.add(refresh);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(scrollPane);
		mainPanel.add(lowerPanel);

		frame.getContentPane().add(mainPanel);

		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private <R> R callMethodViaCustomDialog(FunctionThrowsException<R> methodReference, Frame owner,
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
		// otherwise, show a method-specific success message after the method call.
		while(toReturn == null)
		{
			if(dialog.getOptionPaneValue() == affirmativeOptionText) //user hit the affirmative button
			{
				try
				{
					toReturn = methodReference.apply(dialog.getLabelToTypedText());

					// Ask the user to confirm entered data, if there was any. If this is an update operation, and if,
					// while this confirmation dialog is shown, user 2 on a different terminal successfully updates the
					// same row as user 1 is about to update, user 1's attempt will fail.
					if(!dialog.isDummyDialog())
					{
						// Build up the string representation of the entered data to show back to the user
						StringBuilder sb = new StringBuilder();
						for (Map.Entry<String, String> entry : dialog.getLabelToTypedText().entrySet())
						{
							sb.append(entry.getKey());
							sb.append(" = ");
							sb.append(entry.getValue());
							sb.append("\n");
						}

						int choice = JOptionPane.showConfirmDialog(dialog, sb.toString(), "Confirm",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE); //TODO: "OK" should be custom operation-based label

						if(choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION)
						{
							// Let the user go back and change data
							model.rollback();
							toReturn = null;
							dialog.revive();
							continue;
						}
					}

					// If we reached this point, the backend method executed, but did not commit its changes
					model.commit();
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(dialog, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					if(dialog.isDummyDialog())
					{
						// if we were trying to do this operation without showing a dialog box and an error occurred,
						// just stop trying to do the operation.
						break;
					}
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

	public void actionPerformed(ActionEvent e) {
		String selectedTable = (String) cb.getSelectedItem();

		ResultSet newData = null;
		try
		{
			switch (selectedTable)
			{
				case FOREST:
					newData = model.fetchForest();
					break;
				case COVERAGE:
					newData = model.fetchCoverage();
					break;
				case INTERSECTION:
					newData = model.fetchIntersection();
					break;
				case REPORT:
					newData = model.fetchReport();
					break;
				case ROAD:
					newData = model.fetchRoad();
					break;
				case SENSOR:
					newData = model.fetchSensor();
					break;
				case STATE:
					newData = model.fetchState();
					break;
				case WORKER:
					newData = model.fetchWorker();
					break;
				default:
					break;
			}

			databaseViewer.setModel(new CustomTableModel(newData));
		}
		catch(SQLException exception)
		{
			exception.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(
				new Runnable() 
				{
					public void run() 
					{
						View view = new View();
						view.createAndShowGUI();
					}
				}
				);
	}

	private static class ModelWindowAdapter extends WindowAdapter
	{
		Model model;

		public ModelWindowAdapter(Model m)
		{
			model = m;
		}

		public void windowClosing(WindowEvent e)
		{
			try
			{
				model.closeConnection();
			}
			catch(SQLException exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private static class RefreshButtonAction extends AbstractAction
	{
		View v;

		public RefreshButtonAction(View v)
		{
			super("Refresh");
			this.v = v;
		}

		public void actionPerformed(ActionEvent a)
		{
			// Just call the same method that gets called when a new selection is made in the table-selector JComboBox.
			// This will reload the currently selected table.
			v.actionPerformed(null);
		}
	}

	private class MenuItemAction<R> extends AbstractAction
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
			String title;
			Object toDisplay;
			int messageType;
			R result = callMethodViaCustomDialog(methodToCall, dialogOwner, dialogTitle, dialogLabelsAndFormats,
					dialogAffirmativeOptionText, dialogHasCancelButton);

			if(result != null)
			{
				if(result instanceof ResultSet)
				{
					try
					{
						TableModel model = new CustomTableModel((ResultSet) result);
						JTable table = new JTable(model);
						toDisplay = new JScrollPane(table);
					}
					catch(SQLException e)
					{
						e.printStackTrace();
						return;
					}

					messageType = JOptionPane.PLAIN_MESSAGE;
					title = "Data";
				}
				else
				{
					toDisplay = result;
					messageType = JOptionPane.INFORMATION_MESSAGE;
					title = "Confirmation";
				}
				JOptionPane.showMessageDialog(dialogOwner, toDisplay, title, messageType);
			}
		}
	}
}
