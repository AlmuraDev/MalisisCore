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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.scrolling.UIScrollBar;
import net.malisis.core.client.gui.component.scrolling.UISlimScrollbar;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.util.FontColors;
import net.malisis.core.util.MathUtil;
import net.malisis.core.util.MouseButton;
import net.malisis.core.util.TriFunction;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BasicList<T> extends BasicContainer<BasicList<T>> {

  private final UIScrollBar scrollbar;
  private boolean canDeselect, canInternalClick, isDirty, readOnly;
  private float scrollStep = 0.25F, extraScrollStep = 0.125F;
  private int itemSpacing = 0;
  private List<T> items = new ArrayList<>();
  private TriFunction<MalisisGui, BasicList<T>, T, ? extends ItemComponent<?>> itemComponentFactory = DefaultItemComponent::new;
  @Nullable private T selectedItem;
  @Nullable private Consumer<T> onSelectConsumer;

  public BasicList(final int width, final int height) {
    super(width, height);

    this.scrollbar = new UISlimScrollbar(this, UIScrollBar.Type.VERTICAL);
    this.scrollbar.autoHide = true;

    this.setBackgroundAlpha(0);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicList(int, int)}
   */
  @Deprecated
  public BasicList(final MalisisGui gui, final int width, final int height) {
    this(width, height);
  }

  public int getSize() {
    return this.getItems().size();
  }

  /**
   * Gets an unmodifiable list of items
   *
   * @return The unmodifiable list of items
   */
  public List<T> getItems() {
    return Collections.unmodifiableList(this.items);
  }

  /**
   * Clears current items and adds provided items, if null items collection provided then only a clear will occur
   *
   * @param items The items to set
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setItems(@Nullable final Collection<T> items) {
    this.items.clear();
    if (items != null) {
      this.items.addAll(items);
    }
    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));

    return this;
  }

  /**
   * Gets the item located at the index provided
   *
   * @param index The index
   * @return The item located at the specified index
   */
  @Nullable
  public T getItem(final int index) {
    return this.items.get(index);
  }

  /**
   * Adds the provided item to the list
   *
   * @param item The item to add
   */
  public boolean addItem(final T item) {
    final boolean result = this.items.add(item);

    if (result) {
      this.isDirty = true;
      this.fireEvent(new ItemsChangedEvent<>(this));
    }

    return result;
  }

  /**
   * Adds provided items to the list
   *
   * @param items The items to add
   * @return True if all items were added, otherwise false
   */
  public boolean addItems(final Collection<T> items) {
    final boolean result = this.items.addAll(items);

    if (result) {
      this.isDirty = true;
      this.fireEvent(new ItemsChangedEvent<>(this));
    }

    return result;
  }

  public void addItem(final int index, final T item) {
    this.items.add(index, item);

    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));
  }

  /**
   * Removes specified item from the list
   *
   * @param item The item to remove
   * @return True if the item was removed, otherwise false
   */
  public boolean removeItem(final T item) {
    final boolean result = this.items.remove(item);
    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));

    return result;
  }

  /**
   * Removes the item located at the index provided
   *
   * @param index The index
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> removeItem(final int index) {
    this.items.remove(index);
    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));
    return this;
  }

  /**
   * REmoves specified items from the list
   *
   * @param items The items to remove
   * @return True if all items were removed, otherwise false
   */
  public boolean removeItems(final Collection<T> items) {
    final boolean result = this.items.removeAll(items);
    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));

    return result;
  }

  /**
   * Clears the list of items
   *
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> clearItems() {
    this.items.clear();
    this.setSelectedItem(null);
    this.isDirty = true;
    this.fireEvent(new ItemsChangedEvent<>(this));

    return this;
  }

  /**
   * Gets the selected item if present
   *
   * @return The selected item if present, null otherwise
   */
  @Nullable
  public T getSelectedItem() {
    return this.selectedItem;
  }

  /**
   * Sets the selected item (if list is not read-only)
   *
   * @param item The item to select
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setSelectedItem(@Nullable final T item) {
    return this.setSelectedItem(item, true);
  }

  /**
   * Sets the selected item (if list is not read-only)
   *
   * @param item The item to select
   * @param markDirty Mark the list as dirty
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setSelectedItem(@Nullable final T item, final boolean markDirty) {
    if (!this.readOnly) {
      if (this.fireEvent(new SelectEvent<>(this, this.selectedItem, item))) {
        this.selectedItem = item;
        this.isDirty = markDirty;
        if (this.onSelectConsumer != null) {
          this.onSelectConsumer.accept(item);
        }
      }
    }

    return this;
  }

  /**
   * Gets the spacing between {@link ItemComponent}s
   *
   * @return The spacing
   */
  public int getItemComponentSpacing() {
    return this.itemSpacing;
  }

  /**
   * Sets the spacing between {@link ItemComponent}s
   *
   * @param spacing The space to use between components
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setItemComponentSpacing(final int spacing) {
    this.itemSpacing = spacing;
    return this;
  }

  /**
   * Gets read-only status
   *
   * @return True if read-only, otherwise false
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Sets read-only status
   *
   * @param readOnly The value to set the status as
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  /**
   * Gets deselect status
   *
   * @return True if items can be deselected, otherwise false
   */
  public boolean canDeselect() {
    return this.canDeselect;
  }

  /**
   * Sets deselect status
   *
   * @param canDeselect The value to set the status as
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setCanDeselect(final boolean canDeselect) {
    this.canDeselect = canDeselect;
    return this;
  }

  /**
   * Gets internal click status
   *
   * @return The value to set the status as
   */
  public boolean canInternalClick() {
    return this.canInternalClick;
  }

  /**
   * Sets internal click status
   *
   * @param canInternalClick canInternalClick The internal click enable status. If true the clicks on an item component will not count against
   * the item component.
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setCanInternalClick(final boolean canInternalClick) {
    this.canInternalClick = canInternalClick;
    return this;
  }

  /**
   * Gets the item component factory
   *
   * @return The item component factory
   */
  public TriFunction<MalisisGui, BasicList<T>, T, ? extends ItemComponent<?>> getItemComponentFactory() {
    return this.itemComponentFactory;
  }

  /**
   * Sets the item component factory
   *
   * @param factory The component factory
   * @return The {@link BasicList <T>}
   */
  public BasicList<T> setItemComponentFactory(final TriFunction<MalisisGui, BasicList<T>, T, ? extends ItemComponent<?>> factory) {
    this.itemComponentFactory = factory;
    return this;
  }

  @Nullable
  public Consumer<T> getSelectConsumer() {
    return this.onSelectConsumer;
  }

  public BasicList<T> setSelectConsumer(final Consumer<T> onSelectConsumer) {
    this.onSelectConsumer = onSelectConsumer;
    return this;
  }

  public UIScrollBar getScrollBar() {
    return this.scrollbar;
  }

  public void markDirty() {
    this.isDirty = true;
  }

  public void createItemComponents() {
    final float scrollPoint = this.getScrollBar().offset();
    final Integer focusedX = MalisisGui.getFocusedComponent() == null ? null : MalisisGui.getFocusedComponent().screenX();
    final Integer focusedY = MalisisGui.getFocusedComponent() == null ? null : MalisisGui.getFocusedComponent().screenY();
    final boolean wasItemFocused = focusedX != null && focusedY != null && this.getComponentAt(focusedX, focusedY) != null;

    this.removeAll();

    int startY = 0;
    for (final T item : this.items) {
      final ItemComponent<?> component = this.itemComponentFactory.apply(this.getGui(), this, item);
      component.attachData(item);
      component.setPosition(0, startY);

      if (wasItemFocused && component.screenX() == focusedX && component.screenY() == focusedY) {
        component.setFocused(true);
      }

      this.add(component);

      startY += component.getHeight() + this.itemSpacing;
    }

    this.getScrollBar().scrollTo(scrollPoint);

    this.isDirty = false;

    // Update scroll step
    final float step = MathUtil.scalef(1, 0, this.items.size(), 0f, 1f);
    this.scrollStep = step;
    this.extraScrollStep = step * 10;
  }

  @Override
  public void setClipContent(final boolean clipContent) {
  }

  @Override
  public boolean shouldClipContent() {
    return true;
  }

  @Override
  public float getScrollStep() {
    return GuiScreen.isCtrlKeyDown() ? this.extraScrollStep : this.scrollStep;
  }

  @Override
  public void render(final GuiRenderer renderer) {
    if (this.isDirty) {
      this.createItemComponents();
    }
    super.render(renderer);
  }

  /**
   * A row of a {@link BasicList}. Subclasses keep the (gui, parent, item) constructor used by the item component factory of the list,
   * and add their children in {@link #construct(MalisisGui)}.
   */
  public static class ItemComponent<T> extends BasicContainer<ItemComponent<T>> {

    private static final int BORDER_COLOR = 0x808080;
    private static final int INNER_COLOR = 0x000000;
    private static final int INNER_HOVER_COLOR = 0x282828;
    private static final int INNER_SELECTED_COLOR = 0x414141;

    protected T item;
    @Nullable private Consumer<T> onDoubleClick;

    public ItemComponent(final MalisisGui gui, final BasicList<T> parent, final T item) {
      this(gui, parent, item, null);
    }

    public ItemComponent(final MalisisGui gui, final BasicList<T> parent, final T item, @Nullable final Consumer<T> onDoubleClick) {
      // Set the parent
      this.setParent(parent);
      // construct() and the item code it calls use getGui() before this is added to the screen, as in 6.5.x
      this.gui = gui != null ? gui : parent.getGui();

      // Set the item
      this.item = item;

      // Set the consumer
      this.onDoubleClick = onDoubleClick;

      // Set padding
      this.setPadding(3, 3);

      // Set colors
      this.setColor(INNER_COLOR);
      this.setBorder(BORDER_COLOR, 1, 255);

      // Set default size
      setSize(0, 15);

      this.construct(gui);
    }

    private static boolean hasParent(final UIComponent parent, final UIComponent component) {
      final UIComponent componentParent = component.getParent();
      if (componentParent == null) {
        return false;
      }

      if (componentParent == parent) {
        return true;
      }

      if (componentParent.getParent() != null) {
        return hasParent(parent, componentParent.getParent());
      }

      return false;
    }

    protected void construct(final MalisisGui gui) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onClick() {
      final UIComponent component = getComponentAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
      final BasicList parent = (BasicList) this.parent;

      if (this.equals(component) || !this.equals(component) && !parent.canInternalClick && hasParent(this, component)) {
        if (parent.isReadOnly()) {
          return false;
        }

        if (parent.canDeselect()) {
          parent.setSelectedItem(parent.getSelectedItem() == this.item ? null : this.item, false);
        } else {
          parent.setSelectedItem(this.item, false);
        }
      }
      return true;
    }

    @Override
    public void render(final GuiRenderer renderer) {
      if (this.parent instanceof BasicList) {
        final BasicList<?> parent = (BasicList<?>) this.parent;
        // Fill the width of the list, less the scrollbar when it shows
        final int width = parent.getWidth() - parent.getLeftPadding() - parent.getRightPadding() - (parent.getScrollBar().isEnabled() ?
          parent.getScrollBar().getWidth() + 1 : 0);
        if (width != this.getWidth()) {
          this.setSize(width, this.getRawHeight());
        }

        final UIComponent hovered = MalisisGui.getHoveredComponent();
        if (parent.getSelectedItem() == this.item) {
          this.setColor(INNER_SELECTED_COLOR);
        } else if (hovered != null && (this.equals(hovered) || this.equals(hovered.getParent()))) {
          this.setColor(INNER_HOVER_COLOR);
        } else {
          this.setColor(INNER_COLOR);
        }
      }
      super.render(renderer);
    }

    @Override
    public boolean onDoubleClick(final MouseButton button) {
      if (button != MouseButton.LEFT) {
        return super.onDoubleClick(button);
      }

      final UIComponent componentAt = this.getComponentAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
      final UIComponent parentComponentAt = componentAt == null ? null : componentAt.getParent();
      if (!(componentAt instanceof BasicList.ItemComponent) && !(parentComponentAt instanceof BasicList.ItemComponent)) {
        return super.onDoubleClick(button);
      }

      if (this.onDoubleClick != null) {
        this.onDoubleClick.accept(item);
      }

      return true;
    }

    public void setOnDoubleClickConsumer(@Nullable final Consumer<T> onDoubleClick) {
      this.onDoubleClick = onDoubleClick;
    }
  }

  public static class DefaultItemComponent<T> extends ItemComponent<T> {

    DefaultItemComponent(final MalisisGui gui, final BasicList<T> parent, final T item) {
      super(gui, parent, item);
    }

    @Override
    protected void construct(final MalisisGui gui) {
      final UILabel label = new UILabel(this.item.toString());
      label.setFontOptions(FontColors.WHITE_FO);
      // 6.5.x drew the text at 2,3 from the corner of the row, inside its padding of 3
      label.setPosition(-1, 0);
      this.add(label);
    }
  }

  public static class ItemsChangedEvent<T> extends ComponentEvent<BasicList<T>> {

    public ItemsChangedEvent(final BasicList<T> component) {
      super(component);
    }
  }

  public static class SelectEvent<T> extends ComponentEvent.ValueChange<BasicList<T>, T> {

    public SelectEvent(final BasicList<T> component, @Nullable final T oldValue, @Nullable final T newValue) {
      super(component, oldValue, newValue);
    }
  }
}
