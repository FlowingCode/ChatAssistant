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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import java.time.format.DateTimeFormatter;
import lombok.EqualsAndHashCode;

/**
 * Component that wraps the vaadin-message web component and builds it based on a Message.
 *
 * @author mmlopez
 */
@SuppressWarnings("serial")
@JsModule("@vaadin/message-list/src/vaadin-message.js")
@Tag("vaadin-message")
@CssImport("./styles/chat-message-styles.css")
@EqualsAndHashCode(callSuper=false)
public class ChatMessage extends Component implements HasComponents {
  
  private Message message;
  private Div loader;
  
  public ChatMessage(Message message) {
    setMessage(message);
  }

  /**
   * Updates the component by setting the current underlying message
   * @param message
   */
  public void setMessage(Message message) {
    this.message = message;
    if (message.isLoading()) {
      loader = new Div(new Div(),new Div(), new Div(), new Div());
      loader.setClassName("lds-ellipsis");
      this.add(loader);
    } else {
      if (loader!=null) {
        this.remove(loader);
        loader = null;
      }
      this.getElement().executeJs("this.appendChild(document.createTextNode($0));", message.getContent());
    }
    if (message.getName()!=null) {
      this.setUserName(message.getName());
      if (message.getAvatar()!=null) {
        this.setUserImg(message.getAvatar());
      }
    }
    if (message.getMessageTime()!=null) {
      String formattedTime = message.getMessageTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
      this.setTime(formattedTime);
    }
  }
  
  /**
   * Returns the underlying message
   * @return
   */
  public Message getMessage() {
    return message;
  }
  
  private void setUserName(String username) {
    getElement().setAttribute("user-name", username);
  }
  
  private void setUserImg(String imageUrl) {
    getElement().setAttribute("user-img", imageUrl);
  }
  
  private void setTime(String time) {
    getElement().setAttribute("time", time);
  }
  
}
