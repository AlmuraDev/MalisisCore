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
package net.malisis.core.client.gui.component.decoration;

import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.util.FontColors;
import net.malisis.core.client.gui.MalisisGui;

public class BasicLine extends BasicContainer<BasicLine> {

  public BasicLine(final MalisisGui gui, final int width) {
    this(gui, width, 1, false);
  }

  public BasicLine(final MalisisGui gui, final int size, final boolean vertical) {
    this(gui, size, 1, vertical);
  }

  public BasicLine(final MalisisGui gui, final int width, final int thickness) {
    this(gui, width, thickness, false);
  }

  public BasicLine(final MalisisGui gui, final int size, final int thickness, final boolean vertical) {
    super(gui, vertical ? thickness : size, vertical ? size : thickness);
    this.setColor(FontColors.WHITE);
    this.setBackgroundAlpha(185);
  }
}
