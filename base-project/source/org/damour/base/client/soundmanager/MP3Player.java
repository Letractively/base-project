package org.damour.base.client.soundmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.ui.dialogs.DialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MP3Player {

  private DialogBox popupPanel = new DialogBox(false, false);
  private Label songLabel = new Label("", false);
  private Label statusLabel = new Label("", false);
  private Label artistLabel = new Label("", false);
  private Label artistLabelLabel = new Label("", false);
  private List<String> playlist = new ArrayList<String>();
  private static HashMap<String, String> loadedMap = new HashMap<String, String>();
  private int playlistIndex = 0;
  private int currentlyPlayingIndex = -1;
  private boolean paused = false;
  final Image pauseButton = new Image();

  private static MP3Player instance = new MP3Player();

  public static MP3Player getInstance() {
    return instance;
  }

  private MP3Player() {
    // initialize ui/popuppanel
    pauseButton.setUrl(BaseImageBundle.images.player_play_32().getURL());
    pauseButton.setStyleName("mp3playerButton");
    pauseButton.setTitle("Play");
    pauseButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        togglePause();
      }
    });
    Image stopButton = new Image();
    stopButton.setUrl(BaseImageBundle.images.player_stop_32().getURL());
    stopButton.setStyleName("mp3playerButton");
    stopButton.setTitle("Stop");
    stopButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        stop();
      }
    });
    Image nextButton = new Image();
    nextButton.setUrl(BaseImageBundle.images.player_next_32().getURL());
    nextButton.setStyleName("mp3playerButton");
    nextButton.setTitle("Next");
    nextButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        next();
      }
    });
    Image prevButton = new Image();
    prevButton.setUrl(BaseImageBundle.images.player_prev_32().getURL());
    prevButton.setStyleName("mp3playerButton");
    prevButton.setTitle("Previous");
    prevButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        prev();
      }
    });
    Image hideButton = new Image();
    hideButton.setUrl(BaseImageBundle.images.player_hide_32().getURL());
    hideButton.setStyleName("mp3playerButton");
    hideButton.setTitle("Hide");
    hideButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    Image closeButton = new Image();
    closeButton.setUrl(BaseImageBundle.images.player_close_32().getURL());
    closeButton.setStyleName("mp3playerButton");
    closeButton.setTitle("Close");
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        for (String soundName : loadedMap.keySet()) {
          destroySound(soundName);
        }
        loadedMap.clear();
        playlist.clear();
        stop();
        hide();
      }
    });
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(pauseButton);
    buttonPanel.add(prevButton);
    buttonPanel.add(stopButton);
    buttonPanel.add(nextButton);
    buttonPanel.add(hideButton);
    buttonPanel.add(closeButton);

    DOM.setStyleAttribute(songLabel.getElement(), "color", "blue");
    DOM.setStyleAttribute(songLabel.getElement(), "fontSize", "8pt");
    DOM.setStyleAttribute(statusLabel.getElement(), "fontSize", "8pt");

    HorizontalPanel artistLabelPanel = new HorizontalPanel();
    artistLabelPanel.add(artistLabelLabel);
    artistLabelPanel.add(artistLabel);
    DOM.setStyleAttribute(artistLabel.getElement(), "color", "blue");
    DOM.setStyleAttribute(artistLabel.getElement(), "fontSize", "8pt");
    DOM.setStyleAttribute(artistLabelLabel.getElement(), "fontSize", "8pt");

    HorizontalPanel songLabelPanel = new HorizontalPanel();
    songLabelPanel.add(statusLabel);
    songLabelPanel.add(songLabel);

    VerticalPanel playerContentPanel = new VerticalPanel();
    playerContentPanel.setStyleName("mp3player");
    playerContentPanel.add(artistLabelPanel);
    playerContentPanel.add(songLabelPanel);
    playerContentPanel.add(buttonPanel);

    popupPanel.setHTML("MP3Player");
    popupPanel.setWidget(playerContentPanel);
  }

  public void addSoundToPlayList(String soundName, String url) {
    if (!url.endsWith(".mp3")) {
      if (!url.contains("?")) {
        url += "?";
      } else {
        url += "&";
      }
      url += "filename=" + url;
    }
    if (!loadedMap.containsKey(soundName)) {
      createSound(soundName, url);
    }
    playlist.add(soundName);
  }

  public void next() {
    if (playlistIndex == playlist.size() - 1) {
      // we're at the end, there is no next
      return;
    }
    nativeStop();
    ++playlistIndex;
    play();
  }

  public void prev() {
    if (playlistIndex == 0) {
      // we're at the end, there is no next
      return;
    }
    nativeStop();
    --playlistIndex;
    play();
  }

  public void play() {
    if (paused) {
      togglePause();
      return;
    }
    if (currentlyPlayingIndex == playlistIndex) {
      // already playing
      return;
    }
    if (playlistIndex >= playlist.size()) {
      playlistIndex = 0;
    }
    if (playlistIndex >= 0 && playlistIndex < playlist.size()) {
      String soundName = playlist.get(playlistIndex);
      currentlyPlayingIndex = playlistIndex;
      playSound(soundName);
    }
  }

  private void playNext() {
    play();
  }

  public void stop() {
    try {
      if (isSoundManagerLoaded()) {
        playlistIndex = 0;
        currentlyPlayingIndex = -1;
        statusLabel.setText("Stopped");
        songLabel.setText("");
        artistLabel.setText("");
        artistLabelLabel.setText("");
        pauseButton.setUrl(BaseImageBundle.images.player_play_32().getURL());
        pauseButton.setTitle("Play");
        nativeStop();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void togglePause() {
    try {
      if (isSoundManagerLoaded()) {
        if (playlistIndex >= 0 && playlistIndex < playlist.size()) {
          paused = !paused;
          if (paused) {
            pauseButton.setUrl(BaseImageBundle.images.player_play_32().getURL());
            pauseButton.setTitle("Play");
          } else {
            pauseButton.setUrl(BaseImageBundle.images.player_pause_32().getURL());
            pauseButton.setTitle("Pause");
          }
          String soundName = playlist.get(playlistIndex);
          nativeTogglePause(soundName);
          setID3Info(soundName);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void show() {
    popupPanel.center();
  }

  public void hide() {
    popupPanel.hide();
  }

  public void playSound(final String soundName) {
    try {
      if (isSoundManagerLoaded()) {
        statusLabel.setText("Playing: ");
        songLabel.setText(soundName);
        nativePlaySound(this, soundName);
        setID3Info(soundName);
        pauseButton.setUrl(BaseImageBundle.images.player_pause_32().getURL());
        pauseButton.setTitle("Pause");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setID3Info(final String soundName) {
    Timer t = new Timer() {
      public void run() {
        statusLabel.setText("Playing: ");
        String title = getID3Tag(soundName, "TIT2");
        if (title != null && !"".equals(title)) {
          songLabel.setText(title);
        } else {
          title = getID3Tag(soundName, "title");
          if (title != null && !"".equals(title)) {
            songLabel.setText(title);
          } else {
            songLabel.setText(soundName);
          }
        }
        String artist = getID3Tag(soundName, "TPE1");
        if (artist != null && !"".equals(artist)) {
          artistLabelLabel.setText("Artist: ");
          artistLabel.setText(artist);
        } else {
          artistLabelLabel.setText("");
          artistLabel.setText("");
        }
      }
    };
    t.schedule(250);
  }

  /**
   * Create / Register a sound with the sound manager. If the sound is in your module public folder, be sure to build that into the url. This can be done simply
   * by adding GWT.getModuleBaseURL() to beginning of the url.
   * 
   * @param soundName
   *          any name you want to reference the sound by
   * @param url
   *          any valid url to an MP3
   */
  public void createSound(String soundName, String url) {
    if (!url.endsWith(".mp3")) {
      if (!url.contains("?")) {
        url += "?";
      } else {
        url += "&";
      }
      url += "filename=" + url;
    }
    try {
      if (isSoundManagerLoaded()) {
        if (!loadedMap.containsKey(soundName)) {
          loadedMap.put(soundName, url);
          nativeCreateSound(this, soundName, url);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void destroySound(String soundName) {
    try {
      if (isSoundManagerLoaded()) {
        playlist.remove(soundName);
        loadedMap.remove(soundName);
        nativeDestroySound(soundName);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onfinish(String soundName) {
    if (playlist.contains(soundName)) {
      playlistIndex++;
    }
    playNext();
  }

  private native String getID3Tag(String soundName, String tag)
  /*-{
    var id3Obj = $wnd.soundManager.getSoundById(soundName).id3;
    //var prop = null;
    //for (prop in id3Obj) {
      //alert("stuff: " + id3Obj[prop]);
    //}
    return id3Obj[tag];
  }-*/;

  private native void nativeTogglePause(String soundName)
  /*-{
    $wnd.soundManager.togglePause(soundName);
  }-*/;

  private native void nativeStop()
  /*-{
    $wnd.soundManager.stopAll();
  }-*/;

  private native void nativeDestroySound(String soundName)
  /*-{
    $wnd.soundManager.destroySound(soundName);
  }-*/;

  private native void nativePlaySound(MP3Player player, String soundName)
  /*-{
    $wnd.soundManager.play(soundName);
  }-*/;

  private native void nativeCreateSound(MP3Player player, String soundName, String url)
  /*-{
    var myPlayer = player;
    var mySoundName = soundName;
    var soundManager = $wnd.soundManager;
    playNextSound = function() {
      myPlayer.@org.damour.base.client.soundmanager.MP3Player::onfinish(Ljava/lang/String;)(mySoundName);
    }
    var mySoundObject = $wnd.soundManager.createSound({
      id: soundName,
      url: url,
      onfinish: playNextSound,
      stream: true
    });
  }-*/;

  private native boolean isSoundManagerLoaded()
  /*-{
    return (true == $wnd.soundManager.onLoadExecuted);
  }-*/;

}
