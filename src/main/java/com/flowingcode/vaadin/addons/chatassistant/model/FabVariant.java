/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.addons.chatassistant.model;

import com.vaadin.flow.component.button.ButtonVariant;

/**
 * Theme variants supported by the chat assistant floating action button (FAB).
 *
 * <p>Variants fall into two groups: the <em>size</em> variants {@link #SMALL} and {@link #LARGE}
 * resize the FAB to a predefined diameter and are mutually exclusive (only one is active at a time),
 * while the <em>color</em> variants are applied to the FAB and accumulate. All variants work under
 * both the Lumo and Aura themes, except {@link #LUMO_CONTRAST}, which is only effective under Lumo.
 *
 * @since 5.1.0
 */
public enum FabVariant {

  /** Renders the FAB at the small predefined diameter. */
  SMALL(ButtonVariant.LUMO_SMALL, true, null),
  /** Renders the FAB at the large predefined diameter. */
  LARGE(ButtonVariant.LUMO_LARGE, true, null),
  /** Applies the primary color to the FAB. */
  PRIMARY(ButtonVariant.LUMO_PRIMARY, false, null),
  /** Applies the success (green) color to the FAB. */
  SUCCESS(ButtonVariant.LUMO_SUCCESS, false, "aura-accent-green"),
  /** Applies the error (red) color to the FAB. */
  ERROR(ButtonVariant.LUMO_ERROR, false, "aura-accent-red"),
  /** Applies the contrast color to the FAB. Effective only under the Lumo theme. */
  LUMO_CONTRAST(ButtonVariant.LUMO_CONTRAST, false, null);

  private final ButtonVariant buttonVariant;
  private final boolean sizeVariant;
  private final String auraClass;

  FabVariant(ButtonVariant buttonVariant, boolean sizeVariant, String auraClass) {
    this.buttonVariant = buttonVariant;
    this.sizeVariant = sizeVariant;
    this.auraClass = auraClass;
  }

  /**
   * Returns whether this is a size variant ({@link #SMALL} or {@link #LARGE}), which resizes the FAB.
   *
   * @return {@code true} if this is a size variant
   */
  public boolean isSizeVariant() {
    return sizeVariant;
  }

  /**
   * Returns the Vaadin button variant that carries this variant's color.
   *
   * @return the underlying {@link ButtonVariant}
   */
  public ButtonVariant getButtonVariant() {
    return buttonVariant;
  }

  /**
   * Returns the Aura accent CSS class for this variant, or {@code null} if it renders from its Lumo
   * token alone. Aura styles its accent colors via a CSS class rather than the theme attribute.
   *
   * @return the Aura accent class, or {@code null}
   */
  public String getAuraClass() {
    return auraClass;
  }
}
