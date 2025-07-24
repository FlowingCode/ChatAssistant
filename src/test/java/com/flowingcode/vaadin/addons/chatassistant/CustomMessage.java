package com.flowingcode.vaadin.addons.chatassistant;

import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
public class CustomMessage extends Message {
  
  private String tagline;
  
}
