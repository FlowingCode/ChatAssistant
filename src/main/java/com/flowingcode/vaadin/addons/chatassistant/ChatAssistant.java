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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

/**
 * Component that allows to create a floating chat button that will open a chat window that can be
 * used to provide a chat assistant feature.
 *
 * @author mmlopez
 */
@SuppressWarnings("serial")
@NpmPackage(value = "wc-chatbot", version = "0.1.1")
@JsModule("wc-chatbot/dist/wc-chatbot.js")
@CssImport("./styles/chat-assistant-styles.css")
@Tag("chat-bot")
public class ChatAssistant extends Div {

  /**
   * Sends a message represented by the string message programmatically to the component, with
   * default settings.
   *
   * @param message
   */
  public void sendMessage(String message) {
    sendMessage(new Message(message));
  }

  /** Shows or hides the chat window */
  public void toggle() {
    getElement().executeJs("setTimeout(() => {this.toggle();})");
  }

  /**
   * Sends a message to the component, by using the supplied Message object.
   *
   * @param message
   */
  public void sendMessage(Message message) {
    getElement()
        .executeJs(
            "setTimeout(() => { this.sendMessage('"
                + message.getContent()
                + "', {\n"
                + "  continued: "
                + message.isContinued()
                + ",\n"
                + "  right: "
                + message.isRight()
                + ",\n"
                + "  delay: "
                + message.getDelay()
                + ",\n"
                + "  loading: "
                + message.isLoading()
                + ",\n"
                + "  sender: {\n"
                + "    name: '"
                + message.getSender().getName()
                + "',\n"
                + "    id: '"
                + message.getSender().getId()
                + "',\n"
                + "    avatar: '"
                + message.getSender().getAvatar()
                + "'\n"
                + "  }\n"
                + "}) });");
  }

  /**
   * Adds a listener that will be notified when the ChatSentEvent is fired, allowing to react when a
   * message is sent by the user or programmatically.
   *
   * @param listener
   * @return Registration object to allow removing the listener
   */
  public Registration addChatSentListener(ComponentEventListener<ChatSentEvent> listener) {
    return addListener(ChatSentEvent.class, listener);
  }

  /**
   * Event that represents a chat being sent to the component.
   *
   * @author mmlopez
   */
  @DomEvent("sent")
  public static class ChatSentEvent extends ComponentEvent<ChatAssistant> {
    private final String message;
    private boolean right;

    public ChatSentEvent(
        ChatAssistant source,
        boolean fromClient,
        @EventData("event.detail.message.message") String message,
        @EventData("event.detail.message.right") boolean right) {
      super(source, fromClient);
      this.message = message.replaceAll("^<[^>]+>|<[^>]+>$", "");
      this.right = right;
    }

    public String getMessage() {
      return message;
    }

    public boolean isRight() {
      return right;
    }
  }
}
