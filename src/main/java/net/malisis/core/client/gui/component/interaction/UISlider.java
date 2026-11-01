/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

import static com.google.common.base.Preconditions.*;
import static net.malisis.core.client.gui.element.position.Positions.*;

import com.google.common.base.Converter;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.malisis.core.util.Silenced;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.Objects;

/**
 * @author Ordinastie
 *
 */
public class UISlider<T> extends UIComponent implements IContentHolder
{
	private static int SLIDER_WIDTH = 8;

	/** Text to display over the slider. */
	protected final GuiText text;

	/** Current value. */
	protected T value;
	/** Position offset of the slider. */
	protected float offset;

	/** Amount of offset scrolled by when using the scroll wheel. */
	protected float scrollStep = 0.05F;
	/** Font options set with {@link #setFontOptions(FontOptions)} and {@link #setHoveredFontOptions(FontOptions)}. */
	protected FontOptions fontOptions;
	protected FontOptions hoveredFontOptions;

	/** Converter from float (0-1 offset) to the value. */
	protected Converter<Float, T> converter;

	public UISlider(int width, Converter<Float, T> converter, String text)
	{
		this.converter = checkNotNull(converter);
		this.value = converter.convert(0F);

		//6.5.x labels are String.format patterns of the value ("Opacity: %d"); passed through as is, the % reached the translation and
		//showed "Format error", so they are formatted here and shown as {label}
		boolean formatted = text != null && text.contains("%") && !text.contains("{");
		this.text = GuiText	.builder()
							.parent(this)
							.text(formatted ? "{label}" : text)
							.position(this::textPosition, o -> middleAligned(o, 0))
							.bind("value", this::getValue)
							.bind("label", () -> formatted ? formatLabel(text) : "")
							.zIndex(this::getZIndex)
							.fontOptions(FontOptions.builder().color(0xFFFFFF).shadow().when(this::isHovered).color(0xFFFFA0).build())
							.build();

		setSize(Size.of(width, 20));

		GuiShape sliderShape = GuiShape	.builder(this)
										.position(this::scrollPosition, 0)
										.size(Size.of(SLIDER_WIDTH, () -> size().height()))
										.icon(GuiIcon.SLIDER)
										.border(5)
										.build();
		setBackground(GuiShape.builder(this).icon(GuiIcon.SLIDER_BG).build());
		//text over the slider knob, as in 6.5.x
		setForeground(sliderShape.and(this.text));
	}

	private String formatLabel(String pattern)
	{
		String str = Silenced.get(() -> String.format(pattern, value));
		return str != null ? str : TextFormatting.ITALIC + "Format error";
	}

	//#region Getters/Setters
	@Override
	public GuiText content()
	{
		return text;
	}

	/**
	 * Sets the {@link FontOptions} used for the text while this {@link UISlider} is hovered.
	 *
	 * @param fontOptions the font options, null to use the regular ones
	 * @return this UI slider
	 */
	public UISlider<T> setHoveredFontOptions(FontOptions fontOptions)
	{
		this.hoveredFontOptions = fontOptions;
		applyFontOptions();
		return this;
	}

	/**
	 * Sets the value for this {@link UISlider}.
	 *
	 * @param value the value
	 * @return this UI slider
	 */
	public UISlider<T> setValue(T value)
	{
		if (Objects.equals(this.value, value))
			return this;
		if (!fireEvent(new ComponentEvent.ValueChange<>(this, this.value, value)))
			return this;

		this.value = value;
		this.offset = MathHelper.clamp(converter.reverse().convert(value), 0, 1);
		return this;
	}

	/**
	 * Gets the value for this {@link UISlider}.
	 *
	 * @return the value
	 */
	public T getValue()
	{
		return value;
	}

	public void setFontOptions(FontOptions fontOptions)
	{
		this.fontOptions = fontOptions;
		applyFontOptions();
	}

	private void applyFontOptions()
	{
		if (fontOptions == null)
			fontOptions = text.getFontOptions();
		text.setFontOptions(hoveredFontOptions == null	? fontOptions
														: fontOptions.toBuilder().when(this::isHovered).from(hoveredFontOptions).build());
	}

	public int scrollPosition()
	{
		return (int) (offset * (size().width() - SLIDER_WIDTH));
	}

	public int textPosition()
	{
		//centered over the knob as in 6.5.x; moving it out of the knob's way pushed longer labels out of the slider
		return (size().width() - text.size().width()) / 2;
	}

	/**
	 * Sets the amount of offset to scroll with the wheel.
	 *
	 * @param scrollStep the scroll step
	 * @return the UI slider
	 */
	public UISlider<T> setScrollStep(float scrollStep)
	{
		this.scrollStep = scrollStep;
		return this;
	}

	//#end Getters/Setters
	@Override
	public boolean onClick()
	{
		slideTo();
		return true;
	}

	@Override
	public boolean onScrollWheel(int delta)
	{
		slideTo(scrollTarget(delta));
		return true;
	}

	/**
	 * The offset one scroll notch moves to: the next value in the scroll direction. A slider with few values (options, 0-255)
	 * steps one value; scrolling by {@link #scrollStep} couldn't reach the next option, the knob snapped back to its own, and it
	 * jumped many values at a time. A slider whose value changes with any movement keeps the {@link #scrollStep}.
	 */
	private float scrollTarget(int delta)
	{
		float fine = 0.001F * Math.signum(delta);
		float o = offset;
		for (int i = 1; i <= 1000; i++)
		{
			o = MathHelper.clamp(offset + fine * i, 0, 1);
			if (!Objects.equals(converter.convert(o), value))
				return i == 1 ? offset + delta * scrollStep : o;
			if (o == 0 || o == 1)
				break;
		}
		return o;
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		slideTo();
		return true;
	}

	/**
	 * Slides the slider to the specified pixel position.<br>
	 */
	public void slideTo()
	{
		int l = size().width() - SLIDER_WIDTH;
		int pos = MathHelper.clamp(mousePosition().x() - SLIDER_WIDTH / 2, 0, l);
		slideTo((float) pos / l);
	}

	/**
	 * Slides the slider to the specified offset between 0 and 1.<br>
	 * Sets the value relative to the offset.
	 *
	 * @param offset the offset
	 */
	public void slideTo(float offset)
	{
		if (!isEnabled())
			return;

		setValue(converter.convert(MathHelper.clamp(offset, 0, 1)));
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + text + " | " + text.position() + "@" + text.size() + TextFormatting.RESET + "] "
				+ super.getPropertyString();
	}
}
