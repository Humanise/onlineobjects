var controller = {
  $ready : function() {
    this._load();
  },
  $click$flushCache : function() {
    hui.ui.request({
      url : 'flushCache',
      $success : function() {
        hui.ui.msg.success({text:'Flushed'})
      }
    })
  },
  _load : function() {
    hui.ui.request({
      url : '/settings/data',
      $object : function(data) {
        console.log(data);
      }
    })
  }
}