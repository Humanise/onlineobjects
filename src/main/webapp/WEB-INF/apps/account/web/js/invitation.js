hui.ui.listen({
  $ready : function() {

  },
  $submit$invitationFormula : function(form) {
    var values = form.getValues();
    if (values.password!==values.passwordAgain) {
      return;
    }
    hui.ui.request({
      url : '/signUp',
      parameters : {
        username : values.username,
        password : values.password,
        code : this._getCode()
      },
      $object : function(response) {
        if (response.success) {
          hui.ui.alert({text:'It worked!'})
        } else {
          hui.ui.alert({title:'It failed!',text : response.description});
        }
      },
      $failure : function() {
        hui.ui.alert({text:'It failed!'})
      }
    })
  },
  _getCode : function() {
    var node = hui.find('[data-code]');
    return node ? node.getAttribute('data-code') : undefined;
  }
})