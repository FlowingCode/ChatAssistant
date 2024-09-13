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
package com.flowingcode.vaadin.addons.chatassistant.it;

import com.flowingcode.vaadin.addons.chatassistant.it.po.ChatAssistantElement;
import com.flowingcode.vaadin.addons.chatassistant.it.po.ChatBubbleElement;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.ParagraphElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

public class BasicIT extends AbstractViewTest {

  @Test
  public void sendMessageFromUser() {
    ChatAssistantElement element = $(ChatAssistantElement.class).first();
    TestBenchElement input = element.$(TestBenchElement.class).id("msg-input");
    input.sendKeys("hello");
    TestBenchElement button = element.$(NativeButtonElement.class).get(1);
    button.click();
    String notificationMessage = $(NotificationElement.class).waitForFirst().getText();
    Assert.assertEquals("hello", notificationMessage);
  }
  
  @Test
  public void sendMessageFromAssistant() {
    ChatAssistantElement element = $(ChatAssistantElement.class).first();
    TextAreaElement ta = $(TextAreaElement.class).first();
    ta.setValue("What can I do for you?");
    ButtonElement chatButton = $(ButtonElement.class).first();
    chatButton.click();
    ChatBubbleElement cb = element.$(ChatBubbleElement.class).get(1);
    String chat = cb.getText();
    Assert.assertEquals("What can I do for you?", chat);
  }
}
