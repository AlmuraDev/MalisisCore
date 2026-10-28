/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie, Almura Development Studios
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

package net.malisis.core.client.gui.element.size;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.size.Size.ISize;

/**
 * A size the way 6.5.x components were sized: a positive width/height is fixed, zero or less is relative to the parent's padded
 * width/height (0 fills it, -10 leaves 10 pixels). Kept so code written for the 6.5.x GUI API keeps its layout on 6.6.
 */
public class RelativeSize implements ISize
{
	private final UIComponent owner;
	private final int width;
	private final int height;

	public RelativeSize(UIComponent owner, int width, int height)
	{
		this.owner = owner;
		this.width = width;
		this.height = height;
	}

	/** The width as given: positive is fixed, zero or less is relative to the parent. */
	public int rawWidth()
	{
		return width;
	}

	/** The height as given: positive is fixed, zero or less is relative to the parent. */
	public int rawHeight()
	{
		return height;
	}

	@Override
	public int width()
	{
		if (width > 0)
			return width;
		UIComponent parent = owner.getParent();
		return parent != null ? parent.size().width() - Padding.of(parent).horizontal() + width : 0;
	}

	@Override
	public int height()
	{
		if (height > 0)
			return height;
		UIComponent parent = owner.getParent();
		return parent != null ? parent.size().height() - Padding.of(parent).vertical() + height : 0;
	}

	@Override
	public String toString()
	{
		return width() + "x" + height() + " (" + width + "x" + height + ")";
	}
}
