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
import java.util.Map;

import static USForestRegistry.StringConstants.*;

/**
 * The GUI user interface for this application. It communicates with the Model backend to read from and write to the
 * underlying database.
 */
public class View implements ActionListener
{
	Model model;
	JComboBox<String> cb;
	JTable databaseViewer;
	JFrame frame;

	/**
	* Creates a View object, and in doing so, builds up the GUI and shows it.
	*/
	public View()
	{
		//Create and set up the main frame.
		frame = new JFrame("U.S. Forest Registry");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//Before showing the main frame, show the initial database login dialog, and connect to the database
		LabelAndFormat[] connectLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(HOSTNAME, null),
						new LabelAndFormat(PORT, NumberFormat.getIntegerInstance()),
						new LabelAndFormat(DATABASE_NAME, null),
						new LabelAndFormat(USERNAME, null),
						new LabelAndFormat(PASSWORD, null)
				};

		// Create the Model object and connect to the underlying database
		model = new Model();
		callMethodViaCustomDialog(model::initializeAndConnect, "Connect to Database",
				connectLabelsAndFormats, "Connect", false);

		if(!model.isConnectedToDatabase())
		{
			//User X'd out without logging in
			frame.dispose();
			return;
		}

		// Perform the custom method of closing the model connection when the user X's out the main frame
		frame.addWindowListener(new ModelWindowAdapter());

		// Create the GUI's menu bar and the menu items it contains.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);

		JMenu insert = new JMenu(INSERT);

		// This specifies the text fields, and the format of inputs permitted in those text fields,
		// that this menu item's dialog box will present to the user to fill in
		LabelAndFormat[] addForestLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(FOREST_NO, null), // "null" = there is no special formatter associated with this text field
						new LabelAndFormat(NAME, null),
						new LabelAndFormat(AREA, model.NUMBER_INSTANCES[0]), // pull from the Model's pool of formatters for real numbers
						new LabelAndFormat(ACID_LEVEL, model.NUMBER_INSTANCES[1]),
						new LabelAndFormat(MBR_XMIN, model.NUMBER_INSTANCES[2]),
						new LabelAndFormat(MBR_XMAX, model.NUMBER_INSTANCES[3]),
						new LabelAndFormat(MBR_YMIN, model.NUMBER_INSTANCES[4]),
						new LabelAndFormat(MBR_YMAX, model.NUMBER_INSTANCES[5]),
						new LabelAndFormat(STATE_ABBREVIATION, null)
				};
		// Finish customizing the dialog box and text for this menu item, and specify what method in the Model that this menu
		// item will call when selected by the user
		MenuItemAction<String> addForestAction = new MenuItemAction<>(ADD_FOREST+DOTS, model::addForest,
				ADD_FOREST, addForestLabelsAndFormats, INSERT, true);
		JMenuItem addForest = new JMenuItem(addForestAction);

		LabelAndFormat[] addWorkerLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(SSN, null),
						new LabelAndFormat(NAME, null),
						new LabelAndFormat(RANK, model.INTEGER_INSTANCES[0]), // pull from the Model's pool of formatters for integers
						new LabelAndFormat(EMPLOYING_STATE_ABBREVIATION, null)
				};
		MenuItemAction<String> addWorkerAction = new MenuItemAction<>(ADD_WORKER+DOTS, model::addWorker,
				ADD_WORKER, addWorkerLabelsAndFormats, INSERT, true);
		JMenuItem addWorker = new JMenuItem(addWorkerAction);

		LabelAndFormat[] addSensorLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(SENSOR_ID, model.INTEGER_INSTANCES[0]),
						new LabelAndFormat(X, model.NUMBER_INSTANCES[0]),
						new LabelAndFormat(Y, model.NUMBER_INSTANCES[1]),
						new LabelAndFormat(LAST_CHARGED, model.DATE_INSTANCES[0]), // pull from the Model's pool of formatters for dates/times
						new LabelAndFormat(MAINTAINER, null),
						new LabelAndFormat(LAST_READ, model.DATE_INSTANCES[1]),
						new LabelAndFormat(ENERGY, model.NUMBER_INSTANCES[0])
				};
		MenuItemAction<String> addSensorAction = new MenuItemAction<>(ADD_SENSOR+DOTS, model::addSensor,
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
				model::switchWorkersDuties, SWITCH_WORKERS_DUTIES, switchWorkersDutiesLabelsAndFormats, UPDATE,
				true);
		JMenuItem switchWorkersDuties = new JMenuItem(switchWorkersDutiesAction);

		LabelAndFormat[] updateSensorStatusLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(X, model.NUMBER_INSTANCES[0]),
						new LabelAndFormat(Y, model.NUMBER_INSTANCES[1]),
						new LabelAndFormat(LAST_CHARGED, model.DATE_INSTANCES[0]),
						new LabelAndFormat(ENERGY, model.NUMBER_INSTANCES[2]),
						new LabelAndFormat(TEMPERATURE, model.NUMBER_INSTANCES[3])
				};
		MenuItemAction<String> updateSensorStatusAction = new MenuItemAction<>(UPDATE_SENSOR_STATUS + DOTS,
				model::updateSensorStatus, UPDATE_SENSOR_STATUS, updateSensorStatusLabelsAndFormats, UPDATE,
				true);
		JMenuItem updateSensorStatus = new JMenuItem(updateSensorStatusAction);

		LabelAndFormat[] updateForestCoveredAreaLabelsAndformats = new LabelAndFormat[]
				{
						new LabelAndFormat(FOREST_NAME, null),
						new LabelAndFormat(AREA, model.NUMBER_INSTANCES[0]),
						new LabelAndFormat(STATE_ABBREVIATION, null)
				};
		MenuItemAction<String> updateForestCoveredAreaAction = new MenuItemAction<>(UPDATE_FOREST_COVERED_AREA + DOTS,
				model::updateForestCoveredArea, UPDATE_FOREST_COVERED_AREA, updateForestCoveredAreaLabelsAndformats,
				UPDATE, true);
		JMenuItem updateForestCoveredArea = new JMenuItem(updateForestCoveredAreaAction);

		update.add(switchWorkersDuties);
		update.add(updateSensorStatus);
		update.add(updateForestCoveredArea);


		JMenu select = new JMenu(QUERY);

		LabelAndFormat[] findTopKBusyWorkersLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(K, model.INTEGER_INSTANCES[0])
				};
		MenuItemAction<ResultSet> findTopKBusyWorkersAction = new MenuItemAction<>(FIND_TOP_K_BUSY_WORKERS + DOTS,
				model::findTopKBusyWorkers, FIND_TOP_K_BUSY_WORKERS, findTopKBusyWorkersLabelsAndFormats, QUERY,
				true);
		JMenuItem findTopKBusyWorkers = new JMenuItem(findTopKBusyWorkersAction);

		// The method that this menu item calls does not require input from the user, so it does not trigger the showing
		// of a dialog box. Thus, the array of LabelAndFormat is null
		MenuItemAction<ResultSet> displaySensorsRankingAction = new MenuItemAction<>(DISPLAY_SENSORS_RANKING,
				model::displaySensorsRanking, "", null, "", false);
		JMenuItem displaySensorsRanking = new JMenuItem(displaySensorsRankingAction);

		select.add(findTopKBusyWorkers);
		select.add(displaySensorsRanking);


		menuBar.add(insert);
		menuBar.add(update);
		menuBar.add(select);
		frame.setJMenuBar(menuBar);

		// This TableModel will hold the contents of an entire table, read from the underlying database.
		// A JTable component will display this information whenever the user selects a database table to display.
		CustomTableModel tableModel = null;
		try
		{
			tableModel = new CustomTableModel(model.fetchForest());
		}
		catch(SQLException e)
		{
			displayException(e);
		}
		databaseViewer = new JTable();
		databaseViewer.setModel(tableModel);
		JScrollPane scrollPane = new JScrollPane(databaseViewer);
		scrollPane.setPreferredSize(new Dimension(700, 200));
		databaseViewer.setFillsViewportHeight(true);

		// Add the JTable, its label, its refresh button, and the table selector drop-down menu to the GUI
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

		// Display the main window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * Present a dialog box to the user that takes input in several labeled text fields. Then, call a Model method
	 * and feed it the user-entered information as arguments.
	 * @param methodReference the Model method to call after the user finishes entering information
	 * @param title the title shown on the displayed dialog box. This will be similar to the text of the menu item that
	 *              triggered this dialog box
	 * @param labelsAndFormats the array of LabelAndFormat objects that specify each text field's label and also its
	 *                         Format object, which controls the type of input the text field accepts
	 * @param affirmativeOptionText the label for the affirmative button shown on the dialog
	 * @param hasCancelButton whether the dialog shows a Cancel button in addition to the affirmative button. Only the
	 *                        initial database login dialog lacks the Cancel button
	 * @param <R> the return type of the Model method that is called
	 * @return the confirmation information returned by the called Model method, if it is successful
	 */
	private <R> R callMethodViaCustomDialog(FunctionThrowsException<R> methodReference,
												   String title, LabelAndFormat[] labelsAndFormats,
												   String affirmativeOptionText, boolean hasCancelButton)
	{
		// Generate the dialog box according to the arguments to this function
		R toReturn = null;
		LabeledTextFieldDialog dialog = new LabeledTextFieldDialog(
				frame,
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
					// while this confirmation dialog is shown, user 2 on a different terminal tries to update the
					// same row as user 1 is about to update, user 2's attempt will fail.
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

						int choice = JOptionPane.showConfirmDialog(frame, sb.toString(), "Confirm entered data",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

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
					displayException(e);
					if(dialog.isDummyDialog())
					{
						// if we were trying to do this operation without showing a dialog box and an error occurred,
						// just stop trying to do the operation.
						break;
					}
					dialog.revive();
				}
			}
			else //user hit Cancel, or X'd out the dialog
			{
				break;
			}
		}

		dialog.dispose();
		return toReturn;
	}

	/**
	 * Show a dialog box with an "error" icon, and information about an exception that was thrown.
	 * @param e the exception to be displayed
	 */
	private void displayException(Exception e)
	{
		JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Process a user action received by the View object. The only actions the View object receives are those created
	 * by the user selecting a table to display from the drop-down table selection menu.
	 * @param e the user action to process
	 */
	public void actionPerformed(ActionEvent e) {
		String selectedTable = (String) cb.getSelectedItem();

		ResultSet newData = null;
		try
		{
			// fetch the appropriate table's data from the Model
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

			// display this new information in the GUI's JTable
			databaseViewer.setModel(new CustomTableModel(newData));
		}
		catch(SQLException exception)
		{
			displayException(exception);
		}
	}

	/**
	 * Entrypoint for the application.
	 * @param args (ignored)
	 */
	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(View::new);
	}

	/**
	 * An object of this class processes user actions that affect the entire main window, specifically,
	 * closing the window.
	 */
	private class ModelWindowAdapter extends WindowAdapter
	{
		/**
		 * When the main window is closed by the user, close the connection to the underlying database.
		 * @param e the window closing event
		 */
		public void windowClosing(WindowEvent e)
		{
			try
			{
				model.closeConnection();
			}
			catch(SQLException exception)
			{
				displayException(exception);
			}
		}
	}

	/**
	 * An object of this class processes the event generated when the user clicks the "Refresh" button for the
	 * displayed table.
	 */
	private static class RefreshButtonAction extends AbstractAction
	{
		View v;

		public RefreshButtonAction(View v)
		{
			super("Refresh");
			this.v = v;
		}

		/**
		 * Reload the currently selected table.
		 * @param a the event generated by the user clicking the "Refresh" button
		 */
		public void actionPerformed(ActionEvent a)
		{
			// Just call the same method that gets called when a new selection is made in the table selection
			// drop down menu
			v.actionPerformed(null);
		}
	}

	/**
	 * Objects of this class represent the action triggered by a GUI menu item when clicked.
	 * @param <R> the return type of the Model method that is called when clicking this menu item
	 */
	private class MenuItemAction<R> extends AbstractAction
	{
		private final FunctionThrowsException<R> methodToCall;
		private final String dialogTitle;
		private final LabelAndFormat[] dialogLabelsAndFormats;
		private final String dialogAffirmativeOptionText;
		private final boolean dialogHasCancelButton;

		/**
		 * @param text the text of the menu item containing this MenuItemAction
		 * @param methodToCall the Model method to call when the user clicks this menu item
		 * @param dialogTitle the title shown on the displayed dialog box. This will be similar to "text"
		 * @param dialogLabelsAndFormats the array of LabelAndFormat objects that specify the dialog box's text field's
		 *                               labels and also their Format objects, which control the type of input the
		 *                               text fields accept
		 * @param dialogAffirmativeOptionText the label for the affirmative button shown on the dialog
		 * @param dialogHasCancelButton whether the dialog shows a Cancel button in addition to the affirmative button.
		 *                              Only the initial database login dialog lacks the Cancel button
		 */
		public MenuItemAction(String text, FunctionThrowsException<R> methodToCall, String dialogTitle,
							  LabelAndFormat[] dialogLabelsAndFormats, String dialogAffirmativeOptionText,
							  boolean dialogHasCancelButton)
		{
			super(text);
			this.methodToCall = methodToCall;
			this.dialogTitle = dialogTitle;
			this.dialogLabelsAndFormats = dialogLabelsAndFormats;
			this.dialogAffirmativeOptionText = dialogAffirmativeOptionText;
			this.dialogHasCancelButton = dialogHasCancelButton;
		}

		/**
		 * This method is called when the user clicks on the menu item containing this MenuItemAction object.
		 * It calls the specified Model method and, if successful, it display's the Model method's confirmation
		 * message.
		 * @param a the user's click action
		 */
		public void actionPerformed(ActionEvent a)
		{
			String title;
			Object toDisplay;
			int messageType;
			R result = callMethodViaCustomDialog(methodToCall, dialogTitle, dialogLabelsAndFormats,
					dialogAffirmativeOptionText, dialogHasCancelButton);

			if(result != null) // the method succeeded, so show confirmation
			{
				if(result instanceof ResultSet)
				{
					// the "confirmation message" is actually a table full of data, as with the "Find Top-k Busy Workers"
					// and "Display Sensors Ranking" methods
					try
					{
						TableModel model = new CustomTableModel((ResultSet) result);
						JTable table = new JTable(model);
						toDisplay = new JScrollPane(table);
					}
					catch(SQLException e)
					{
						displayException(e);
						return;
					}

					messageType = JOptionPane.PLAIN_MESSAGE;
					title = "Data";
				}
				else
				{
					// the confirmation message is a simple string
					toDisplay = result;
					messageType = JOptionPane.INFORMATION_MESSAGE;
					title = "Confirmation";
				}
				JOptionPane.showMessageDialog(frame, toDisplay, title, messageType);
			}
		}
	}
}
