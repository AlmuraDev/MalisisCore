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
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.FontColors;
import net.malisis.core.util.TriFunction;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BasicDualListContainer<T> extends BasicContainer<BasicDualListContainer<T>> {

  private final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> leftComponentFactory, rightComponentFactory;
  private final String leftTitle, rightTitle;
  protected BasicContainer<?> leftContainer, middleContainer, rightContainer;
  protected BasicList<T> leftDynamicList, rightDynamicList;

  public BasicDualListContainer(final int width, final int height, final String leftTitle, final String rightTitle,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> leftComponentFactory,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> rightComponentFactory) {
    super(width, height);
    this.leftTitle = leftTitle;
    this.rightTitle = rightTitle;
    this.leftDynamicList = new BasicList<>(0, 0);
    this.rightDynamicList = new BasicList<>(0, 0);
    this.leftComponentFactory = leftComponentFactory;
    this.rightComponentFactory = rightComponentFactory;
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicDualListContainer(int, int, String, String, TriFunction, TriFunction)}
   */
  @Deprecated
  public BasicDualListContainer(final MalisisGui gui, final int width, final int height, final String leftTitle, final String rightTitle,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> leftComponentFactory,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> rightComponentFactory) {
    this(width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);
  }

  protected void construct(final MalisisGui gui) {
    this.setBorder(FontColors.WHITE, 1, 185);
    this.setBackgroundAlpha(0);

    this.middleContainer = this.createMiddleContainer(gui);

    final int middleContainerWidth = this.middleContainer == null ? 0 : this.middleContainer.getWidth();

    // Create left container
    this.leftContainer = new BasicContainer<>((this.getWidth() - middleContainerWidth - 5) / 2, 0);
    this.leftContainer.setBackgroundAlpha(0);
    this.leftContainer.setPadding(4, 20, 4, 4);

    final UILabel leftContainerLabel = new UILabel(this.leftTitle);
    leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

    this.leftDynamicList.setItemComponentFactory(this.leftComponentFactory);
    this.leftDynamicList.setItemComponentSpacing(1);
    this.leftDynamicList.setCanDeselect(false);
    this.leftDynamicList.setName("list.left");
    this.leftDynamicList.register(this);

    this.leftContainer.add(leftContainerLabel, this.leftDynamicList);

    // Create right container
    this.rightContainer = new BasicContainer<>((this.getWidth() - middleContainerWidth - 5) / 2, 0);
    this.rightContainer.setBackgroundAlpha(0);
    this.rightContainer.setPadding(4, 20, 4, 4);
    this.rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

    final UILabel rightContainerLabel = new UILabel(this.rightTitle);
    rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

    this.rightDynamicList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
    this.rightDynamicList.setItemComponentFactory(this.rightComponentFactory);
    this.rightDynamicList.setItemComponentSpacing(1);
    this.rightDynamicList.setCanDeselect(false);
    this.rightDynamicList.setName("list.right");
    this.rightDynamicList.register(this);

    this.rightContainer.add(rightContainerLabel, this.rightDynamicList);

    final int innerWidth = this.getRawWidth() - (this.getLeftBorderSize() + this.getRightBorderSize());
    final BasicLine titleLine = new BasicLine(innerWidth);
    titleLine.setPosition(0, BasicScreen.getPaddedY(leftContainerLabel, 2));

    this.add(this.leftContainer);
    if (this.middleContainer != null) {
      this.add(this.middleContainer);
    }
    this.add(this.rightContainer, titleLine);

    // Lines drawn by hand in 6.5.x: under the titles, and down the middle around the middle container
    final BasicLine headerLine = new BasicLine(innerWidth);
    headerLine.setPosition(this.getLeftBorderSize(), this.leftContainer.getTopPadding() - 4);

    final int startX = this.getWidth() / 2;
    final int middleContainerHeight = this.middleContainer == null ? 0 : this.middleContainer.getHeight();
    final int halfHeight = (this.getHeight() - middleContainerHeight - this.getTopBorderSize()) / 2;
    final BasicLine upperLine = new BasicLine(halfHeight, true);
    upperLine.setPosition(startX, 1);
    final BasicLine lowerLine = new BasicLine(halfHeight, true);
    lowerLine.setPosition(startX, 1 + halfHeight + middleContainerHeight - 1);

    this.add(headerLine, upperLine, lowerLine);
  }

  @Subscribe
  private void onItemSelect(final BasicList.SelectEvent<T> event) {
    this.updateControls(event.getNewValue(), this.getSideFromList(event.getComponent()));
  }

  public BasicDualListContainer<T> setItems(final List<T> list, final SideType target) {
    this.getListFromSide(target).setItems(list);
    this.fireEvent(new PopulateEvent<>(this, target));

    return this;
  }

  public Collection<T> getItems(final SideType target) {
    return this.getListFromSide(target).getItems();
  }

  @Nullable
  protected BasicContainer<?> createMiddleContainer(final MalisisGui gui) {
    return null;
  }

  protected BasicList<T> getListFromSide(final SideType target) {
    if (target == SideType.LEFT) {
      return this.leftDynamicList;
    }

    return this.rightDynamicList;
  }

  protected BasicList<T> getOpposingListFromSide(final SideType target) {
    if (target == SideType.LEFT) {
      return this.rightDynamicList;
    }

    return this.leftDynamicList;
  }

  protected SideType getSideFromList(final BasicList<T> list) {
    return list.getName().equalsIgnoreCase("list.left") ? SideType.LEFT : SideType.RIGHT;
  }

  protected SideType getOpposingSideFromList(final BasicList<T> list) {
    return list.getName().equalsIgnoreCase("list.left") ? SideType.RIGHT : SideType.LEFT;
  }

  protected SideType getOppositeSide(final SideType side) {
    return side == SideType.LEFT ? SideType.RIGHT : SideType.LEFT;
  }

  protected void updateControls(@Nullable final T selectedValue, final SideType targetSide) {
    // Deselect on the list that wasn't targeted
    final BasicList<T> targetList = this.getListFromSide(targetSide);

    // Unregister and re-register to avoid recursion
    targetList.unregister(this);
    targetList.setSelectedItem(null);
    targetList.register(this);

    this.fireEvent(new UpdateEvent<>(this));
  }

  public enum SideType {
    LEFT, RIGHT
  }

  public static class PopulateEvent<T> extends ComponentEvent<BasicDualListContainer<T>> {

    public final SideType side;

    public PopulateEvent(final BasicDualListContainer<T> component, final SideType side) {
      super(component);
      this.side = side;
    }
  }

  public static class UpdateEvent<T> extends ComponentEvent<BasicDualListContainer<T>> {

    public UpdateEvent(final BasicDualListContainer<T> component) {
      super(component);
    }
  }
}
