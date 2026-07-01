/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
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

import com.flowingcode.vaadin.addons.chatassistant.model.ChatAssistantMode;
import com.flowingcode.vaadin.addons.chatassistant.model.FabPosition;
import com.flowingcode.vaadin.addons.chatassistant.model.FabVariant;
import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.dom.Style.AlignItems;
import com.vaadin.flow.dom.Style.AlignSelf;
import com.vaadin.flow.dom.Style.Display;
import com.vaadin.flow.dom.Style.Position;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.shared.Registration;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that allows to create a floating chat button that will open a chat window that can be
 * used to provide a chat assistant feature.
 *
 * <p>Only one {@code ChatAssistant} is supported per {@link UI}: the FAB and chat window are
 * UI-level surfaces. If a second instance is attached to the same UI it stays inactive and hidden
 * (a warning is logged) instead of competing with the already-attached instance.
 *
 * @author mmlopez
 */
@JsModule("./fc-chat-assistant-movement.js")
@JsModule("./fc-chat-assistant-resize.js")
@CssImport("./styles/fc-chat-assistant-style.css")
@Tag("animated-fab")
public class ChatAssistant<T extends Message> extends Div {

  protected Component fabIcon = createDefaultFabIcon();

  protected final Button fab = new Button();
  protected final Div unreadBadge = new Div();
  protected final Div fabWrapper = new Div(fab, unreadBadge);
  protected final Popover chatWindow = new Popover();
  protected final Div overlay = new Div();
  protected final VerticalLayout container = new VerticalLayout();

  /** The resize handle directions, in creation order. */
  private static final List<String> RESIZER_DIRECTIONS =
      List.of(
          "top", "bottom", "right", "top-right", "bottom-right", "left", "top-left", "bottom-left");

  /** The eight resize handles keyed by direction (see {@link #RESIZER_DIRECTIONS}). */
  private final Map<String, Div> resizers = new LinkedHashMap<>();

  protected static final int DEFAULT_FAB_SIZE = 60;
  protected static final int DEFAULT_FAB_ICON_SIZE = 40;
  protected static final int DEFAULT_FAB_MARGIN = 25;
  protected static final int DEFAULT_RESIZER_SIZE = 25;
  protected static final int DEFAULT_MAX_RESIZER_SIZE = 200;
  protected static final int DEFAULT_DRAG_SENSITIVITY = 25;

  private static final ChatAssistantMode DEFAULT_MODE = ChatAssistantMode.DESKTOP;
  private static final FabPosition DEFAULT_POSITION = FabPosition.BOTTOM_RIGHT;
  private static final boolean DEFAULT_FAB_ANCHORED_TO_VIEWPORT = true;
  private static final boolean DEFAULT_RESIZE_INDICATORS_VISIBLE = false;
  private static final boolean DEFAULT_WINDOW_RESIZABLE = true;
  private static final boolean DEFAULT_FAB_MOVABLE = true;
  private static final int DEFAULT_CONTENT_MIN_WIDTH = 150;
  private static final int DEFAULT_CONTENT_MIN_HEIGHT = 150;
  private static final int DEFAULT_FAB_SMALL_SIZE = 50;
  private static final int DEFAULT_FAB_LARGE_SIZE = 72;
  private static final int DEFAULT_MOBILE_BREAKPOINT = 768;
  private static final String DEFAULT_POPOVER_TAG = "fc-chat-assistant-popover";
  private static final String DEFAULT_DIALOG_TAG = "fc-chat-assistant-dialog";
  private static final String DEFAULT_FAB_CLASS = "fc-chat-assistant-fab";
  private static final String DEFAULT_RESIZE_CLASS = "fc-chat-assistant-resize";
  private static final String DEFAULT_UNREAD_BADGE_CLASS = "fc-chat-assistant-unread-badge";
  private static final String RESIZE_INDICATOR_VISIBLE_CLASS = "fc-chat-assistant-resize-indicator-visible";
  // Theme-agnostic token fallback chains (Lumo token, then Aura token, then a literal) so the badge
  // renders correctly under both the Lumo and Aura themes of Vaadin 25.
  private static final String DEFAULT_UNREAD_BADGE_BACKGROUND ="var(--lumo-warning-color, var(--aura-accent-yellow, #e07a00))";
  private static final String DEFAULT_UNREAD_BADGE_COLOR = "var(--lumo-warning-contrast-color, var(--aura-accent-contrast-color, #ffffff))";
  // Shared spacing/typography tokens used for the badge, with the same cross-theme fallback pattern.
  private static final String BADGE_FONT_SIZE = "var(--lumo-font-size-xs, var(--aura-font-size-s, 0.75rem))";
  private static final String BADGE_PADDING = "var(--lumo-space-xs, var(--vaadin-padding-xs, 0.25rem))";
  // Key under which the active ChatAssistant is tracked on its UI to enforce a single instance per UI.
  private static final String UI_INSTANCE_KEY = "fc-chat-assistant-ui-instance";
  // Custom CSS properties that carry the chat window size constraints to the resize frontend.
  private static final String CSS_WIDTH = "--fc-width";
  private static final String CSS_HEIGHT = "--fc-height";
  private static final String CSS_MIN_WIDTH = "--fc-min-width";
  private static final String CSS_MIN_HEIGHT = "--fc-min-height";
  private static final String CSS_MAX_WIDTH = "--fc-max-width";
  private static final String CSS_MAX_HEIGHT = "--fc-max-height";
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatAssistant.class);

  /** Lazily loaded and cached data URI of the bundled chatbot SVG (see {@link #getDefaultFabIconSrc()}). */
  private static volatile String defaultFabIconSrc;

  private final Map<Integer, ScreenSizeListenerEntry> screenSizeListeners = new HashMap<>();
  private final Dialog mobileChatWindow = new Dialog();

  private final VirtualList<T> content = new VirtualList<>();
  private final List<T> messages = new ArrayList<>();

  private MessageInput messageInput;
  private ChatAssistantMode mode = DEFAULT_MODE;
  private Component headerComponent;
  private Component footerContainer;
  private FabPosition fabPosition = DEFAULT_POSITION;
  private FabVariant activeSizeVariant = null;
  private Span whoIsTyping;
  private Registration defaultSubmitListenerRegistration;
  private boolean resizable = DEFAULT_WINDOW_RESIZABLE;
  private boolean fabMovable = DEFAULT_FAB_MOVABLE;
  private boolean desktopFabMovablePreference = DEFAULT_FAB_MOVABLE;
  private boolean mobileModeSwitchingEnabled = false;
  private boolean fabAnchoredToViewport = DEFAULT_FAB_ANCHORED_TO_VIEWPORT;
  private boolean resizeIndicatorsVisible = DEFAULT_RESIZE_INDICATORS_VISIBLE;
  private int mobileBreakpoint = DEFAULT_MOBILE_BREAKPOINT;
  private int fabMargin = DEFAULT_FAB_MARGIN;
  private int fabSize = DEFAULT_FAB_SIZE;
  private int screenSizeKeySeq = 0;
  private int unreadMessages = 0;

  /**
   * Creates a ChatAssistant with the given initial messages, using the defaults for every other
   * setting. The messages are copied defensively: later changes to the supplied list are not
   * reflected, and the component's own updates do not leak back into it. Mutate the conversation
   * through {@link #sendMessage(Message)} rather than the supplied list.
   *
   * <p>To configure multiple aspects at construction time, prefer {@code ChatAssistant.builder()}.
   *
   * @param messages the initial messages
   * @param markdownEnabled flag to enable or disable markdown support
   */
  public ChatAssistant(List<T> messages, boolean markdownEnabled) {
    this(
        null,
        DEFAULT_WINDOW_RESIZABLE,
        DEFAULT_FAB_MOVABLE,
        null,
        DEFAULT_FAB_ANCHORED_TO_VIEWPORT,
        DEFAULT_RESIZE_INDICATORS_VISIBLE,
        markdownEnabled,
        messages,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  /**
   * Creates a ChatAssistant with no messages, using the defaults for every setting.
   *
   * <p>To configure multiple aspects at construction time, prefer {@code ChatAssistant.builder()}.
   */
  public ChatAssistant() {
    this(new ArrayList<>(), false);
  }

  /**
   * Creates a ChatAssistant with no messages, using the defaults for every other setting.
   *
   * <p>To configure multiple aspects at construction time, prefer {@code ChatAssistant.builder()}.
   *
   * @param markdownEnabled flag to enable or disable markdown support
   */
  public ChatAssistant(boolean markdownEnabled) {
    this(new ArrayList<>(), markdownEnabled);
  }

  /**
   * Builder constructor. Every configurable aspect is optional; when a value is {@code null} or
   * invalid it falls back to its corresponding default.
   *
   * @param fabIcon the FAB icon ({@code null} keeps the default chatbot icon)
   * @param resizable whether the chat window is resizable (default {@value
   *     #DEFAULT_WINDOW_RESIZABLE})
   * @param fabMovable whether the FAB can be dragged (default {@value #DEFAULT_FAB_MOVABLE})
   * @param mobileBreakpoint the maximum screen width in pixels below which mobile mode is activated
   *     automatically. Providing any non-null value <b>enables automatic switching</b> (disabled by
   *     default); {@code null} leaves it disabled, and {@code 0} enables switching but keeps the
   *     component in desktop mode at any width
   * @param fabAnchoredToViewport whether the FAB is anchored to the viewport ({@code true}, the
   *     default) or positioned within its container ({@code false}) (default {@value
   *     #DEFAULT_FAB_ANCHORED_TO_VIEWPORT})
   * @param resizeIndicatorsVisible whether the resize handles show a direction arrowhead (default
   *     {@value #DEFAULT_RESIZE_INDICATORS_VISIBLE})
   * @param markdownEnabled whether markdown is enabled in messages
   * @param messages the initial messages ({@code null} starts empty)
   * @param defaultFabPosition the FAB's initial corner (default {@link FabPosition#BOTTOM_RIGHT})
   * @param defaultFabMargin the FAB's margin to the viewport edges in pixels (default {@value
   *     #DEFAULT_FAB_MARGIN})
   * @param minWidth the chat window minimum width ({@code null} keeps the default)
   * @param minHeight the chat window minimum height ({@code null} keeps the default)
   * @param width the chat window's initial width as an absolute CSS length ({@code null} keeps the
   *     default)
   * @param height the chat window's initial height as an absolute CSS length ({@code null} keeps
   *     the default)
   * @param maxWidth the chat window maximum width ({@code null} keeps the default)
   * @param maxHeight the chat window maximum height ({@code null} keeps the default)
   */
  @Builder
  private ChatAssistant(
      SvgIcon fabIcon,
      boolean resizable,
      boolean fabMovable,
      Integer mobileBreakpoint,
      boolean fabAnchoredToViewport,
      boolean resizeIndicatorsVisible,
      boolean markdownEnabled,
      List<T> messages,
      FabPosition defaultFabPosition,
      String defaultFabMargin,
      String minWidth,
      String minHeight,
      String width,
      String height,
      String maxWidth,
      String maxHeight) {
    if (messages != null) {
      this.messages.addAll(messages);
    }
    this.setUI(
        fabIcon,
        resizable,
        fabMovable,
        mobileBreakpoint,
        fabAnchoredToViewport,
        resizeIndicatorsVisible,
        defaultFabPosition,
        defaultFabMargin,
        minWidth,
        minHeight,
        width,
        height,
        maxWidth,
        maxHeight);
    this.initializeHeader();
    this.initializeFooter();
    this.initializeContent(markdownEnabled);
    this.initializeChatWindow();
  }

  /** Seeds the builder defaults for the flags whose real default is not {@code false}. */
  public static class ChatAssistantBuilder<T extends Message> {
    private boolean resizable = DEFAULT_WINDOW_RESIZABLE;
    private boolean fabMovable = DEFAULT_FAB_MOVABLE;
    private boolean fabAnchoredToViewport = DEFAULT_FAB_ANCHORED_TO_VIEWPORT;
  }

  /**
   * Initializes the UI applying the given configuration. Any {@code null} or invalid value falls
   * back to its corresponding default.
   */
  private void setUI(
      SvgIcon fabIcon,
      boolean resizable,
      boolean fabMovable,
      Integer mobileBreakpoint,
      boolean fabAnchoredToViewport,
      boolean resizeIndicatorsVisible,
      FabPosition fabPosition,
      String fabMargin,
      String minWidth,
      String minHeight,
      String width,
      String height,
      String maxWidth,
      String maxHeight) {
    String fontSize = BADGE_FONT_SIZE;
    getStyle().setZIndex(1000);

    // The overlay fills the popover content part (which Vaadin clamps to the viewport); resizing
    // writes the desired size onto the popover content, never onto this Div, so it can't overflow.
    // The resize bounds live in custom properties read by the resize script (setting them here has
    // no layout effect on the 100% Div, avoiding any overflow).
    overlay
        .getStyle()
        .setDisplay(Display.FLEX)
        .setAlignItems(AlignItems.STRETCH)
        .set("flex", "1")
        .setMaxHeight("100%")
        .setBoxSizing(Style.BoxSizing.BORDER_BOX)
        // Allow the overlay to shrink below its content size so the message VirtualList scrolls
        // internally under the popover's viewport clamp instead of forcing the window taller.
        .setMinWidth("0")
        .setMinHeight("0")
        .set(CSS_MIN_WIDTH, DEFAULT_CONTENT_MIN_WIDTH + "px")
        .set(CSS_MIN_HEIGHT, DEFAULT_CONTENT_MIN_HEIGHT + "px");

    mobileChatWindow.setSizeFull();
    mobileChatWindow.setModal(false);
    mobileChatWindow.setDraggable(false);
    mobileChatWindow.setResizable(false);
    mobileChatWindow.addClassName(DEFAULT_DIALOG_TAG);
    // The dialog content is laid out on open; re-deliver the current size to each listener, matching
    // the desktop popover behavior (once-per-open initial delivery).
    mobileChatWindow.addOpenedChangeListener(
        ev -> {
          if (ev.isOpened()) {
            screenSizeListeners.keySet().forEach(this::applyScreenSizeListener);
          }
        });

    this.fabIcon = fabIcon != null ? fabIcon : createDefaultFabIcon();

    fab.getStyle().setBorderRadius("50%");
    fab.setIcon(this.fabIcon);
    fab.addClassName(DEFAULT_FAB_CLASS);
    fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    fabWrapper
        .getStyle()
        .setDisplay(Style.Display.INLINE_FLEX)
        .setAlignItems(Style.AlignItems.CENTER)
        .setJustifyContent(Style.JustifyContent.CENTER)
        .setPosition(Style.Position.FIXED);

    // Apply the FAB diameter and icon size (the single sizing code path).
    setFabSize(DEFAULT_FAB_SIZE);

    unreadBadge.setText(String.valueOf(unreadMessages));
    unreadBadge.addClassName(DEFAULT_UNREAD_BADGE_CLASS);
    unreadBadge
        .getStyle()
        .setTextAlign(Style.TextAlign.CENTER)
        .setPosition(Style.Position.ABSOLUTE)
        .setJustifyContent(Style.JustifyContent.CENTER)
        .setAlignItems(Style.AlignItems.CENTER)
        .setDisplay(Style.Display.FLEX)
        .setPadding(BADGE_PADDING)
        .setFontWeight(Style.FontWeight.BOLD)
        .setFontSize(fontSize)
        .setBorderRadius("50%")
        .setBackgroundColor(DEFAULT_UNREAD_BADGE_BACKGROUND)
        .setScale("0")
        .setMinHeight(fontSize)
        .setMinWidth(fontSize)
        .setHeight(fontSize)
        .setWidth(fontSize)
        .setMaxHeight(fontSize)
        .setMaxWidth(fontSize)
        .setTop("0")
        .setRight("0")
        .setColor(DEFAULT_UNREAD_BADGE_COLOR);

    chatWindow.add(overlay);
    chatWindow.setPosition(PopoverPosition.TOP);
    chatWindow.addClassName(DEFAULT_POPOVER_TAG);
    chatWindow.setTarget(fab);

    for (String direction : RESIZER_DIRECTIONS) {
      Div resizer = new Div();
      applyGenericResizerStyle(resizer, direction);
      applyResizerEdgeStyle(resizer, direction);
      resizers.put(direction, resizer);
      overlay.add(resizer);
    }
    overlay.add(container);

    this.fabPosition = fabPosition != null ? fabPosition : DEFAULT_POSITION;
    this.fabMargin = parseFabMargin(fabMargin);

    if (minWidth != null) {
      setWindowMinWidth(minWidth);
    }
    if (minHeight != null) {
      setWindowMinHeight(minHeight);
    }
    if (maxWidth != null) {
      setWindowMaxWidth(maxWidth);
    }
    if (maxHeight != null) {
      setWindowMaxHeight(maxHeight);
    }
    if (width != null) {
      setWindowWidth(width);
    }
    if (height != null) {
      setWindowHeight(height);
    }

    // A breakpoint enables automatic mobile switching; leaving it null keeps switching off.
    if (mobileBreakpoint != null) {
      this.mobileBreakpoint = Math.max(mobileBreakpoint, 0);
      this.mobileModeSwitchingEnabled = true;
    }

    setFabMovable(fabMovable);
    setWindowResizable(resizable);
    setFabAnchoredToViewport(fabAnchoredToViewport);
    setResizeIndicatorsVisible(resizeIndicatorsVisible);

    add(chatWindow, fabWrapper, mobileChatWindow);
  }

  /**
   * Parses the given margin value, falling back to {@value #DEFAULT_FAB_MARGIN} when null, negative
   * or invalid.
   */
  // Package-private (not private) so unit tests in this package can exercise the parsing fallbacks.
  int parseFabMargin(String fabMargin) {
    if (fabMargin == null) {
      return DEFAULT_FAB_MARGIN;
    }
    try {
      int margin = Integer.parseInt(fabMargin.trim().replace("px", ""));
      return margin >= 0 ? margin : DEFAULT_FAB_MARGIN;
    } catch (NumberFormatException e) {
      return DEFAULT_FAB_MARGIN;
    }
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI ui = attachEvent.getUI();
    Object registered = ComponentUtil.getData(ui, UI_INSTANCE_KEY);
    // Reject a different instance (a second per UI is unsupported); allow this one to re-attach.
    if (registered != null && registered != this) {
      LOGGER.warn(
          "Another ChatAssistant is already attached to this UI; only one is supported per UI."
              + " This instance will stay inactive and hidden.");
      setVisible(false);
      return;
    }
    setVisible(true);
    ComponentUtil.setData(ui, UI_INSTANCE_KEY, this);
    addComponentRefreshedListener(
        "fc-chat-assistant-drag-listener",
        "window.fcChatAssistantMovement($0, $1, $2, $3, $4, $5);",
        this.getElement(),
        fabWrapper.getElement(),
        fab.getElement(),
        fabMargin,
        DEFAULT_DRAG_SENSITIVITY,
        fabPosition.name());

    if (mobileModeSwitchingEnabled) {
      addComponentRefreshedListener(
          "fc-chat-assistant-mobile-listener",
          "window.fcChatAssistantMobileMode($0, $1);",
          this.getElement(),
          mobileBreakpoint);
    }
    // (Re)establish any screen-size observers registered before attach (and after a reattach). They
    // are also re-applied when the popover opens, since the overlay's content is rebuilt each time.
    screenSizeListeners.keySet().forEach(this::applyScreenSizeListener);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    // Release the per-UI slot so the UI can be reused or a replacement instance attached. Only
    // clear it
    // when it still points at this instance, to avoid clobbering another instance's registration.
    UI ui = detachEvent.getUI();
    if (ComponentUtil.getData(ui, UI_INSTANCE_KEY) == this) {
      ComponentUtil.setData(ui, UI_INSTANCE_KEY, null);
    }
    super.onDetach(detachEvent);
  }

  /** Receives mobile mode changes from the client when the viewport crosses the breakpoint. */
  @ClientCallable
  protected void onMobileModeChange(boolean mobile) {
    if (mobileModeSwitchingEnabled) {
      setMode(mobile ? ChatAssistantMode.MOBILE : ChatAssistantMode.DESKTOP, true);
    }
  }

  /**
   * Receives chat-window size threshold crossings from the client and dispatches to the matching
   * listener.
   */
  @ClientCallable
  protected void onScreenSizeChange(int key, boolean matches) {
    ScreenSizeListenerEntry entry = screenSizeListeners.get(key);
    if (entry == null) {
      // The listener was removed between client setup and this callback; ignore.
      return;
    }
    entry.listener.onComponentEvent(
        new ScreenSizeEvent(this, true, entry.width, entry.height, matches));
  }

  /** Toggles the chat window's opened state. Called from the client on FAB click. */
  @ClientCallable
  protected void onClick() {
    if (isOpened()) {
      close();
    } else {
      open();
    }
  }

  /** Applies common styles to the resizer elements based on the specified direction. */
  private void applyGenericResizerStyle(Div resizer, String direction) {
    resizer
        .getStyle()
        .setPosition(Style.Position.ABSOLUTE)
        .setDisplay(Style.Display.INLINE_BLOCK)
        .setZIndex(1001);
    setResizerClass(resizer, direction, DEFAULT_WINDOW_RESIZABLE);
    // A subtle arrowhead pointing in this resizer's drag direction. Hidden until the feature is
    // enabled (setResizeIndicatorsVisible) and only shown while the resizer is actually draggable
    // (the resize script toggles a class for that); see fc-chat-assistant-style.css.
    Div arrow = new Div();
    arrow.addClassName(DEFAULT_RESIZE_CLASS + "-arrow");
    arrow.addClassName(DEFAULT_RESIZE_CLASS + "-arrow-" + direction);
    resizer.add(arrow);
  }

  /** Positions and sizes a resize handle against the edge(s) named by its direction. */
  private void applyResizerEdgeStyle(Div resizer, String direction) {
    String edge = DEFAULT_RESIZER_SIZE + "px";
    switch (direction) {
      case "top" -> resizer.getStyle().setTop("0").setLeft("0").setHeight(edge).setWidth("100%");
      case "bottom" ->
          resizer.getStyle().setBottom("0").setLeft("0").setHeight(edge).setWidth("100%");
      case "right" ->
          resizer.getStyle().setTop("0").setRight("0").setHeight("100%").setWidth(edge);
      case "left" -> resizer.getStyle().setTop("0").setLeft("0").setHeight("100%").setWidth(edge);
      case "top-right" ->
          resizer.getStyle().setTop("0").setRight("0").setHeight(edge).setWidth(edge);
      case "bottom-right" ->
          resizer.getStyle().setBottom("0").setRight("0").setHeight(edge).setWidth(edge);
      case "top-left" -> resizer.getStyle().setTop("0").setLeft("0").setHeight(edge).setWidth(edge);
      case "bottom-left" ->
          resizer.getStyle().setBottom("0").setLeft("0").setHeight(edge).setWidth(edge);
      default -> throw new IllegalArgumentException("Unknown resizer direction: " + direction);
    }
  }

  private void setResizerClass(Div resizer, String direction, boolean resizable) {
    String classname = DEFAULT_RESIZE_CLASS + "-" + direction;
    if (resizable && !resizer.getClassNames().contains(classname)) {
      resizer.addClassName(classname);
    } else if (!resizable && resizer.getClassNames().contains(classname)) {
      resizer.removeClassName(classname);
    }
  }

  /**
   * Runs the given JavaScript once per component instance, using a flag on the element to avoid
   * registering duplicate client-side listeners across refreshes.
   *
   * @param uniqueFlag a unique identifier for this registration
   * @param executable the JavaScript to execute
   * @param parameters parameters for the executable
   */
  protected void addComponentRefreshedListener(
      String uniqueFlag, String executable, Serializable... parameters) {
    this.getElement()
        .executeJs(
            String.format(
                """
            if(!this['%1$s']) { %2$s }
            if(!this['%1$s']) {
              this['%1$s'] = '%1$s';
            };
            """,
                uniqueFlag, executable),
            parameters);
  }

  /**
   * Creates the default chatbot icon. The SVG is read from the classpath and inlined as a data URI,
   * so it does not depend on a statically served path: it works both in the demo and when the
   * add-on is packaged as a jar, and (unlike a StreamResource) needs no UI/session, keeping the
   * icon serializable.
   */
  private static SvgIcon createDefaultFabIcon() {
    return new SvgIcon(getDefaultFabIconSrc());
  }

  /**
   * Returns the bundled chatbot data URI, loading it lazily on first use and caching the result.
   * Loading is deferred (not done in a static initializer) and degrades gracefully so that a missing
   * or unreadable icon resource cannot fail class initialization for callers that supply their own
   * FAB icon.
   */
  private static String getDefaultFabIconSrc() {
    String src = defaultFabIconSrc;
    if (src == null) {
      src = loadDefaultFabIconSrc();
      defaultFabIconSrc = src;
    }
    return src;
  }

  /**
   * Loads the bundled chatbot SVG from the classpath and encodes it as a data URI, returning an
   * empty string (no icon) if the resource is missing or cannot be read.
   */
  private static String loadDefaultFabIconSrc() {
    try (InputStream in =
        ChatAssistant.class.getResourceAsStream("/META-INF/resources/icons/chatbot.svg")) {
      if (in == null) {
        LOGGER.warn(
            "Default chatbot icon not found on the classpath"
                + " (/META-INF/resources/icons/chatbot.svg); the FAB will have no default icon.");
        return "";
      }
      byte[] svg = in.readAllBytes();
      return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg);
    } catch (IOException e) {
      LOGGER.warn("Could not load the default chatbot icon; the FAB will have no default icon.", e);
      return "";
    }
  }

  /**
   * Sets the icon for the floating action button. The icon's size is automatically adjusted to fit
   * within the current FAB size.
   *
   * @param icon the icon component, it cannot be null
   */
  public void setFabIcon(Component icon) {
    setFabIcon(icon, getFabIconSize());
  }

  /**
   * Sets the icon for the floating action button with a custom size. The size is capped at the
   * current FAB size.
   *
   * @param icon the icon component, it cannot be null
   * @param size the icon size in pixels, it must be greater than 0
   */
  public void setFabIcon(Component icon, int size) {
    Objects.requireNonNull(icon, "Icon cannot be null");
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    this.fabIcon = icon;
    fab.setIcon(icon);
    applyIconSize(icon, Math.min(size, fabSize));
  }

  /**
   * Sets the FAB diameter in pixels, scaling the icon to match. This is the single sizing entry
   * point; the theme-variant API uses it to apply the {@link FabVariant#SMALL}/{@link
   * FabVariant#LARGE} sizes.
   *
   * @param size the FAB diameter in pixels, it must be greater than 0
   */
  private void setFabSize(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    this.fabSize = size;
    fab.getStyle()
        .setMinHeight(size + "px")
        .setMinWidth(size + "px")
        .setHeight(size + "px")
        .setWidth(size + "px")
        .setMaxHeight(size + "px")
        .setMaxWidth(size + "px");
    fabWrapper.getStyle().setHeight(size + "px").setWidth(size + "px");
    if (fabIcon != null) {
      applyIconSize(fabIcon, getFabIconSize());
    }
  }

  /** The icon size that fits the current FAB size (at least 1px so it is always a valid size). */
  private int getFabIconSize() {
    return Math.max(1, fabSize - 20);
  }

  /**
   * Pins a deterministic pixel size on the FAB icon, regardless of its concrete component type.
   * Both width/height and min/max are set so the size is exact: the default chatbot SVG declares an
   * intrinsic size and a {@code vaadin-icon} carries a Lumo {@code em}-based size, either of which
   * would otherwise leak through and make the rendered size depend on prior state.
   */
  private void applyIconSize(Component icon, int px) {
    icon.getStyle()
        .setWidth(px + "px")
        .setHeight(px + "px")
        .setMinWidth(px + "px")
        .setMinHeight(px + "px")
        .setMaxWidth(px + "px")
        .setMaxHeight(px + "px");
  }

  /**
   * Adds the given theme variants to the FAB. Color variants are applied to the underlying button
   * and accumulate; the size variants {@link FabVariant#SMALL} and {@link FabVariant#LARGE} instead
   * resize the FAB (and its icon) to a predefined diameter and are mutually exclusive, so if both
   * are added the last one wins.
   *
   * @since 5.1.0
   * @param variants the variants to add
   */
  public void addFabThemeVariants(FabVariant... variants) {
    for (FabVariant variant : variants) {
      if (variant == FabVariant.SMALL || variant == FabVariant.LARGE) {
        activeSizeVariant = variant;
        setFabSize(variant == FabVariant.LARGE ? DEFAULT_FAB_LARGE_SIZE : DEFAULT_FAB_SMALL_SIZE);
      } else {
        applyFabColorVariant(variant);
      }
    }
  }

  /**
   * Removes the given theme variants from the FAB. Removing the currently active size variant
   * ({@link FabVariant#SMALL}/{@link FabVariant#LARGE}) resets the FAB to its default size.
   *
   * @since 5.1.0
   * @param variants the variants to remove
   */
  public void removeFabThemeVariants(FabVariant... variants) {
    for (FabVariant variant : variants) {
      if (variant == FabVariant.SMALL || variant == FabVariant.LARGE) {
        if (variant == activeSizeVariant) {
          activeSizeVariant = null;
          setFabSize(DEFAULT_FAB_SIZE);
        }
      } else {
        removeFabColorVariant(variant);
      }
    }
  }

  private void applyFabColorVariant(FabVariant variant) {
    fab.addThemeVariants(variant.getButtonVariant());
    // Aura styles its accent colors via a CSS class rather than the theme attribute, so add it too
    // when present; the color then renders under both Lumo and Aura.
    if (variant.getAuraClass() != null) {
      fab.getElement().getClassList().add(variant.getAuraClass());
    }
  }

  private void removeFabColorVariant(FabVariant variant) {
    fab.removeThemeVariants(variant.getButtonVariant());
    if (variant.getAuraClass() != null) {
      fab.getElement().getClassList().remove(variant.getAuraClass());
    }
  }

  /** Sets the opened state of the chat window. If true, opens the window; if false, closes it. */
  public void setOpened(boolean opened) {
    if (opened) {
      open();
    } else {
      close();
    }
  }

  /** Opens the chat window. */
  public void open() {
    if (isMobile()) {
      mobileChatWindow.open();
    } else {
      chatWindow.open();
    }
  }

  /** Closes the chat window. */
  public void close() {
    if (isMobile()) {
      mobileChatWindow.close();
    } else {
      chatWindow.close();
    }
  }

  /** Returns true if the chat window is opened, false otherwise. */
  public boolean isOpened() {
    return isMobile() ? mobileChatWindow.isOpened() : chatWindow.isOpened();
  }

  /** Returns true if the component is currently in {@link ChatAssistantMode#MOBILE} mode. */
  private boolean isMobile() {
    return this.mode == ChatAssistantMode.MOBILE;
  }

  /**
   * Sets whether the chat window is resizable.
   *
   * @since 5.1.0
   * @param resizable whether the chat window can be resized
   */
  public void setWindowResizable(boolean resizable) {
    this.resizable = resizable;
    if (resizable) {
      overlay.getElement().setAttribute("resizable", true);
    } else {
      overlay.getElement().removeAttribute("resizable");
    }
    resizers.forEach((direction, resizer) -> setResizerClass(resizer, direction, resizable));
  }

  /**
   * Returns whether the chat window is resizable.
   *
   * @since 5.1.0
   * @return {@code true} if the chat window is resizable
   */
  public boolean isWindowResizable() {
    return resizable;
  }

  /**
   * Sets whether a small arrowhead is shown on each resize handle, pointing in that handle's resize
   * direction, to hint where the chat window can be dragged. The indicators are subtle, hidden by
   * default, and only shown on the handles that can currently be dragged given the window's position.
   *
   * @since 5.1.0
   * @param visible whether the resize direction indicators are visible
   */
  public void setResizeIndicatorsVisible(boolean visible) {
    this.resizeIndicatorsVisible = visible;
    overlay.getElement().getClassList().set(RESIZE_INDICATOR_VISIBLE_CLASS, visible);
  }

  /**
   * Returns true if the resize direction indicators are visible, false otherwise.
   *
   * @since 5.1.0
   */
  public boolean isResizeIndicatorsVisible() {
    return resizeIndicatorsVisible;
  }

  /**
   * Sets whether the FAB is movable. In {@link ChatAssistantMode#DESKTOP} mode this also becomes
   * the preference restored when returning from {@link ChatAssistantMode#MOBILE} mode (which always
   * forces the FAB non-movable). Has no effect while the FAB is not anchored to the viewport (see
   * {@link #setFabAnchoredToViewport(boolean)}), since a container-positioned FAB is never draggable.
   *
   * @since 5.1.0
   * @param movable whether the FAB can be dragged
   */
  public void setFabMovable(boolean movable) {
    this.fabMovable = movable;
    // Remember the user's choice as the desktop preference, but not the value forced in mobile mode.
    if (!isMobile()) {
      this.desktopFabMovablePreference = movable;
    }
    if (movable) {
      fab.getElement().setAttribute("movable", true);
    } else {
      fab.getElement().removeAttribute("movable");
    }
  }

  /**
   * Returns whether the FAB is currently movable. Only a FAB anchored to the viewport can be moved,
   * and the FAB is not movable by default while in {@link ChatAssistantMode#MOBILE} mode.
   *
   * @since 5.1.0
   * @return {@code true} if the FAB is currently movable
   */
  public boolean isFabMovable() {
    return fabMovable && fabAnchoredToViewport;
  }

  /**
   * Sets whether the FAB is anchored to the viewport. When {@code true} (the default) the FAB
   * floats over the viewport; when {@code false} it is positioned within its container, so it can
   * be placed inside a bounded element. A FAB that is not anchored to the viewport is not movable.
   *
   * @since 5.1.0
   */
  public void setFabAnchoredToViewport(boolean anchoredToViewport) {
    this.fabAnchoredToViewport = anchoredToViewport;
    fabWrapper.getStyle().setPosition(anchoredToViewport ? Position.FIXED : Position.ABSOLUTE);
    if (anchoredToViewport) {
      fab.getElement().setAttribute("anchored", true);
    } else {
      fab.getElement().removeAttribute("anchored");
    }
  }

  /**
   * Returns true if the FAB is anchored to the viewport, false if positioned within its container.
   *
   * @since 5.1.0
   */
  public boolean isFabAnchoredToViewport() {
    return fabAnchoredToViewport;
  }

  /**
   * Moves the FAB to the given corner. This also becomes the position the FAB returns to when
   * {@link #resetFabPosition()} is called.
   *
   * @since 5.1.0
   * @param fabPosition the corner to move the FAB to, it cannot be null
   */
  public void setFabPosition(FabPosition fabPosition) {
    Objects.requireNonNull(fabPosition, "Position cannot be null");
    this.fabPosition = fabPosition;
    resetFabPosition();
  }

  /**
   * Returns the FAB's configured corner.
   *
   * @since 5.1.0
   */
  public FabPosition getFabPosition() {
    return fabPosition;
  }

  /**
   * Moves the FAB back to its configured corner.
   *
   * @since 5.1.0
   */
  public void resetFabPosition() {
    this.getElement()
        .executeJs(
            "window.fcChatAssistantResetPosition($0, $1, $2);",
            fabWrapper.getElement(),
            fabMargin,
            fabPosition.name());
  }

  /**
   * Sets the chat window minimum width, the lower bound enforced while resizing.
   *
   * @param minWidth the minimum width as a CSS length (e.g. "150px")
   */
  public void setWindowMinWidth(String minWidth) {
    this.overlay.getStyle().set(CSS_MIN_WIDTH, minWidth);
    applyWindowConstraints();
  }

  /**
   * Sets the chat window minimum width, the lower bound enforced while resizing.
   *
   * @since 5.1.0
   * @param minWidth the minimum width in px (e.g. 150)
   */
  public void setWindowMinWidth(int minWidth) {
    this.overlay.getStyle().set(CSS_MIN_WIDTH, minWidth + "px");
    applyWindowConstraints();
  }

  /**
   * Sets the chat window minimum height, the lower bound enforced while resizing.
   *
   * @since 5.1.0
   * @param minHeight the minimum height in px (e.g. 150)
   */
  public void setWindowMinHeight(int minHeight) {
    this.overlay.getStyle().set(CSS_MIN_HEIGHT, minHeight + "px");
    applyWindowConstraints();
  }

  /**
   * Sets the chat window minimum height, the lower bound enforced while resizing.
   *
   * @param minHeight the minimum height as a CSS length (e.g. "150px")
   */
  public void setWindowMinHeight(String minHeight) {
    this.overlay.getStyle().set(CSS_MIN_HEIGHT, minHeight);
    applyWindowConstraints();
  }

  /**
   * Sets the chat window maximum width, the upper bound enforced while resizing.
   *
   * @param maxWidth the maximum width as a CSS length
   */
  public void setWindowMaxWidth(String maxWidth) {
    this.overlay.getStyle().set(CSS_MAX_WIDTH, maxWidth);
    applyWindowConstraints();
  }

  /**
   * Sets the chat window maximum width, the upper bound enforced while resizing.
   *
   * @since 5.1.0
   * @param maxWidth the maximum width in px (e.g. 150)
   */
  public void setWindowMaxWidth(int maxWidth) {
    this.overlay.getStyle().set(CSS_MAX_WIDTH, maxWidth + "px");
    applyWindowConstraints();
  }

  /**
   * Sets the chat window maximum height, the upper bound enforced while resizing.
   *
   * @param maxHeight the maximum height as a CSS length
   */
  public void setWindowMaxHeight(String maxHeight) {
    this.overlay.getStyle().set(CSS_MAX_HEIGHT, maxHeight);
    applyWindowConstraints();
  }

  /**
   * Sets the chat window maximum height, the upper bound enforced while resizing.
   *
   * @since 5.1.0
   * @param maxHeight the maximum height in px (e.g. 150)
   */
  public void setWindowMaxHeight(int maxHeight) {
    this.overlay.getStyle().set(CSS_MAX_HEIGHT, maxHeight + "px");
    applyWindowConstraints();
  }

  /**
   * Sets the chat window's initial height. Prefer absolute or viewport-relative units (e.g. "400px",
   * "60vh"); a percentage resolves against the popover, not the viewport, and may not behave as
   * expected.
   *
   * @param height the height as a CSS length
   */
  public void setWindowHeight(String height) {
    applyWindowSize("height", height);
  }

  /**
   * Sets the chat window's initial height. Use absolute units (e.g. 400).
   *
   * @since 5.1.0
   * @param height the height in px (e.g. 400)
   */
  public void setWindowHeight(int height) {
    applyWindowSize("height", height + "px");
  }

  /**
   * Sets the chat window's initial width. Prefer absolute or viewport-relative units (e.g. "400px",
   * "40vw"); a percentage resolves against the popover, not the viewport, and may not behave as
   * expected.
   *
   * @param width the width as a CSS length
   */
  public void setWindowWidth(String width) {
    applyWindowSize("width", width);
  }

  /**
   * Sets the chat window's initial width. Use absolute units (e.g. 400).
   *
   * @since 5.1.0
   * @param width the width in px (e.g. 400)
   */
  public void setWindowWidth(int width) {
    applyWindowSize("width", width + "px");
  }

  /** Sizes the popover content part (works on Vaadin 24 and 25). */
  private void applyWindowSize(String dimension, String value) {
    if (value == null) {
      return;
    }
    // Persist the desired size on the durable overlay Div (as a custom property that survives
    // close/reopen); the actual content part is sized by applyWindowConstraints on open.
    overlay.getStyle().set("height".equals(dimension) ? CSS_HEIGHT : CSS_WIDTH, value);
    applyWindowConstraints();
  }

  /**
   * Re-applies the configured size and min/max bounds to the popover content part, reading them from
   * the {@code --fc-*} custom properties on the durable overlay Div. Only pushes to the client while
   * the popover is open; when it is closed the properties are applied on the next open (see the
   * open listener in {@link #initializeChatWindow()}), so calling this at construction time does not
   * spin a client-side poll against a content part that does not exist yet.
   */
  private void applyWindowConstraints() {
    if (!chatWindow.isOpened()) {
      return;
    }
    this.getElement()
        .executeJs("window.fcChatAssistantApplyConstraints($0, $1);", overlay, DEFAULT_POPOVER_TAG);
  }

  protected void initializeHeader() {
    Icon minimize = VaadinIcon.CLOSE.create();
    minimize.addClickListener((ev) -> onClick());
    Span title = new Span("Chat Assistant");
    title.setWidthFull();
    HorizontalLayout header = new HorizontalLayout(title, minimize);
    header.setWidthFull();
    this.headerComponent = header;
  }

  @SuppressWarnings("unchecked")
  protected void initializeFooter() {
    this.messageInput = new MessageInput();
    this.messageInput
        .getStyle()
        .setMaxHeight("80px")
        .set("width", "100%")
        .setPadding("0 2px"); // Account for border when focused (it will get cropped otherwise)

    this.defaultSubmitListenerRegistration =
        this.messageInput.addSubmitListener(
            (se) ->
                this.sendMessage(
                    (T)
                        Message.builder()
                            .messageTime(LocalDateTime.now())
                            .name("User")
                            .content(se.getValue())
                            .build()));
    this.whoIsTyping = new Span();
    this.whoIsTyping.setClassName("chat-assistant-who-is-typing");
    this.whoIsTyping.setVisible(false);

    VerticalLayout footer = new VerticalLayout(this.whoIsTyping, this.messageInput);
    footer.setWidthFull();
    footer.setSpacing(false);
    footer.setMargin(false);
    footer.setPadding(false);

    this.footerContainer = footer;
  }

  @SuppressWarnings("unchecked")
  protected void initializeContent(boolean markdownEnabled) {
    this.content.setRenderer(
        new ComponentRenderer<>(
            message -> new ChatMessage<>(message, markdownEnabled),
            (component, message) -> {
              ((ChatMessage<T>) component).setMessage(message);
              return component;
            }));
    this.content.setItems(this.messages);
    // Allow the content to shrink below its intrinsic size so the popover clamp produces an internal
    // scroll instead of overflowing (the standard flexbox min-content fix).
    this.content.getStyle().set("flex", "1").setMinHeight("0");
    this.container.add(this.headerComponent, this.content, this.footerContainer);
    this.container.setPadding(true);
    this.container.setMargin(false);
    this.container.setSpacing(false);
    this.container.setSizeFull();
    this.container
        .getStyle()
        .set("flex", "1")
        .setHeight(null)
        // Allow the column to shrink below its min-content height so the configured/min window
        // height wins and the message list scrolls internally instead of forcing the window taller.
        .set("min-height", "0")
        .setAlignItems(AlignItems.STRETCH)
        .setAlignSelf(AlignSelf.STRETCH);
  }

  protected void initializeChatWindow() {
    this.chatWindow.setOpenOnClick(false);
    this.chatWindow.setCloseOnOutsideClick(false);
    this.chatWindow.addOpenedChangeListener(
        ev -> {
          if (ev.isOpened()) {
            // The overlay (and its content part) is recreated on each open, so re-apply the last
            // size (the configured initial size or whatever the user resized to), stored on the
            // overlay Div.
            this.getElement()
                .executeJs(
                    "window.fcChatAssistantRestoreWindowSize($0, $1);",
                    overlay,
                    DEFAULT_POPOVER_TAG);
            // Re-establish the chat-window size observers against the freshly laid-out overlay and
            // re-deliver the current state for this open.
            screenSizeListeners.keySet().forEach(this::applyScreenSizeListener);
            resizers.forEach(
                (direction, resizer) ->
                    addComponentRefreshedListener(
                        "fc-chat-assistant-resize-" + direction + "-listener",
                        "window.fcChatAssistantResize($0, $1, $2, $3, $4, $5, '" + direction + "');",
                        this.getElement(),
                        resizer.getElement(),
                        overlay,
                        DEFAULT_POPOVER_TAG,
                        DEFAULT_RESIZER_SIZE,
                        DEFAULT_MAX_RESIZER_SIZE));
          }
        });
  }

  public void setDataProvider(DataProvider<T, ?> dataProvider) {
    this.content.setDataProvider(dataProvider);
  }

  /**
   * Uses the provided string as the text shown over the message input to indicate that someone is
   * typing.
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

  /** Clears the text shown over the message input to indicate that someone is typing. */
  public void clearWhoIsTyping() {
    this.whoIsTyping.setText(null);
    this.whoIsTyping.setVisible(false);
  }

  /**
   * Sets the SubmitListener that will be notified when the user submits a message on the underlying
   * messageInput.
   *
   * @param listener the listener that will be notified when the SubmitEvent is fired
   * @return registration for removal of the listener
   */
  public Registration setSubmitListener(ComponentEventListener<MessageInput.SubmitEvent> listener) {
    if (this.defaultSubmitListenerRegistration != null) {
      this.defaultSubmitListenerRegistration.remove();
    }
    this.defaultSubmitListenerRegistration = this.messageInput.addSubmitListener(listener);
    return this.defaultSubmitListenerRegistration;
  }

  public void refreshContent() {
    this.content.getDataProvider().refreshAll();
    this.content.scrollToEnd();
  }

  /**
   * Sends a message programmatically to the component. Should not be used when a custom
   * DataProvider is used. Instead, just refresh the custom DataProvider.
   *
   * @param message the message to be sent programmatically
   */
  public void sendMessage(T message) {
    this.messages.add(message);
    refreshContent();
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
   * @deprecated use {@link #setOpened(boolean)} instead
   */
  @Deprecated(since = "5.0.0")
  public void setMinimized(boolean minimized) {
    if (minimized && isOpened()) {
      close();
    } else if (!minimized && !isOpened()) {
      open();
    }
  }

  /**
   * Returns the visibility of the chat window.
   *
   * @return true if the chat window is minimized false otherwise
   * @deprecated use {@link #isOpened()} instead
   */
  @Deprecated(since = "5.0.0")
  public boolean isMinimized() {
    return !isOpened();
  }

  /**
   * Allows changing the header of the chat window.
   *
   * @param component to be used as a replacement for the header
   */
  public void setHeaderComponent(Component component) {
    if (this.headerComponent != null) {
      this.container.remove(this.headerComponent);
    }

    component.addClassName("chat-header");
    this.headerComponent = component;
    this.container.addComponentAsFirst(this.headerComponent);
  }

  /** Returns the current component configured as the header of the chat window. */
  public Component getHeaderComponent() {
    return this.headerComponent;
  }

  /**
   * Allows changing the footer of the chat window.
   *
   * @param component to be used as a replacement for the footer, it cannot be null
   */
  public void setFooterComponent(Component component) {
    Objects.requireNonNull(component, "Component cannot be null");
    this.container.remove(this.footerContainer);
    this.footerContainer = component;
    this.container.add(this.footerContainer);
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
   * Scrolls to the given position. Scrolls so that the element is shown at the start of the visible
   * area whenever possible.
   *
   * <p>If the index parameter exceeds current item set size the grid will scroll to the end.
   *
   * @param position zero based index of the item to scroll to in the current view.
   */
  public void scrollToIndex(int position) {
    this.content.scrollToIndex(position);
  }

  /** Scrolls to the first element. */
  public void scrollToStart() {
    this.content.scrollToStart();
  }

  /** Scrolls to the last element of the list. */
  public void scrollToEnd() {
    this.content.scrollToEnd();
  }

  /**
   * Allows changing the renderer used to display messages in the chat window.
   *
   * @param renderer the renderer to use for rendering {@link Message} objects, it cannot be null
   */
  public void setMessagesRenderer(Renderer<T> renderer) {
    Objects.requireNonNull(renderer, "Renderer cannot be null");
    this.content.setRenderer(renderer);
  }

  /**
   * Sets the avatar provider that will be used to create the avatar
   *
   * @param avatarProvider the avatar provider that will be used to create the avatar
   * @deprecated use {@link #setFabIcon(Component)} instead
   */
  @Deprecated(since = "5.0.0", forRemoval = true)
  public void setAvatarProvider(SerializableSupplier<Avatar> avatarProvider) {
    Objects.requireNonNull(avatarProvider, "Avatar provider cannot be null");
    Avatar avatar = avatarProvider.get();
    if (avatar == null) {
      throw new IllegalArgumentException("Avatar provider returned null");
    }
    setFabIcon(avatar);
  }

  /**
   * Returns the number of unread messages displayed in the chat assistant.
   *
   * @return the number of unread messages
   */
  public int getUnreadMessages() {
    return Math.max(unreadMessages, 0);
  }

  /**
   * Sets the number of unread messages shown on the FAB badge. The value is clamped to the
   * 0&ndash;99 range; the badge is hidden when it is 0.
   *
   * @param unreadMessages the number of unread messages to set
   */
  public void setUnreadMessages(int unreadMessages) {
    this.unreadMessages = unreadMessages >= 0 ? Math.min(unreadMessages, 99) : 0;
    unreadBadge.setText(String.valueOf(this.unreadMessages));
    if (this.unreadMessages > 0) {
      unreadBadge.getStyle().setScale("1");
    } else {
      unreadBadge.getStyle().setScale("0");
    }
  }

  /**
   * Sets the background and text color of the unread badge. If null or empty, the default values
   * are used.
   *
   * @since 5.1.0
   * @param background the background color of the unread badge
   * @param color the text color of the unread badge
   */
  public void setUnreadBadgeColors(String background, String color) {
    if (background != null && !background.isBlank()) {
      unreadBadge.getStyle().set("background-color", background);
    } else {
      unreadBadge.getStyle().set("background-color", DEFAULT_UNREAD_BADGE_BACKGROUND);
    }
    if (color != null && !color.isBlank()) {
      unreadBadge.getStyle().set("color", color);
    } else {
      unreadBadge.getStyle().set("color", DEFAULT_UNREAD_BADGE_COLOR);
    }
  }

  /**
   * Sets the display mode programmatically. In {@link ChatAssistantMode#MOBILE} mode the chat
   * window opens as a full-screen dialog and the FAB is not movable (dragging would compete with
   * touch scrolling, and the full-screen dialog already covers the viewport); the desktop movable
   * preference is preserved and restored when switching back. In {@link ChatAssistantMode#DESKTOP}
   * mode the window opens as an anchored popover.
   *
   * <p>When automatic switching is enabled (see {@link #setMobileModeSwitchingEnabled(boolean)}),
   * this value may be overridden the next time the viewport crosses the configured breakpoint. To
   * keep full manual control, disable automatic switching first.
   *
   * @since 5.1.0
   * @param mode the mode to switch to, it cannot be null
   */
  public void setMode(ChatAssistantMode mode) {
    Objects.requireNonNull(mode, "Mode cannot be null");
    setMode(mode, false);
  }

  /**
   * Returns the current display mode.
   *
   * @since 5.1.0
   */
  public ChatAssistantMode getMode() {
    return mode;
  }

  /**
   * Applies the given mode, reconciling the active surface and open state, and fires a {@link
   * ModeChangedEvent} when the mode actually changes.
   *
   * @param mode the mode to switch to
   * @param fromClient whether the change originated from a client-side breakpoint crossing
   */
  protected void setMode(ChatAssistantMode mode, boolean fromClient) {
    if (this.mode == mode) {
      return;
    }
    boolean mobile = mode == ChatAssistantMode.MOBILE;
    // Capture the open state before switching so it can be carried over to the other surface.
    boolean wasOpened = isOpened();
    this.mode = mode;
    this.container.setPadding(!mobile);
    if (mobile) {
      if (overlay.getChildren().anyMatch(child -> child == container)) {
        overlay.remove(container);
        mobileChatWindow.add(container);
      }
      // Mobile mode always disables dragging. The desktop preference is retained in
      // desktopFabMovablePreference (setFabMovable does not overwrite it while in mobile mode) and
      // restored when leaving mobile, so isFabMovable() honestly reports false here.
      setFabMovable(false);
      // The popover would otherwise stay open with no content; move the open state to the dialog.
      if (wasOpened) {
        chatWindow.close();
        mobileChatWindow.open();
      }
    } else {
      if (mobileChatWindow.getChildren().anyMatch(child -> child == container)) {
        mobileChatWindow.remove(container);
        overlay.add(container);
      }
      setFabMovable(this.desktopFabMovablePreference);
      // Move the open state from the dialog back to the popover.
      if (wasOpened) {
        mobileChatWindow.close();
        chatWindow.open();
      }
    }
    // Return the FAB to its configured corner; its dragged position is not meaningful across modes.
    resetFabPosition();
    fireEvent(new ModeChangedEvent(this, fromClient, this.mode));
  }

  /**
   * Sets the mobile mode programmatically. Convenience wrapper over {@link
   * #setMode(ChatAssistantMode)}.
   *
   * @since 5.1.0
   * @param mobileMode {@code true} for {@link ChatAssistantMode#MOBILE}, {@code false} for {@link
   *     ChatAssistantMode#DESKTOP}
   */
  public void setMobileMode(boolean mobileMode) {
    setMode(mobileMode ? ChatAssistantMode.MOBILE : ChatAssistantMode.DESKTOP);
  }

  /**
   * Returns true if the component is currently in {@link ChatAssistantMode#MOBILE} mode.
   *
   * @since 5.1.0
   */
  public boolean isMobileMode() {
    return isMobile();
  }

  /**
   * Adds a listener that is notified whenever the component switches between {@link
   * ChatAssistantMode#MOBILE} and {@link ChatAssistantMode#DESKTOP} mode.
   *
   * @since 5.1.0
   * @param listener the listener to add; it receives the mode the component switched to
   * @return a registration for removing the listener
   */
  public Registration addModeChangedListener(ComponentEventListener<ModeChangedEvent> listener) {
    return addListener(ModeChangedEvent.class, listener);
  }

  /**
   * Event fired when the chat assistant switches between mobile and desktop mode.
   *
   * @since 5.1.0
   */
  public static class ModeChangedEvent extends ComponentEvent<ChatAssistant<?>> {

    private final ChatAssistantMode mode;

    protected ModeChangedEvent(
        ChatAssistant<?> source, boolean fromClient, ChatAssistantMode mode) {
      super(source, fromClient);
      this.mode = mode;
    }

    /** Returns the mode the component switched to. */
    public ChatAssistantMode getMode() {
      return mode;
    }
  }

  /**
   * Adds a listener that is notified when the chat window's own size crosses the given threshold.
   * At least one of {@code width}/{@code height} must be non-null; a {@code null} axis is not
   * tracked. When both are given, the listener fires only when both are simultaneously satisfied
   * (AND).
   *
   * <p>The chat window only has a size while it is open, so the listener observes size changes
   * (drag resize, {@link #setWindowWidth}/{@link #setWindowHeight}, or viewport clamping) while
   * open. On each open it is invoked once with the current state, then only when the size crosses
   * the threshold. The threshold is inclusive: a window exactly at the threshold counts as above it
   * ({@link ScreenSizeEvent#isAboveThreshold()} is {@code true}). Each listener only receives
   * events for its own threshold.
   *
   * @since 5.1.0
   * @param width the width threshold in pixels, or {@code null} to ignore width
   * @param height the height threshold in pixels, or {@code null} to ignore height
   * @param listener the listener to add
   * @return a registration for removing the listener
   */
  public Registration addScreenSizeListener(
      Integer width, Integer height, ComponentEventListener<ScreenSizeEvent> listener) {
    Objects.requireNonNull(listener, "Listener cannot be null");
    if (width == null && height == null) {
      throw new IllegalArgumentException("At least one of width or height must be provided");
    }
    if ((width != null && width <= 0) || (height != null && height <= 0)) {
      throw new IllegalArgumentException("Thresholds must be greater than 0");
    }

    int key = ++screenSizeKeySeq;
    screenSizeListeners.put(key, new ScreenSizeListenerEntry(width, height, listener));
    if (this.getUI().isPresent()) {
      applyScreenSizeListener(key);
    }

    return () -> {
      screenSizeListeners.remove(key);
      this.getElement()
          .executeJs("window.fcChatAssistantScreenSizeOff?.($0, $1);", this.getElement(), key);
    };
  }

  /**
   * (Re)registers a single chat-window size observer on the client. Called on attach and on each
   * popover/dialog open (the surface content is rebuilt each open); the JS replaces any existing
   * observer for the key and re-delivers the current state, so it is safe to call repeatedly.
   *
   * <p>The observed element is {@link #container}, the content surface that is moved between the
   * desktop popover ({@link #overlay}) and the mobile dialog ({@link #mobileChatWindow}). Observing
   * it (rather than {@code overlay}) keeps the listener working in both modes: the mobile dialog
   * host has no size of its own, and a {@code ResizeObserver} follows the element across DOM moves.
   */
  private void applyScreenSizeListener(Integer key) {
    ScreenSizeListenerEntry entry = screenSizeListeners.get(key);
    if (entry == null) {
      return;
    }
    this.getElement()
        .executeJs(
            "window.fcChatAssistantScreenSize($0, $1, $2, $3, $4);",
            this.getElement(),
            container,
            key,
            entry.width,
            entry.height);
  }

  /** Per-key bookkeeping for a screen-size listener: its thresholds and the listener to invoke. */
  private static final class ScreenSizeListenerEntry implements Serializable {
    private final Integer width;
    private final Integer height;
    private final ComponentEventListener<ScreenSizeEvent> listener;

    ScreenSizeListenerEntry(
        Integer width, Integer height, ComponentEventListener<ScreenSizeEvent> listener) {
      this.width = width;
      this.height = height;
      this.listener = listener;
    }
  }

  /**
   * Direction in which the chat window crossed a configured threshold.
   *
   * @since 5.1.0
   */
  public enum ScreenSizeDirection {
    /** The chat window grew to or past the threshold (it is now at or above it). */
    INCREASED,
    /** The chat window shrank below the threshold (it is now under it). */
    DECREASED
  }

  /**
   * Event fired when the chat window's size crosses a width and/or height threshold registered
   * through {@link #addScreenSizeListener(Integer, Integer, ComponentEventListener)}. It reports
   * the crossing direction and the configured threshold(s); no live size is exposed.
   *
   * @since 5.1.0
   */
  public static class ScreenSizeEvent extends ComponentEvent<ChatAssistant<?>> {

    private final Integer widthThreshold;
    private final Integer heightThreshold;
    private final boolean aboveThreshold;

    protected ScreenSizeEvent(
        ChatAssistant<?> source,
        boolean fromClient,
        Integer widthThreshold,
        Integer heightThreshold,
        boolean aboveThreshold) {
      super(source, fromClient);
      this.widthThreshold = widthThreshold;
      this.heightThreshold = heightThreshold;
      this.aboveThreshold = aboveThreshold;
    }

    /** The configured width threshold in pixels, or {@code null} if width was not tracked. */
    public Integer getWidthThreshold() {
      return widthThreshold;
    }

    /** The configured height threshold in pixels, or {@code null} if height was not tracked. */
    public Integer getHeightThreshold() {
      return heightThreshold;
    }

    /**
     * Returns {@code true} when the chat window is now at or above the threshold (it increased past
     * it), {@code false} when it is now below (it decreased under it).
     */
    public boolean isAboveThreshold() {
      return aboveThreshold;
    }

    /** Convenience view of {@link #isAboveThreshold()} as a direction. */
    public ScreenSizeDirection getDirection() {
      return aboveThreshold ? ScreenSizeDirection.INCREASED : ScreenSizeDirection.DECREASED;
    }
  }

  /**
   * Returns the maximum screen width, in pixels, below which mobile mode is activated
   * automatically.
   *
   * @since 5.1.0
   * @return the breakpoint in pixels
   */
  public int getMobileBreakpoint() {
    return mobileBreakpoint;
  }

  /**
   * Enables or disables automatic switching between mobile and desktop mode based on the configured
   * breakpoint. Automatic switching is <b>disabled by default</b>; it is enabled either by defining
   * a breakpoint in the constructor or by calling this method with {@code true}. Enable it only
   * after preparing the mobile experience (e.g. providing a way to close the full-screen dialog).
   *
   * <p>When disabled, the component is left in whatever mode it is currently in (freeze), and the
   * mode can only be changed manually via {@link #setMode(ChatAssistantMode)}. When enabled, the
   * breakpoint is evaluated against the current viewport width. If no breakpoint was configured,
   * the default ({@value #DEFAULT_MOBILE_BREAKPOINT}px) is used.
   *
   * @since 5.1.0
   * @param enabled {@code true} to switch automatically on viewport changes, {@code false} to
   *     freeze
   */
  public void setMobileModeSwitchingEnabled(boolean enabled) {
    if (this.mobileModeSwitchingEnabled == enabled) {
      return;
    }
    this.mobileModeSwitchingEnabled = enabled;
    if (enabled) {
      this.getElement()
          .executeJs(
              "window.fcChatAssistantMobileMode($0, $1);",
              this.getElement(),
              this.mobileBreakpoint);
    } else {
      this.getElement().executeJs("window.fcChatAssistantMobileModeOff($0);", this.getElement());
    }
  }

  /**
   * Returns whether automatic switching between mobile and desktop mode is enabled.
   *
   * @since 5.1.0
   * @return {@code true} if automatic switching is enabled
   */
  public boolean isMobileModeSwitchingEnabled() {
    return mobileModeSwitchingEnabled;
  }
}
