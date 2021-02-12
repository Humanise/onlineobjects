var documentController = {

  text: undefined,

  widgets : {
    selectionPanel: null
  },
  $ready : function() {
    this.root = hui.ui.get('internetaddress').element;
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
      panel.clear();
      var self = this
      items.forEach(function(item) {
        panel.add(hui.ui.Button.create({text: item.info.description, listen: {
          $click : function() {
            self._inspectItem(item);
            panel.hide();
          }
        }}))
      })
      panel.show({target: e.element})
    }
  },
  _inspectItem : function(item) {
    if (['Statement'].indexOf(item.info.type) !== -1) {
      appController.show(item.info)
    } else if (item.info.type = 'Link') {
      window.open(item.info.url)
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
  }
};

hui.ui.listen(documentController);