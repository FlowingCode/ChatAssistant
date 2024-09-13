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

import com.flowingcode.vaadin.addons.chatassistant.model.Message;
import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.demo.SourcePosition;
import com.google.common.base.Strings;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DemoSource(sourcePosition = SourcePosition.PRIMARY)
@PageTitle("Lazy Loading Demo")
@SuppressWarnings("serial")
@Route(value = "chat-assistant/lazy-loading-demo", layout = ChatAssistantDemoView.class)
@CssImport("./styles/chat-assistant-styles-demo.css")
public class ChatAssistantLazyLoadingDemo extends VerticalLayout {
  
  List<Message> messages = new ArrayList<>(Arrays.asList(
      Message.builder().name("Claudius").content("I have sent to seek him and to find the body.\n"
          + "How dangerous is it that this man goes loose!\n"
          + "Yet must not we put the strong law on him.\n"
          + "He's loved of the distracted multitude,\n"
          + "Who like not in their judgment, but their eyes.\n"
          + "And where 'tis so, th' offender's scourge is weighed,\n"
          + "But never the offense. To bear all smooth and even,\n"
          + "This sudden sending him away must seem\n"
          + "Deliberate pause. Diseases desperate grown\n"
          + "By desperate appliance are relieved,\n"
          + "Or not at all.").build(),
      Message.builder().name("Rosencrantz").content("(Enter)").build(),
      Message.builder().name("Claudius").content("How now, what hath befall'n?").build(),
      Message.builder().name("Rosencrantz").content("Where the dead body is bestowed, my lord,\n"
          + "We cannot get from him.").build(),
      Message.builder().name("Claudius").content("But where is he?").build(),
      Message.builder().name("Rosencrantz").content("Without, my lord; guarded, to know your pleasure.").build(),
      Message.builder().name("Claudius").content("Bring him before us.").build(),
      Message.builder().name("Rosencrantz").content("Ho, Guildenstern! Bring in my lord.").build(),
      Message.builder().name("Claudius").content("Now, Hamlet, where's Polonius?").build(),
      Message.builder().name("Hamlet").content("At supper.").build(),
      Message.builder().name("Claudius").content("At supper? Where? ").build(),
      Message.builder().name("Hamlet").content("Not where he eats, but where he is eaten. A certain \n"
          + "convocation of politic worms are e'en at him. Your worm is your \n"
          + "only emperor for diet. We fat all creatures else to fat us, and \n"
          + "we fat ourselves for maggots. Your fat king and your lean beggar \n"
          + "is but variable service- two dishes, but to one table. That's the \n"
          + "end.").build(),
      Message.builder().name("Claudius").content("Alas, alas!").build(),
      Message.builder().name("Hamlet").content("A man may fish with the worm that hath eat of a king, and eat \n"
          + "of the fish that hath fed of that worm.").build(),
      Message.builder().name("Claudius").content("What dost thou mean by this?").build(),
      Message.builder().name("Hamlet").content("Nothing but to show you how a king may go a progress through \n"
          + "the guts of a beggar.\n"
          + "").build(),
      Message.builder().name("Claudius").content("Where is Polonius?").build(),
      Message.builder().name("Hamlet").content("In heaven. Send thither to see. If your messenger find him not \n"
          + "there, seek him i' th' other place yourself. But indeed, if you\n"
          + "find him not within this month, you shall nose him as you go up \n"
          + "the stair, into the lobby.").build(),
      Message.builder().name("Claudius").content("Go seek him there.").build(),
      Message.builder().name("Hamlet").content("He will stay till you come.").build(),
      Message.builder().name("Claudius").content("Hamlet, this deed, for thine especial safety,- \n"
          + "Which we do tender as we dearly grieve \n"
          + "For that which thou hast done,- must send thee hence \n"
          + "With fiery quickness. Therefore prepare thyself. \n"
          + "The bark is ready and the wind at help, \n"
          + "Th' associates tend, and everything is bent \n"
          + "For England.").build(),
      Message.builder().name("Hamlet").content("For England?").build(),
      Message.builder().name("Claudius").content("Ay, Hamlet.").build(),
      Message.builder().name("Hamlet").content("Good.").build(),
      Message.builder().name("Claudius").content("So is it, if thou knew'st our purposes.").build(),
      Message.builder().name("Hamlet").content("I see a cherub that sees them. But come, for England! \n"
          + "Farewell, dear mother.").build(),
      Message.builder().name("Claudius").content("Thy loving father, Hamlet.").build(),
      Message.builder().name("Hamlet").content("My mother! Father and mother is man and wife; man and wife is\n"
          + "one flesh; and so, my mother. Come, for England!").build(),
      Message.builder().name("Claudius").content("Follow him at foot; tempt him with speed aboard. \n"
          + "Delay it not; I'll have him hence to-night. \n"
          + "Away! for everything is seal'd and done\n"
          + "That else leans on th' affair. Pray you make haste.").build(),
      Message.builder().name("Claudius").content("And, England, if my love thou hold'st at aught,- \n"
          + "As my great power thereof may give thee sense, \n"
          + "Since yet thy cicatrice looks raw and red\n"
          + "After the Danish sword, and thy free awe \n"
          + "Pays homage to us,- thou mayst not coldly set \n"
          + "Our sovereign process, which imports at full, \n"
          + "By letters congruing to that effect, \n"
          + "The present death of Hamlet. Do it, England; \n"
          + "For like the hectic in my blood he rages, \n"
          + "And thou must cure me. Till I know 'tis done, \n"
          + "Howe'er my haps, my joys were ne'er begun. ").build()
      ));
  
  public ChatAssistantLazyLoadingDemo() {
    ChatAssistant chatAssistant = new ChatAssistant();
    chatAssistant.setClassName("small");
    Span lazyLoadingData = new Span();
    DataProvider<Message,?> dataProvider = DataProvider.fromCallbacks(query->{
      lazyLoadingData.setText("Loading messages from: " + query.getOffset() + ", with limit: " + query.getLimit());
      return messages.stream().skip(query.getOffset()).limit(query.getLimit());
    }, query->{
      return messages.size();
    });
    chatAssistant.setDataProvider(dataProvider);
    
    TextArea message = new TextArea();
    message.setLabel("Enter a message from the assistant");
    message.setSizeFull();
    message.addKeyPressListener(ev->{
      if (Strings.isNullOrEmpty(chatAssistant.getWhoIsTyping())) {
        chatAssistant.setWhoIsTyping("Assistant is generating an answer ...");
      }
    });
    message.addBlurListener(ev->chatAssistant.clearWhoIsTyping());

    Button chat = new Button("Chat");
    chat.addClickListener(ev -> {
      Message m = Message.builder().content(message.getValue()).messageTime(LocalDateTime.now())
          .name("Assistant").avatar("chatbot.png").build();

      messages.add(m);
      dataProvider.refreshAll();
      chatAssistant.scrollToEnd();
      message.clear();
    });
    chatAssistant.setSubmitListener(ev -> {
      Message userMessage = Message.builder().messageTime(LocalDateTime.now())
          .name("User").content(ev.getValue()).build();
      messages.add(userMessage);
      dataProvider.refreshAll();
      chatAssistant.scrollToEnd();
    });
    Icon minimize = VaadinIcon.MINUS.create();
    minimize.addClickListener(ev -> chatAssistant.setMinimized(!chatAssistant.isMinimized()));
    Span title = new Span("Customized Assistant Header");
    title.setWidthFull();
    HorizontalLayout headerBar = new HorizontalLayout(title, minimize);
    headerBar.setWidthFull();
    chatAssistant.setHeaderComponent(headerBar);

    add(message, lazyLoadingData, chat, chatAssistant);
  }
  
}
