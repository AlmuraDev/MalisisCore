/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

package net.malisis.core.asm.mixin.core;

import java.util.ArrayDeque;
import java.util.Deque;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.malisis.core.registry.Registries;
import net.malisis.core.util.callback.CallbackResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * @author Ordinastie
 *
 */
@Mixin(value = Chunk.class, priority = 1001)
public class MixinChunk
{
	/**
	 * The state each running setBlockState() replaced. A stack rather than a single field: setBlockState() can re-enter the same chunk
	 * (breakBlock/onBlockAdded placing other blocks), and the inner call mustn't overwrite the outer call's old state.
	 */
	private final Deque<IBlockState> malisis$oldStates = new ArrayDeque<>();

	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
			at = @At("HEAD"),
			cancellable = true)
	private void preSetBlock(BlockPos pos, IBlockState newState, CallbackInfoReturnable<IBlockState> cir)
	{
		//with Sponge, this only forwards to bridge$setBlockState(), handled below
		if (!Registries.hasSpongeChunkBridge())
			malisis$preSetBlock(pos, newState, ((Chunk) (Object) this).getBlockState(pos), cir);
	}

	//every return, so the stack stays balanced when setBlockState() returns early (nothing changed)
	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
			at = @At("RETURN"))
	private void postSetBlock(BlockPos pos, IBlockState newState, CallbackInfoReturnable<IBlockState> cir)
	{
		if (!Registries.hasSpongeChunkBridge())
			malisis$postSetBlock(pos, newState, cir);
	}

	/*
	 * Sponge: the server's World.setBlockState() goes through Sponge's PhaseTracker, which sets the block with the chunk's
	 * bridge$setBlockState() (added by Sponge) and never calls the vanilla setBlockState() above, so the set block callbacks never ran on
	 * the server (e.g. the big doors' positions were never recorded or saved, and their collision and click boxes were missing after a
	 * restart until a door was used). Optional (require = 0): the method only exists with Sponge. The flag is Sponge's BlockChangeFlag,
	 * coerced to Object as MalisisCore doesn't depend on Sponge.
	 */
	@Inject(method = "bridge$setBlockState", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void spongePreSetBlock(BlockPos pos, IBlockState newState, IBlockState currentState, @Coerce Object flag,
			CallbackInfoReturnable<IBlockState> cir)
	{
		malisis$preSetBlock(pos, newState, currentState != null ? currentState : ((Chunk) (Object) this).getBlockState(pos), cir);
	}

	@Inject(method = "bridge$setBlockState", at = @At("RETURN"), remap = false, require = 0)
	private void spongePostSetBlock(BlockPos pos, IBlockState newState, IBlockState currentState, @Coerce Object flag,
			CallbackInfoReturnable<IBlockState> cir)
	{
		malisis$postSetBlock(pos, newState, cir);
	}

	private void malisis$preSetBlock(BlockPos pos, IBlockState newState, IBlockState oldState, CallbackInfoReturnable<IBlockState> cir)
	{
		CallbackResult<Void> cb = Registries.processPreSetBlock((Chunk) (Object) this, pos, oldState, newState);
		if (cb != null && cb.shouldReturn()) {
			//cancelled: the method doesn't run, so the post callback won't either
			cir.cancel();
			return;
		}
		malisis$oldStates.push(oldState);
	}

	private void malisis$postSetBlock(BlockPos pos, IBlockState newState, CallbackInfoReturnable<IBlockState> cir)
	{
		IBlockState oldState = malisis$oldStates.poll();
		//setBlockState() returns null when nothing was changed
		if (oldState != null && cir.getReturnValue() != null) {
			Registries.processPostSetBlock((Chunk) (Object) this, pos, oldState, newState);
		}
	}
}
