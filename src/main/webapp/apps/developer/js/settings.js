hui.ui.listen({
  $ready : function() {
    this._load();
    //hui.ui.get('data').listen(this);
    //hui.ui.get('data').refresh();
  },
  _load : function() {
    hui.ui.request({
      url: '/settings/data',
      method: 'GET',
      $object : this._setValues.bind(this)
    })
  },
  _setValues : function(values) {
    hui.ui.get('settings').setValues(values);
  },
  $valuesChanged$settings : function(values) {
    hui.ui.request({
      url: '/settings/data',
      method: 'POST',
      parameters : values
    })
  },
  $valueChanged$sporadicErrors : function() {
  },
  $objectsLoaded$data : function() {
    console.log(arguments)
  }
})
