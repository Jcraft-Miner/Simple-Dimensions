package lumien.simpledimensions.client.gui;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import lumien.simpledimensions.network.PacketHandler;
import lumien.simpledimensions.network.messages.MessageCreateDimension;
import lumien.simpledimensions.util.WorldInfoSimple;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCreateDimension extends GuiScreen
{
	private Gui parentScreen;
	private GuiTextField dimensionNameTextField;
	private GuiTextField seedTextField;
	private String saveDirName;
	private String gameType = "survival";
	private String savedGameMode;

	private boolean generateStructures = true;
	private boolean allowCheats;
	private boolean bonusChest;
	private boolean hardcore;
	private boolean alreadyGenerated;
	private boolean userInMoreOptions;

	private GuiButton btnMoreOptions;
	private GuiButton btnStructures;
	private GuiButton btnDimensionType;
	private GuiButton btnCustomizeType;
	private GuiButton btnEnvironmentType;

	private String worldSeed;
	private String worldName;
	private int selectedIndex;
	private int selectedEnvironmentIndex;
	public String chunkProviderSettingsJson = "";
	/** These filenames are known to be restricted on one or more OS's. */
	private static final String[] disallowedFilenames = new String[] { "CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };

	public GuiCreateDimension(Gui parent)
	{
		this.parentScreen = parent;
		this.worldSeed = "";
		this.worldName = I18n.format("simpleDimensions.newDimension", new Object[0]);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen()
	{
		this.dimensionNameTextField.updateCursorCounter();
		this.seedTextField.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("simpleDimensions.create", new Object[0])));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
		this.buttonList.add(this.btnMoreOptions = new GuiButton(3, this.width / 2 - 75, 187, 150, 20, I18n.format("simpleDimensions.moreDimensionOptions", new Object[0])));
		this.buttonList.add(this.btnStructures = new GuiButton(4, this.width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures", new Object[0])));
		this.btnStructures.visible = false;
		this.buttonList.add(this.btnDimensionType = new GuiButton(5, this.width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType", new Object[0])));
		this.btnDimensionType.visible = false;
		this.buttonList.add(this.btnCustomizeType = new GuiButton(8, this.width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType", new Object[0])));
		this.btnCustomizeType.visible = false;
		this.buttonList.add(this.btnEnvironmentType = new GuiButton(11, this.width / 2 - 155, 151, 150, 20, I18n.format("simpleDimensions.environmentType", new Object[0])));
		this.btnEnvironmentType.visible = false;
		this.dimensionNameTextField = new GuiTextField(9, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
		this.dimensionNameTextField.setFocused(true);
		this.dimensionNameTextField.setText(this.worldName);
		this.seedTextField = new GuiTextField(10, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
		this.seedTextField.setText(this.worldSeed);
		this.showMoreWorldOptions(this.userInMoreOptions);
		this.calcSaveDirName();
		this.updateDisplayState();
	}

	private void calcSaveDirName()
	{
		this.saveDirName = this.dimensionNameTextField.getText().trim();
		char[] achar = ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS;
		int i = achar.length;

		for (int j = 0; j < i; ++j)
		{
			char c0 = achar[j];
			this.saveDirName = this.saveDirName.replace(c0, '_');
		}

		if (StringUtils.isEmpty(this.saveDirName))
		{
			this.saveDirName = "World";
		}

		this.saveDirName = getUncollidingSaveDirName(this.mc.getSaveLoader(), this.saveDirName);
	}

	private static String getDisplayableName(String input)
	{
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;

		for (char c : input.replace("_", " ").toCharArray())
		{
			if (Character.isSpaceChar(c))
			{
				nextTitleCase = true;
			}
			else if (nextTitleCase)
			{
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}

			titleCase.append(c);
		}

		return titleCase.toString();
	}

	private void updateDisplayState()
	{
		this.btnStructures.displayString = I18n.format("selectWorld.mapFeatures", new Object[0]) + " ";

		if (this.generateStructures)
		{
			this.btnStructures.displayString = this.btnStructures.displayString + I18n.format("options.on", new Object[0]);
		}
		else
		{
			this.btnStructures.displayString = this.btnStructures.displayString + I18n.format("options.off", new Object[0]);
		}

		this.btnDimensionType.displayString = I18n.format("selectWorld.mapType", new Object[0]) + " " + I18n.format(WorldType.WORLD_TYPES[this.selectedIndex].getTranslationKey(), new Object[0]);
		this.btnEnvironmentType.displayString = I18n.format("simpleDimensions.environmentType", new Object[0]) + " " + I18n.format(getDisplayableName(DimensionType.values()[this.selectedEnvironmentIndex].getName()), new Object[0]);
	}

	public static String getUncollidingSaveDirName(ISaveFormat saveLoader, String name)
	{
		name = name.replaceAll("[\\./\"]", "_");
		String[] astring = disallowedFilenames;
		int i = astring.length;

		for (int j = 0; j < i; ++j)
		{
			String s1 = astring[j];

			if (name.equalsIgnoreCase(s1))
			{
				name = "_" + name + "_";
			}
		}

		while (saveLoader.getWorldInfo(name) != null)
		{
			name = name + "-";
		}

		return name;
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat
	 * events
	 */
	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button.enabled)
		{
			if (button.id == 1)
			{
				this.mc.displayGuiScreen(null); // (this.parentScreen);
			}
			else if (button.id == 0)
			{
				this.mc.displayGuiScreen((GuiScreen) null);

				if (this.alreadyGenerated)
				{
					return;
				}

				this.alreadyGenerated = true;
				long i = (new Random()).nextLong();
				String s = this.seedTextField.getText();

				if (!StringUtils.isEmpty(s))
				{
					try
					{
						long j = Long.parseLong(s);

						if (j != 0L)
						{
							i = j;
						}
					}
					catch (NumberFormatException numberformatexception)
					{
						i = s.hashCode();
					}
				}

				WorldType.WORLD_TYPES[this.selectedIndex].onGUICreateWorldPress();

				GameType gametype = GameType.getByName(this.gameType);
				WorldSettings worldsettings = new WorldSettings(i, gametype, this.generateStructures, this.hardcore, WorldType.WORLD_TYPES[this.selectedIndex]);
				worldsettings.setGeneratorOptions(this.chunkProviderSettingsJson);

				if (this.bonusChest && !this.hardcore)
				{
					worldsettings.enableBonusChest();
				}

				if (this.allowCheats && !this.hardcore)
				{
					worldsettings.enableCommands();
				}

				WorldInfoSimple worldInfo = new WorldInfoSimple(worldsettings, this.dimensionNameTextField.getText().trim(), DimensionType.values()[this.selectedEnvironmentIndex]);
				MessageCreateDimension createMessage = new MessageCreateDimension(worldInfo);

				PacketHandler.INSTANCE.sendToServer(createMessage);
			}
			else if (button.id == 3)
			{
				this.toggleMoreWorldOptions();
			}
			else if (button.id == 4)
			{
				this.generateStructures = !this.generateStructures;
				this.updateDisplayState();
			}
			else if (button.id == 5)
			{
				++this.selectedIndex;

				if (this.selectedIndex >= WorldType.WORLD_TYPES.length)
				{
					this.selectedIndex = 0;
				}

				while (!this.canSelectCurWorldType())
				{
					++this.selectedIndex;

					if (this.selectedIndex >= WorldType.WORLD_TYPES.length)
					{
						this.selectedIndex = 0;
					}
				}

				this.chunkProviderSettingsJson = "";
				this.updateDisplayState();
				this.showMoreWorldOptions(this.userInMoreOptions);
			}
			else if (button.id == 11)
			{
				++this.selectedEnvironmentIndex;

				if (this.selectedEnvironmentIndex >= DimensionType.values().length)
				{
					this.selectedEnvironmentIndex = 0;
				}

				while (!this.canSelectCurEnvironmentType())
				{
					++this.selectedEnvironmentIndex;

					if (this.selectedEnvironmentIndex >= DimensionType.values().length)
					{
						this.selectedEnvironmentIndex = 0;
					}
				}

				this.updateDisplayState();
				this.showMoreWorldOptions(this.userInMoreOptions);
			}
			else if (button.id == 8)
			{
				if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.FLAT)
				{
					mc.displayGuiScreen(new GuiCreateFlatDimension(this, this.chunkProviderSettingsJson));
				}
				else if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.CUSTOMIZED)
				{
					mc.displayGuiScreen(new GuiCustomizeDimension(this, this.chunkProviderSettingsJson));
				}
			}
		}
	}

	private boolean canSelectCurWorldType()
	{
		WorldType worldtype = WorldType.WORLD_TYPES[this.selectedIndex];
		return worldtype != null && worldtype.canBeCreated() ? (worldtype == WorldType.DEBUG_ALL_BLOCK_STATES ? isShiftKeyDown() : true) : false;
	}

	private boolean canSelectCurEnvironmentType()
	{
		DimensionType environmenttype = DimensionType.values()[this.selectedEnvironmentIndex];

		return true;
	}

	private void toggleMoreWorldOptions()
	{
		this.showMoreWorldOptions(!this.userInMoreOptions);
	}

	private void showMoreWorldOptions(boolean toggle)
	{
		this.userInMoreOptions = toggle;

		if (WorldType.WORLD_TYPES[this.selectedIndex] == WorldType.DEBUG_ALL_BLOCK_STATES)
		{
			if (this.savedGameMode == null)
			{
				this.savedGameMode = this.gameType;
			}

			this.gameType = "spectator";
			this.btnStructures.visible = false;
			this.btnDimensionType.visible = this.userInMoreOptions;
			this.btnCustomizeType.visible = false;
			this.btnEnvironmentType.visible = false;
		}
		else
		{
			if (this.savedGameMode != null)
			{
				this.gameType = this.savedGameMode;
				this.savedGameMode = null;
			}

			this.btnStructures.visible = this.userInMoreOptions && WorldType.WORLD_TYPES[this.selectedIndex] != WorldType.CUSTOMIZED;
			this.btnDimensionType.visible = this.userInMoreOptions;
			this.btnCustomizeType.visible = this.userInMoreOptions && WorldType.WORLD_TYPES[this.selectedIndex].isCustomizable();
			this.btnEnvironmentType.visible = this.userInMoreOptions;
		}

		this.updateDisplayState();

		if (this.userInMoreOptions)
		{
			this.btnMoreOptions.displayString = I18n.format("gui.done", new Object[0]);
		}
		else
		{
			this.btnMoreOptions.displayString = I18n.format("simpleDimensions.moreDimensionOptions", new Object[0]);
		}
	}

	/**
	 * Fired when a key is typed (except F11 who toggle full screen). This is
	 * the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character
	 * (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
		if (this.dimensionNameTextField.isFocused() && !this.userInMoreOptions)
		{
			this.dimensionNameTextField.textboxKeyTyped(typedChar, keyCode);
			this.worldName = this.dimensionNameTextField.getText();
		}
		else if (this.seedTextField.isFocused() && this.userInMoreOptions)
		{
			this.seedTextField.textboxKeyTyped(typedChar, keyCode);
			this.worldSeed = this.seedTextField.getText();
		}

		if (keyCode == 28 || keyCode == 156)
		{
			this.actionPerformed(this.buttonList.get(0));
		}

		this.buttonList.get(0).enabled = this.dimensionNameTextField.getText().length() > 0;
		this.calcSaveDirName();
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.userInMoreOptions)
		{
			this.seedTextField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		else
		{
			this.dimensionNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, I18n.format("simpleDimensions.create", new Object[0]), this.width / 2, 20, -1);

		if (this.userInMoreOptions)
		{
			this.drawString(this.fontRenderer, I18n.format("selectWorld.enterSeed", new Object[0]), this.width / 2 - 100, 47, -6250336);
			this.drawString(this.fontRenderer, I18n.format("selectWorld.seedInfo", new Object[0]), this.width / 2 - 100, 85, -6250336);

			if (this.btnStructures.visible)
			{
				this.drawString(this.fontRenderer, I18n.format("selectWorld.mapFeatures.info", new Object[0]), this.width / 2 - 150, 122, -6250336);
			}

			if (this.btnEnvironmentType.visible)
			{
				this.drawString(this.fontRenderer, I18n.format("simpleDimensions.environmentType.info", new Object[0]), this.width / 2 - 150, 172, -6250336);
			}

			this.seedTextField.drawTextBox();

			if (WorldType.WORLD_TYPES[this.selectedIndex].hasInfoNotice())
			{
				this.fontRenderer.drawSplitString(I18n.format(WorldType.WORLD_TYPES[this.selectedIndex].getInfoTranslationKey(), new Object[0]), this.btnDimensionType.x + 2, this.btnDimensionType.y + 22, this.btnDimensionType.getButtonWidth(), 10526880);
			}
		}
		else
		{
			this.drawString(this.fontRenderer, I18n.format("simpleDimensions.enterName", new Object[0]), this.width / 2 - 100, 47, -6250336);
			this.drawString(this.fontRenderer, I18n.format("selectWorld.resultFolder", new Object[0]) + " " + this.saveDirName, this.width / 2 - 100, 85, -6250336);
			this.dimensionNameTextField.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public void recreateFromExistingWorld(WorldInfo original)
	{
		this.worldName = I18n.format("selectWorld.newWorld.copyOf", new Object[] { original.getWorldName() });
		this.worldSeed = original.getSeed() + "";
		this.selectedIndex = original.getTerrainType().getId();
		this.chunkProviderSettingsJson = original.getGeneratorOptions();
		this.generateStructures = original.isMapFeaturesEnabled();
		this.allowCheats = original.areCommandsAllowed();

		if (original.isHardcoreModeEnabled())
		{
			this.gameType = "hardcore";
		}
		else if (original.getGameType().isSurvivalOrAdventure())
		{
			this.gameType = "survival";
		}
		else if (original.getGameType().isCreative())
		{
			this.gameType = "creative";
		}
	}
}