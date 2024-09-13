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

import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.flowingcode.vaadin.addons.chatassistant.model.Sender;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("Lazy Loading Demo")
@SuppressWarnings("serial")
@Route(value = "chat-assistant/lazy-loading-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantLazyLoadingDemo extends VerticalLayout {
  
  List<Message> messages = new ArrayList<Message>();
  
  public ChatAssistantLazyLoadingDemo() {
    ChatAssistant chatAssistant = new ChatAssistant();
    Span lazyLoadingData = new Span();
    DataProvider<Message,?> dataProvider = DataProvider.fromCallbacks(query->{
      lazyLoadingData.setText("Loading messages from: " + query.getOffset() + ", with limit: " + query.getLimit());
      return messages.stream().skip(query.getOffset()).limit(query.getLimit());
    }, query->{
      return messages.size();
    });
    chatAssistant.setDataProvider(dataProvider);
    
    TextArea message = new TextArea();
    message.setLabel("Enter a message from the assistant");
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
          .sender(Sender.builder().name("Assistant").avatar("chatbot.png").build()).build();

      messages.add(m);
      dataProvider.refreshAll();
      chatAssistant.scrollToEnd();
      message.clear();
    });
    Message welcomeMessage = Message.builder().content("Hello, I am here to assist you")
        .messageTime(LocalDateTime.now())
        .sender(Sender.builder().name("Assistant").avatar("chatbot.png").build()).build();
    messages.add(welcomeMessage);
    chatAssistant.toggle();
    chatAssistant.setSubmitListener(ev -> {
      Message userMessage = Message.builder().messageTime(LocalDateTime.now())
          .sender(Sender.builder().name("User").build()).content(ev.getValue()).build();
      messages.add(userMessage);
      dataProvider.refreshAll();
      chatAssistant.scrollToEnd();
    });
    Icon minimize = VaadinIcon.MINUS.create();
    minimize.addClickListener(ev -> chatAssistant.toggle());
    Span title = new Span("Customized Assistant Header");
    title.setWidthFull();
    HorizontalLayout headerBar = new HorizontalLayout(title, minimize);
    headerBar.setWidthFull();
    chatAssistant.setHeaderComponent(headerBar);

    add(message, lazyLoadingData, chat, chatAssistant);
  }
}
