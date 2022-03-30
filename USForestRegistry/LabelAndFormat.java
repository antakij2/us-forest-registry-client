package USForestRegistry;

import java.text.Format;
import java.text.NumberFormat;

/**
 * Each object of this class specifies the String label and allowed input format for a text field in
 * LabeledTextFieldDialog object.
 */
public class LabelAndFormat
{
	public final String label;
	public final Format format;

	/**
	 * @param label the String label to display before a text field for user input
	 * @param format the formatter for the input in the text field (real number, integer, date/time, etc.)
	 */
	public LabelAndFormat(String label, Format format)
	{
		this.label = label;

		if(format instanceof NumberFormat)
		{
			((NumberFormat) format).setGroupingUsed(false); // don't insert commas to group the entered numbers
		}

		this.format = format;
	}
}
