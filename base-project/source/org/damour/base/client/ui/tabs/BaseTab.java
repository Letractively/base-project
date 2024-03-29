/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 * 
 * @author Michael D'Amour
 * 
 */
package org.damour.base.client.ui.tabs;

import org.damour.base.client.images.BaseImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BaseTab extends SimplePanel {

  private BaseTabPanel tabPanel;
  private Widget content;
  private Label label = new Label();

  public BaseTab(String text, String tooltip, BaseTabPanel tabPanel, Widget content, boolean closeable) {
    this.content = content;
    this.tabPanel = tabPanel;
    setStylePrimaryName("base-tabWidget");
    sinkEvents(Event.ONDBLCLICK | Event.ONMOUSEUP);

    if (closeable) {
      final Image closeTabImage = new Image(BaseImageBundle.images.closeTab());

      closeTabImage.setStyleName("base-tabWidget-close");
      closeTabImage.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          event.getNativeEvent().stopPropagation();
          closeTab();
        }
      });
      closeTabImage.addMouseOverHandler(new MouseOverHandler() {
        public void onMouseOver(MouseOverEvent event) {
          closeTabImage.setResource(BaseImageBundle.images.closeTabHover());
        }
      });
      closeTabImage.addMouseOutHandler(new MouseOutHandler() {

        public void onMouseOut(MouseOutEvent event) {
          closeTabImage.setResource(BaseImageBundle.images.closeTab());
        }
      });

      HorizontalPanel p = new HorizontalPanel();
      setupLabel(text, tooltip);
      p.add(label);
      p.add(closeTabImage);
      setWidget(p);
    } else {
      setupLabel(text, tooltip);
      setWidget(label);
    }
  }

  public void setupLabel(String text, String tooltip) {
    label.setText(text);
    label.setTitle(tooltip);
    label.setStylePrimaryName("base-tabWidgetLabel");
  }

  public Widget getContent() {
    return content;
  }

  public void setContent(Widget content) {
    this.content = content;
  }

  protected BaseTabPanel getTabPanel() {
    return tabPanel;
  }

  protected void setTabPanel(BaseTabPanel tabPanel) {
    this.tabPanel = tabPanel;
  }

  public void onBrowserEvent(Event event) {
    if ((DOM.eventGetType(event) & Event.ONDBLCLICK) == Event.ONDBLCLICK) {
      onDoubleClick(event);
    } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
      onRightClick(event);
    } else if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
      if (event.getEventTarget().toString().toLowerCase().indexOf("image") == -1) {
        fireTabSelected();
      }
    }
    super.onBrowserEvent(event);
  }

  public void onDoubleClick(Event event) {
  }

  public void onRightClick(Event event) {
  }

  public void setSelected(boolean selected) {
    if (selected) {
      addStyleDependentName("selected");
    } else {
      removeStyleDependentName("selected");
    }
  }

  public String getLabelText() {
    return label.getText();
  }

  public void setLabelText(String text) {
    label.setText(text);
  }

  public void setLabelTooltip(String tooltip) {
    label.setTitle(tooltip);
  }

  public String getLabelTooltip() {
    return label.getTitle();
  }

  protected void closeTab() {
    tabPanel.closeTab(this, true);
  }

  protected void fireTabSelected() {
    tabPanel.selectTab(this);
  }

}
