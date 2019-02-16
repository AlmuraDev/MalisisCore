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

import net.malisis.core.util.FontColors;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
      final UILabel titleLabel = new UILabel(this, this.title);
      titleLabel.setFontOptions(FontColors.WHITE_FO);
      titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

      this.addToScreen(titleLabel);
    }
    this.container = new UIVanillaContainer(this);
    this.container.setBackgroundAlpha(0);
    this.container.setPosition(0, 36);
    this.container.setSize(this.width, this.height - 102);
    this.container.setClipContent(true);
    this.addToScreen(this.container);
  }

  /**
   * Gets the primary container for the screen
   *
   * @return The primary container
   */
  public UIBackgroundContainer getContainer() {
    return this.container;
  }

  /**
   * Gets the screen's title
   *
   * @return The title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Sets the screen's title
   *
   * @param title The title
   */
  public void setTitle(final String title) {
    this.title = title;
  }


  private static class UIVanillaContainer extends UIBackgroundContainer {

    private static final int CONTAINER_COLOR = FontColors.BLACK;
    private static final int BORDER_HEIGHT = 4;
    private final MalisisGui gui;

    private UIVanillaContainer(final MalisisGui gui) {
      super(gui);
      this.gui = gui;
    }

    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
      super.drawBackground(renderer, mouseX, mouseY, partialTick);

      renderer.enableBlending();
      renderer.disableTextures();
      this.rp.usePerVertexAlpha.set(true);
      this.rp.usePerVertexColor.set(true);

      // Top border
      this.shape.resetState();
      this.shape.setSize(this.gui.width, BORDER_HEIGHT);
      this.shape.setPosition(0, -4);
      this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(255));
      this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(120));
      renderer.drawShape(this.shape, this.rp);

      // Middle
      this.shape.resetState();
      this.shape.setSize(this.gui.width, this.gui.height - 104);
      this.shape.setPosition(0, 0);
      this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(120));
      this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(120));
      renderer.drawShape(this.shape, this.rp);

      // Bottom border
      this.shape.resetState();
      this.shape.setSize(this.gui.width, BORDER_HEIGHT);
      this.shape.setPosition(0, this.gui.height - 104);
      this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(120));
      this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(255));
      renderer.drawShape(this.shape, this.rp);
      renderer.next();

      this.setSize(this.gui.width, this.gui.height - 104);

      renderer.enableTextures();
    }
  }
}
