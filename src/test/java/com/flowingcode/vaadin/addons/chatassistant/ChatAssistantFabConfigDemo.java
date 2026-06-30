/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2025 Flowing Code
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

import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("FAB Configuration Demo")
@Route(value = "chat-assistant/fab-config-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantFabConfigDemo extends VerticalLayout {

  public ChatAssistantFabConfigDemo() {
    SvgIcon icon = new SvgIcon("chatbot.svg");

    // Build the assistant with a custom FAB icon via the builder.
    ChatAssistant<Message> chatAssistant = ChatAssistant.<Message>builder().fabIcon(icon).build();
    chatAssistant.setWindowWidth("400px");
    chatAssistant.setWindowHeight("400px");

    // Size variants resize the FAB (and its icon): SMALL (50px) and LARGE (72px); removing the active
    // one restores the default (60px). Only one size is active at a time.
    Button small = new Button("Small",
        ev -> chatAssistant.addFabThemeVariants(ButtonVariant.LUMO_SMALL)
    );
    Button large = new Button("Large",
        ev -> chatAssistant.addFabThemeVariants(ButtonVariant.LUMO_LARGE)
    );
    Button defaultSize = new Button("Default size", ev -> chatAssistant.removeFabThemeVariants(ButtonVariant.LUMO_SMALL,
        ButtonVariant.LUMO_LARGE)
    );

    // Color variants are applied to the underlying button; the icon follows the button color.
    Button success = new Button("Success",
        ev -> chatAssistant.addFabThemeVariants(ButtonVariant.LUMO_SUCCESS)
    );
    Button error = new Button("Error",
        ev -> chatAssistant.addFabThemeVariants(ButtonVariant.LUMO_ERROR)
    );
    Button contrast = new Button("Contrast",
        ev -> chatAssistant.addFabThemeVariants(ButtonVariant.LUMO_CONTRAST)
    );
    Button clearColors = new Button("Clear colors", ev -> chatAssistant.removeFabThemeVariants(
        ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_CONTRAST)
    );

    // Lock down or free up the FAB and the window, and toggle the resize direction hints. Each toggle
    // shows a notification reporting the resulting state.
    Button movable = new Button("Toggle movable", ev -> {
      chatAssistant.setFabMovable(!chatAssistant.isFabMovable());
      Notification.show("FAB movable: " + chatAssistant.isFabMovable());
    });
    Button resizable = new Button("Toggle resizable", ev -> {
      chatAssistant.setWindowResizable(!chatAssistant.isWindowResizable());
      Notification.show("Window resizable: " + chatAssistant.isWindowResizable());
    });
    Button indicators = new Button("Toggle resize hints", ev -> {
      chatAssistant.setResizeIndicatorsVisible(!chatAssistant.isResizeIndicatorsVisible());
      Notification.show("Resize hints visible: " + chatAssistant.isResizeIndicatorsVisible());
    });

    // Seed the conversation and open the window so the styling and resize hints are visible right away.
    chatAssistant.sendMessage(
        Message.builder()
            .content("Use the buttons to restyle the FAB.")
            .messageTime(LocalDateTime.now())
            .name("Assistant")
            .avatar("chatbot.png")
            .build()
    );
    chatAssistant.setOpened(true);

    add(section("Size variants", small, large, defaultSize),
        section("Color variants", success, error, contrast, clearColors),
        section("Behavior", movable, resizable, indicators),
        chatAssistant);
  }

  /** A titled group of buttons whose row wraps on narrow screens. */
  private static VerticalLayout section(String title, Button... buttons) {
      Span heading = new Span(title);

      HorizontalLayout row = new HorizontalLayout(buttons);
      row.getStyle().set("flex-wrap", "wrap");

      VerticalLayout group = new VerticalLayout(heading, row);
      group.setPadding(false);
      group.setSpacing(false);
      return group;
  }
}
