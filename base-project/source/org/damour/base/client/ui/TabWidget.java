package org.damour.base.client.ui;

import org.damour.base.client.images.BaseImageBundle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabWidget extends HorizontalPanel implements TabListener {

  private TabPanel tabPanel;
  private Widget content;
  private HorizontalPanel panel = new HorizontalPanel();
  private HTML leftCap = new HTML();
  private Image closeTabImage = new Image();

  public TabWidget(String text, boolean closable, TabPanel tabPanel, Widget content) {
    this.tabPanel = tabPanel;
    this.content = content;

    tabPanel.addTabListener(this);

    panel.setStyleName("tabWidget");

    final Label textLabel = new Label(text, false);
    textLabel.setHeight("100%");

    leftCap.setStyleName("tabWidgetCap");
    leftCap.setHeight("24px");
    leftCap.setWidth("5px");

    BaseImageBundle.images.closeTab().applyTo(closeTabImage);
    closeTabImage.setTitle("Close Tab");
    closeTabImage.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        closeTab();
      }
    });
    closeTabImage.addMouseListener(new MouseListener() {

      public void onMouseDown(Widget sender, int x, int y) {
      }

      public void onMouseEnter(Widget sender) {
        BaseImageBundle.images.closeTabHover().applyTo(closeTabImage);
      }

      public void onMouseLeave(Widget sender) {
        BaseImageBundle.images.closeTab().applyTo(closeTabImage);
      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
      }

    });

    
    DOM.setStyleAttribute(getElement(), "margin", "0px 1px 0px 1px");
    DOM.setStyleAttribute(getElement(), "padding", "0px 0px 0px 0px");

    
    panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    panel.add(textLabel);
    panel.setSpacing(0);
    DOM.setStyleAttribute(panel.getElement(), "margin", "0px 0px 0px 0px");
    DOM.setStyleAttribute(panel.getElement(), "padding", "5px 0px 3px 0px");

    if (closable) {
      panel.add(closeTabImage);
      panel.setSpacing(0);
      
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "0px 0px 0px 0px");
      DOM.setStyleAttribute(textLabel.getElement(), "padding", "0px 5px 0px 5px");
      
      DOM.setStyleAttribute(closeTabImage.getElement(), "margin", "0px 5px 0px 0px");
      DOM.setStyleAttribute(closeTabImage.getElement(), "padding", "0px 0px 0px 0px");
    } else {
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "0px 5px 0px 0px");
    }

    add(leftCap);
    add(panel);

    preventTextSelection(textLabel.getElement());
    sinkEvents(Event.MOUSEEVENTS);
  }

  public boolean isSelected() {
    if (tabPanel.getTabBar().getSelectedTab() == -1) {
      return false;
    }
    return (content == tabPanel.getWidget(tabPanel.getTabBar().getSelectedTab()));
  }

  public void onBrowserEvent(Event event) {
    blur(getElement().getParentElement().getParentElement());
    blur(getElement().getParentElement());
    blur(getElement());
    if ((DOM.eventGetType(event) & Event.ONMOUSEOVER) == Event.ONMOUSEOVER) {
      if (!isSelected()) {
        panel.addStyleDependentName("hover");
        leftCap.addStyleDependentName("hover");
        panel.removeStyleDependentName("selected");
        leftCap.removeStyleDependentName("selected");
      }
    } else if ((DOM.eventGetType(event) & Event.ONMOUSEOUT) == Event.ONMOUSEOUT) {
      if (!isSelected()) {
        panel.removeStyleDependentName("hover");
        leftCap.removeStyleDependentName("hover");
        panel.removeStyleDependentName("selected");
        leftCap.removeStyleDependentName("selected");
      }
    } else if ((DOM.eventGetType(event) & Event.ONMOUSEUP) == Event.ONMOUSEUP) {
      if (!isSelected()) {
        panel.addStyleDependentName("selected");
        leftCap.addStyleDependentName("selected");
      } else {
        panel.removeStyleDependentName("selected");
        leftCap.removeStyleDependentName("selected");
      }
    }
  }

  public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
    blur(getElement().getParentElement().getParentElement());
    blur(getElement().getParentElement());
    blur(getElement());
    return true;
  }

  public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
    blur(getElement().getParentElement().getParentElement());
    blur(getElement().getParentElement());
    blur(getElement());
    if (content == tabPanel.getWidget(tabIndex)) {
      panel.addStyleDependentName("selected");
      leftCap.addStyleDependentName("selected");
      panel.removeStyleDependentName("hover");
      leftCap.removeStyleDependentName("hover");
    } else {
      panel.removeStyleDependentName("selected");
      leftCap.removeStyleDependentName("selected");
      panel.removeStyleDependentName("hover");
      leftCap.removeStyleDependentName("hover");
    }
  }

  public void closeTab() {
    tabPanel.remove(tabPanel.getWidgetIndex(content));
  }

  private static native void preventTextSelection(Element ele) /*-{
    ele.onselectstart=function() {return false};
    ele.ondragstart=function() {return false};
   }-*/;
  
  public static native void blur(Element e)/*-{
  e.blur();
 }-*/;
}
