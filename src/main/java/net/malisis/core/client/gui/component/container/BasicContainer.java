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

import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextInput;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.render.shape.VertexPosition;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/**
 * A {@link UIContainer} with a background colored per corner (a gradient) and an optional border with a size per side.
 *
 * @param <T> the container type, kept so 6.5.x declarations such as {@code BasicContainer<?>} still compile
 */
@SideOnly(Side.CLIENT)
public class BasicContainer<T extends BasicContainer<T>> extends UIContainer implements ITransformable.Color {

  // ITransformable declares Position and Size types, which hide the GUI classes of the same names: those are fully qualified here

  private int topLeftColor = -1;
  private int topRightColor = -1;
  private int bottomLeftColor = -1;
  private int bottomRightColor = -1;
  private int topLeftAlpha = 255;
  private int topRightAlpha = 255;
  private int bottomLeftAlpha = 255;
  private int bottomRightAlpha = 255;
  private int leftBorderSize = 0;
  private int rightBorderSize = 0;
  private int topBorderSize = 0;
  private int bottomBorderSize = 0;
  private int borderColor = 0;
  private int borderAlpha = 0;
  /** Label for the title, added to the container when a title is set. */
  protected final UILabel titleLabel = new UILabel();

  public BasicContainer() {
    final GuiShape center = GuiShape.builder(this)
      .position(s -> net.malisis.core.client.gui.element.position.Position.of(() -> this.leftBorderSize, () -> this.topBorderSize))
      .size(s -> net.malisis.core.client.gui.element.size.Size.of(() -> this.innerBorderWidth(), () -> this.innerBorderHeight()))
      .color((fp, vp) -> this.cornerColor(vp))
      .alpha((fp, vp) -> this.cornerAlpha(vp))
      .icon(GuiIcon.NONE)
      .build();
    final GuiShape top = this.borderShape(0, 0, () -> this.size().width(), () -> this.topBorderSize);
    final GuiShape bottom = this.borderShape(0, () -> this.size().height() - this.bottomBorderSize, () -> this.size().width(),
      () -> this.bottomBorderSize);
    final GuiShape left = this.borderShape(0, () -> this.topBorderSize, () -> this.leftBorderSize, this::innerBorderHeight);
    final GuiShape right = this.borderShape(() -> this.size().width() - this.rightBorderSize, () -> this.topBorderSize,
      () -> this.rightBorderSize, this::innerBorderHeight);

    this.setBackground(renderer -> {
      if (this.alpha == 0) {
        return;
      }
      center.render(renderer);
      if (this.hasBorder()) {
        top.render(renderer);
        bottom.render(renderer);
        left.render(renderer);
        right.render(renderer);
      }
      renderer.next();
    });
  }

  public BasicContainer(final String title) {
    this();
    setTitle(title);
  }

  public BasicContainer(final int width, final int height) {
    this();
    setSize(width, height);
  }

  public BasicContainer(final String title, final int width, final int height) {
    this();
    setTitle(title);
    setSize(width, height);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicContainer()}
   */
  @Deprecated
  public BasicContainer(final MalisisGui gui) {
    this();
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicContainer(String)}
   */
  @Deprecated
  public BasicContainer(final MalisisGui gui, final String title) {
    this(title);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicContainer(int, int)}
   */
  @Deprecated
  public BasicContainer(final MalisisGui gui, final int width, final int height) {
    this(width, height);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicContainer(String, int, int)}
   */
  @Deprecated
  public BasicContainer(final MalisisGui gui, final String title, final int width, final int height) {
    this(title, width, height);
  }

  private GuiShape borderShape(final int x, final IntSupplier y, final IntSupplier width,
    final IntSupplier height) {
    return this.borderShape(() -> x, y, width, height);
  }

  private GuiShape borderShape(final int x, final int y, final IntSupplier width, final IntSupplier height) {
    return this.borderShape(() -> x, () -> y, width, height);
  }

  private GuiShape borderShape(final IntSupplier x, final IntSupplier y,
    final IntSupplier width, final IntSupplier height) {
    return GuiShape.builder(this)
      .position(s -> net.malisis.core.client.gui.element.position.Position.of(x, y))
      .size(s -> net.malisis.core.client.gui.element.size.Size.of(width, height))
      .color(() -> this.borderColor)
      .alpha(() -> this.borderAlpha)
      .icon(GuiIcon.NONE)
      .build();
  }

  private boolean hasBorder() {
    return this.leftBorderSize > 0 || this.rightBorderSize > 0 || this.topBorderSize > 0 || this.bottomBorderSize > 0;
  }

  private int innerBorderWidth() {
    return Math.max(this.size().width() - this.leftBorderSize - this.rightBorderSize, 0);
  }

  private int innerBorderHeight() {
    return Math.max(this.size().height() - this.topBorderSize - this.bottomBorderSize, 0);
  }

  private int cornerColor(final VertexPosition position) {
    switch (position) {
      case TOPLEFT:
        return this.topLeftColor;
      case TOPRIGHT:
        return this.topRightColor;
      case BOTTOMLEFT:
        return this.bottomLeftColor;
      default:
        return this.bottomRightColor;
    }
  }

  private int cornerAlpha(final VertexPosition position) {
    switch (position) {
      case TOPLEFT:
        return this.topLeftAlpha;
      case TOPRIGHT:
        return this.topRightAlpha;
      case BOTTOMLEFT:
        return this.bottomLeftAlpha;
      default:
        return this.bottomRightAlpha;
    }
  }

  /**
   * Sets the title for this container, shown by a label at its top left. A null or empty title removes it.
   *
   * @param title the title
   * @return this container
   */
  public BasicContainer<T> setTitle(final String title) {
    if (title == null || title.isEmpty()) {
      remove(this.titleLabel);
      return this;
    }

    this.titleLabel.setText(title);
    add(this.titleLabel);
    return this;
  }

  public String getTitle() {
    return this.content.components.contains(this.titleLabel) ? this.titleLabel.getText() : null;
  }

  /**
   * Sets the border for this container
   *
   * @param color The color to use
   * @param size The size of the border
   * @param alpha The alpha value
   * @return The container with applied changes
   */
  public BasicContainer<T> setBorder(final int color, final int size, final int alpha) {
    return this.setBorders(color, alpha, size, size, size, size);
  }

  /**
   * Sets the borders for this container
   *
   * @param color The color to use
   * @param alpha The alpha value
   * @param leftBorderSize The size of the left border
   * @param topBorderSize The size of the top border
   * @param rightBorderSize The size of the right border
   * @param bottomBorderSize The size of the bottom border
   * @return The container with applied changes
   */
  public BasicContainer<T> setBorders(final int color, final int alpha, final int leftBorderSize, final int topBorderSize, final int rightBorderSize,
    final int bottomBorderSize) {
    this.borderColor = color;
    this.borderAlpha = alpha;
    this.leftBorderSize = leftBorderSize;
    this.topBorderSize = topBorderSize;
    this.rightBorderSize = rightBorderSize;
    this.bottomBorderSize = bottomBorderSize;
    return this;
  }

  public int getTopLeftColor() {
    return topLeftColor;
  }

  public BasicContainer<T> setTopLeftColor(final int topLeftColor) {
    this.topLeftColor = topLeftColor;
    return this;
  }

  public int getTopRightColor() {
    return topRightColor;
  }

  public BasicContainer<T> setTopRightColor(final int topRightColor) {
    this.topRightColor = topRightColor;
    return this;
  }

  public int getBottomLeftColor() {
    return bottomLeftColor;
  }

  public BasicContainer<T> setBottomLeftColor(final int bottomLeftColor) {
    this.bottomLeftColor = bottomLeftColor;
    return this;
  }

  public int getBottomRightColor() {
    return bottomRightColor;
  }

  public BasicContainer<T> setBottomRightColor(final int bottomRightColor) {
    this.bottomRightColor = bottomRightColor;
    return this;
  }

  public BasicContainer<T> setTopColor(final int color) {
    setTopLeftColor(color);
    setTopRightColor(color);
    return this;
  }

  public BasicContainer<T> setBottomColor(final int color) {
    setBottomLeftColor(color);
    setBottomRightColor(color);
    return this;
  }

  public BasicContainer<T> setLeftColor(final int color) {
    setTopLeftColor(color);
    setBottomLeftColor(color);
    return this;
  }

  public BasicContainer<T> setRightColor(final int color) {
    setTopRightColor(color);
    setBottomRightColor(color);
    return this;
  }

  @Override
  public void setColor(final int color) {
    setTopColor(color);
    setBottomColor(color);
  }

  public int getTopLeftAlpha() {
    return topLeftAlpha;
  }

  public BasicContainer<T> setTopLeftAlpha(final int topLeftAlpha) {
    this.topLeftAlpha = topLeftAlpha;
    return this;
  }

  public int getTopRightAlpha() {
    return topRightAlpha;
  }

  public BasicContainer<T> setTopRightAlpha(final int topRightAlpha) {
    this.topRightAlpha = topRightAlpha;
    return this;
  }

  public int getBottomLeftAlpha() {
    return bottomLeftAlpha;
  }

  public BasicContainer<T> setBottomLeftAlpha(final int bottomLeftAlpha) {
    this.bottomLeftAlpha = bottomLeftAlpha;
    return this;
  }

  public int getBottomRightAlpha() {
    return bottomRightAlpha;
  }

  public BasicContainer<T> setBottomRightAlpha(final int bottomRightAlpha) {
    this.bottomRightAlpha = bottomRightAlpha;
    return this;
  }

  public BasicContainer<T> setTopAlpha(final int alpha) {
    setTopLeftAlpha(alpha);
    setTopRightAlpha(alpha);
    return this;
  }

  public BasicContainer<T> setBottomAlpha(final int alpha) {
    setBottomLeftAlpha(alpha);
    setBottomRightAlpha(alpha);
    return this;
  }

  public BasicContainer<T> setLeftAlpha(final int alpha) {
    setTopLeftAlpha(alpha);
    setBottomLeftAlpha(alpha);
    return this;
  }

  public BasicContainer<T> setRightAlpha(final int alpha) {
    setTopRightAlpha(alpha);
    setBottomRightAlpha(alpha);
    return this;
  }

  public BasicContainer<T> setBackgroundAlpha(final int alpha) {
    setTopAlpha(alpha);
    setBottomAlpha(alpha);
    return this;
  }

  /**
   * Sets the padding on every side.
   *
   * @param padding The padding
   * @return The container with applied changes
   */
  public BasicContainer<T> setPadding(final int padding) {
    this.setPadding(Padding.of(padding));
    return this;
  }

  /**
   * Sets the horizontal (left and right) and vertical (top and bottom) padding.
   *
   * @param horizontal The left and right padding
   * @param vertical The top and bottom padding
   * @return The container with applied changes
   */
  public BasicContainer<T> setPadding(final int horizontal, final int vertical) {
    this.setPadding(Padding.of(horizontal, vertical));
    return this;
  }

  /**
   * Sets the padding per side.
   *
   * @param left The left padding
   * @param top The top padding
   * @param right The right padding
   * @param bottom The bottom padding
   * @return The container with applied changes
   */
  public BasicContainer<T> setPadding(final int left, final int top, final int right, final int bottom) {
    this.setPadding(Padding.of(top, bottom, left, right));
    return this;
  }

  public int getLeftPadding() {
    return this.padding().left();
  }

  public int getTopPadding() {
    return this.padding().top();
  }

  public int getRightPadding() {
    return this.padding().right();
  }

  public int getBottomPadding() {
    return this.padding().bottom();
  }

  public BasicContainer<T> setWidth(final int width) {
    this.setSize(width, this.getRawHeight());
    return this;
  }

  public BasicContainer<T> setHeight(final int height) {
    this.setSize(this.getRawWidth(), height);
    return this;
  }

  public int getLeftBorderSize() {
    return this.leftBorderSize;
  }

  public int getRightBorderSize() {
    return this.rightBorderSize;
  }

  public int getTopBorderSize() {
    return this.topBorderSize;
  }

  public int getBottomBorderSize() {
    return this.bottomBorderSize;
  }

  public int getBorderColor() {
    return this.borderColor;
  }

  public int getBorderAlpha() {
    return this.borderAlpha;
  }

  /**
   * Gets an unmodifiable set of components
   *
   * @return The components
   */
  public Set<UIComponent> getComponents() {
    return Collections.unmodifiableSet(this.content.components);
  }

  public void tabToLastControl() {
    final BasicTextInput currentTextbox =
      BasicScreen.getFocusedComponent() instanceof BasicTextInput ? ((BasicTextInput) BasicScreen.getFocusedComponent()) : null;
    BasicTextInput toFocus = null;
    final int lastTabIndex = currentTextbox == null ? 0 : currentTextbox.getTabIndex();

    // Attempt to find the next control in line
    final List<BasicTextInput> filteredComponents =
      this.content.components.stream().filter(c -> c.isEnabled() && c.isVisible() && c instanceof BasicTextInput).map(c -> (BasicTextInput) c)
        .filter(BasicTextInput::isEditable).sorted(Comparator.comparingInt(BasicTextInput::getTabIndex).reversed()).collect(Collectors.toList());

    // Attempt 1: See if we can find an index that is higher than our current
    for (final BasicTextInput tb : filteredComponents) {
      if (tb == currentTextbox) {
        continue;
      }

      if (tb.getTabIndex() < lastTabIndex) {
        toFocus = tb;
        break;
      }
    }

    // Attempt 2: See if we can find an index with the same value
    if (toFocus == null) {
      for (final BasicTextInput tb : filteredComponents) {
        if (tb == currentTextbox) {
          continue;
        }

        if (tb.getTabIndex() == lastTabIndex) {
          toFocus = tb;
          break;
        }
      }
    }

    // Attempt 3: We attempt to find the rollover point and start there
    if (toFocus == null) {
      BasicTextInput highestIndexTextBox = currentTextbox;
      for (final BasicTextInput tb : filteredComponents) {

        if (tb == currentTextbox) {
          continue;
        }

        if (highestIndexTextBox == null || tb.getTabIndex() > highestIndexTextBox.getTabIndex()) {
          highestIndexTextBox = tb;
        }
      }

      if (highestIndexTextBox != null) {
        toFocus = highestIndexTextBox;
      }
    }

    if (toFocus != null && currentTextbox != null) {
      currentTextbox.deselectAll();
      toFocus.focus();
      toFocus.selectAll();
    }
  }

  public void tabToNextControl() {
    final BasicTextInput currentTextbox =
      BasicScreen.getFocusedComponent() instanceof BasicTextInput ? ((BasicTextInput) BasicScreen.getFocusedComponent()) : null;
    BasicTextInput toFocus = null;
    final int lastTabIndex = currentTextbox == null ? 0 : currentTextbox.getTabIndex();

    // Attempt to find the next control in line
    final List<BasicTextInput> filteredComponents =
      this.content.components.stream().filter(c -> c.isEnabled() && c.isVisible() && c instanceof BasicTextInput).map(c -> (BasicTextInput) c)
        .filter(BasicTextInput::isEditable).sorted(Comparator.comparingInt(BasicTextInput::getTabIndex)).collect(Collectors.toList());

    // Attempt 1: See if we can find an index that is higher than our current
    for (final BasicTextInput tb : filteredComponents) {
      if (tb == currentTextbox) {
        continue;
      }

      if (tb.getTabIndex() > lastTabIndex) {
        toFocus = tb;
        break;
      }
    }

    // Attempt 2: See if we can find an index with the same value
    if (toFocus == null) {
      for (final BasicTextInput tb : filteredComponents) {
        if (tb == currentTextbox) {
          continue;
        }

        if (tb.getTabIndex() == lastTabIndex) {
          toFocus = tb;
          break;
        }
      }
    }

    // Attempt 3: We attempt to find the rollover point and start there
    if (toFocus == null) {
      BasicTextInput lowestIndexTextBox = currentTextbox;
      for (final BasicTextInput tb : filteredComponents) {

        if (tb == currentTextbox) {
          continue;
        }

        if (lowestIndexTextBox == null || tb.getTabIndex() < lowestIndexTextBox.getTabIndex()) {
          lowestIndexTextBox = tb;
        }
      }

      if (lowestIndexTextBox != null) {
        toFocus = lowestIndexTextBox;
      }
    }

    if (toFocus != null && currentTextbox != null) {
      currentTextbox.deselectAll();
      toFocus.focus();
      toFocus.selectAll();
    }
  }
}
