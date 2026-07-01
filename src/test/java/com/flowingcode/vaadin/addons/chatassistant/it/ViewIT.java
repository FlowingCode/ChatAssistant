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

package com.flowingcode.vaadin.addons.chatassistant.it;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.vaadin.testbench.TestBenchElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

public class ViewIT extends AbstractViewTest {

  private Matcher<TestBenchElement> hasBeenUpgradedToCustomElement =
      new TypeSafeDiagnosingMatcher<TestBenchElement>() {

        @Override
        public void describeTo(Description description) {
          description.appendText("a custom element");
        }

        @Override
        protected boolean matchesSafely(TestBenchElement item, Description mismatchDescription) {
          String script =
              "return !!window.customElements.get(arguments[0].tagName.toLowerCase())";
          if (!item.getTagName().contains("-")) {
            return true;
          }
          if ((Boolean) item.getCommandExecutor().executeScript(script, item)) {
            return true;
          } else {
            mismatchDescription.appendText(item.getTagName() + " ");
            mismatchDescription.appendDescriptionOf(is(not(this)));
            return false;
          }
        }
      };

  @Test
  public void componentWorks() {
    TestBenchElement element = $("animated-fab").first();
    MatcherAssert.assertThat(element, hasBeenUpgradedToCustomElement);
  }
}
