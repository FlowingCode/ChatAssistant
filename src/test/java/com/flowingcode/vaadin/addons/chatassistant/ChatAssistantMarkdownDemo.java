/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2024 Flowing Code
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
import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.Timer;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("Markdown Demo")
@SuppressWarnings("serial")
@Route(value = "chat-assistant/markdown-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantMarkdownDemo extends VerticalLayout {
  
  public ChatAssistantMarkdownDemo() {
    ChatAssistant chatAssistant = new ChatAssistant(true);
    TextArea message = new TextArea();
    message.setLabel("Enter a message from the assistant (try using Markdown)");
    message.setSizeFull();
    message.addKeyPressListener(ev->{
      if (Strings.isNullOrEmpty(chatAssistant.getWhoIsTyping())) {
        chatAssistant.setWhoIsTyping("Assistant is generating an answer ...");
      }
    });
    message.addBlurListener(ev->chatAssistant.clearWhoIsTyping());

    Button chat = new Button("Chat");
    chat.addClickListener(ev -> {
      Message m = Message.builder().content(message.getValue()).messageTime(LocalDateTime.now())
          .name("Assistant").avatar("chatbot.png").build();

      chatAssistant.sendMessage(m);
      message.clear();
    });
    chatAssistant.sendMessage(Message.builder().content("**Hello, I am here to assist you**")
        .messageTime(LocalDateTime.now())
        .name("Assistant").avatar("chatbot.png").build());

    add(message, chat, chatAssistant);
  }
}
