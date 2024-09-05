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
package com.flowingcode.vaadin.addons.chatassistant;

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("Customized Assistant Demo")
@SuppressWarnings("serial")
@Route(value = "chat-assistant/customized-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class CustomizedChatAssistantDemo extends VerticalLayout {
  
  private Message delayedMessage;
  private ChatAssistant chatAssistant;

  public CustomizedChatAssistantDemo() {
    chatAssistant = new ChatAssistant();
    TextArea message = new TextArea();
    message.setLabel("Enter a message from the assistant");
    message.setSizeFull();

    Button chat = new Button("Chat");
    chat.addClickListener(ev -> {
      Message m = Message.builder().content(message.getValue())
          .sender(Sender.builder().name("Assistant").id("1").avatar("chatbot.png").build()).build();

      chatAssistant.sendMessage(m);
      message.clear();
    });
    Button chatWithThinking = new Button("Chat With Thinking");
    chatWithThinking.addClickListener(ev -> {
      Message m = Message.builder().loading(true)
          .sender(Sender.builder().name("Assistant").id("3").avatar("chatbot.png").build()).build();
      delayedMessage = Message.builder().content(message.getValue()).continued(false)
          .sender(Sender.builder().name("Assistant").id("1").avatar("chatbot.png").build()).build();

      chatAssistant.sendMessage(m);
      this.getElement().executeJs("setTimeout(()=>{this.$server.sendDelayedMessage();}, 5000)");
      message.clear();
    });
    chatAssistant.sendMessage(Message.builder().content("Hello, I am here to assist you")
        .sender(Sender.builder().name("Assistant").id("1").avatar("chatbot.png").build()).build());
    chatAssistant.toggle();
    chatAssistant.addChatSentListener(ev -> {
      if (ev.isRight()) {
        Notification.show(ev.getMessage());
      }
    });
    Icon minimize = VaadinIcon.MINUS.create();
    minimize.addClickListener(ev->chatAssistant.toggle());
    Span title = new Span("Customized Assistant");
    title.setWidthFull();
    HorizontalLayout headerBar = new HorizontalLayout(title, minimize);
    headerBar.setWidthFull();
    chatAssistant.setHeaderComponent(headerBar);
    
    HorizontalLayout footer = new HorizontalLayout();
    footer.setPadding(true);
    footer.setWidthFull();
    TextField messageTF = new TextField();
    messageTF.setWidthFull();
    messageTF.setPlaceholder("Type your message here ...");
    Button button = new Button("Send");
    button.addClickListener(ev->{
      Message m = Message.builder().content(messageTF.getValue()).right(true).build();
      chatAssistant.sendMessage(m);
      messageTF.clear();
    });
    messageTF.addKeyPressListener(Key.ENTER, ev->{
      button.click();
      messageTF.clear();
    });
    footer.add(messageTF,button);
    chatAssistant.setFooterComponent(footer);

    add(message, chat, chatWithThinking, chatAssistant);
  }
  
  @ClientCallable
  public void sendDelayedMessage() {
    if (delayedMessage!= null) {
      chatAssistant.sendMessage(delayedMessage);
      chatAssistant.hideLastLoading();
      delayedMessage = null;
    }
  }
  
}
