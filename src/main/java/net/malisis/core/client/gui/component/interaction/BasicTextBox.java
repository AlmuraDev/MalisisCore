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
package net.malisis.core.client.gui.component.interaction;

import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.event.ComponentEvent;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class BasicTextBox extends UITextField {

  @Nullable private Consumer<BasicTextBox> onEnter;
  private boolean acceptsTab = true;
  private boolean acceptsReturn = true;
  private int tabIndex = 0;

  public BasicTextBox(final BasicScreen screen) {
    this(screen, "", false);
  }

  public BasicTextBox(final BasicScreen screen, final String text) {
    this(screen, text, false);
  }

  public BasicTextBox(final BasicScreen screen, final boolean multiLine) {
    this(screen, "", multiLine);
  }

  public BasicTextBox(final BasicScreen screen, final String text, final boolean multiLine) {
    super(screen, text, multiLine);
    this.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
  }

  @Override
  public boolean onKeyTyped(final char keyChar, final int keyCode) {
    if (keyCode == Keyboard.KEY_RETURN && (!this.multiLine || !this.acceptsReturn)) {
      if (this.onEnter != null) {
        this.onEnter.accept(this);
      }
      return false;
    }

    if (keyCode == Keyboard.KEY_ESCAPE) {
      this.closeDeep(this);
      return false;
    }

    if (keyCode == Keyboard.KEY_TAB && !this.acceptsTab) {
      if (this.parent instanceof BasicContainer) {
        if (BasicScreen.isShiftKeyDown()) {
          // Tab backwards
          ((BasicContainer) this.parent).tabToLastControl();
        } else {
          // Tab forwards
          ((BasicContainer) this.parent).tabToNextControl();
        }
        return false;
      }
    }

    return super.onKeyTyped(keyChar, keyCode);
  }

  @Override
  public UITextField setEditable(final boolean editable) {
    this.setFontOptions(editable ? UIConstants.DEFAULT_TEXTBOX_FO : UIConstants.READ_ONLY_TEXTBOX_FO);
    return super.setEditable(editable);
  }

  /**
   * Focuses on the {@link BasicTextBox}
   *
   * @return The textbox
   */
  public BasicTextBox focus() {
    if (!isEnabled()) {
      return this;
    }
    this.setFocused(true);
    ((BasicScreen) getGui()).setFocusedComponent(this);

    return this;
  }

  /**
   * Selects all the text in the component
   *
   * @return The textbox
   */
  public BasicTextBox selectAll() {
    this.selectingText = true;
    this.selectionPosition.jumpToBeginning();
    this.cursorPosition.jumpToEnd();

    return this;
  }

  /**
   * Deselect all the text in the component
   *
   * @return The textbox
   */
  public BasicTextBox deselectAll() {
    this.selectingText = false;
    this.selectionPosition.jumpTo(0);
    this.cursorPosition.jumpTo(this.text.length());

    return this;
  }

  /**
   * Wrap the selected text in the provided values
   *
   * @param prefix The prefix
   * @param suffix The suffix
   */
  public void wrap(final String prefix, final String suffix) {
    if (this.getSelectedText().isEmpty()) {
      return;
    }

    final StringBuilder oldText = this.text;
    final String oldValue = this.text.toString();
    final boolean cursorIsStart = this.cursorPosition.getPosition() < this.selectionPosition.getPosition();
    final CursorPosition start = cursorIsStart ? this.cursorPosition : this.selectionPosition;
    final CursorPosition end = start == this.cursorPosition ? this.selectionPosition : this.cursorPosition;
    String newValue = oldText.insert(start.getPosition(), prefix).insert(end.getPosition() + prefix.length(), suffix).toString();

    if (this.filterFunction != null) {
      newValue = this.filterFunction.apply(newValue);
    }

    if (!fireEvent(new ComponentEvent.ValueChange<>(this, oldValue, newValue))) {
      return;
    }

    this.text = new StringBuilder(newValue);
    buildLines();

    final int jumpLength = cursorIsStart ? suffix.length() : prefix.length();
    this.cursorPosition.jumpBy(jumpLength);
    this.selectionPosition.jumpBy(jumpLength);
  }

  /**
   * Get whether or not this {@link BasicTextBox} allows ENTER input, only relevant when multiline.
   *
   * @return True if ENTER is accepted, otherwise false.
   */
  public boolean getAcceptsReturn() {
    return this.acceptsReturn;
  }

  /**
   * Sets whether or not this {@link BasicTextBox} allows ENTER input, only relevant when multipline.
   *
   * @param acceptsReturn True if ENTER is accepted, otherwise false.
   * @return The textbox
   */
  public BasicTextBox setAcceptsReturn(final boolean acceptsReturn) {
    this.acceptsReturn = acceptsReturn;
    return this;
  }

  /**
   * Get whether or not this {@link BasicTextBox} allows TAB input.
   *
   * @return True if TAB is accepted, otherwise false.
   */
  public boolean getAcceptsTab() {
    return this.acceptsTab;
  }

  /**
   * Sets whether or not this {@link BasicTextBox} allows TAB input.
   *
   * @param acceptsTab True if TAB is accepted, otherwise false.
   * @return The textbox
   */
  public BasicTextBox setAcceptsTab(final boolean acceptsTab) {
    this.acceptsTab = acceptsTab;
    return this;
  }

  /**
   * Gets the index used for tab order. If multiple controls have the same index then the first found will be used.
   *
   * @return The index
   */
  public int getTabIndex() {
    return this.tabIndex;
  }

  /**
   * Sets the index used for tab order. If multiple controls have the same index then the first found will be used.
   *
   * @param tabIndex The index
   * @return The textbox
   */
  public BasicTextBox setTabIndex(final int tabIndex) {
    this.tabIndex = tabIndex;
    return this;
  }

  /**
   * Sets the consumer used when using ENTER (only used if {@link BasicTextBox#getAcceptsReturn()} returns false)
   *
   * @param onEnter The consumer
   * @return The textbox
   */
  public BasicTextBox setOnEnter(final Consumer<BasicTextBox> onEnter) {
    this.onEnter = onEnter;
    return this;
  }

  private void closeDeep(final UIComponent component) {
    if (component.getParent() instanceof BasicForm) {
      ((BasicForm) component.getParent()).onClose();
      return;
    }

    if (component.getParent() != null) {
      this.closeDeep(component.getParent());
    }
  }
}
