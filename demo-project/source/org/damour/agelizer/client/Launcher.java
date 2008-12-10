package org.damour.agelizer.client;

import org.damour.agelizer.client.images.AgelizerImageBundle;
import org.damour.base.client.objects.User;
import org.damour.base.client.ui.authentication.CreateNewAccountCommand;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Launcher extends VerticalPanel {

  public Launcher(User user) {
    setVerticalAlignment(ALIGN_MIDDLE);
    setHorizontalAlignment(ALIGN_CENTER);
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setHorizontalAlignment(ALIGN_CENTER);
    buttonPanel.setVerticalAlignment(ALIGN_MIDDLE);

    final Image createAccountImage = new Image();
    if (user != null) {
      AgelizerImageBundle.images.createAccount_disabled_212x89().applyTo(createAccountImage);
    } else {
      AgelizerImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
      createAccountImage.setTitle("Create an Account");
      createAccountImage.setStyleName("genericImageButton");
      createAccountImage.addMouseListener(new MouseListener() {
        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          AgelizerImageBundle.images.createAccount_hover_212x89().applyTo(createAccountImage);
        }

        public void onMouseLeave(Widget sender) {
          AgelizerImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
          AgelizerImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
        }
      });
      createAccountImage.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          CreateNewAccountCommand cmd = new CreateNewAccountCommand();
          cmd.execute();
        }
      });
    }

    final Image uploadPhotosImage = new Image();
    if (user == null) {
      AgelizerImageBundle.images.uploadPhotos_disabled_189x89().applyTo(uploadPhotosImage);
    } else {
      AgelizerImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
      uploadPhotosImage.setTitle("Upload Photos");
      uploadPhotosImage.setStyleName("genericImageButton");
      uploadPhotosImage.addMouseListener(new MouseListener() {
        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          AgelizerImageBundle.images.uploadPhotos_hover_189x89().applyTo(uploadPhotosImage);
        }

        public void onMouseLeave(Widget sender) {
          AgelizerImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
          AgelizerImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
        }
      });
    }

    final Image ratePhotosImage = new Image();
    AgelizerImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
    ratePhotosImage.setTitle("Rate Photos");
    ratePhotosImage.setStyleName("genericImageButton");
    ratePhotosImage.addMouseListener(new MouseListener() {
      public void onMouseDown(Widget sender, int x, int y) {
      }

      public void onMouseEnter(Widget sender) {
        AgelizerImageBundle.images.ratePhotos_hover_172x89().applyTo(ratePhotosImage);
      }

      public void onMouseLeave(Widget sender) {
        AgelizerImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
        AgelizerImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
      }
    });

    buttonPanel.add(createAccountImage);
    buttonPanel.add(uploadPhotosImage);
    buttonPanel.add(ratePhotosImage);
    add(buttonPanel);
  }
}
