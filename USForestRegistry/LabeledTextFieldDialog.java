package USForestRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

/**
 * A custom JDialog that allows for configurable buttons, and text fields for user input.
 */
class LabeledTextFieldDialog extends JDialog implements ActionListener, PropertyChangeListener
{
	private static final String CANCEL = "Cancel";

	private JTextField[] textFields = null;
	private JOptionPane optionPane;
	private String[] options = null;
	private String affirmativeOptionText = null;
	private LinkedHashMap<String, String> labelToTypedText = null;
	private boolean isDummyDialog;

	/**
	 * Create and show the dialog box.
	 * @param owner the Java Swing component that is the parent of this LabeledTextFieldDialog
	 * @param title the title shown on the dialog box
	 * @param labelsAndFormats the array of LabelAndFormat objects that specify each text field's label and also its
	 * 	                       Format object, which controls the type of input the text field accepts
	 * @param affirmativeOptionText the label for the affirmative button shown on the dialog
	 * @param hasCancelButton whether the dialog shows a Cancel button in addition to the affirmative button
	 */
	public LabeledTextFieldDialog(Frame owner, String title, LabelAndFormat[] labelsAndFormats,
								  String affirmativeOptionText, boolean hasCancelButton)
	{
		super(owner, title, true);

		if(labelsAndFormats == null)
		{
			// In this case, this dialog box isn't meant to be shown (because no user entered info is required),
			// but rather just exist invisibly and act as if the user pressed the affirmative button on it
			optionPane = new JOptionPane();
			optionPane.setValue(affirmativeOptionText);
			isDummyDialog = true;
			return;
		}
		this.affirmativeOptionText = affirmativeOptionText;

		// Create text fields with specified formatting, and group them with their corresponding labels
		textFields = new JTextField[labelsAndFormats.length];
		labelToTypedText = new LinkedHashMap<>();
		JPanel row;
		JPanel[] labeledTextFields = new JPanel[labelsAndFormats.length];
		for(int i=0; i<labelsAndFormats.length; ++i)
		{
			// This "row" will contain the label, a tooltip icon if applicable, and the text field for user input
			row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
			row.add(new JLabel(labelsAndFormats[i].label + " "));

			if(labelsAndFormats[i].format == null)
			{
				textFields[i] = new JTextField();
			}
			else
			{
				Format formatter = labelsAndFormats[i].format;
				textFields[i] = new JFormattedTextField(formatter);
				if(formatter instanceof SimpleDateFormat)
				{
					// If this text field is meant to take date/time information, it requires an extra icon with
					// a mouseable tooltip to explain the expected format of the date/time information
					JLabel iconLabel = new JLabel("ⓘ ");
					iconLabel.setToolTipText("Format: yyyy-mm-dd hh:mm:ss");
					row.add(iconLabel);

					// Ensure the text field has enough space on screen to display all user-entered info
					textFields[i].setColumns(13);
				}

				// This allows the user to hit the Enter key to submit the entire form when the cursor is in this text field
				textFields[i].addActionListener(this);
			}

			// Store the labels for future use, to be paired with the user-entered text for each labeled field
			labelToTypedText.put(labelsAndFormats[i].label, null);

			row.add(textFields[i]);
			labeledTextFields[i] = row;
		}

		//Put the labels, text fields, and option buttons all together
		if(hasCancelButton)
		{
			options = new String[]{affirmativeOptionText, CANCEL};
		}
		else
		{
			options = new String[]{affirmativeOptionText};
		}
		optionPane = new JOptionPane(labeledTextFields,
					     JOptionPane.PLAIN_MESSAGE,
					     JOptionPane.YES_NO_OPTION,
					     null,
					     options,
					     options[0]);
		
		optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.PAGE_AXIS));
		setContentPane(optionPane);

		// The first text field should have the first focus
		addComponentListener(new ComponentAdapter()
							 {
								 public void componentShown(ComponentEvent ce)
								 {
									 textFields[0].requestFocusInWindow();
								 }
							 }
							 );

		// This event handler verifies input entered by the user.
		optionPane.addPropertyChangeListener(this);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * @return whether this dialog box is just meant to exist invisibly and act as if the user pressed the
	 * affirmative button on it
	 */
	public boolean isDummyDialog()
	{
		return isDummyDialog;
	}

	/**
	 * @return the button that the user clicked on the dialog box
	 */
	public Object getOptionPaneValue()
	{
		return optionPane.getValue();
	}

	/**
	 * @return a mapping of field labels to the user-entered input for that field
	 */
	public LinkedHashMap<String, String> getLabelToTypedText()
	{
		return labelToTypedText;
	}

	/**
	 * Make an invisible dialog box visible again, and reset its internal value so that an event will fire when the
	 * user clicks a new button.
	 */
	public void revive()
	{
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		setVisible(true);
	}

	/**
	 * Called when the user hits Enter to submit the information in the dialog box
	 * @param e the user's keyboard event
	 */
	public void actionPerformed(ActionEvent e)
	{
		optionPane.setValue(options[0]); //set to the affirmative button
	}

	/**
	 * Check that all fields have acceptable values in them, and create a mapping of the field labels to the
	 * user-entered text for that field.
	 * @param e the event caused by the user clicking a button on the dialog box
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		String prop = e.getPropertyName();

        if ((e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop)
				|| JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
		{
			Object value = optionPane.getValue();
			if (value.equals(affirmativeOptionText)) //user hit the affirmative button
			{
				//Make sure all formatted boxes (which will be converted to something that isn't text)
				// have a nonempty and non-whitespace string in them
				boolean allValuesFilled = true;
				int index = 0;
				for(String label : labelToTypedText.keySet())
				{
					String textOfThisField = textFields[index].getText();
					if(textFields[index] instanceof JFormattedTextField)
					{
							if(textOfThisField == null || textOfThisField.trim().equals(""))
							{
								// If the user left one of the formatted text fields empty,
								// show a warning message about the box that needs a value, and let the user fill it in
								JOptionPane.showMessageDialog(this, "Please enter a value for \"" + label + "\"",
										"Missing value", JOptionPane.WARNING_MESSAGE);
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
								allValuesFilled = false;
								break;
							}
					}
					else if(textOfThisField.trim().equals(""))
					{
						// It's an unformatted string field, and it's empty, so interpret it as a SQL null value
						textOfThisField = null;
					}

					// Copy the user-entered text into the LinkedHashMap, paired with the field label
					labelToTypedText.put(label, textOfThisField);
					++index;
				}

				if(allValuesFilled)
				{
					setVisible(false);
				}
			}
			else if(value.equals(CANCEL)) //user hit the Cancel button
			{
				setVisible(false);
			}
		}
	}
}
