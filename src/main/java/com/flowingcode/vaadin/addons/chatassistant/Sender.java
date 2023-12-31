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

import java.util.Optional;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents a chat message sender:
 *
 * <ul>
 *   <li>name: The name of the sender.
 *   <li>id: a special id to identify the sender.
 *   <li>avatar: an image that represents the sender.
 * </ul>
 *
 * @author mmlopez
 */
@Getter
@Setter
@Builder
public class Sender {

  private String name;
  private String id;
  private String avatar;
  
  public JsonValue getJsonObject() {
    JsonObject result = Json.createObject();
    Optional.ofNullable(name).ifPresent(value->result.put("name", name));
    Optional.ofNullable(id).ifPresent(value->result.put("id", id));
    Optional.ofNullable(avatar).ifPresent(value->result.put("avatar", avatar));
    return result;
  }
  
}
