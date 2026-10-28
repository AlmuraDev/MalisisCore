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

import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.BasicSimpleButton;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.malisis.core.util.MouseButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicForm extends BasicContainer<BasicForm> {

  private BasicContainer<?> titleContainer;
  private BasicLine line;
  private BasicSimpleButton closeButton;
  private boolean closable, movable, dragging;
  /** Where the mouse grabbed the form, from its top left corner. */
  private int dragOffsetX, dragOffsetY;

  public BasicForm(final int width, final int height) {
    this(width, height, "");
  }

  public BasicForm(final int width, final int height, final String title) {
    super(title, width, height);

    // Defaults
    this.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
    this.setMovable(true);
    this.setBorder(FontColors.WHITE, 1, 185);
    this.setBackgroundAlpha(215);
    this.setColor(Integer.MIN_VALUE);
    this.setPadding(3, 20, 3, 3);

    this.construct();

    // Needs to happen after construct
    this.setClosable(true);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicForm(int, int)}
   */
  @Deprecated
  public BasicForm(final MalisisGui gui, final int width, final int height) {
    this(width, height);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicForm(int, int, String)}
   */
  @Deprecated
  public BasicForm(final MalisisGui gui, final int width, final int height, final String title) {
    this(width, height, title);
  }

  private void construct() {
    this.titleContainer = new BasicContainer<>();
    this.titleContainer.setPosition(-this.getLeftBorderedPadding(), -19, Anchor.TOP | Anchor.LEFT);
    this.titleContainer.setSize(net.malisis.core.client.gui.element.size.Size.of(() -> this.getWidth() - this.getRightBorderedPadding(), 15));
    this.titleContainer.setColor(0x363636);
    this.titleContainer.setBackgroundAlpha(215);

    this.add(this.titleContainer);

    this.line = new BasicLine(1);
    this.line.setSize(net.malisis.core.client.gui.element.size.Size.of(() -> this.getWidth() + this.getLeftBorderedPadding() + this.getRightBorderedPadding(), 1));
    this.line.setPosition(-(this.getLeftBorderedPadding()), -4);

    this.add(this.line);

    this.closeButton = new BasicSimpleButton();
    this.closeButton.setFontOptions(FontColors.GRAY_FO);
    this.closeButton.setHoveredFontOptions(FontColors.WHITE_FO);
    this.closeButton.setText("x");
    this.closeButton.setName("button.form.close");
    this.closeButton.setPosition(0, 0, Anchor.MIDDLE | Anchor.RIGHT);
    this.closeButton.register(this);

    this.titleContainer.add(this.titleLabel);
    this.titleLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().shadow(false).build());
    this.titleLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
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

  /**
   * Sets the title shown in the title bar.
   *
   * @param title the title
   * @return this form
   */
  @Override
  public BasicForm setTitle(final String title) {
    // The title label lives in the title bar, see construct()
    this.titleLabel.setText(title == null ? "" : title);
    return this;
  }

  @Override
  public String getTitle() {
    return this.titleLabel.getText();
  }

  public void setTitle(final String text, final FontOptions options) {
    this.setTitle(text);
    this.titleLabel.setFontOptions(options);
  }

  @Override
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
  public boolean onButtonPress(final MouseButton button) {
    final int mouseX = MalisisGui.MOUSE_POSITION.x();
    final int mouseY = MalisisGui.MOUSE_POSITION.y();
    this.dragging = !this.closeButton.isInsideBounds(mouseX, mouseY) && this.titleContainer.isInsideBounds(mouseX, mouseY);
    this.dragOffsetX = mouseX - this.screenX();
    this.dragOffsetY = mouseY - this.screenY();
    return super.onButtonPress(button);
  }

  @Override
  public boolean onButtonRelease(final MouseButton button) {
    if (button == MouseButton.LEFT && this.dragging) {
      this.dragging = false;
    }
    return super.onButtonRelease(button);
  }

  @Override
  public boolean onDrag(final MouseButton button) {
    if (!this.movable || !this.dragging) {
      return super.onDrag(button);
    }

    final UIComponent parentContainer = getParent();
    if (parentContainer == null) {
      return super.onDrag(button);
    }

    // The form's new top left, relative to the parent
    final int xPos = MalisisGui.MOUSE_POSITION.x() - this.dragOffsetX - parentContainer.screenX();
    final int yPos = MalisisGui.MOUSE_POSITION.y() - this.dragOffsetY - parentContainer.screenY();

    final int targetX = Math.min(parentContainer.getWidth() - this.getWidth(), Math.max(xPos, 0));
    final int targetY = Math.min(parentContainer.getHeight() - this.getHeight(), Math.max(yPos, 0));
    // Anchor.NONE adds the parent's padding back
    final Padding padding = Padding.of(parentContainer);
    setPosition(targetX - padding.left(), targetY - padding.top(), Anchor.NONE);
    return true;
  }

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
}
