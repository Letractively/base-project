package org.damour.base.client.ui.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.buttons.IconButton;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CommentWidget extends VerticalPanel {

  private PermissibleObject permissibleObject;
  private List<Comment> comments;
  private boolean sortDescending = true;
  private boolean flatten = false;
  private ListBox maxCommentDepthListBox = new ListBox(false);

  private Comment workingOnComment;

  private int pageNumber = 0;
  private int pageSize = 10;
  private long numComments = 0;
  private long lastPageNumber = 0;
  private boolean paginate = true;

  HashMap<Integer, Page<Comment>> pageCache = new HashMap<Integer, Page<Comment>>();

  private AsyncCallback<Boolean> deleteCommentCallback = new AsyncCallback<Boolean>() {

    public void onSuccess(Boolean result) {
      pageCache.clear();
      fetchPage();
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };
  private AsyncCallback<Boolean> approveCallback = new AsyncCallback<Boolean>() {

    public void onSuccess(Boolean result) {
      workingOnComment.setApproved(true);
      loadCommentWidget(true);
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
      workingOnComment.setApproved(false);
      loadCommentWidget(true);
    }
  };
  private AsyncCallback<Boolean> submitCommentCallback = new AsyncCallback<Boolean>() {

    public void onSuccess(Boolean result) {
      pageCache.clear();
      fetchPage();
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };
  private AsyncCallback<Page<Comment>> pageCallback = new AsyncCallback<Page<Comment>>() {

    public void onSuccess(Page<Comment> page) {
      pageCache.put(page.getPageNumber(), page);
      comments = page.getResults();
      numComments = page.getTotalRowCount();
      lastPageNumber = page.getLastPageNumber();
      if (page.getResults().size() == 0) {
        pageNumber = 0;
        lastPageNumber = 0;
      }
      loadCommentWidget(true);
      prefetchPages();
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };

  private AsyncCallback<List<Comment>> allCommentsCallback = new AsyncCallback<List<Comment>>() {

    public void onSuccess(List<Comment> comments) {
      CommentWidget.this.comments = comments;
      numComments = comments.size();
      loadCommentWidget(true);
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };

  private AsyncCallback<Page<Comment>> preFetchPageCallback = new AsyncCallback<Page<Comment>>() {

    public void onSuccess(Page<Comment> page) {
      pageCache.put(page.getPageNumber(), page);
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };

  public CommentWidget(final PermissibleObject permissibleObject, final List<Comment> comments, boolean paginate) {
    this.permissibleObject = permissibleObject;
    this.comments = comments;
    this.paginate = paginate;

    maxCommentDepthListBox.addItem("None", "999999");
    maxCommentDepthListBox.addItem("1");
    maxCommentDepthListBox.addItem("2");
    maxCommentDepthListBox.addItem("3");
    maxCommentDepthListBox.addItem("4");
    maxCommentDepthListBox.addItem("5");
    maxCommentDepthListBox.setSelectedIndex(0);
    maxCommentDepthListBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        loadCommentWidget(true);
      }
    });

    add(new Label("Loading..."));

    Timer t = new Timer() {
      public void run() {
        if (comments == null) {
          fetchPage();
        } else {
          loadCommentWidget(false);
        }
      }
    };
    t.schedule(1);

  }

  private void loadCommentWidget(final boolean forceOpen) {
    clear();
    if (permissibleObject.isAllowComments()) {

      String fileName = permissibleObject.getName();
      final DisclosurePanel commentDisclosurePanel = new DisclosurePanel("View comments (" + numComments + ") for " + fileName);

      VerticalPanel commentsPanel = new VerticalPanel();
      commentsPanel.setSpacing(0);
      if (numComments > 0) {
        commentsPanel.setStyleName("commentsPanel");
      }
      commentsPanel.setWidth("100%");

      int renderedComments = 0;
      boolean userCanManage = AuthenticationHandler.getInstance().getUser() != null
          && (AuthenticationHandler.getInstance().getUser().isAdministrator() || AuthenticationHandler.getInstance().getUser().equals(
              permissibleObject.getOwner()));
      List<Comment> sortedComments = new ArrayList<Comment>();
      if (comments != null) {
        sortedComments.addAll(comments);
      }
      if (!flatten) {
        sortedComments = sortComments(sortedComments);
      }

      for (final Comment comment : sortedComments) {
        int commentDepth = getCommentDepth(comment);

        int maxDepth = Integer.parseInt(maxCommentDepthListBox.getValue(maxCommentDepthListBox.getSelectedIndex()));
        if (commentDepth >= maxDepth) {
          continue;
        }

        boolean userIsAuthorOfComment = AuthenticationHandler.getInstance().getUser() != null && comment.getAuthor() != null
            && comment.getAuthor().equals(AuthenticationHandler.getInstance().getUser());
        
        if (userCanManage || userIsAuthorOfComment || comment.isApproved()) {

          FlexTable commentHeaderPanel = new FlexTable();
          commentHeaderPanel.setCellPadding(0);
          commentHeaderPanel.setCellSpacing(0);
          commentHeaderPanel.setStyleName("commentHeader");
          commentHeaderPanel.setWidth("100%");

          String authorLabelString = comment.getAuthor() == null ? comment.getEmail() : comment.getAuthor().getUsername();
          if (comment.getAuthor() != null && comment.getAuthor().getFirstname() != null && !"".equals(comment.getAuthor().getFirstname())) {
            authorLabelString += " (" + comment.getAuthor().getFirstname();
            if (comment.getAuthor() != null && comment.getAuthor().getLastname() != null && !"".equals(comment.getAuthor().getLastname())) {
              authorLabelString += " " + comment.getAuthor().getLastname() + ")";
            } else {
              authorLabelString += ")";
            }
          }

          Image replyCommentImage = new Image();
          BaseImageBundle.images.reply().applyTo(replyCommentImage);
          replyCommentImage.setStyleName("commentActionButton");
          replyCommentImage.setTitle("Reply to this comment");
          replyCommentImage.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
              replyToComment(comment);
            }
          });
          int columnIndex = 0;
          commentHeaderPanel.setWidget(0, columnIndex, replyCommentImage);
          commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_LEFT);
          columnIndex++;

          Label authorLabel = new Label(authorLabelString, false);
          commentHeaderPanel.setWidget(0, columnIndex, authorLabel);
          commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_LEFT);
          columnIndex++;
          commentHeaderPanel.setWidget(0, columnIndex, new Label());
          commentHeaderPanel.getFlexCellFormatter().setWidth(0, columnIndex, "100%");
          commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
          columnIndex++;
          Label dateLabel = new Label(new Date(comment.getCommentDate()).toLocaleString(), false);
          commentHeaderPanel.setWidget(0, columnIndex, dateLabel);
          if (!userCanManage && !userIsAuthorOfComment) {
            DOM.setStyleAttribute(dateLabel.getElement(), "padding", "0 5px 0 0");
          }
          commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);

          columnIndex++;
          if (userCanManage || userIsAuthorOfComment) {
            if (userCanManage && !comment.isApproved()) {
              final Image approveCommentImage = new Image();
              BaseImageBundle.images.approve().applyTo(approveCommentImage);
              approveCommentImage.setStyleName("commentActionButton");
              approveCommentImage.setTitle("Approve comment");
              approveCommentImage.addClickListener(new ClickListener() {

                public void onClick(Widget sender) {
                  workingOnComment = comment;
                  approveComment(comment);
                }
              });
              commentHeaderPanel.setWidget(0, columnIndex, approveCommentImage);
              commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
              columnIndex++;
            } else {
              // put 16x16 spacer here for alignment
              final Image approveSpacerImage = new Image();
              BaseImageBundle.images.empty16x16().applyTo(approveSpacerImage);
              approveSpacerImage.setStyleName("commentActionButton");
              commentHeaderPanel.setWidget(0, columnIndex, approveSpacerImage);
              commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
              columnIndex++;
            }
            Image deleteCommentImage = new Image();
            BaseImageBundle.images.delete().applyTo(deleteCommentImage);
            deleteCommentImage.setStyleName("commentActionButton");
            deleteCommentImage.setTitle("Remove comment");
            deleteCommentImage.addClickListener(new ClickListener() {

              public void onClick(Widget sender) {
                IDialogCallback callback = new IDialogCallback() {

                  public void cancelPressed() {
                  }

                  public void okPressed() {
                    deleteComment(comment);
                  }
                };
                PromptDialogBox dialogBox = new PromptDialogBox("Question", "Yes", null, "No", false, true);
                dialogBox.setContent(new Label("Delete comment by " + (comment.getAuthor() == null ? comment.getEmail() : comment.getAuthor().getUsername())
                    + "?"));
                dialogBox.setCallback(callback);
                dialogBox.center();
              }
            });
            commentHeaderPanel.setWidget(0, columnIndex, deleteCommentImage);
            commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
            columnIndex++;
          }

          if (commentDepth > 0) {
            HorizontalPanel commentHeaderPanelWrapper = new HorizontalPanel();
            commentHeaderPanelWrapper.setWidth("100%");
            Label spacerLabel = new Label();
            commentHeaderPanelWrapper.add(spacerLabel);
            if (!flatten) {
              commentHeaderPanelWrapper.setCellWidth(spacerLabel, (commentDepth * 20) + "px");
            }
            commentHeaderPanelWrapper.add(commentHeaderPanel);
            commentsPanel.add(commentHeaderPanelWrapper);
          } else {
            commentsPanel.add(commentHeaderPanel);
          }

          Label commentLabel = new Label(comment.getComment(), true);
          if (comment.isApproved()) {
            commentLabel.setStyleName("comment");
          } else if (userCanManage || userIsAuthorOfComment) {
            commentLabel.setStyleName("commentAwaitingApproval");
          }

          if (commentDepth > 0) {
            HorizontalPanel commentHeaderPanelWrapper = new HorizontalPanel();
            commentHeaderPanelWrapper.setWidth("100%");
            Label spacerLabel = new Label();
            commentHeaderPanelWrapper.add(spacerLabel);
            if (!flatten) {
              commentHeaderPanelWrapper.setCellWidth(spacerLabel, (commentDepth * 20) + "px");
            }
            commentHeaderPanelWrapper.add(commentLabel);
            commentsPanel.add(commentHeaderPanelWrapper);
          } else {
            commentsPanel.add(commentLabel);
          }
          renderedComments++;
        }
      }

      final FlexTable mainPanel = new FlexTable();
      mainPanel.setWidth("100%");
      int row = 0;
      if (paginate) {
        mainPanel.setWidget(row, 0, createButtonPanel(mainPanel, forceOpen));
        mainPanel.getCellFormatter().setHorizontalAlignment(row++, 0, HasHorizontalAlignment.ALIGN_LEFT);
      }
      mainPanel.setWidget(row, 0, commentsPanel);
      mainPanel.getCellFormatter().setWidth(row++, 0, "100%");

      commentDisclosurePanel.setContent(mainPanel);
      commentDisclosurePanel.setOpen(renderedComments == 0 || forceOpen);
      commentDisclosurePanel.setWidth("100%");
      add(createCommentPostPanel());
      add(commentDisclosurePanel);
    }
  }

  private Widget createPageControllerPanel(final FlexTable mainPanel) {
    final IconButton nextPageImageButton = new IconButton(null, true, BaseImageBundle.images.next(), BaseImageBundle.images.next(), BaseImageBundle.images
        .next(), BaseImageBundle.images.next());
    nextPageImageButton.setSTYLE("commentToolBarButton");
    nextPageImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (pageNumber == lastPageNumber) {
          return;
        }
        pageNumber++;
        fetchPage();
      }
    });
    final IconButton previousPageImageButton = new IconButton(null, false, BaseImageBundle.images.previous(), BaseImageBundle.images.previous(),
        BaseImageBundle.images.previous(), BaseImageBundle.images.previous());
    previousPageImageButton.setSTYLE("commentToolBarButton");
    previousPageImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (pageNumber == 0) {
          return;
        }
        pageNumber--;
        fetchPage();
      }
    });
    final IconButton lastPageImageButton = new IconButton(null, false, BaseImageBundle.images.last(), BaseImageBundle.images.last(), BaseImageBundle.images
        .last(), BaseImageBundle.images.last());
    lastPageImageButton.setSTYLE("commentToolBarButton");
    lastPageImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        pageNumber = (int) lastPageNumber;
        fetchPage();
      }
    });
    final IconButton firstPageImageButton = new IconButton(null, false, BaseImageBundle.images.first(), BaseImageBundle.images.first(), BaseImageBundle.images
        .first(), BaseImageBundle.images.first());
    firstPageImageButton.setSTYLE("commentToolBarButton");
    firstPageImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        pageNumber = 0;
        fetchPage();
      }
    });
    if (lastPageNumber < 0) {
      firstPageImageButton.setEnabled(false);
      previousPageImageButton.setEnabled(false);
      nextPageImageButton.setEnabled(false);
      lastPageImageButton.setEnabled(false);
    }

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    buttonPanel.add(firstPageImageButton);
    buttonPanel.add(previousPageImageButton);
    Label pageLabel = new Label("Page " + (pageNumber + 1) + " of " + (lastPageNumber + 1), false);
    if (lastPageNumber < 0) {
      pageLabel.setText("Page 0 of 0");
    }
    DOM.setStyleAttribute(pageLabel.getElement(), "margin", "0 5px 0 5px");
    buttonPanel.add(pageLabel);
    buttonPanel.add(nextPageImageButton);
    buttonPanel.add(lastPageImageButton);
    return buttonPanel;
  }

  private Widget createButtonPanel(final FlexTable mainPanel, final boolean forceOpen) {
    final IconButton reloadImageButton = new IconButton("Refresh", true, BaseImageBundle.images.refresh_16(), BaseImageBundle.images.refresh_16(),
        BaseImageBundle.images.refresh_16(), BaseImageBundle.images.refresh_disabled_16());
    reloadImageButton.setSTYLE("commentToolBarButton");
    reloadImageButton.setTitle("Refresh comments");
    reloadImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        pageCache.clear();
        fetchPage();
      }
    });

    final IconButton sortImageButton = new IconButton("Sort " + (sortDescending ? "Ascending" : "Descending"), true, BaseImageBundle.images.sort(),
        BaseImageBundle.images.sort(), BaseImageBundle.images.sort(), BaseImageBundle.images.sort());
    sortImageButton.setSTYLE("commentToolBarButton");
    sortImageButton.setTitle(sortDescending ? "Show oldest comments first" : "Show most recent comments first");
    sortImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        sortDescending = !sortDescending;
        // this could be optimized if we have all the pages, then we have all the data
        // we could do it all on the client
        pageCache.clear();
        fetchPage();
      }
    });

    IconButton flattenImageButton = null;
    if (flatten) {
      flattenImageButton = new IconButton("Hierarchy", true, BaseImageBundle.images.hierarchy(), BaseImageBundle.images.hierarchy(), BaseImageBundle.images
          .hierarchy(), BaseImageBundle.images.hierarchy());
      flattenImageButton.setTitle("Build a comment hierarchy");
    } else {
      flattenImageButton = new IconButton("Flatten", true, BaseImageBundle.images.flatten(), BaseImageBundle.images.flatten(),
          BaseImageBundle.images.flatten(), BaseImageBundle.images.flatten());
      flattenImageButton.setTitle("Flatten the comment hierarchy");
    }
    flattenImageButton.setSTYLE("commentToolBarButton");
    flattenImageButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        flatten = !flatten;
        loadCommentWidget(forceOpen);
      }
    });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    buttonPanel.add(createPageControllerPanel(mainPanel));
    Label spacer1 = new Label();
    buttonPanel.add(spacer1);
    buttonPanel.setCellWidth(spacer1, "20px");
    buttonPanel.add(reloadImageButton);
    buttonPanel.add(sortImageButton);
    buttonPanel.add(flattenImageButton);
    Label maxCommentDepthLabel = new Label("Max Depth", false);
    maxCommentDepthLabel.setTitle("Set the maximum depth of comments to show");
    Label spacer2 = new Label();
    buttonPanel.add(spacer2);
    buttonPanel.setCellWidth(spacer2, "20px");
    buttonPanel.add(maxCommentDepthLabel);
    buttonPanel.add(maxCommentDepthListBox);
    return buttonPanel;
  }

  private DisclosurePanel createCommentPostPanel() {
    DisclosurePanel postCommentDisclosurePanel = new DisclosurePanel("Post Comment");
    postCommentDisclosurePanel.setWidth("100%");
    postCommentDisclosurePanel.setOpen(true);
    VerticalPanel postCommentPanel = new VerticalPanel();
    postCommentPanel.setWidth("100%");
    // create text area for comment
    final TextArea commentTextArea = new TextArea();
    commentTextArea.setVisibleLines(3);
    commentTextArea.setWidth("500px");
    // create textfield for email address (if not logged in)
    final TextBox emailTextField = new TextBox();
    emailTextField.setVisibleLength(60);
    // create button panel
    HorizontalPanel buttonPanelWrapper = new HorizontalPanel();
    buttonPanelWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    buttonPanelWrapper.setWidth("500px");
    HorizontalPanel buttonPanel = new HorizontalPanel();
    // create buttons
    final Button submitButton = new Button("Submit");
    submitButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        String commentStr = commentTextArea.getText();
        if (commentStr == null || "".equals(commentStr.trim())) {
          return;
        }
        Comment comment = new Comment();
        comment.setAuthor(AuthenticationHandler.getInstance().getUser());
        comment.setComment(commentStr);
        comment.setPermissibleObject(permissibleObject);
        comment.setEmail(emailTextField.getText());
        submitButton.setEnabled(false);
        submitComment(comment);
      }
    });
    final Button clearButton = new Button("Clear");
    clearButton.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        commentTextArea.setText("");
        submitButton.setEnabled(true);
      }
    });
    // add buttons
    buttonPanel.add(clearButton);
    buttonPanel.add(submitButton);
    buttonPanelWrapper.add(buttonPanel);
    // add panels
    if (AuthenticationHandler.getInstance().getUser() == null) {
      postCommentPanel.add(new Label("Email:"));
      postCommentPanel.add(emailTextField);
    }
    postCommentPanel.add(new Label("Comment:"));
    postCommentPanel.add(commentTextArea);
    postCommentPanel.add(buttonPanelWrapper);
    postCommentDisclosurePanel.setContent(postCommentPanel);
    return postCommentDisclosurePanel;
  }

  private void replyToComment(final Comment parentComment) {
    String replyPromptMessage = "Reply";
    if (parentComment.getAuthor() != null) {
      replyPromptMessage = "Reply To: " + parentComment.getAuthor().getUsername();
    } else if (!StringUtils.isEmpty(parentComment.getEmail())) {
      replyPromptMessage = "Reply To: " + parentComment.getEmail();
    }
    PromptDialogBox dialog = new PromptDialogBox(replyPromptMessage, "Submit", null, "Cancel", false, true);
    dialog.setAllowKeyboardEvents(false);
    VerticalPanel replyPanel = new VerticalPanel();

    final TextArea textArea = new TextArea();
    textArea.setCharacterWidth(60);
    textArea.setVisibleLines(4);
    final TextBox emailTextBox = new TextBox();
    if (AuthenticationHandler.getInstance().getUser() == null) {
      replyPanel.add(new Label("Email:"));
      replyPanel.add(emailTextBox);
    }
    replyPanel.add(textArea);

    dialog.setFocusWidget(textArea);
    dialog.setContent(replyPanel);
    dialog.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        if (textArea.getText() == null || "".equals(textArea.getText())) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Comment is blank.", false, true, true);
          dialog.center();
          return false;
        }
        return true;
      }
    });
    dialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        Comment newComment = new Comment();
        newComment.setAuthor(AuthenticationHandler.getInstance().getUser());
        newComment.setComment(textArea.getText());
        newComment.setPermissibleObject(permissibleObject);
        newComment.setParentComment(parentComment);
        newComment.setEmail(emailTextBox.getText());
        submitComment(newComment);
      }

      public void cancelPressed() {
      }
    });
    dialog.center();
  }

  private List<Comment> sortComments(List<Comment> comments) {
    List<Comment> sortedComments = new ArrayList<Comment>();
    for (Comment comment : comments) {
      if (!sortedComments.contains(comment)) {
        sortedComments.add(comment);
        Comment parentComment = comment.getParentComment();
        Comment previousParentComment = null;
        while (parentComment != null) {
          // insert parents ahead of their child, if not already
          if (previousParentComment != null) {
            if (!sortedComments.contains(parentComment)) {
              sortedComments.add(sortedComments.indexOf(previousParentComment), parentComment);
            }
          } else {
            if (!sortedComments.contains(parentComment)) {
              sortedComments.add(sortedComments.indexOf(comment), parentComment);
            }
          }
          previousParentComment = parentComment;
          parentComment = parentComment.getParentComment();
        }
      }
    }
    return sortedComments;
  }

  private int getCommentDepth(Comment comment) {
    int depth = 0;
    Comment parent = comment.getParentComment();
    while (parent != null) {
      depth++;
      parent = parent.getParentComment();
    }
    return depth;
  }

  private void prefetchPage(int pageNumber) {
    Page<Comment> page = pageCache.get(pageNumber);
    if (page == null && pageNumber >= 0 && pageNumber <= lastPageNumber) {
      BaseServiceCache.getService().getCommentPage(permissibleObject, sortDescending, pageNumber, pageSize, preFetchPageCallback);
    }
  }

  private void prefetchPages() {
    // fetch the first page & last page (for people who jump to beginning/end)
    prefetchPage(0);
    prefetchPage((int) lastPageNumber);
    // try to fetch a few pages before and after the current page and cache them
    prefetchPage(pageNumber + 1);
    prefetchPage(pageNumber + 2);
    prefetchPage(pageNumber + 3);
    prefetchPage(pageNumber + 4);
    prefetchPage(pageNumber + 5);
    prefetchPage(pageNumber - 1);
    prefetchPage(pageNumber - 2);
    prefetchPage(pageNumber - 3);
    prefetchPage(pageNumber - 4);
    prefetchPage(pageNumber - 5);
  }

  private void fetchPage() {
    if (paginate) {
      Page<Comment> page = pageCache.get(pageNumber);
      if (page != null) {
        pageCallback.onSuccess(page);
      } else {
        BaseServiceCache.getService().getCommentPage(permissibleObject, sortDescending, pageNumber, pageSize, pageCallback);
      }
    } else {
      BaseServiceCache.getService().getComments(permissibleObject, allCommentsCallback);
    }
  }

  private void submitComment(Comment comment) {
    BaseServiceCache.getService().submitComment(comment, submitCommentCallback);
  }

  private void approveComment(Comment comment) {
    BaseServiceCache.getService().approveComment(comment, approveCallback);
  }

  private void deleteComment(Comment comment) {
    BaseServiceCache.getService().deleteComment(comment, deleteCommentCallback);
  }

  public boolean isFlatten() {
    return flatten;
  }

  public void setFlatten(boolean flatten) {
    this.flatten = flatten;
    loadCommentWidget(true);
  }
}
