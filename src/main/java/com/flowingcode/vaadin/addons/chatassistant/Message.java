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

import elemental.json.Json;
import elemental.json.JsonObject;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
@Builder
public class Message {

  @Builder.Default
  private UUID id = UUID.randomUUID();
  @Builder.Default
  private String content = "";
  private boolean continued;
  private boolean right;
  @Builder.Default
  private Integer delay = 0;
  private boolean loading;
  private Sender sender;
  private LocalDateTime messageTime;
  
  public JsonObject getJsonObject() {
    JsonObject result = Json.createObject();
    result.put("message", content);
    result.put("continued", continued);
    result.put("right", right);
    Optional.ofNullable(delay).ifPresent(value->result.put("delay", delay));
    result.put("loading", loading);
    Optional.ofNullable(sender).ifPresent(value->result.put("sender", sender.getJsonObject()));
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Message other = (Message) obj;
    return Objects.equals(id, other.id);
  }

}
