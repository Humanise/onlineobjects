var documentController = {

  text: undefined,

  widgets : {
    selectionPanel: null,
    multipleSelectionPanel: null
  },
  $ready : function() {
    this.root = hui.find('.js-internetaddress-formatted');
    this.widgets.selectionPanel = hui.ui.get('selectionPanel');
    this.widgets.multipleSelectionPanel = hui.ui.get('multipleSelectionPanel');
    hui.listen(this.root,'click',this._click.bind(this));
    this._listenForText();
  },

  reset: function() {
    if (this.widgets.selectionPanel) {
      this.widgets.selectionPanel.hide();
      this.widgets.multipleSelectionPanel.hide();
    }
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

  _listenForText : function() {
    var textListener = function() {
      var selection = document.getSelection();
      this.text = '';
      var panel = this.widgets.selectionPanel;
      if (selection.type == 'Range' && selection.rangeCount == 1) {
        var range = selection.getRangeAt(0);
        var common = range.commonAncestorContainer;
        if (hui.dom.isDescendantOrSelf(common,this.root)) {
          this.text = hui.selection.getText();
          //this._suggest();
          var rects = range.getClientRects();
          if (rects.length > 0) {
            panel.show({target: rects[0]});
            return;
          }
        }
      }
      panel.hide();
    }.bind(this);
    hui.listen(document.body,'mouseup',textListener);
    hui.listen(window,'keyup',textListener);
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
  $click$highlightSelection : function() {
    appController.createStatementOnAddress(this.text);
    this.widgets.selectionPanel.hide();
  }
};

hui.ui.listen(documentController);