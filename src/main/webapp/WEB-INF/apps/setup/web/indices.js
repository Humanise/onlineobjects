hui.ui.listen({

  publisherId : null,

  $open$list : function(row) {
    this._loadPublisher(row.id);
  },
  $select$list : function(obj) {
    this._updateViewer(obj);
  },
  $select$selection : function(obj) {
    if (obj) {
      this._updateViewer(obj.value);
    }
  },
  _updateViewer : function(name) {
    if (!name) {
      contentHeader.setText();
      contentViewer.setHTML('');
      return;
    }
    return;
    contentHeader.setText(name);
    var html = '<div class="viewer">';
    html+='<h1>'+name+'</h1>';
    html+='</div>';
    contentViewer.setHTML(html);

    hui.ui.request({
      url : 'getIndexStatistics',
      parameters : {name: name},
      $object : function(result) {
        hui.log(result)
        contentViewer.setHTML('<div class="viewer">'+result.rendering+'</div>');
      }.bind(this)
    });
  },
  _buildHTML : function(obj) {
    return html;
  }
})