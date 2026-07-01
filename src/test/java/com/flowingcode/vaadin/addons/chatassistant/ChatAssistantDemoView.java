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

import com.flowingcode.vaadin.addons.DemoLayout;
import com.flowingcode.vaadin.addons.GithubLink;
import com.flowingcode.vaadin.addons.demo.TabbedDemo;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

@ParentLayout(DemoLayout.class)
@Route("chat-assistant")
@GithubLink("https://github.com/FlowingCode/ChatAssistant")
public class ChatAssistantDemoView extends TabbedDemo {

  public ChatAssistantDemoView() {
    // Core usage and FAB/window configuration.
    addDemo(ChatAssistantDemo.class);
    addDemo(ChatAssistantFabConfigDemo.class);
    addDemo(ChatAssistantBoxDemo.class);
    addDemo(ChatAssistantModeDemo.class);

    // Content and data-handling features.
    addDemo(ChatAssistantLazyLoadingDemo.class);
    addDemo(ChatAssistantMarkdownDemo.class);
    addDemo(ChatAssistantGenerativeDemo.class);
    setSizeFull();
  }
}
