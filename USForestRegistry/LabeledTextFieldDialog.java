package USForestRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.LinkedHashMap;
//TODO: limit varchar field lengths somehow because error messages when they are too long don't say which field was too long.
// or just include (abbreviation) in string label. Same with YYYY-MM-DD HH:MM:SS format for timestamps.
class LabeledTextFieldDialog extends JDialog implements ActionListener, PropertyChangeListener
{
	private static final String CANCEL = "Cancel";

	private JTextField[] textFields = null;
	private JOptionPane optionPane;
	private String[] options = null;
	private String affirmativeOptionText = null;
	private LinkedHashMap<String, String> labelToTypedText = null;
	private boolean isDummyDialog;

	//TODO: proper password field
	public LabeledTextFieldDialog(Frame owner, String title, LabelAndFormat[] labelsAndFormats,
								  String affirmativeOptionText, boolean hasCancelButton)
	{
		super(owner, title, true);

		if(labelsAndFormats == null)
		{
			optionPane = new JOptionPane();
			optionPane.setValue(affirmativeOptionText);
			isDummyDialog = true;
			return;
		}
		this.affirmativeOptionText = affirmativeOptionText;

		//Create text fields with specified formatting, and group them with their corresponding labels
		textFields = new JTextField[labelsAndFormats.length];
		labelToTypedText = new LinkedHashMap<>();
		JPanel row;
		JPanel[] labeledTextFields = new JPanel[labelsAndFormats.length];
		for(int i=0; i<labelsAndFormats.length; ++i)
		{
			if(labelsAndFormats[i].format == null)
			{
				textFields[i] = new JTextField();
			}
			else
			{
				textFields[i] = new JFormattedTextField(labelsAndFormats[i].format);

				//This allows the user to hit the Enter key to submit the entire form when the cursor is in this text field
				textFields[i].addActionListener(this);
			}

			// Store the labels for future use, to be paired with the user-entered text for each labeled field
			labelToTypedText.put(labelsAndFormats[i].label, null);

			row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
			row.add(new JLabel(labelsAndFormats[i].label));
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

		//The first text field should have the first focus
		addComponentListener(new ComponentAdapter()
							 {
								 public void componentShown(ComponentEvent ce)
								 {
									 textFields[0].requestFocusInWindow();
								 }
							 }
							 );

		//This event handler verifies input entered by the user.
		optionPane.addPropertyChangeListener(this);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public boolean isDummyDialog()
	{
		return isDummyDialog;
	}

	public Object getOptionPaneValue()
	{
		return optionPane.getValue();
	}

	public LinkedHashMap<String, String> getLabelToTypedText()
	{
		return labelToTypedText;
	}

	public void revive()
	{
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		optionPane.setValue(options[0]); //set to the affirmative button
	}

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
					if(textFields[index] instanceof JFormattedTextField &&
							(textOfThisField == null || textOfThisField.trim().equals("")))
					{
						// Show a warning message about the box that needs a value, and let the user fill it in
						JOptionPane.showMessageDialog(this, "Please enter a value for \"" + label + "\"",
								"Missing value", JOptionPane.WARNING_MESSAGE);
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						allValuesFilled = false;
						break;
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
