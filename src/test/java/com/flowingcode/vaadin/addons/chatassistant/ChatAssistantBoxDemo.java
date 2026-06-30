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

import com.flowingcode.vaadin.addons.chatassistant.model.FabPosition;
import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("FAB In A Box Demo")
@Route(value = "chat-assistant/box-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantBoxDemo extends VerticalLayout {

  public ChatAssistantBoxDemo() {
    SvgIcon icon = new SvgIcon("chatbot.svg");
    icon.setColor("var(--lumo-primary-contrast-color)");

    // With fabAnchoredToViewport(false) the FAB is positioned relative to its container instead of
    // the viewport, so it lives inside the box below rather than floating over the whole screen.
    ChatAssistant<Message> chatAssistant = ChatAssistant.<Message>builder()
        .fabIcon(icon)
        .fabAnchoredToViewport(false)
        .build();
    chatAssistant.setWindowWidth("400px");
    chatAssistant.setWindowHeight("400px");

    // Seed the conversation with a greeting.
    chatAssistant.sendMessage(
        Message.builder()
            .content("Use the buttons to move me around the box.")
            .messageTime(LocalDateTime.now())
            .name("Assistant")
            .avatar("chatbot.png")
            .build()
    );

    // Move the FAB to each corner of the box.
    HorizontalLayout controls = new HorizontalLayout(
        new Button("Top left", ev -> chatAssistant.setFabPosition(FabPosition.TOP_LEFT)),
        new Button("Top right", ev -> chatAssistant.setFabPosition(FabPosition.TOP_RIGHT)),
        new Button("Bottom left", ev -> chatAssistant.setFabPosition(FabPosition.BOTTOM_LEFT)),
        new Button("Bottom right", ev -> chatAssistant.setFabPosition(FabPosition.BOTTOM_RIGHT))
    );
    controls.getStyle().set("flex-wrap", "wrap");

    // A visible, relatively-positioned box that hosts the non-fixed FAB.
    Span description = new Span("The FAB is not anchored to the viewport but positioned relative to this box. This behaviour disables dragging.");
    Div box = new Div(chatAssistant);
    box.getStyle()
        .set("position", "relative")
        .set("border", "2px dashed var(--lumo-contrast-30pct)")
        .set("border-radius", "var(--lumo-border-radius-l)")
        .setWidth("600px")
        .setHeight("400px");

    add(controls, description, box);
  }
}
