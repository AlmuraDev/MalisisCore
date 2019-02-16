/*
 * This file is part of MalisisCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) AlmuraDev
 * Copyright (c) contributors
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

import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface FontColors {

  int BLACK = 0x000000;
  FontOptions BLACK_FO = FontOptions.builder().color(BLACK).build();

  int DARK_BLUE = 0x0000AA;
  FontOptions DARK_BLUE_FO = FontOptions.builder().color(DARK_BLUE).build();

  int DARK_AQUA = 0x00AAAA;
  FontOptions DARK_AQUA_FO = FontOptions.builder().color(DARK_AQUA).build();

  int DARK_GREEN = 0x00AA00;
  FontOptions DARK_GREEN_FO = FontOptions.builder().color(DARK_GREEN).build();

  int DARK_RED = 0xAA0000;
  FontOptions DARK_RED_FO = FontOptions.builder().color(DARK_RED).build();

  int DARK_PURPLE = 0xAA00AA;
  FontOptions DARK_PURPLE_FO = FontOptions.builder().color(DARK_PURPLE).build();

  int GOLD = 0xFFAA00;
  FontOptions GOLD_FO = FontOptions.builder().color(GOLD).build();

  int GRAY = 0xAAAAAA;
  FontOptions GRAY_FO = FontOptions.builder().color(GRAY).build();

  int DARK_GRAY = 0x555555;
  FontOptions DARK_GRAY_FO = FontOptions.builder().color(DARK_GRAY).build();

  int BLUE = 0x5555FF;
  FontOptions BLUE_FO = FontOptions.builder().color(BLUE).build();

  int GREEN = 0x55FF55;
  FontOptions GREEN_FO = FontOptions.builder().color(GREEN).build();

  int AQUA = 0x55FFFF;
  FontOptions AQUA_FO = FontOptions.builder().color(AQUA).build();

  int RED = 0xFF5555;
  FontOptions RED_FO = FontOptions.builder().color(RED).build();

  int LIGHT_PURPLE = 0xFF55FF;
  FontOptions LIGHT_PURPLE_FO = FontOptions.builder().color(LIGHT_PURPLE).build();

  int YELLOW = 0xFFFFAA;
  FontOptions YELLOW_FO = FontOptions.builder().color(YELLOW).build();

  int WHITE = 0xFFFFFF;
  FontOptions WHITE_FO = FontOptions.builder().color(WHITE).build();
}

