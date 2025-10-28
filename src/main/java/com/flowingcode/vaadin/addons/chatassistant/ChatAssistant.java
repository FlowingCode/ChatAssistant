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
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableSupplier;
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
@NpmPackage(value = "react-draggable", version = "4.4.6")
@NpmPackage(value = "@mui/material", version = "7.1.2")
@NpmPackage(value = "@mui/icons-material", version = "6.1.0")
@NpmPackage(value = "@emotion/react", version = "11.14.0")
@NpmPackage(value = "@emotion/styled", version = "11.14.0")
@JsModule("./react/animated-fab.tsx")
@JsModule("./fcChatAssistantConnector.js")
@Tag("animated-fab")
@CssImport("./styles/chat-assistant-styles.css")
public class ChatAssistant<T extends Message> extends ReactAdapterComponent implements ClickNotifier<ChatAssistant<T>> {

  private static final String CHAT_HEADER_CLASS_NAME = "chat-header";

  private Component headerComponent;
  private VerticalLayout container;
  private Component footerContainer;
  private VirtualList<T> content = new VirtualList<>();
  private Popover chatWindow;
  private List<T> messages;
  private MessageInput messageInput;
  private Span whoIsTyping;
  private boolean minimized = false;
  private Registration defaultSubmitListenerRegistration;
  private SerializableSupplier<Avatar> avatarProvider = () -> new Avatar("Chat Assistant");
  private Avatar avatar;

  /**
   * Default constructor. Creates a ChatAssistant with no messages.
   */
  public ChatAssistant() {
    this(new ArrayList<>(), false);
  }

  /**
   * Creates a ChatAssistant with no messages.
   *
   * @param markdownEnabled flag to enable or disable markdown support
   */
  public ChatAssistant(boolean markdownEnabled) {
    this(new ArrayList<>(), markdownEnabled);
  }
  
  /**
   * Creates a ChatAssistant with the given list of messages.
   * 
   * @param messages the list of messages
   * @param markdownEnabled flag to enable or disable markdown support
   */
  public ChatAssistant(List<T> messages, boolean markdownEnabled) {
    this.messages = messages;
    initializeHeader();
    initializeFooter();
    initializeContent(markdownEnabled);
    initializeChatWindow();
    initializeAvatar();
  }

  private void initializeHeader() {
    Icon minimize = VaadinIcon.CLOSE.create();
    minimize.addClickListener(ev -> setMinimized(!minimized));
    Span title = new Span("Chat Assistant");
    title.setWidthFull();
    HorizontalLayout header = new HorizontalLayout(title, minimize);
    header.setWidthFull();
    headerComponent = header;
  }

  @SuppressWarnings("unchecked")
  private void initializeFooter() {
    messageInput = new MessageInput();
    messageInput.setWidthFull();
    messageInput.setMaxHeight("80px");
    messageInput.getStyle().set("padding", "0");
    defaultSubmitListenerRegistration = messageInput.addSubmitListener(se -> {
      sendMessage((T) Message.builder().messageTime(LocalDateTime.now())
          .name("User").content(se.getValue()).build());
    });
    whoIsTyping = new Span();
    whoIsTyping.setClassName("chat-assistant-who-is-typing");
    whoIsTyping.setVisible(false);
    VerticalLayout footer = new VerticalLayout(whoIsTyping, messageInput);
    footer.setWidthFull();
    footer.setSpacing(false);
    footer.setMargin(false);
    footer.setPadding(false);
    footerContainer = footer;
  }

  @SuppressWarnings("unchecked")
  private void initializeContent(boolean markdownEnabled) {
    content.setRenderer(new ComponentRenderer<>(message -> new ChatMessage<T>(message, markdownEnabled), 
        (component, message) -> {
          ((ChatMessage<T>) component).setMessage(message);
          return component;
        }));
    content.setItems(messages);
    content.setSizeFull();
    container = new VerticalLayout(headerComponent, content, footerContainer);
    container.setClassName("chat-assistant-container-vertical-layout");
    container.setPadding(false);
    container.setMargin(false);
    container.setSpacing(false);
    container.setSizeFull();
    container.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    container.setFlexGrow(1, content);
  }

  private void initializeChatWindow() {
    VerticalLayout resizableVL = new VerticalLayout();
    resizableVL.setClassName("chat-assistant-resizable-vertical-layout");
    resizableVL.add(container);
    chatWindow = new Popover();
    chatWindow.add(resizableVL);
    chatWindow.setOpenOnClick(false);
    chatWindow.setCloseOnOutsideClick(false);
    chatWindow.addOpenedChangeListener(ev -> minimized = !ev.isOpened());
    chatWindow.addAttachListener(e -> e.getUI().getPage()
        .executeJs("window.Vaadin.Flow.fcChatAssistantConnector.observePopoverResize($0)", chatWindow.getElement()));

    this.getElement().addEventListener("avatar-clicked", ev ->{
      if (this.minimized) {
        chatWindow.open();
      } else {
        chatWindow.close();
      }
    });
  }

  private void initializeAvatar() {
    if (avatar!=null) {
      avatar.removeFromParent();
    }
    avatar = avatarProvider.get();
    this.getElement().appendChild(avatar.getElement());
    this.addAttachListener(ev -> this.getElement().executeJs("return;")
        .then(ev2 -> this.getElement().executeJs("this.childNodes[1].childNodes[0].childNodes[0].appendChild($0)", avatar.getElement())
            .then(ev3 -> {
              chatWindow.setTarget(avatar);
              avatar.setSizeFull();
            })));
  }

  /**
   * Sets the data provider of the internal VirtualList.
   * 
   * @param dataProvider the data provider to be used
   */
  public void setDataProvider(DataProvider<T, ?> dataProvider) {
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

  private void refreshContent() {
    content.getDataProvider().refreshAll();
    content.scrollToEnd();
  }

  /**
   * Sends a message programmatically to the component. Should not be used when a custom
   * DataProvider is used. Instead just refresh the custom DataProvider.
   *
   * @param message the message to be sent programmatically
   */
  public void sendMessage(T message) {
    messages.add(message);
    content.getDataProvider().refreshAll();
    content.scrollToEnd();
  }
  
  /**
   * Updates a previously entered message.
   * 
   * @param message the message to be updated
   */
  public void updateMessage(T message) {
    this.content.getDataProvider().refreshItem(message);
  }

  /**
   * Shows or hides chat window.
   * 
   * @param minimized true for hiding the chat window and false for displaying it
   */
  public void setMinimized(boolean minimized) {
    if (this.minimized != minimized) {
      this.minimized = minimized;
      if (!minimized) {
        refreshContent();
      }
    }
    if (minimized && chatWindow.isOpened()) {
      chatWindow.close();
    } else if (!minimized && !chatWindow.isOpened()) {
      chatWindow.open();
    }
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
    if (headerComponent != null) {
      container.remove(headerComponent);
    }
    component.addClassName(CHAT_HEADER_CLASS_NAME);
    headerComponent = component;
    container.addComponentAsFirst(headerComponent);
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
    container.remove(footerContainer);
    footerContainer = component;
    container.add(footerContainer);
  }
  
  /**
   * Returns the current component configured as the footer of the chat window.
   * 
   * @return component used as the footer of the chat window 
   */
  public Component getFooterComponent() {
    return footerContainer;
  }
  
  /**
   * Scrolls to the given position. Scrolls so that the element is shown at
   * the start of the visible area whenever possible.
   * <p>
   * If the index parameter exceeds current item set size the grid will scroll
   * to the end.
   *
   * @param position
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
  
  /**
   * Allows changing the renderer used to display messages in the chat window.
   *
   * @param renderer the renderer to use for rendering {@link Message} objects, it cannot be null
   */
  public void setMessagesRenderer(Renderer<T> renderer) {
    Objects.requireNonNull(renderer, "Renderer cannot not be null");
    content.setRenderer(renderer);
  }
  
  /**
   * Sets the avatar provider that will be used to create the avatar
   * 
   * @param avatarProvider
   */
  public void setAvatarProvider(SerializableSupplier<Avatar> avatarProvider) {
    this.avatarProvider = avatarProvider;
    this.initializeAvatar();
  }

  /**
   * Return the number of unread messages to be displayed in the chat assistant.
   * @return the number of unread messages
   */
  public int getUnreadMessages() {
    Integer unreadMessages = getState("unreadMessages", Integer.class);
    return unreadMessages==null?0:unreadMessages;
  }

  /**
   * Sets the number of unread messages to be displayed in the chat assistant.
   * @param unreadMessages
   */
  public void setUnreadMessages(int unreadMessages) {
    setState("unreadMessages",unreadMessages);
  }

}
