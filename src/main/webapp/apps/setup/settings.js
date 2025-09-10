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
      method: 'GET',
      $object : function(data) {
        hui.ui.get('settingsForm').setValues(data);
      }
    })
  },
  $submit$settingsForm : function() {
    hui.ui.request({
      url : '/settings/data',
      method: 'POST',
      parameters : hui.ui.get('settingsForm').getValues(),
      $success : function() {
        hui.ui.msg.success({text: 'Saved'})
      },
      $failure : function() {
        hui.ui.msg.fail({text: 'Not good'})
      }
    })
    
  }
}