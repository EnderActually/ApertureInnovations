package net.mistersecret312.aperture_innovations.blocks.enums;

public enum ConnectionState
	{
		NONE("none"),
		LINK("link"),
		SIDE("side"),
		UP("up"),
		DOWN("down");

		public final String name;
		ConnectionState(String name)
		{
			this.name = name;
		}

		public static ConnectionState fromString(String name)
		{
			return switch(name)
			{
				case "link" -> LINK;
				case "side" -> SIDE;
				case "up" -> UP;
				case "down" -> DOWN;

				default -> NONE;
			};

		}
	}