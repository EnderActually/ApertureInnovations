package net.mistersecret312.aperture_innovations.compat.iris;

import net.irisshaders.iris.Iris;
import net.minecraftforge.fml.ModList;

public class IrisCompat
{
	public static boolean isIrisLoaded()
	{
		return ModList.get().isLoaded("oculus");
	}

	public static boolean areShadersOn()
	{
		return Iris.getCurrentPack().isPresent();
	}
}
