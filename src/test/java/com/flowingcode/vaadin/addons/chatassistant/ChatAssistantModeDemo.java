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

import com.flowingcode.vaadin.addons.chatassistant.model.ChatAssistantMode;
import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("Modes & Responsive Demo")
@Route(value = "chat-assistant/mode-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantModeDemo extends VerticalLayout {

  public ChatAssistantModeDemo() {
    SvgIcon icon = new SvgIcon("chatbot.svg");
    icon.setColor("var(--lumo-primary-contrast-color)");

    // Build the assistant with auto-switching enabled: setting a breakpoint makes it switch to
    // mobile (full-screen dialog) below 768px and back to desktop (anchored popover) above it.
    ChatAssistant<Message> chatAssistant = ChatAssistant.<Message>builder()
        .fabIcon(icon)
        .mobileBreakpoint(768)
        .build();
    chatAssistant.setWindowWidth("400px");
    chatAssistant.setWindowHeight("400px");

    // React to every mode change, whether automatic or manual.
    chatAssistant.addModeChangedListener(
        ev -> Notification.show("Switched to " + ev.getMode() + " mode")
      );

    // React to the chat window's own size crossing a 500px width threshold (independent of the
    // viewport breakpoint above). Fires once on registration and then on every crossing.
    chatAssistant.addScreenSizeListener(500, null,
        ev -> Notification.show("Chat window is now " + ev.getDirection() + " 500px wide")
      );

    // Switch the mode manually (only sticks while auto-switching is disabled).
    Button mobile = new Button("Set mobile", ev -> chatAssistant.setMode(ChatAssistantMode.MOBILE));
    Button desktop = new Button("Set desktop", ev -> chatAssistant.setMode(ChatAssistantMode.DESKTOP));

    // Freeze/resume automatic switching on the configured breakpoint.
    Button toggleSwitching = new Button("Toggle auto switching", ev -> {
      chatAssistant.setMobileModeSwitchingEnabled(!chatAssistant.isMobileModeSwitchingEnabled());
      Notification.show("Auto switching: " + chatAssistant.isMobileModeSwitchingEnabled());
    });

    // Move the FAB back to its configured corner after it has been dragged.
    Button reset = new Button("Reset FAB position", ev -> chatAssistant.resetFabPosition());

    HorizontalLayout controls = new HorizontalLayout(mobile, desktop, toggleSwitching, reset);
    controls.getStyle().set("flex-wrap", "wrap");

    // Seed the conversation with a greeting and open the window.
    chatAssistant.sendMessage(
      Message.builder()
        .content("Resize the window to switch modes.")
        .messageTime(LocalDateTime.now())
        .name("Assistant")
        .avatar("chatbot.png")
        .build()
    );
    chatAssistant.setOpened(true);

    add(controls, chatAssistant);
  }
}
