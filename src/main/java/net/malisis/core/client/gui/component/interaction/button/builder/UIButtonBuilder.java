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
package net.malisis.core.client.gui.component.interaction.button.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public final class UIButtonBuilder {

  private final MalisisGui gui;
  @Nullable private UIContainer container;
  @Nullable private FontOptions fontOptions;
  @Nullable private FontOptions hoverFontOptions;
  @Nullable private MalisisFont font;
  @Nullable private Object object;
  @Nullable private String text;
  @Nullable private UIImage image;
  @Nullable private UITooltip tooltip;
  @Nullable private Runnable onClickRunnable;
  private int width, height, x, y, z, anchor;
  private boolean enabled = true;
  private boolean visible = true;

  public UIButtonBuilder(final MalisisGui gui) {
    this.gui = gui;
  }

  public UIButtonBuilder text(final String text) {
    this.text = text;
    return this;
  }

  public UIButtonBuilder tooltip(final String text) {
    return this.tooltip(new UITooltip(this.gui, text, 15));
  }

  public UIButtonBuilder tooltip(final UITooltip tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  public UIButtonBuilder image(final UIImage image) {
    this.image = image;
    return this;
  }

  public UIButtonBuilder icon(final GuiIcon icon) {
    return this.image(new UIImage(this.gui, null, icon));
  }

  public UIButtonBuilder texture(final GuiTexture texture) {
    return this.image(new UIImage(this.gui, texture, null));
  }

  public UIButtonBuilder size(final int width, final int height) {
    this.width(width);
    this.height(height);
    return this;
  }

  public UIButtonBuilder size(final int size) {
    this.size(size, size);
    return this;
  }

  public UIButtonBuilder width(final int width) {
    this.width = width;
    return this;
  }

  public UIButtonBuilder height(final int height) {
    this.height = height;
    return this;
  }

  public UIButtonBuilder fontOptions(final FontOptions fontOptions) {
    this.fontOptions = fontOptions;
    return this;
  }

  public UIButtonBuilder hoverFontOptions(final FontOptions hoverFontOptions) {
    this.hoverFontOptions = hoverFontOptions;
    return this;
  }

  public UIButtonBuilder font(final MalisisFont font) {
    this.font = font;
    return this;
  }

  public UIButtonBuilder enabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public UIButtonBuilder visible(final boolean visible) {
    this.visible = visible;
    return this;
  }

  public UIButtonBuilder position(final int x, final int y) {
    return this.position(x, y, 0);
  }

  public UIButtonBuilder position(final int x, final int y, final int z) {
    this.x(x);
    this.y(y);
    this.z(z);
    return this;
  }

  public UIButtonBuilder x(final int x) {
    this.x = x;
    return this;
  }

  public UIButtonBuilder y(final int y) {
    this.y = y;
    return this;
  }

  public UIButtonBuilder z(final int z) {
    this.z = z;
    return this;
  }

  public UIButtonBuilder anchor(final int anchor) {
    this.anchor = anchor;
    return this;
  }

  public UIButtonBuilder listener(final Object object) {
    this.object = object;
    return this;
  }

  public UIButtonBuilder container(final UIContainer container) {
    this.container = container;
    return this;
  }

  public UIButtonBuilder onClick(final Runnable onClickRunnable) {
    this.onClickRunnable = onClickRunnable;
    return this;
  }

  @SuppressWarnings("unchecked")
  public UIButton build(final String id) {
    checkNotNull(id);

    final UIButton button = new UIButton(this.gui);
    button.setPosition(this.x, this.y);
    button.setName(id);

    if (this.text != null) {
      button.setText(this.text);
    }
    if (this.fontOptions != null) {
      button.setFontOptions(this.fontOptions);
    }
    if (this.image != null) {
      button.setImage(this.image);
    }
    if (this.tooltip != null) {
      button.setTooltip(this.tooltip);
    }
    if (this.width != 0) {
      button.setSize(this.width, button.getHeight());
    }
    if (this.height != 0) {
      button.setSize(button.getWidth(), this.height);
    }
    if (this.anchor != 0) {
      button.setAnchor(this.anchor);
    }
    if (this.object != null) {
      button.register(this.object);
    }
    if (this.hoverFontOptions != null) {
      button.setHoveredFontOptions(this.hoverFontOptions);
    }
    if (this.font != null) {
      button.setFont(this.font);
    }
    if (this.container != null) {
      this.container.add(button);
    }
    if (this.onClickRunnable != null) {
      button.onClick(this.onClickRunnable);
    }
    button.setEnabled(this.enabled);
    button.setVisible(this.visible);
    button.setZIndex(this.z);

    return button;
  }
}
