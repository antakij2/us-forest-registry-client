package USForestRegistry;

import java.awt.*;
import javax.swing.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.function.Supplier;
import java.util.Collection;

public class View 
{
	/*
	* Create the GUI and show it. 
	*/
	private static void createAndShowGUI() 
	{
		//Create and set up the window.
		JFrame frame = new JFrame("U.S. Forest Registry");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//Create the menu bar. Make it have a green background.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setBackground(new Color(154, 165, 127));
		menuBar.setPreferredSize(new Dimension(200, 20));
 
		//Set the menu bar and add the label to the content pane.
		frame.setJMenuBar(menuBar);
		//frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		Model model = null;
		String loginAffirmativeOptionText = "Connect";
		LabeledTextFieldDialog login = new LabeledTextFieldDialog(
				frame,
				"Connect to Database",
				new LabelAndFormat[]
						{
								new LabelAndFormat("hostname", null),
								new LabelAndFormat("port", NumberFormat.getIntegerInstance()),
								new LabelAndFormat("database name", null),
								new LabelAndFormat("username", null),
								new LabelAndFormat("password", null)
						},
				loginAffirmativeOptionText,
				false
		);

		while(true)
		{
			if(login.getOptionPaneValue() == loginAffirmativeOptionText)
			{
				try
				{
					model = new Model(login.getLabelToTypedText());
					model.closeConnection();
					//TODO: enable menu bar
					break;
				}
				catch(SQLException e)
				{
					JOptionPane.showMessageDialog(login, e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					login.revive();
				}
			}
			else //user hit Cancel, or X'd out the window
			{
				break;
			}
		}
		login.dispose();
	}

	public static <T>
	Collection<T> transferElements(Collection<T> sourceCollection, Supplier<Collection<T>> collectionFactory)
	{
		Collection<T> result = collectionFactory.get();
		for (T t : sourceCollection) {
			result.add(t);
		}
		return result;
	}



	private static Object callModelMethodViaCustomDialog(Frame owner, String title,
														 LabelAndFormat[] labelsAndFormats, String affirmativeOptionText,
														 boolean hasCancelButton) //TODO: first arg is method reference to call
	{
		LabeledTextFieldDialog dialog = new LabeledTextFieldDialog(
				owner,
				title,
				labelsAndFormats,
				affirmativeOptionText,
				hasCancelButton
		);

		while(true)
		{
			if(dialog.getOptionPaneValue() == affirmativeOptionText) //user hit the affirmative button
			{
				try
				{
					//TODO: this method returns a model, or null. So keep return type as Object to keep it neutral
					model = new Model(login.getTypedText());
					//TODO: enable menu bar
					break; //showConfirmation=true
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
