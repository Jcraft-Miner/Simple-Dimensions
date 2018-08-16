package lumien.simpledimensions.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class SimpleDimensionsConfig
{
	Configuration config;

	int startDimensionID;

	public void preInit(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		startDimensionID = config.get("Settings", "StartDimensionID", 30000, "Where should SimpleDimensions start to search for free Dimension IDs?").getInt();

		if (config.hasChanged())
		{
			config.save();
		}
	}

	public int startDimensionID()
	{
		return startDimensionID;
	}
}
