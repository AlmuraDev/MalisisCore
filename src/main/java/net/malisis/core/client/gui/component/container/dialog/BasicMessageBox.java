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
package net.malisis.core.client.gui.component.container.dialog;

import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.util.FontColors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BasicMessageBox extends BasicForm {
  private static final int buttonPadding = 4;
  private static final int bottomPadding = 23;
  private final MessageBoxButtons messageBoxButtons;
  private final String message;
  @Nullable private final Consumer<MessageBoxResult> consumer;
  @Nullable private MessageBoxResult result;

  private BasicMessageBox(final MalisisGui gui, final String title, final String message, final MessageBoxButtons buttons,
    @Nullable final Consumer<MessageBoxResult> consumer) {
    super(gui, 300, 115, title);
    this.messageBoxButtons = buttons;
    this.message = message;
    this.consumer = consumer;
    this.construct(gui);
    this.setClosable(true);
  }

  public static void showDialog(@Nullable final GuiScreen gui, final String title, final String message, final MessageBoxButtons buttons) {
    showDialog(gui, title, message, buttons, null);
  }

  public static void showDialog(@Nullable final GuiScreen gui, final String title, final String message, final MessageBoxButtons buttons,
    @Nullable final Consumer<MessageBoxResult> consumer) {
    final MessageBoxDialogScreen screen = new MessageBoxDialogScreen(gui, title, message, buttons, consumer);
    screen.display();
  }

  private void construct(final MalisisGui gui) {
    setBottomPadding(bottomPadding);

    if (!this.message.isEmpty()) {
      final UILabel messageLabel = new UILabel(gui, this.message, true);
      messageLabel.setSize(BasicScreen.getPaddedWidth(this), BasicScreen.getPaddedHeight(this) - (buttonPadding * 2));
      messageLabel.setFontOptions(FontColors.WHITE_FO);

      final UISlimScrollbar scrollbar = new UISlimScrollbar(gui, messageLabel, UIScrollBar.Type.VERTICAL);
      scrollbar.setAutoHide(true);
      add(messageLabel.setAnchor(Anchor.CENTER | Anchor.MIDDLE));
    }

    final UIBackgroundContainer buttonContainer = new UIBackgroundContainer(gui);
    final List<UIButton> buttons = new ArrayList<>();
    switch (this.messageBoxButtons) {
      case OK:
        buttons.add(buildButton(gui, "button.ok"));
        break;
      case OK_CANCEL:
        buttons.add(buildButton(gui, "button.ok"));
        buttons.add(buildButton(gui, "button.cancel"));
        break;
      case YES_NO:
        buttons.add(buildButton(gui, "button.yes"));
        buttons.add(buildButton(gui, "button.no"));
        break;
      case YES_NO_CANCEL:
        buttons.add(buildButton(gui, "button.yes"));
        buttons.add(buildButton(gui, "button.no"));
        buttons.add(buildButton(gui, "button.cancel"));
        break;
      case CLOSE:
        buttons.add(buildButton(gui, "button.close"));
    }

    int width = 0;
    int x = 0;
    for (final UIButton button : buttons) {
      buttonContainer.add(button.setPosition(x, 0));
      x += button.getWidth() + buttonPadding;
      width += button.getWidth();
    }

    width += buttonPadding * (buttons.size() - 1);
    buttonContainer.setSize(width, UIConstants.Button.HEIGHT_TINY);
    buttonContainer.setPosition(0, bottomPadding + -buttonPadding, Anchor.BOTTOM | Anchor.RIGHT);
    buttonContainer.setBackgroundAlpha(0);
    add(buttonContainer);
  }

  @Subscribe
  public void onButtonClick(final UIButton.ClickEvent event) {
    switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
      case "button.cancel":
        this.result = MessageBoxResult.CANCEL;
        onClose();
        break;
      case "button.close":
        this.result = MessageBoxResult.CLOSE;
        onClose();
        break;
      case "button.no":
        this.result = MessageBoxResult.NO;
        onClose();
        break;
      case "button.ok":
        this.result = MessageBoxResult.OK;
        onClose();
        break;
      case "button.yes":
        this.result = MessageBoxResult.YES;
        onClose();
        break;
      case "button.form.close":
        switch (messageBoxButtons) {
          case OK:
            this.result = MessageBoxResult.OK;
            onClose();
            break;
          case OK_CANCEL:
            this.result = MessageBoxResult.CANCEL;
            onClose();
            break;
          case CLOSE:
            this.result = MessageBoxResult.CLOSE;
            onClose();
            break;
          case YES_NO:
            this.result = MessageBoxResult.NO;
            onClose();
            break;
          case YES_NO_CANCEL:
            this.result = MessageBoxResult.CANCEL;
            onClose();
            break;
        }
    }
  }

  @Override
  public void onClose() {
    if (this.consumer != null) {
      try {
        this.consumer.accept(this.result);
      } catch (final Exception e) {
        //ClientStaticAccess.logger.error("An exception occurred while executing the consumer!", e); // TODO: ?
      }
    }
    super.onClose();
  }

  private UIButton buildButton(final MalisisGui gui, final String name) {
    switch (name.toLowerCase()) {
      case "button.cancel":
        return new UIButtonBuilder(gui).text(I18n.format(I18n.format("gui.cancel")))
          .size(UIConstants.Button.WIDTH_TINY, UIConstants.Button.HEIGHT_TINY).listener(this).build("button.cancel");
      case "button.close":
        return new UIButtonBuilder(gui).text(I18n.format(I18n.format("almura.menu_button.close")))
          .size(UIConstants.Button.WIDTH_TINY, UIConstants.Button.HEIGHT_TINY).listener(this).build("button.close");
      case "button.no":
        return new UIButtonBuilder(gui).text(I18n.format(I18n.format("gui.no"))).size(UIConstants.Button.WIDTH_TINY, UIConstants.Button.HEIGHT_TINY)
          .listener(this).build("button.no");
      case "button.yes":
        return new UIButtonBuilder(gui).text(I18n.format(I18n.format("gui.yes"))).size(UIConstants.Button.WIDTH_TINY, UIConstants.Button.HEIGHT_TINY)
          .listener(this).build("button.yes");
      default:
        return new UIButtonBuilder(gui).text(I18n.format(I18n.format("almura.menu_button.ok")))
          .size(UIConstants.Button.WIDTH_TINY, UIConstants.Button.HEIGHT_TINY).listener(this).build("button.ok");
    }
  }

  public static class MessageBoxDialogScreen extends BasicScreen {

    @Nullable private final GuiScreen gui;
    private final String title;
    private final String message;
    private final MessageBoxButtons buttons;
    @Nullable private final Consumer<MessageBoxResult> consumer;

    public MessageBoxDialogScreen(@Nullable final GuiScreen gui, final String title, final String message, final MessageBoxButtons buttons,
      @Nullable final Consumer<MessageBoxResult> consumer) {
      super(gui, true);
      this.gui = gui;
      this.title = title;
      this.message = message;
      this.buttons = buttons;
      this.consumer = consumer;
    }

    @Override
    public void construct() {
      guiscreenBackground = false;
      // Disable escape key press
      registerKeyListener((keyChar, keyCode) -> keyCode == Keyboard.KEY_ESCAPE);
      addToScreen(
        new BasicMessageBox(this, title, message, buttons, consumer).setMovable(true).setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER).setZIndex(50)
          .setBackgroundAlpha(255));
    }

    @Nullable
    public GuiScreen getParent() {
      return this.gui;
    }
  }
}
