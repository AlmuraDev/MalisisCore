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

package net.malisis.core.util.blockdata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.util.Silenced;
import net.malisis.core.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * {@link BlockDataHandler} handles custom data being stored for a specific {@link BlockPos}.
 *
 * <p>
 * Custom data is identified by a {@link String} identifier, and a way to convert from and into a {@link ByteBuf} using
 * {@link #registerBlockData(String, Function, Function)}.
 *
 * <p>
 * Custom data is then stored and retrieved using {@link #setData(String, IBlockAccess, BlockPos, Object)},
 * {@link #getData(String, IBlockAccess, BlockPos)} and {@link #removeData(String, IBlockAccess, BlockPos)} with the corresponding
 * identifier.
 *
 * @author Ordinastie
 */
@AutoLoad
public class BlockDataHandler
{
	private static BlockDataHandler instance = new BlockDataHandler();
	private static Field chunkCacheField;
	private static Class<?> chunkCacheClass;

	static
	{
		if (MalisisCore.isClient() && FMLClientHandler.instance().hasOptifine())
		{
			chunkCacheClass = Silenced.get(() -> Class.forName("ChunkCacheOF"));
			if (chunkCacheClass != null)
				chunkCacheField = AsmUtils.changeFieldAccess(chunkCacheClass, "chunkCache");
		}
	}

	private Map<String, HandlerInfo<?>> handlerInfos = new HashMap<>();
	private static final ThreadLocal<Table<String, Chunk, ChunkData<?>>> datas = ThreadLocal.withInitial(HashBasedTable::create);
	/** Client only: data received for chunks the client hadn't loaded yet, applied when they load (see {@link #setBlockData}). */
	private static final Map<Long, Map<String, ChunkData<?>>> pendingClientData = new HashMap<>();

	private BlockDataHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public Map<String, HandlerInfo<?>> getHandlerInfos()
	{
		return handlerInfos;
	}

	/**
	 * Gets the actual world object based on the passed {@link IBlockAccess}.
	 *
	 * @param world the world
	 * @return the world
	 */
	private World world(IBlockAccess world)
	{
		if (world instanceof World)
			return (World) world;
		else if (world instanceof ChunkCache)
			return ((ChunkCache) world).world;

		//OptiFine's chunk cache is client only, and on a dedicated server just loading FMLClientHandler fails (it refers to IntegratedServer)
		if (world == null || !MalisisCore.isClient() || !FMLClientHandler.instance().hasOptifine())
			return null;
		if (chunkCacheClass == null || chunkCacheField == null) //was != null, so OptiFine's chunk cache never resolved and render-time lookups came back empty
			return null;
		if (!chunkCacheClass.isAssignableFrom(world.getClass()))
			return null;

		return world(Silenced.get(() -> ((ChunkCache) chunkCacheField.get(world))));
	}

	/**
	 * Gets the {@link ChunkData} for the specified identifier and {@link BlockPos}
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the chunk data
	 */
	private <T> ChunkData<T> chunkData(String identifier, World world, BlockPos pos)
	{
		return world != null ? chunkData(identifier, world, world.getChunk(pos)) : null;
	}

	/**
	 * Gets the {@link ChunkData} for the specified identifier and {@link Chunk}.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param chunk the chunk
	 * @return the chunk data
	 */
	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> chunkData(String identifier, World world, Chunk chunk)
	{
		Table<String, Chunk, ChunkData<?>> data = datas.get();
		return (ChunkData<T>) data.get(identifier, chunk);

	}

	/**
	 * Creates the {@link ChunkData} for specified identifier for the {@link Chunk} at the {@link BlockPos}.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the chunk data
	 */
	@SuppressWarnings("unchecked")
	private <T> ChunkData<T> createChunkData(String identifier, World world, BlockPos pos)
	{
		Chunk chunk = world.getChunk(pos);

		//System.out.println("createChunkData (" + chunk.xPosition + "/" + chunk.zPosition + ") for " + identifier);

		ChunkData<T> chunkData = new ChunkData<>((HandlerInfo<T>) handlerInfos.get(identifier));
		datas.get().put(identifier, chunk, chunkData);
		return chunkData;
	}

	//#region Events
	/**
	 * Saves the data in NBT for the {@link Chunk}.<br>
	 * Also unloads the data if the <code>Chunk</code> is marked as <i>unloaded</i>.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataLoad(ChunkDataEvent.Load event)
	{
		NBTTagCompound nbt = event.getData();

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			if (!nbt.hasKey(handlerInfo.identifier))
				continue;

			//			MalisisCore.message("onDataLoad (" + event.getChunk().xPosition + "/" + event.getChunk().zPosition + ") for "
			//					+ handlerInfo.identifier);
			ChunkData<?> chunkData = new ChunkData<>(handlerInfo);
			chunkData.fromBytes(Unpooled.copiedBuffer(nbt.getByteArray(handlerInfo.identifier)));
			datas.get().put(handlerInfo.identifier, event.getChunk(), chunkData);
		}
	}

	/**
	 * On data save.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataSave(ChunkDataEvent.Save event)
	{
		NBTTagCompound nbt = event.getData();

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = chunkData(handlerInfo.identifier, event.getWorld(), event.getChunk());
			if (chunkData != null && chunkData.hasData())
			{
				//				MalisisCore.message("onDataSave (" + event.getChunk().xPosition + "/" + event.getChunk().zPosition + ") for "
				//						+ handlerInfo.identifier);
				ByteBuf buf = Unpooled.buffer();
				chunkData.toBytes(buf);
				nbt.setByteArray(handlerInfo.identifier, buf.capacity(buf.writerIndex()).array());
			}

			//unload data on save because saving is called after unload
			if (event.getChunk().unloadQueued)
				datas.get().remove(handlerInfo.identifier, event.getChunk());

		}
	}

	/**
	 * Unloads the data for the {@link Chunk}.<br>
	 * Does the process client side only because unloading happens before saving on the server.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onDataUnload(ChunkEvent.Unload event)
	{
		//only unload on client, server unloads on save
		if (!event.getWorld().isRemote)
			return;

		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			datas.get().remove(handlerInfo.identifier, event.getChunk());
		}
	}

	/**
	 * Client only.<br>
	 * Applies the data received before the {@link Chunk} was loaded.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event)
	{
		if (!event.getWorld().isRemote || pendingClientData.isEmpty())
			return;

		Chunk chunk = event.getChunk();
		Map<String, ChunkData<?>> pending = pendingClientData.remove(ChunkPos.asLong(chunk.x, chunk.z));
		if (pending != null)
			pending.forEach((identifier, chunkData) -> datas.get().put(identifier, chunk, chunkData));
	}

	/**
	 * Releases the data of every {@link Chunk} of the unloading {@link World}.<br>
	 * Chunk unload and save events don't cover everything: the client gets no chunk unload when it disconnects or changes dimension, and
	 * the server's world unload (and Sponge's own chunk unloads) can skip the save that releases the data, leaving the chunks, and the
	 * world, referenced. Posted on the world's own thread, which owns its data.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		World world = event.getWorld();
		Table<String, Chunk, ChunkData<?>> data = datas.get();
		List<Chunk> chunks = data.columnKeySet().stream().filter(chunk -> chunk.getWorld() == world).collect(Collectors.toList());
		chunks.forEach(chunk -> data.column(chunk).clear());

		if (world.isRemote)
			pendingClientData.clear();
	}

	/**
	 * Server only.<br>
	 * Sends the chunks coordinates to the client when they get watched by them.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onChunkWatched(ChunkWatchEvent.Watch event)
	{
		Chunk chunk = event.getPlayer().world.getChunk(event.getChunk().x, event.getChunk().z);
		for (HandlerInfo<?> handlerInfo : handlerInfos.values())
		{
			ChunkData<?> chunkData = instance.chunkData(handlerInfo.identifier, chunk.getWorld(), chunk);
			if (chunkData != null && chunkData.hasData())
				BlockDataMessage.sendBlockData(chunk, handlerInfo.identifier, chunkData.toBytes(Unpooled.buffer()), event.getPlayer());
		}
	}

	//#end Events

	/**
	 * Registers a custom block data with the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param fromBytes the from bytes
	 * @param toBytes the to bytes
	 */
	public static <T> void registerBlockData(String identifier, Function<ByteBuf, T> fromBytes, Function<T, ByteBuf> toBytes)
	{
		instance.handlerInfos.put(identifier, new HandlerInfo<>(identifier, fromBytes, toBytes));
	}

	/**
	 * Gets the custom data stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @return the data
	 */
	public static <T> T getData(String identifier, IBlockAccess world, BlockPos pos)
	{
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, instance.world(world), pos);
		return chunkData != null ? chunkData.getData(pos) : null;
	}

	/**
	 * Sets the custom data to be stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @param data the data
	 */
	public static <T> void setData(String identifier, IBlockAccess world, BlockPos pos, T data)
	{
		setData(identifier, world, pos, data, false);
	}

	/**
	 * Sets the custom data to be stored at the {@link BlockPos} for the specified identifier and eventually sends the data to the clients
	 * watching the chunk.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @param data the data
	 * @param sendToClients the send to clients
	 */
	public static <T> void setData(String identifier, IBlockAccess world, BlockPos pos, T data, boolean sendToClients)
	{
		World w = instance.world(world);
		ChunkData<T> chunkData = instance.<T> chunkData(identifier, w, pos);
		if (chunkData == null)
			chunkData = instance.<T> createChunkData(identifier, instance.world(world), pos);

		//MalisisCore.message("SetData " + identifier + " for " + pos + " > " + data);
		chunkData.setData(pos, data);
		if (sendToClients && !w.isRemote)
		{
			ByteBuf buf = chunkData.toBytes(Unpooled.buffer());
			Utils.getLoadedChunk(w, pos).ifPresent(chunk -> BlockDataMessage.sendBlockData(chunk, identifier, buf));
		}
	}

	/**
	 * Removes the custom data stored at the {@link BlockPos} for the specified identifier.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 */

	public static <T> void removeData(String identifier, IBlockAccess world, BlockPos pos)
	{
		removeData(identifier, world, pos, false);
	}

	/**
	 * Removes the custom data stored at the {@link BlockPos} for the specified identifier and eventually sends it to clients watching the
	 * chunk.
	 *
	 * @param <T> the generic type
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 * @param sendToClients the send to clients
	 */
	public static <T> void removeData(String identifier, IBlockAccess world, BlockPos pos, boolean sendToClients)
	{
		setData(identifier, world, pos, null, sendToClients);
	}

	/**
	 * Server only.<br>
	 * Sends the custom data for the specified identifier of the {@link Chunk} containing the {@link BlockPos} to the clients watching it.
	 * Lets callers changing many positions send each chunk once, rather than once per position with
	 * {@link #setData(String, IBlockAccess, BlockPos, Object, boolean)}.
	 *
	 * @param identifier the identifier
	 * @param world the world
	 * @param pos the pos
	 */
	public static void sendData(String identifier, World world, BlockPos pos)
	{
		if (world.isRemote)
			return;

		ChunkData<?> chunkData = instance.chunkData(identifier, world, pos);
		if (chunkData == null)
			return;

		ByteBuf buf = chunkData.toBytes(Unpooled.buffer());
		Utils.getLoadedChunk(world, pos).ifPresent(chunk -> BlockDataMessage.sendBlockData(chunk, identifier, buf));
	}

	/**
	 * Called on the client when receiving the data from the server, either because client started to watch the chunk or server manually
	 * sent the data.
	 *
	 * @param chunkX the chunk X
	 * @param chunkZ the chunk Z
	 * @param identifier the identifier
	 * @param data the data
	 */
	static void setBlockData(int chunkX, int chunkZ, String identifier, ByteBuf data)
	{
		HandlerInfo<?> handlerInfo = instance.handlerInfos.get(identifier);
		if (handlerInfo == null)
			return;

		//MalisisCore.message("Received blockData (" + chunkX + "/" + chunkZ + ") for " + identifier);
		ChunkData<?> chunkData = new ChunkData<>(handlerInfo).fromBytes(data);
		//getChunk() would hand back the shared empty chunk for a chunk the client hasn't loaded yet, and the data would be lost on it
		Chunk chunk = Utils.getClientWorld().getChunkProvider().getLoadedChunk(chunkX, chunkZ);
		if (chunk == null || chunk.isEmpty())
		{
			pendingClientData.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), k -> new HashMap<>()).put(handlerInfo.identifier, chunkData);
			return;
		}
		datas.get().put(handlerInfo.identifier, chunk, chunkData);
	}

	public static BlockDataHandler get()
	{
		return instance;
	}

	/**
	 * Internal container for custom data identifier and conversion from/to {@link ByteBuf}.
	 *
	 * @param <T> the generic type
	 */
	public static class HandlerInfo<T>
	{
		String identifier;
		private Function<ByteBuf, T> fromBytes;
		private Function<T, ByteBuf> toBytes;

		public HandlerInfo(String identifier, Function<ByteBuf, T> fromBytes, Function<T, ByteBuf> toBytes)
		{
			this.identifier = identifier;
			this.fromBytes = fromBytes;
			this.toBytes = toBytes;
		}
	}

	/**
	 * Internal data storage for a specified {@link HandlerInfo}.
	 *
	 * @param <T> the generic type
	 */
	static class ChunkData<T>
	{
		private HandlerInfo<T> handlerInfos;
		private HashMap<BlockPos, T> data = new HashMap<>();

		public ChunkData(HandlerInfo<T> handlerInfo)
		{
			this.handlerInfos = handlerInfo;
		}

		public boolean hasData()
		{
			return data.size() > 0;
		}

		public T getData(BlockPos pos)
		{
			return data.get(pos);
		}

		public void setData(BlockPos pos, T blockData)
		{
			if (blockData != null)
				data.put(pos, blockData);
			else
				data.remove(pos);
		}

		public ChunkData<T> fromBytes(ByteBuf buf)
		{
			while (buf.isReadable())
			{
				BlockPos pos = BlockPos.fromLong(buf.readLong());
				ByteBuf b = buf.readBytes(buf.readInt());
				T blockData = handlerInfos.fromBytes.apply(b);
				data.put(pos, blockData);
			}

			return this;
		}

		public ByteBuf toBytes(ByteBuf buf)
		{
			for (Entry<BlockPos, T> entry : data.entrySet())
			{
				ByteBuf b = handlerInfos.toBytes.apply(entry.getValue());
				buf.writeLong(entry.getKey().toLong());
				buf.writeInt(b.writerIndex());
				buf.writeBytes(b);
			}
			return buf;
		}

	}
}