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
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.shared.Registration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
  
  private static final String CHAT_HEADER_CLASS_NAME = "chat-header";
  private Component headerComponent;
  private Component footerComponent;
  private VerticalLayout footerContainer;
  private VirtualList<Message> content = new VirtualList<>();
  private List<Message> messages;
  private MessageInput messageInput;
  private Span whoIsTyping;
  private boolean minimized = false;
  private Registration defaultSubmitListenerRegistration;

  /**
   * Default constructor. Creates a ChatAssistant with no messages.
   */
  public ChatAssistant() {
    this(new ArrayList<>());
  }

  /**
   * Creates a ChatAssistant with the given list of messages.
   * 
   * @param messages the list of messages
   */
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
    footerContainer = new VerticalLayout(whoIsTyping);
    footerContainer.setSpacing(false);
    footerContainer.setMargin(false);
    footerContainer.setPadding(false);
    footerContainer.getElement().setAttribute("slot", "footer");
    add(footerContainer);
    this.setFooterComponent(messageInput);
    this.getElement().addEventListener("bot-button-clicked", this::handleClick).addEventData("event.detail");
    
    Icon minimize = VaadinIcon.CHEVRON_DOWN_SMALL.create();
    minimize.addClickListener(ev -> this.setMinimized(!minimized));
    Span title = new Span("Chat Assistant");
    title.setWidthFull();
    HorizontalLayout headerBar = new HorizontalLayout(title, minimize);
    headerBar.setWidthFull();
    this.setHeaderComponent(headerBar);
  }

  private void handleClick(DomEvent event) {
    minimized = event.getEventData().getObject("event.detail").getBoolean("minimized");
    if (!minimized) {
      refreshContent();
    }
  }
  
  /**
   * Sets the data provider of the internal VirtualList.
   * 
   * @param dataProvider the data provider to be used
   */
  public void setDataProvider(DataProvider<Message, ?> dataProvider) {
    content.setDataProvider(dataProvider);
  }

  /**
   * Uses the provided string as the text shown over the message input to indicate that someone is typing.
   * 
   * @param whoIsTyping string to be shown as an indication of someone typing
   */
  public void setWhoIsTyping(String whoIsTyping) {
    this.whoIsTyping.setText(whoIsTyping);
    this.whoIsTyping.setVisible(true);
  }
  
  /**
   * Returns the current text shown over the message input to indicate that someone is typing.
   * 
   * @return the current text or null if not configured
   */
  public String getWhoIsTyping() {
    return whoIsTyping.getText();
  }
  
  /**
   * Clears the text shown over the message input to indicate that someone is typing.
   */
  public void clearWhoIsTyping() {
    this.whoIsTyping.setText(null);
    this.whoIsTyping.setVisible(false);
  }
  
  /**
   * Sets the SubmitListener that will be notified when the user submits a message on the underlying messageInput.
   * 
   * @param listener the listener that will be notified when the SubmitEvent is fired
   * @return registration for removal of the listener
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
          let chatBotContainer = this.shadowRoot.querySelector($1);
          this.shadowRoot.querySelector($0).addEventListener("click", function() {
            let buttonClickedEvent = new CustomEvent("bot-button-clicked", {
                detail: {
                  minimized: chatBotContainer.classList.contains('animation-scale-out'),
                },
              });
            chatbot.dispatchEvent(buttonClickedEvent);
          });
        })
        """, ".bot-button", ".chatbot-container");
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
   * Sends a message programmatically to the component. Should not be used when a custom
   * DataProvider is used. Instead just refresh the custom DataProvider.
   *
   * @param message the message to be sent programmatically
   */
  public void sendMessage(Message message) {
    messages.add(message);
    content.getDataProvider().refreshAll();
    content.scrollToEnd();
  }
  
  /**
   * Updates a previously entered message.
   * 
   * @param message the message to be updated
   */
  public void updateMessage(Message message) {
    this.content.getDataProvider().refreshItem(message);
  }

  /**
   * Shows or hides chat window.
   * 
   * @param minimized true for hiding the chat window and false for displaying it
   */
  public void setMinimized(boolean minimized) {
    if (!minimized && this.minimized) {
      getElement().executeJs("setTimeout(() => {this.toggle();})");
      this.refreshContent();
    } else if (minimized && !this.minimized) {
      getElement().executeJs("setTimeout(() => {this.toggle();})");      
    }
    this.minimized = minimized;
  }
  
  /**
   * Returns the visibility of the chat window.
   * 
   * @return true if the chat window is minimized false otherwise
   */
  public boolean isMinimized() {
    return minimized;
  }
  
  /**
   * Allows changing the header of the chat window.
   * 
   * @param component to be used as a replacement for the header
   */
  public void setHeaderComponent(Component component) {
    if (headerComponent!=null) {
      this.remove(headerComponent);
    }
    component.addClassName(CHAT_HEADER_CLASS_NAME);
    this.headerComponent = component;
    this.getElement().executeJs("setTimeout(() => this.shadowRoot.querySelector($0).innerHTML = $1)", ".chatbot-header", "<slot name='header'></slot>");
    component.getElement().setAttribute("slot", "header");
    this.add(headerComponent);
  }
  
  /**
   * Returns the current component configured as the header of the chat window.
   * 
   * @return component used as the header of the chat window
   */
  public Component getHeaderComponent() {
    return headerComponent;
  }
  
  /**
   * Allows changing the footer of the chat window.
   * 
   * @param component to be used as a replacement for the footer, it cannot be null
   */
  public void setFooterComponent(Component component) {
    Objects.requireNonNull(component, "Component cannot not be null");
    if (footerComponent!=null) {
      this.footerContainer.remove(footerComponent);
    }
    this.getElement().executeJs("setTimeout(() => this.shadowRoot.querySelector($0).innerHTML = $1)", ".chat-footer", "<slot name='footer'></slot>");
    this.footerComponent = component;
    footerContainer.add(footerComponent);
  }
  
  /**
   * Returns the current component configured as the footer of the chat window.
   * 
   * @return component used as the footer of the chat window 
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
