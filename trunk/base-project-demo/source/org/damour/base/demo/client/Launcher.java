package org.damour.base.demo.client;

import org.damour.base.client.objects.User;
import org.damour.base.client.ui.authentication.CreateNewAccountCommand;
import org.damour.base.client.ui.colorpicker.ColorPickerDialog;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.demo.client.images.DemoImageBundle;

import com.google.gwt.user.client.Window;
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
      DemoImageBundle.images.createAccount_disabled_212x89().applyTo(createAccountImage);
    } else {
      DemoImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
      createAccountImage.setTitle("Create an Account");
      createAccountImage.setStyleName("genericImageButton");
      createAccountImage.addMouseListener(new MouseListener() {
        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          DemoImageBundle.images.createAccount_hover_212x89().applyTo(createAccountImage);
        }

        public void onMouseLeave(Widget sender) {
          DemoImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
          DemoImageBundle.images.createAccount_212x89().applyTo(createAccountImage);
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
      DemoImageBundle.images.uploadPhotos_disabled_189x89().applyTo(uploadPhotosImage);
    } else {
      DemoImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
      uploadPhotosImage.setTitle("Upload Photos");
      uploadPhotosImage.setStyleName("genericImageButton");
      uploadPhotosImage.addMouseListener(new MouseListener() {
        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          DemoImageBundle.images.uploadPhotos_hover_189x89().applyTo(uploadPhotosImage);
        }

        public void onMouseLeave(Widget sender) {
          DemoImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
          DemoImageBundle.images.uploadPhotos_189x89().applyTo(uploadPhotosImage);
        }
      });
    }
    uploadPhotosImage.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        final ColorPickerDialog picker = new ColorPickerDialog("00ff00");
        picker.center();
        picker.setCallback(new IDialogCallback() {
          public void cancelPressed() {
          }
          public void okPressed() {
            Window.alert(picker.getHexColor());
          }
        });
      }
    });

    
    final Image ratePhotosImage = new Image();
    DemoImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
    ratePhotosImage.setTitle("Rate Photos");
    ratePhotosImage.setStyleName("genericImageButton");
    ratePhotosImage.addMouseListener(new MouseListener() {
      public void onMouseDown(Widget sender, int x, int y) {
      }

      public void onMouseEnter(Widget sender) {
        DemoImageBundle.images.ratePhotos_hover_172x89().applyTo(ratePhotosImage);
      }

      public void onMouseLeave(Widget sender) {
        DemoImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
        DemoImageBundle.images.ratePhotos_172x89().applyTo(ratePhotosImage);
      }
    });

    buttonPanel.add(createAccountImage);
    buttonPanel.add(uploadPhotosImage);
    buttonPanel.add(ratePhotosImage);
    add(buttonPanel);
  }
}
