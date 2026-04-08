package net.mistersecret312.aperture_innovations.multitool;

public abstract class InteractionType
{
	static class Toggle extends InteractionType
	{
		private boolean value;
		public Toggle() {}

		public void toggle()
		{
			this.value = !value;
		}
	}

	static class NumberField extends InteractionType
	{
		private double value;
		public NumberField() {}

		public void setValue(double value)
		{
			this.value = value;
		}

		public double getValue()
		{
			return value;
		}
	}

	static class TextField extends InteractionType
	{
		private String value;

		private final int maxSymbols;
		public TextField(int maxSymbols)
		{
			this.maxSymbols = maxSymbols;
			this.value = "";
		}

		public int getMaxSymbols()
		{
			return maxSymbols;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}
	}

	record Slider(double min, double max, double step){};

}
