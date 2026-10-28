/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.content.IContentHolder;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.element.size.RelativeSize;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.element.size.Sizes;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.client.gui.text.GuiText;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;

/**
 * UIButton
 *
 * @author Ordinastie, PaleoCrafter
 */
public class UIButton extends UIComponent implements IContentHolder
{
	/** The {@link FontOptions} of the text content, when not hovered or disabled. */
	protected FontOptions regularFontOptions = FontOptions.builder().color(0xFFFFFF).shadow().build();
	/** The {@link FontOptions} of the text content while hovered. */
	protected FontOptions hoveredFontOptions = FontOptions.builder().color(0xFFFFA0).shadow().build();
	/** The {@link FontOptions} of the text content while disabled. */
	protected FontOptions disabledFontOptions = FontOptions.builder().color(0xCCCCCC).shadow().build();
	/** The {@link FontOptions} used for the {@link UIButton} text content, picking one of the above. */
	protected FontOptions fontOptions = buildFontOptions();

	protected IPosition offsetPosition = Position.of(() -> isPressed() ? 1 : 0, () -> isPressed() ? 1 : 0);
	protected IPosition contentPosition = null;

	/** Content used for this {@link UIButton}. */
	protected IContent content;
	/** Whether this {@link UIButton} is currently being pressed. */
	protected boolean isPressed = false;

	/** Action to execute when the button is clicked. */
	protected Runnable action;

	/**
	 * Instantiates a new {@link UIButton}.
	 */
	public UIButton()
	{
		setAutoSize();
		setBackground(GuiShape.builder(this).border(5).icon((Supplier<GuiIcon>) () -> {
			if (isDisabled())
				return GuiIcon.BUTTON_DISABLED;
			if (isHovered())
				return isPressed() ? GuiIcon.BUTTON_HOVER_PRESSED : GuiIcon.BUTTON_HOVER;
			return GuiIcon.BUTTON;
		}).build());
		setForeground(this::content);
	}

	/**
	 * Instantiates a new {@link UIButton} with specified label.
	 *
	 * @param text the text
	 */
	public UIButton(String text)
	{
		this();
		setText(text);
	}

	/**
	 * Instantiates a new {@link UIButton} with specified content.
	 *
	 * @param content the content
	 */
	public UIButton(UIComponent content)
	{
		this();
		setContent(content);
	}

	//#region Getters/Setters
	/**
	 * Sets the content for this {@link UIButton}.
	 *
	 * @param content the content
	 */
	public void setContent(IContent content)
	{
		this.content = content;
		content.setParent(this);
		content.setPosition(Position.middleCenter(content).plus(offsetPosition));
	}

	public void setText(String text)
	{
		GuiText gt = GuiText.of(text, fontOptions);
		setContent(gt);
	}

	/**
	 * Gets the {@link UIComponent} used as content for this {@link UIButton}.
	 *
	 * @return the content component
	 */
	@Override
	public IContent content()
	{
		return content;
	}

	/**
	 * Checks if this {@link UIButton} is currently being pressed.
	 *
	 * @return true, if is pressed
	 */
	public boolean isPressed()
	{
		return isPressed;
	}

	/**
	 * Sets whether the size of this {@link UIButton} should be calculated automatically.
	 *
	 * @return the UI button
	 */
	public UIButton setAutoSize()
	{
		setSize(Size.sizeOfContent(this, 6, 6));
		return this;
	}

	/**
	 * Sets the size the 6.5.x way: the button still grows to fit its content, so the size is a minimum.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends UIComponent> T setSize(int width, int height)
	{
		IntSupplier contentWidth = Sizes.widthOfContent(this, 6);
		IntSupplier contentHeight = Sizes.heightOfContent(this, 6);
		setSize(new RelativeSize(this, width, height) {
			@Override
			public int width()
			{
				return Math.max(super.width(), contentWidth.getAsInt());
			}

			@Override
			public int height()
			{
				return Math.max(super.height(), contentHeight.getAsInt());
			}
		});
		return (T) this;
	}

	public UIButton onClick(Runnable action)
	{
		this.action = action;
		return this;
	}

	public FontOptions defaultFontOptions()
	{
		return fontOptions;
	}

	/**
	 * Sets the {@link FontOptions} of the text, when not hovered or disabled.
	 *
	 * @param fontOptions the font options
	 */
	public void setFontOptions(FontOptions fontOptions)
	{
		this.regularFontOptions = checkNotNull(fontOptions);
		updateFontOptions();
	}

	/**
	 * Sets the {@link FontOptions} of the text while this {@link UIButton} is hovered.
	 *
	 * @param fontOptions the font options
	 */
	public void setHoveredFontOptions(FontOptions fontOptions)
	{
		this.hoveredFontOptions = checkNotNull(fontOptions);
		updateFontOptions();
	}

	/**
	 * Sets the {@link FontOptions} of the text while this {@link UIButton} is disabled.
	 *
	 * @param fontOptions the font options
	 */
	public void setDisabledFontOptions(FontOptions fontOptions)
	{
		this.disabledFontOptions = checkNotNull(fontOptions);
		updateFontOptions();
	}

	public FontOptions getFontOptions()
	{
		return regularFontOptions;
	}

	public FontOptions getHoveredFontOptions()
	{
		return hoveredFontOptions;
	}

	private FontOptions buildFontOptions()
	{
		//disabled first: a disabled button can still be hovered
		return regularFontOptions	.toBuilder()
									.when(this::isDisabled)
									.from(disabledFontOptions)
									.when(this::isHovered)
									.from(hoveredFontOptions)
									.build();
	}

	private void updateFontOptions()
	{
		fontOptions = buildFontOptions();
		if (content instanceof GuiText)
			((GuiText) content).setFontOptions(fontOptions);
	}

	//#end Getters/Setters
	protected void executeAction()
	{
		MalisisGui.playSound(SoundEvents.UI_BUTTON_CLICK);
		if (action != null)
			action.run();
	}

	@Override
	public boolean onClick()
	{
		executeAction();
		fireEvent(new ClickEvent(this, MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y()));
		return true;
	}

	@Override
	public boolean onButtonPress(MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = true;
		return super.onButtonPress(button);
	}

	@Override
	public boolean onButtonRelease(MouseButton button)
	{
		if (button == MouseButton.LEFT)
			isPressed = false;
		return super.onButtonRelease(button);
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)
		{
			executeAction();
			return true;
		}

		return false;
	}

	@Override
	public String getPropertyString()
	{
		return "[" + TextFormatting.GREEN + content + TextFormatting.RESET + "] " + super.getPropertyString();
	}

	/**
	 * Event fired when a {@link UIButton} is clicked, after its action ran.
	 */
	public static class ClickEvent extends ComponentEvent<UIButton>
	{
		/** Position of the mouse when clicked . */
		private int x, y;

		/**
		 * Instantiates a new {@link ClickEvent}.
		 *
		 * @param component the component
		 * @param x the x coordinate of the mouse
		 * @param y the y coordinate of the mouse
		 */
		public ClickEvent(UIButton component, int x, int y)
		{
			super(component);
			this.x = x;
			this.y = y;
		}

		/**
		 * Gets the x coordinate of the mouse.
		 *
		 * @return the x
		 */
		public int getX()
		{
			return x;
		}

		/**
		 * Gets the y coordinate of the mouse.
		 *
		 * @return the y
		 */
		public int getY()
		{
			return y;
		}
	}
}
