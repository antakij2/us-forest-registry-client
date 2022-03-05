package USForestRegistry;

import java.awt.*;
import javax.swing.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import static USForestRegistry.AttributeNames.*;

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

		//Create the menu bar and contained menu items.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);

		JMenu insert = new JMenu("INSERT");
		JMenuItem addForest = new JMenuItem("Add Forest..."); //TODO: make these strings constants, so they can be the titles of the dialog boxes they create
		JMenuItem addWorker = new JMenuItem("Add Worker...");
		JMenuItem addSensor = new JMenuItem("Add Sensor...");
		insert.add(addForest);
		insert.add(addWorker);
		insert.add(addSensor);

		JMenu update = new JMenu("UPDATE");
		JMenuItem switchWorkersDuties = new JMenuItem("Switch Workers' Duties...");
		JMenuItem updateSensorStatus = new JMenuItem("Update Sensor Status...");
		JMenuItem updateForestCoveredArea = new JMenuItem("Update Forest Covered Area...");
		update.add(switchWorkersDuties);
		update.add(updateSensorStatus);
		update.add(updateForestCoveredArea);

		JMenu select = new JMenu("SELECT");
		JMenuItem findTopKBusyWorkers = new JMenuItem("Find Top K Busy Workers...");
		JMenuItem displaySensorsRanking = new JMenuItem("Display Sensors Ranking");
		select.add(findTopKBusyWorkers);
		select.add(displaySensorsRanking);

		menuBar.add(insert);
		menuBar.add(update);
		menuBar.add(select);
		frame.setJMenuBar(menuBar);

		//Create table viewing area.
		//TODO: frame.getContentPane().add(tableViewingArea, BorderLayout.CENTER);

		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// Show the initial database connection/login dialog, and connect to the database
		LabelAndFormat[] connectLabelsAndFormats = new LabelAndFormat[]
				{
						new LabelAndFormat(HOSTNAME, null),
						new LabelAndFormat(PORT, NumberFormat.getIntegerInstance()),
						new LabelAndFormat(DATABASE_NAME, null),
						new LabelAndFormat(USERNAME, null),
						new LabelAndFormat(PASSWORD, null)
				};
		Model model = callMethodViaCustomDialog(Model::new, frame, "Connect to Database",
			connectLabelsAndFormats, "Connect", false);
	}

	private static <R> R callMethodViaCustomDialog(FunctionThrowsSQLException<R> methodReference, Frame owner,
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
					//TODO: enable menu bar if instantiating model -- that's in a View wrapper function
					break; //showConfirmation=true, or just show it here
				}
				catch(SQLException e)
				{
					JOptionPane.showMessageDialog(dialog, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					dialog.revive();
				}
			}
			else //user hit Cancel, or X'd out the window
			{
				break; //showConfirmation=false
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
}
