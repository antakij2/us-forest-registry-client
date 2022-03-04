package USForestRegistry;

import java.text.Format;
import java.text.NumberFormat;

public class LabelAndFormat
{
	public final String label;
	public final Format format;

	public LabelAndFormat(String label, Format format)
	{
		this.label = label;

		if(format instanceof NumberFormat)
		{
			((NumberFormat) format).setGroupingUsed(false);
		}

		this.format = format;
	}
}
