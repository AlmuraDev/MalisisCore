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
package net.malisis.core.client.gui.component.container;

import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.client.gui.component.interaction.button.BasicSimpleButton;
import net.malisis.core.util.FontColors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicForm extends BasicContainer<BasicForm> {
  private BasicContainer<?> titleContainer;
  private BasicLine line;
  private BasicSimpleButton closeButton;
  private boolean closable, movable, dragging;

  public BasicForm(final MalisisGui gui, final int width, final int height) {
    this(gui, width, height, "");
  }

  public BasicForm(final MalisisGui gui, final int width, final int height, final String title) {
    super(gui, title, width, height);

    // Defaults
    this.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
    this.setMovable(true);
    this.setBorder(FontColors.WHITE, 1, 185);
    this.setBackgroundAlpha(215);
    this.setColor(Integer.MIN_VALUE);
    this.setPadding(3, 20, 3, 3);

    this.construct(gui);

    // Needs to happen after construct
    this.setClosable(true);
  }

  private void construct(final MalisisGui gui) {
    this.titleContainer = new BasicContainer<>(gui);
    this.titleContainer.setPosition(-this.getLeftBorderedPadding(), -19, Anchor.TOP | Anchor.LEFT);
    this.titleContainer.setSize(this.getWidth() - this.getRightBorderedPadding(), 15);
    this.titleContainer.setColor(0x363636);
    this.titleContainer.setBackgroundAlpha(215);

    this.add(this.titleContainer);

    this.line = new BasicLine(gui, this.getWidth() + this.getLeftBorderedPadding() + this.getRightBorderedPadding());
    this.line.setPosition(-(this.getLeftBorderedPadding()), -4);

    this.add(this.line);

    this.closeButton = new BasicSimpleButton(getGui(), "x");
    this.closeButton.setName("button.form.close");
    this.closeButton.setPosition(0, 0, Anchor.MIDDLE | Anchor.RIGHT);
    this.closeButton.register(this);

    if (this.titleLabel != null) {
      this.remove(this.titleLabel);
      this.titleContainer.add(this.titleLabel);
      this.titleLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().shadow(false).build());
      this.titleLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
    }
  }

  public boolean isClosable() {
    return this.closable;
  }

  public BasicForm setClosable(final boolean closable) {
    this.closable = closable;

    if (closable) {
      this.titleContainer.add(this.closeButton);
    } else {
      this.titleContainer.remove(this.closeButton);
    }

    return this;
  }

  public boolean isMovable() {
    return this.movable;
  }

  public BasicForm setMovable(final boolean movable) {
    this.movable = movable;
    return this;
  }

  public void setTitle(final String text, final FontOptions options) {
    super.setTitle(text);
    this.titleLabel.setFontOptions(options);
  }

  public void onClose() {
    final MalisisGui currentGui = getGui();
    if (currentGui != null) {
      if (currentGui.isOverlay()) {
        currentGui.closeOverlay();
      } else {
        currentGui.close();
      }
    }
  }

  private int getLeftBorderedPadding() {
    return this.getLeftPadding() - this.getLeftBorderSize();
  }

  private int getRightBorderedPadding() {
    return this.getRightPadding() - this.getRightBorderSize();
  }

  @Override
  public boolean onButtonPress(final int x, final int y, final MouseButton button) {
    this.dragging = !this.closeButton.isInsideBounds(x, y) && this.titleContainer.isInsideBounds(x, y);
    return super.onButtonPress(x, y, button);
  }

  @Override
  public boolean onButtonRelease(final int x, final int y, final MouseButton button) {
    if (button == MouseButton.LEFT && this.dragging) {
      this.dragging = false;
    }
    return super.onButtonRelease(x, y, button);
  }

  @Override
  public boolean onDrag(final int lastX, final int lastY, final int x, final int y, final MouseButton button) {
    if (!this.movable || !this.dragging) {
      return super.onDrag(lastX, lastY, x, y, button);
    }

    final UIComponent<?> parentContainer = getParent();
    if (parentContainer == null) {
      return super.onDrag(lastX, lastY, x, y, button);
    }

    final int xPos = getParent().relativeX(x) - relativeX(lastX);
    final int yPos = getParent().relativeY(y) - relativeY(lastY);

    final int targetX = Math.min(parentContainer.getWidth() - this.width, Math.max(xPos, 0));
    final int targetY = Math.min(parentContainer.getHeight() - this.height, Math.max(yPos, 0));
    setPosition(targetX, targetY, Anchor.NONE);
    return true;
  }

  @Override
  public BasicForm setSize(final int width, final int height) {
    super.setSize(width, height);

    if (this.line != null) {
      this.line.setWidth(this.getWidth() + this.getLeftBorderedPadding() + this.getRightBorderedPadding());
    }

    if (this.titleContainer != null) {
      this.titleContainer.setSize(this.getWidth() - this.getRightBorderedPadding(), 15);
    }

    return this;
  }

  @SuppressWarnings("unchecked")
  @Subscribe
  public void onButtonClick(final UIButton.ClickEvent event) {
    if ("button.form.close".equals(event.getComponent().getName().toLowerCase())) {
      if (this.closeButton.isInsideBounds(event.getX(), event.getY())) {
        this.onClose();
        if (getParent() instanceof UIContainer) {
          ((UIContainer) this.getParent()).remove(this);
        }
      }
    }
  }

  @Override
  public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
    super.drawBackground(renderer, mouseX, mouseY, partialTick);
  }

  @Override
  public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
    super.drawForeground(renderer, mouseX, mouseY, partialTick);
    this.closeButton.setFontOptions(this.closeButton.isInsideBounds(mouseX, mouseY) ? FontColors.WHITE_FO : FontColors.GRAY_FO);
  }
}
