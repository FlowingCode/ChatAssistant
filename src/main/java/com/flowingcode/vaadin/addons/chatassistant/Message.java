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

/**
 * Class that represents a chat message. It contains several configurations to control the
 * appearance of the message, such as:
 *
 * <ul>
 *   <li>continued: when true, the message is continued with the previous one.
 *   <li>right: when true, the message is aligned to the right.
 *   <li>delay: it can show some delay before being sent.
 *   <li>loading: it can show a loading image before showing the message.
 *   <li>sender: it allows to specify the sender.
 * </ul>
 *
 * @author mmlopez
 */
public class Message {

  private String content;
  private boolean continued;
  private boolean right;
  private Integer delay = 0;
  private boolean loading;
  private Sender sender;

  public Message(String content) {
    this(
        content,
        false,
        true,
        0,
        false,
        new Sender("Guest", "2", "https://ui-avatars.com/api/?name=Guest"));
  }

  public Message(
      String content,
      Boolean continued,
      Boolean right,
      Integer delay,
      Boolean loading,
      Sender sender) {
    super();
    this.content = content;
    this.continued = continued;
    this.right = right;
    this.delay = delay;
    this.loading = loading;
    this.sender = sender;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isContinued() {
    return continued;
  }

  public void setContinued(boolean continued) {
    this.continued = continued;
  }

  public boolean isRight() {
    return right;
  }

  public void setRight(boolean right) {
    this.right = right;
  }

  public Integer getDelay() {
    return delay;
  }

  public void setDelay(Integer delay) {
    this.delay = delay;
  }

  public boolean isLoading() {
    return loading;
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
  }

  public Sender getSender() {
    return sender;
  }

  public void setSender(Sender sender) {
    this.sender = sender;
  }
}
