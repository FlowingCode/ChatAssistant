[![Published on Vaadin Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/chat-assistant)
[![Stars on vaadin.com/directory](https://img.shields.io/vaadin-directory/star/app-layout-addon.svg)](https://vaadin.com/directory/component/chat-assistant)
[![Build Status](https://jenkins.flowingcode.com/job/ChatAssistant-addon/badge/icon)](https://jenkins.flowingcode.com/job/ChatAssistant-addon)

# Chat Assistant Add-on

Vaadin Add-on that displays a chat assistant floating window using [wc-chatbot](https://github.com/yishiashia/wc-chatbot).

## Features

* Messages can be sent by the user or programmatically.
* Listen for new messages written by the user.
* Toggle the chat window on/off.

## Online demo

[Online demo here](http://addonsv24.flowingcode.com/chat-assistant)

## Download release

[Available in Vaadin Directory](https://vaadin.com/directory/component/chat-assistant)

### Maven install

Add the following dependencies in your pom.xml file:

```xml
<dependency>
   <groupId>org.vaadin.addons.flowingcode</groupId>
   <artifactId>chat-assistant-addon</artifactId>
   <version>X.Y.Z</version>
</dependency>
```
<!-- the above dependency should be updated with latest released version information -->

```xml
<repository>
   <id>vaadin-addons</id>
   <url>https://maven.vaadin.com/vaadin-addons</url>
</repository>
```

For SNAPSHOT versions see [here](https://maven.flowingcode.com/snapshots/).

## Building and running demo

- git clone repository
- mvn clean install jetty:run

To see the demo, navigate to http://localhost:8080/

## Release notes

See [here](https://github.com/FlowingCode/ChatAssistant/releases)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. 

As first step, please refer to our [Development Conventions](https://github.com/FlowingCode/DevelopmentConventions) page to find information about Conventional Commits & Code Style requirements.

Then, follow these steps for creating a contibution:

- Fork this project.
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- For commit message, use [Conventional Commits](https://github.com/FlowingCode/DevelopmentConventions/blob/main/conventional-commits.md) to describe your change.
- Send a pull request for the original project.
- Comment on the original issue that you have implemented a fix for it.

## License & Author

This add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

Chat Assistant Add-on is written by Flowing Code S.A.

# Developer Guide

## Getting started

Simple example showing the basic options:

	ChatAssistant chatAssistant = new ChatAssistant();
	TextArea message = new TextArea();
	message.setLabel("Enter a message from the assistant");
	message.setSizeFull();
	
	Button chat = new Button("Chat");
	chat.addClickListener(ev->{
	  Message m = new Message(message.getValue(),false,false,0,false,new Sender("Assistant","1","https://ui-avatars.com/api/?name=Bot"));
	  chatAssistant.sendMessage(m);
	  message.clear();
	});
	chatAssistant.sendMessage(new Message("Hello, I am here to assist you",false,false,0,false,new Sender("Assistant","1","https://ui-avatars.com/api/?name=Bot")));
	chatAssistant.toggle();
	chatAssistant.addChatSentListener(ev->{
		Notification.show(ev.getMessage());
	});

## Special configuration when using Spring

By default, Vaadin Flow only includes ```com/vaadin/flow/component``` to be always scanned for UI components and views. For this reason, the addon might need to be whitelisted in order to display correctly. 

To do so, just add ```com.flowingcode``` to the ```vaadin.whitelisted-packages``` property in ```src/main/resources/application.properties```, like:

```vaadin.whitelisted-packages = com.vaadin,org.vaadin,dev.hilla,com.flowingcode```
 
More information on Spring whitelisted configuration [here](https://vaadin.com/docs/latest/integrations/spring/configuration/#configure-the-scanning-of-packages).
