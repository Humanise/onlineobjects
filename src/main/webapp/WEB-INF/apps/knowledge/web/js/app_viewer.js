var documentController = {

  text: undefined,

  widgets : {
    selectionPanel: null,
    multipleSelectionPanel: null,
    tagSelectedText: null
  },
  $ready : function() {
    this.root = hui.find('.js-internetaddress-formatted');
    this.widgets.selectionPanel = hui.ui.get('selectionPanel');
    this.widgets.tagSelectedText = hui.ui.get('tagSelectedText');
    this.widgets.multipleSelectionPanel = hui.ui.get('multipleSelectionPanel');
    hui.listen(this.root,'click',this._click.bind(this));
    this._listenForText();
  },
  $$afterResize : function() {
    this._adjustSelectionPanel();
  },
  reset: function() {
    if (this.widgets.selectionPanel) {
      this.widgets.selectionPanel.hide();
      this.widgets.multipleSelectionPanel.hide();
    }
  },
  $scrolled$mainScroller : function() {
    this._adjustSelectionPanel();
  },

  _click : function(e) {
    e = hui.event(e);
    var items = this._findClickedItems(e);
    if (items.length == 1) {
      e.stop();
      this._inspectItem(items[0]);
    }
    else if (items.length > 1) {
      e.stop();
      var panel = this.widgets.multipleSelectionPanel;
      hui.ui.get('multipleSelectionItems').setData(items);
      panel.show({target: e.getElement()});
    }
  },
  $render$multipleSelectionItems : function(item) {
    return hui.build('div.multiple_selection_item', {text: item.info.description});
  },
  $select$multipleSelectionItems : function(e) {
    this._inspectItem(e.data);
    this.widgets.multipleSelectionPanel.hide();
  },
  _inspectItem : function(item) {
    if (['Statement'].indexOf(item.info.type) !== -1) {
      appController.show(item.info);
    } else if (item.info.type == 'Link') {
      window.open(item.info.url);
    }
  },

  _adjustSelectionPanel : function() {
    var panel = this.widgets.selectionPanel;
    if (panel && panel.isVisible()) {
      this._respondToTextSelection();
    }  
  },
  
  _respondToTextSelection : function() {
    var selection = document.getSelection();
    this.text = '';
    var panel = this.widgets.selectionPanel;
    if (selection.type == 'Range' && selection.rangeCount == 1) {
      var range = selection.getRangeAt(0);
      var common = range.commonAncestorContainer;
      if (hui.dom.isDescendantOrSelf(common,this.root)) {
        this.text = hui.selection.getText().trim();
        if (this.text) {
          //this._suggest();
          var rects = range.getClientRects();
          if (rects.length > 0) {
            panel.show({target: rects[0]});
            return;
          }          
        }
      }
    }
    panel.hide();
  },

  _listenForText : function() {
    var textListener = this._respondToTextSelection.bind(this);
    var timer;
    var later = function() {
      clearTimeout(timer);
      timer = setTimeout(textListener, 100);
    }
    hui.listen(document.body,'mouseup',later);
    hui.listen(window,'keyup',later);
  },

  _suggest : function() {
    if (!this.text) { return; }
    hui.ui.request({
      url: '/app/suggest',
      parameters: {text: this.text},
      $object: function(data) {
        console.log(data[0]);
      },
    });
  },
  _findClickedItems : function(e) {
    var found = [],
      p = e.element

    while (p) {
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
  $click$highlightSelection : function(button) {
    var warn;
    if (this.text.length < 10) { warn = 'Really create such a short quote?' }
    if (warn) {
      hui.ui.confirmOverlay({
        target: button,
        text: warn,
        modal: true,
        $ok : this._createStatement.bind(this)
      });
    } else {
      this._createStatement();
    }
  },
  _createStatement : function() {
    document.getSelection().removeAllRanges();
    appController.createStatementOnAddress(this.text);
    this.widgets.selectionPanel.hide();
  },
  $click$tagSelectedText : function(button) {
    var warn;
    if (this.text.length > 50) { warn = 'Really create such a long tag?' }
    if (this.text.length < 2) { warn = 'Really create such a short tag?' }
    if (warn) {
      hui.ui.confirmOverlay({
        target: button,
        text: warn,
        modal: true,
        $ok : this._tagSelectedText.bind(this)
      });
    } else {
      this._tagSelectedText();
    }
  },
  _tagSelectedText : function() {
    document.getSelection().removeAllRanges();
    appController.createTagOnCurrentItem(this.text);
    this.widgets.selectionPanel.hide();
  }
};

hui.ui.listen(documentController);