package com.flowingcode.vaadin.addons.chatassistant;

import com.vaadin.flow.component.html.Span;

@SuppressWarnings("serial")
public class CustomChatMessage extends ChatMessage<CustomMessage> {
  
  private Span tagline = new Span();

  public CustomChatMessage(CustomMessage message) {
    super(message);
    tagline.setText(message.getTagline());
    tagline.getStyle().set("display", "block");
    tagline.getStyle().set("font-size", "x-small");
    this.add(tagline);
  }
  
  

}
