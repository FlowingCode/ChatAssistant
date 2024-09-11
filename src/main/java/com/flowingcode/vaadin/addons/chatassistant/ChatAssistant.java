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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that allows to create a floating chat button that will open a chat window that can be
 * used to provide a chat assistant feature.
 *
 * @author mmlopez
 */
@SuppressWarnings("serial")
@NpmPackage(value = "wc-chatbot", version = "0.2.0")
@JsModule("wc-chatbot/dist/wc-chatbot.js")
@CssImport("./styles/chat-assistant-styles.css")
@Tag("chat-bot")
public class ChatAssistant extends Div {
  
  private Component headerComponent;
  private Component footerComponent;
  private VirtualList<Message> content = new VirtualList<>();
  private List<Message> messages;
  private MessageInput messageInput;
  
  public ChatAssistant() {
    this(new ArrayList<>());
  }

  public ChatAssistant(List<Message> messages) {
    this.messages = messages;
    this.getElement().executeJs("return;").then(
        (ev) -> this.getElement().executeJs("this.shadowRoot.querySelector($0).innerHTML = $1",
            ".chatbot-body", "<slot name='content'></slot>"));
    this.getElement().executeJs("return;")
        .then((ev) -> this.getElement().executeJs(
            "this.shadowRoot.querySelector($0).style.setProperty('padding', '0px');",
            ".chatbot-body"));
    content.getElement().setAttribute("slot", "content");
    content.setItems(messages);

    content.setRenderer(new ComponentRenderer<ChatMessage, Message>(
        message -> new ChatMessage(message), (component, message) -> {
          ((ChatMessage) component).setMessage(message);
          return component;
        }));
    this.add(content);
    messageInput = new MessageInput();
    messageInput.addSubmitListener(
        se -> this.sendMessage(Message.builder().messageTime(LocalDateTime.now())
            .sender(Sender.builder().name("User").build()).content(se.getValue()).build()));
    this.setFooterComponent(messageInput);
  }
  
  public Registration addSubmitListener(ComponentEventListener<SubmitEvent> listener) {
    return messageInput.addSubmitListener(listener);
  }

  /**
   * Sends a message represented by the string message programmatically to the component, with
   * default settings.
   *
   * @param message
   */
  public void sendMessage(String message) {
    messages.add(Message.builder().content(message).build());
    content.getDataProvider().refreshAll();
    content.scrollToEnd();
  }

  /**
   * Sends a message programmatically to the component
   *
   * @param message
   */
  public void sendMessage(Message message) {
    messages.add(message);
    content.getDataProvider().refreshAll();
    content.scrollToEnd();
  }
  
  /**
   * Updates a message
   * @param message
   */
  public void updateMessage(Message message) {
    this.content.getDataProvider().refreshItem(message);
  }

  /**
   * Shows or hides chat assistant
   */
  public void toggle() {
    getElement().executeJs("setTimeout(() => {this.toggle();})");
  }
  
  /**
   * Sets a component as a replacement for the header of the chat
   * @param component
   */
  public void setHeaderComponent(Component component) {
    this.headerComponent = component;
    this.getElement().executeJs("return;").then((ev) -> this.getElement()
        .executeJs("this.shadowRoot.querySelector($0).innerHTML = $1", ".chatbot-header", "<slot name='header'></slot>"));
    component.getElement().setAttribute("slot", "header");
    this.add(headerComponent);
  }
  
  /**
   * Returns the current component configured as a replacement for the header of the chat
   * @return
   */
  public Component getHeaderComponent() {
    return headerComponent;
  }
  
  /**
   * Sets a Vaadin component as a replacement for the footer of the chat
   * @param component
   */
  public void setFooterComponent(Component component) {
    this.footerComponent = component;
    this.getElement().executeJs("return;").then((ev) -> this.getElement()
        .executeJs("this.shadowRoot.querySelector($0).innerHTML = $1", ".chat-footer", "<slot name='footer'></slot>"));
    component.getElement().setAttribute("slot", "footer");
    this.add(footerComponent);
  }
  
  /**
   * Returns the current Vaadin component configured as a replacement for the footer of the chat
   * @return
   */
  public Component getFooterComponent() {
    return footerComponent;
  }
  
  
}
