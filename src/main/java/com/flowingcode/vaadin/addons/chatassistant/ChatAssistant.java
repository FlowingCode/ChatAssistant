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
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsModule("./fc-chat-assistant-movement.js")
@JsModule("./fc-chat-assistant-resize-top.js")
@JsModule("./fc-chat-assistant-resize-top-right.js")
@JsModule("./fc-chat-assistant-resize-right.js")
@JsModule("./fc-chat-assistant-resize-bottom-right.js")
@JsModule("./fc-chat-assistant-resize-bottom.js")
@JsModule("./fc-chat-assistant-resize-left.js")
@JsModule("./fc-chat-assistant-resize-bottom-left.js")
@JsModule("./fc-chat-assistant-resize-top-left.js")
@CssImport("./styles/fc-chat-assistant-style.css")
@Tag("animated-fab")
public class ChatAssistant<T extends Message> extends Div {

  protected SvgIcon fabIcon;

  protected final Button fab = new Button();
  protected final Div unreadBadge = new Div();
  protected final Div fabWrapper = new Div(fab, unreadBadge);
  protected final Popover chatWindow = new Popover();
  protected final Div overlay = new Div();
  protected final VerticalLayout container = new VerticalLayout();

  protected final Div resizerTop = new Div();
  protected final Div resizerBottom = new Div();
  protected final Div resizerTopRight = new Div();
  protected final Div resizerBottomRight = new Div();
  protected final Div resizerRight = new Div();
  protected final Div resizerLeft = new Div();
  protected final Div resizerBottomLeft = new Div();
  protected final Div resizerTopLeft = new Div();

  protected static final int DEFAULT_FAB_SIZE = 60;
  protected static final int DEFAULT_FAB_ICON_SIZE = 45;
  protected static final int DEFAULT_FAB_MARGIN = 25;
  protected static final int DEFAULT_RESIZER_SIZE = 25;
  protected static final int DEFAULT_MAX_RESIZER_SIZE = 200;
  protected static final int DEFAULT_DRAG_SENSITIVITY = 10;

  private static final int DEFAULT_CONTENT_MIN_WIDTH = 150;
  private static final int DEFAULT_CONTENT_MIN_HEIGHT = 150;
  private static final String DEFAULT_POPOVER_TAG = "fc-chat-assistant-popover";
  private static final String DEFAULT_FAB_CLASS = "fc-chat-assistant-fab";
  private static final String DEFAULT_RESIZE_CLASS = "fc-chat-assistant-resize";
  private static final String DEFAULT_UNREAD_BADGE_CLASS = "fc-chat-assistant-unread-badge";

  private Component headerComponent;
  private Component footerContainer;
  private VirtualList<T> content;
  private List<T> messages;
  private MessageInput messageInput;
  private Span whoIsTyping;
  private Registration defaultSubmitListenerRegistration;
  private int unreadMessages = 0;

  public ChatAssistant(List<T> messages, boolean markdownEnabled) {
    this.setUI();

    this.content = new VirtualList();
    this.messages = messages;
    this.initializeHeader();
    this.initializeFooter();
    this.initializeContent(markdownEnabled);
    this.initializeChatWindow();
  }

  public ChatAssistant() {
    this(new ArrayList(), false);
  }

  public ChatAssistant(boolean markdownEnabled) {
    this(new ArrayList(), markdownEnabled);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    addComponentRefreshedListener(
        "fc-chat-assistant-drag-listener",
        "window.fcChatAssistantMovement($0, $1, $2, $3, $4, $5);",
        this.getElement(), fabWrapper.getElement(), overlay, fab.getElement(), DEFAULT_FAB_MARGIN,
        DEFAULT_DRAG_SENSITIVITY

    );
    chatWindow.addOpenedChangeListener(ev -> {
      if (ev.isOpened()) {
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-top-listener",
            "window.fcChatAssistantResizeTop($0, $1, $2, $3, $4);",
            resizerTop.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE

        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-bottom-right-listener",
            "window.fcChatAssistantResizeBottomRight($0, $1, $2, $3, $4);",
            resizerBottomRight.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-top-right-listener",
            "window.fcChatAssistantResizeTopRight($0, $1, $2, $3, $4);",
            resizerTopRight.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-right-listener",
            "window.fcChatAssistantResizeRight($0, $1, $2, $3, $4);",
            resizerRight.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-bottom-listener",
            "window.fcChatAssistantResizeBottom($0, $1, $2, $3, $4);",
            resizerBottom.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-left-listener",
            "window.fcChatAssistantResizeLeft($0, $1, $2, $3, $4);",
            resizerLeft.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-top-left-listener",
            "window.fcChatAssistantResizeTopLeft($0, $1, $2, $3, $4);",
            resizerTopLeft.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
        addComponentRefreshedListener(
            "fc-chat-assistant-resize-bottom-left-listener",
            "window.fcChatAssistantResizeBottomLeft($0, $1, $2, $3, $4);",
            resizerBottomLeft.getElement(), overlay,
            DEFAULT_POPOVER_TAG, DEFAULT_RESIZER_SIZE, DEFAULT_MAX_RESIZER_SIZE
        );
      }
    });
  }

  private void setUI() {
    getStyle()
        .setZIndex(1000);

    overlay.getStyle()
        .setMinHeight(DEFAULT_CONTENT_MIN_HEIGHT + "px")
        .setMinWidth(DEFAULT_CONTENT_MIN_WIDTH + "px");

    fabIcon = new SvgIcon("/icons/chatbot.svg");
    fabIcon.setSize(DEFAULT_FAB_ICON_SIZE + "px");

    fab.getStyle()
        .setBorderRadius("50%")
        .setMinHeight(DEFAULT_FAB_SIZE + "px")
        .setMinWidth(DEFAULT_FAB_SIZE + "px")
        .setHeight(DEFAULT_FAB_SIZE + "px")
        .setWidth(DEFAULT_FAB_SIZE + "px")
        .setMaxHeight(DEFAULT_FAB_SIZE + "px")
        .setMaxWidth(DEFAULT_FAB_SIZE + "px");
    fab.setIcon(fabIcon);
    fab.addClassName(DEFAULT_FAB_CLASS);
    fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    fabWrapper.getStyle()
        .setHeight(DEFAULT_FAB_SIZE + "px")
        .setWidth(DEFAULT_FAB_SIZE + "px")
        .setDisplay(Style.Display.INLINE_FLEX)
        .setAlignItems(Style.AlignItems.CENTER)
        .setJustifyContent(Style.JustifyContent.CENTER)
        .setPosition(Style.Position.FIXED);

    unreadBadge.setText(String.valueOf(unreadMessages));
    unreadBadge.addClassName(DEFAULT_UNREAD_BADGE_CLASS);
    unreadBadge.getStyle()
        .setTextAlign(Style.TextAlign.CENTER)
        .setPosition(Style.Position.ABSOLUTE)
        .setJustifyContent(Style.JustifyContent.CENTER)
        .setAlignItems(Style.AlignItems.CENTER)
        .setDisplay(Style.Display.FLEX)
        .setPadding("var(--lumo-space-xs)")
        .setFontWeight(Style.FontWeight.BOLD)
        .setFontSize("var(--lumo-font-size-xs)")
        .setBorderRadius("50%")
        .setBackgroundColor("var(--lumo-warning-color)")
        .setScale("0")
        .setMinHeight("var(--lumo-font-size-xs)")
        .setMinWidth("var(--lumo-font-size-xs)")
        .setHeight("var(--lumo-font-size-xs)")
        .setWidth("var(--lumo-font-size-xs)")
        .setMaxHeight("var(--lumo-font-size-xs)")
        .setMaxWidth("var(--lumo-font-size-xs)")
        .setTop("0")
        .setRight("0")
        .setColor("var(--lumo-warning-text-color)");

    chatWindow.add(overlay);
    chatWindow.setPosition(PopoverPosition.TOP);
    chatWindow.addClassName(DEFAULT_POPOVER_TAG);
    chatWindow.setOpenOnClick(false);
    chatWindow.setTarget(fab);

    applyGenericResizerStyle(resizerTop, "top");
    resizerTop.getStyle()
        .setTop("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth("100%");

    applyGenericResizerStyle(resizerBottom, "bottom");
    resizerBottom.getStyle()
        .setBottom("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth("100%");

    applyGenericResizerStyle(resizerTopRight, "top-right");
    resizerTopRight.getStyle()
        .setRight("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    applyGenericResizerStyle(resizerBottomRight, "bottom-right");
    resizerBottomRight.getStyle()
        .setBottom("0")
        .setRight("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    applyGenericResizerStyle(resizerRight, "right");
    resizerRight.getStyle()
        .setRight("0")
        .setHeight("100%")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    applyGenericResizerStyle(resizerLeft, "left");
    resizerLeft.getStyle()
        .setLeft("0")
        .setHeight("100%")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    applyGenericResizerStyle(resizerBottomLeft, "bottom-left");
    resizerBottomLeft.getStyle()
        .setBottom("0")
        .setLeft("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    applyGenericResizerStyle(resizerTopLeft, "top-left");
    resizerTopLeft.getStyle()
        .setTop("0")
        .setLeft("0")
        .setHeight(DEFAULT_RESIZER_SIZE + "px")
        .setWidth(DEFAULT_RESIZER_SIZE + "px");

    overlay.add(
        resizerTop, resizerBottom,
        resizerRight, resizerTopRight, resizerBottomRight,
        resizerLeft, resizerTopLeft, resizerBottomLeft,
        container
    );
    add(chatWindow, fabWrapper);
  }

  /** Receives click events from the client side to toggle the chat window's opened state. */
  @ClientCallable
  protected void onClick() {
    if(chatWindow.isOpened()) {
      chatWindow.close();
    }
    else {
      chatWindow.open();
    }
  }

  /** Applies common styles to the resizer elements based on the specified direction. */
  protected void applyGenericResizerStyle(Div resizer, String direction) {
    resizer.getStyle()
        .setPosition(Style.Position.ABSOLUTE)
        .setDisplay(Style.Display.INLINE_BLOCK)
        .setZIndex(1001);
    resizer.addClassName(DEFAULT_RESIZE_CLASS + "-" + direction);
  }


  /**
   * Adds a component refresh listener that prevents stacking up duplicate listeners on the client side.
   * Uses a unique flag to track if the listener has already been added for this component instance,
   * ensuring the callback only executes once per component refresh cycle.
   *
   * @param uniqueFlag   a unique identifier for the component instance
   * @param executable   the JavaScript action to execute when the component is refreshed,
   * @param parameters   parameters for the executable
   */
  protected void addComponentRefreshedListener(String uniqueFlag, String executable, Serializable... parameters) {
    this.getElement().executeJs(
        String.format(
            """
            if(!this['%1$s']) { %2$s }
            if(!this['%1$s']) {
              this['%1$s'] = '%1$s';
            };
            """, uniqueFlag, executable),
        parameters
    );
  }

  /** Sets the icon for the floating action button.
   * <br>The icon's size is automatically adjusted to fit within the FAB. */
  public void setFabIcon(Component icon) {
    icon.getStyle()
        .setWidth(DEFAULT_FAB_ICON_SIZE + "px")
        .setHeight(DEFAULT_FAB_ICON_SIZE + "px");
    fab.setIcon(icon);
  }

  /** Sets the opened state of the chat window. If true, opens the window; if false, closes it. */
  public void setOpened(boolean opened) {
    if(opened) {
      chatWindow.open();
    }
    else {
      chatWindow.close();
    }
  }

  /** Opens the chat window. */
  public void open() {
    chatWindow.open();
  }

  /** Closes the chat window. */
  public void close() {
    chatWindow.close();
  }

  /** Sets the chat window minimum width. Applies when resizing. **/
  public void setWindowMinWidth(String minWidth) {
    this.overlay.setMinWidth(minWidth);
  }

  /** Sets the chat window minimum height. Applies when resizing. **/
  public void setWindowMinHeight(String minHeight) {
    this.overlay.setMinHeight(minHeight);
  }

  /** Sets the chat window maximum width. Applies when resizing. **/
  public void setWindowMaxWidth(String maxWidth) {
    this.overlay.setMaxWidth(maxWidth);
  }

  /** Sets the chat window maximum height. Applies when resizing. **/
  public void setWindowMaxHeight(String maxHeight) {
    this.overlay.setMaxHeight(maxHeight);
  }

  /** Sets the chat window default height. Applies when resizing. **/
  public void setWindowHeight(String height) {
    this.overlay.setHeight(height);
  }

  /** Sets the chat window default width. Applies when resizing. **/
  public void setWindowWidth(String width) {
    this.overlay.setWidth(width);
  }

  @SuppressWarnings("unchecked")
  private void initializeHeader() {
    Icon minimize = VaadinIcon.CLOSE.create();
    minimize.addClickListener((ev) -> onClick());
    Span title = new Span("Chat Assistant");
    title.setWidthFull();
    HorizontalLayout header = new HorizontalLayout(new Component[]{title, minimize});
    header.setWidthFull();
    this.headerComponent = header;
  }

  @SuppressWarnings("unchecked")
  private void initializeFooter() {
    this.messageInput = new MessageInput();
    this.messageInput.setWidthFull();
    this.messageInput.setMaxHeight("80px");
    this.messageInput.getStyle().set("padding", "0");
    this.defaultSubmitListenerRegistration = this.messageInput.addSubmitListener((se) -> this.sendMessage(
        (T) Message.builder().messageTime(
            LocalDateTime.now()).name("User").content(se.getValue()).build()));
    this.whoIsTyping = new Span();
    this.whoIsTyping.setClassName("chat-assistant-who-is-typing");
    this.whoIsTyping.setVisible(false);
    VerticalLayout footer = new VerticalLayout(new Component[]{this.whoIsTyping, this.messageInput});
    footer.setWidthFull();
    footer.setSpacing(false);
    footer.setMargin(false);
    footer.setPadding(false);
    this.footerContainer = footer;
  }

  @SuppressWarnings("unchecked")
  private void initializeContent(boolean markdownEnabled) {
    this.content.setRenderer(new ComponentRenderer((message) -> new ChatMessage((Message) message, markdownEnabled), (component, message) -> {
      ((ChatMessage)component).setMessage((Message) message);
      return component;
    }));
    this.content.setItems(this.messages);
    this.content.setSizeFull();
    this.container.add(this.headerComponent, this.content, this.footerContainer);
    this.container.setPadding(true);
    this.container.setMargin(false);
    this.container.setSpacing(false);
    this.container.setSizeFull();
    this.container.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    this.container.setFlexGrow((double)1.0F, new HasElement[]{this.content});
  }

  private void initializeChatWindow() {
    this.chatWindow.setOpenOnClick(false);
    this.chatWindow.setCloseOnOutsideClick(false);
  }

  @SuppressWarnings("unchecked")
  public void setDataProvider(DataProvider<T, ?> dataProvider) {
    this.content.setDataProvider(dataProvider);
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
    this.whoIsTyping.setText((String)null);
    this.whoIsTyping.setVisible(false);
  }

  /**
   * Sets the SubmitListener that will be notified when the user submits a message on the underlying messageInput.
   *
   * @param listener the listener that will be notified when the SubmitEvent is fired
   * @return registration for removal of the listener
   */
  public Registration setSubmitListener(ComponentEventListener<MessageInput.SubmitEvent> listener) {
    if(this.defaultSubmitListenerRegistration != null) {
      this.defaultSubmitListenerRegistration.remove();
    }
    this.defaultSubmitListenerRegistration = this.messageInput.addSubmitListener(listener);
    return this.defaultSubmitListenerRegistration;
  }

  private void refreshContent() {
    this.content.getDataProvider().refreshAll();
    this.content.scrollToEnd();
  }

  /**
   * Sends a message programmatically to the component. Should not be used when a custom
   * DataProvider is used. Instead just refresh the custom DataProvider.
   *
   * @param message the message to be sent programmatically
   */
  public void sendMessage(T message) {
    this.messages.add(message);
    this.content.getDataProvider().refreshAll();
    this.content.scrollToEnd();
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
    if (minimized && this.chatWindow.isOpened()) {
      this.chatWindow.close();
    } else if (!minimized && !this.chatWindow.isOpened()) {
      this.chatWindow.open();
    }
  }

  /**
   * Returns the visibility of the chat window.
   *
   * @return true if the chat window is minimized false otherwise
   */
  public boolean isMinimized() {
    return !chatWindow.isOpened();
  }

  /**
   * Allows changing the header of the chat window.
   *
   * @param component to be used as a replacement for the header
   */
  public void setHeaderComponent(Component component) {
    if (this.headerComponent != null) {
      this.container.remove(new Component[]{this.headerComponent});
    }

    component.addClassName("chat-header");
    this.headerComponent = component;
    this.container.addComponentAsFirst(this.headerComponent);
  }

  /**
   * Returns the current component configured as the header of the chat window.
   *
   * @return component used as the header of the chat window
   */
  public Component getHeaderComponent() {
    return this.headerComponent;
  }

  /**
   * Allows changing the footer of the chat window.
   *
   * @param component to be used as a replacement for the footer, it cannot be null
   */
  public void setFooterComponent(Component component) {
    Objects.requireNonNull(component, "Component cannot not be null");
    this.container.remove(new Component[]{this.footerContainer});
    this.footerContainer = component;
    this.container.add(new Component[]{this.footerContainer});
  }

  /**
   * Returns the current component configured as the footer of the chat window.
   *
   * @return component used as the footer of the chat window
   */
  public Component getFooterComponent() {
    return this.footerContainer;
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
    this.content.setRenderer(renderer);
  }

  /**
   * Sets the avatar provider that will be used to create the avatar
   *
   * @param avatarProvider
   * @deprecated use {@link #setFabIcon(Component)} instead
   */
  @Deprecated(since = "5.0.0", forRemoval = true)
  public void setAvatarProvider(SerializableSupplier<Avatar> avatarProvider) {
    Objects.requireNonNull(avatarProvider, "Avatar provider cannot be null");
    Avatar avatar = avatarProvider.get();
    setFabIcon(avatar);
  }

    /**
     * Return the number of unread messages to be displayed in the chat assistant.
     * @return the number of unread messages
     */
  public int getUnreadMessages() {
    return Math.max(unreadMessages, 0);
  }

  /**
   * Sets the number of unread messages to be displayed in the chat assistant.
   * @param unreadMessages
   */
  public void setUnreadMessages(int unreadMessages) {
    this.unreadMessages = unreadMessages >= 0 ? Math.min(unreadMessages, 99) : 0;
    unreadBadge.setText(String.valueOf(this.unreadMessages));
    if(this.unreadMessages > 0) {
      unreadBadge.getStyle().setScale("1");
    }
    else {
      unreadBadge.getStyle().setScale("0");
    }
  }
}
