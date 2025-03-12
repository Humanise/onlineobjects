hui.control({
  '#name' : 'internetAddress',

  nodes: {
    intel: '.id-internetaddress-intel',
    head: '.js-internetaddress-head',
    text: '.js-internetaddress-text',
    formatted: '.js-internetaddress-formatted',
    selectionDetails: '.id-selection-details'
  },
  components: {
    words: 'internetaddressWords',
    tags: 'internetaddressTags',
    related: 'internetaddressRelated',
    selectionPanel: 'selectionPanel',
    tagSelectedText: 'tagSelectedText',
    multipleSelectionPanel: 'multipleSelectionPanel',
  },

  render : function() {
    this.reset();
    this._clean();
    var data = appController.getCurrentItem();
    if (data.url) {
      hui.build('a', {href:data.url, text: data.url, target: '_blank', parent: this.nodes.head});
    }

    var html = data.formatted;
    if (!html) {
      html = '<h1>'+hui.string.escape(data.title || 'Loading...')+'<h1>';
    }
    this.nodes.formatted.innerHTML = html;
    
    this.nodes.text.innerText = data.text || '';

    this.components.words.setData(data.words);
    this.components.tags.setData(data.tags);
    this.fetchRelated();
  },

  _clean : function() {
    this.nodes.intel.innerText = '';
    this.nodes.intel.style.display = 'none';
    this.nodes.head.innerHTML = '';    
  },

  text: undefined,

  $ready : function() {
    this.root = hui.find('.js-internetaddress-formatted');

    hui.on(this.nodes.selectionDetails, 'mousedown', (e) => {e.stopPropagation()});
    hui.on(this.nodes.selectionDetails, 'mouseup', (e) => {e.stopPropagation()});
    
    hui.listen(this.root,'click',this._click.bind(this));
    this._listenForText();
  },
  $$afterResize : function() {
    this._adjustSelectionPanel();
  },
  reset: function() {
    if (this.components.selectionPanel) {
      this.components.selectionPanel.hide();
      this.components.multipleSelectionPanel.hide();
    }
  },
  'viewMode.valueChanged!' : function(value) {
    hui.find('.js-internetaddress-formatted').style.display = (value == 'formatted' ? '' : 'none')
    hui.find('.js-internetaddress-text').style.display = (value == 'text' ? '' : 'none')
    this.reset();
  },
  'internetaddressWords.render!' : function(obj) {
    return appController._renderWord(obj);
  },
  'internetaddressTags.render!' : function(obj) {
    return appController._renderTag(obj);
  },
  'mainScroller.scrolled!' : function() {
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
      var panel = this.components.multipleSelectionPanel;
      hui.ui.get('multipleSelectionItems').setData(items);
      panel.show({target: e.getElement()});
    }
  },
  'multipleSelectionItems.render!' : function(item) {
    return hui.build('div.multiple_selection_item', {text: item.info.description});
  },
  'multipleSelectionItems.select!' : function(e) {
    this._inspectItem(e.data);
    this.components.multipleSelectionPanel.hide();
  },
  _inspectItem : function(item) {
    if (['Statement'].indexOf(item.info.type) !== -1) {
      appController.show(item.info);
    } else if (item.info.type == 'Link') {
      window.open(item.info.url);
    }
  },

  _adjustSelectionPanel : function() {
    var panel = this.components.selectionPanel;
    if (panel && panel.isVisible()) {
      this._respondToTextSelection();
    }
  },
  _respondToTextSelection : function() {
    var selection = document.getSelection();
    this._text = '';
    if (selection.type == 'Range' && selection.rangeCount == 1) {
      var range = selection.getRangeAt(0);
      var common = range.commonAncestorContainer;
      if (hui.dom.isDescendantOrSelf(common,this.root)) {
        this._text = hui.selection.getText().trim();
        this._checkTextChange();
        if (this._text) {
          //this._suggest();
          var rects = range.getClientRects();
          if (rects.length > 0) {
            this.components.selectionPanel.show({target: rects[0]});
            return;
          }          
        }
      }
    }
    this._hideSelectionPanel();
  },
  _hideSelectionPanel : function() {
    var panel = this.components.selectionPanel;
    if (panel.isVisible()) {
      panel.hide();
      this._clearSelectionDetails();
    }    
  },
  
  _latestText : null,
  _checkTextChange : function() {
    if (this._latestText !== this._text) {
      this._clearSelectionDetails();
      this._latestText = this._text;
    }
  },

  _getSelectionRect : function() {
    var selection = document.getSelection();
    if (selection.type == 'Range' && selection.rangeCount == 1) {
      var range = selection.getRangeAt(0);
      var common = range.commonAncestorContainer;
      if (hui.dom.isDescendantOrSelf(common, this.root)) {
        this._text = hui.selection.getText().trim();
        if (this._text) {
          //this._suggest();
          var rects = range.getClientRects();
          if (rects.length > 0) {
            return rects[0];
          }
        }
      }
    }
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
    if (!this._text) { return; }
    hui.ui.request({
      url: '/app/suggest',
      parameters: {text: this._text},
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
  'highlightSelection.click!' : function(button) {
    var warn;
    if (this._text.length < 10) { warn = 'Really create such a short quote?' }
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
    appController.createStatementOnAddress(this._text);
    this.components.selectionPanel.hide();
  },
  'tagSelectedText.click!' : function(button) {
    var warn;
    if (this._text.length > 50) { warn = 'Really create such a long tag?' }
    if (this._text.length < 2) { warn = 'Really create such a short tag?' }
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
    appController.createTagOnCurrentItem(this._text);
    this.components.selectionPanel.hide();
  },
  _clearSelectionDetails : function() {
    var n = this.nodes.selectionDetails;
    n.innerText = '';
    n.style.display = 'none';
    this._defineLevel = 'simple';
  },
  'defineSelectedText.click!' : function(button) {
    this._define();
  },
  _define : function(params) {
    params = params || {};
    var url = '/app/intel/define?text=' + encodeURIComponent(this._text);
    if (params.detailed) {
      url += "&detailed=true";
    }
    this._defined = true;
    var panel = this.components.selectionPanel;
    var container = this.nodes.selectionDetails;
    container.innerText = 'Just a second...';
    container.style.display = '';
    panel.updatePosition();
    oo.intelligence.stream({url: url, $html: (s) => {
      container.innerHTML = s;
      panel.updatePosition();
    }, $finally: () => {
      if (params.detailed) {
        return;
      }
      var link = hui.build('a.intel_more.oo_link.oo_link-subtle', {text: 'more...', href: '#'});
      hui.on(link, 'click', (e) => {
        e.stopPropagation();
        e.preventDefault();
        this._define({detailed: true})
      });
      (container.lastElementChild || container).appendChild(link)
    }})
  },

  'click! summarizeInternetAddress' : function() {
    var item = appController.getCurrentItem();
    var url = '/app/internetaddress/intel/summarize?id=' + item.id;
    this._intel(url);
  },
  'click! pointsInternetAddress' : function() {
    var item = appController.getCurrentItem();
    var url = '/app/internetaddress/intel/points?id=' + item.id;
    this._intel(url);
  },
  'click! authorInternetAddress' : function() {
    var item = appController.getCurrentItem();
    var url = '/app/internetaddress/intel/people?id=' + item.id;
    this._intel(url);
  },
  _intel : function(url) {
    var text = '';
    var output = this.nodes.intel;
    output.innerText = 'Let me think...';
    output.style.display = '';
    oo.intelligence.stream({url: url, $html: (s) => {
      output.innerHTML = s;
    }});
  },

  fetchRelated : function() {
    if (!appController._base.intelligence) {
      return;
    }
    var item = appController.getCurrentItem();
    hui.ui.request({
      url: '/app/related',
      method: 'GET',
      parameters: {id : item.id},
      $object : function(data) {
        this.components.related.setData(data);
      }.bind(this)
    })
  },
  'internetaddressRelated.render!' : function(item) {
    return appController._render_relation(item);
    /*
    return hui.build('div.perspective_relation', {children:[
      hui.build('div.perspective_relation_title',{text: item.title})
    ]});*/
  },
  'internetaddressRelated.select!' : function(item) {
    appController.show(item.data);
  }
})


