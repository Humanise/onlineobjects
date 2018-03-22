var internetAddressViewer = {

  name : 'internetAddressViewer',

  _viewedItem : null,
  _currentArticle : null,
  text : '',

  nodes : {
    viewer : '.js-internetaddress',
    content : '.js-viewer-content',
    meta : '.js-internetaddress-meta',
    title: '.js-viewer-title',
    link: '.js-viewer-link',
    formatted: '.js-viewer-formatted',
    footer: '.js-viewer-footer',
    text: '.js-viewer-text',
    frame: '.js-viewer-frame'
  },

  widgets : {
    selectionPanel : null
  },

  $ready : function() {

    this.nodes = hui.collect(this.nodes);

    this.viewerSpinner = hui.get('viewer_spinner');

    this.widgets.selectionPanel = hui.ui.get('selectionPanel');

    hui.listen(this.nodes.viewer,'click',this._click.bind(this));
    hui.listen(this.nodes.meta,'click',this._clickInfo.bind(this));
    this._listenForText();

    this.widgets.viewSelection = new oo.Segmented({
      name : 'internetAddressViewSelection',
      element : hui.find('.js_internetaddress_viewselector'),
      selectedClass : 'is-selected',
      value : 'formatted'
    });
  },

  _listenForText : function() {
    var textListener = function() {
      var selection = document.getSelection();
      this.text = '';
      var panel = this.widgets.selectionPanel;
      if (selection.type == 'Range' && selection.rangeCount == 1) {
        var range = selection.getRangeAt(0);
        var common = range.commonAncestorContainer;
        if (hui.dom.isDescendantOrSelf(common,this.nodes.content)) {
          this.text = hui.selection.getText();
          var rects = range.getClientRects();
          if (rects.length > 0) {
            panel.position({
              rect : rects[0],
              position : 'vertical'
            });
            panel.show();
            return;
          }
        }
      }
      panel.hide();
    }.bind(this);
    hui.listen(document.body,'mouseup',textListener);
    hui.listen(window,'keyup',textListener);
  },

  _click : function(e) {

    e = hui.event(e);
    var items = this._findClickedItems(e);
    if (items.length > 0 && !this.text) {
      e.stop();
      hui.log(items);
      var first = items[0]
      if (e.altKey && first.info.type == 'Statement') {
        statementController.edit(first.info.id);
      } else {
        reader.peek({items:items,context:{type:'InternetAddress',id:this._viewedItem.id}});
      }
      return;
    } else {
      var action = e.findByClass('js_reader_action');
      if (action) {
        var data = hui.string.fromJSON(action.getAttribute('data'));
        if (data) {
          e.stop();
          this._performAction(data);
        }
      }
    }
  },
  _performAction : function(data) {
    if (data.action == 'highlightStatement') {
      this._highlightStatement(data.id);
    }
    else if (data.action == 'editStatement') {
      reader.edit({type:'Statement',id:data.id});
    }
    else if (data.action == 'viewInternetAddress') {
      reader.view({type:'InternetAddress', id: data.id});
    }
    else if (data.action == 'openUrl') {
      window.open(data.url);
    }
  },
  _findClickedItems : function(e) {
    var found = [],
      p = e.element,
      viewer = this.nodes.viewer

    while (p && p != viewer) {
      if (hui.cls.has(p,'js_reader_item')) {
        found.push({
          node : p,
          info : hui.string.fromJSON(p.getAttribute('data-info'))
        });
      }
      p = p.parentNode;
    }
    return found;
  },

  _clickInfo : function(e) {

    e = hui.event(e);
    var a = e.findByTag('a');
    if (a) {
      e.stop();
      if (hui.cls.has(a,'is-add')) {
        var finder = oo.WordFinder.get();
        finder.show();
        finder.setSearch(this.text);
      }
      else if (hui.cls.has(a,'is-word')) {
        this._clickWord(a);
      }
      else if (hui.cls.has(a,'is-tag')) {
        this._clickTag(a);
      }
    }
  },

  $statementChanged$statementEditor : function() {
    this.reload();
  },

  $wordChanged : function() {
    this.reload();
  },

  $valueChanged$extractionAlgorithm : function() {
    this.reload();
  },
  $valueChanged$highlightRendering : function() {
    this.reload();
  },

  reload : function() {
    if (this._viewedItem) {
      this.show({
        id : this._viewedItem.id,
        reload : true
      });
    }
  },

  show: function(object) {
    if (!object || !object.id) {
      return;
    }
    if (this._viewedItem) {
      if (this._viewedItem.id == object.id) {
        if (object.statementId) {
          this._highlightStatement(object.statementId);
          return;
        }
      } else {
        hui.log('New object - resetting');
        this.nodes.formatted.innerHTML = '';
        this.nodes.text.innerHTML = '';
        this.nodes.title.innerHTML = '';
        this.nodes.meta.innerHTML = '';
        this.nodes.footer.innerHTML = '';
        this.nodes.footer.style.display = 'none';
        this.nodes.content.scrollTop = 0;
        this._markInbox(false);
        this._markFavorite(false);
      }
    } else {
      hui.log('No viewed item');
    }
    addressInfoController.clear();
    this._viewedItem = {id : object.id};
    this._lockViewer();
    hui.dom.setText(this.nodes.title, hui.string.escape(object.title || 'Loading...'));
    this.nodes.viewer.style.display = 'block';
    this.viewerVisible = true;
    hui.cls.add(this.viewerSpinner, 'oo_spinner_visible');
    var parameters = {
      id : object.id,
      algorithm : hui.ui.get('extractionAlgorithm').getValue(),
      highlight : hui.ui.get('highlightRendering').getValue()
    };

    var self = this;
    hui.ui.request({
      url: '/loadArticle',
      parameters: parameters,
      $object: function(article) {
        self._drawArticle(article);
        if (object.statementId) {
          window.setTimeout(function() {
            self._highlightStatement(object.statementId);
          },500)
        }
      },
      $failure: function() {
        self.hide();
        hui.ui.msg.fail({
          text: 'Sorry!'
        });
      },
      $finally: function() {
        self._unlockViewer();
        hui.cls.remove(self.viewerSpinner, 'oo_spinner_visible');
      }
    });
  },

  _markInbox : function(checked) {
    var link = hui.ui.get('inboxButton');
    link.setSelected(checked);
  },

  _markFavorite : function(checked) {
    var link = hui.ui.get('favoriteButton');
    link.setSelected(checked);
  },
  _formatText : function(text) {
    // TODO This should use split in order to handle HTML in the text
    text = hui.string.escape(text || '');
    // TODO: This cleanup should be done server side
    return '<p>' + text.trim().replace(/[ \xa0]*[\n][ \xa0]*/g,'\n').replace(/[\n]{2,}/g,'</p><p>').replace(/\n/g,'<br/>') + '</p>';
  },

  _drawArticle : function(article) {
    this._currentArticle = article;
    this.nodes.formatted.innerHTML = '<div class="reader_text">' + article.formatted + '</div>';
    this.nodes.footer.innerHTML = '';
    hui.each(article.quotes, function(quote) {
      var node = hui.build('div',{
        text : quote.text,
        parent : this.nodes.footer,
        'class' : 'reader_view_quote js_reader_action',
        data : hui.string.toJSON({"action":"highlightStatement","id": quote.id})
      });
      if (!quote.found) {
        hui.cls.add(node, 'is-not-found');
      }
      hui.build('span',{
        'class' : 'oo_icon oo_icon_info_light reader_view_quote_icon js_reader_action',
        'data' : hui.string.toJSON({action:'editStatement', id: quote.id}),
        parent: node
      })
    }.bind(this));
    hui.each(article.similar, function(other) {
      hui.build('div',{
        'class' : 'reader_view_quote js_reader_action',
        'data' : hui.string.toJSON({action:'viewInternetAddress', id: other.entity.id}),
        text: (other.entity.name || 'No title') + " ~ " + (Math.round(other.similarity*1000)/10)+"%",
        parent: this.nodes.footer
      })
    }.bind(this));
    this.nodes.footer.style.display = this.nodes.footer.childNodes.length ? 'block' : 'none';
    this.nodes.text.innerHTML = this._formatText(article.text);
    hui.dom.setText(this.nodes.title, article.title);
    hui.dom.setText(this.nodes.link, article.urlText);
    this.nodes.link.setAttribute('href', article.url);
    this.nodes.meta.innerHTML = article.info;
    this._markInbox(article.inbox);
    this._markFavorite(article.favorite);
    var view = this.widgets.viewSelection.getValue();
    if (view === 'web') {
      // TODO Find a way to handle errors
      this.nodes.frame.setAttribute('src',article.url);
    }
    this.frameSet = view === 'web';
  },

  _highlightStatement : function(id) {
    if (id==null || id==undefined) {
      return;
    }
    var marks = document.querySelectorAll('mark[data-id="' + id + '"]');
    if (marks.length==0) {
      return;
    }
    var mark = marks[0];
    var content = this.nodes.content;
    var top = content.clientHeight / -2;
    var parent = mark.parentNode;
    while (parent && parent!==content) {
      top += parent.offsetTop;
      parent = parent.offsetParent;
    }
    top = Math.max(0, Math.round(top));
    var dur = Math.min(1500,Math.abs(top - content.scrollTop));

    var blink = function(node) {
      hui.cls.add(node,'is-highlighted');
      window.setTimeout(function() {
        hui.cls.remove(node,'is-highlighted');
      },600);
    }

    hui.animate({
      node : content,
      property : 'scrollTop',
      value : top,
      duration : dur,
      ease : hui.ease.slowFastSlow,
      $complete : function() {
        for (var i = 0; i < marks.length; i++) {
          blink(marks[i]);
        }
      }
    });
  },

  _editStatement : function(options) {
    statementController.edit(options.link.getAttribute('data-id'));
  },

  $click$closeAddress : function() {
    this.hide();
  },

  $addressWillBeDeleted$addressEditor : function() {
    this._lockViewer();
  },

  $addressWasDeleted$addressEditor : function() {
    this.hide();
  },

  $addressChanged$addressEditor : function() {
    this.reload();
  },

  $hypothesisChanged : function() {
    this.reload();
  },

  hide : function() {
    this._unlockViewer();
    this.nodes.viewer.style.display='none';
    this.viewerVisible = false;
    this.nodes.frame.src = "about:blank";
    this.nodes.meta.innerHTML = ''
    this._viewedItem = null;
    addressInfoController.clear();
    reader.PeekController.hide();
  },
  _lockViewer : function() {
    this._locked = true;
    hui.cls.add(this.nodes.viewer,'is-locked');
  },
  _unlockViewer : function() {
    this._locked = false;
    hui.cls.remove(this.nodes.viewer,'is-locked');
  },
  $click$favoriteButton : function() {
    if (this._locked) {return}
    this._lockViewer();
    var newValue = !this._currentArticle.favorite;
    this._markFavorite(newValue);
    hui.ui.request({
      url : '/changeFavoriteStatus',
      parameters : {id:this._currentArticle.id,favorite:newValue,type:'InternetAddress'},
      $success : function() {
        this._currentArticle.favorite = newValue;
        hui.ui.get('listSource').refresh();
        hui.ui.msg.success({text:newValue ? 'Added to favorites' : 'Removed from favorites'});
      }.bind(this),
      $finally : function() {
        this._unlockViewer();
      }.bind(this)
    });
  },
  $click$inboxButton : function() {
    if (this._locked) {return}
    this._lockViewer();
    var newValue = !this._currentArticle.inbox;
    this._markInbox(newValue);
    hui.ui.request({
      url : '/changeInboxStatus',
      parameters : {id:this._currentArticle.id,inbox:newValue,type:'InternetAddress'},
      $success : function() {
        this._currentArticle.inbox = newValue;
        hui.ui.get('listSource').refresh();
        hui.ui.msg.success({text:newValue ? 'Added to inbox' : 'Removed from inbox'});
      }.bind(this),
      $finally : function() {
        this._unlockViewer();
      }.bind(this)
    });
  },

  $click$inspectButton : function() {
    oo.Inspector.inspect({id:this._currentArticle.id})
  },

  $click$infoButton : function() {
    if (!this._currentArticle) {
      return;
    }
    addressInfoController.edit(this._currentArticle);
  },

  $click$analyzeButton : function() {
    if (!this._currentArticle) {
      return;
    }
    window.open('/en/analyze?id=' + this._currentArticle.id);
  },

  $click$quoteFromSelection : function() {
    if (hui.isBlank(this.text)) {
      return;
    }
    var parameters = {
      id : this._currentArticle.id,
      text : this.text
    }
    this._lockViewer();
    var panel = this.widgets.selectionPanel;
    hui.ui.request({
      url : '/addQuote',
      parameters : parameters,
      $success : function() {
        this.reload();
        hui.ui.callDelegates(this,'statementChanged');
      }.bind(this),
      $finally : function() {
        panel.hide();
      }.bind(this)
    })
  },
  $click$hypothesisFromSelection : function() {
    if (hui.isBlank(this.text)) {
      return;
    }
    var parameters = {
      id : this._currentArticle.id,
      text : this.text
    }
    this._lockViewer();
    var panel = this.widgets.selectionPanel;
    hui.ui.request({
      url : '/addHypothesis',
      parameters : parameters,
      $success : function() {
        this.reload();
        hui.ui.callDelegates(this,'hypothesisChanged');
      }.bind(this),
      $finally : function() {
        panel.hide();
      }.bind(this)
    });
  },
  $click$personFromSelection : function() {
    if (hui.isBlank(this.text)) {
      return;
    }
    var parameters = {
      id : this._currentArticle.id,
      text : this.text
    }
    this._lockViewer();
    var panel = this.widgets.selectionPanel;
    hui.ui.request({
      url : '/addPerson',
      parameters : parameters,
      $success : function() {
        this.reload();
        hui.ui.callDelegates(this,'addressChanged');
      }.bind(this),
      $finally : function() {
        panel.hide();
      }.bind(this)
    });
  },
  $click$tagFromSelection : function() {
    var finder = oo.WordFinder.get();
    finder.show();
    finder.setSearch(this.text);
  },
  $click$searchFromSelection : function() {
    reader.search({text:this.text});
  },

  $valueChanged$internetAddressViewSelection : function(value) {

    this.nodes.content.className = 'reader_view_content is-show-'+value;
    if (!this.frameSet && value === 'web') {
      this.nodes.frame.src = this._currentArticle.url;
      this.frameSet = true;
    }
  },

  // Words...

  $found$wordFinder : function(obj) {
    var p = {
      internetAddressId : this._viewedItem.id,
      wordId : obj.id
    }
    hui.ui.request({
      url : '/addWord',
      parameters : p,
      $success : function() {
        this.reload();
        hui.ui.get('tagSource').refresh();
        hui.ui.get('listSource').refresh();
      }.bind(this)
    })
  },

  _activeWordId : null,

  _clickWord : function(node) {

    reader.peek({
      item:{
        info : {
          id : node.getAttribute('data'),
          type: 'Word'
        },
        node: node
      },
      context:{type:'InternetAddress',id:this._viewedItem.id}
    });
  }
}
hui.ui.listen(internetAddressViewer);

var addressSelection = {
  _check : function() {
    var selection = window.getSelection();
    if (selection.rangeCount<1) {return}
  }
}
hui.ui.listen(addressSelection);