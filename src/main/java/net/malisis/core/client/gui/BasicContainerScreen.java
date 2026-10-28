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
package net.malisis.core.client.gui;

import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.util.FontColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.IntSupplier;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BasicContainerScreen extends BasicScreen {

  private UIVanillaContainer container;
  private String title;

  public BasicContainerScreen(@Nullable final BasicScreen parent, final String title) {
    super(parent);
    this.title = title;
  }

  @Override
  public void construct() {
    if (!this.title.isEmpty()) {
      final UILabel titleLabel = new UILabel(this.title);
      titleLabel.setFontOptions(FontColors.WHITE_FO);
      titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);
      this.addToScreen(titleLabel);
    }

    this.container = new UIVanillaContainer(this);
    this.container.setBackgroundAlpha(0);
    this.container.setPosition(0, 36);
    this.container.setClipContent(true);
    this.addToScreen(this.container);
  }

  public BasicContainer<?> getContainer() {
    return this.container;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * The area under the title, darkened like vanilla lists, with gradient top and bottom edges. Fills the width of the screen.
   */
  private static class UIVanillaContainer extends BasicContainer<UIVanillaContainer> {

    private static final int CONTAINER_COLOR = FontColors.BLACK;
    private static final int BORDER_HEIGHT = 4;
    private static final int SPACE_AROUND = 104;

    private UIVanillaContainer(final MalisisGui gui) {
      // ITransformable declares a Size type that hides the GUI's: fully qualified here
      this.setSize(net.malisis.core.client.gui.element.size.Size.of(() -> gui.width, () -> gui.height - SPACE_AROUND));

      final GuiShape top = this.band(() -> -BORDER_HEIGHT, () -> BORDER_HEIGHT, 255, 120);
      final GuiShape middle = this.band(() -> 0, () -> this.getHeight(), 120, 120);
      final GuiShape bottom = this.band(this::getHeight, () -> BORDER_HEIGHT, 120, 255);
      final net.malisis.core.client.gui.render.IGuiRenderer background = this.backgroundRenderer.get();
      this.setBackground(renderer -> {
        if (background != null) {
          background.render(renderer);
        }
        top.render(renderer);
        middle.render(renderer);
        bottom.render(renderer);
        renderer.next();
      });
    }

    private GuiShape band(final IntSupplier y, final IntSupplier height, final int topAlpha, final int bottomAlpha) {
      return GuiShape.builder(this)
        .position(s -> net.malisis.core.client.gui.element.position.Position.of(() -> 0, y))
        .size(s -> net.malisis.core.client.gui.element.size.Size.of(this::getWidth, height))
        .color(CONTAINER_COLOR)
        .alpha((fp, vp) -> vp.y() == 0 ? topAlpha : bottomAlpha)
        .icon(GuiIcon.NONE)
        .build();
    }
  }
}
