package org.damour.base.demo.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

public interface AgelizerImageBundle extends ImageBundle {
  public static final AgelizerImageBundle images = (AgelizerImageBundle) GWT.create(AgelizerImageBundle.class);

  AbstractImagePrototype createAccount_212x89();
  AbstractImagePrototype createAccount_hover_212x89();
  AbstractImagePrototype createAccount_disabled_212x89();

  AbstractImagePrototype ratePhotos_172x89();
  AbstractImagePrototype ratePhotos_hover_172x89();
  AbstractImagePrototype ratePhotos_disabled_172x89();

  AbstractImagePrototype uploadPhotos_189x89();
  AbstractImagePrototype uploadPhotos_hover_189x89();
  AbstractImagePrototype uploadPhotos_disabled_189x89();

}
