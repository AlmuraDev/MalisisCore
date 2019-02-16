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
import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.util.FontColors;
import net.malisis.core.util.TriFunction;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.event.ComponentEvent;
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

  public BasicDualListContainer(final MalisisGui gui, final int width, final int height, final String leftTitle, final String rightTitle,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> leftComponentFactory,
    final TriFunction<MalisisGui, BasicList<T>, T, ? extends BasicList.ItemComponent<?>> rightComponentFactory) {
    super(gui, width, height);

    this.leftTitle = leftTitle;
    this.rightTitle = rightTitle;

    this.leftDynamicList = new BasicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
    this.rightDynamicList = new BasicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);

    this.leftComponentFactory = leftComponentFactory;
    this.rightComponentFactory = rightComponentFactory;
  }

  protected void construct(final MalisisGui gui) {
    this.setBorder(FontColors.WHITE, 1, 185);
    this.setBackgroundAlpha(0);

    this.middleContainer = this.createMiddleContainer(gui);

    final int middleContainerWidth = this.middleContainer == null ? 0 : this.middleContainer.getWidth();

    // Create left container
    this.leftContainer = new BasicContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
    this.leftContainer.setBackgroundAlpha(0);
    this.leftContainer.setPadding(4, 4);
    this.leftContainer.setTopPadding(20);

    final UILabel leftContainerLabel = new UILabel(gui, this.leftTitle);
    leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

    this.leftDynamicList.setItemComponentFactory(this.leftComponentFactory);
    this.leftDynamicList.setItemComponentSpacing(1);
    this.leftDynamicList.setCanDeselect(false);
    this.leftDynamicList.setName("list.left");
    this.leftDynamicList.register(this);

    this.leftContainer.add(leftContainerLabel, this.leftDynamicList);

    // Create right container
    this.rightContainer = new BasicContainer(gui, (this.width - middleContainerWidth - 5) / 2, UIComponent.INHERITED);
    this.rightContainer.setBackgroundAlpha(0);
    this.rightContainer.setPadding(4, 4);
    this.rightContainer.setTopPadding(20);
    this.rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

    final UILabel rightContainerLabel = new UILabel(gui, this.rightTitle);
    rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

    this.rightDynamicList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
    this.rightDynamicList.setItemComponentFactory(this.rightComponentFactory);
    this.rightDynamicList.setItemComponentSpacing(1);
    this.rightDynamicList.setCanDeselect(false);
    this.rightDynamicList.setName("list.right");
    this.rightDynamicList.register(this);

    this.rightContainer.add(rightContainerLabel, this.rightDynamicList);

    final BasicLine titleLine = new BasicLine(gui, this.getRawWidth() - (this.getLeftBorderSize() + this.getRightBorderSize()));
    titleLine.setPosition(0, BasicScreen.getPaddedY(leftContainerLabel, 2));

    this.add(this.leftContainer, this.middleContainer, this.rightContainer, titleLine);
  }

  @Override
  public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
    super.drawBackground(renderer, mouseX, mouseY, partialTick);

    final int startX = this.width / 2;
    final int middleContainerHeight = this.middleContainer == null ? 0 : this.middleContainer.getHeight();
    final int halfHeight = (this.height - middleContainerHeight - this.getTopBorderSize()) / 2;
    int startY = 1;

    // Draw: top-left -> top-right (title line)
    renderer.drawRectangle(this.getLeftBorderSize(), this.leftContainer.getTopPadding() - 4, 1,
      this.getRawWidth() - (this.getLeftBorderSize() + this.getRightBorderSize()), 1, FontColors.WHITE, 185);

    // Draw: top -> middle_section
    renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
    startY += halfHeight;

    // Skip: middle_section
    startY += middleContainerHeight - 1;

    // Draw: middle_section -> bottom
    renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
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
