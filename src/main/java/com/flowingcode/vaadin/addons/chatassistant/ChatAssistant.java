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
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.DomEvent;
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
  private Span whoIsTyping;
  private boolean minimized = true;
  private Registration defaultSubmitListenerRegistration;

  public ChatAssistant() {
    this(new ArrayList<>());
  }

  public ChatAssistant(List<Message> messages) {
    this.messages = messages;
    content.getElement().setAttribute("slot", "content");
    content.setItems(messages);

    content.setRenderer(new ComponentRenderer<ChatMessage, Message>(
        message -> new ChatMessage(message), (component, message) -> {
          ((ChatMessage) component).setMessage(message);
          return component;
        }));
    this.add(content);
    messageInput = new MessageInput();
    messageInput.setSizeFull();
    defaultSubmitListenerRegistration = messageInput
        .addSubmitListener(se -> this.sendMessage(Message.builder().messageTime(LocalDateTime.now())
            .name("User").content(se.getValue()).build()));
    whoIsTyping = new Span();
    whoIsTyping.setClassName("chat-assistant-who-is-typing");
    whoIsTyping.setVisible(false);
    VerticalLayout vl = new VerticalLayout(whoIsTyping, messageInput);
    vl.setSpacing(false);
    vl.setMargin(false);
    vl.setPadding(false);
    this.setFooterComponent(vl);
    this.getElement().addEventListener("bot-button-clicked", this::handleClick);
  }

  private void handleClick(DomEvent event) {
    getElement().executeJs(
        "return this.shadowRoot.querySelector(\".chatbot-container\").classList.contains('animation-scale-out')")
        .then(result -> {
          minimized = result.asBoolean();
          if (!minimized) {
            refreshContent();
          }
        });
  }
  
  /**
   * Sets the data provider of the internal VirtualList
   * @param dataProvider
   */
  public void setDataProvider(DataProvider<Message, ?> dataProvider) {
    content.setDataProvider(dataProvider);
  }

  /**
   * Uses the provided string as the text shown over the message input to indicate that someone is typing
   * @param whoIsTyping
   */
  public void setWhoIsTyping(String whoIsTyping) {
    this.whoIsTyping.setText(whoIsTyping);
    this.whoIsTyping.setVisible(true);
  }
  
  /**
   * Returns the current text shown over the message input to indicate that someone is typing
   * @return the current text or null if not configured
   */
  public String getWhoIsTyping() {
    return whoIsTyping.getText();
  }
  
  /**
   * Clears the text shown over the message input to indicate that someone is typing
   */
  public void clearWhoIsTyping() {
    this.whoIsTyping.setText(null);
    this.whoIsTyping.setVisible(false);
  }
  
  /**
   * Sets the SubmitListener that will be notified when the user submits a message on the underlying messageInput
   * @param listener
   * @return
   */
  public Registration setSubmitListener(ComponentEventListener<SubmitEvent> listener) {
    defaultSubmitListenerRegistration.remove();
    return messageInput.addSubmitListener(listener);
  }
  
  protected void onAttach(AttachEvent attachEvent) {
    if (!minimized) {
      getElement().executeJs("setTimeout(() => this.toggle())");
      this.getElement().executeJs("return;").then((ev) -> {
        refreshContent();
      });
    }
    this.getElement().executeJs("setTimeout(() => this.shadowRoot.querySelector($0).innerHTML = $1)",
          ".chatbot-body", "<slot name='content'></slot>");
      this.getElement().executeJs(
          "this.shadowRoot.querySelector($0).style.setProperty('padding', '0px');",
          ".chatbot-body");
    this.getElement().executeJs("""
        setTimeout(() => {
          let chatbot = this;
          this.shadowRoot.querySelector($0).addEventListener("click", function() {
            chatbot.dispatchEvent(new CustomEvent("bot-button-clicked"));
          });
        })
        """, ".bot-button");
    if (footerComponent!=null) {
      this.setFooterComponent(footerComponent);
    }
    if (headerComponent!=null) {
      this.setHeaderComponent(headerComponent);
    }
  }

  private void refreshContent() {
    this.content.getDataProvider().refreshAll();
    this.content.getElement().executeJs("this.requestContentUpdate();");
    this.content.scrollToEnd();
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
    getElement().executeJs(
        "return this.shadowRoot.querySelector(\".chatbot-container\").classList.contains('animation-scale-out')")
        .then(result -> {
          minimized = result.asBoolean(); 
          if (!minimized) {
            refreshContent();
          }
        });
  }
  
  /**
   * Sets a component as a replacement for the header of the chat
   * @param component
   */
  public void setHeaderComponent(Component component) {
    this.headerComponent = component;
    this.getElement().executeJs("setTimeout(() => this.shadowRoot.querySelector($0).innerHTML = $1)", ".chatbot-header", "<slot name='header'></slot>");
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
    this.getElement().executeJs("setTimeout(() => this.shadowRoot.querySelector($0).innerHTML = $1)", ".chat-footer", "<slot name='footer'></slot>");
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
  
  /**
   * Scrolls to the given row index. Scrolls so that the element is shown at
   * the start of the visible area whenever possible.
   * <p>
   * If the index parameter exceeds current item set size the grid will scroll
   * to the end.
   *
   * @param rowIndex
   *            zero based index of the item to scroll to in the current view.
   */
  public void scrollToIndex(int position) {
    this.content.scrollToIndex(position);
  }
  
  /**
   * Scrolls to the first element.
   */
  public void scrollToStart() {
    this.content.scrollToStart();
  }
  
  /**
   * Scrolls to the last element of the list.
   */
  public void scrollToEnd() {
    this.content.scrollToEnd();
  }
  
}
