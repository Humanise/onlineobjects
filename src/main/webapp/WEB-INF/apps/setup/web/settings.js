var controller = {
  $click$flushCache : function() {
    hui.ui.request({
      url : 'flushCache',
      $success : function() {
        hui.ui.msg.success({text:'Flushed'})
      }
    })
  }
}