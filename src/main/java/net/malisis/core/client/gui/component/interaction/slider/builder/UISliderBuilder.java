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
package net.malisis.core.client.gui.component.interaction.slider.builder;

import com.google.common.base.Converter;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UISliderBuilder {

  private final MalisisGui gui;
  @Nullable private UIContainer container;
  @Nullable private FontOptions fontOptions;
  private FontOptions hoverFontOptions;
  private int width, height, x, y, z, anchor;
  @Nullable private MalisisFont font;
  @Nullable private Object object;
  @Nullable private String text = "";
  @Nullable private Object value;
  @Nullable private UITooltip tooltip;
  @Nullable private Converter converter;
  private float scrollStep;
  private boolean enabled = true;

  public UISliderBuilder(final MalisisGui gui, @Nullable final Converter converter) {
    this.gui = gui;
    this.converter = converter;
  }

  public UISliderBuilder text(final String text) {
    this.text = text;
    return this;
  }

  public UISliderBuilder tooltip(final String text) {
    return this.tooltip(new UITooltip(this.gui, text, 15));
  }

  public UISliderBuilder tooltip(final UITooltip tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  public UISliderBuilder size(final int width, final int height) {
    this.width(width);
    this.height(height);
    return this;
  }

  public UISliderBuilder width(final int width) {
    this.width = width;
    return this;
  }

  public UISliderBuilder height(final int height) {
    this.height = height;
    return this;
  }

  public UISliderBuilder fontOptions(final FontOptions fontOptions) {
    this.fontOptions = fontOptions;
    return this;
  }

  public UISliderBuilder font(final MalisisFont font) {
    this.font = font;
    return this;
  }

  public UISliderBuilder enabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public UISliderBuilder position(final int x, final int y) {
    return this.position(x, y, 0);
  }

  public UISliderBuilder position(final int x, final int y, final int z) {
    this.x(x);
    this.y(y);
    this.z(z);
    return this;
  }

  public UISliderBuilder x(final int x) {
    this.x = x;
    return this;
  }

  public UISliderBuilder y(final int y) {
    this.y = y;
    return this;
  }

  public UISliderBuilder z(final int z) {
    this.z = z;
    return this;
  }

  public UISliderBuilder anchor(final int anchor) {
    this.anchor = anchor;
    return this;
  }

  public UISliderBuilder listener(final Object object) {
    this.object = object;
    return this;
  }

  public UISliderBuilder value(final Object value) {
    this.value = value;
    return this;
  }

  public UISliderBuilder container(final UIContainer container) {
    this.container = container;
    return this;
  }

  public UISliderBuilder scrollStep(final float scrollStep) {
    this.scrollStep = scrollStep;
    return this;
  }

  @SuppressWarnings("unchecked")
  public UISlider build(final String id) {
    final UISlider slider = new UISlider(this.gui, this.width, this.converter, this.text);
    slider.setPosition(this.x, this.y);
    if (id != null) {
      slider.setName(id);
    }
    if (this.tooltip != null) {
      slider.setTooltip(this.tooltip);
    }
    if (this.width != 0) {
      slider.setSize(this.width, slider.getHeight());
    }
    if (this.height != 0) {
      slider.setSize(slider.getWidth(), this.height);
    }
    if (this.anchor != 0) {
      slider.setAnchor(this.anchor);
    }
    if (this.object != null) {
      slider.register(this.object);
    }
    if (this.fontOptions != null) {
      slider.setFontOptions(this.fontOptions);
    }
    if (this.hoverFontOptions != null) {
      slider.setHoveredFontOptions(this.hoverFontOptions);
    }
    if (this.font != null) {
      slider.setFont(this.font);
    }
    if (this.container != null) {
      this.container.add(slider);
    }
    if (this.value != null) {
      slider.setValue(this.value);
    }
    if (this.scrollStep != 0) {
      slider.setScrollStep(this.scrollStep);
    }
    slider.setEnabled(this.enabled);
    slider.setZIndex(this.z);

    return slider;
  }
}
