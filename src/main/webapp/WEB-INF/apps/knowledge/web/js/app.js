hui.ui.listen({
  _active: null,

  $click$addButton : function(button) {
    hui.ui.get('addPanel').show({target: button.getElement()})
  },

  $select$list : function(item) {
    if (!item) { return; }
    if (item.kind == 'InternetAddress') {
      this._loadAddress(item);
    } else {
      this._loadPerspective(item);
    }
  },
  _loadPerspective : function(perspective) {
    hui.ui.request({
      url: '/app/' + perspective.kind.toLowerCase(),
      parameters: {
        id: perspective.id
      },
      $object : this._onPerspective.bind(this)
    })
  },
  _onPerspective : function(data) {
    hui.ui.get('article').hide();
    var fragment = hui.ui.get('perspective');
    fragment.clear();
    fragment.add(hui.build('h1',{text: data.text}))
    fragment.show();
  },
  _loadAddress : function(address) {
    hui.ui.request({
      url: '/app/internetaddress',
      parameters: {
        id: address.id
      },
      $object : this._onArticle.bind(this)
    })
  },
  _onArticle : function(data) {
    hui.ui.get('perspective').hide();
    this._active = data;
    var fragment = hui.ui.get('article');
    fragment.show();
    fragment.setHTML('<div class="page">' + data.formatted + '</div>');
    hui.ui.get('favorite').setValue(data.favorite);
    hui.ui.get('inbox').setValue(data.inbox);
  },
  $valueChanged$favorite : function(value) {
    hui.ui.request({
      url: '/changeFavoriteStatus',
      parameters: {id: this._active.id, favorite: value},
      $success: function() {
        hui.ui.get('list').refresh();
      }
    })
  }
})