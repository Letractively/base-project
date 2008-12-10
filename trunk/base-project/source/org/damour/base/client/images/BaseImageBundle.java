package org.damour.base.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;

public interface BaseImageBundle extends ImageBundle, TreeImages {
  public static final BaseImageBundle images = (BaseImageBundle) GWT.create(BaseImageBundle.class);

  // media player
  AbstractImagePrototype player_play_32();
  AbstractImagePrototype player_pause_32();
  AbstractImagePrototype player_next_32();
  AbstractImagePrototype player_prev_32();
  AbstractImagePrototype player_hide_32();
  AbstractImagePrototype player_close_32();
  AbstractImagePrototype player_stop_32();
  
  // Tree
  AbstractImagePrototype treeOpen();
  AbstractImagePrototype treeClosed();
  AbstractImagePrototype treeLeaf();

  // file manager
  AbstractImagePrototype open_32();
  AbstractImagePrototype open_disabled_32();
  AbstractImagePrototype save_16();
  AbstractImagePrototype save_disabled_16();
  AbstractImagePrototype upload();
  AbstractImagePrototype upload_disabled();
  AbstractImagePrototype download();
  AbstractImagePrototype download_disabled();
  AbstractImagePrototype newFolder();
  AbstractImagePrototype newFolder_disabled();
  AbstractImagePrototype rename();
  AbstractImagePrototype rename_disabled();
  AbstractImagePrototype share();
  AbstractImagePrototype share_disabled();
  AbstractImagePrototype properties16();
  AbstractImagePrototype properties_disabled_16();
  AbstractImagePrototype folder32();
  AbstractImagePrototype file32();
  AbstractImagePrototype archive32();
  AbstractImagePrototype png32();
  AbstractImagePrototype gif32();
  AbstractImagePrototype jpg32();
  AbstractImagePrototype bmp32();
  AbstractImagePrototype movie32();
  AbstractImagePrototype audio32();
  AbstractImagePrototype text32();
  AbstractImagePrototype html32();
  AbstractImagePrototype jar32();
  AbstractImagePrototype location_16();
  AbstractImagePrototype location_disabled_16();
  AbstractImagePrototype showHide16();
  
  // comment widget
  AbstractImagePrototype next();
  AbstractImagePrototype previous();
  AbstractImagePrototype first();
  AbstractImagePrototype last();
  AbstractImagePrototype hierarchy();
  AbstractImagePrototype flatten();
  AbstractImagePrototype sort();
  AbstractImagePrototype refresh_16();
  AbstractImagePrototype refresh_disabled_16();
  AbstractImagePrototype reply();
  AbstractImagePrototype delete();
  AbstractImagePrototype delete_disabled();
  AbstractImagePrototype approve();
  AbstractImagePrototype empty16x16();
  
  // combo menu button icons
  AbstractImagePrototype downArrow();
  AbstractImagePrototype downArrowDisabled();

  // tab close icons
  AbstractImagePrototype closeTab();
  AbstractImagePrototype closeTabHover();

  // star rating widget
  AbstractImagePrototype starNoVotes();
  AbstractImagePrototype starFull();
  AbstractImagePrototype starHalf();
  AbstractImagePrototype starEmpty();
  AbstractImagePrototype starHover();
  
  // content advisory widget
  AbstractImagePrototype advisoryNR();
  AbstractImagePrototype advisoryG();
  AbstractImagePrototype advisoryPG();
  AbstractImagePrototype advisoryPG13();
  AbstractImagePrototype advisoryR();
  AbstractImagePrototype advisoryNC17();
}
