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
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.event.ComponentEvent;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

/**
 * A text input of the common controls ({@link BasicTextBox}, {@link BasicTextArea}): part of the tab order of its {@link BasicContainer}.
 */
public interface BasicTextInput {

  int getTabIndex();

  boolean isEditable();

  boolean isEnabled();

  boolean isVisible();

  /**
   * Focuses this input.
   *
   * @return this input
   */
  BasicTextInput focus();

  /**
   * Selects all the text.
   *
   * @return this input
   */
  BasicTextInput selectAll();

  /**
   * Deselects all the text.
   *
   * @return this input
   */
  BasicTextInput deselectAll();

  // Behavior shared by the common text inputs. Package-private access to UITextField's protected state.

  /**
   * Handles the keys the common text inputs treat specially: ENTER (when not inserted as text), ESCAPE (closes the parent
   * {@link BasicForm}) and TAB (when not accepted as text, moves through the tab order).
   *
   * @return true if the key was handled here and must not reach the text field
   */
  static <T extends UITextField> boolean handleKey(final T field, final int keyCode, final boolean enterIsText, final boolean acceptsTab,
    final Consumer<T> onEnter) {
    if (keyCode == Keyboard.KEY_RETURN && !enterIsText) {
      if (onEnter != null) {
        onEnter.accept(field);
      }
      return true;
    }

    if (keyCode == Keyboard.KEY_ESCAPE) {
      closeForm(field);
      return true;
    }

    if (keyCode == Keyboard.KEY_TAB && !acceptsTab && field.getParent() instanceof BasicContainer) {
      if (BasicScreen.isShiftKeyDown()) {
        ((BasicContainer<?>) field.getParent()).tabToLastControl();
      } else {
        ((BasicContainer<?>) field.getParent()).tabToNextControl();
      }
      return true;
    }

    return false;
  }

  static void focus(final UITextField field) {
    if (!field.isEnabled()) {
      return;
    }
    field.setFocused(true);
    if (field.getGui() instanceof BasicScreen) {
      ((BasicScreen) field.getGui()).setFocusedComponent(field);
    }
  }

  static void selectAll(final UITextField field) {
    field.selectingText = true;
    field.selectionCursor.jumpToStart();
    field.cursor.jumpToEnd();
  }

  static void deselectAll(final UITextField field) {
    field.selectingText = false;
    field.selectionCursor.jumpTo(0);
    field.cursor.jumpTo(field.text.length());
  }

  /**
   * Wraps the selected text in the provided values.
   */
  static void wrap(final UITextField field, final String prefix, final String suffix) {
    if (field.getSelectedText().isEmpty()) {
      return;
    }

    final String oldValue = field.text.toString();
    final boolean cursorIsStart = field.cursor.index() < field.selectionCursor.index();
    final int start = Math.min(field.cursor.index(), field.selectionCursor.index());
    final int end = Math.max(field.cursor.index(), field.selectionCursor.index());
    String newValue = new StringBuilder(oldValue).insert(start, prefix).insert(end + prefix.length(), suffix).toString();

    if (field.filterFunction != null) {
      newValue = field.filterFunction.apply(newValue);
    }

    if (!field.fireEvent(new ComponentEvent.ValueChange<>(field, oldValue, newValue))) {
      return;
    }

    field.text = new StringBuilder(newValue);
    field.guiText.setText(newValue);

    final int jumpLength = cursorIsStart ? suffix.length() : prefix.length();
    field.cursor.jumpBy(jumpLength);
    field.selectionCursor.jumpBy(jumpLength);
  }

  static void closeForm(final UIComponent component) {
    if (component.getParent() instanceof BasicForm) {
      ((BasicForm) component.getParent()).onClose();
      return;
    }

    if (component.getParent() != null) {
      closeForm(component.getParent());
    }
  }
}
