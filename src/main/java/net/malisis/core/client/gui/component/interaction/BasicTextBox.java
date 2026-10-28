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

import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * A single line text box of the common controls. See {@link BasicTextArea} for multiple lines.
 */
public class BasicTextBox extends UITextField implements BasicTextInput {

  @Nullable private Consumer<BasicTextBox> onEnter;
  private boolean acceptsTab = true;
  private boolean acceptsReturn = true;
  private int tabIndex = 0;

  public BasicTextBox() {
    this("");
  }

  public BasicTextBox(final String text) {
    super(text);
    this.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicTextBox()}
   */
  @Deprecated
  public BasicTextBox(final BasicScreen screen) {
    this();
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicTextBox(String)}
   */
  @Deprecated
  public BasicTextBox(final BasicScreen screen, final String text) {
    this(text);
  }

  @Override
  public boolean onKeyTyped(final char keyChar, final int keyCode) {
    // Keys handled here go on to the screen, as they did in 6.5.x
    if (BasicTextInput.handleKey(this, keyCode, false, this.acceptsTab, this.onEnter)) {
      return false;
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
  @Override
  public BasicTextBox focus() {
    BasicTextInput.focus(this);
    return this;
  }

  /**
   * Selects all the text in the component
   *
   * @return The textbox
   */
  @Override
  public BasicTextBox selectAll() {
    BasicTextInput.selectAll(this);
    return this;
  }

  /**
   * Deselect all the text in the component
   *
   * @return The textbox
   */
  @Override
  public BasicTextBox deselectAll() {
    BasicTextInput.deselectAll(this);
    return this;
  }

  /**
   * Wrap the selected text in the provided values
   *
   * @param prefix The prefix
   * @param suffix The suffix
   */
  public void wrap(final String prefix, final String suffix) {
    BasicTextInput.wrap(this, prefix, suffix);
  }

  /**
   * Get whether or not this {@link BasicTextBox} allows ENTER input. A single line box never inserts it, this is kept for 6.5.x code.
   *
   * @return True if ENTER is accepted, otherwise false.
   */
  public boolean getAcceptsReturn() {
    return this.acceptsReturn;
  }

  /**
   * Sets whether or not this {@link BasicTextBox} allows ENTER input. A single line box never inserts it, this is kept for 6.5.x code.
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
  @Override
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
   * Sets the consumer used when using ENTER.
   *
   * @param onEnter The consumer
   * @return The textbox
   */
  public BasicTextBox setOnEnter(final Consumer<BasicTextBox> onEnter) {
    this.onEnter = onEnter;
    return this;
  }
}
