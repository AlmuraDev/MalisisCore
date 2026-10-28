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
package net.malisis.core.client.gui;

import net.malisis.core.MalisisCore;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public abstract class BasicScreen extends MalisisGui {

  @Nullable protected final GuiScreen parent;
  private final boolean drawParentInBackground;

  /**
   * Creates a gui with an absent parent
   */
  public BasicScreen() {
    this(null);
  }

  /**
   * Creates a gui with a parent
   *
   * @param parent the {@link BasicScreen} that we came from
   */
  public BasicScreen(@Nullable final GuiScreen parent) {
    this(parent, false);
  }

  public BasicScreen(@Nullable final GuiScreen parent, final boolean drawParentInBackground) {
    this.parent = parent;
    this.drawParentInBackground = drawParentInBackground;
  }

  /**
   * Gets the X position after applying padding against another component, uses {@link Anchor#LEFT} by default
   *
   * @param component The component to apply padding against
   * @param padding The padding to use
   * @return The padded X position
   */
  public static int getPaddedX(final UIComponent component, final int padding) {
    return getPaddedX(component, padding, Anchor.LEFT);
  }

  /**
   * Gets the X position after applying padding against another component
   *
   * @param component The component to apply padding against
   * @param padding The padding to use
   * @param anchor The direction we're going
   * @return The padded X position
   */
  public static int getPaddedX(final UIComponent component, final int padding, final int anchor) {
    if (anchor == Anchor.LEFT) {
      return component.getX() + component.getWidth() + padding;
    } else if (anchor == Anchor.RIGHT) {
      return component.getX() - component.getWidth() - padding;
    } else {
      throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be LEFT or RIGHT.");
    }
  }

  /**
   * Gets the Y position after applying padding against another component, uses {@link Anchor#TOP} by default
   *
   * @param component The component to apply padding against
   * @param padding The padding to use
   * @return The padded Y position
   */
  public static int getPaddedY(final UIComponent component, final int padding) {
    return getPaddedY(component, padding, Anchor.TOP);
  }

  /**
   * Gets the Y position after applying padding against another component
   *
   * @param component The component to apply padding against
   * @param padding The padding to use
   * @param anchor The direction we're going
   * @return The padded Y position
   */
  public static int getPaddedY(final UIComponent component, final int padding, final int anchor) {
    if (anchor == Anchor.BOTTOM) {
      return component.getY() - component.getHeight() - padding;
    } else if (anchor == Anchor.TOP) {
      return component.getY() + component.getHeight() + padding;
    } else {
      throw new IllegalArgumentException("Invalid anchor used [" + anchor + "], anchor must be BOTTOM or TOP.");
    }
  }

  /**
   * Gets the width of a component with padding removed
   *
   * @param component The component to get width from
   * @return The width of a component with padding removed
   */
  public static int getPaddedWidth(final UIComponent component) {
    return component.getWidth() - Padding.of(component).horizontal();
  }

  /**
   * Gets the height of a component with padding removed
   *
   * @param component The component to get height from
   * @return The height of a component with padding removed
   */
  public static int getPaddedHeight(final UIComponent component) {
    return component.getHeight() - Padding.of(component).vertical();
  }

  @Override
  public void drawScreen(final int mouseX, final int mouseY, final float partialTick) {
    if (this.drawParentInBackground) {
      Optional.ofNullable(this.parent).ifPresent(screen -> screen.drawScreen(mouseX, mouseY, partialTick));
    }
    super.drawScreen(mouseX, mouseY, partialTick);
  }

  @Override
  public void updateScreen() {
    if (this.drawParentInBackground) {
      Optional.ofNullable(this.parent).ifPresent(GuiScreen::updateScreen);
    }
    super.updateScreen();
  }

  @Override
  public void onResize(final Minecraft minecraft, final int width, final int height) {
    if (this.drawParentInBackground) {
      Optional.ofNullable(this.parent).ifPresent(screen -> screen.onResize(minecraft, width, height));
    }
    super.onResize(minecraft, width, height);
  }

  @Override
  public void update() {
    if (this.drawParentInBackground) {
      Optional.ofNullable(this.parent).filter(screen -> screen instanceof MalisisGui).ifPresent(screen -> ((MalisisGui) screen).update());
    }
    super.update();
  }

  @Override
  public void updateGui() {
    if (this.drawParentInBackground) {
      Optional.ofNullable(this.parent).filter(screen -> screen instanceof MalisisGui).ifPresent(screen -> ((MalisisGui) screen).updateGui());
    }
    super.updateGui();
  }

  /**
   * Closes this {@link BasicScreen} and displays the parent, if present.
   */
  @Override
  public void close() {
    setFocusedComponent(null, true);
    setHoveredComponent(null, true);
    Keyboard.enableRepeatEvents(false);

    if (this.mc.player != null && this.mc.player.openContainer != this.mc.player.inventoryContainer) {
      this.mc.player.closeScreen();
    }

    this.onClose();

    this.mc.displayGuiScreen(Optional.ofNullable(this.parent).orElse(null));
  }

  /**
   * Same as {@link MalisisGui#mousePressed(UIComponent, int)}, except the component is focused before the button press is sent to it.
   */
  @Override
  protected void mousePressed(final UIComponent component, final int button) {
    try {
      final long time = System.currentTimeMillis();

      if (component != null && component.isEnabled()) {
        component.setFocused(true);

        boolean regularClick = true;
        final boolean doubleClick = button == this.lastClickButton && time - this.lastClickTime < 250 && component == this.focusedComponent;

        if (doubleClick) {
          regularClick = !component.onDoubleClick(MouseButton.getButton(button));
          this.lastClickTime = 0;
        }

        if (regularClick) {
          component.onButtonPress(MouseButton.getButton(button));
          if (this.draggedComponent == null) {
            this.draggedComponent = component;
          }
        }
      } else {
        setFocusedComponent(null, true);
        if (this.inventoryContainer != null && !this.inventoryContainer.getPickedItemStack().isEmpty()) {
          final MalisisInventoryContainer.ActionType action =
            button == 1 ? MalisisInventoryContainer.ActionType.DROP_ONE : MalisisInventoryContainer.ActionType.DROP_STACK;
          MalisisGui.sendAction(action, null, button);
        }
      }

      this.lastClickTime = time;
      this.lastClickButton = button;
    } catch (final Exception e) {
      MalisisCore.message("A problem occurred : " + e.getClass().getSimpleName() + ": " + e.getMessage());
      e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }
  }

  /**
   * Keys only go to the focused component: the hovered one would steal keys meant for the tab order of text boxes.
   */
  @Override
  protected boolean sendKeysToHoveredComponent() {
    return false;
  }

  public void setFocusedComponent(final UIComponent component) {
    this.focusedComponent = component;
  }

  public void addToScreen(final UIComponent... components) {
    Arrays.stream(components).forEach(this::addToScreen);
  }

  protected void onClose() {}
}
