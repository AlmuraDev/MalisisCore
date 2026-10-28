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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.util.FontColors;

public class BasicLine extends BasicContainer<BasicLine> {

  public BasicLine(final int width) {
    this(width, 1, false);
  }

  public BasicLine(final int size, final boolean vertical) {
    this(size, 1, vertical);
  }

  public BasicLine(final int width, final int thickness) {
    this(width, thickness, false);
  }

  public BasicLine(final int size, final int thickness, final boolean vertical) {
    super(vertical ? thickness : size, vertical ? size : thickness);
    this.setColor(FontColors.WHITE);
    this.setBackgroundAlpha(185);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicLine(int)}
   */
  @Deprecated
  public BasicLine(final MalisisGui gui, final int width) {
    this(width);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicLine(int, boolean)}
   */
  @Deprecated
  public BasicLine(final MalisisGui gui, final int size, final boolean vertical) {
    this(size, vertical);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicLine(int, int)}
   */
  @Deprecated
  public BasicLine(final MalisisGui gui, final int width, final int thickness) {
    this(width, thickness);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicLine(int, int, boolean)}
   */
  @Deprecated
  public BasicLine(final MalisisGui gui, final int size, final int thickness, final boolean vertical) {
    this(size, thickness, vertical);
  }
}
