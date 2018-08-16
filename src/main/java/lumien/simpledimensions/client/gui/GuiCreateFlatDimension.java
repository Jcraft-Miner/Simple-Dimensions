package lumien.simpledimensions.client.gui;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCreateFlatDimension extends GuiScreen
{
	private final GuiCreateDimension createDimensionGui;
	private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
	/** The title given to the flat world currently in creation */
	private String flatWorldTitle;
	/** The text used to identify the material for a layer */
	private String materialText;
	/** The text used to identify the height of a layer */
	private String heightText;
	private GuiCreateFlatDimension.Details createFlatWorldListSlotGui;
	/** The (unused and permenantly hidden) add layer button */
	private GuiButton addLayerButton;
	/** The (unused and permenantly hidden) edit layer button */
	private GuiButton editLayerButton;
	/** The remove layer button */
	private GuiButton removeLayerButton;

	public GuiCreateFlatDimension(GuiCreateDimension createWorldGuiIn, String preset)
	{
		this.createDimensionGui = createWorldGuiIn;
		this.setPreset(preset);
	}

	/**
	 * Gets the superflat preset in the text format described on the Superflat
	 * article on the Minecraft Wiki
	 */
	public String getPreset()
	{
		return this.theFlatGeneratorInfo.toString();
	}

	/**
	 * Sets the superflat preset. Invalid or null values will result in the
	 * default superflat preset being used.
	 * 
	 * @param preset
	 *            The new preset to use in the format described on the Superflat
	 *            article on the Minecraft Wiki
	 */
	public void setPreset(String preset)
	{
		this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(preset);
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called
	 * when the GUI is displayed and when the window resizes, the buttonList is
	 * cleared beforehand.
	 */
	@Override
	public void initGui()
	{
		this.buttonList.clear();
		this.flatWorldTitle = I18n.format("createWorld.customize.flat.title", new Object[0]);
		this.materialText = I18n.format("createWorld.customize.flat.tile", new Object[0]);
		this.heightText = I18n.format("createWorld.customize.flat.height", new Object[0]);
		this.createFlatWorldListSlotGui = new GuiCreateFlatDimension.Details();
		this.buttonList.add(this.addLayerButton = new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer", new Object[0]) + " (NYI)"));
		this.buttonList.add(this.editLayerButton = new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer", new Object[0]) + " (NYI)"));
		this.buttonList.add(this.removeLayerButton = new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer", new Object[0])));
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
		this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets", new Object[0])));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
		this.addLayerButton.visible = this.editLayerButton.visible = false;
		this.theFlatGeneratorInfo.updateLayers();
		this.onLayersChanged();
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		this.createFlatWorldListSlotGui.handleMouseInput();
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed
	 * for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		int i = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.selectedLayer - 1;

		if (button.id == 1)
		{
			this.mc.displayGuiScreen(this.createDimensionGui);
		}
		else if (button.id == 0)
		{
			this.createDimensionGui.chunkProviderSettingsJson = this.getPreset();
			this.mc.displayGuiScreen(this.createDimensionGui);
		}
		else if (button.id == 5)
		{
			this.mc.displayGuiScreen(new GuiFlatDimensionPresets(this));
		}
		else if (button.id == 4 && this.hasSelectedLayer())
		{
			this.theFlatGeneratorInfo.getFlatLayers().remove(i);
			this.createFlatWorldListSlotGui.selectedLayer = Math.min(this.createFlatWorldListSlotGui.selectedLayer, this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
		}

		this.theFlatGeneratorInfo.updateLayers();
		this.onLayersChanged();
	}

	/**
	 * Would update whether or not the edit and remove buttons are enabled, but
	 * is currently disabled and always disables the buttons (which are
	 * invisible anyways)
	 */
	public void onLayersChanged()
	{
		boolean flag = this.hasSelectedLayer();
		this.removeLayerButton.enabled = flag;
		this.editLayerButton.enabled = flag;
		this.editLayerButton.enabled = false;
		this.addLayerButton.enabled = false;
	}

	/**
	 * Returns whether there is a valid layer selection
	 */
	private boolean hasSelectedLayer()
	{
		return this.createFlatWorldListSlotGui.selectedLayer > -1 && this.createFlatWorldListSlotGui.selectedLayer < this.theFlatGeneratorInfo.getFlatLayers().size();
	}

	/**
	 * Draws the screen and all the components in it.
	 * 
	 * @param mouseX
	 *            Mouse x coordinate
	 * @param mouseY
	 *            Mouse y coordinate
	 * @param partialTicks
	 *            How far into the current tick (1/20th of a second) the game is
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		this.createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRenderer, this.flatWorldTitle, this.width / 2, 8, 16777215);
		int i = this.width / 2 - 92 - 16;
		this.drawString(this.fontRenderer, this.materialText, i, 32, 16777215);
		this.drawString(this.fontRenderer, this.heightText, i + 2 + 213 - this.fontRenderer.getStringWidth(this.heightText), 32, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@SideOnly(Side.CLIENT)
	class Details extends GuiSlot
	{
		/**
		 * The currently selected layer; -1 if there is no selection. This is in
		 * the order that it is displayed on- screen, with the topmost layer
		 * having index 0.
		 */
		public int selectedLayer = -1;

		public Details()
		{
			super(GuiCreateFlatDimension.this.mc, GuiCreateFlatDimension.this.width, GuiCreateFlatDimension.this.height, 43, GuiCreateFlatDimension.this.height - 60, 24);
		}

		/**
		 * Draws an item with a background at the given coordinates. The item
		 * and its background are 20 pixels tall/wide (though only the inner
		 * 18x18 is actually drawn on)
		 * 
		 * @param x
		 *            Display x coordinate; the background is drawn 1 pixel
		 *            further than this and the itemstack is drawn 2 pixels
		 *            further
		 * @param z
		 *            Display z coordinate; the background is drawn 1 pixel
		 *            further than this and the itemstack is drawn 2 pixels
		 *            further
		 * @param itemToDraw
		 *            The item to draw
		 */
		private void drawItem(int x, int z, ItemStack itemToDraw)
		{
			this.drawItemBackground(x + 1, z + 1);
			GlStateManager.enableRescaleNormal();

			if (itemToDraw != null && itemToDraw.getItem() != null)
			{
				RenderHelper.enableGUIStandardItemLighting();
				GuiCreateFlatDimension.this.itemRender.renderItemIntoGUI(itemToDraw, x + 2, z + 2);
				RenderHelper.disableStandardItemLighting();
			}

			GlStateManager.disableRescaleNormal();
		}

		/**
		 * Draws the background icon for an item, with the indented texture from
		 * stats.png
		 * 
		 * @param x
		 *            Display x coordinate
		 * @param y
		 *            Display y coordinate
		 */
		private void drawItemBackground(int x, int y)
		{
			this.drawItemBackground(x, y, 0, 0);
		}

		/**
		 * Draws the background icon for an item, using a texture from stats.png
		 * with the given coords
		 * 
		 * @param x
		 *            Display x coordinate
		 * @param z
		 *            Display z coordinate
		 * @param textureX
		 *            Leftmost coordinate of the icon in stats.png to use; only
		 *            ever set to 0
		 * @param textureY
		 *            Topmost coordinate of the icon in stats.png to use; only
		 *            ever set to 0
		 */
		private void drawItemBackground(int x, int z, int textureX, int textureY)
		{
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.getTextureManager().bindTexture(Gui.STAT_ICONS);
			float f = 0.0078125F;
			float f1 = 0.0078125F;
			int i = 18;
			int j = 18;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(x + 0, z + 18, GuiCreateFlatDimension.this.zLevel).tex((textureX + 0) * 0.0078125F, (textureY + 18) * 0.0078125F).endVertex();
			vertexbuffer.pos(x + 18, z + 18, GuiCreateFlatDimension.this.zLevel).tex((textureX + 18) * 0.0078125F, (textureY + 18) * 0.0078125F).endVertex();
			vertexbuffer.pos(x + 18, z + 0, GuiCreateFlatDimension.this.zLevel).tex((textureX + 18) * 0.0078125F, (textureY + 0) * 0.0078125F).endVertex();
			vertexbuffer.pos(x + 0, z + 0, GuiCreateFlatDimension.this.zLevel).tex((textureX + 0) * 0.0078125F, (textureY + 0) * 0.0078125F).endVertex();
			tessellator.draw();
		}

		@Override
		protected int getSize()
		{
			return GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size();
		}

		/**
		 * The element in the slot that was clicked, boolean for whether it was
		 * double clicked or not
		 */
		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
		{
			this.selectedLayer = slotIndex;
			GuiCreateFlatDimension.this.onLayersChanged();
		}

		/**
		 * Returns true if the element passed in is currently selected
		 */
		@Override
		protected boolean isSelected(int slotIndex)
		{
			return slotIndex == this.selectedLayer;
		}

		@Override
		protected void drawBackground()
		{
		}

		@Override
		protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks)
		{
			FlatLayerInfo flatlayerinfo = GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().get(GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
			IBlockState iblockstate = flatlayerinfo.getLayerMaterial();
			Block block = iblockstate.getBlock();
			Item item = Item.getItemFromBlock(block);
			ItemStack itemstack = block != Blocks.AIR && item != null ? new ItemStack(item, 1, block.getMetaFromState(iblockstate)) : null;
			String s = itemstack == null ? I18n.format("createWorld.customize.flat.air", new Object[0]) : item.getItemStackDisplayName(itemstack);

			if (item == null)
			{
				if (block != Blocks.WATER && block != Blocks.FLOWING_WATER)
				{
					if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
					{
						item = Items.LAVA_BUCKET;
					}
				}
				else
				{
					item = Items.WATER_BUCKET;
				}

				if (item != null)
				{
					itemstack = new ItemStack(item, 1, block.getMetaFromState(iblockstate));
					s = block.getLocalizedName();
				}
			}

			this.drawItem(insideLeft, yPos, itemstack);
			GuiCreateFlatDimension.this.fontRenderer.drawString(s, insideLeft + 18 + 5, yPos + 3, 16777215);
			String s1;

			if (entryID == 0)
			{
				s1 = I18n.format("createWorld.customize.flat.layer.top", new Object[] { Integer.valueOf(flatlayerinfo.getLayerCount()) });
			}
			else if (entryID == GuiCreateFlatDimension.this.theFlatGeneratorInfo.getFlatLayers().size() - 1)
			{
				s1 = I18n.format("createWorld.customize.flat.layer.bottom", new Object[] { Integer.valueOf(flatlayerinfo.getLayerCount()) });
			}
			else
			{
				s1 = I18n.format("createWorld.customize.flat.layer", new Object[] { Integer.valueOf(flatlayerinfo.getLayerCount()) });
			}

			GuiCreateFlatDimension.this.fontRenderer.drawString(s1, insideLeft + 2 + 213 - GuiCreateFlatDimension.this.fontRenderer.getStringWidth(s1), yPos + 3, 16777215);
		}

		@Override
		protected int getScrollBarX()
		{
			return this.width - 70;
		}
	}
}