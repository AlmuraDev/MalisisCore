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

package net.malisis.core.client.gui.element.position;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.position.Position.IPosition;

/**
 * A position given as an offset from an {@link Anchor} of the parent, the way 6.5.x components were positioned:
 * <ul>
 * <li>LEFT (or none) and TOP offset from the parent's padded left/top edge,</li>
 * <li>RIGHT and BOTTOM align the component to the parent's padded right/bottom edge, then add the offset,</li>
 * <li>CENTER and MIDDLE center the component in the parent's full width/height (padding ignored), then add the offset.</li>
 * </ul>
 * Kept so code written for the 6.5.x GUI API keeps its layout on 6.6.
 */
public class AnchoredPosition implements IPosition
{
	private final UIComponent owner;
	private final int x;
	private final int y;
	private final int anchor;

	public AnchoredPosition(UIComponent owner, int x, int y, int anchor)
	{
		this.owner = owner;
		this.x = x;
		this.y = y;
		this.anchor = anchor;
	}

	/** The x offset, before the anchor is applied. */
	public int rawX()
	{
		return x;
	}

	/** The y offset, before the anchor is applied. */
	public int rawY()
	{
		return y;
	}

	public int anchor()
	{
		return anchor;
	}

	@Override
	public int x()
	{
		UIComponent parent = owner.getParent();
		if (parent == null)
			return x;

		int a = Anchor.horizontal(anchor);
		int space = parent.size().width() - owner.size().width();
		if (a == Anchor.CENTER)
			return x + space / 2;
		if (a == Anchor.RIGHT)
			return x + space - Padding.of(parent).right();
		return x + Padding.of(parent).left();
	}

	@Override
	public int y()
	{
		UIComponent parent = owner.getParent();
		if (parent == null)
			return y;

		int a = Anchor.vertical(anchor);
		int space = parent.size().height() - owner.size().height();
		if (a == Anchor.MIDDLE)
			return y + space / 2;
		if (a == Anchor.BOTTOM)
			return y + space - Padding.of(parent).bottom();
		return y + Padding.of(parent).top();
	}

	@Override
	public String toString()
	{
		return x() + "," + y() + " (" + x + "," + y + " anchor " + anchor + ")";
	}
}
