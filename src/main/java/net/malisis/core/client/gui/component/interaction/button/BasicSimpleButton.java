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
package net.malisis.core.client.gui.component.interaction.button;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A {@link UIButton} without its background: only the text or image is drawn.
 */
@SideOnly(Side.CLIENT)
public class BasicSimpleButton extends UIButton {

  private FontOptions textFontOptions = this.fontOptions;

  public BasicSimpleButton() {
    super();
    this.setBackground(() -> null);
  }

  public BasicSimpleButton(final String text) {
    this();
    this.setText(text);
  }

  public BasicSimpleButton(final UIComponent content) {
    this();
    this.setContent(content);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicSimpleButton()}
   */
  @Deprecated
  public BasicSimpleButton(final MalisisGui gui) {
    this();
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicSimpleButton(String)}
   */
  @Deprecated
  public BasicSimpleButton(final MalisisGui gui, final String text) {
    this(text);
  }

  /**
   * @deprecated components no longer take their gui, use {@link #BasicSimpleButton(UIComponent)}
   */
  @Deprecated
  public BasicSimpleButton(final MalisisGui gui, final UIImage image) {
    this(image);
  }

  @Override
  public void setText(final String text) {
    this.setContent(GuiText.of(text, this.textFontOptions));
  }

  /**
   * Sets the {@link FontOptions} of the text, use {@link FontOptions.FontOptionsBuilder#when} for hovered or disabled variants.
   *
   * @param fontOptions the font options
   */
  public void setFontOptions(final FontOptions fontOptions) {
    this.textFontOptions = fontOptions;
    if (this.content instanceof GuiText) {
      ((GuiText) this.content).setFontOptions(fontOptions);
    }
  }

  public FontOptions getFontOptions() {
    return this.textFontOptions;
  }
}
