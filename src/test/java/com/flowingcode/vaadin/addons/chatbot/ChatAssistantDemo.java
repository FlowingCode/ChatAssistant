/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 Flowing Code
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
package com.flowingcode.vaadin.addons.chatbot;

import com.flowingcode.vaadin.addons.chatassistant.ChatAssistant;
import com.flowingcode.vaadin.addons.chatassistant.Message;
import com.flowingcode.vaadin.addons.chatassistant.Sender;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Chat Assistant Add-on Demo")
@SuppressWarnings("serial")
@Route(value = "chat-assistant/basic-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantDemo extends VerticalLayout {

  public ChatAssistantDemo() {
    ChatAssistant chatAssistant = new ChatAssistant();
    TextArea message = new TextArea();
    message.setLabel("Enter a message from the assistant");
    message.setSizeFull();

    Button chat = new Button("Chat");
    chat.addClickListener(
        ev -> {
          Message m =
              new Message(
                  message.getValue(),
                  false,
                  false,
                  0,
                  false,
                  new Sender("Assistant", "1", "chatbot.png"));
          chatAssistant.sendMessage(m);
          message.clear();
        });
    chatAssistant.sendMessage(
        new Message(
            "Hello, I am here to assist you",
            false,
            false,
            0,
            false,
            new Sender("Assistant", "1", "chatbot.png")));
    chatAssistant.toggle();
    chatAssistant.addChatSentListener(
        ev -> {
          if (ev.isRight()) {
            Notification.show(ev.getMessage());
          }
        });

    add(message, chat, chatAssistant);
  }
}
