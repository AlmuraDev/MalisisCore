/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.util;

import java.lang.ref.WeakReference;

import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Utility class for {@link TileEntity}.
 *
 * @author Ordinastie
 */
public class TileEntityUtils
{
	/** Reference to the {@link TileEntity} currently being use for current opened {@link MalisisGui}. Weak (as is the gui): nothing clears
	 * them, and they'd otherwise keep the last gui, tile entity and client world alive after leaving the world.<br>
	 * Not {@code @SideOnly}: Forge strips such fields on the server but keeps the static initializer that sets them, which would then fail
	 * with NoSuchFieldError on the first use of this class. A {@link WeakReference} is safe on the server. */
	private static WeakReference<TileEntity> currentTileEntity = new WeakReference<>(null);

	/** Reference the to currently opened {@link MalisisGui}. */
	private static WeakReference<MalisisGui> currenGui = new WeakReference<>(null);

	/**
	 * Gets the {@link TileEntity} of type <b>T</b> at the specified {@link BlockPos}.<br>
	 * If no <code>TileEntity</code> was found at the coordinates, or if the <code>TileEntity</code> is not of type <b>T</b>, returns
	 * <code>null</code> instead.
	 *
	 * @param <T> type of TileEntity requested
	 * @param clazz the class of the TileEntity
	 * @param world the world
	 * @param pos the pos
	 * @return the tile entity at the coordinates, or null if no tile entity, or not of type T
	 */
	public static <T> T getTileEntity(Class<T> clazz, IBlockAccess world, BlockPos pos)
	{
		if (world == null)
			return null;

		TileEntity te = world.getTileEntity(pos);
		return te != null && clazz.isAssignableFrom(te.getClass()) ? Silenced.get(() -> clazz.cast(te)) : null;
	}

	/**
	 * Links the {@link TileEntity} to the {@link MalisisGui}.<br>
	 * Allows the TileEntity to notify the MalisisGui of updates.
	 *
	 * @param te the TileEntity
	 * @param gui the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void linkTileEntityToGui(TileEntity te, MalisisGui gui)
	{
		currentTileEntity = new WeakReference<>(te);
		currenGui = new WeakReference<>(gui);
		gui.updateGui();
	}

	/**
	 * Notifies the currently opened {@link MalisisGui} to update.
	 *
	 * @param te the {@link TileEntity} linked to the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void updateGui(TileEntity te)
	{
		MalisisGui gui = currenGui.get();
		if (te != currentTileEntity.get() || gui == null)
			return;
		gui.updateGui();
	}

	public static void notifyUpdate(TileEntity te)
	{
		if (te.getWorld() == null)
			return;
		IBlockState state = te.getWorld().getBlockState(te.getPos());
		te.getWorld().notifyBlockUpdate(te.getPos(), state, state, 3);
	}
}
