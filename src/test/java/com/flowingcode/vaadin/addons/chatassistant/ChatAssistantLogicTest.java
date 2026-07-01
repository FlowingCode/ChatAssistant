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
package com.flowingcode.vaadin.addons.chatassistant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.flowingcode.vaadin.addons.chatassistant.model.FabVariant;
import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.vaadin.flow.component.button.ButtonVariant;
import org.junit.Test;

/**
 * Unit tests for the pure-Java logic in {@link ChatAssistant} that does not require an attached UI:
 * FAB margin parsing, unread-message clamping, screen-size listener argument validation, and the
 * {@link FabVariant} mapping. These are exercised on an unattached component (no UI/DOM), mirroring
 * the lightweight style of {@code SerializationTest}.
 */
public class ChatAssistantLogicTest {

  private ChatAssistant<Message> newChatAssistant() {
    return new ChatAssistant<>();
  }

  // parseFabMargin -----------------------------------------------------------

  @Test
  public void parseFabMargin_null_returnsDefault() {
    assertEquals(ChatAssistant.DEFAULT_FAB_MARGIN, newChatAssistant().parseFabMargin(null));
  }

  @Test
  public void parseFabMargin_plainNumber_isParsed() {
    assertEquals(30, newChatAssistant().parseFabMargin("30"));
  }

  @Test
  public void parseFabMargin_withPxSuffixAndWhitespace_isParsed() {
    assertEquals(42, newChatAssistant().parseFabMargin("  42px "));
  }

  @Test
  public void parseFabMargin_invalid_returnsDefault() {
    assertEquals(
        ChatAssistant.DEFAULT_FAB_MARGIN, newChatAssistant().parseFabMargin("not-a-number"));
  }

  @Test
  public void parseFabMargin_negative_returnsDefault() {
    assertEquals(ChatAssistant.DEFAULT_FAB_MARGIN, newChatAssistant().parseFabMargin("-5"));
  }

  // setUnreadMessages clamping ----------------------------------------------

  @Test
  public void setUnreadMessages_negative_clampsToZero() {
    ChatAssistant<Message> ca = newChatAssistant();
    ca.setUnreadMessages(-5);
    assertEquals(0, ca.getUnreadMessages());
  }

  @Test
  public void setUnreadMessages_aboveMax_clampsTo99() {
    ChatAssistant<Message> ca = newChatAssistant();
    ca.setUnreadMessages(150);
    assertEquals(99, ca.getUnreadMessages());
  }

  @Test
  public void setUnreadMessages_inRange_isUnchanged() {
    ChatAssistant<Message> ca = newChatAssistant();
    ca.setUnreadMessages(7);
    assertEquals(7, ca.getUnreadMessages());
  }

  @Test
  public void setUnreadMessages_boundaries_areKept() {
    ChatAssistant<Message> ca = newChatAssistant();
    ca.setUnreadMessages(0);
    assertEquals(0, ca.getUnreadMessages());
    ca.setUnreadMessages(99);
    assertEquals(99, ca.getUnreadMessages());
  }

  // addScreenSizeListener validation ----------------------------------------

  @Test
  public void addScreenSizeListener_nullListener_throws() {
    ChatAssistant<Message> ca = newChatAssistant();
    assertThrows(NullPointerException.class, () -> ca.addScreenSizeListener(100, 100, null));
  }

  @Test
  public void addScreenSizeListener_bothThresholdsNull_throws() {
    ChatAssistant<Message> ca = newChatAssistant();
    assertThrows(
        IllegalArgumentException.class, () -> ca.addScreenSizeListener(null, null, ev -> {}));
  }

  @Test
  public void addScreenSizeListener_nonPositiveThreshold_throws() {
    ChatAssistant<Message> ca = newChatAssistant();
    assertThrows(IllegalArgumentException.class, () -> ca.addScreenSizeListener(0, null, ev -> {}));
  }

  @Test
  public void addScreenSizeListener_validThreshold_returnsRegistration() {
    ChatAssistant<Message> ca = newChatAssistant();
    assertNotNull(ca.addScreenSizeListener(200, null, ev -> {}));
  }

  // FabVariant -------------------------------------------------------------

  @Test
  public void fabVariant_sizeClassification_isCorrect() {
    assertTrue(FabVariant.SMALL.isSizeVariant());
    assertTrue(FabVariant.LARGE.isSizeVariant());
    assertFalse(FabVariant.SUCCESS.isSizeVariant());
    assertFalse(FabVariant.ERROR.isSizeVariant());
    assertFalse(FabVariant.LUMO_CONTRAST.isSizeVariant());
    assertFalse(FabVariant.PRIMARY.isSizeVariant());
  }

  @Test
  public void fabVariant_colorMapping_hasThemeVariantAndAuraClass() {
    // SUCCESS maps to a Lumo variant plus an Aura accent class (Aura styles accents via CSS class,
    // not the theme attribute), so the color renders under both themes.
    assertEquals(ButtonVariant.LUMO_SUCCESS, FabVariant.SUCCESS.getButtonVariant());
    assertEquals("aura-accent-green", FabVariant.SUCCESS.getAuraClass());
  }

  @Test
  public void fabVariant_colorMapping_withoutAuraClass() {
    // PRIMARY is cross-theme via its token, so it needs no extra Aura class.
    assertEquals(ButtonVariant.LUMO_PRIMARY, FabVariant.PRIMARY.getButtonVariant());
    assertNull(FabVariant.PRIMARY.getAuraClass());
  }

  // isFabMovable effective state --------------------------------------------

  @Test
  public void isFabMovable_reflectsEffectiveDragState() {
    ChatAssistant<Message> ca = newChatAssistant();
    // Defaults: movable and anchored to the viewport, so the FAB is draggable.
    assertTrue(ca.isFabMovable());
    // A container-anchored FAB cannot be dragged, even though the movable preference is unchanged.
    ca.setFabAnchoredToViewport(false);
    assertFalse(ca.isFabMovable());
    // Re-anchoring to the viewport restores draggability.
    ca.setFabAnchoredToViewport(true);
    assertTrue(ca.isFabMovable());
    // Turning off movable makes it non-draggable regardless of anchoring.
    ca.setFabMovable(false);
    assertFalse(ca.isFabMovable());
  }
}
