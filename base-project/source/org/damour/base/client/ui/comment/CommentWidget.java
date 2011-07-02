package org.damour.base.client.ui.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.buttons.IconButton;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.utils.CursorUtils;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
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
  private List<PermissibleObject> comments;
  private boolean sortDescending = true;
  private boolean flatten = false;
  private ListBox maxCommentDepthListBox = new ListBox(false);

  private Comment workingOnComment;

  private ICommentLayoutComplete layoutCallback;
  private int pageNumber = 0;
  private int pageSize = 10;
  private long numComments = 0;
  private long lastPageNumber = 0;
  private boolean paginate = true;

  HashMap<Integer, Page<PermissibleObject>> pageCache = new HashMap<Integer, Page<PermissibleObject>>();

  private AsyncCallback<Boolean> deleteCommentCallback = new AsyncCallback<Boolean>() {

    public void onSuccess(Boolean result) {
      BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), pageSize, new AsyncCallback<PageInfo>() {
        public void onFailure(Throwable caught) {
        };

        public void onSuccess(PageInfo pageInfo) {
          numComments = pageInfo.getTotalRowCount();
          lastPageNumber = pageInfo.getLastPageNumber();
          while (pageNumber > lastPageNumber) {
            pageNumber--;
          }
          pageCache.clear();
          fetchPage();
        };
      });
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
      BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), pageSize, new AsyncCallback<PageInfo>() {
        public void onFailure(Throwable caught) {
        };

        public void onSuccess(PageInfo pageInfo) {
          numComments = pageInfo.getTotalRowCount();
          lastPageNumber = pageInfo.getLastPageNumber();
          while (pageNumber > lastPageNumber) {
            pageNumber--;
          }
          pageCache.clear();
          fetchPage();
        };
      });
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };
  private AsyncCallback<Page<PermissibleObject>> pageCallback = new AsyncCallback<Page<PermissibleObject>>() {

    public void onSuccess(Page<PermissibleObject> page) {
      pageCache.put(page.getPageNumber(), page);
      comments = page.getResults();
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

  private AsyncCallback<List<PermissibleObject>> allCommentsCallback = new AsyncCallback<List<PermissibleObject>>() {
    public void onSuccess(List<PermissibleObject> comments) {
      CursorUtils.setDefaultCursor(CommentWidget.this);
      CommentWidget.this.comments = comments;
      numComments = comments.size();
      loadCommentWidget(true);
    }

    public void onFailure(Throwable caught) {
      CursorUtils.setDefaultCursor(CommentWidget.this);
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };

  private AsyncCallback<Page<PermissibleObject>> preFetchPageCallback = new AsyncCallback<Page<PermissibleObject>>() {

    public void onSuccess(Page<PermissibleObject> page) {
      pageCache.put(page.getPageNumber(), page);
    }

    public void onFailure(Throwable caught) {
      MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
      dialog.center();
    }
  };

  public CommentWidget(final PermissibleObject permissibleObject, final List<PermissibleObject> comments, ICommentLayoutComplete layoutCallback,
      boolean paginate, int pageSize) {
    this.permissibleObject = permissibleObject;
    this.comments = comments;
    this.paginate = paginate;
    this.pageSize = pageSize;
    this.layoutCallback = layoutCallback;

    maxCommentDepthListBox.addItem("None", "999999");
    maxCommentDepthListBox.addItem("1");
    maxCommentDepthListBox.addItem("2");
    maxCommentDepthListBox.addItem("3");
    maxCommentDepthListBox.addItem("4");
    maxCommentDepthListBox.addItem("5");
    maxCommentDepthListBox.setSelectedIndex(0);
    maxCommentDepthListBox.addChangeHandler(new ChangeHandler() {

      public void onChange(ChangeEvent event) {
        loadCommentWidget(true);
      }
    });

    add(new Label("Loading..."));

    Timer t = new Timer() {
      public void run() {
        if (comments == null) {
          BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), CommentWidget.this.pageSize, new AsyncCallback<PageInfo>() {
            public void onFailure(Throwable caught) {
            };

            public void onSuccess(PageInfo pageInfo) {
              numComments = pageInfo.getTotalRowCount();
              lastPageNumber = pageInfo.getLastPageNumber();
              fetchPage();
            };
          });
        } else {
          numComments = comments.size();
          if (CommentWidget.this.paginate) {
            // make pages
            lastPageNumber = new Double(Math.floor((double) (numComments - 1) / CommentWidget.this.pageSize)).longValue();

            int currPage = -1;
            List<PermissibleObject> sortedComments = sortComments(comments);
            for (int i = 0; i < numComments; i++) {
              if (i % CommentWidget.this.pageSize == 0) {
                currPage++;
              }
              if (pageCache.get(currPage) == null) {
                pageCache.put(currPage, new Page<PermissibleObject>());
              }
              Page<PermissibleObject> page = pageCache.get(currPage);
              page.setPageNumber(currPage);
              page.getResults().add(sortedComments.get(i));
            }
            if (pageCache.size() > 0) {
              CommentWidget.this.comments = pageCache.get(0).getResults();
            }
          }
          loadCommentWidget(true);
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
          && (AuthenticationHandler.getInstance().getUser().isAdministrator() || AuthenticationHandler.getInstance().getUser()
              .equals(permissibleObject.getOwner()));
      List<PermissibleObject> sortedComments = new ArrayList<PermissibleObject>();
      if (comments != null) {
        sortedComments.addAll(comments);
      }
      if (!flatten) {
        sortedComments = sortComments(sortedComments);
      }

      for (PermissibleObject obj : sortedComments) {
        final Comment comment = (Comment) obj;
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
          replyCommentImage.setResource(BaseImageBundle.images.reply());
          replyCommentImage.setStyleName("commentActionButton");
          replyCommentImage.setTitle("Reply to this comment");
          replyCommentImage.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
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
              approveCommentImage.setResource(BaseImageBundle.images.approve());
              approveCommentImage.setStyleName("commentActionButton");
              approveCommentImage.setTitle("Approve comment");
              approveCommentImage.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
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
              approveSpacerImage.setResource(BaseImageBundle.images.empty16x16());
              approveSpacerImage.setStyleName("commentActionButton");
              commentHeaderPanel.setWidget(0, columnIndex, approveSpacerImage);
              commentHeaderPanel.getFlexCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
              columnIndex++;
            }
            Image deleteCommentImage = new Image();
            deleteCommentImage.setResource(BaseImageBundle.images.delete());
            deleteCommentImage.setStyleName("commentActionButton");
            deleteCommentImage.setTitle("Remove comment");
            deleteCommentImage.addClickHandler(new ClickHandler() {

              public void onClick(ClickEvent event) {
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

          // Label commentLabel = new Label(comment.getId() + " " + comment.getComment(), true);
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
    if (layoutCallback != null) {
      layoutCallback.layoutComplete();
    }
  }

  private Widget createPageControllerPanel(final FlexTable mainPanel) {
    final IconButton nextPageImageButton = new IconButton(null, true, BaseImageBundle.images.next(), BaseImageBundle.images.next(),
        BaseImageBundle.images.next(), BaseImageBundle.images.next());
    nextPageImageButton.setSTYLE("commentToolBarButton");
    nextPageImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
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
    previousPageImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (pageNumber == 0) {
          return;
        }
        pageNumber--;
        fetchPage();
      }
    });
    final IconButton lastPageImageButton = new IconButton(null, false, BaseImageBundle.images.last(), BaseImageBundle.images.last(),
        BaseImageBundle.images.last(), BaseImageBundle.images.last());
    lastPageImageButton.setSTYLE("commentToolBarButton");
    lastPageImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        pageNumber = (int) lastPageNumber;
        fetchPage();
      }
    });
    final IconButton firstPageImageButton = new IconButton(null, false, BaseImageBundle.images.first(), BaseImageBundle.images.first(),
        BaseImageBundle.images.first(), BaseImageBundle.images.first());
    firstPageImageButton.setSTYLE("commentToolBarButton");
    firstPageImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
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
      pageLabel.setText("Page 1 of 1");
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
    reloadImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), pageSize, new AsyncCallback<PageInfo>() {
          public void onFailure(Throwable caught) {
          };

          public void onSuccess(PageInfo pageInfo) {
            numComments = pageInfo.getTotalRowCount();
            lastPageNumber = pageInfo.getLastPageNumber();
            pageCache.clear();
            fetchPage();
          };
        });
      }
    });

    final IconButton sortImageButton = new IconButton("Sort " + (sortDescending ? "Ascending" : "Descending"), true, BaseImageBundle.images.sort(),
        BaseImageBundle.images.sort(), BaseImageBundle.images.sort(), BaseImageBundle.images.sort());
    sortImageButton.setSTYLE("commentToolBarButton");
    sortImageButton.setTitle(sortDescending ? "Show oldest comments first" : "Show most recent comments first");
    sortImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        sortDescending = !sortDescending;
        // this could be optimized if we have all the pages, then we have all the data
        // we could do it all on the client
        pageCache.clear();
        fetchPage();
      }
    });

    IconButton flattenImageButton = null;
    if (flatten) {
      flattenImageButton = new IconButton("Hierarchy", true, BaseImageBundle.images.hierarchy(), BaseImageBundle.images.hierarchy(),
          BaseImageBundle.images.hierarchy(), BaseImageBundle.images.hierarchy());
      flattenImageButton.setTitle("Build a comment hierarchy");
    } else {
      flattenImageButton = new IconButton("Flatten", true, BaseImageBundle.images.flatten(), BaseImageBundle.images.flatten(),
          BaseImageBundle.images.flatten(), BaseImageBundle.images.flatten());
      flattenImageButton.setTitle("Flatten the comment hierarchy");
    }
    flattenImageButton.setSTYLE("commentToolBarButton");
    flattenImageButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
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
    submitButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String commentStr = commentTextArea.getText();
        if (commentStr == null || "".equals(commentStr.trim())) {
          return;
        }
        Comment comment = new Comment();
        comment.setGlobalRead(true);
        comment.setOwner(permissibleObject.getOwner());
        comment.setAuthor(AuthenticationHandler.getInstance().getUser());
        comment.setComment(commentStr);
        comment.setParent(permissibleObject);
        comment.setEmail(emailTextField.getText());
        submitButton.setEnabled(false);
        submitComment(comment);
      }
    });
    final Button clearButton = new Button("Clear");
    clearButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
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
        newComment.setGlobalRead(true);
        newComment.setOwner(permissibleObject.getOwner());
        newComment.setAuthor(AuthenticationHandler.getInstance().getUser());
        newComment.setComment(textArea.getText());
        newComment.setParent(permissibleObject);
        newComment.setParentComment(parentComment);
        newComment.setEmail(emailTextBox.getText());
        submitComment(newComment);
      }

      public void cancelPressed() {
      }
    });
    dialog.center();
  }

  private List<PermissibleObject> sortComments(List<PermissibleObject> comments) {
    List<Comment> commentSet = new ArrayList<Comment>();

    for (PermissibleObject obj : comments) {
      Comment comment = (Comment) obj;
      commentSet.add(comment);
      Comment parent = comment.getParentComment();
      do {
        if (parent != null) {
          if (!commentSet.contains(parent)) {
            commentSet.add(parent);
          }
          parent = parent.getParentComment();
        }
      } while (parent != null);
    }

    // create bare nodes for each element
    PermissibleObjectTreeNode rootNode = new PermissibleObjectTreeNode();

    for (Comment comment : commentSet) {
      PermissibleObjectTreeNode node = new PermissibleObjectTreeNode();
      node.setObject(comment);
      if (comment.getParentComment() == null) {
        if (!rootNode.getChildren().containsKey(comment)) {
          rootNode.getChildren().put(comment, node);
        }
      } else {
        // try to find parent in rootNode tree
        PermissibleObjectTreeNode parent = findParentCommentNode(rootNode, node);
        if (parent == null) {
          // this nodes parent cannot be found, let's add all of his parents
          List<Comment> parentComments = new ArrayList<Comment>();
          Comment parentComment = comment.getParentComment();
          do {
            if (parentComment != null) {
              parentComments.add(parentComment);
              parentComment = parentComment.getParentComment();
            }
          } while (parent != null);
          if (parentComments.size() == 0) {
            rootNode.getChildren().put(comment, node);
          } else {
            // reverse the order of the list and add/find existing parents
            Collections.reverse(parentComments);
            for (Comment myParentComment : parentComments) {
              PermissibleObjectTreeNode myParentCommentNode = new PermissibleObjectTreeNode();
              myParentCommentNode.setObject(myParentComment);
              PermissibleObjectTreeNode parentParent = findParentCommentNode(rootNode, myParentCommentNode);
              if (parentParent == null) {
                rootNode.getChildren().put(myParentComment, myParentCommentNode);
              } else {
                if (!parentParent.getChildren().containsKey(myParentComment)) {
                  parentParent.getChildren().put(myParentComment, myParentCommentNode);
                }
              }
            }
            // we better find it now
            parent = findParentCommentNode(rootNode, node);
            parent.getChildren().put(comment, node);
          }
        } else {
          if (!parent.getChildren().containsKey(comment)) {
            parent.getChildren().put(comment, node);
          }
        }
      }
    }

    // march the tree
    ArrayList<PermissibleObject> list = new ArrayList<PermissibleObject>();
    marchTree(list, rootNode);

    return list;
  }

  private void marchTree(List<PermissibleObject> comments, PermissibleObjectTreeNode node) {
    if (node.getObject() != null) {
      comments.add(node.getObject());
    }

    // let's sort the children so the list makes sense
    List<PermissibleObject> permissibleObjects = new ArrayList<PermissibleObject>(node.getChildren().keySet());
    Collections.sort(permissibleObjects);

    for (PermissibleObject object : permissibleObjects) {
      marchTree(comments, node.getChildren().get(object));
    }
  }

  private PermissibleObjectTreeNode findParentCommentNode(PermissibleObjectTreeNode rootNode, PermissibleObjectTreeNode node) {
    if (node.getObject().equals(rootNode.getObject())) {
      return rootNode;
    }
    for (PermissibleObject obj : rootNode.getChildren().keySet()) {
      if (obj.equals(((Comment) node.getObject()).getParentComment())) {
        return rootNode.getChildren().get(obj);
      }
      PermissibleObjectTreeNode possibleParent = findParentCommentNode(rootNode.getChildren().get(obj), node);
      if (possibleParent != null) {
        return possibleParent;
      }
    }
    return null;
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
    Page<PermissibleObject> page = pageCache.get(pageNumber);
    if (page == null && pageNumber >= 0 && pageNumber <= lastPageNumber) {
      BaseServiceCache.getService().getPage(permissibleObject, Comment.class.getName(), "id", sortDescending, pageNumber, pageSize, preFetchPageCallback);
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
      Page<PermissibleObject> page = pageCache.get(pageNumber);
      if (page != null) {
        pageCallback.onSuccess(page);
      } else {
        BaseServiceCache.getService().getPage(permissibleObject, Comment.class.getName(), "id", sortDescending, pageNumber, pageSize, pageCallback);
      }
    } else {
      CursorUtils.setBusyCursor(CommentWidget.this);
      BaseServiceCache.getService().getPermissibleObjects(permissibleObject, Comment.class.getName(), allCommentsCallback);
    }
  }

  private void submitComment(final Comment comment) {
    CursorUtils.setBusyCursor(this);
    BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), pageSize, new AsyncCallback<PageInfo>() {
      public void onFailure(Throwable caught) {
        CursorUtils.setDefaultCursor(CommentWidget.this);
      }

      public void onSuccess(PageInfo pageInfo) {
        CursorUtils.setDefaultCursor(CommentWidget.this);
        numComments = pageInfo.getTotalRowCount();
        lastPageNumber = pageInfo.getLastPageNumber();
        BaseServiceCache.getService().submitComment(comment, submitCommentCallback);
      }
    });
  }

  private void approveComment(Comment comment) {
    BaseServiceCache.getService().approveComment(comment, approveCallback);
  }

  private void deleteComment(final Comment comment) {
    BaseServiceCache.getService().getPageInfo(permissibleObject, Comment.class.getName(), pageSize, new AsyncCallback<PageInfo>() {
      public void onFailure(Throwable caught) {
      };

      public void onSuccess(PageInfo pageInfo) {
        numComments = pageInfo.getTotalRowCount();
        lastPageNumber = pageInfo.getLastPageNumber();
        BaseServiceCache.getService().deleteComment(comment, deleteCommentCallback);
      };
    });
  }

  public boolean isFlatten() {
    return flatten;
  }

  public void setFlatten(boolean flatten) {
    this.flatten = flatten;
    loadCommentWidget(true);
  }
}
