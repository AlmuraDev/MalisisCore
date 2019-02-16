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
package net.malisis.core.client.gui.component.interaction.button;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicSimpleButton extends UIButton {

  public BasicSimpleButton(final MalisisGui gui) {
    super(gui);
  }

  public BasicSimpleButton(final MalisisGui gui, final String text) {
    super(gui, text);
  }

  public BasicSimpleButton(final MalisisGui gui, final UIImage image) {
    super(gui, image);
  }

  // Draw nothing to avoid drawing the background images
  @Override
  public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
  }

  @Override
  public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTicks) {
    final int w;
    final int h;

    if (this.image != null) {
      w = this.image.getWidth();
      h = this.image.getHeight();
    } else {
      w = (int) this.font.getStringWidth(this.text, this.fontOptions);
      h = (int) this.font.getStringHeight(this.fontOptions);
    }

    int x = (this.width - w) / 2;
    int y = (this.height - h) / 2;

    x += this.offsetX;
    y += this.offsetY;

    if (this.image != null) {
      this.image.setPosition(x, y);
      this.image.setZIndex(this.zIndex);
      this.image.draw(renderer, mouseX, mouseY, partialTicks);
    } else {
      renderer.drawText(this.font, this.text, x, y, this.zIndex, isHovered() ? this.hoveredFontOptions : this.fontOptions);
    }
  }
}
